package jp.andpad.blackboardkotlin.presentation

import android.databinding.BindingAdapter
import android.databinding.ObservableField
import android.hardware.Camera
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import jp.andpad.blackboardkotlin.OrientationSensor


class CameraWithBlackboardViewModel(private val brackboard: Blackboard, private val view: SurfaceView, portraitObservable: Observable<OrientationSensor.Orientation>) {

    // TODO: verに応じて変更
    // var shouldUseCameraManager = !(cameraManager == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)

    // subject
    val finishSubject = PublishSubject.create<Boolean>()!! // activityにfinish通知

    // variables
    private var camera: Camera? = null

    private val holder: SurfaceHolder = view.holder


    // view variables
    var isBackVisible = ObservableField<Boolean>(false)

    val orientation = ObservableField<OrientationSensor.Orientation>(OrientationSensor.Orientation.DEFAULT)

    init {
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceDestroyed(holder: SurfaceHolder) {
                camera?.release()
                camera = null
            }

            override fun surfaceCreated(holder: SurfaceHolder) {
                camera = Camera.open().apply {
                    setPreviewDisplay(holder)
                }
                setCamera()
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                camera?.startPreview()
            }
        })

        portraitObservable.subscribe {
            orientation.set(it)
        }

    }

    // functions
    fun onShutter(view: View) {
        camera?.stopPreview()
        isBackVisible.set(true)
    }

    fun onBackPressed(view: View) {
        camera?.startPreview()
        isBackVisible.set(false)
    }

    fun onTouchSurface(view: View) {
        camera?.autoFocus(autoFocusCallback)
    }

    fun onFinishBtnPressed(view: View) {
        finishSubject.onNext(true)
    }

    fun setCamera() {
        val camera = camera ?: return
        val parameters = camera.parameters
        // 画面の向きを変更する
        camera.setDisplayOrientation(90)
        // サイズを設定
        val size = parameters.supportedPreviewSizes[0]
        parameters.setPreviewSize(size.width, size.height)
        // レイアウト調整
        val layoutParams = view.layoutParams
        layoutParams.width = size.height
        layoutParams.height = size.width
        view.layoutParams = layoutParams
        camera.parameters = parameters
    }

    private val autoFocusCallback = Camera.AutoFocusCallback { b, camera ->
        camera.setOneShotPreviewCallback(previewCallback)
    }

    private val previewCallback = Camera.PreviewCallback { bytes, camera ->
        // autofocusが行われた直後に行う処理
    }

    companion object {

        // dataBinding adapter
        @JvmStatic
        @BindingAdapter("autoRotation")
        fun setRotation(view: View, orientation: OrientationSensor.Orientation) {
            when (orientation) {
                OrientationSensor.Orientation.PORTRAIT, OrientationSensor.Orientation.DEFAULT ->
                    view.rotation = 0f
                OrientationSensor.Orientation.RIGHT_BOTTOM -> view.rotation = -90f
                OrientationSensor.Orientation.LEFT_BOTTOM -> view.rotation = -90f
                OrientationSensor.Orientation.REVERSE_PORTRAIT -> view.rotation = 180f
            }
        }
    }

}