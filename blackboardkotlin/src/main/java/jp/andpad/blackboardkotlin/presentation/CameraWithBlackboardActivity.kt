package jp.andpad.blackboardkotlin.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import jp.andpad.blackboardkotlin.OrientationSensor
import jp.andpad.blackboardkotlin.R
import jp.andpad.blackboardkotlin.databinding.ActivityCameraWithBlackboardBinding


class CameraWithBlackboardActivity : AppCompatActivity() {

    private val binding by lazy {
        DataBindingUtil
                .setContentView<ActivityCameraWithBlackboardBinding>(this, R.layout.activity_camera_with_blackboard)
    }

    private lateinit var viewModel: CameraWithBlackboardViewModel

    // sensorManagerにsensorをセットする
    private val orientationSensor = OrientationSensor()
    private var sensorManager: SensorManager? = null

    // 黒板のビュー
    private val blackboard = Blackboard()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (checkPermission()) return
        // 画面の回転を検知するときに利用する
        registerSensor()

        setViewModel()
    }

    /**
     * viewModelのインスタンス生成
     */
    private fun setViewModel() {
        viewModel = CameraWithBlackboardViewModel(blackboard, binding.previewSurface, orientationSensor.publisher).apply {
            finishSubject.subscribe {
                finishActivity()
            }
        }
        binding.viewModel = viewModel
    }

    private fun registerSensor() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager?.let {
            it.registerListener(
                    orientationSensor,
                    it.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                    SensorManager.SENSOR_DELAY_NORMAL
            )
            it.registerListener(
                    orientationSensor,
                    it.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                    SensorManager.SENSOR_DELAY_NORMAL
            )
            it.registerListener(
                    orientationSensor,
                    it.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }
    private fun unregisterSensor() {
        sensorManager?.unregisterListener(orientationSensor)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setViewModel()
                } else {
                    finishActivity()
                }
            }
        }
    }

    private fun checkPermission(): Boolean {
        val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            // Android 6.0 のみ、該当パーミッションが許可されていない場合
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // パーミッションが必要であることを明示するアプリケーション独自のUIを表示
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
                return true
            }
            return false
        } else {
            // 許可済みの場合、もしくはAndroid 6.0以前
            // パーミッションが必要な処理
            return false
        }
    }

    private fun finishActivity() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterSensor()
    }

    companion object {
        const val CAMERA_REQUEST_CODE = 1
    }
}