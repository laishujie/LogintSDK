package com.xmiles.finevideo

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hithway.loginsdkhelper.bean.ShareObj
import com.hithway.loginsdkhelper.bean.QqShareObj
import com.hithway.loginsdkhelper.callback.PLATFORM
import com.hithway.loginsdkhelper.callback.SHARE_TAG
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val mSocialSdkHelper by lazy { (application as App).getSocialSdkHelper() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_.setOnClickListener {
            login(PLATFORM.WEI_XIN)
        }
        btn_qq.setOnClickListener {
            login(PLATFORM.QQ)
        }
        btn_wb.setOnClickListener {
            login(PLATFORM.WEI_BO)
        }

        val title = "title"
        val des = "描述描述描述描述描述描述描述描述描述描述描述描述"


        share_btn.setOnClickListener {
            val tag = when (radioGSharePlatform.checkedRadioButtonId) {
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

            val shareObj = when (radioGShareMedia.checkedRadioButtonId) {
               /* R.id.radioShareText -> {
                    ShareObj.buildTextObj(title, des)
                }
                R.id.radioShareImage -> {
                    ShareObj.buildImageObj(
                        BitmapFactory.decodeResource(
                            resources,
                            R.mipmap.send_img
                        )
                    )
                }*/
                R.id.radioShareWeb -> {
                    if (tag == SHARE_TAG.QQ || tag == SHARE_TAG.QZONE) {
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
               /* R.id.radioShareMusic -> {
                    ShareObj.buildMusicObj(
                        title,
                        des,
                        BitmapFactory.decodeResource(resources, R.mipmap.send_img),
                        "http://staff2.ustc.edu.cn/~wdw/softdown/index.asp/0042515_05.ANDY.mp3"
                    )
                }
                R.id.radioShareVideo -> {
                    ShareObj.buildVideoObj(
                        title,
                        des,
                        BitmapFactory.decodeResource(resources, R.mipmap.send_img),
                        "http://www.qq.com"
                    )
                }*/

                else -> null
            }


            if (tag != null && shareObj != null)
                share(tag, shareObj)
        }
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
