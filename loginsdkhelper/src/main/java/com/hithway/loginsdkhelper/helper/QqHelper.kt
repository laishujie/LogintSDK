package com.hithway.loginsdkhelper.helper

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import com.google.gson.Gson
import com.hithway.loginsdkhelper.bean.QQLoginResultEntity
import com.hithway.loginsdkhelper.bean.QQUserInfoResponse
import com.hithway.loginsdkhelper.callback.SHARE_TAG
import com.tencent.connect.UserInfo
import com.tencent.tauth.IUiListener
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError
import com.tencent.connect.share.QzoneShare
import com.tencent.connect.share.QzonePublish
import android.os.Bundle
import com.hithway.loginsdkhelper.SocialUtil
import com.hithway.loginsdkhelper.bean.ShareObj
import com.tencent.connect.share.QQShare




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
    override fun shareWeb(shareTag: SHARE_TAG, shareObj: ShareObj) {

    }

    override fun shareImage(shareTag: SHARE_TAG, shareObj: ShareObj) {
        if(shareTag == SHARE_TAG.QQ){

        }

        /*if (shareTag === Target.SHARE_QQ_FRIENDS) {
            // 可以兼容分享图片和gif
            val params = buildCommonBundle("", shareMediaObj.getSummary(), "", shareTarget)
            params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE)
            params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, shareMediaObj.getThumbImagePath())
            mTencentApi.shareToQQ(activity, params, mIUiListenerWrap)
        } else if (shareTarget === Target.SHARE_QQ_ZONE) {
            val params = Bundle()
            params.putInt(
                QzoneShare.SHARE_TO_QZONE_KEY_TYPE,
                QzonePublish.PUBLISH_TO_QZONE_TYPE_PUBLISHMOOD
            )
            params.putString(
                QzoneShare.SHARE_TO_QQ_SUMMARY,
                shareMediSHARE_REQ_CODEaObj.getSummary()
            )
            val imageUrls = ArrayList()
            imageUrls.add(shareMediaObj.getThumbImagePath())
            params.putStringArrayList(QzonePublish.PUBLISH_TO_QZONE_IMAGE_URL, imageUrls)
            mTencentApi.publishToQzone(activity, params, mIUiListenerWrap)
        }*/

    }

    override fun shareMusic(shareTag: SHARE_TAG, shareObj: ShareObj) {
    }

    override fun shareOpenApp() {
    }


    override fun share(
        shareTag: SHARE_TAG,
        shareObj: ShareObj,
        success: (() -> Unit?)?,
        error: ((String) -> Unit?)?
    ) {
        initShareListener()
        super.share(shareTag, shareObj, success, error)
    }

    override fun shareText(shareTag: SHARE_TAG, shareObj: ShareObj) {
        if (shareTag === SHARE_TAG.QQ) {
            getActivity()?.apply {
                shareTextByIntent(
                    this,
                    shareObj,
                    SocialUtil.QQ_PKG,
                    SocialUtil.QQ_FRIENDS_PAGE
                )
            }
        } else if (shareTag === SHARE_TAG.QZONE) {
            val params = Bundle()
            params.putInt(
                QzoneShare.SHARE_TO_QZONE_KEY_TYPE,
                QzonePublish.PUBLISH_TO_QZONE_TYPE_PUBLISHMOOD
            )
            params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, shareObj.summary)
            mTencent.publishToQzone(getActivity(), params, mIShareListener)
        }
    }


    private fun initShareListener() {
        mIShareListener = object : IUiListener {
            override fun onComplete(p0: Any?) {
                getActivity()?.runOnUiThread {
                    shareSuccessCallBack?.invoke()
                }
            }

            override fun onCancel() {
                getActivity()?.runOnUiThread {
                    error?.invoke("分享取消")
                }
            }

            override fun onError(p0: UiError?) {
                getActivity()?.runOnUiThread {
                    error?.invoke("分享失败 ")
                }
            }
        }
    }


    fun shareTextByIntent(activity: Activity, obj: ShareObj, pkg: String, page: String) {
        try {
            IntentShareUtils.shareText(activity, obj.title, obj.summary, pkg, page)
        } catch (e: Exception) {
            error?.invoke("跳转失败")
        }

    }

    override fun shareVideo(shareTag: SHARE_TAG, shareObj: ShareObj) {
    }


    private val mTencent: Tencent = Tencent.createInstance(appId, activity.applicationContext)

    private var mILoginListener: IUiListener? = null
    private var mIUserListener: IUiListener? = null
    private var mIShareListener: IUiListener? = null

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

    override fun destroy() {
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mILoginListener?.apply {
            Tencent.onActivityResultData(requestCode, resultCode, data, this)
        }
        mIShareListener?.apply {
            Tencent.onActivityResultData(requestCode, resultCode, data, this)
        }
    }


}