package com.example.joni.mobileproject.fragments

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.joni.mobileproject.PdfActivity
import com.example.joni.mobileproject.R
import com.example.joni.mobileproject.VideoActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.home_fragment_layout, container, false)

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
        btnDownload.setOnClickListener {
            if (workspace != null && tool != null && dataType != null ){
                getStuffFromFirebaseDB(workspace!!, tool!!, dataType!!)
            }
        }

        createTempFile()
    }

    // modify this to get wanted stuff, testing with one image
    // get all data to list and add item selector
    // can also get only the tools for the workspace, just pass empty string for the tool and dataType
    private fun getStuffFromFirebaseDB(workspace: String, tool: String, dataType: String){
        val ref = firebaseDatabase.getReference("$workspace/tools$tool$dataType")
        //showLoadingDialog("Loading image")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {

                p0.children.forEach {
                    Log.d("HomeFragment", "Stuff: $it")
                }

                // test with one image
                /*
                try {
                    val image = p0.children.first().getValue(Image::class.java)
                    val myURL = URL(image?.imageUrl)
                    GetCont().execute(myURL)
                } catch (e:Exception){
                    Log.e("URL", "URL creation",e)
                }
                */
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })
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
            imgView.setImageBitmap(result)
            Handler().post { dialog?.dismiss() }
        }
    }

    // download video or pdf file from firebase and create a tempfile from it
    // display it in another activity
    // tested only with another project because, free limits of firebase have reached
    // TODO test tomorrow, when this projects firebase storage is in use
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
}

class Image(val imageId: String, val imageUrl: String, val title: String){
    constructor(): this("", "", "")
}