package com.comet.server

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.comet.server.bluetooth.dao.BluetoothService
import com.comet.server.bluetooth.repository.BluetoothServerRepository
import com.comet.server.databinding.ActivityMainBinding
import com.comet.server.web.repository.WebRepository
import com.comet.server.web.util.WebUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val bluetoothManager : BluetoothManager by lazy {  getSystemService(BluetoothManager::class.java) } //lazy로 접근해서 onCreate 이전에 호출되는것 방지
    private var activityMainBinding : ActivityMainBinding? = null
    private val activityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                init()
            }
            else if (it.resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "블루투스를 킨 상태여야 사용가능합니다.", Toast.LENGTH_LONG).show()
                finish()
            }
        }

    private val viewModel : MainViewModel by lazy {
        val webRepository = WebRepository()
        val bluetoothRepository = BluetoothServerRepository(BluetoothService(bluetoothManager)) //bad usage (hilt로 inject 시켜줘서 따로 따로 하는게 좋지만.. 토이프로젝트라서 일단 이렇게 사용)
        ViewModelProvider(this, MainViewModelFactory(bluetoothRepository, webRepository))[MainViewModel::class.java] //lazy하게 접근해서 바로 초기화 안되게
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkBluetoothSupport()
        activateBluetooth()
    }

    fun init() {
        viewModel.open()
    }

    fun checkBluetoothSupport() {
        if (bluetoothManager.adapter == null) {
            Toast.makeText(this, "블루투스를 지원하지 않습니다.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    fun activateBluetooth() {
        if (bluetoothManager.adapter.isEnabled)
        // init
            init()
        else
            activityResultLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
    }

}