package com.example.joni.mobileproject

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.app.ActivityOptions
import android.nfc.NfcAdapter
import android.os.CountDownTimer
import com.example.joni.mobileproject.objects.NFCUtil


class SplashActivity : AppCompatActivity() {
    private lateinit var mDelayHandler: Handler
    private var mNfcAdapter: NfcAdapter? = null
    private val delay: Long = 2000
    private val wait: Long = 3000

    private val mRunnable: Runnable = Runnable {
        if (!isFinishing) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
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
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)
        setContentView(R.layout.activity_splash)
        mDelayHandler = Handler()
        mDelayHandler.postDelayed(mRunnable, delay)
    }

    public override fun onDestroy() {
        mDelayHandler.removeCallbacks(mRunnable)
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        mNfcAdapter?.let {
            NFCUtil.enableNFCInForeground(it, this, javaClass)
        }
    }

    override fun onPause() {
        super.onPause()
        mNfcAdapter?.let {
            NFCUtil.disableNFCInForeground(it, this)
        }
    }

}