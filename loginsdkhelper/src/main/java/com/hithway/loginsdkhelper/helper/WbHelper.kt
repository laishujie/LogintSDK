package com.hithway.loginsdkhelper.helper

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import com.hithway.loginsdkhelper.SocialUtil
import com.hithway.loginsdkhelper.bean.ShareObj
import com.hithway.loginsdkhelper.bean.WBUserInfoResponse
import com.hithway.loginsdkhelper.callback.SHARE_TAG
import com.sina.weibo.sdk.WbSdk
import com.sina.weibo.sdk.api.ImageObject
import com.sina.weibo.sdk.api.TextObject
import com.sina.weibo.sdk.api.WebpageObject
import com.sina.weibo.sdk.api.WeiboMultiMessage
import com.sina.weibo.sdk.auth.*
import com.sina.weibo.sdk.auth.sso.SsoHandler
import com.sina.weibo.sdk.share.WbShareCallback
import com.sina.weibo.sdk.share.WbShareHandler
import com.sina.weibo.sdk.utils.LogUtil
import com.sina.weibo.sdk.utils.Utility
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.Util
import java.io.IOException
import java.net.URL

class WbHelper(
    activity: Activity,
    appId: String?,
    appKey: String?,
    appSecret: String?,
    redirectUrl: String?
) :
    BaseSdkHelper<WBUserInfoResponse>(activity, appId, appKey, appSecret) {

     val THUMB_IMAGE_SIZE = 32 * 1024

    override fun shareWeb(shareTag: SHARE_TAG, shareObj: ShareObj) {
        if(shareObj.thumbImageBitmap==null){
            error?.invoke("thumbImageBitmap 为空")
            return
        }
        val multiMessage = WeiboMultiMessage()
        multiMessage.mediaObject =
            getWebObj(shareObj, SocialUtil.bmpToByteArray(shareObj.thumbImageBitmap, true))
        getActivity()?.apply {
            sendWeiboMultiMsg(this, multiMessage)
        }
    }

    override fun shareImage(shareTag: SHARE_TAG, shareObj: ShareObj) {
        if(shareObj.thumbImageBitmap==null){
            error?.invoke("thumbImageBitmap 为空")
            return
        }

        val thumbData = SocialUtil.bmpToByteArray(shareObj.thumbImageBitmap, true)

        if (thumbData != null && thumbData.size > THUMB_IMAGE_SIZE) {
            error?.invoke("图片太大，分享失败。")
            return
        }
        getActivity()?.apply {
            val multiMessage = WeiboMultiMessage()
            multiMessage.imageObject = getImageObj(shareObj.thumbImagePath, thumbData)
            multiMessage.textObject = getTextObj(shareObj.summary!!)
            sendWeiboMultiMsg(this, multiMessage)
        }
    }

    override fun shareMusic(shareTag: SHARE_TAG, shareObj: ShareObj) {
    }

    override fun shareOpenApp() {
    }

    override fun shareText(shareTag: SHARE_TAG, shareObj: ShareObj) {
        if(TextUtils.isEmpty(shareObj.summary)){
            error?.invoke("shareObj.summary is null")
            return
        }
        getActivity()?.apply {
            val multiMessage = WeiboMultiMessage()
            multiMessage.textObject = getTextObj(shareObj.summary!!)
            sendWeiboMultiMsg(this, multiMessage)
        }
    }


    private fun getImageObj(localPath: String?, data: ByteArray?): ImageObject? {
        if (TextUtils.isEmpty(localPath) || data == null) return null

        val imageObject = ImageObject()
        //设置缩略图。 注意：最终压缩过的缩略图大小不得超过 32kb。
        imageObject.imageData = data
        imageObject.imagePath = localPath
        return imageObject
    }

    private fun getTextObj(summary: String): TextObject? {
        if (TextUtils.isEmpty(summary)) return null

        val textObject = TextObject()
        textObject.text = summary
        return textObject
    }

    override fun shareVideo(shareTag: SHARE_TAG, shareObj: ShareObj) {
    }

    private var initOk = false

    private var mSsoHandler: SsoHandler? = null

    init {
        initOk = if (TextUtils.isEmpty(appId) || TextUtils.isEmpty(redirectUrl)) {
            false
        } else {
            WbSdk.install(
                activity.applicationContext,
                AuthInfo(activity.applicationContext, appId, redirectUrl, "")
            )
            true
        }
    }

    override fun share(
        shareTag: SHARE_TAG,
        shareObj: ShareObj,
        success: (() -> Unit?)?,
        error: ((String) -> Unit?)?
    ) {
        if(!initOk){
            error?.invoke("appId 或者 redirectUrl 为空")
            return
        }
        super.share(shareTag, shareObj, success, error)
    }

    fun getUserInfo(accessToken: Oauth2AccessToken) {
        val url =
            URL("https://api.weibo.com/2/users/show.json?access_token=" + accessToken.token + "&uid=" + accessToken.uid + "")
        val client = OkHttpUtil.getInstance()
        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                getActivity()?.apply {
                    runOnUiThread {
                        error?.invoke("获取用户信息失败")
                    }
                }
            }

            override fun onResponse(call: Call?, response: Response?) {
                getActivity()?.apply {
                    response?.also {
                        val info = Gson().fromJson<WBUserInfoResponse>(
                            it.body().string(),
                            WBUserInfoResponse::class.java
                        )
                        runOnUiThread {
                            success?.invoke(info)
                        }
                    }
                }
            }
        })
    }

    private fun getWebObj(obj: ShareObj, thumbData: ByteArray): WebpageObject {
        val mediaObject = WebpageObject()
        mediaObject.identify = Utility.generateGUID()
        mediaObject.title = obj.title
        mediaObject.description = obj.summary
        // 注意：最终压缩过的缩略图大小不得超过 32kb。
        mediaObject.thumbData = thumbData
        mediaObject.actionUrl = obj.targetUrl
        mediaObject.defaultText = obj.summary
        return mediaObject
    }

    private var mWbShareHandler: WbShareHandler? = null


    private fun sendWeiboMultiMsg(activity: Activity, message: WeiboMultiMessage?) {
        if (mWbShareHandler == null) {
            mWbShareHandler = WbShareHandler(activity)
            mWbShareHandler?.registerApp()
        }
        if (mWbShareHandler != null && message != null) {
            mWbShareHandler?.shareMessage(message, false)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mSsoHandler?.authorizeCallBack(requestCode, resultCode, data)
        mWbShareHandler?.doResultIntent(data, object : WbShareCallback {
            override fun onWbShareFail() {
                error?.invoke("分享失败")
            }

            override fun onWbShareCancel() {
                error?.invoke("分享取消")
            }

            override fun onWbShareSuccess() {
                error?.invoke("分享成功")
            }
        })
    }

    override fun login() {
        getActivity()?.apply {
            if (initOk) {
                mSsoHandler = SsoHandler(getActivity())
                mSsoHandler?.authorize(object : WbAuthListener {
                    override fun onSuccess(oauth2AccessToken: Oauth2AccessToken?) {
                        Log.e("11111", "thread " + Thread.currentThread() + "")
                        oauth2AccessToken?.also {
                            if (oauth2AccessToken.isSessionValid) {
                                AccessTokenKeeper.writeAccessToken(getActivity(), oauth2AccessToken)
                                getUserInfo(oauth2AccessToken)
                            } else {
                                //handler.sendEmptyMessage(GET_INFO_ERROR)
                                error?.invoke("获取token失败")
                            }
                        }
                    }

                    override fun onFailure(p0: WbConnectErrorMessage?) {
                        p0?.apply {
                            error?.invoke(errorMessage)
                        }
                    }

                    override fun cancel() {
                        Log.e("11111", "thread " + Thread.currentThread() + "")
                        error?.invoke("已取消")
                    }

                })
            } else {
                error?.invoke("appId 或者 redirectUrl 为空")
            }
        }
    }
}