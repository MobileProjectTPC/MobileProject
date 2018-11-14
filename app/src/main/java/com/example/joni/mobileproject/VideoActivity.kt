package com.example.joni.mobileproject

import android.media.MediaPlayer
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.MediaController
import kotlinx.android.synthetic.main.activity_video.*
import java.io.File

class VideoActivity: AppCompatActivity(){

    lateinit var mediacontroller: MediaController
    private var videoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        videoFile = File(intent.getStringExtra("videofile"))

        if (videoFile != null){
            playVideo(videoFile!!.absolutePath)
        }
    }

    private fun playVideo(uri: String){
        videoView.setVideoPath(uri)
        videoView.start()
        mediacontroller = MediaController(this)
        mediacontroller.setAnchorView(videoView)
        videoView.setMediaController(mediacontroller)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (videoFile != null){
            videoFile!!.delete()
        }
        Log.d("VideoActivity", "Destroyed")
    }

}