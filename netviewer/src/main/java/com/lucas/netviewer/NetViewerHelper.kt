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

    private const val TAG = "NetViewerHelper"
    private var monitorDataInterface: MonitorDataInterface? = null
    var context: Context? = null
    private var port = 0
    var isPrintNetLog = true         // 是否直接打印网络日志
    var isOpenMonitor = true

    //有从来ASM修改字节码对OKHTTP进行hook用的
    val hookInterceptors = listOf(
        MonitorInterceptor(),
    )

    fun init(context: Context) {
        NetViewerHelper.context = context
        thread {
            val propertiesData = MonitorProperties().paramsProperties()
            // 此处可在配置文件中设置多个配置项，如：port
            port = propertiesData?.port?.toInt() ?: 0
        }
    }

    fun setMonitorDataInterface(monitorDataInterface: MonitorDataInterface) {
        this.monitorDataInterface = monitorDataInterface
    }

    fun dealMonitorData(monitorData: MonitorData) {
        if (isPrintNetLog) {
            Log.d(TAG, "dealMonitorData: " + GsonHelper.toJson(monitorData))
        }
        monitorDataInterface?.getMonitorData(monitorData)
    }

}