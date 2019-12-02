package com.hithway.loginsdkhelper.helper

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import com.google.gson.Gson
import com.hithway.loginsdkhelper.bean.QQLoginResultEntity
import com.hithway.loginsdkhelper.bean.QQUserInfoResponse
import com.tencent.connect.UserInfo
import com.tencent.tauth.IUiListener
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError


/**
 *
 * @author  Lai
 *
 * @time 2019/12/1 17:29
 * @describe describe
 *
 */
class QqHelper(activity: Activity, appId: String?, appKey: String?, appSecret: String?) :
    BaseSdkHelper<QQUserInfoResponse>(activity, appId, appKey, appSecret) {

    private val mTencent: Tencent = Tencent.createInstance(appId, activity.applicationContext)

    private var mILoginListener: IUiListener? = null
    private var mIUserListener: IUiListener? = null


    private fun initLoginListener() {
        mILoginListener = object : IUiListener {
            override fun onComplete(p0: Any?) {
                p0?.also { it ->
                    val unionIdInfo =
                        Gson().fromJson(it.toString(), QQLoginResultEntity::class.java)
                    getUserInfo(unionIdInfo)
                }
            }

            override fun onCancel() {
                error?.invoke("用户取消")
            }

            override fun onError(p0: UiError?) {
                p0?.apply {
                    error?.invoke(toString())
                }
            }
        }
    }

    private fun initUserInfoListener() {
        mIUserListener = object : IUiListener {
            override fun onComplete(p0: Any?) {
                p0?.apply {
                    val userInfo = Gson().fromJson<QQUserInfoResponse>(
                        toString(),
                        QQUserInfoResponse::class.java
                    )
                    success?.invoke(userInfo)
                }
            }

            override fun onCancel() {
                error?.invoke("取消获取用户QQ信息")
            }

            override fun onError(p0: UiError?) {
                error?.invoke("获取用户QQ信息错误")
            }

        }
    }

    override fun logout(activity: Activity) {
        if (!TextUtils.isEmpty(mTencent.accessToken))
            mTencent.logout(getActivity())
    }
    //

    private fun getUserInfo(loginResult: QQLoginResultEntity) {
        mTencent.setAccessToken(loginResult.access_token, loginResult.expires_in)
        mTencent.openId = loginResult.openid
        if (!mTencent.isSessionValid) {
            return
        }

        val mQQToken = mTencent.qqToken
        initUserInfoListener()
        val userInfo = UserInfo(getActivity(), mQQToken)
        userInfo.getUserInfo(mIUserListener)
    }

    override fun login() {
        getActivity()?.apply {
            if (mTencent.isQQInstalled(this)) {
                if (!mTencent.isSessionValid) {
                    initLoginListener()
                    mTencent.login(this, "all", mILoginListener)
                } else {
                    val info = UserInfo(this, mTencent.qqToken)
                    initUserInfoListener()
                    info.getUserInfo(mIUserListener)
                }
            } else {
                error?.invoke("未安装qq")
            }

        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mILoginListener?.apply {
            Tencent.onActivityResultData(requestCode, resultCode, data, this)
        }
    }


}