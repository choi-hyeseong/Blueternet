package com.comet.server.bluetooth.callback

import com.comet.server.web.domain.RequestJson

// 블루투스에서 데이터 받을때 수행하는 콜백
interface BluetoothCallback {

    fun onMessage(requestJson: RequestJson)
}