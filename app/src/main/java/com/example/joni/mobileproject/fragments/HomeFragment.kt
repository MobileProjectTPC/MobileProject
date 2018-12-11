package com.example.joni.mobileproject.fragments


import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.joni.mobileproject.*
import com.example.joni.mobileproject.adapters.SlidingImageAdapter
import com.example.joni.mobileproject.adapters.SlidingImage_Adapter
import com.example.joni.mobileproject.models.Image
import com.example.joni.mobileproject.models.ImageModel
import com.example.joni.mobileproject.models.Tool
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.viewpagerindicator.CirclePageIndicator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.home_fragment_layout.*
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class HomeFragment: Fragment() {

    private var workspace: String? = null
    private var tool: String? = null
    private var dataType: String? = null

    private var dialog: AlertDialog? = null

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val firebaseStorage = FirebaseStorage.getInstance()

    private var imageModelArrayList: java.util.ArrayList<ImageModel>? = null
    private val myImageList = intArrayOf(
            R.drawable.workshop_tutor_logo_text,
            R.drawable.workshop_tutor_logo_text,
            R.drawable.workshop_tutor_logo_text,
            R.drawable.workshop_tutor_logo_text,
            R.drawable.workshop_tutor_logo_text,
            R.drawable.workshop_tutor_logo_text
    )
    private var portfolioImageModelArrayList: java.util.ArrayList<ImageModel>? = null
    private val myPortfolioImageList = intArrayOf(
            R.drawable.workshop_tutor_logo_text,
            R.drawable.workshop_tutor_logo_text,
            R.drawable.workshop_tutor_logo_text,
            R.drawable.workshop_tutor_logo_text,
            R.drawable.workshop_tutor_logo_text,
            R.drawable.workshop_tutor_logo_text
    )
    private lateinit var mPager: ViewPager
    private lateinit var indicator: CirclePageIndicator
    private lateinit var mPortfolioPager: ViewPager
    private lateinit var portfolioIndicator: CirclePageIndicator
    private lateinit var scanButton: Button
    private lateinit var toolsButton: Button
    private lateinit var portfolioButton: Button

    val mutableList : MutableList<Image> = ArrayList()

    companion object {
        private var currentPage = 0
        const val RECORD_REQUEST_CODE = 1

        const val TOOL_LIST = "TOOL_LIST"

        /*
        fun newInstance(myList: Serializable): DetailFragment {
            return DetailFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(TOOL_LIST, myList)
                }
            }
        }
        */
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.home_fragment_layout, container, false)

        imageModelArrayList = ArrayList()
        imageModelArrayList = populateList(myImageList)
        portfolioImageModelArrayList = ArrayList()
        portfolioImageModelArrayList = populateList(myPortfolioImageList)

        mPager = rootView.findViewById(R.id.pager)

        val toolList = arguments!!.getSerializable(TOOL_LIST) as java.util.ArrayList<Tool>
        val arrayList = ArrayList<String>()
        toolList.forEach {
            arrayList.add(it.image)
        }
        val urls: Array<String> = arrayList.toArray(arrayOfNulls<String>(arrayList.size))
        mPager.adapter = SlidingImage_Adapter(
                context!!,
                urls
        )

        indicator = rootView.findViewById(R.id.indicator)


        mPortfolioPager = rootView.findViewById(R.id.portfolioPager)
        mPortfolioPager.adapter = SlidingImageAdapter(
                context!!,
                this.portfolioImageModelArrayList!!
        )

        portfolioIndicator = rootView.findViewById(R.id.portfolioIndicator)

        //tvResult = rootView.findViewById(R.id.tvresult)

        //6098530220cc996d7d8bc83c72e0b5bcae6195fa?

        scanButton = rootView.findViewById(R.id.button_scan_qr)


        //setupPermissions()

        init()

        toolsButton = rootView.findViewById(R.id.btn_tools)
        toolsButton.setOnClickListener {
            val intent = Intent(context, ToolsActivity::class.java)
            startActivity(intent)
        }

        portfolioButton = rootView.findViewById(R.id.btnPortfolio)
        portfolioButton.setOnClickListener {
            val intent = Intent(context, PortfolioActivity::class.java)
            intent.putExtra("origin", 0)
            intent.putExtra("workspace", workspace)
            startActivity(intent)
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // select workspace and assign it to workspace variable
        // remember put / first
        workspace = "/workspace"
        // scan the NFC-tag and assign value to tool variable
        // remember put / first
        tool = "/tool1"
        // define what you want to get from that tool, images, videos, pdfs
        // remember put / first
        dataType = "/videos"

        Log.d("HomeFragment", "HomeFragment created")

        // replace the download button with NFC scanner
        //
        btnDownloadPdf.setOnClickListener {
            /*
            if (workspace != null && tool != null && dataType != null ){
                getStuffFromFirebaseDB(workspace!!, tool!!, dataType!!)
            }
            */
            createTempFile("pdfs", "5d713890-159b-404e-b5c8-7c630a36d772.pdf")
        }
        btnDownloadVideo.setOnClickListener {
            createTempFile("videos", "df3ba79c-7ec2-4136-ab10-e9f52b78f683")
        }
    }

    /* TODO NEVER USED, BUT LEAVE IT HERE IF NEEDED, ERASE LATER
    // modify this to get wanted stuff, testing with one image
    // get all data to list and add item selector
    // can also get only the tools for the workspace, just pass empty string for the tool and dataType
    private fun getStuffFromFirebaseDB(workspace: String, tool: String, dataType: String){
        val ref = firebaseDatabase.getReference("/$workspace/tools/$tool/$dataType")
        //showLoadingDialog("Loading image")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {

                p0.children.forEach {
                    Log.d("HomeFragment", "Stuff: $it")
                    if (mutableList.isEmpty()) {
                        mutableList.add(it.getValue(Image::class.java)!!)
                    }

                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }



    // AsyncTask for loading and displaying selected image
    inner class GetCont: AsyncTask<URL, Unit, Bitmap>() {

        override fun doInBackground(vararg url: URL?): Bitmap {
            lateinit var bm: Bitmap
            try {
                val myConn = url[0]!!.openConnection() as HttpURLConnection
                val istream: InputStream = myConn.inputStream
                bm = BitmapFactory.decodeStream(istream)
                myConn.disconnect()
            } catch (e:Exception) {
                Log.e("Connection", "Reading error", e)
            }
            return bm
        }

        override fun onPostExecute(result: Bitmap) {
            expandedImage.setImageBitmap(result)
            //Handler().post { dialog?.dismiss() }
        }
    }
    */
    
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

    // download video or pdf file from firebase and create a tempfile from it
    // display it in another activity
    // dataType = pdfs or videos
    private fun createTempFile(dataType: String, fileId: String){
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

    private fun populateList(imagelist: IntArray): java.util.ArrayList<ImageModel> {

        val list = java.util.ArrayList<ImageModel>()

        for (i in 0..5) {
            val imageModel = ImageModel()
            imageModel.setImageDrawables(imagelist[i])
            list.add(imageModel)
        }

        return list
    }

    private fun init() {

        scanButton.setOnClickListener {
            val permission = ContextCompat.checkSelfPermission(context!!,
                    Manifest.permission.CAMERA)
            if (permission != PackageManager.PERMISSION_GRANTED) {
                makeRequestCamera()
            }
            else {
                val intent = Intent(context!!, ScanActivity::class.java)
                startActivity(intent)
            }
        }

        indicator.setViewPager(mPager)

        val density = resources.displayMetrics.density

        //Set circle indicator radius
        indicator.radius = 5 * density

        // Pager listener over indicator
        indicator.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageSelected(position: Int) {
                currentPage = position
            }

            override fun onPageScrolled(pos: Int, arg1: Float, arg2: Int) {

            }

            override fun onPageScrollStateChanged(pos: Int) {

            }


        })


        portfolioIndicator.setViewPager(mPortfolioPager)

        //Set circle indicator radius
        portfolioIndicator.radius = 5 * density

        // Pager listener over indicator
        portfolioIndicator.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageSelected(position: Int) {
                currentPage = position
            }

            override fun onPageScrolled(pos: Int, arg1: Float, arg2: Int) {

            }

            override fun onPageScrollStateChanged(pos: Int) {

            }


        })

    }

    /*
    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(context!!,
                Manifest.permission.CAMERA)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            makeRequestCamera()
        }
    }
    */
    private fun makeRequestCamera() {
        Log.d("MainActivity", "makeRCamera")
        ActivityCompat.requestPermissions(activity!!,
                arrayOf(Manifest.permission.CAMERA),
                RECORD_REQUEST_CODE
        )
    }





}
