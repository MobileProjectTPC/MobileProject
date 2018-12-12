package com.example.joni.mobileproject.fragments

import android.app.AlertDialog
import android.app.FragmentManager
import android.content.Intent
import android.databinding.DataBindingUtil
import android.databinding.DataBindingUtil.setContentView
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.joni.mobileproject.*
import com.example.joni.mobileproject.adapters.DocumentsAdapter
import com.example.joni.mobileproject.adapters.DocumentsEditAdapter
import com.example.joni.mobileproject.adapters.SlidingImageAdapter
import com.example.joni.mobileproject.adapters.SlidingImageVideoAdapter
import com.example.joni.mobileproject.databinding.FragmentPortfolioDetailBinding
import com.example.joni.mobileproject.models.*
import com.squareup.picasso.Picasso
import com.viewpagerindicator.CirclePageIndicator
import java.io.Serializable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class DetailPortfolioFragment : Fragment() {

    private lateinit var binding: FragmentPortfolioDetailBinding
    private var position: Int = 0
    private var page: Int = 0
    private var myList: ArrayList<Image> = java.util.ArrayList()
    private var portfolios: ArrayList<Portfolio> = java.util.ArrayList()
    private lateinit var imageUri: Uri
    private var user:String =""
    private var nfcTrue = false
    private var dialog: AlertDialog? = null
    private val firebaseStorage = FirebaseStorage.getInstance()
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private var userCreated: Boolean = false

    private var summaryImageVideoModelArrayList: java.util.ArrayList<ImageModel>? = null
    val mySummaryImageList = intArrayOf(
            R.drawable.workshop_tutor_logo_text,
            R.drawable.workshop_tutor_logo_text,
            R.drawable.workshop_tutor_logo_text,
            R.drawable.workshop_tutor_logo_text,
            R.drawable.workshop_tutor_logo_text,
            R.drawable.workshop_tutor_logo_text
    )
    private var summaryImageVideoArrayList: java.util.ArrayList<ImageVideo>? = null

    private val editMainPictureFragment = EditMainPictureFragment()
    private val addPictureFragment = AddPictureFragment()
    private val addVideoFragment = AddVideoFragment()
    private val addPdfFragment = AddPdfFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("DetailPortfolioFragment_test", "onCreate()")

        arguments?.let {
            position = it.getInt(EXTRA_POSITION)
            page = it.getInt(EXTRA_PAGE)
            myList = it.getSerializable(MY_LIST) as java.util.ArrayList<Image>
            portfolios = it.getSerializable(MY_PORTFOLIOS) as java.util.ArrayList<Portfolio>
            user = it.getString(USER)
            nfcTrue = it.getBoolean(NFC_TRUE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (nfcTrue) { activity!!.finish()}
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_portfolio_detail, container, false)
        binding.image.transitionName = "${getString(R.string.transition_image)}_${page}_$position"
        binding.text.transitionName = "${getString(R.string.transition_text)}_${page}_$position"
        binding.text.text = myList[position].title

        var firebaseData = FirebaseDatabase.getInstance().reference

        if (portfolios[position].user == user) {
            binding.editMainImage.visibility = View.VISIBLE
            binding.btnAddImage.visibility = View.VISIBLE
            binding.btnAddVideo.visibility = View.VISIBLE
            binding.btnAddPDF.visibility = View.VISIBLE
            binding.btnDelete.visibility = View.VISIBLE
            userCreated = true
        }
        else{
            binding.editMainImage.visibility = View.INVISIBLE
            binding.btnAddImage.visibility = View.INVISIBLE
            binding.btnAddVideo.visibility = View.INVISIBLE
            binding.btnAddPDF.visibility = View.INVISIBLE
            binding.btnDelete.visibility = View.INVISIBLE

        }


        binding.editMainImage.setOnClickListener {

            var arguments: Bundle = Bundle()
            arguments.putSerializable("Project", portfolios[position])

            editMainPictureFragment.arguments = arguments
            fragmentManager!!.beginTransaction()
                    .replace(R.id.placeholder, editMainPictureFragment).commit()

        }

        if (portfolios[position].summary != null || portfolios[position].summary.toString() != "null") {
            binding.summaryText.text = portfolios[position].summary
        }
        else{
            binding.summary.visibility = View.INVISIBLE
            binding.summaryText.visibility = View.INVISIBLE
        }

        binding.btnAddImage.setOnClickListener {
            var arguments: Bundle = Bundle()
            arguments.putInt("Mode", 1) // Mode: 1 = Add new pictures from existing project
            arguments.putSerializable("Project", portfolios[position])

            addPictureFragment.arguments = arguments
            fragmentManager!!.beginTransaction()
                    .replace(R.id.placeholder, addPictureFragment).commit()
        }

        binding.btnAddVideo.setOnClickListener {
            var arguments: Bundle = Bundle()
            arguments.putInt("Mode", 1) // Mode: 1 = Add new pictures from existing project
            arguments.putSerializable("Project", portfolios[position])

            addVideoFragment.arguments = arguments
            fragmentManager!!.beginTransaction()
                    .replace(R.id.placeholder, addVideoFragment).commit()
        }


        imageUri = Uri.parse(myList[position].imageUrl)
        Picasso.get()
                .load(imageUri)
                .placeholder(R.drawable.progress_animation)
                .error(R.drawable.workshop_tutor_logo_text)
                .into(binding.image)

        summaryImageVideoModelArrayList = ArrayList()
        summaryImageVideoModelArrayList = populateList(mySummaryImageList)

        if (portfolios[position].images != null || portfolios[position].videos != null) {
            summaryImageVideoArrayList = ArrayList()
            summaryImageVideoArrayList = makeList(portfolios[position].images, portfolios[position].videos!!)

            binding.summaryImagePager.adapter = SlidingImageVideoAdapter(
                    context!!,
                    this.summaryImageVideoArrayList!!,
                    this,
                    userCreated,
                    portfolios[position]
            )

            binding.summaryImageIndicator.setViewPager(binding.summaryImagePager)

            val density = resources.displayMetrics.density

            //Set circle indicator radius
            binding.summaryImageIndicator.radius = 5 * density

            // Pager listener over indicator
            binding.summaryImageIndicator.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {

                override fun onPageSelected(position: Int) {
                    //HomeFragment.currentPage = position
                }

                override fun onPageScrolled(pos: Int, arg1: Float, arg2: Int) {

                }

                override fun onPageScrollStateChanged(pos: Int) {

                }
            })
        }
        else{
            binding.summaryImagePager.visibility = View.VISIBLE
            binding.summaryImageIndicator.visibility = View.INVISIBLE
        }
        /*
        http://developine.com/develop-android-image-gallery-app-kotlin-with-source-code/
        https://www.nplix.com/create-animated-video-thumbnail-android/
        Glide.with(context)
                    .load(urlofVideo)
                    .centerCrop()
                    .placeholder(anybackgroundColor)
                    .crossFade()
                    .into(ImagViewtoload);

        private void setScaleAnimation(View view) {
            ScaleAnimation anim = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            anim.setDuration(FADE_DURATION);
            view.startAnimation(anim);
        }
        private final static int FADE_DURATION = 1000;
        setScaleAnimation(((VideoViewHolder) holder).vImage);
        setScaleAnimation(imageView);
        */
        binding.btnAddPDF.setOnClickListener {
            var arguments: Bundle = Bundle()
            arguments.putInt("Mode", 1) // Mode: 1 = Add new PDFs from existing project
            arguments.putSerializable("Project", portfolios[position])

            addPdfFragment.arguments = arguments
            fragmentManager!!.beginTransaction()
                    .replace(R.id.placeholder, addPdfFragment).commit()
        }


        if (portfolios[position].pdfs != null) {
            Log.d("DocumentAdapter_pdfs", portfolios[position].pdfs!!.size.toString())
            var adapter = DocumentsAdapter(context!!, portfolios[position].pdfs!!, userCreated, portfolios[position])

            binding.listViewDocuments?.adapter = adapter
            getListViewSize(binding.listViewDocuments)

            binding.listViewDocuments.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                // value of item that is clicked
                //val itemValue = binding.listViewDocuments.getItemAtPosition(position) as String

                createTempFile("pdfs", "5d713890-159b-404e-b5c8-7c630a36d772.pdf")
            }
        }

        binding.btnDelete.setOnClickListener{
            val builder: AlertDialog.Builder
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert)
            } else {
                builder = AlertDialog.Builder(context)
            }
            builder.setTitle("Delete Project")
                    .setMessage("Are you sure you want to delete this project? \nThis cannot be undone!")
                    .setPositiveButton(android.R.string.yes) { dialog, which ->
                        // continue with delete
                        firebaseData.child("portfolio").child(portfolios[position].uid!!).removeValue()
                        Toast.makeText(context, "The project has been deleted", Toast.LENGTH_LONG).show()
                        activity!!.supportFragmentManager.beginTransaction().remove(this).commit()
                    }
                    .setNegativeButton(android.R.string.no) { dialog, which ->
                        // do nothing
                    }
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
        }


        return binding.root
    }


    private fun populateList(imagelist: IntArray): java.util.ArrayList<ImageModel> {

        val list = java.util.ArrayList<ImageModel>()

        for (i in 0..5) {
            val imageModel = ImageModel()
            imageModel.setImageDrawables(imagelist[i])
            list.add(imageModel)
        }
        return list
    }

    companion object {

        private const val EXTRA_POSITION = "com.example.joni.mobileproject#POSITION"
        private const val EXTRA_PAGE = "com.example.joni.mobileproject#PAGE"
        private const val MY_LIST = "mylist"
        private const val MY_PORTFOLIOS = "portfolios"
        private const val USER = "user"
        private const val NFC_TRUE = "nfc"

        fun newInstance(position: Int, page: Int, myList: Serializable, portfolios: Serializable, user: String, nfcTrue: Boolean): DetailPortfolioFragment {
            return DetailPortfolioFragment().apply {
                arguments = Bundle().apply {
                    putInt(EXTRA_POSITION, position)
                    putInt(EXTRA_PAGE, page)
                    putSerializable(MY_LIST, myList)
                    putSerializable(MY_PORTFOLIOS, portfolios)
                    putString(USER, user)
                    putBoolean(NFC_TRUE, nfcTrue)
                }
            }
        }
    }

    // download video or pdf file from firebase and create a tempfile from it
    // display it in another activity
    // dataType = pdfs or videos
    fun createTempFile(dataType: String, fileId: String){
        showLoadingDialog("Loading file")

        val ref = firebaseStorage.reference.child("/$dataType/$fileId")
        val localFile = File.createTempFile("file", "")
        localFile.deleteOnExit()

        if (dataType.equals("videos")){
            Log.d("TAG", "Here should be the loaded file: ${localFile.absolutePath}")

            ref.getFile(localFile).addOnSuccessListener {
                Log.d("TAG", "Get some: $it")
                val intent = Intent(activity, VideoActivity::class.java)
                intent.putExtra("videofile", localFile.absolutePath)
                Handler().post { dialog?.dismiss() }
                startActivity(intent)
            }.addOnFailureListener {
                Log.d("HomeFragment", "Something fucked: $it")
                localFile.delete()
                Handler().post { dialog?.dismiss() }
            }
        }
        else if (dataType.equals("pdfs")){
            Log.d("TAG", "Here should be the loaded file: ${localFile.absolutePath}")

            ref.getFile(localFile).addOnSuccessListener {
                Log.d("TAG", "Get some: $it")
                val intent = Intent(activity, PdfActivity::class.java)
                intent.putExtra("pdffile", localFile.absolutePath)
                Handler().post { dialog?.dismiss() }
                startActivity(intent)
            }.addOnFailureListener {
                Log.d("HomeFragment", "Something fucked: $it")
                localFile.delete()
                Handler().post { dialog?.dismiss() }
            }
        }
    }

    private fun showLoadingDialog(message: String) {
        val builder = AlertDialog.Builder(context)
        val dialogView = layoutInflater.inflate(R.layout.progress_dialog_layout, null)
        val dialogTxtView = dialogView.findViewById<TextView>(R.id.txtUploadProgress)
        dialogTxtView.text = message
        builder.setView(dialogView)
        builder.setCancelable(false)
        dialog = builder.create()
        dialog!!.show()
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
            list.add(ImageVideo(images[i].imageUrl, images[i].imageId, false))
        }
        for (i in 0 until videos.size){
            list.add(ImageVideo(videos[i].videoUrl, videos[i].videoId, true))
        }
        return list
    }

    /*
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == 2) {
            activity!!.fragmentManager.beginTransaction().remove(this).commit()
        }
    }
    */

    fun refresh(position: Int){
        var query = firebaseDatabase.getReference("portfolio").child(portfolios[position].uid)
        var updatePortfolio: Portfolio
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                updatePortfolio = (activity as PortfolioActivity).makePortfolio(p0)
                portfolios[position] = updatePortfolio
                binding.summaryText.text = portfolios[position].summary

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }
}