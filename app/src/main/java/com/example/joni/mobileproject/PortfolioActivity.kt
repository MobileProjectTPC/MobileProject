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
import com.example.joni.mobileproject.fragments.DetailPortfolioFragment
import com.example.joni.mobileproject.models.Image
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class PortfolioActivity : AppCompatActivity(), TransitionNavigation {
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    val newList = java.util.ArrayList<Image>()
    lateinit var detailPortfolioFragment: DetailPortfolioFragment
    private lateinit var vibrator: Vibrator
    private val firebaseAuth = FirebaseAuth.getInstance()
    private var user: FirebaseUser? = null
    private var origin: Int? = null  // origin: 0 = From Home, 1 = From Profile
    private var workspace:String? = null

    companion object {
        const val DETAIL_FRAGMENT_TAG = "DetailPortfolioFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_portfolio)
        Log.d("PortfolioActivity_test", "PortfolioActivity Created")
        origin = intent.getIntExtra("origin", -1)
        Log.d("PortfolioActivity_test", origin.toString())
        if (origin == 1){
            user = firebaseAuth.currentUser
            getStuffFromFirebaseDB(1, user, null)
        }
        else if (origin == 0){
            workspace = intent.getStringExtra("workspace")
            Log.d("PortfolioActivity", workspace)
            getStuffFromFirebaseDB(2, null, workspace)
        }
        else if (origin == -1){
            Log.d("PortfolioActivity", "Error")
        }
    }

    override fun goToDetail(transitionItems: List<View>, position: Int, page: Int) {

        val transaction = supportFragmentManager.beginTransaction()

        transitionItems.forEach {
            transaction.addSharedElement(it, it.transitionName)
        }

        detailPortfolioFragment = DetailPortfolioFragment.newInstance(position, page, newList, true)

        val transitionSet = TransitionSet().apply {
            addTransition(ChangeTransform())
            addTransition(ChangeClipBounds())
            addTransition(ChangeBounds())
        }

        detailPortfolioFragment.sharedElementEnterTransition = transitionSet
        detailPortfolioFragment.sharedElementReturnTransition = transitionSet

        transaction.replace(R.id.root, detailPortfolioFragment, PortfolioActivity.DETAIL_FRAGMENT_TAG)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun getStuffFromFirebaseDB(mode: Int, user: FirebaseUser?, workspace: String?) {
        Log.d("PortfolioActivity_test", "getStuffFromFirebase")
        var query: Query?

        if (mode == 1){
            query = firebaseDatabase.getReference("portfolio").orderByChild("user").equalTo(user?.uid)
        }
        else{
            query = firebaseDatabase.getReference("portfolio").orderByChild("workspace").equalTo(workspace)
        }
        Log.d("PortfolioActivity_test", query.toString())

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                Log.d("PortfolioActivity_Test", p0.toString())
                if (p0?.value == null) {
                    if (mode == 1) {
                        Toast.makeText(applicationContext, "You have not created any portfolio yet", Toast.LENGTH_LONG).show()
                    }
                    else{
                        Toast.makeText(applicationContext, "There hasn't been any portfolio added in this workspace yet", Toast.LENGTH_LONG).show()
                    }
                } else
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
}
