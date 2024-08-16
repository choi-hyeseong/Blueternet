package com.comet.btclient

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.comet.btclient.bluetooth.domain.PairedBluetoothDevice
import com.comet.btclient.bluetooth.repository.BlueternetRepository
import com.comet.btclient.connectivity.callback.ResponseCallback
import com.comet.btclient.connectivity.domain.RequestJson
import com.comet.btclient.connectivity.domain.ResponseJson

/**
 * MainActivity의 활동을 처리하는 VM
 */
class MainViewModel(private val bluetoothRepository: BlueternetRepository) : ViewModel(), ResponseCallback {

    val deviceLiveData : MutableLiveData<List<PairedBluetoothDevice>> = MutableLiveData()
    val notifyLiveData : MutableLiveData<String> = MutableLiveData()
    val errorLiveData : MutableLiveData<String> = MutableLiveData() // 회전시 중복 발생되는 문제점 있는 방식이지만, 현재는 우선 사용 ( Event 클래스 사용하면 해결되는 부분)

    // 디바이스 검색
    suspend fun findDevices() {
        kotlin.runCatching {
            deviceLiveData.postValue(bluetoothRepository.findDevices())
        }.onFailure {
            errorLiveData.postValue("블루투스 기기를 찾을 수 없습니다. ${it.message}")
        }
    }

    suspend fun connect() {

    }

    suspend fun disconnect() {

    }

    suspend fun request(url : String, contentType : String, body : String?) {
        if (!validate(url, contentType))
            errorLiveData.postValue("입력란이 비어 있습니다.")
        bluetoothRepository.request(RequestJson(url, contentType, body))
    }

    fun validate(url : String, contentType: String) : Boolean {
        return !(url.isBlank() || contentType.isBlank())
    }

    override fun onMessage(responseJson: ResponseJson) {
        TODO("Not yet implemented")
    }


}

class MainViewModelFactory(private val bluetoothRepository: BlueternetRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(MainViewModel::class.java))
            throw IllegalArgumentException("잘못된 요청입니다.")
        return MainViewModel(bluetoothRepository) as T
    }
}