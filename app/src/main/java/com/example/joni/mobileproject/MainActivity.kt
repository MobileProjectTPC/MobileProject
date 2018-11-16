package com.example.joni.mobileproject


import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.os.Vibrator
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.example.joni.mobileproject.fragments.HomeFragment
import com.example.joni.mobileproject.fragments.NotificationsFragment
import com.example.joni.mobileproject.fragments.ProfileFragment
import com.example.joni.mobileproject.objects.NFCUtil
import kotlinx.android.synthetic.main.activity_main.*
import android.support.v4.view.ViewPager
import android.view.MenuItem
import android.support.v4.widget.NestedScrollView
import android.view.View
import com.example.joni.mobileproject.adapters.ViewPagerAdapter

class MainActivity : AppCompatActivity() {

    private val viewGroup: ViewGroup? = null
    private var mNfcAdapter: NfcAdapter? = null
    private lateinit var vibrator: Vibrator
    private lateinit var viewPager: ViewPager
    private var prevMenuItem: MenuItem? = null
    private lateinit var scrollView: NestedScrollView
    private lateinit var homeFragment: HomeFragment
    private lateinit var profileFragment: ProfileFragment
    private lateinit var notificationsFragment: NotificationsFragment

    companion object {
        const val HOME_FRAGMENT_TAG = "HomeFragment"
        const val PROFILE_FRAGMENT_TAG = "ProfileFragment"
        const val HOME_FRAGMENT = 0
        const val PROFILE_FRAGMENT = 1
        const val NOTIFICATIONS_FRAGMENT = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        //window.enterTransition = Explode()
        setContentView(R.layout.activity_main)
        viewPager = this.findViewById(R.id.fragment_viewpager)
        setupNavigation(viewPager)

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        scrollView = findViewById(R.id.nested_scrollview)
        scrollView.isFillViewport = true



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
        val currentFragment = supportFragmentManager.findFragmentByTag("android:switcher:" + R.id.fragment_viewpager + ":" + viewPager.currentItem)
        if (currentFragment.toString().startsWith(HOME_FRAGMENT_TAG)) {
            vibrator.vibrate(100)
            Toast.makeText(this, NFCUtil.retrieveNFCMessage(intent), Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupNavigation(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        homeFragment = HomeFragment.newInstance()
        profileFragment = ProfileFragment.newInstance()
        notificationsFragment = NotificationsFragment.newInstance()
        adapter.addFragment(homeFragment)
        adapter.addFragment(profileFragment)
        adapter.addFragment(notificationsFragment)
        viewPager.adapter = adapter

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {

                val currentFragment = supportFragmentManager.findFragmentByTag("android:switcher:" + R.id.fragment_viewpager + ":" + viewPager.currentItem)
                if (currentFragment.toString().startsWith(PROFILE_FRAGMENT_TAG)) {
                    val currentUser = profileFragment.mAuth.currentUser
                    profileFragment.updateUI(currentUser)
                    //this@MainActivity.findViewById<View>(R.id.log_out_button).visibility = View.VISIBLE
                }
                else {
                    this@MainActivity.findViewById<View>(R.id.log_out_button).visibility = View.INVISIBLE
                }


                if (prevMenuItem != null) {
                    prevMenuItem!!.isChecked = false
                }
                else {
                    navigation.menu.getItem(0).isChecked = false
                }
                navigation.menu.getItem(position).isChecked = true
                prevMenuItem = navigation.menu.getItem(position)
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        navigation.setOnNavigationItemSelectedListener { item ->

            when (item.itemId) {
                R.id.navigation_home -> {
                    viewPager.currentItem = HOME_FRAGMENT
                }
                R.id.navigation_profile -> {
                    viewPager.currentItem = PROFILE_FRAGMENT
                }
                R.id.navigation_notifications-> {
                    viewPager.currentItem = NOTIFICATIONS_FRAGMENT
                }
            }
            true
        }
    }

}
