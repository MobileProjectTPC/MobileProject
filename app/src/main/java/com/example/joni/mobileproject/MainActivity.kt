package com.example.joni.mobileproject


import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.os.Vibrator
import android.support.design.widget.BottomNavigationView
import android.support.v4.widget.NestedScrollView
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.example.joni.mobileproject.fragments.HomeFragment
import com.example.joni.mobileproject.fragments.NotificationsFragment
import com.example.joni.mobileproject.fragments.RegisterFragment
import com.example.joni.mobileproject.objects.NFCUtil
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val viewGroup: ViewGroup? = null
    private var mNfcAdapter: NfcAdapter? = null
    private lateinit var vibrator: Vibrator
    private lateinit var scrollView: NestedScrollView

    private val homeFragment = HomeFragment()
    private val registerFragment = RegisterFragment()
    private val notificationsFragment = NotificationsFragment()

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, homeFragment).commit()
                Log.d("MainActivity", "Home pressed")
                //message.setText(R.string.title_home)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_profile -> {
                supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, registerFragment).commit()
                Log.d("MainActivity", "Profile pressed")
                //message.setText(R.string.title_dashboard)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, notificationsFragment).commit()
                Log.d("MainActivity", "Notifications pressed")
                //message.setText(R.string.title_notifications)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        scrollView = findViewById(R.id.nested_scrollview)
        scrollView.isFillViewport = true

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        supportFragmentManager.beginTransaction().add(R.id.fragmentContainer, homeFragment, "HomeFragment").commit()

    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_close_app, viewGroup)
        val dialogText: TextView = dialogView.findViewById(R.id.dialog_text)
        dialogText.text = "Do you want to close the app?"
        builder.setView(dialogView)
                .setPositiveButton("Yes") { _, _ ->
                    finish()
                }
                .setNegativeButton("No") { _, _ ->
                }.show()
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


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val currentFragment = supportFragmentManager.findFragmentByTag("HomeFragment")
        if (currentFragment != null && currentFragment.isVisible) {
            vibrator.vibrate(100)
            Toast.makeText(this, NFCUtil.retrieveNFCMessage(intent), Toast.LENGTH_SHORT).show()
        }
    }
}
