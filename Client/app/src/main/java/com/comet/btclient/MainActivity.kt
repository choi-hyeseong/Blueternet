package com.comet.btclient

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.comet.btclient.bluetooth.dao.BluetoothService
import com.comet.btclient.bluetooth.repository.BlueternetRepository
import com.comet.btclient.databinding.ActivityMainBinding

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
        val bluetoothRepository = BlueternetRepository(BluetoothService(bluetoothManager)) //bad usage (hilt로 inject 시켜줘서 따로 따로 하는게 좋지만.. 토이프로젝트라서 일단 이렇게 사용)
        ViewModelProvider(this, MainViewModelFactory(bluetoothRepository))[MainViewModel::class.java] //lazy하게 접근해서 바로 초기화 안되게
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding?.root)

        checkBluetoothSupport()
        activateBluetooth()
    }

    override fun onDestroy() {
        super.onDestroy()
        activityMainBinding = null
        viewModel.disconnect()
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

    fun init() {
        // initialize - observer
        activityMainBinding?.let { binding ->

            // 스피너 선택 변경시 vm에 통보
            binding.listBle.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    viewModel.selectDevice(position)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // nothing
                }
            }
            // 스피너 갱신
            viewModel.deviceLiveData.observe(this) {
                binding.listBle.adapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, it.map { device -> "${device.name}-${device.address}"})
            }

            // 응답 성공적으로 받아왔을때 response
            viewModel.responseLiveData.observe(this) {
                binding.response.text = Html.fromHtml(it, Html.FROM_HTML_MODE_LEGACY).toString()
            }

            viewModel.notifyLiveData.observe(this) {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }


            viewModel.errorLiveData.observe(this) {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }

            //연결 버튼
            binding.connect.setOnClickListener {
                viewModel.connect()
            }

            //종료 버튼
            binding.disconnect.setOnClickListener {
                viewModel.disconnect()
            }

            // 요청버튼
            binding.request.setOnClickListener {
                val typeId = binding.contentGroup.checkedRadioButtonId
                val contentType = if (typeId == R.id.www) "application/x-www-form-urlencoded" else "application/json"
                viewModel.request(binding.url.text.toString(), contentType, binding.body.text.toString())
            }

        }
        viewModel.findDevices()
    }
}

fun Any.getClassName() : String = this::class.java.name