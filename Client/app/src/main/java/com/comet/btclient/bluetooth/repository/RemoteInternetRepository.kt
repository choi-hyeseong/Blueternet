package com.comet.btclient.bluetooth.repository

import com.comet.btclient.bluetooth.domain.PairedBluetoothDevice
import com.comet.btclient.connectivity.callback.ResponseCallback
import com.comet.btclient.connectivity.domain.RequestJson

// 외부 연결을 통해 인터넷을 사용하는 레포지토리
interface RemoteInternetRepository {

    // 디바이스를 검색
    suspend fun findDevices() : List<PairedBluetoothDevice>

    suspend fun connect(pairedBluetoothDevice: PairedBluetoothDevice,  responseCallback: ResponseCallback)

    suspend fun disconnect()

    suspend fun request(requestJson: RequestJson)

}
