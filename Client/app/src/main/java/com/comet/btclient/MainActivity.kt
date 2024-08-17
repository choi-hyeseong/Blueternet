package com.comet.btclient

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.comet.btclient.bluetooth.dao.BluetoothService
import com.comet.btclient.bluetooth.repository.BlueternetRepository
import com.comet.btclient.bluetooth.repository.RemoteInternetRepository
import com.comet.btclient.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val PERM_CODE = 200
    private var activityMainBinding : ActivityMainBinding? = null
    private val activityResultLauncher: ActivityResultLauncher<Intent> = initBluetoothCallback()
    private val bluetoothManager : BluetoothManager by lazy {  getSystemService(BluetoothManager::class.java) } //lazy로 접근해서 onCreate 이전에 호출되는것 방지
    private val bluetoothRepository : RemoteInternetRepository by lazy {  BlueternetRepository(BluetoothService(bluetoothManager)) } //bad usage (hilt로 inject 시켜줘서 따로 따로 하는게 좋지만.. 토이프로젝트라서 일단 이렇게 사용)
    private val viewModel : MainViewModel by lazy { ViewModelProvider(this, MainViewModelFactory(bluetoothRepository))[MainViewModel::class.java] } //lazy하게 접근해서 바로 초기화 안되게

    // 블루투스 허용 콜백
    private fun initBluetoothCallback() : ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                init()
            }
            else if (it.resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "블루투스를 킨 상태여야 사용가능합니다.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun enableBluetooth() {
        activityResultLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding?.root)

        activateBluetooth() //블루투스 활성화
    }

    override fun onDestroy() {
        super.onDestroy()
        activityMainBinding = null
        viewModel.disconnect()
    }

    private fun activateBluetooth() {
        // 블루투스 확인
        if (bluetoothManager.adapter == null) {
            createToast("블루투스를 지원하지 않습니다.")
            finish()
            return
        }

        // Android 12이상 블루투스 펄미션 확인. 만약 블루투스가 꺼져있다면 펄미션 허용 콜백에서 같이 킴 (enable 호출)
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_CONNECT), PERM_CODE)
        else if (!bluetoothManager.adapter.isEnabled)
            // 블루투스가 허용되지 않은경우
            enableBluetooth()
        else
            // init
            init()
    }

    private fun hasPermission(permission: String) : Boolean {
        return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun createToast(message : String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != PERM_CODE)
            return
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            enableBluetooth()
        else {
            createToast("권한이 허용되지 않았습니다..")
            finish()
        }
    }

    private fun init() {
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
                createToast(it)
            }


            viewModel.errorLiveData.observe(this) {
                createToast(it)
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