package com.hithway.loginsdkhelper.bean

import com.hithway.loginsdkhelper.callback.SHARE_TYPE

class QqShareObj(shareType: SHARE_TYPE) : ShareObj(shareType) {
    companion object{
        // 分享web，打开链接
        // 分享web，打开链接
        fun buildWebObj(
            title: String, summary: String,thumbImagePath:String, targetUrl: String
        ): ShareObj {
            val shareMediaObj = ShareObj(SHARE_TYPE.SHARE_TYPE_WEB)
            shareMediaObj.init(title, summary, thumbImagePath, targetUrl)
            return shareMediaObj
        }
    }
}