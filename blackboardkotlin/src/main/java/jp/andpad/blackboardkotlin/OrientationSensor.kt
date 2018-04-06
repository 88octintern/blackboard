package jp.andpad.blackboardkotlin

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import io.reactivex.subjects.PublishSubject


/**
 * Created by 88oct on 2018/04/06.
 */
class OrientationSensor() : SensorEventListener {
    private val MATRIX_SIZE = 16

    enum class Orientation {
        PORTRAIT,
        REVERSE_PORTRAIT,
        RIGHT_BOTTOM,
        LEFT_BOTTOM,
        DEFAULT
    }

    val publisher = PublishSubject.create<Orientation>()!!
    private var preOri = Orientation.DEFAULT
    private var ori = Orientation.DEFAULT

    /* 回転行列 */
    var inR = FloatArray(MATRIX_SIZE)
    var outR = FloatArray(MATRIX_SIZE)
    var I = FloatArray(MATRIX_SIZE)

    var orientationValues = FloatArray(3)
    var magneticValues = FloatArray(3)
    var accelerometerValues = FloatArray(3)

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_MAGNETIC_FIELD -> magneticValues = event.values.clone()
            Sensor.TYPE_ACCELEROMETER -> accelerometerValues = event.values.clone()
        }

        if (magneticValues != null && accelerometerValues != null) {

            SensorManager.getRotationMatrix(inR, I, accelerometerValues, magneticValues)

            //Activityの表示が縦固定の場合。横向きになる場合、修正が必要です
            SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR)
            SensorManager.getOrientation(outR, orientationValues)

            val roll = radianToDegree(orientationValues[2]).toFloat()
            preOri = ori
            ori = roundOrientationDegree(roll)
            if (preOri != ori) publisher.onNext(ori)
        }
    }

    fun radianToDegree(rad: Float): Int {
        return Math.floor(Math.toDegrees(rad.toDouble())).toInt()
    }

    private fun roundOrientationDegree(roll: Float): Orientation {
        //inputのroll(Y軸のDegree)は-180～180の範囲を想定
        if (-225 < roll && roll <= -135) return Orientation.REVERSE_PORTRAIT
        if (-135 < roll && roll <= -45) return Orientation.LEFT_BOTTOM
        if (-45 < roll && roll <= 45) return Orientation.PORTRAIT
        if (45 < roll && roll <= 135) return Orientation.RIGHT_BOTTOM
        return if (135 < roll && roll <= 225) Orientation.REVERSE_PORTRAIT else Orientation.PORTRAIT
    }
}