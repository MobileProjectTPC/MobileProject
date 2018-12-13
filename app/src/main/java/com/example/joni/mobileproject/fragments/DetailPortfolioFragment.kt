package com.example.joni.mobileproject.fragments

import android.app.AlertDialog
import android.content.Intent
import android.databinding.DataBindingUtil
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
import com.example.joni.mobileproject.adapters.SlidingImageVideoAdapter
import com.example.joni.mobileproject.databinding.FragmentPortfolioDetailBinding
import com.example.joni.mobileproject.models.*
import com.squareup.picasso.Picasso
import java.io.Serializable
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import okhttp3.internal.Internal.instance
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
    private val mySummaryImageList = intArrayOf(
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
        if (!nfcTrue) {
            activity!!.finish()
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_portfolio_detail, container, false)
        binding.image.transitionName = "${getString(R.string.transition_image)}_${page}_$position"
        binding.text.transitionName = "${getString(R.string.transition_text)}_${page}_$position"
        binding.text.text = myList[position].title

        val firebaseData = FirebaseDatabase.getInstance().reference

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

            val arguments = Bundle()
            arguments.putSerializable("Project", portfolios[position])
            arguments.putString("position", position.toString())

            editMainPictureFragment.arguments = arguments
            fragmentManager!!.beginTransaction()
                    .replace(R.id.placeholder, editMainPictureFragment).commit()
        }

        /*
        Log.d("DetailPortfolioFragment_test", "portfolios[position].summary: " + portfolios[position].summary)
        if (portfolios[position].summary != null || portfolios[position].summary.toString() != "null") {
            binding.summaryText.text = portfolios[position].summary
        }
        else{
            binding.summaryText.text = ""
            binding.summary.visibility = View.INVISIBLE
            binding.summaryText.visibility = View.INVISIBLE
        }
        */



        binding.btnAddImage.setOnClickListener {
            val arguments = Bundle()
            arguments.putInt("Mode", 1) // Mode: 1 = Add new pictures from existing project
            arguments.putSerializable("Project", portfolios[position])
            arguments.putString("position", position.toString())

            addPictureFragment.arguments = arguments
            fragmentManager!!.beginTransaction()
                    .replace(R.id.placeholder, addPictureFragment).commit()
        }

        binding.btnAddVideo.setOnClickListener {
            val arguments = Bundle()
            arguments.putInt("Mode", 1) // Mode: 1 = Add new pictures from existing project
            arguments.putSerializable("Project", portfolios[position])
            arguments.putString("position", position.toString())

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

        Log.d("DetailPortfoliofragment_test", "imagesize: " + portfolios[position].images.size)
        Log.d("DetailPortfoliofragment_test", "videosize: " + portfolios[position].videos!!.size)
        if (portfolios[position].images.size - 1 > 0 || portfolios[position].videos!!.size > 0) {
            binding.noImageVideoMessage.visibility = View.INVISIBLE
            Log.d("DetailPortfoliofragment_test", "portfolios[position].images is not null" )
            summaryImageVideoArrayList = ArrayList()
            summaryImageVideoArrayList = makeList(portfolios[position].images, portfolios[position].videos!!)

            binding.summaryImagePager.adapter = SlidingImageVideoAdapter(
                    context!!,
                    this.summaryImageVideoArrayList!!,
                    this,
                    userCreated,
                    portfolios[position],
                    position,
                    activity!!
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
            Log.d("DetailPortfoliofragment_test", "portfolios[position].images is null" )
            binding.summaryImagePager.visibility = View.INVISIBLE
            binding.summaryImageIndicator.visibility = View.INVISIBLE
        }

        binding.btnAddPDF.setOnClickListener {
            val arguments = Bundle()
            arguments.putInt("Mode", 1) // Mode: 1 = Add new PDFs from existing project
            arguments.putSerializable("Project", portfolios[position])
            arguments.putString("position", position.toString())

            addPdfFragment.arguments = arguments
            fragmentManager!!.beginTransaction()
                    .replace(R.id.placeholder, addPdfFragment).commit()
        }

        if (portfolios[position].pdfs!!.size > 0) {
            binding.noPDFMessage.visibility = View.INVISIBLE

            val adapter = DocumentsAdapter(context!!, portfolios[position].pdfs!!, userCreated, portfolios[position])

            binding.listViewDocuments.adapter = adapter
            getListViewSize(binding.listViewDocuments)

            binding.listViewDocuments.onItemClickListener = AdapterView.OnItemClickListener { _, _, listViewPosition, _ ->
                createTempFile("pdfs", portfolios[position].pdfs!![listViewPosition].PDFId)
            }
        }

        binding.btnDelete.setOnClickListener{
            val builder: AlertDialog.Builder = if (Build.VERSION.SDK_INT >= 26) {
                AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert)
            } else {
                AlertDialog.Builder(context)
            }
            builder.setTitle("Delete Project")
                    .setMessage("Are you sure you want to delete this project? \nThis cannot be undone!")
                    .setPositiveButton(android.R.string.yes) { _, _ ->
                        // continue with delete
                        firebaseData.child("portfolio").child(portfolios[position].uid).removeValue()
                        Toast.makeText(context, "The project has been deleted", Toast.LENGTH_LONG).show()
                        activity!!.supportFragmentManager.beginTransaction().remove(this).commit()
                    }
                    .setNegativeButton(android.R.string.no) { _, _ ->
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

        if (dataType == "videos"){
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
        else if (dataType == "pdfs"){
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

    private fun getListViewSize(myListView: ListView) {
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
        val list: ArrayList<ImageVideo> = java.util.ArrayList()
        for (i in 1 until images.size){
            list.add(ImageVideo(images[i].imageUrl, images[i].imageId, false))
        }
        for (i in 0 until videos.size){
            list.add(ImageVideo(videos[i].videoUrl, videos[i].videoId, true))
        }
        return list
    }

    fun refresh(project: String) {
        var query = firebaseDatabase.getReference("portfolio").child(project)

        fun refresh(position: Int) {
            val query = firebaseDatabase.getReference("portfolio").child(portfolios[position].uid)

            var updatePortfolio: Portfolio
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    updatePortfolio = makePortfolio(p0)

                    fragmentManager!!.beginTransaction().replace(R.id.fragmentContainer, DetailPortfolioFragment(), MainActivity.HOME_FRAGMENT_TAG).commit()
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })

            //Log.d("refresh()_test", "refresh()")
            //activity!!.supportFragmentManager.beginTransaction().remove(this).commit()
        }
    }
    fun makePortfolio(dS: DataSnapshot): Portfolio{
        val date: String = dS.child("date").value.toString()

        val name: String = dS.child("name").value.toString()

        val images: ArrayList<Image> = java.util.ArrayList()
        images.add(Image(dS.child("images").child("0").child("imageId").value.toString(), dS.child("images").child("0").child("imageUrl").value.toString(), dS.child("images").child("0").child("title").value.toString()))
        dS.child("images").children.forEach{
            if (it.key != "0"){
                images.add(Image(it.child("imageId").value.toString(), it.child("imageUrl").value.toString(), it.child("title").value.toString()))
            }
        }

        val pdfs: ArrayList<PDF> = java.util.ArrayList()
        dS.child("pdfs").children.forEach{
            pdfs.add(PDF(it.child("pdfid").value.toString(), it.child("pdfurl").value.toString(), it.child("title").value.toString()))
        }

        val progresses: ArrayList<Progress> = java.util.ArrayList()

        val summary: String = dS.child("summary").value.toString()
        val tool: String = dS.child("tool").value.toString()
        val uid: String = dS.child("uid").value.toString()
        val user: String = dS.child("user").value.toString()

        val videos: ArrayList<Video> = java.util.ArrayList()
        dS.child("videos").children.forEach {
            videos.add(Video(it.child("videoid").value.toString(), it.child("videoUrl").value.toString(), it.child("title").value.toString()))
        }

        val workspace: String = dS.child("workspace").value.toString()

        return Portfolio(date, images, name, pdfs, progresses, summary, tool, uid, user, videos, workspace)
    }



}
