package com.example.joni.mobileproject

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.transition.Explode
import android.view.Window
import android.app.ActivityOptions
import android.os.CountDownTimer


class SplashActivity : AppCompatActivity() {
    private lateinit var mDelayHandler: Handler
    private val delay: Long = 2000
    private val wait: Long = 3000

    private val mRunnable: Runnable = Runnable {
        if (!isFinishing) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
            //startActivity(intent)
            object : CountDownTimer(wait, wait) {
                override fun onTick(millisUntilFinished: Long) {
                }
                override fun onFinish() {
                    finish()
                }
            }.start()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)
        mDelayHandler = Handler()
        mDelayHandler.postDelayed(mRunnable, delay)
    }

    public override fun onDestroy() {
        mDelayHandler.removeCallbacks(mRunnable)
        super.onDestroy()
    }

}