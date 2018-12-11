package com.example.joni.mobileproject

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
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
import com.example.joni.mobileproject.fragments.HomeFragment.Companion.TOOL_LIST
import com.example.joni.mobileproject.fragments.NotificationsFragment
import com.example.joni.mobileproject.fragments.RegisterFragment
import com.example.joni.mobileproject.models.Tool
import com.example.joni.mobileproject.objects.NFCUtil
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private val viewGroup: ViewGroup? = null
    private var mNfcAdapter: NfcAdapter? = null
    private lateinit var vibrator: Vibrator
    private lateinit var scrollView: NestedScrollView

    private val homeFragment = HomeFragment()
    private val registerFragment = RegisterFragment()
    private val notificationsFragment = NotificationsFragment()

    private val firebaseDatabase = FirebaseDatabase.getInstance()

    var toolList = java.util.ArrayList<Tool>()

    private var mBluetoothAdapter: BluetoothAdapter? = null

    private var workspace: String? = null

    private val mHandler: Handler = object:
            Handler(Looper.getMainLooper()){
        override fun handleMessage(inputMessage: Message) {
            Log.d("MainActivityHandle","input: ${inputMessage.obj} workspace: $workspace")
            if (workspace == null){
                workspace = inputMessage.obj.toString()
                getTools(inputMessage.obj.toString())
                getWorkspace(inputMessage.obj.toString())
            }
            if (workspace!! != inputMessage.obj){
                if(inputMessage.what == 0){
                    workspace = inputMessage.obj.toString()
                    getTools(inputMessage.obj.toString())
                    getWorkspace(inputMessage.obj.toString())
                }
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
                "tool1", "tool2", "tool3", "tool4", "tool5", "tool6", "tool7", "tool8", "tool9"
        )
    }

    private val mOnNavigationItemSelectedListener =
            BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                val b = Bundle()
                b.putSerializable(TOOL_LIST, toolList)
                homeFragment.arguments = b
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


        if (hasPermissions()){
            startBLEScannerThread()
        }

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        val homeFragment = HomeFragment()
        val b = Bundle()
        b.putSerializable(TOOL_LIST, toolList)
        homeFragment.arguments = b

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

    // if user allows the fine_location start BLEScanner
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("MainActivity", "onRequest... requestCode: $requestCode permissions: ${permissions[0]} grantResult: ${grantResults[0]}")


        if (requestCode == 1 && permissions[0] == android.Manifest.permission.ACCESS_FINE_LOCATION && grantResults[0] == 0){
            startBLEScannerThread()
        }

    }

    // check permissions, fine_location and camera
    // if fine_location permission is granted, return true and then the BLEScanner thread is able to start
    private fun hasPermissions(): Boolean {

        if (mBluetoothAdapter == null || !mBluetoothAdapter!!.isEnabled) {
            Log.d("DBG", "No Bluetooth LE capability")
            return false
        } else if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.CAMERA), 1)
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                return true
            }

            return false
        }
        return true
    }

    // if bluetooth is on start the BLEScanner thread
    private fun startBLEScannerThread(){
        if (mBluetoothAdapter != null){
            Log.d("MainActivity", "mBluetoothAdapter not null")
            if (mBluetoothAdapter!!.isEnabled){
                Log.d("MainActivity", "mBluetoothAdapter isEnable")
                val myRunnable = BleScan(mHandler, mBluetoothAdapter!!)
                val myThread = Thread(myRunnable)
                myThread.start()
            }
            else {
                Log.d("MainActivity", "Turn on the bluetooth")
            }
        }
    }

    // get the tools that are in the workspace you are in
    // put them in a list and send that list to homefragment
    private fun getTools(workspace: String) {
        val ref = firebaseDatabase.getReference("hacklab/$workspace/tools/")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {

                toolList.clear()

                p0.children.forEach {
                    Log.d("MainTAG", "$it")
                    toolList.add(it.getValue(Tool::class.java)!!)
                }
                val homeFragment = HomeFragment()
                val b = Bundle()
                b.putSerializable(TOOL_LIST, toolList)
                homeFragment.arguments = b
                supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, homeFragment, HOME_FRAGMENT_TAG).commit()
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }

    private fun getWorkspace(workspace: String) {
        val ref = firebaseDatabase.getReference("hacklab/$workspace")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                Log.d("MAindsdfaf", "${p0.child("image").value}")

                if (p0.child("image").value != null){
                    try {
                        val myURL = p0.child("image").value.toString()
                        //GetCont().execute(myURL)
                        Picasso.get().load(myURL).into(expandedImage)
                    } catch (e:Exception){
                        Log.e("URL", "URL creation",e)
                    }
                }
                else {
                    expandedImage.setImageResource(R.drawable.workshop_tutor_icon)
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }
}
