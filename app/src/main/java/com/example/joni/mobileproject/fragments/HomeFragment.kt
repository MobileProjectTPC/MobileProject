package com.example.joni.mobileproject.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
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
import com.example.joni.mobileproject.adapters.SlidingImagesAdapter
import com.example.joni.mobileproject.models.Tool
import com.google.firebase.storage.FirebaseStorage
import com.viewpagerindicator.CirclePageIndicator
import kotlinx.android.synthetic.main.home_fragment_layout.*
import java.io.File

class HomeFragment: Fragment() {

    private var workspace: String? = null
    private var tool: String? = null
    private var dataType: String? = null

    private var dialog: AlertDialog? = null

    private val firebaseStorage = FirebaseStorage.getInstance()

    private lateinit var mPager: ViewPager
    private lateinit var indicator: CirclePageIndicator
    private lateinit var mPortfolioPager: ViewPager
    private lateinit var portfolioIndicator: CirclePageIndicator
    private lateinit var scanButton: Button
    private lateinit var toolsButton: Button
    private lateinit var portfolioButton: Button

    companion object {
        private var currentPage = 0
        const val RECORD_REQUEST_CODE = 1

        const val TOOL_LIST = "TOOL_LIST"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.home_fragment_layout, container, false)

        mPager = rootView.findViewById(R.id.pager)

        val toolList = arguments!!.getSerializable(TOOL_LIST) as java.util.ArrayList<Tool>
        val arrayList = ArrayList<String>()
        toolList.forEach {
            arrayList.add(it.image)
        }
        val urls: Array<String> = arrayList.toArray(arrayOfNulls<String>(arrayList.size))
        mPager.adapter = SlidingImagesAdapter(
                context!!,
                urls
        )

        indicator = rootView.findViewById(R.id.indicator)

        scanButton = rootView.findViewById(R.id.button_scan_qr)

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
