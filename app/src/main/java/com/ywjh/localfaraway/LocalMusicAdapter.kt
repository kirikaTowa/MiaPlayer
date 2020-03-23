package com.ywjh.localfaraway

import android.content.Context

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ywjh.localfaraway.LocalMusicBean


class LocalMusicAdapter(internal var context: Context, internal var mDatas: List<LocalMusicBean>) ://传入上下文和List
    RecyclerView.Adapter<LocalMusicAdapter.LocalMusicViewHolder>() {//kotlin设置数据源可以直接放里面




    //J1：写一个接口   含有一个方法获取  用于(获取)传入View以及位置
    interface OnItemClickListenerM {
        fun OnItemClick(view: View, position: Int)//点击位置的position传进去
    }
    //j2通过函数传递接口   创建接口对象实现
    internal lateinit var onItemClickListenerm: OnItemClickListenerM
    //j3 对象
    fun setOnItemClickListenerM(onItemClickListenerm: OnItemClickListenerM) {//该接口对象初始化
        this.onItemClickListenerm = onItemClickListenerm
    }


//v1先定义Viewholde内部类             这里定义的itemView是可以直接用的
    inner class LocalMusicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {//直接构造方法拿到View
        //var idTv: TextView 核心是拿到View

        var albumIg:ImageView?=null
        var songTv: TextView
        var singerTv: TextView
        var albumTv: TextView
        var timeTv: TextView




    //v2.用于显示到View界面上
        init {
            //idTv = itemView.findViewById(R.id.item_local_music_num)
            albumIg= itemView.findViewById(R.id.item_local_music_albumpic)
            songTv = itemView.findViewById(R.id.item_local_music_song)
            songTv = itemView.findViewById(R.id.item_local_music_song)
            singerTv = itemView.findViewById(R.id.item_local_music_singer)
            albumTv = itemView.findViewById(R.id.item_local_music_album)
            timeTv = itemView.findViewById(R.id.item_local_music_durtion)
        }
    }


    //1获取context上下文 创建holder对象 通过View创建holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocalMusicViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_local_music, parent, false)
        val holder = LocalMusicViewHolder(view)//内部类holder
         return holder//拿到holder也行
    }
//绑定界面1.创建ViewHolder方法进行赋值  2.kotlin惰性加载
//2.点击事件:java 监听外层view 找每条holder的记录
    //kotlin 定义

    //recycleView和listview不一样 未封装成型的点击事件类
    //在onbind中可以获取每项的itemview 设定一个接口 自己封装，调用onitemlistener



//2为子项赋值 每次回滚时执行实时富裕项目
    override fun onBindViewHolder(holder: LocalMusicViewHolder, position: Int) {
        var musicBean:LocalMusicBean ?=null
        musicBean = mDatas[position]
        // holder.idTv.setText(musicBean.getId())
        holder.songTv.setText(musicBean.getSong())
        holder.singerTv.setText(musicBean.getSinger())
        holder.albumTv.setText(musicBean.getAlbum())
        holder.timeTv.setText(musicBean.getDuration())

       if (musicBean.getthumb()!=null)
       holder.albumIg?.setImageBitmap(musicBean.getthumb())

        holder.itemView.setOnClickListener { //每项被点击时实现接口回调 回调的数据是v和position
            //监听父view  设置点击该view的 onItemClickListenerm赋予view和position
                v -> onItemClickListenerm?.OnItemClick(v, position)
        }
    }


//3计数
    override fun getItemCount(): Int {
        return mDatas.size
    }


}




