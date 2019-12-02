package com.hithway.loginsdkhelper

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.hithway.loginsdkhelper.bean.QQUserInfoResponse
import com.hithway.loginsdkhelper.bean.WBUserInfoResponse
import com.hithway.loginsdkhelper.bean.WXUserInfoResponse
import com.hithway.loginsdkhelper.callback.PLATFORM
import com.hithway.loginsdkhelper.helper.QqHelper
import com.hithway.loginsdkhelper.helper.WbHelper
import com.hithway.loginsdkhelper.helper.WxHelper
import java.lang.ref.WeakReference

class SocialSdkHelper private constructor(private val builder: Builder) {
    private val TAG = SocialSdkHelper::class.java.name

    //微信帮助类
    var mWxHelper: WxHelper? = null
    //平台
    var mPlatform: PLATFORM? = null
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
    var mQqHelper: QqHelper? = null
    //微博帮助类
    var mWbHelper: WbHelper? = null

    fun getErrorCallBack(): ((String) -> Unit?)? {
        return mErrorCallBack
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

    fun platform(platform: PLATFORM): SocialSdkHelper {
        this.mPlatform = platform
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


    fun login() {
        if (mActivity == null || mActivity?.get() == null) {
            Log.e(TAG, "请绑定activity")
            return
        }

        when (mPlatform) {
            PLATFORM.WEI_XIN -> {
                mWxHelper?.onDestroy()
                mWxHelper = WxHelper(mActivity?.get()!!, getWxAppId(), "", getWxAppSecret())
                mWxHelper?.login(mWxSuccessCallBack, mErrorCallBack)
            }
            PLATFORM.QQ -> {
                mQqHelper?.onDestroy()
                mQqHelper = QqHelper(mActivity?.get()!!, getQqAppId(), "", "")
                mQqHelper?.login(mQQSuccessCallBack, mErrorCallBack)
            }
            PLATFORM.WEI_BO -> {
                mQqHelper?.onDestroy()
                mWbHelper = WbHelper(mActivity?.get()!!, getWbAppId(), "", "", getWbRedirectUrl())
                mWbHelper?.login(mWbSuccessCallBack, mErrorCallBack)
            }
            else -> getErrorCallBack()?.invoke("请指定对应的平台")
        }
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