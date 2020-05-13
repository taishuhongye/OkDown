package com.znb.okdown

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.ok.down.DownLoadHttpUtils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val header = HashMap<String, String>()
        header["ua"] = "your dad"
        var tvTotal = ""
        btn_down.setOnClickListener {
            DownLoadHttpUtils.getInstance().setActionCallBack({
                tvTotal = it.toString()
                Log.d(TAG, "total : $it")
                Log.d(TAG, "Thread : ${Thread.currentThread().name}")
            }, {
                tv_progress.text = "$it/$tvTotal"
                Log.d(TAG, "progress : $it")
                Log.d(TAG, "Thread : ${Thread.currentThread().name}")
            }, {
                tv_progress.text = "$it"
                Log.d(TAG, "success : $it")
                Log.d(TAG, "Thread : ${Thread.currentThread().name}")
            }, {
                Log.d(TAG, "error : $it")
                Log.d(TAG, "Thread : ${Thread.currentThread().name}")
            })
                .addHeader(header)
                .initUrl(
                    "https://download.sj.qq.com/upload/connAssitantDownload/upload/MobileAssistant_1.apk",
                    null
                )
                .setFilePath(applicationContext.filesDir.absolutePath)
                .setFileName("app_baby.apk")//皮
                .down()
        }
    }
}
