package com.xmiles.finevideo.wxapi

import com.hithway.loginsdkhelper.SocialSdkHelper
import com.hithway.loginsdkhelper.helper.BaseWXEntryActivity
import com.xmiles.finevideo.App


class WXEntryActivity : BaseWXEntryActivity() {
    override fun getSocialSdkHelper(): SocialSdkHelper {
        return (application as App).getSocialSdkHelper()
    }

}