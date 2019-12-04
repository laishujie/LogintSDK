package com.xmiles.finevideo

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.hithway.loginsdkhelper.bean.ShareObj
import com.hithway.loginsdkhelper.bean.QqShareObj
import com.hithway.loginsdkhelper.callback.PLATFORM
import com.hithway.loginsdkhelper.callback.SHARE_TAG
import com.xmiles.finevideo.utils.EasyPermissions
import com.xmiles.finevideo.utils.EasyPhoto
import com.xmiles.finevideo.utils.PermissionAlwaysDenyNotifier
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : AppCompatActivity() {

    private val mSocialSdkHelper by lazy { (application as App).getSocialSdkHelper() }
    private val title = "title"
    private val des = "描述描述描述描述描述描述描述描述描述描述描述描述"

    private var mShareTag: SHARE_TAG? = null

    // 创建EasyPhoto实例
    private val photo = EasyPhoto()
        .setCallback {
            val shareObj = when (mShareTag) {
                SHARE_TAG.QQ, SHARE_TAG.QZONE -> {
                    QqShareObj.buildLocalImageObj(des, it.absolutePath)
                }
                SHARE_TAG.WEIXIN, SHARE_TAG.WEIXIN_CIRCLE, SHARE_TAG.SINA_WB -> {
                    ShareObj.buildImageObj(
                        BitmapFactory.decodeFile(it.absolutePath),
                        des
                    )
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
            login(PLATFORM.WEI_XIN)
        }
        btn_qq.setOnClickListener {
            login(PLATFORM.QQ)
        }
        btn_wb.setOnClickListener {
            login(PLATFORM.WEI_BO)
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

            if (radioGShareMedia.checkedRadioButtonId == R.id.radioShareImage) {
                photo.selectPhoto(this)
                return@setOnClickListener
            }

            val shareObj = when (radioGShareMedia.checkedRadioButtonId) {
                R.id.radioShareText -> {
                    ShareObj.buildTextObj(title, des)
                }
                R.id.radioShareWeb -> {
                    if (mShareTag == SHARE_TAG.QQ || mShareTag == SHARE_TAG.QZONE) {
                        QqShareObj.buildWebObj(
                            title,
                            des,
                            "http://imgcache.qq.com/qzone/space_item/pre/0/66768.gif"
                            , "http://baidu.com"
                        )
                    } else {
                        ShareObj.buildWebObj(
                            title,
                            des,
                            BitmapFactory.decodeResource(resources, R.mipmap.send_img),
                            "http://baidu.com"
                        )
                    }
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


    private fun login(platform: PLATFORM) {
        mSocialSdkHelper.withActivity(this)
            .error {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
            .qqSuccessCallBack {
                text.text = it.toString()
            }
            .wxSuccessCallBack {
                text.text = it.toString()
            }
            .wbSuccessCallBack {
                text.text = it.toString()
            }
            .login(platform)
    }

    //TODO 微信平台规定 取消分享也算分享成功 https://open.weixin.qq.com/cgi-bin/announce?spm=a311a.9588098.0.0&action=getannouncement&key=11534138374cE6li&version=
    private fun share(shareTag: SHARE_TAG, shareObj: ShareObj) {
        mSocialSdkHelper.withActivity(this)
            .error {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
            .shareSuccess {
                Toast.makeText(this, "分享成功", Toast.LENGTH_LONG).show()
            }
            .share(shareTag, shareObj)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mSocialSdkHelper.onActivityResult(requestCode, resultCode, data)
    }
}
