package com.lucas.netviewer.data

import java.io.Serializable

class MonitorData : Serializable {
    var id: Long = 0
    var duration: Long = 0
    var method: String? = null
    var url: String? = null
    var host: String? = null
    var path: String? = null
    var scheme: String? = null
    var protocol: String? = null
    var requestTime: String? = null
    var requestHeaders: String? = null
    var requestBody: String? = null
    var requestContentType: String? = null
    var responseCode: Int = 0
    var responseTime: String? = null
    var responseHeaders: String? = null
    var responseBody: String? = null
    var responseMessage: String? = null
    var responseContentType: String? = null
    var responseContentLength: Long? = null
    var errorMsg: String? = null
    var source: String? = null
}
