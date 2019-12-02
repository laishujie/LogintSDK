package com.hithway.loginsdkhelper.callback

interface IRequestHelper<T> {

    fun login(
        success: ((T) -> Unit?)?,
        error: ((String) -> Unit?)?
    )
}