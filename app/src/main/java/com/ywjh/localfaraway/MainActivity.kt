package com.ywjh.localfaraway

import android.Manifest
import android.Manifest.permission.READ_PHONE_STATE
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView

import android.view.View
import android.widget.ImageView


import androidx.recyclerview.widget.LinearLayoutManager

import android.net.Uri
import android.provider.MediaStore

import java.util.Date;
import java.text.SimpleDateFormat

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable

import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream


class MainActivity : AppCompatActivity(), View.OnClickListener {
    //一:In
    //1.惰性加载最下面的当前歌曲播放控件
    internal lateinit var nextIv: ImageView
    internal lateinit var playIv: ImageView//惰性加载的好处  该使用时自然初始化 不同刚开始初始化
    internal lateinit var lastIv:ImageView//下一曲
    internal lateinit var singerTv: TextView//歌手
    internal lateinit var songTv:TextView//歌曲名
    internal lateinit var imageBm:ImageView
    var musicRv: RecyclerView? = null//Recycleview与另一个layout中的RecycleView做适配

    //2.初始化存放数据源
    //var mDates:MutableList<LocalMusicBean>?=null//泛型适配
    internal lateinit var mDatas: MutableList<LocalMusicBean>
    //3定义一个adapter做recycleview做适配
    private var adapterT: LocalMusicAdapter? = null
    //4.创建多媒体音乐播放器
    internal var mediaPlayer: MediaPlayer? = null
    //5.创建  记录  当前正在播放的音乐的位置
    internal var currnetPlayPosition = -1//正在播放的位置
    //6.定义进度条 用于记录暂停时的位置也可以做一个进度条让他显示出来
    internal var currentPausePositionInSong = 0

    //7.创建静态伴生对象 来进行权限读取
    companion object {
        //读写权限
        private val PERMISSIONS_STORAGE =
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_PHONE_STATE)
        //请求状态码
        private val REQUEST_PERMISSION_CODE = 1
    }


    //2.Core层
    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //1.设置主类LayOut
        setContentView(R.layout.activity_main)
        /* 2.如果获取到的当前机器版本大于安卓6.0
         判断是否含有了写文件的权限，如果没有则调用动态申请权限的代码，
         ActivityCompat.requestPermission方法的第一个参数是目标Activity,填写this即可，
         第二个参数是String[]字符数组类型的权限集，第三个即请求码：*/
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
           /* if (ActivityCompat.checkSelfPermission(//
                    this,//当前活动
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {*/
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE)
           /* }*/
        }


        initView()//调用方法初始化界面
        mediaPlayer = MediaPlayer()//新建对象媒体播放器 处于空闲状态(Idle)
        mDatas = ArrayList<LocalMusicBean>()//用到新建的数据集
        // mediaPlayer = new MediaPlayer();对应java初始化不用写new
        //2.1.1创建适配器对象
        adapterT = LocalMusicAdapter(this, mDatas)//1.传入adapter当前context上下文环境  2.传入集合
        //musicRv?.setAdapter(adapter)
        musicRv?.adapter = adapterT//2.1.2RecycleView设置适配器 adapter初始化赋值
        //置布局管理器2.2.1
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)//垂直滑动 选择不反转
        //musicRv?.setLayoutManager(layoutManager)
        musicRv?.layoutManager = layoutManager//2.2.2RecycleView设置布局 adapter的方法可以这样写
        loadLocalMusicDate()//2.3条目设定好了后 加载本地数据

        setEventListener()//2.4设置每一项的点击事件 进行监听设置
    }//结束create方法


    //权限方法回调
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (i in permissions.indices) {
                Log.i("MainActivity", "申请的权限为：" + permissions[i] + ",申请结果：" + grantResults[i])
            }
        }
    }
    //3.初始化定义的操控标签  与layout匹配
    fun initView()
    {
        imageBm=findViewById(R.id.local_music_bottom_iv_icon)
        nextIv=findViewById(R.id.local_music_bottom_iv_next)
        playIv = findViewById(R.id.local_music_bottom_iv_play)
        lastIv = findViewById(R.id.local_music_bottom_iv_last)
        singerTv = findViewById(R.id.local_music_bottom_tv_singer)
        songTv = findViewById(R.id.local_music_bottom_tv_song)
        musicRv = findViewById(R.id.local_music_rv)
        nextIv!!.setOnClickListener(this);
        lastIv!!.setOnClickListener(this);
        playIv!!.setOnClickListener(this)
    }
    //4.获取本地数据
    fun loadLocalMusicDate(){
        //加载本地存储中的音乐文件到mp3中
        //1，使用ContentResolver对象   获取contentprider 实现跨进程通信
        var resolver: ContentResolver =contentResolver//get方法 应该是有简写
        //2.获取本地音乐存储的Url地址  选择音乐Audio类
        var uri:Uri=MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        //uri=content://media/external/audio/media 这是音频媒体表的uri
        /* MediaStore.Audio.Artists.Albums
         打印它的所有的列：发现了album_art，它缓存专辑封面。所以现在我们知道需要找album_art*/
        //3.开始查询 新建cursor对象
        val cursor: Cursor? = resolver.query(uri, null, null, null, null)
        //4.遍历地址
        var id:Long=0
        while(cursor!!.moveToNext()){
            var song=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))//获取字段
            var singer=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))//获取字段
            var album=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))//获取字段
            id++
            val sid = id.toString()//将其转为string类型 计数keyid字段 扫描ID
            //添加   获取该音乐所在专辑的id 真实ID
            //所有专辑都有自己的ID 所以肯定不会空
            var albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ID));
            //得到播放路径
            var path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
            //long型时间长度  代表毫秒数
            var duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))

            var sdf = SimpleDateFormat("mm:ss")//转一下格式 java.txt包
            var time = sdf.format(Date(duration))
            //albumart文件夹专门用于存放专辑图片
            //组合找到当前前专辑所在Uri content://media/external/audio/albumart/4
            var albumUri:Uri ?=null
            print("查找前uri    "+albumUri+"   albumID"+albumId    )
            //I/System.out: 查找前uri    null   albumID81 发现这和id是不太一样的
            /*
            * id78  letter song 专辑79
            * id=89   起风了  专辑87
            * id=100  告白气球 专辑87*/
            albumUri= ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId)
            println("查找后uri"+albumUri)
            var thumb:Bitmap?=null//有些专辑没图片
            if (albumUri!=null)//找不到对应图片地址的bean传空值 在adapter设定若为空则 显示默认的
            {
                thumb=createThumbFromUir(resolver,albumUri)
            }

            //将同一行中的数据封装入对象
            val bean = LocalMusicBean(sid, song, singer, album, time, path,albumUri,thumb)
            mDatas?.add(bean)//MutableList才有add方法
            println("获取数据成功:" + bean.toString())
        }
        cursor.close();
        //数据源变化 ，提示适配器更新 //将测试数据去掉
        adapterT?.notifyDataSetChanged();
    }
//创建封面图片方法
    fun createThumbFromUir(res: ContentResolver, albumUri: Uri): Bitmap ?{
        var inF: InputStream? = null
        var bmp: Bitmap? = null
        try {
            inF = res.openInputStream(albumUri)
            val sBitmapOptions = BitmapFactory.Options()
            bmp = BitmapFactory.decodeStream(inF, null, sBitmapOptions)//流用stream直接图片用decodeResource
            inF!!.close()
        } catch (e: FileNotFoundException) {

        } catch (e: IOException) {
            e.printStackTrace()
        }

        return bmp
    }


    //5.设置RecycleView中的点击事件
    fun setEventListener() {
        /* 设置每一项的点击事件(onClick监听)*/
        adapterT!!.setOnItemClickListenerM(object : LocalMusicAdapter.OnItemClickListenerM{//静态接口  进入holder监听回调 通过回调得到View和position
            override fun OnItemClick(view: View, position: Int) {//拿到 View,position 重写接口方法 通过bean类
                currnetPlayPosition = position//获取当前位置编号 记录正在播放
                var musicbean:LocalMusicBean= (mDatas as ArrayList<LocalMusicBean>).get(position)//找到对应的该条bean记录
                playMusicPosition(musicbean)//设置底部栏
            }
        }
        )
    }
    //6.封装这个方法 供上一曲下一曲使用 避免冗余  根据传入对象播放音乐
    private fun playMusicPosition(musicbean: LocalMusicBean) {

        //设置底部栏目
        imageBm?.setImageBitmap(musicbean.getthumb())//取出对应的图片
        singerTv?.setText(musicbean.getSinger())
        songTv?.setText(musicbean.getSong())
        stopMusic()
        //重置多媒体播放器
        mediaPlayer!!.reset()//清除原本的点击地址，并且设置新的播放路径
        //                设置新的播放路径
        try {
            mediaPlayer!!.setDataSource(musicbean.getPath())//重新设置播放源
            playMusic()

        } catch (e: IOException) {
            e.printStackTrace();
        }
    }

    override fun onClick(v:View)
    {
        when(v.id){
            R.id.local_music_bottom_iv_last ->
            {
                if (currnetPlayPosition == 0) {
                    Toast.makeText(this, "已经是第一首了，没有上一曲！", Toast.LENGTH_SHORT).show()
                    return
                }
                currnetPlayPosition = currnetPlayPosition - 1
                val lastBean = mDatas[currnetPlayPosition]
                playMusicPosition(lastBean)
            }
            R.id.local_music_bottom_iv_next->
            {
                if (currnetPlayPosition == mDatas.size-1) {
                    Toast.makeText(this, "已经是第一首了，没有下一曲！", Toast.LENGTH_SHORT).show()
                    return
                }
                currnetPlayPosition = currnetPlayPosition +1
                val nextBean = mDatas[currnetPlayPosition]
                playMusicPosition(nextBean)
            }
            R.id.local_music_bottom_iv_play->
            {
                if(currnetPlayPosition==-1)
                {
                    //未选中音乐
                    Toast.makeText(this,"请选择要播放的音乐",Toast.LENGTH_SHORT).show()
                }
                else if (mediaPlayer?.isPlaying()!!)
                {
                    //此时处于播放状态 需要暂停音乐
                    pauseMusic()
                }
                else
                {
                    //此时没有音乐，开始播放
                    playMusic()
                }
            }
        }
    }


    private fun playMusic() {
        //两种情况
        //1，从暂停到播放
        //2.从停止到播放
        if (mediaPlayer != null && !mediaPlayer!!.isPlaying()){
            if(currentPausePositionInSong==0)//未存在暂停记录
            {
                try {

                    mediaPlayer!!.prepare()//先准备prepare就是让mediaplayer准备，准备好就播放 因为之前重置了
                    mediaPlayer!!.start()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            else
            {//暂停到播放
                mediaPlayer!!.seekTo(currentPausePositionInSong)//进度调整到那个位置
                mediaPlayer!!.start()
            }
            //设置点击"播放选项"变为"暂停选项"
            playIv?.setImageResource(R.mipmap.icon_pause)
        }
    }
    //增加继续播放下一首音乐
    private fun pauseMusic(){
        if (mediaPlayer != null && mediaPlayer!!.isPlaying()) {
            currentPausePositionInSong = mediaPlayer!!.getCurrentPosition()//记录播放位置
            mediaPlayer!!.pause()
            playIv?.setImageResource(R.mipmap.icon_play)
        }
    }
    //暂停播放音乐
    private fun stopMusic() {
        if (mediaPlayer != null) {//可以用let语法写
            currentPausePositionInSong = 0
            mediaPlayer!!.pause()
            mediaPlayer!!.seekTo(0)
            mediaPlayer!!.stop()
            playIv?.setImageResource(R.mipmap.icon_play)
        }
    }

    //活动销毁时停止音乐
    protected fun onDestory(){
        mediaPlayer?.release()
        super.onDestroy()
        stopMusic()
    }
}
