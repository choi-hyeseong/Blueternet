package com.comet.btclient.bluetooth.dao

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import com.comet.btclient.bluetooth.domain.PairedBluetoothDevice
import java.util.UUID

/**
 * Bluetooth에 접근하는 서비스 (Dao)
 * @property bluetoothManager 블루투스에 접근하는 시스템 서비스입니다.
 * @property bluetoothAdapter 블루투스를 관리하는 어댑터입니다.
 */
class BluetoothService(private val bluetoothManager: BluetoothManager) {

    private val bluetoothAdapter : BluetoothAdapter by lazy { bluetoothManager.adapter }

    /**
     * 페어링된 기기를 찾습니다. 주변 기기를 탐색하는 방식도 있지만, 현재는 페어링된 기기를 대상으로 합니다. - 미페어링시 페어링 UI등등 발생 이유.
     * @suppress PermCheck는 MainActivity에서 수행함.
     * @throws IllegalStateException 블루투스가 활성화되어 있지 않으면 발생합니다.
     */

    @SuppressLint("MissingPermission")
    suspend fun findDevices() : List<PairedBluetoothDevice> {
        if (!bluetoothAdapter.isEnabled)
            // 비활성화 되어 있는 경우 오류 발생
            throw IllegalStateException("블루투스가 활성화 되어 있지 않습니다.")
        return bluetoothAdapter.bondedDevices.map { PairedBluetoothDevice(it.name, it.address) }.toList()
    }

    /**
     * 해당 기기와 블루투스 연결을 수행합니다.
     * @throws IOException 연결 실패시 발생합니다.
     */
    @SuppressLint("MissingPermission")
    suspend fun connect(uuid: UUID, bluetoothDevice: BluetoothDevice) : BluetoothSocket {
        return bluetoothDevice.createRfcommSocketToServiceRecord(uuid).also { it.connect() }
    }

    // 해당 주소로부터 디바이스 흭득하기
    suspend fun getRemoteDevice(address : String) : BluetoothDevice {
        return bluetoothAdapter.getRemoteDevice(address)
    }
}