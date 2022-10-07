package com.lucas.netviewer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.lucas.netviewer.data.MonitorData
import okhttp3.*
import okio.IOException

class MainActivity : AppCompatActivity() {
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val request = Request.Builder().url("https://www.wanandroid.com/article/list/0/json").build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string()
                runOnUiThread {
                    Toast.makeText(this@MainActivity, result, Toast.LENGTH_SHORT).show()
                }
            }
        })

        // 在主项目中获取NetworkViewer中的每次网络请求数据monitorData
        NetViewerHelper.setMonitorDataInterface(object  : MonitorDataInterface{
            override fun getMonitorData(monitorData: MonitorData) {
                Log.d("tag", "getMonitorData: ." + monitorData.host)
            }
        })
    }
}