package com.comet.server.web.repository

import com.comet.server.web.domain.RequestJson
import com.comet.server.web.domain.ResponseJson
import com.comet.server.web.util.WebUtil

class WebRepository {

    suspend fun request(requestJson: RequestJson) : ResponseJson {
        val response = WebUtil.request(requestJson.url, requestJson.contentType, requestJson.body)
        return ResponseJson(response.code(), response.body().string())
    }
}