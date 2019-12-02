package com.xmiles.finevideo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hithway.loginsdkhelper.callback.PLATFORM
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
    }

    private fun login(platform: PLATFORM) {
        mSocialSdkHelper.withActivity(this)
            .platform(platform)
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
            .login()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mSocialSdkHelper.onActivityResult(requestCode, resultCode, data)
    }
}
