package com.comet.btclient.bluetooth.repository

import android.bluetooth.BluetoothSocket
import android.util.Log
import com.comet.btclient.bluetooth.dao.BluetoothService
import com.comet.btclient.bluetooth.domain.PairedBluetoothDevice
import com.comet.btclient.connectivity.callback.ResponseCallback
import com.comet.btclient.connectivity.domain.RequestJson
import com.comet.btclient.connectivity.domain.ResponseJson
import com.comet.btclient.getClassName
import com.google.gson.Gson
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.BufferedWriter
import java.util.UUID
import kotlin.concurrent.thread

// Bluetooth를 사용하는 구현체. Stateful Repository로 구현 (VM 과부화 X)
class BlueternetRepository(private val bluetoothService: BluetoothService) : RemoteInternetRepository {

    // 현재 연결여부
    private var isConnected: Boolean = false
    private val uuid: UUID = UUID.fromString("a7edee4b-2cae-4195-9719-6175e78eda85") //unique id
    private var connectThread: BluetoothRunner? = null //소켓 핸들링하는 thread

    // 디바이스 검색
    override suspend fun findDevices(): List<PairedBluetoothDevice> {
        return bluetoothService.findDevices()
    }

    // 연결
    override suspend fun connect(pairedBluetoothDevice: PairedBluetoothDevice, responseCallback: ResponseCallback) {
        if (isConnected && connectThread?.isInterrupted == false)
            throw IllegalStateException("이미 연결중입니다.")
        val device = bluetoothService.getRemoteDevice(pairedBluetoothDevice.address)
        val socket = bluetoothService.connect(uuid, device) //연결
        connectThread = BluetoothRunner(socket, responseCallback).also { it.start() }
        isConnected = true
    }

    // 연결 종료
    override suspend fun disconnect() {
        connectThread?.interrupt()
        isConnected = false
    }

    override suspend fun request(requestJson: RequestJson) {
        connectThread?.send(requestJson)
    }

    class BluetoothRunner(socket: BluetoothSocket, private val callback: ResponseCallback) : Thread() {

        private val bufferedWriter: BufferedWriter by lazy { socket.outputStream.bufferedWriter() }
        private val bufferedReader: BufferedReader by lazy { socket.inputStream.bufferedReader() }
        private val gson: Gson = Gson()

        override fun run() {
            kotlin.runCatching {
                var line = bufferedReader.readLine()
                while (line != null) {
                    val response = gson.fromJson(line, ResponseJson::class.java)
                    callback.onMessage(response)
                    line = bufferedReader.readLine()
                }
            }.onFailure {
                Log.w(getClassName(), "Can't read message : ${it.message}")
                interrupt()
            }
        }

        fun send(requestJson: RequestJson) {
            kotlin.runCatching {
                bufferedWriter.write(gson.toJson(requestJson))
            }.onFailure {
                Log.w(getClassName(), "Can't send message with $requestJson")
            }
        }
    }

}