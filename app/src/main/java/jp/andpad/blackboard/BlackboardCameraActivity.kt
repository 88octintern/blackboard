package jp.andpad.blackboard

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import jp.andpad.blackboardkotlin.presentation.CameraWithBlackboardActivity

class BlackboardCameraActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blackboard_camera)
        startActivity(Intent(this, CameraWithBlackboardActivity::class.java))
    }
}
