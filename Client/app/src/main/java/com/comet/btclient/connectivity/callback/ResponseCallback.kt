package com.comet.btclient.connectivity.callback

import com.comet.btclient.connectivity.domain.ResponseJson

// Bluetooth 응답시 호출되는 콜백입니다.
interface ResponseCallback {

    // 메시지 수신시
    fun onMessage(responseJson: ResponseJson)
}