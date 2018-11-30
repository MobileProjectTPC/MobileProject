package com.example.joni.mobileproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Vibrator
import android.support.v7.app.AppCompatActivity
import android.transition.ChangeBounds
import android.transition.ChangeClipBounds
import android.transition.ChangeTransform
import android.transition.TransitionSet
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.joni.mobileproject.adapters.TransitionNavigation
import com.example.joni.mobileproject.fragments.DetailFragment
import com.example.joni.mobileproject.fragments.PortfolioFragment
import com.example.joni.mobileproject.fragments.ToolFragment
import com.example.joni.mobileproject.models.Image
import com.example.joni.mobileproject.objects.NFCUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class PortfolioActivity : AppCompatActivity(), TransitionNavigation {
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    val newList = java.util.ArrayList<Image>()
    lateinit var detailFragment: DetailFragment
    private lateinit var vibrator: Vibrator
    private val firebaseAuth = FirebaseAuth.getInstance()
    private var user: FirebaseUser? = null

    companion object {
        const val DETAIL_FRAGMENT_TAG = "DetailFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("PortfolioActivity_test", "PortfolioActivity Created")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_portfolio)
        user = firebaseAuth.currentUser
        getStuffFromFirebaseDB(user!!)
        /*
        profileName = if (savedInstanceState == null) {
            val extras = intent.extras
            extras?.getString(MainActivity.TOOL)
        } else {
            //savedInstanceState.getSerializable("Tool") as String
            savedInstanceState.getString(MainActivity.TOOL)
        }
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        */
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

        transaction.replace(R.id.root, detailFragment, ToolsActivity.DETAIL_FRAGMENT_TAG)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun getStuffFromFirebaseDB(user: FirebaseUser) {
        Log.d("PortfolioActivity_test", "getStuffFromFirebase")
        val query: Query = firebaseDatabase.getReference("portfolio").orderByChild("uid").equalTo(user.uid)
        Log.d("PortfolioActivity_test", query.toString())
        query.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                Log.d("PortfolioActivity_Test", p0.toString())
                if (p0 == null || p0.value == null){
                    Toast.makeText(applicationContext, "You have not created any portfolio", Toast.LENGTH_SHORT).show()
                }
                else

                p0.children.forEach {
                    newList.add(it.getValue(Image::class.java)!!)
                }
                val mfc = PortfolioFragment()
                val b = Bundle()
                b.putSerializable("Parcel", newList)
                mfc.arguments = b

                supportFragmentManager.beginTransaction()
                        .add(R.id.root, mfc)
                        .commitAllowingStateLoss()

            }

            override fun onCancelled(p0: DatabaseError) {
            }

        })
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val currentFragment = supportFragmentManager.findFragmentByTag(ToolsActivity.DETAIL_FRAGMENT_TAG)
        if (currentFragment == null) {
            vibrator.vibrate(MainActivity.VIBRATION_TIME)
            val tool = NFCUtil.retrieveNFCMessage(intent)
            if (MainActivity.listOfTools.contains(tool)) {

            }
            else {
                Toast.makeText(this, applicationContext.getText(R.string.unrecognizable_nfc_tag), Toast.LENGTH_SHORT).show()
            }
        }
    }
}