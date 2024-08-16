package com.comet.btclient

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.comet.btclient.bluetooth.domain.PairedBluetoothDevice
import com.comet.btclient.bluetooth.repository.BlueternetRepository
import com.comet.btclient.connectivity.callback.ResponseCallback
import com.comet.btclient.connectivity.domain.RequestJson
import com.comet.btclient.connectivity.domain.ResponseJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * MainActivity의 활동을 처리하는 VM
 */
class MainViewModel(private val bluetoothRepository: BlueternetRepository) : ViewModel(), ResponseCallback {

    val deviceLiveData : MutableLiveData<List<PairedBluetoothDevice>> = MutableLiveData()
    val responseLiveData : MutableLiveData<String> = MutableLiveData()
    val errorLiveData : MutableLiveData<String> = MutableLiveData() // 회전시 중복 발생되는 문제점 있는 방식이지만, 현재는 우선 사용 ( Event 클래스 사용하면 해결되는 부분)
    val devices : MutableList<PairedBluetoothDevice> = mutableListOf()
    val notifyLiveData : MutableLiveData<String> = MutableLiveData()
    var selectedDevice : PairedBluetoothDevice? = null

    // 디바이스 검색
    fun findDevices() {
        CoroutineScope(Dispatchers.IO).launch {
            kotlin.runCatching {
                devices.apply {
                    clear()
                    addAll(bluetoothRepository.findDevices())
                }
                deviceLiveData.postValue(devices)
            }.onFailure {
                errorLiveData.postValue("블루투스 기기를 찾을 수 없습니다. ${it.message}")
            }
        }
    }

    fun connect() {
        CoroutineScope(Dispatchers.IO).launch {
            if (selectedDevice == null)
                errorLiveData.postValue("기기를 선택해주세요.")
            else
                kotlin.runCatching {
                    notifyLiveData.postValue("기기에 연결중입니다.")
                    bluetoothRepository.connect(selectedDevice!!, this@MainViewModel) //연결됨
                }.onFailure {
                    errorLiveData.postValue("연결할 수 없습니다.")
                }

        }

    }

    fun disconnect() {
        CoroutineScope(Dispatchers.IO).launch {
            kotlin.runCatching {
                bluetoothRepository.disconnect()
                notifyLiveData.postValue("종료되었습니다.")
            }.onFailure {
                errorLiveData.postValue(it.message)
            }
        }
    }

    fun request(url : String, contentType : String, body : String?) {
        CoroutineScope(Dispatchers.IO).launch {
            if (!validate(url, contentType))
                errorLiveData.postValue("입력란이 비어 있습니다.")
            bluetoothRepository.request(RequestJson(url, contentType, body))
        }
    }

    fun validate(url : String, contentType: String) : Boolean {
        return !(url.isBlank() || contentType.isBlank())
    }

    override fun onMessage(responseJson: ResponseJson) {
        if (responseJson.code != 200)
            errorLiveData.postValue("정상적인 응답을 받지 못했습니다.")
        else
            responseLiveData.postValue(responseJson.data)
    }

    // spinner 선택
    fun selectDevice(position : Int) {
        selectedDevice = devices[position]
    }


}

class MainViewModelFactory(private val bluetoothRepository: BlueternetRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(MainViewModel::class.java))
            throw IllegalArgumentException("잘못된 요청입니다.")
        return MainViewModel(bluetoothRepository) as T
    }
}