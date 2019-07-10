package com.owoh.video.activity

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Window
import android.view.WindowManager
import com.owoh.R
import com.owoh.databinding.ActivityCameraBinding
import com.owoh.video.fragment.CameraPreviewFragment

/**
 * 相机预览页面
 */
class CameraPreviewActivity : AppCompatActivity() {
        private lateinit var binding: ActivityCameraBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera)
        if (null == savedInstanceState) {
            val fragment = CameraPreviewFragment()
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, fragment, FRAGMENT_CAMERA)
                .addToBackStack(FRAGMENT_CAMERA)
                .commit()
        }

    }


    override fun onResume() {
        super.onResume()

    }

    override fun onBackPressed() {
        // 判断fragment栈中的个数，如果只有一个，则表示当前只处于预览主页面点击返回状态
        val backStackEntryCount = supportFragmentManager.backStackEntryCount
        if (backStackEntryCount > 1) {
            supportFragmentManager.popBackStack()
        } else if (backStackEntryCount == 1) {
            val fragment = supportFragmentManager
                .findFragmentByTag(FRAGMENT_CAMERA) as CameraPreviewFragment?
            if (fragment != null) {
                if (!fragment.onBackPressed()) {
                    finish()
                }
            }
        } else {
            super.onBackPressed()
        }
    }

    companion object {

        private const val FRAGMENT_CAMERA = "fragment_camera"
    }


}
