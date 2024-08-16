package com.comet.server.web.util

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class WebUtil {

    companion object {
        val okHttpClient = OkHttpClient()

        suspend fun request(url: String, contentType: String, body: String?): Response {
            // body는 일단 get요청만 하므로 미구현.
            val request = Request.Builder().url(url).addHeader("Content-Type", contentType).build()
            return okHttpClient.newCall(request).execute()
        }
    }



}