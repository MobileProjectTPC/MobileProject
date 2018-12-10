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
import kotlinx.android.synthetic.main.fragment_detail.*
import kotlinx.android.synthetic.main.profile_fragment_layout.*
import java.io.File
import java.util.*

class AddPictureFragment: Fragment() {

    val REQUEST_IMAGE_CAPTURE = 1
    var mCurrentPhotoPath: String? = null
    val pFileName = "temp_photo"
    lateinit var imageFile: File
    lateinit var pictureButton: AppCompatImageView
    lateinit var addPictureButton: Button
    lateinit var projectName: TextView

    private val firebaseStorage = FirebaseStorage.getInstance()
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private var dialog: AlertDialog? = null
    private val viewGroup: ViewGroup? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.add_image, container, false)

        pictureButton = rootView.findViewById(R.id.picture)
        pictureButton.setOnClickListener {
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

        projectName = activity!!.findViewById(R.id.final_project_name)

        addPictureButton = rootView.findViewById(R.id.add_picture)
        addPictureButton.setOnClickListener {
            if (picture.drawable != null &&
                    text_picture_title.text.isNotEmpty() &&
                    text_picture_description.text.isNotEmpty()) {

                //PUT FILE TO THE DATABASE HERE!!

                val project = projectName.text.toString()

                val title = text_picture_title.text.toString()
                val description = text_picture_description.text.toString()

                val filename = UUID.randomUUID().toString()
                val ref = firebaseStorage.getReference("/portfolio/$project/$filename")

                val file = File(mCurrentPhotoPath!!)
                val imageUri = Uri.fromFile(file)
                ref.putFile(imageUri)
                        .addOnSuccessListener {
                            Log.d("TAG", "Successfully uploaded file: ${it.metadata?.path}")

                            ref.downloadUrl.addOnSuccessListener {
                                Log.d("TAG", "File location: $it")
                                saveFileToDatabase(filename, it.toString(), title, description, project)
                                Handler().post { dialog!!.dismiss() }
                            }

                        }
                        .addOnFailureListener {
                            Handler().post { dialog!!.dismiss() }
                            Log.d("TAG", "Something went wrong when loading the file")
                        }




                activity!!.supportFragmentManager.beginTransaction().remove(this).commit()
                showUploadDialog("Uploading file")
                //Toast.makeText(context!!, "Picture added to the project ${projectName.text}", Toast.LENGTH_SHORT).show()

            }
            else {
                Toast.makeText(context!!, "null", Toast.LENGTH_SHORT).show()
            }
        }
        return rootView

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, recIntent: Intent?) {

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {

            picture.setImageURI(Uri.parse(mCurrentPhotoPath!!))
            picture.requestFocus()
            picture.background = null
        }

    }


    private fun saveFileToDatabase(fileId: String, fileUrl: String, title: String, description: String, project: String) {
        val ref = firebaseDatabase.getReference("/portfolio/$project/images/$fileId")

        val filename = UUID.randomUUID().toString()
        val image = Image(filename, fileUrl, title)
        //val arrayList = ArrayList<Image>()
        //arrayList.add(image)

        ref.setValue(image)
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