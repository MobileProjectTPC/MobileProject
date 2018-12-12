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
import android.util.Log
import android.widget.Toast
import com.example.joni.mobileproject.models.Tool
import com.example.joni.mobileproject.objects.NFCUtil

class ToolsActivity : AppCompatActivity(), TransitionNavigation {

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    val newList = java.util.ArrayList<Tool>()
    lateinit var detailFragment: DetailFragment
    private lateinit var vibrator: Vibrator
    private var toolName: String? = null
    private var mNfcAdapter: NfcAdapter? = null
    private var listRetrieved = false
    private lateinit var workspace: String

    companion object {
        const val DETAIL_FRAGMENT_TAG = "DetailFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tools)



        workspace = "3Dprinting"  ////////////////////////??????????????




        getStuffFromFirebaseDB(workspace, "3Dprinter", "images")

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
        val position = 0
        val page = 0
        var specificTool = ""
        var workSpace = ""

        when (tool) {
            MainActivity.listOfTools[0] -> {
                specificTool = "3Dprinter"
                workSpace = "3Dprinting"
            }
            MainActivity.listOfTools[1] -> {
                specificTool = "3Dprinter2"
                workSpace = "3Dprinting"
            }
            MainActivity.listOfTools[2] -> {
                specificTool = "3Dprinter3"
                workSpace = "3Dprinting"
            }
            MainActivity.listOfTools[3] -> {
                specificTool = "Drill"
                workSpace = "Metalworking"
            }
            MainActivity.listOfTools[4] -> {
                specificTool = "Drill"
                workSpace = "Metalworking"
            }
            MainActivity.listOfTools[5] -> {
                specificTool = "Drill"
                workSpace = "Metalworking"
            }
            MainActivity.listOfTools[6] -> {
                specificTool = "Sandingmachine"
                workSpace = "Woodworking"
            }
            MainActivity.listOfTools[7] -> {
                specificTool = "Sandingmachine"
                workSpace = "Woodworking"
            }
            MainActivity.listOfTools[8] -> {
                specificTool = "Sandingmachine"
                workSpace = "Woodworking"
            }
        }
        val specificToolList = java.util.ArrayList<Tool>()
        val ref = firebaseDatabase.getReference("hacklab/$workSpace/tools/$specificTool")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                specificToolList.add(p0.getValue(Tool::class.java)!!)

                val transaction = supportFragmentManager.beginTransaction()

                detailFragment = DetailFragment.newInstance(position, page, specificToolList, nfcTrue)

                transaction.replace(R.id.root, detailFragment, DETAIL_FRAGMENT_TAG)
                transaction.addToBackStack(null)
                transaction.commit()
            }
            override fun onCancelled(p0: DatabaseError) {
            }
        })


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
        //val ref = firebaseDatabase.getReference("/$workspace/tools/$tool/$dataType")
        val ref = firebaseDatabase.getReference("hacklab/$workspace/tools/")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {

                p0.children.forEach {
                    Log.d("ToolsActivity it:", it.toString())
                    Log.d("ToolsActivity it:.getValue()", it.getValue(Tool::class.java).toString())
                    newList.add(it.getValue(Tool::class.java)!!)
                }

                val workspace2 = "Metalworking"
                val ref2 = firebaseDatabase.getReference("hacklab/$workspace2/tools/")
                ref2.addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(p0: DataSnapshot) {

                        p0.children.forEach {
                            Log.d("ToolsActivity it:", it.toString())
                            Log.d("ToolsActivity it:.getValue()", it.getValue(Tool::class.java).toString())
                            newList.add(it.getValue(Tool::class.java)!!)
                        }
                        val workspace3 = "Woodworking"
                        val ref3 = firebaseDatabase.getReference("hacklab/$workspace3/tools/")
                        ref3.addListenerForSingleValueEvent(object : ValueEventListener {

                            override fun onDataChange(p0: DataSnapshot) {

                                p0.children.forEach {
                                    Log.d("ToolsActivity it:", it.toString())
                                    Log.d("ToolsActivity it:.getValue()", it.getValue(Tool::class.java).toString())
                                    newList.add(it.getValue(Tool::class.java)!!)
                                }
                                listRetrieved = true

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

                    override fun onCancelled(p0: DatabaseError) {
                    }
                })



                /*
                listRetrieved = true

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
                */
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
        if (currentFragment == null && listRetrieved) {
            vibrator.vibrate(MainActivity.VIBRATION_TIME)
            val tool = NFCUtil.retrieveNFCMessage(intent)
            if (MainActivity.listOfTools.contains(tool)) {
                goToDetailFromNFC(tool, false)
            }
            else {
                Toast.makeText(this, applicationContext.getText(R.string.unrecognizable_nfc_tag), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
