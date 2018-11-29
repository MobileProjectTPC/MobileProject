package com.example.joni.mobileproject

import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.*
import android.support.v7.app.AppCompatActivity
import android.transition.ChangeBounds
import android.transition.ChangeClipBounds
import android.transition.ChangeTransform
import android.transition.TransitionSet
import android.util.Log
import android.view.View
import com.example.joni.mobileproject.adapters.TransitionNavigation
import com.example.joni.mobileproject.fragments.DetailFragment
import com.example.joni.mobileproject.models.Image
import com.example.joni.mobileproject.fragments.ToolFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.os.Bundle
import android.widget.Toast
import com.example.joni.mobileproject.objects.NFCUtil

class ToolsActivity : AppCompatActivity(), TransitionNavigation {

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    val newList = java.util.ArrayList<Image>()
    lateinit var detailFragment: DetailFragment
    private lateinit var vibrator: Vibrator
    private var toolName: String? = null
    private var mNfcAdapter: NfcAdapter? = null

    companion object {
        const val DETAIL_FRAGMENT_TAG = "DetailFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tools)

        getStuffFromFirebaseDB("workspace", "tool1", "images")

        toolName = if (savedInstanceState == null) {
            val extras = intent.extras
            extras?.getString(MainActivity.TOOL)
        } else {
            //savedInstanceState.getSerializable("Tool") as String
            savedInstanceState.getString(MainActivity.TOOL)
        }

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    fun goToDetailFromNFC(tool: String, nfcTrue: Boolean) {
        var position = 0
        var page = 0

        when (tool) {
            MainActivity.listOfTools[0] -> {
                position = 0
                page = 0
            }
            MainActivity.listOfTools[1] -> {
                position = 1
                page = 1
            }
            else -> {
                position = 2
                page = 2
            }
        }

        val transaction = supportFragmentManager.beginTransaction()

        detailFragment = DetailFragment.newInstance(position, page, newList, nfcTrue)

        transaction.replace(R.id.root, detailFragment, DETAIL_FRAGMENT_TAG)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun goToDetail(transitionItems: List<View>, position: Int, page: Int) {

        val transaction = supportFragmentManager.beginTransaction()

        transitionItems.forEach {
            transaction.addSharedElement(it, it.transitionName)
        }

        detailFragment = DetailFragment.newInstance(position, page, newList, false)

        val transitionSet = TransitionSet().apply {
            addTransition(ChangeTransform())
            addTransition(ChangeClipBounds())
            addTransition(ChangeBounds())
        }

        detailFragment.sharedElementEnterTransition = transitionSet
        detailFragment.sharedElementReturnTransition = transitionSet

        transaction.replace(R.id.root, detailFragment, DETAIL_FRAGMENT_TAG)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun getStuffFromFirebaseDB(workspace: String, tool: String, dataType: String) {
        val ref = firebaseDatabase.getReference("/$workspace/tools/$tool/$dataType")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {

                p0.children.forEach {
                    Log.d("HomeFragment", "Stuff: $it")
                    newList.add(it.getValue(Image::class.java)!!)
                    Log.d("tää", "tää ois tää $newList")

                }

                if (toolName != null) {
                    goToDetailFromNFC(toolName!!, true)
                }
                else {
                    val mfc = ToolFragment()
                    val b = Bundle()
                    b.putSerializable("Parcel", newList)
                    mfc.arguments = b

                    supportFragmentManager.beginTransaction()
                            .add(R.id.root, mfc)
                            .commitAllowingStateLoss()
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })
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
        val currentFragment = supportFragmentManager.findFragmentByTag(DETAIL_FRAGMENT_TAG)
        if (currentFragment == null) {
            vibrator.vibrate(MainActivity.VIBRATION_TIME)
            val tool = NFCUtil.retrieveNFCMessage(intent)
            if (MainActivity.listOfTools.contains(tool)) {
                goToDetailFromNFC(tool, false)
            }
            else {
                Toast.makeText(this, "Unrecognizable NFC tag!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
