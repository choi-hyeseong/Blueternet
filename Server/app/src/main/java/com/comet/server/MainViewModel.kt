package com.comet.server

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.comet.server.bluetooth.callback.BluetoothCallback
import com.comet.server.bluetooth.repository.BluetoothServerRepository
import com.comet.server.web.domain.RequestJson
import com.comet.server.web.repository.WebRepository
import com.comet.server.web.util.WebUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(private val webRepository: WebRepository, private val bluetoothServerRepository: BluetoothServerRepository) : BluetoothCallback, ViewModel() {

    fun open() {
        CoroutineScope(Dispatchers.IO).launch {
            bluetoothServerRepository.open(this@MainViewModel)
        }
    }

    override fun onMessage(requestJson: RequestJson) {
        CoroutineScope(Dispatchers.IO).launch {
            bluetoothServerRepository.write(webRepository.request(requestJson))
        }

    }
}

class MainViewModelFactory(private val bluetoothRepository: BluetoothServerRepository, private val webRepository: WebRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(MainViewModel::class.java))
            throw IllegalArgumentException("잘못된 요청입니다.")
        return MainViewModel(webRepository, bluetoothRepository) as T
    }
}