package com.xmiles.finevideo

import android.app.Application
import com.hithway.loginsdkhelper.SocialSdkHelper

class App : Application() {

   private val mSocialSdkHelper by lazy {
        SocialSdkHelper.Builder()
            .wxAppId("")
            .wxAppSecret("")
            .qqAppId("")
            .wbAppId("")
            .wbRedirectUrl("")
            .build()
    }

    fun getSocialSdkHelper():SocialSdkHelper{
        return mSocialSdkHelper
    }

}