package com.hithway.loginsdkhelper.helper

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import com.hithway.loginsdkhelper.bean.ShareObj
import com.hithway.loginsdkhelper.bean.WBUserInfoResponse
import com.hithway.loginsdkhelper.callback.SHARE_TAG
import com.sina.weibo.sdk.WbSdk
import com.sina.weibo.sdk.auth.*
import com.sina.weibo.sdk.auth.sso.SsoHandler
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
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
    override fun shareWeb(shareTag: SHARE_TAG, shareObj: ShareObj) {

    }

    override fun shareImage(shareTag: SHARE_TAG, shareObj: ShareObj) {
    }

    override fun shareMusic(shareTag: SHARE_TAG, shareObj: ShareObj) {
    }

    override fun shareOpenApp() {
    }

    override fun shareText(shareTag: SHARE_TAG, shareObj: ShareObj) {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mSsoHandler?.authorizeCallBack(requestCode, resultCode, data)
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
            }else{
                error?.invoke("appId 或者 redirectUrl 为空")
            }
        }
    }
}