package com.example.joni.mobileproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Vibrator
import android.provider.MediaStore
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
import com.example.joni.mobileproject.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.io.Serializable

class PortfolioActivity : AppCompatActivity(), TransitionNavigation {
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    val newList = java.util.ArrayList<Image>()
    lateinit var detailPortfolioFragment: DetailPortfolioFragment
    private lateinit var vibrator: Vibrator
    private val firebaseAuth = FirebaseAuth.getInstance()
    private var user: FirebaseUser? = null
    private var origin: Int? = null  // origin: 0 = From Home, 1 = From Profile
    private var workspace:String? = null
    private var portfolios: ArrayList<Portfolio> = java.util.ArrayList()
    private var portfolioPosition: Int? = null

    companion object {
        const val DETAIL_FRAGMENT_TAG = "DetailPortfolioFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_portfolio)
        Log.d("PortfolioActivity_test", "PortfolioActivity Created")
        origin = intent.getIntExtra("origin", -1)
        portfolioPosition = intent.getIntExtra("position", -1)
        Log.d("tää", portfolioPosition.toString())
        Log.d("PortfolioActivity_test", origin.toString())
        user = firebaseAuth.currentUser
        if (origin == 1){
            getStuffFromFirebaseDB(1, user, null)
        }
        else if (origin == 0){
            workspace = intent.getStringExtra("workspace")
            getStuffFromFirebaseDB(2, user, workspace)
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

        if(user == null){
            detailPortfolioFragment = DetailPortfolioFragment.newInstance(position, page, newList, portfolios, "", true)
        }
        else {
            detailPortfolioFragment = DetailPortfolioFragment.newInstance(position, page, newList, portfolios, user!!.uid, true)
        }

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
            if (workspace.equals("/workspace")){
                query = firebaseDatabase.getReference("portfolio")
            }
            else{
                query = firebaseDatabase.getReference("portfolio").orderByChild("workspace").equalTo(workspace)
            }
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
                        portfolios.add(makePortfolio(it))
                        Log.d("PortfolioActivity_test it: ", it.child("images").toString())
                        //Log.d("PortfolioActivity it:.getValue()", it.child("images").getValue(Image::class.java).toString())

                        newList.add(portfolios.last().images[0])
                    }
                val mfc = PortfolioFragment()
                val b = Bundle()
                b.putSerializable("Parcel", newList)
                b.putSerializable("Portfolios", portfolios)

                if (user == null){
                    b.putString("User", "")
                }
                else {
                    b.putString("User", user.uid)
                    val arr = java.util.ArrayList<FirebaseUser>()
                    arr.add(user)
                    b.putSerializable("fireBaseUser", arr)
                }
                b.putString("portfolio", portfolioPosition!!.toString())
                mfc.arguments = b


                Log.d("tää", "${mfc.arguments} ja ${b.get("portfolio")} ja $portfolioPosition")

                supportFragmentManager.beginTransaction()
                        .add(R.id.root, mfc)
                        .commitAllowingStateLoss()
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }

    fun makePortfolio(dS: DataSnapshot): Portfolio{
        Log.d("makePortfolio_dS", dS.toString())
        Log.d("makePortfolio_dS", dS.child("images").value.toString())
        var date: String = dS.child("date").value.toString()

        var name: String = dS.child("name").value.toString()

        var images: ArrayList<Image> = java.util.ArrayList()
        var numberImages:Long = dS.child("images").childrenCount
        Log.d("makePortfolio_dS", numberImages.toString())
        images.add(Image(dS.child("images").child("0").child("imageId").value.toString(), dS.child("images").child("0").child("imageUrl").value.toString(), dS.child("images").child("0").child("title").value.toString()))
        dS.child("images").children.forEach{
            if (it.key != "0"){
                images.add(Image(it.child("imageId").value.toString(), it.child("imageUrl").value.toString(), it.child("title").value.toString()))
            }
        }

        var pdfs: ArrayList<PDF> = java.util.ArrayList()
        var numberPDF: Long = dS.child("pdfs").childrenCount
        Log.d("numberPDF", numberPDF.toString())
        dS.child("pdfs").children.forEach{
            pdfs.add(PDF(it.child("pdfid").value.toString(), it.child("pdfurl").value.toString(), it.child("title").value.toString()))
        }

        var progresses: ArrayList<Progress> = java.util.ArrayList()
        /*
        var numberProgress: Long = dS.child("progresses").childrenCount
        Log.d("PortfolioActivity_test", "Number of Progress is: " + numberProgress.toString())
        for (i in 1 until numberProgress + 1){
            Log.d("PortfolioActivity_test", "Run loop?")
            var date: String = dS.child("progresses").child("progress$i").child("date").value.toString()

            var progressImages: ArrayList<Image> = java.util.ArrayList()
            var numberImages:Long = dS.child("processes").child("process$i").child("images").childrenCount
            for (i in 1 until numberImages + 1){
                progressImages.add(Image("", dS.child("progresses").child("progress$i").child("images").child("image$i").value.toString(), name))
            }

            var summary: String = dS.child("progresses").child("progress$i").child("summary").value.toString()

            var progressVideos: ArrayList<Video> = java.util.ArrayList()
            var numberVideos:Long = dS.child("processes").child("process$i").child("videos").childrenCount
            for (i in 1 until numberVideos + 1){
                progressVideos.add(Video("", dS.child("progresses").child("progress$i").child("videos").child("video$i").value.toString(), ""))
            }
            Log.d("PortfolioActivity_test", "progress added")
            progresses.add(Progress(date, progressImages, summary, progressVideos))
        }
        */

        var summary: String = dS.child("summary").value.toString()
        var tool: String = dS.child("tool").value.toString()
        var uid: String = dS.child("uid").value.toString()
        var user: String = dS.child("user").value.toString()

        var videos: ArrayList<Video> = java.util.ArrayList()
        var numberVideos:Long = dS.child("videos").childrenCount
        dS.child("videos").children.forEach {
            videos.add(Video(it.child("videoId").value.toString(), it.child("videoUrl").value.toString(), it.child("title").value.toString()))
        }

        var workspace: String = dS.child("workspace").value.toString()

        return Portfolio(date, images, name, pdfs, progresses, summary, tool, uid, user, videos, workspace)
    }
}
