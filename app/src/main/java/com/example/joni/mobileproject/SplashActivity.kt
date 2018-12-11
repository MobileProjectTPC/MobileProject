package com.example.joni.mobileproject

import android.app.ActivityOptions
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v7.app.AppCompatActivity
import com.example.joni.mobileproject.objects.NFCUtil
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class SplashActivity : AppCompatActivity() {
    private var mNfcAdapter: NfcAdapter? = null
    private val wait: Long = 3000

    private val firebaseDatabase = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)
        setContentView(R.layout.activity_splash)
        getStuffFromFirebaseDB("3Dprinting")
    }

    public override fun onDestroy() {
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


    private fun getStuffFromFirebaseDB(workspace: String) {
        val ref = firebaseDatabase.getReference("hacklab/$workspace/tools/")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {

                p0.children.forEach {
                }

                val mainIntent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(mainIntent, ActivityOptions.makeSceneTransitionAnimation(this@SplashActivity).toBundle())
                object : CountDownTimer(wait, wait) {
                    override fun onTick(millisUntilFinished: Long) {
                    }
                    override fun onFinish() {
                        finish()
                    }
                }.start()

            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }

}