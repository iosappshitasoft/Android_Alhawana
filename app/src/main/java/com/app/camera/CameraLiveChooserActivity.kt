package com.app.camera

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.app.alhawanajd.BaseActivity
import com.app.alhawanajd.R
import com.app.camera.audioimage.ImageAndAudioMixerActivity
import com.app.camera.textpost.TextPostActivity


class CameraLiveChooserActivity : BaseActivity() {

    private lateinit var tabCamera: TextView
    private lateinit var tabLive: TextView
    private lateinit var imgCamera: ImageView
    private lateinit var imgLive: ImageView
    private lateinit var tabAudio: TextView
    private lateinit var imgAudio: ImageView

    private lateinit var tabImage: TextView
    private lateinit var imgImage: ImageView
    private lateinit var tabText: TextView
    private lateinit var imgText: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_live_chooser)

        tabCamera = findViewById(R.id.tabCamera)
        imgCamera = findViewById(R.id.imgCamera)
        tabImage = findViewById(R.id.tabImage)
        imgImage = findViewById(R.id.imgImage)
        tabText = findViewById(R.id.tabText)
        imgText = findViewById(R.id.imgText)

        loadFragment(CameraFragmentNew())
        updateTabUI(0)

        tabCamera.setOnClickListener {
            loadFragment(CameraFragmentNew())
            updateTabUI(0)
            imgCamera.visibility =  View.VISIBLE
            imgLive.visibility = View.INVISIBLE
            imgAudio.visibility = View.INVISIBLE
            imgImage.visibility = View.INVISIBLE
        }

        tabImage.setOnClickListener {
            updateTabUI(0)
            imgCamera.visibility =  View.INVISIBLE
            imgImage.visibility = View.VISIBLE
            imgText.visibility = View.INVISIBLE
            val intent = Intent(this, ImageAndAudioMixerActivity::class.java)
            startActivity(intent)
        }

        tabText.setOnClickListener {
            updateTabUI(4)
            imgCamera.visibility =  View.INVISIBLE
            imgImage.visibility =  View.INVISIBLE
            imgText.visibility = View.VISIBLE
            val intent = Intent(this, TextPostActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onNetworkChanged(isConnected: Boolean) {
        // TODO("Not yet implemented")
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameContainer, fragment)
            .commit()
    }

    private fun updateTabUI(selected: Int) {
        tabCamera.setTextColor(if (selected == 0) Color.WHITE else Color.GRAY)
        tabImage.setTextColor(if (selected == 3) Color.WHITE else Color.GRAY)
        tabText.setTextColor(if (selected == 4) Color.WHITE else Color.GRAY)
    }
}
