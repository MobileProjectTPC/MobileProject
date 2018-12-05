package com.example.joni.mobileproject

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.os.*
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
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
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    private val viewGroup: ViewGroup? = null
    private var mNfcAdapter: NfcAdapter? = null
    private lateinit var vibrator: Vibrator
    private lateinit var scrollView: NestedScrollView

    private val homeFragment = HomeFragment()
    private val registerFragment = RegisterFragment()
    private val notificationsFragment = NotificationsFragment()

    private var mBluetoothAdapter: BluetoothAdapter? = null

    private val mHandler: Handler = object:
            Handler(Looper.getMainLooper()){
        override fun handleMessage(inputMessage: Message) {
            if(inputMessage.what == 0){
                Log.d("MainActivity", "You are in this workspace: ${inputMessage.obj}")
            }
        }
    }

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

        Log.d("MainActivity", "MainActivity started")

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        scrollView = findViewById(R.id.nested_scrollview)
        scrollView.isFillViewport = true

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter

        hasPermissions()

        if (mBluetoothAdapter != null){
            if (mBluetoothAdapter!!.isEnabled){
                val myRunnable = BleScan(mHandler, mBluetoothAdapter!!)
                val myThread = Thread(myRunnable)
                myThread.start()
            }
            else {
                Log.d("MainActivity", "Turn on the bluetooth")
            }
        }

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        supportFragmentManager.beginTransaction().add(R.id.fragmentContainer, homeFragment, HOME_FRAGMENT_TAG).commit()

    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_close_app, viewGroup)
        val dialogText: TextView = dialogView.findViewById(R.id.dialog_text)
        dialogText.text = applicationContext.getText(R.string.close_app)
        builder.setView(dialogView)
                .setPositiveButton(R.string.yes) { _, _ ->
                    finish()
                }
                .setNegativeButton(R.string.no) { _, _ ->
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
                Toast.makeText(this, applicationContext.getText(R.string.unrecognizable_nfc_tag), Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun hasPermissions(): Boolean {
        if (mBluetoothAdapter == null || !mBluetoothAdapter!!.isEnabled) {
            Log.d("DBG", "No Bluetooth LE capability")
            return false
        } else if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            Log.d("DBG", "No fine location access")
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return true // assuming that the user grants permission
        }

        return true
    }
}
