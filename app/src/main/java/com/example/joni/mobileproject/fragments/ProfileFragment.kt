package com.example.joni.mobileproject.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.joni.mobileproject.ProjectCreateActivity
import com.example.joni.mobileproject.PortfolioActivity
import com.example.joni.mobileproject.R
import com.example.joni.mobileproject.models.Image
import com.example.joni.mobileproject.models.PDF
import com.example.joni.mobileproject.models.Video
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.profile_fragment_layout.*
import java.util.*

class ProfileFragment: Fragment() {

    private val imageRequestCode = 0
    private val PDFRequestCode = 1
    private val videoRequestCode = 2

    private var selectedPhotoUri: Uri? = null
    private var selectedPDFUri: Uri? = null
    private var selectedVideoUri: Uri? = null

    private var dialog: AlertDialog? = null
    private val viewGroup: ViewGroup? = null

    private val firebaseStorage = FirebaseStorage.getInstance()
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()

    private lateinit var toolbar: Toolbar




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        if (!firebaseAuth.currentUser!!.isEmailVerified) {
            fragmentManager!!.beginTransaction().replace(R.id.fragmentContainer, EmailNotVerifiedFragment()).commit()
        }

        val rootView = inflater.inflate(R.layout.profile_fragment_layout, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val user = firebaseAuth.currentUser

        Log.d("ProfileFragment", "ProfileFragment created")

        getUserInformation(user!!.uid)


        btnUploadImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, imageRequestCode)
        }

        btnUploadPDF.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/pdf"
            startActivityForResult(intent, PDFRequestCode)
        }

        btnUploadVideo.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "video/*"
            startActivityForResult(intent, videoRequestCode)
        }

        btnSignout.setOnClickListener {
            signOut()
        }

        btnUpdate.setOnClickListener {
            fragmentManager!!.beginTransaction().replace(R.id.fragmentContainer, UpdateUserDataFragment()).commit()
        }


        btnCreateProject.setOnClickListener {
            val projectIntent = Intent(context!!, ProjectCreateActivity::class.java)
            startActivity(projectIntent)
        }
        btnMyPortfolio.setOnClickListener {
            val intent = Intent(context, PortfolioActivity::class.java)
            intent.putExtra("origin",1)
            startActivity(intent)
        }

        super.onViewCreated(view, savedInstanceState)
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == imageRequestCode && resultCode == Activity.RESULT_OK && data != null) {
            //selectedPhotoUri = data.data
            uploadFileToFirebase(data.data, "images", "ImageTitle")
        } else if (requestCode == PDFRequestCode && resultCode == Activity.RESULT_OK && data != null) {
            //selectedPDFUri = data.data
            uploadFileToFirebase(data.data, "pdfs", "PDFTitle")
        } else if (requestCode == videoRequestCode && resultCode == Activity.RESULT_OK && data != null) {
            //selectedVideoUri = data.data
            uploadFileToFirebase(data.data, "videos", "VideoTitle")
        }

    }

    private fun uploadFileToFirebase(uri: Uri, fileType: String, title: String) {
        val filename = UUID.randomUUID().toString()
        val ref = firebaseStorage.getReference("/$fileType/$filename")
        showUploadDialog("Uploading file")

        ref.putFile(uri)
                .addOnSuccessListener {
                    Log.d("TAG", "Successfully uploaded file: ${it.metadata?.path}")

                    ref.downloadUrl.addOnSuccessListener {
                        Log.d("TAG", "File location: $it")
                        saveFileToDatabase(filename, it.toString(), title, "workspace", "tool1", fileType)
                        Handler().post { dialog!!.dismiss() }
                    }

                }
                .addOnFailureListener {
                    Handler().post { dialog!!.dismiss() }
                    Log.d("TAG", "Something went wrong when loading the file")
                }
    }

    private fun saveFileToDatabase(fileId: String, fileUrl: String, title: String, workSpace: String, tool: String, fileType: String) {
        val ref = firebaseDatabase.getReference("/$workSpace/tools/$tool/$fileType/$fileId")

        if (fileType.equals("images")){
            val image = Image(fileId, fileUrl, title)
            ref.setValue(image)
                    .addOnSuccessListener {
                        Log.d("TAG", "Image to tool database")
                    }
                    .addOnFailureListener {
                        Log.d("TAG", "Something went wrong with the database")
                    }
        }
        else if (fileType.equals("pdfs")){
            val pdf = PDF(fileId, fileUrl, title)
            ref.setValue(pdf)
                    .addOnSuccessListener {
                        Log.d("TAG", "PDF to tool database")
                    }
                    .addOnFailureListener {
                        Log.d("TAG", "Something went wrong with the database")
                    }
        }
        else if (fileType.equals("videos")){
            val video = Video(fileId, fileUrl, title)
            ref.setValue(video)
                    .addOnSuccessListener {
                        Log.d("TAG", "Video to tool database")
                    }
                    .addOnFailureListener {
                        Log.d("TAG", "Something went wrong with the database")
                    }
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

    private fun signOut() {
        val builder = AlertDialog.Builder(context!!)
        val dialogView = layoutInflater.inflate(R.layout.dialog_close_app, viewGroup)
        builder.setView(dialogView)
                .setPositiveButton(R.string.yes) { _, _ ->
                    firebaseAuth.signOut()
                    fragmentManager!!.beginTransaction().replace(R.id.fragmentContainer, SigninFragment()).commit()
                }
                .setNegativeButton(R.string.no) { _, _ ->
                }.show()
    }

    private fun getUserInformation(user: String){
        val ref = firebaseDatabase.getReference("/users/$user")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                Log.d("ProfileFragment", "Value: ${p0.value}")
                Log.d("ProfileFragment", "Child: ${p0.children}")
                Log.d("ProfileFragment", "Child path firstname: ${p0.child("firstName").value}")

                if (txtProfile != null){
                    txtProfile.text = "First name: ${p0.child("firstName").value} Last name: ${p0.child("lastName").value}"
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }
}