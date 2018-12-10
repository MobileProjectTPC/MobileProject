package com.example.joni.mobileproject.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.support.v7.widget.AppCompatImageView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import com.example.joni.mobileproject.ProjectCreateActivity
import com.example.joni.mobileproject.R
import kotlinx.android.synthetic.main.activity_project_create.*
import kotlinx.android.synthetic.main.add_image.*
import kotlinx.android.synthetic.main.add_project.*
import java.io.File
import android.R.attr.fragment
import android.R.attr.port
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.widget.Toast
import com.example.joni.mobileproject.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.collections.ArrayList


class AddProjectFragment: Fragment() {

    val REQUEST_IMAGE_CAPTURE = 1
    var mCurrentPhotoPath: String? = null
    val pFileName = "temp_photo"
    lateinit var imageFile: File
    lateinit var projectPictureButton: AppCompatImageView
    lateinit var addProjectButton: Button
    lateinit var projectName: TextView
    lateinit var buttonAddFile: Button

    private val firebaseStorage = FirebaseStorage.getInstance()
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private var dialog: AlertDialog? = null
    private val viewGroup: ViewGroup? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.add_project, container, false)

        projectPictureButton = rootView.findViewById(R.id.project_picture)
        projectPictureButton.setOnClickListener {
            val imgPath = activity!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            imageFile = File.createTempFile(pFileName, ".jpg", imgPath )
            mCurrentPhotoPath = imageFile.absolutePath
            val imageURI: Uri = FileProvider.getUriForFile(context!!,
                    "com.example.joni.mobileproject",
                    imageFile)
            val myIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            val iIntent = Intent(Intent.ACTION_VIEW)
            //iIntent.addFlags(FLAG_GRANT_READ_URI_PERMISSION)

            if (myIntent.resolveActivity(activity!!.packageManager) != null) {
                myIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI)
                startActivityForResult(myIntent, REQUEST_IMAGE_CAPTURE)
            }
        }

        addProjectButton = rootView.findViewById(R.id.btnAddProject)
        addProjectButton.setOnClickListener {


            if (text_project_name.text.isNotEmpty() && project_picture.drawable != null) {


                val project = text_project_name.text.toString()

                //val filename = UUID.randomUUID().toString()
                val ref = firebaseStorage.getReference("/portfolio/$project")

                val file = File(mCurrentPhotoPath!!)
                val imageUri = Uri.fromFile(file)
                ref.putFile(imageUri)
                        .addOnSuccessListener {
                            Log.d("TAG", "Successfully uploaded file: ${it.metadata?.path}")

                            ref.downloadUrl.addOnSuccessListener {
                                Log.d("TAG", "File location: $it")
                                saveFileToDatabase(project, it.toString(), project)
                                //Handler().post { dialog!!.dismiss() }
                            }

                        }
                        .addOnFailureListener {
                            //Handler().post { dialog!!.dismiss() }
                            Log.d("TAG", "Something went wrong when loading the file")
                        }


                projectName.visibility = TextView.VISIBLE
                projectName.text = text_project_name.text
                buttonAddFile.isEnabled = true
                activity!!.supportFragmentManager.beginTransaction().remove(this).commit()
                Toast.makeText(context!!, "Project ${text_project_name.text.toString()} created", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(context!!, "Give Project name and picture first", Toast.LENGTH_SHORT).show()
            }
        }

        projectName = activity!!.findViewById(R.id.final_project_name)
        buttonAddFile = activity!!.findViewById(R.id.btnAddFile)


        return rootView
    }


    private fun saveFileToDatabase(fileId: String, fileUrl: String, title: String) {
        val ref = firebaseDatabase.getReference("/portfolio/$fileId")

        val filename = UUID.randomUUID().toString()
        val image = Image(filename, fileUrl, title)
        val arrayList = ArrayList<Image>()
        arrayList.add(image)

        val user = firebaseAuth.currentUser!!.uid

        val portfolio = Portfolio(null, arrayList, null, null, null, null, null, fileId, user, null, null)
        ref.setValue(portfolio)
                .addOnSuccessListener {
                    Log.d("TAG", "Image to tool database")
                }
                .addOnFailureListener {
                    Log.d("TAG", "Something went wrong with the database")
                }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, recIntent: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            project_picture.setImageURI(Uri.parse(mCurrentPhotoPath!!))
            project_picture.requestFocus()
            project_picture.background = null
        }

    }

}