package com.ywjh.localfaraway

import android.graphics.drawable.BitmapDrawable
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.content.ContentResolver
import android.net.Uri


class LocalMusicBean {
    private var id: String? = null //歌曲id
    private var song: String? = null //歌曲名称
    private var singer: String? = null //歌手名称
    private var album: String? = null //专辑名称
    private var duration: String? = null //歌曲时长
    private var path: String? = null //歌曲路径

    private var albumUri: Uri ? = null ;//存储音乐封面的Uri地址
    private var thumb:Bitmap? = null ;//存储封面图片
    //构造方法
    constructor(id: String?, song: String?, singer: String?, album: String?, duration: String?, path: String?, albumUri: Uri ?,thumb:Bitmap?) {
        this.id = id
        this.song = song
        this.singer = singer
        this.album = album
        this.duration = duration
        this.path = path
        this.albumUri=albumUri
        this.thumb=thumb
    }

    constructor()
    fun getthumb(): Bitmap? {
        return thumb
    }

    fun setthumb(thumb: Bitmap) {
        this.thumb = thumb
    }
    fun getalbumUri(): Uri? {
        return albumUri
    }

    fun setalbumUri(albumUri: Uri) {
        this.albumUri = albumUri
    }

    fun getId(): String? {
        return id
    }

    fun setId(id: String) {
        this.id = id
    }

    fun getSong(): String? {
        return song
    }

    fun setSong(song: String) {
        this.song = song
    }

    fun getSinger(): String? {
        return singer
    }

    fun setSinger(singer: String) {
        this.singer = singer
    }

    fun getAlbum(): String? {
        return album
    }

    fun setAlbum(album: String) {
        this.album = album
    }

    fun getDuration(): String? {
        return duration
    }

    fun setDuration(duration: String) {
        this.duration = duration
    }

    fun getPath(): String? {
        return path
    }

    fun setPath(path: String) {
        this.path = path
    }

    override fun toString(): String {
        return "LocalMusicBean(id=$id, song=$song, singer=$singer, album=$album, duration=$duration, path=$path)"
    }



}