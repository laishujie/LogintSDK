package com.hithway.loginsdkhelper.helper

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.hithway.loginsdkhelper.SocialSdkHelper
import com.hithway.loginsdkhelper.bean.WXUserInfoResponse
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException


/**
 *
 * @author  Lai
 *
 * @time 2019/11/30 11:42
 * @describe describe
 *
 */
abstract class BaseWXEntryActivity : AppCompatActivity(), IWXAPIEventHandler {

    private var api: IWXAPI? = null


    abstract fun getSocialSdkHelper(): SocialSdkHelper


    override fun onReq(p0: BaseReq?) {

    }

    override fun onResp(baseResp: BaseResp?) {
        baseResp?.apply {
            Log.d("WXEntryActivity", baseResp.errCode.toString() + baseResp.errStr)

            if (baseResp.type == ConstantsAPI.COMMAND_SENDAUTH) {
                when (baseResp.errCode) {
                    BaseResp.ErrCode.ERR_OK -> {
                        val appSecret = getSocialSdkHelper().getWxAppSecret()
                        val appId = getSocialSdkHelper().getWxAppId()
                        val code = (baseResp as SendAuth.Resp).code
                        val authUrl = StringBuilder()
                        authUrl
                            .append("https://api.weixin.qq.com/sns/oauth2/access_token?appid=")
                            .append(appId)
                            .append("&secret=")
                            .append(appSecret)
                            .append("&code=")
                            .append(code)
                            .append("&grant_type=authorization_code")
                        getAuth(authUrl.toString())
                    }
                    //用户拒绝授权
                    BaseResp.ErrCode.ERR_AUTH_DENIED -> {
                        getSocialSdkHelper().getErrorCallBack()?.invoke("拒绝授权")
                        finish()
                    }
                    BaseResp.ErrCode.ERR_USER_CANCEL -> {//用户取消
                        getSocialSdkHelper().getErrorCallBack()?.invoke("用户取消")
                        finish()
                    }
                    else -> finish()
                }
            }else if (baseResp.type == ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX) {
                // 取消分享也算分享成功 https://open.weixin.qq.com/cgi-bin/announce?spm=a311a.9588098.0.0&action=getannouncement&key=11534138374cE6li&version=
                when (baseResp.errCode) {
                    BaseResp.ErrCode.ERR_OK ->
                        getSocialSdkHelper().getShareSuccessCallBack()?.invoke()
                    // 分享成功
                    BaseResp.ErrCode.ERR_USER_CANCEL ->
                        // 分享取消
                        getSocialSdkHelper().getErrorCallBack()?.invoke("分享取消")
                    BaseResp.ErrCode.ERR_SENT_FAILED ->
                        // 分享失败
                        getSocialSdkHelper().getErrorCallBack()?.invoke("分享失败")
                    BaseResp.ErrCode.ERR_AUTH_DENIED ->
                        // 分享被拒绝
                        getSocialSdkHelper().getErrorCallBack()?.invoke("分享被拒绝")
                }
                finish()
            }
        }
    }

    private fun getAuth(url: String) {
        val client = OkHttpUtil.getInstance()
        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                runOnUiThread {
                    getSocialSdkHelper().getErrorCallBack()?.invoke("微信验证失败")
                }
            }

            override fun onResponse(call: Call?, response: Response?) {
                response?.body()?.string()?.apply {
                    val authObj = JSONObject(this)
                    val accessToken = authObj.getString("access_token")
                    val openId = authObj.getString("openid")
                    val userInfoUrl = StringBuilder()
                    userInfoUrl
                        .append("https://api.weixin.qq.com/sns/userinfo?access_token=")
                        .append(accessToken)
                        .append("&openid=")
                        .append(openId)
                        .append("&lang=")
                        .append("zh_CN")
                    getUserInfo(userInfoUrl.toString())
                }
            }

        })

    }


    /**
     * 获取用户信息
     * @param url 请求的地址
     */
    private fun getUserInfo(url: String) {
        val client = OkHttpUtil.getInstance()
        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                runOnUiThread {
                    getSocialSdkHelper().getErrorCallBack()?.invoke("获取用户信息失败")
                }
                finish()
            }

            override fun onResponse(call: Call?, response: Response?) {
                response?.body()?.string()?.apply {
                    val loginResponse = Gson().fromJson(this, WXUserInfoResponse::class.java)
                    runOnUiThread {
                        getSocialSdkHelper().getWxSuccessCallBack()
                            ?.invoke(loginResponse)
                        finish()
                    }
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        OkHttpUtil.cancelAll()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val wxAppId = getSocialSdkHelper().getWxAppId()

        api = WXAPIFactory.createWXAPI(this, wxAppId, true)
        api?.handleIntent(intent, this)
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        api?.handleIntent(intent, this)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }

}