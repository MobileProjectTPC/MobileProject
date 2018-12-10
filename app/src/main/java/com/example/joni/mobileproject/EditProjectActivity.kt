package com.example.joni.mobileproject

import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Vibrator
import android.provider.MediaStore
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.transition.ChangeBounds
import android.transition.ChangeClipBounds
import android.transition.ChangeTransform
import android.transition.TransitionSet
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.joni.mobileproject.adapters.*
import com.example.joni.mobileproject.fragments.*
import com.example.joni.mobileproject.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.viewpagerindicator.CirclePageIndicator

class EditProjectActivity : AppCompatActivity() {

    private val editMainPictureFragment = EditMainPictureFragment()
    private val addVideoFragment = AddVideoFragment()
    //private val addPdfFragment = AddPdfFragment()

    private lateinit var mSummaryPager: ViewPager
    private lateinit var summaryIndicator: CirclePageIndicator
    private lateinit var mProgressPager: ViewPager
    private lateinit var progressIndicator: CirclePageIndicator

    private lateinit var listViewDocuments: ListView

    private var summaryImageVideoArrayList: java.util.ArrayList<ImageVideo>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_project)

        Log.d("EditProjectActivity_Test", "Set Text?")

        var project: Portfolio = intent.getSerializableExtra("Project") as Portfolio

        val image: ImageView = findViewById(R.id.image)
        Picasso.get().load(project.images[0].imageUrl).into(image)

        val editMainImage: ImageView = findViewById(R.id.editMainImage)
        editMainImage.setOnClickListener {

            var arguments: Bundle = Bundle()
            arguments.putSerializable("Project", project)

            editMainPictureFragment.arguments = arguments
            supportFragmentManager.beginTransaction()
                    .replace(R.id.placeholder, editMainPictureFragment).commit()

        }

        val title: EditText = findViewById(R.id.title)
        Log.d("EditProjectActivity_Test", "Set Text?")
        //title.setText(project.name)
        title.setText(project.name)

        val summaryText: EditText = findViewById(R.id.summaryText)
        summaryText.setText(project.summary)

        mSummaryPager = findViewById(R.id.summaryImagePager)
        summaryIndicator = findViewById(R.id.summaryImageIndicator)

        summaryImageVideoArrayList = makeList(project.images, project.videos!!)

        mSummaryPager.adapter = SlidingImageVideoEditAdapter(
                this, summaryImageVideoArrayList!!
        )

        summaryIndicator.setViewPager(mSummaryPager)

        val density = resources.displayMetrics.density

        //Set circle indicator radius
        summaryIndicator.radius = 5 * density

        // Pager listener over indicator
        summaryIndicator.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageSelected(position: Int) {
                //HomeFragment.currentPage = position
            }

            override fun onPageScrolled(pos: Int, arg1: Float, arg2: Int) {

            }

            override fun onPageScrollStateChanged(pos: Int) {

            }
        })



        var adapter = DocumentsEditAdapter(this, project.pdfs!!)

        listViewDocuments = findViewById(R.id.listViewDocuments)

        listViewDocuments.adapter = adapter
        getListViewSize(listViewDocuments)

    }

    fun getListViewSize(myListView: ListView) {
        val myListAdapter = myListView.adapter
                ?: //do nothing return null
                return
        //set listAdapter in loop for getting final size
        var totalHeight = 0
        for (size in 0 until myListAdapter.count) {
            val listItem = myListAdapter.getView(size, null, myListView)
            listItem.measure(0, 0)
            totalHeight += listItem.measuredHeight
        }
        //setting listview item in adapter
        val params = myListView.layoutParams
        params.height = totalHeight + myListView.dividerHeight * (myListAdapter.count - 1)
        myListView.layoutParams = params
        // print height of adapter on log
        Log.i("height of listItem:", totalHeight.toString())
    }

    private fun makeList(images: ArrayList<Image>, videos: ArrayList<Video>): java.util.ArrayList<ImageVideo>{
        var list: ArrayList<ImageVideo> = java.util.ArrayList()
        for (i in 1 until images.size){
            list.add(ImageVideo(images[i].imageUrl, false))
        }
        for (i in 0 until videos.size){
            list.add(ImageVideo(videos[i].videoUrl, true))
        }
        return list
    }
}