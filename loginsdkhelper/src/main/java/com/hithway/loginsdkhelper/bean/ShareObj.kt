package com.hithway.loginsdkhelper.bean

import android.graphics.Bitmap
import java.io.Serializable
import android.os.Parcelable
import com.hithway.loginsdkhelper.callback.SHARE_TYPE


open class ShareObj(val shareType: SHARE_TYPE) : Serializable {

    val TAG = ShareObj::class.java.simpleName

    // title 标题，如果不设置为app name
    var title: String? = null
    // 概要，描述，desc
    var summary: String? = null
    var thumbImageBitmap: Bitmap? = null
    // 启动url，点击之后指向的url，启动新的网页
    var targetUrl: String? = null
    // 资源url,音视频播放源
    var mediaPath: String? = null
    // 音视频时间
    var duration = 10
    // 附加信息
    var extraTag: Parcelable? = null
    // 新浪分享带不带文字
    var isSinaWithSummary = true
    // 新浪分享带不带图片
    var isSinaWithPicture = false
    // 使用本地 intent 打开，分享本地视频用
    var isShareByIntent = false

    var thumbImagePath: String? = null

    companion object {
        // 分享文字，qq 好友原本不支持，使用intent兼容
        fun buildTextObj(title: String, summary: String): ShareObj {
            val shareMediaObj = ShareObj(SHARE_TYPE.SHARE_TYPE_TEXT)
            shareMediaObj.title = title
            shareMediaObj.summary = summary
            return shareMediaObj
        }



        fun buildWebObj(
            title: String, summary: String, thumbImageBitmap: Bitmap, targetUrl: String
        ): ShareObj {
            val shareMediaObj = ShareObj(SHARE_TYPE.SHARE_TYPE_WEB)
            shareMediaObj.init(title, summary, thumbImageBitmap, targetUrl)
            return shareMediaObj
        }

        // 分享图片
        fun buildImageObj(path: Bitmap): ShareObj {
            val shareMediaObj = ShareObj(SHARE_TYPE.SHARE_TYPE_IMAGE)
            shareMediaObj.thumbImageBitmap = (path)
            return shareMediaObj
        }

        // 分享图片，带描述，qq微信好友会分为两条消息发送
        fun buildImageObj(bitmap: Bitmap, summary: String): ShareObj {
            val shareMediaObj = ShareObj(SHARE_TYPE.SHARE_TYPE_IMAGE)
            shareMediaObj.thumbImageBitmap = bitmap
            shareMediaObj.summary = summary
            return shareMediaObj
        }

        // 分享音乐,qq空间不支持，使用web分享
        fun buildMusicObj(
            title: String,
            summary: String,
            thumbImageBitmap: Bitmap,
            targetUrl: String
        ): ShareObj {
            val shareMediaObj = ShareObj(SHARE_TYPE.SHARE_TYPE_MUSIC)
            shareMediaObj.init(title, summary, thumbImageBitmap, targetUrl)
            return shareMediaObj
        }

        // 分享视频，
        // 本地视频使用 intent 兼容，qq 空间本身支持本地视频发布
        // 支持网络视频
        fun buildVideoObj(
            title: String,
            summary: String,
            thumbImageBitmap: Bitmap,
            targetUrl: String
        ): ShareObj {
            val shareMediaObj = ShareObj(SHARE_TYPE.SHARE_TYPE_VIDEO)
            shareMediaObj.init(title, summary, thumbImageBitmap, targetUrl)
            return shareMediaObj
        }

        // 本地视频
        fun buildVideoObj(title: String, summary: String, localVideoPath: String): ShareObj {
            val shareMediaObj = ShareObj(SHARE_TYPE.SHARE_TYPE_VIDEO)
            shareMediaObj.mediaPath = (localVideoPath)
            shareMediaObj.isShareByIntent = (true)
            shareMediaObj.title = (title)
            shareMediaObj.summary = (summary)
            return shareMediaObj
        }

    }

    fun init(title: String, summary: String, thumbImageBitmap: Bitmap, targetUrl: String) {
        this.title = title
        this.summary = summary
        this.thumbImageBitmap = thumbImageBitmap
        this.targetUrl = targetUrl
    }

    fun init(title: String, summary: String, thumbImagePath: String, targetUrl: String) {
        this.title = title
        this.summary = summary
        this.thumbImagePath = thumbImagePath
        this.targetUrl = targetUrl
    }

}