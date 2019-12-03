package com.hithway.loginsdkhelper.callback

import com.hithway.loginsdkhelper.bean.ShareObj

interface IRequestHelper<T> {

    fun login(
        success: ((T) -> Unit?)?,
        error: ((String) -> Unit?)?
    )

    fun share(
        shareTag: SHARE_TAG,
        shareObj: ShareObj,
        success: (()->Unit?)?,
        error: ((String) -> Unit?)?
    )
}