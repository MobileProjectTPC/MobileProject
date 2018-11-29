package com.example.joni.mobileproject

import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.os.Vibrator
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.widget.NestedScrollView
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.example.joni.mobileproject.fragments.HomeFragment
import com.example.joni.mobileproject.fragments.NotificationsFragment
import com.example.joni.mobileproject.fragments.RegisterFragment
import com.example.joni.mobileproject.objects.NFCUtil
import kotlinx.android.synthetic.main.activity_main.*
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    private val viewGroup: ViewGroup? = null
    private var mNfcAdapter: NfcAdapter? = null
    private lateinit var vibrator: Vibrator
    private lateinit var scrollView: NestedScrollView

    private val homeFragment = HomeFragment()
    private val registerFragment = RegisterFragment()
    private val notificationsFragment = NotificationsFragment()

    companion object {
        const val HOME_FRAGMENT_TAG = "HomeFragment"
        const val REGISTER_FRAGMENT_TAG = "RegisterFragment"
        const val NOTIFICTIONS_FRAGMENT_TAG = "NotificationFragment"
        const val VIBRATION_TIME: Long = 100
        const val TOOL = "Tool"
        val listOfTools = arrayListOf(
                "tool1", "tool2", "tool3"
        )
    }

    private val mOnNavigationItemSelectedListener =
            BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                setupFragment(homeFragment, HOME_FRAGMENT_TAG)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_profile -> {
                setupFragment(registerFragment, REGISTER_FRAGMENT_TAG)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                setupFragment(notificationsFragment, NOTIFICTIONS_FRAGMENT_TAG)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private fun setupFragment(fragment: Fragment, name: String) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment, name).commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        scrollView = findViewById(R.id.nested_scrollview)
        scrollView.isFillViewport = true

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        supportFragmentManager.beginTransaction().add(R.id.fragmentContainer, homeFragment, HOME_FRAGMENT_TAG).commit()

    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_close_app, viewGroup)
        val dialogText: TextView = dialogView.findViewById(R.id.dialog_text)
        dialogText.text = "Do you want to close the app?" //Make strings for these
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
        val currentFragment = supportFragmentManager.findFragmentByTag(HOME_FRAGMENT_TAG)
        if (currentFragment != null && currentFragment.isVisible) {
            vibrator.vibrate(VIBRATION_TIME)
            val tool = NFCUtil.retrieveNFCMessage(intent)

            if (listOfTools.contains(tool)) {
                val toolIntent = Intent(this, ToolsActivity::class.java)
                toolIntent.putExtra(TOOL, tool)
                startActivity(toolIntent)
            }
            else {
                Toast.makeText(this, "Unrecognizable NFC tag!", Toast.LENGTH_SHORT).show()
            }

        }
    }
}
