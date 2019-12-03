package com.hithway.loginsdkhelper.helper

import android.app.Activity
import android.content.Intent
import com.hithway.loginsdkhelper.bean.ShareObj
import com.hithway.loginsdkhelper.callback.IRequestHelper
import com.hithway.loginsdkhelper.callback.SHARE_TAG
import com.hithway.loginsdkhelper.callback.SHARE_TYPE
import java.lang.ref.WeakReference

/**
 *
 * @author  Lai
 *
 * @time 2019/12/1 17:52
 * @describe describe
 *
 */
abstract class BaseSdkHelper<T>(
    activity: Activity,
    val appId: String?,
    val appKey: String?,
    val appSecret: String?
) :
    IRequestHelper<T> {

    private val mActivityWeakReference: WeakReference<Activity> = WeakReference(activity)
    var success: ((T) -> Unit?)? = null
    var error: ((String) -> Unit?)? = null
    var shareSuccessCallBack: (() -> Unit?)? = null

    fun getActivity(): Activity? {
        return mActivityWeakReference.get()
    }

    override fun login(success: ((T) -> Unit?)?, error: ((String) -> Unit?)?) {
        this.success = success
        this.error = error
        login()
    }


    open fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

    }

    override fun share(
        shareTag: SHARE_TAG,
        shareObj: ShareObj,
        success: (() -> Unit?)?,
        error: ((String) -> Unit?)?
    ) {
        this.shareSuccessCallBack = success
        this.error = error
        share(shareTag, shareObj)
    }

    abstract fun login()

    open fun logout(activity: Activity) {}


    fun onDestroy() {
        mActivityWeakReference.clear()
        destroy()
    }

    protected open fun destroy(){}

   protected open fun share(shareTag: SHARE_TAG, shareObj: ShareObj) {
        when (shareObj.shareType) {
            SHARE_TYPE.SHARE_TYPE_TEXT -> {
                shareText(shareTag, shareObj)
            }
            SHARE_TYPE.SHARE_OPEN_APP -> {
                shareOpenApp()
            }
            SHARE_TYPE.SHARE_TYPE_MUSIC -> {
                shareMusic(shareTag, shareObj)
            }
            SHARE_TYPE.SHARE_TYPE_IMAGE -> {
                shareImage(shareTag, shareObj)
            }
            SHARE_TYPE.SHARE_TYPE_WEB -> {
                shareWeb(shareTag, shareObj)
            }
            SHARE_TYPE.SHARE_TYPE_VIDEO -> {
                shareVideo(shareTag, shareObj)
            }
        }
    }

    abstract fun shareWeb(shareTag: SHARE_TAG, shareObj: ShareObj)

    abstract fun shareImage(shareTag: SHARE_TAG, shareObj: ShareObj)

    abstract fun shareMusic(shareTag: SHARE_TAG, shareObj: ShareObj)

    abstract fun shareOpenApp()

    abstract fun shareText(shareTag: SHARE_TAG, shareObj: ShareObj)

    abstract fun shareVideo(shareTag: SHARE_TAG, shareObj: ShareObj)
}
