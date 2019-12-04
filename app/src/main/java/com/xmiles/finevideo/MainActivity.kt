package com.xmiles.finevideo

import android.Manifest
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.hithway.loginsdkhelper.bean.QqShareObj
import com.hithway.loginsdkhelper.bean.ShareObj
import com.hithway.loginsdkhelper.callback.SHARE_TAG
import com.xmiles.finevideo.utils.EasyPermissions
import com.xmiles.finevideo.utils.EasyPhoto
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val mSocialSdkHelper by lazy { (application as App).getSocialSdkHelper() }
    private val title = "title"
    private val des = "描述描述描述描述描述描述描述描述描述描述描述描述"
    private val tagUrl = "https://baidu.com"
    private var mShareTag: SHARE_TAG? = null

    private var isShareImage = false

    // 创建EasyPhoto实例
    private val photo = EasyPhoto()
        .setCallback {
            val shareObj = when (mShareTag) {
                SHARE_TAG.QQ, SHARE_TAG.QZONE -> {
                    if (isShareImage) {
                        QqShareObj.buildLocalImageObj(des, it.absolutePath)
                    } else {
                        QqShareObj.buildWebObj(
                            title,
                            des,
                            it.absolutePath,
                            tagUrl
                        )
                    }
                }
                SHARE_TAG.WEIXIN, SHARE_TAG.WEIXIN_CIRCLE, SHARE_TAG.SINA_WB -> {
                    if (isShareImage) {
                        ShareObj.buildImageObj(
                            BitmapFactory.decodeFile(it.absolutePath),
                            des
                        )
                    } else {
                        ShareObj.buildWebObj(
                            title,
                            des,
                            BitmapFactory.decodeFile(it.absolutePath),
                            tagUrl
                        )
                    }
                }
                else -> null
            }

            if (shareObj != null)
                share(mShareTag!!, shareObj)
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions()

        btn_.setOnClickListener {
            mSocialSdkHelper
                .withActivity(this)
                .wx()
                .login({
                    dialogText(it.toString())
                }, {
                    dialogText(it)
                })
        }
        btn_qq.setOnClickListener {
            mSocialSdkHelper.withActivity(this)
                .qq().login({
                    dialogText(it.toString())
                }, {
                    dialogText(it)
                })
        }

        btn_wb.setOnClickListener {
            mSocialSdkHelper.withActivity(this)
                .wb().login({
                    dialogText(it.toString())
                }, {
                    dialogText(it)
                })
        }


        share_btn.setOnClickListener {
            mShareTag = when (radioGSharePlatform.checkedRadioButtonId) {
                R.id.radioShareWX -> {
                    SHARE_TAG.WEIXIN
                }
                R.id.radioShareWXCircle -> {
                    SHARE_TAG.WEIXIN_CIRCLE
                }
                R.id.radioShareQQ -> {
                    SHARE_TAG.QQ
                }
                R.id.radioShareQZone -> {
                    SHARE_TAG.QZONE
                }
                R.id.radioShareSinaWB -> {
                    SHARE_TAG.SINA_WB
                }
                else -> null
            }

            isShareImage = radioGShareMedia.checkedRadioButtonId == R.id.radioShareImage

            if (radioGShareMedia.checkedRadioButtonId == R.id.radioShareImage
                || radioGShareMedia.checkedRadioButtonId == R.id.radioShareWeb
            ) {
                photo.selectPhoto(this)
                return@setOnClickListener
            }


            val shareObj = when (radioGShareMedia.checkedRadioButtonId) {
                R.id.radioShareText -> {
                    ShareObj.buildTextObj(title, des)
                }
                else -> null
            }

            if (mShareTag != null && shareObj != null)
                share(mShareTag!!, shareObj)
        }
    }

    private fun requestPermissions() {
        EasyPermissions.create(// 指定待申请权限
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_CALENDAR,
            Manifest.permission.WRITE_CONTACTS
        )
            // 定制权限申请说明弹窗
            .rational { permission, chain ->
                AlertDialog.Builder(this)
                    .setTitle("权限申请说明")
                    .setMessage("应用需要此权限：\n$permission")
                    .setNegativeButton("拒绝") { _, _ -> chain.cancel() }
                    .setPositiveButton("同意") { _, _ -> chain.process() }
                    .show()
                return@rational true
            }
            // 设置授权结果回调
            .callback { grant ->

            }
            // 发起请求
            .request(this)
    }

    private fun dialogText(str: String) {
        AlertDialog.Builder(this)
            .setMessage(str)
            .show()
    }

    //TODO 微信平台规定 取消分享也算分享成功 https://open.weixin.qq.com/cgi-bin/announce?spm=a311a.9588098.0.0&action=getannouncement&key=11534138374cE6li&version=
    private fun share(shareTag: SHARE_TAG, shareObj: ShareObj) {
        when (shareTag) {
            SHARE_TAG.WEIXIN_CIRCLE, SHARE_TAG.WEIXIN -> {
                mSocialSdkHelper.withActivity(this)
                    .wx().share(shareTag, shareObj, {
                        dialogText("分享成功")
                    }, {
                        dialogText(it)
                    })
            }
            SHARE_TAG.QQ, SHARE_TAG.QZONE -> {
                mSocialSdkHelper.withActivity(this)
                    .qq().share(shareTag, shareObj, {
                        dialogText("分享成功")
                    }, {
                        dialogText(it)
                    })
            }
            SHARE_TAG.SINA_WB -> {
                mSocialSdkHelper.withActivity(this)
                    .wb().share(shareTag, shareObj, {
                        dialogText("分享成功")
                    }, {
                        dialogText(it)
                    })
            }
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mSocialSdkHelper.onActivityResult(requestCode, resultCode, data)
    }
}
