package com.lygttpod.monitor.interceptor

import android.net.Uri
import android.util.Log
import com.lucas.netviewer.NetViewerHelper
import com.lucas.netviewer.data.MonitorData
import com.lucas.netviewer.utils.*
import com.lucas.netviewer.utils.isProbablyUtf8
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import okio.GzipSource
import java.io.EOFException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.min

class MonitorInterceptor : Interceptor {
    companion object {
        private const val TAG = "MonitorInterceptor"
    }

    private var maxContentLength = 5L * 1024 * 1024

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!NetViewerHelper.isOpenMonitor) {
            return chain.proceed(request)
        }
        val monitorData = MonitorData()
        monitorData.method = request.method
        val url = request.url.toString()
        monitorData.url = url
        if (url.isNotBlank()) {
            val uri = Uri.parse(url)
            monitorData.host = uri.host
            monitorData.path = uri.path + if (uri.query != null) "?" + uri.query else ""
            monitorData.scheme = uri.scheme
        }

        val requestBody = request.body
        monitorData.requestTime = Date().formatData(TIME_LONG)

        requestBody?.contentType()?.let { monitorData.requestContentType = it.toString() }

        val startTime = System.nanoTime()
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            monitorData.errorMsg = e.toString()
            insert(monitorData)
            throw e
        }
        try {
            monitorData.requestHeaders = response.request.headers.toJsonString()

            val contentType = response.headers["Content-Type"]
            monitorData.responseTime = Date().formatData(TIME_LONG)
            monitorData.duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
            monitorData.protocol = response.protocol.toString()
            monitorData.responseCode = response.code
            monitorData.responseMessage = response.message

            when {
                requestBody == null || bodyHasUnknownEncoding(request.headers) || requestBody.isDuplex() || requestBody.isOneShot() -> {
                }
                else -> {
                    val buffer = Buffer()
                    requestBody.writeTo(buffer)
                    val charset: Charset =
                        requestBody.contentType()?.charset(StandardCharsets.UTF_8)
                            ?: StandardCharsets.UTF_8
                    if (buffer.isProbablyUtf8()) {
                        monitorData.requestBody = buffer.readString(charset)
                    }
                }
            }

            val responseBody = response.body

            responseBody?.let { body ->
                body.contentType()?.let { monitorData.responseContentType = it.toString() }
            }

            val bodyHasUnknownEncoding = bodyHasUnknownEncoding(response.headers)

            if (responseBody != null && response.promisesBody() && !bodyHasUnknownEncoding) {
                val source = responseBody.source()
                source.request(Long.MAX_VALUE) // Buffer the entire body.
                var buffer = source.buffer

                if (bodyGzipped(response.headers)) {
                    GzipSource(buffer.clone()).use { gzippedResponseBody ->
                        buffer = Buffer()
                        buffer.writeAll(gzippedResponseBody)
                    }
                }

                val charset: Charset = responseBody.contentType()?.charset(StandardCharsets.UTF_8)
                    ?: StandardCharsets.UTF_8

                if (responseBody.contentLength() != 0L && buffer.isProbablyUtf8()) {
                    val body = readFromBuffer(buffer.clone(), charset)
                    monitorData.responseBody = formatBody(body, monitorData.responseContentType)
                }
                monitorData.responseContentLength = buffer.size
            }
            insert(monitorData)
            return response
        } catch (e: Exception) {
            Log.d("MonitorHelper", e.message ?: "")
            return response
        }
    }

    private fun insert(monitorData: MonitorData) {
        NetViewerHelper.dealMonitorData(monitorData)
    }

    private fun bodyHasUnknownEncoding(headers: Headers): Boolean {
        val contentEncoding = headers["Content-Encoding"] ?: return false
        return !contentEncoding.equals("identity", ignoreCase = true) &&
                !contentEncoding.equals("gzip", ignoreCase = true)
    }

    private fun readFromBuffer(buffer: Buffer, charset: Charset?): String {
        val bufferSize = buffer.size
        val maxBytes = min(bufferSize, maxContentLength)
        var body: String = try {
            buffer.readString(maxBytes, charset!!)
        } catch (e: EOFException) {
            "\\n\\n--- Unexpected end of content ---"
        }
        if (bufferSize > maxContentLength) {
            body += "\\n\\n--- Content truncated ---"
        }
        return body
    }

    private fun bodyGzipped(headers: Headers): Boolean {
        return "gzip".equals(headers["Content-Encoding"], ignoreCase = true)
    }

}