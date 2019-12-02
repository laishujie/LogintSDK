package com.xmiles.finevideo

import android.app.Application
import com.hithway.loginsdkhelper.SocialSdkHelper

class App : Application() {

   private val mSocialSdkHelper by lazy {
        SocialSdkHelper.Builder()
            .wxAppId("wxc57ad69c0388d70d")
            .wxAppSecret("934ea89a6a8d8b8d910849c49b29008a")
            .qqAppId("1107942266")
            .wbAppId("2751198159")
            .wbRedirectUrl("")
            .build()
    }

    fun getSocialSdkHelper():SocialSdkHelper{
        return mSocialSdkHelper
    }

    override fun onCreate() {
        super.onCreate()
    }
}