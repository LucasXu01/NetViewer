package com.lucas.netviewer

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.lucas.netviewer.data.MonitorData
import com.lucas.netviewer.utils.GsonHelper
import com.lucas.netviewer.utils.MonitorProperties
import com.lygttpod.monitor.interceptor.MonitorInterceptor
import kotlin.concurrent.thread


@SuppressLint("StaticFieldLeak")
object NetViewerHelper {

    const val TAG = "NetViewerHelper"
    var context: Context? = null
    var port = 0

    //有从来ASM修改字节码对OKHTTP进行hook用的
    val hookInterceptors = listOf(
        MonitorInterceptor(),
    )

    var isOpenMonitor = true


    fun init(context: Context) {
        NetViewerHelper.context = context
        thread {
            val propertiesData = MonitorProperties().paramsProperties()
            port = propertiesData?.port?.toInt() ?: 0
        }
    }


    fun dealMonitorData(monitorData: MonitorData) {
        Log.d(TAG, "dealMonitorData: " + GsonHelper.toJson(monitorData) )
    }




}