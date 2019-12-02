package com.hithway.loginsdkhelper.helper

import android.app.Activity
import android.content.Intent
import com.hithway.loginsdkhelper.callback.IRequestHelper
import java.lang.ref.WeakReference

/**
 *
 * @author  Lai
 *
 * @time 2019/12/1 17:52
 * @describe describe
 *
 */
abstract class BaseSdkHelper<T>(activity: Activity, val appId: String?,val appKey: String?, val appSecret: String?) :
    IRequestHelper<T> {

    private val mActivityWeakReference: WeakReference<Activity> = WeakReference(activity)
    var success: ((T) -> Unit?)? = null
    var error: ((String) -> Unit?)? = null

    fun getActivity(): Activity? {
        return mActivityWeakReference.get()
    }

     override fun login(success: ((T) -> Unit?)?, error: ((String) -> Unit?)?) {
        this.success = success
        this.error = error
        login()
    }

    abstract fun login()

    open fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

    }

    open fun logout(activity: Activity){}

    fun onDestroy(){
        mActivityWeakReference.clear()
    }
}
