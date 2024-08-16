package com.comet.btclient.connectivity.domain

// 웹 요청시 전송되는 json입니다.
data class RequestJson (val url : String, val contentType : String, val body : String?)