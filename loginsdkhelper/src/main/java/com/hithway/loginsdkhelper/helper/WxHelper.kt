package com.hithway.loginsdkhelper.helper

import android.app.Activity
import android.text.TextUtils
import com.hithway.loginsdkhelper.R
import com.hithway.loginsdkhelper.SocialUtil
import com.hithway.loginsdkhelper.bean.WXUserInfoResponse
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory

class WxHelper(activity: Activity, appId: String?, appKey: String?,appSecret: String?) :
    BaseSdkHelper<WXUserInfoResponse>(activity, appId, appKey,appSecret) {

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
}