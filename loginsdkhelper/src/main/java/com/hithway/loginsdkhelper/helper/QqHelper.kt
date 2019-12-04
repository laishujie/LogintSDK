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
import java.util.ArrayList
import java.io.File


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
        if (shareTag == SHARE_TAG.QQ) {
            // 分享图文
            val params =
                buildCommonBundle(shareObj.title, shareObj.summary, shareObj.targetUrl)
            params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT)
            // 本地或网络路径
            if (!TextUtils.isEmpty(shareObj.thumbImagePath))
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, shareObj.thumbImagePath)

            mTencent.shareToQQ(getActivity(), params, mIShareListener)
        } else {
            val imageUrls = ArrayList<String>()
            val params = Bundle()
            params.putInt(
                QzoneShare.SHARE_TO_QZONE_KEY_TYPE,
                QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT
            )
            params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, shareObj.summary)
            params.putString(QzoneShare.SHARE_TO_QQ_TITLE, shareObj.title)
            params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, shareObj.targetUrl)

            if (!TextUtils.isEmpty(shareObj.thumbImagePath)) {
                imageUrls.add(shareObj.thumbImagePath!!)
            }
            params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imageUrls)
            mTencent.shareToQzone(getActivity(), params, mIShareListener)
        }
    }

    override fun shareImage(shareTag: SHARE_TAG, shareObj: ShareObj) {
        if (TextUtils.isEmpty(shareObj.thumbImagePath)) {
            error?.invoke("thumbImagePath 分享图片路径不能为空")
            return
        }
        if (!File(shareObj.thumbImagePath!!).isFile) {
            error?.invoke("分享图片必须是本地图片")
            return
        }
        if (shareTag == SHARE_TAG.QQ) {
            val params = Bundle()
            params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, shareObj.thumbImagePath)
            params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE)
            mTencent.shareToQQ(getActivity(), params, mIShareListener)
        } else if (shareTag == SHARE_TAG.QZONE) {
            val params = Bundle()
            params.putInt(
                QzoneShare.SHARE_TO_QZONE_KEY_TYPE,
                QzonePublish.PUBLISH_TO_QZONE_TYPE_PUBLISHMOOD
            )
            params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, shareObj.summary)
            val imageUrls = ArrayList<String>()
            imageUrls.add(shareObj.thumbImagePath!!)
            params.putStringArrayList(QzonePublish.PUBLISH_TO_QZONE_IMAGE_URL, imageUrls)
            mTencent.publishToQzone(getActivity(), params, mIShareListener)
        }
    }


    override fun shareMusic(shareTag: SHARE_TAG, shareObj: ShareObj) {
    }

    override fun shareOpenApp() {
    }

    private fun buildCommonBundle(
        title: String?,
        summary: String?,
        targetUrl: String?
    ): Bundle {
        val params = Bundle()
        if (!TextUtils.isEmpty(title))
            params.putString(QQShare.SHARE_TO_QQ_TITLE, title)
        if (!TextUtils.isEmpty(summary))
            params.putString(QQShare.SHARE_TO_QQ_SUMMARY, summary)
        if (!TextUtils.isEmpty(targetUrl))
            params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, targetUrl)
        return params
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