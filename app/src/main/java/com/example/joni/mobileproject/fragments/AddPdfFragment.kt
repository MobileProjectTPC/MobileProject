package com.example.joni.mobileproject.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AppCompatImageView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.MediaController
import android.widget.TextView
import android.widget.Toast
import com.example.joni.mobileproject.ProjectCreateActivity
import com.example.joni.mobileproject.R
import com.example.joni.mobileproject.models.Image
import com.example.joni.mobileproject.models.PDF
import com.example.joni.mobileproject.models.Portfolio
import com.example.joni.mobileproject.models.Video
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_project_create.*
import kotlinx.android.synthetic.main.add_image.*
import kotlinx.android.synthetic.main.add_pdf.*
import kotlinx.android.synthetic.main.fragment_detail.*
import kotlinx.android.synthetic.main.profile_fragment_layout.*
import java.io.File
import java.util.*

class AddPdfFragment: Fragment() {

    val REQUEST_PDF = 1
    //var mCurrentPhotoPath: String? = null
    //val pFileName = "temp_photo"
    lateinit var imageFile: File
    lateinit var pictureButton: AppCompatImageView
    lateinit var addPictureButton: Button
    lateinit var projectName: TextView
    lateinit var pdfButton: Button

    private val firebaseStorage = FirebaseStorage.getInstance()
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private var dialog: AlertDialog? = null
    private val viewGroup: ViewGroup? = null
    private var mode: Int = -1
    private var myproject: Portfolio? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.add_pdf, container, false)

        mode = arguments!!.getInt("Mode")

        if (mode == 1){
            myproject = arguments!!.getSerializable("Project") as Portfolio
        }

        pdfButton = rootView.findViewById(R.id.add_pdf)
        pdfButton.setOnClickListener {

            if (text_pdf_title.text.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "application/pdf"
                startActivityForResult(intent, REQUEST_PDF)
            }
            else {
                Toast.makeText(context!!, "Give a title first", Toast.LENGTH_SHORT).show()
            }

        }

        if(mode == 0) {
            projectName = activity!!.findViewById(R.id.final_project_name)
        }

        return rootView

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PDF && resultCode == Activity.RESULT_OK && data != null) {
            //selectedPDFUri = data.data

            val pdf = text_pdf_title.text.toString()

            uploadFileToFirebase(data.data, "pdfs", pdf)
        }

    }

    fun uploadFileToFirebase(uri: Uri, fileType: String, title: String) {
        var project: String
        if (mode == 0) {
            project = projectName.text.toString()
        }
        else{
            project = myproject!!.uid
        }

        val filename = UUID.randomUUID().toString()
        val ref = firebaseStorage.getReference("/portfolio/$project/$filename")

        ref.putFile(uri)
                .addOnSuccessListener {
                    Log.d("TAG", "Successfully uploaded file: ${it.metadata?.path}")

                    ref.downloadUrl.addOnSuccessListener {
                        Log.d("TAG", "File location: $it")
                        saveFileToDatabase(filename, it.toString(), title, project)
                        Handler().post { dialog!!.dismiss() }
                    }

                }
                .addOnFailureListener {
                    Handler().post { dialog!!.dismiss() }
                    Log.d("TAG", "Something went wrong when loading the file")
                }
        activity!!.supportFragmentManager.beginTransaction().remove(this).commit()
        showUploadDialog("Uploading file")
        //Toast.makeText(context!!, "Pdf added to the project ${projectName.text.toString()}", Toast.LENGTH_SHORT).show()
    }


    private fun saveFileToDatabase(fileId: String, fileUrl: String, title: String, project: String) {
        val ref = firebaseDatabase.getReference("/portfolio/$project/pdfs/$fileId")

        //val filename = UUID.randomUUID().toString()
        //val pdf = PDF(filename, fileUrl, title)
        val pdf = PDF(fileId, fileUrl, title)
        //val arrayList = ArrayList<Image>()
        //arrayList.add(image)

        ref.setValue(pdf)
                .addOnSuccessListener {
                    Log.d("TAG", "Image to tool database")
                }
                .addOnFailureListener {
                    Log.d("TAG", "Something went wrong with the database")
                }
    }

    private fun showUploadDialog(message: String) {
        val builder = AlertDialog.Builder(context!!)
        val dialogView = layoutInflater.inflate(R.layout.progress_dialog_layout, null)
        val dialogTxtView = dialogView.findViewById<TextView>(R.id.txtUploadProgress)
        dialogTxtView.text = message
        builder.setView(dialogView)
        builder.setCancelable(false)
        dialog = builder.create()
        dialog!!.show()
    }

}