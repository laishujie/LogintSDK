package com.hithway.loginsdkhelper

import android.app.Activity
import android.content.Intent
import com.hithway.loginsdkhelper.helper.QqHelper
import com.hithway.loginsdkhelper.helper.WbHelper
import com.hithway.loginsdkhelper.helper.WxHelper
import java.lang.ref.WeakReference

/**
 * @author lai
 * @des 第三方集成管理帮助类
 */
class SocialSdkHelper private constructor(private val builder: Builder) {
    private val TAG = SocialSdkHelper::class.java.name

    //微信帮助类
    private var mWxHelper: WxHelper? = null
    //当前activity
    private var mActivity: WeakReference<Activity>? = null
    //qq帮助类
    private var mQqHelper: QqHelper? = null
    //微博帮助类
    private var mWbHelper: WbHelper? = null


    fun withActivity(activity: Activity): SocialSdkHelper {
        mActivity?.clear()
        this.mActivity = WeakReference(activity)
        return this
    }

    fun qq(): QqHelper {
        initQqHelper()
        return mQqHelper!!
    }

    private fun release(){
        mWbHelper?.onDestroy()
        mQqHelper?.onDestroy()
        mWxHelper?.onDestroy()
        mWbHelper=null
        mWxHelper=null
        mQqHelper=null
    }

    fun wb(): WbHelper {
        initWbHelper()
        return mWbHelper!!
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mQqHelper?.apply {
            onActivityResult(requestCode, resultCode, data)
        }
        mWbHelper?.apply {
            onActivityResult(requestCode, resultCode, data)
        }
    }

    fun wx(): WxHelper {
        initWxHelper()
        return mWxHelper!!
    }


    private fun initWxHelper() {
        release()
        mWxHelper = WxHelper(mActivity?.get()!!, getWxAppId(), "", getWxAppSecret())
    }

    private fun initQqHelper() {
        release()
        mQqHelper = QqHelper(mActivity?.get()!!, getQqAppId(), "", "")
    }

    private fun initWbHelper() {
        release()
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

    fun getWxHelper(): WxHelper? {
        return mWxHelper
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

}