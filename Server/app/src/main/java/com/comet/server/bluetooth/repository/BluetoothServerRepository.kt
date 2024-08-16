package com.comet.server.bluetooth.repository

import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.comet.server.bluetooth.callback.BluetoothCallback
import com.comet.server.bluetooth.dao.BluetoothService
import com.comet.server.web.domain.RequestJson
import com.comet.server.web.domain.ResponseJson
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.BufferedWriter
import java.util.UUID

// 인터페이스 없이 바로 선언하기. Stateful repository
class BluetoothServerRepository(private val bluetoothService: BluetoothService) {

    private val name = "BLUETERNET"
    private val uuid: UUID = UUID.fromString("a7edee4b-2cae-4195-9719-6175e78eda85") //unique id
    private var runner: BluetoothRunner? = null

    suspend fun open(callback: BluetoothCallback) {
        kotlin.runCatching {
            val socket = bluetoothService.open(name, uuid)
            runner = BluetoothRunner(socket.accept(), callback).also { it.start() }
        }.onSuccess { Log.w("REPOSITORY", "서버 오픈 성공") }
            .onFailure { Log.e("REPOSITORY", "서버 오픈 실패 ${it.message}") }

    }

    suspend fun write(responseJson: ResponseJson) {
        runner?.write(responseJson)
    }

    suspend fun close() {
        runner?.close()
    }

}

class BluetoothRunner(private val socket: BluetoothSocket, private val callback: BluetoothCallback) : Thread() {

    private val bufferedWriter: BufferedWriter by lazy { socket.outputStream.bufferedWriter() }
    private val bufferedReader: BufferedReader by lazy { socket.inputStream.bufferedReader() }
    private val gson: Gson = Gson()

    override fun run() {
        kotlin.runCatching {
            Log.w("RUNNER", "start reading")
            var line = bufferedReader.readLine()
            Log.w("RUNNER", "$line")
            while (line != null) {
                val response = gson.fromJson(line, RequestJson::class.java)
                callback.onMessage(response)
                line = bufferedReader.readLine()
                Log.w("RUNNER", "$line")
            }
        }.onFailure {
            Log.w("RUNNER", "Can't read message : ${it.message}")
            // 인터럽트 발생이 아닌 경우에만
            if (it !is InterruptedException) close()
        }
    }

    fun write(responseJson: ResponseJson) {
        kotlin.runCatching {
            bufferedWriter.write(gson.toJson(responseJson))
            bufferedWriter.write("\n")
            bufferedWriter.flush()
        }.onFailure {
            Log.w("RUNNER", "Can't send message with $responseJson")
        }
    }

    fun close() {
        interrupt()

    }
}