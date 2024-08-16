package com.comet.server.bluetooth.dao

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import java.util.UUID

//서버 역할을 하는 블루투스 서비스
class BluetoothService(private val bluetoothManager: BluetoothManager) {

    private val bluetoothAdapter by lazy { bluetoothManager.adapter } //lazy로 안전하게 접근

    /**
     * 오류 발생시 IO EXception 호출
     */
    @SuppressLint("MissingPermission")
    suspend fun open(name : String, uuid : UUID): BluetoothServerSocket {
        return bluetoothAdapter.listenUsingRfcommWithServiceRecord(name, uuid)
    }
}