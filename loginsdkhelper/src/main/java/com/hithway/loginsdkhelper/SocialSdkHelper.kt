package com.hithway.loginsdkhelper

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.hithway.loginsdkhelper.bean.QQUserInfoResponse
import com.hithway.loginsdkhelper.bean.ShareObj
import com.hithway.loginsdkhelper.bean.WBUserInfoResponse
import com.hithway.loginsdkhelper.bean.WXUserInfoResponse
import com.hithway.loginsdkhelper.callback.PLATFORM
import com.hithway.loginsdkhelper.callback.SHARE_TAG
import com.hithway.loginsdkhelper.callback.SHARE_TYPE
import com.hithway.loginsdkhelper.helper.QqHelper
import com.hithway.loginsdkhelper.helper.WbHelper
import com.hithway.loginsdkhelper.helper.WxHelper
import java.lang.ref.WeakReference

class SocialSdkHelper private constructor(private val builder: Builder) {
    private val TAG = SocialSdkHelper::class.java.name

    //微信帮助类
    private var mWxHelper: WxHelper? = null
    //统一错误的回调
    private var mErrorCallBack: ((String) -> Unit?)? = null
    //当前activity
    private var mActivity: WeakReference<Activity>? = null
    //微信成功回调
    private var mWxSuccessCallBack: ((WXUserInfoResponse) -> Unit?)? = null
    //qq成功回调
    private var mQQSuccessCallBack: ((QQUserInfoResponse) -> Unit?)? = null
    //微博回调
    private var mWbSuccessCallBack: ((WBUserInfoResponse) -> Unit?)? = null
    //qq帮助类
    private var mQqHelper: QqHelper? = null
    //微博帮助类
    private var mWbHelper: WbHelper? = null

    private var mShareSuccessCallBack: (() -> Unit?)? = null


    fun getErrorCallBack(): ((String) -> Unit?)? {
        return mErrorCallBack
    }

    fun getShareSuccessCallBack(): (() -> Unit?)? {
        return mShareSuccessCallBack
    }


    fun shareSuccess(share: () -> Unit): SocialSdkHelper {
        this.mShareSuccessCallBack = share
        return this
    }

    fun error(error: (errorCallBack: String) -> Unit): SocialSdkHelper {
        this.mErrorCallBack = error
        return this
    }

    fun getWxAppId(): String? {
        return builder.getWxAppId()
    }

    fun getWbAppId(): String? {
        return builder.getWbAppId()
    }

    fun getWbRedirectUrl(): String? {
        return builder.getWbRedirectUrl()
    }

    fun getWxAppSecret(): String? {
        return builder.getWxAppSecret()
    }

    fun getQqAppId(): String? {
        return builder.getQqAppId()
    }

    fun withActivity(activity: Activity): SocialSdkHelper {
        mActivity?.clear()
        this.mActivity = WeakReference(activity)
        return this
    }


    fun wxSuccessCallBack(success: (successCallBack: WXUserInfoResponse) -> Unit): SocialSdkHelper {
        this.mWxSuccessCallBack = success
        return this
    }

    fun wbSuccessCallBack(success: (successCallBack: WBUserInfoResponse) -> Unit): SocialSdkHelper {
        this.mWbSuccessCallBack = success
        return this
    }

    fun qqSuccessCallBack(success: (successCallBack: QQUserInfoResponse) -> Unit): SocialSdkHelper {
        this.mQQSuccessCallBack = success
        return this
    }

    fun getWxSuccessCallBack(): ((successCallBack: WXUserInfoResponse) -> Unit?)? {
        return mWxSuccessCallBack
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mQqHelper?.apply {
            onActivityResult(requestCode, resultCode, data)
        }
        mWbHelper?.apply {
            onActivityResult(requestCode, resultCode, data)
        }
    }


    fun login(platform: PLATFORM) {
        if (mActivity == null || mActivity?.get() == null) {
            Log.e(TAG, "请绑定activity")
            return
        }

        when (platform) {
            PLATFORM.WEI_XIN -> {
                initWxHelper()
                mWxHelper?.login(mWxSuccessCallBack, mErrorCallBack)
            }
            PLATFORM.QQ -> {
                initQqHelper()
                mQqHelper?.login(mQQSuccessCallBack, mErrorCallBack)
            }
            PLATFORM.WEI_BO -> {
                initWbHelper()
                mWbHelper?.login(mWbSuccessCallBack, mErrorCallBack)
            }
        }
    }



    fun share(shareTag: SHARE_TAG, shareObj: ShareObj) {
        when (shareTag) {
            SHARE_TAG.WEIXIN, SHARE_TAG.WEIXIN_CIRCLE -> {
                initWxHelper()
                mWxHelper?.share(shareTag, shareObj, mShareSuccessCallBack, mErrorCallBack)
            }
            SHARE_TAG.QQ,SHARE_TAG.QZONE->{
                initQqHelper()
                mQqHelper?.share(shareTag, shareObj, mShareSuccessCallBack, mErrorCallBack)
            }
            SHARE_TAG.SINA_WB->{
                initWbHelper()
                mWbHelper?.share(shareTag, shareObj, mShareSuccessCallBack, mErrorCallBack)
            }
        }
    }

    private fun initWxHelper() {
        mWxHelper?.onDestroy()
        mWxHelper = WxHelper(mActivity?.get()!!, getWxAppId(), "", getWxAppSecret())
    }

    private fun initQqHelper() {
        mQqHelper?.onDestroy()
        mQqHelper = QqHelper(mActivity?.get()!!, getQqAppId(), "", "")
    }

    private fun initWbHelper(){
        mWbHelper?.onDestroy()
        mWbHelper = WbHelper(mActivity?.get()!!, getWbAppId(), "", "", getWbRedirectUrl())
    }


    class Builder {
        private var wxAppId: String? = null
        private var wxAppSecret: String? = null

        private var qqAppId: String? = null

        private var wbAppId: String? = null
        private var wbRedirectUrl: String? = null


        fun wbRedirectUrl(wbRedirectUrl: String): Builder {
            this.wbRedirectUrl = wbRedirectUrl
            return this
        }

        fun wbAppId(wbAppId: String): Builder {
            this.wbAppId = wbAppId
            return this
        }

        fun getWbAppId(): String? {
            return wbAppId
        }

        fun getWbRedirectUrl(): String? {
            return wbRedirectUrl
        }

        fun wxAppId(wxAppId: String): Builder {
            this.wxAppId = wxAppId
            return this
        }

        fun qqAppId(qqAppId: String): Builder {
            this.qqAppId = qqAppId
            return this
        }


        fun getQqAppId(): String? {
            return qqAppId
        }

        fun getWxAppId(): String? {
            return wxAppId
        }

        fun getWxAppSecret(): String? {
            return wxAppSecret
        }

        fun wxAppSecret(wxAppSecret: String): Builder {
            this.wxAppSecret = wxAppSecret
            return this
        }

        fun build(): SocialSdkHelper {
            return SocialSdkHelper(this)
        }
    }

}