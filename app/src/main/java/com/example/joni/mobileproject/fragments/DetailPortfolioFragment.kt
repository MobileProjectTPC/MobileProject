package com.example.joni.mobileproject.fragments

import android.app.AlertDialog
import android.content.Intent
import android.databinding.DataBindingUtil
import android.databinding.DataBindingUtil.setContentView
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import com.example.joni.mobileproject.PdfActivity
import com.example.joni.mobileproject.R
import com.example.joni.mobileproject.VideoActivity
import com.example.joni.mobileproject.adapters.DocumentsAdapter
import com.example.joni.mobileproject.adapters.SlidingImageAdapter
import com.example.joni.mobileproject.databinding.FragmentPortfolioDetailBinding
import com.example.joni.mobileproject.models.Image
import com.example.joni.mobileproject.models.ImageModel
import com.example.joni.mobileproject.models.Portfolio
import com.example.joni.mobileproject.models.User
import com.squareup.picasso.Picasso
import com.viewpagerindicator.CirclePageIndicator
import java.io.Serializable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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

    private var summaryImageModelArrayList: java.util.ArrayList<ImageModel>? = null
    val mySummaryImageList = intArrayOf(
            R.drawable.workshop_tutor_logo_text,
            R.drawable.workshop_tutor_logo_text,
            R.drawable.workshop_tutor_logo_text,
            R.drawable.workshop_tutor_logo_text,
            R.drawable.workshop_tutor_logo_text,
            R.drawable.workshop_tutor_logo_text
    )
    private var progressImageModelArrayList: java.util.ArrayList<ImageModel>? = null
    val myProgressImageList = intArrayOf(
            R.drawable.workshop_tutor_logo_text,
            R.drawable.workshop_tutor_logo_text,
            R.drawable.workshop_tutor_logo_text,
            R.drawable.workshop_tutor_logo_text,
            R.drawable.workshop_tutor_logo_text,
            R.drawable.workshop_tutor_logo_text
    )
    private lateinit var mSummaryPager: ViewPager
    private lateinit var summaryIndicator: CirclePageIndicator
    private lateinit var mProgressPager: ViewPager
    private lateinit var progressIndicator: CirclePageIndicator


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

        if (portfolios[position].user == user) {
            binding.btnEdit.visibility = View.VISIBLE
        }
        else{
            binding.btnEdit.visibility = View.INVISIBLE
        }

        binding.summaryText.text = portfolios[position].summary

        binding.progressText.text = portfolios[position].progresses[0].summary


        imageUri = Uri.parse(myList[position].imageUrl)
        Picasso.get()
                .load(imageUri)
                .placeholder(R.drawable.progress_animation)
                .error(R.drawable.workshop_tutor_logo_text)
                .into(binding.image)

        summaryImageModelArrayList = ArrayList()
        summaryImageModelArrayList = populateList(mySummaryImageList)
        progressImageModelArrayList = ArrayList()
        progressImageModelArrayList = populateList(myProgressImageList)

        binding.summaryImagePager.adapter = SlidingImageAdapter(
                context!!,
                this.summaryImageModelArrayList!!
        )

        binding.progressImagePager.adapter = SlidingImageAdapter(
                context!!,
                this.progressImageModelArrayList!!
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

        Log.d("DocumentAdapter_pdfs", portfolios[position].pdfs.size.toString())
        var adapter = DocumentsAdapter(activity!!.applicationContext, portfolios[position].pdfs)
        binding.listViewDocuments?.adapter = adapter
        getListViewSize(binding.listViewDocuments)

        binding.listViewDocuments.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            // value of item that is clicked
            //val itemValue = binding.listViewDocuments.getItemAtPosition(position) as String

            createTempFile("pdfs", "5d713890-159b-404e-b5c8-7c630a36d772.pdf")
        }

        binding.progressImageIndicator.setViewPager(binding.progressImagePager)

        //Set circle indicator radius
        binding.progressImageIndicator.radius = 5 * density

        // Pager listener over indicator
        binding.progressImageIndicator.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageSelected(position: Int) {
                //HomeFragment.currentPage = position
            }

            override fun onPageScrolled(pos: Int, arg1: Float, arg2: Int) {

            }

            override fun onPageScrollStateChanged(pos: Int) {

            }
        })

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
    private fun createTempFile(dataType: String, fileId: String){
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
}