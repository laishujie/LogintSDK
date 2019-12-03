package com.hithway.loginsdkhelper.helper

import android.app.Activity
import android.text.TextUtils
import com.hithway.loginsdkhelper.R
import com.hithway.loginsdkhelper.SocialUtil
import com.hithway.loginsdkhelper.bean.ShareObj
import com.hithway.loginsdkhelper.bean.WXUserInfoResponse
import com.hithway.loginsdkhelper.callback.SHARE_TAG
import com.tencent.mm.opensdk.modelmsg.*
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage
import com.tencent.mm.opensdk.modelmsg.WXImageObject
import com.tencent.mm.opensdk.modelmsg.WXMusicObject
import com.tencent.mm.opensdk.modelmsg.WXVideoObject


class WxHelper(activity: Activity, appId: String?, appKey: String?, appSecret: String?) :
    BaseSdkHelper<WXUserInfoResponse>(activity, appId, appKey, appSecret) {


    private val api: IWXAPI = WXAPIFactory.createWXAPI(activity, appId, true)

    init {
        api.registerApp(appId)
    }


    /*基本信息验证*/
    private fun baseVerify(activity: Activity): Boolean {
        if (TextUtils.isEmpty(appId) || TextUtils.isEmpty(appSecret)) {
            error?.invoke(activity.getString(R.string.wx_appid_error))
            return false
        }
        if (!api.isWXAppInstalled) {
            error?.invoke(activity.getString(R.string.wx_uninstall))
            return false
        }
        return true
    }

    override fun login() {
        getActivity()?.also {
            if (baseVerify(it)) {
                val req = SendAuth.Req()
                req.scope = "snsapi_userinfo"
                req.state = SocialUtil.getAppStateName(it) + "_app"
                api.sendReq(req)
            }
        }
    }


    override fun shareText(shareTag: SHARE_TAG, shareObj: ShareObj) {
        val textObj = WXTextObject()
        textObj.text = shareObj.summary
        val msg = WXMediaMessage()
        msg.mediaObject = textObj
        msg.title = shareObj.title
        msg.description = shareObj.summary
        sendMsgToWx(msg, shareTag, "text")
    }

    override fun shareWeb(shareTag: SHARE_TAG, shareObj: ShareObj) {
        if (shareObj.thumbImageBitmap == null || TextUtils.isEmpty(shareObj.targetUrl)) {
            error?.invoke("thumbImageBitmap 或 targetUrl 为空")
            return
        }
        val webPage = WXWebpageObject()
        webPage.webpageUrl = shareObj.targetUrl
        val msg = WXMediaMessage(webPage)
        msg.title = shareObj.title
        msg.description = shareObj.summary
        msg.thumbData = SocialUtil.bmpToByteArray(shareObj.thumbImageBitmap, true)
        sendMsgToWx(msg, shareTag, "web")
    }

    override fun shareImage(shareTag: SHARE_TAG, shareObj: ShareObj) {
        if (shareObj.thumbImageBitmap == null) {
            error?.invoke("thumbImageBitmap 为空")
            return
        }
        // 文件大小不大于10485760  路径长度不大于10240
        val imgObj = WXImageObject(shareObj.thumbImageBitmap)
        val msg = WXMediaMessage()
        msg.mediaObject = imgObj
        msg.thumbData = SocialUtil.bmpToByteArray(shareObj.thumbImageBitmap, true)
        sendMsgToWx(msg, shareTag, "image")
    }

    override fun shareMusic(shareTag: SHARE_TAG, shareObj: ShareObj) {
        val music = WXMusicObject()
        if (shareObj.thumbImageBitmap == null || TextUtils.isEmpty(shareObj.targetUrl)) {
            error?.invoke("thumbImageBitmap 或 targetUrl 为空")
            return
        }
        music.musicUrl = shareObj.targetUrl
        val msg = WXMediaMessage()
        msg.mediaObject = music
        msg.title = shareObj.title
        msg.description = shareObj.summary
        msg.thumbData = SocialUtil.bmpToByteArray(shareObj.thumbImageBitmap, true)
        sendMsgToWx(msg, shareTag, "music")
    }

    override fun shareOpenApp() {
        val rst = api.openWXApp()
        if (rst) {
            shareSuccessCallBack?.invoke()
        } else {
            error?.invoke("open app error")
        }
    }

    private fun sendMsgToWx(msg: WXMediaMessage, shareTarget: SHARE_TAG, sign: String) {
        val req = SendMessageToWX.Req()
        req.transaction = SocialUtil.buildTransaction(sign)
        req.message = msg
        req.scene = getShareToWhere(shareTarget)
        val sendResult = api.sendReq(req)
        if (!sendResult) {
            error?.invoke("sendMsgToWx失败，可能是参数错误")
        }
    }

    override fun shareVideo(shareTag: SHARE_TAG, shareObj: ShareObj) {
        if (shareObj.isShareByIntent) {
            if (TextUtils.isEmpty(shareObj.mediaPath) || shareObj.thumbImageBitmap == null) {
                error?.invoke("mediaPath 或 thumbImageBitmap不能为空 ")
                return
            }
            val gameVideoFileObject = WXGameVideoFileObject()
            val path = shareObj.mediaPath
            gameVideoFileObject.filePath = path
            val msg = WXMediaMessage()
            msg.setThumbImage(shareObj.thumbImageBitmap)
            msg.title = shareObj.title
            msg.description = shareObj.summary
            msg.mediaObject = gameVideoFileObject
            sendMsgToWx(msg, shareTag, "appdata")
        } else if (!TextUtils.isEmpty(shareObj.targetUrl)) {
            if (shareObj.thumbImageBitmap == null) {
                error?.invoke("thumbImageBitmap不能为空 ")
                return
            }
            val video = WXVideoObject()
            video.videoUrl = shareObj.targetUrl
            val msg = WXMediaMessage(video)
            msg.title = shareObj.title
            msg.description = shareObj.summary
            msg.thumbData = SocialUtil.bmpToByteArray(shareObj.thumbImageBitmap, true)
            sendMsgToWx(msg, shareTag, "video")
        }
    }

    private fun getShareToWhere(shareTarget: SHARE_TAG): Int {
        return when (shareTarget) {
            SHARE_TAG.WEIXIN -> SendMessageToWX.Req.WXSceneSession
            SHARE_TAG.WEIXIN_CIRCLE -> SendMessageToWX.Req.WXSceneTimeline
            else -> SendMessageToWX.Req.WXSceneSession
        }
    }

}