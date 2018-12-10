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
import android.widget.MediaController
import android.widget.TextView
import android.widget.Toast
import com.example.joni.mobileproject.R
import kotlinx.android.synthetic.main.activity_project_create.*
import kotlinx.android.synthetic.main.add_image.*
import java.io.File

class AddPictureFragment: Fragment() {

    val REQUEST_IMAGE_CAPTURE = 1
    var mCurrentPhotoPath: String? = null
    val pFileName = "temp_photo"
    lateinit var imageFile: File
    lateinit var pictureButton: AppCompatImageView
    lateinit var addPictureButton: Button
    lateinit var projectName: TextView

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

                activity!!.supportFragmentManager.beginTransaction().remove(this).commit()
                Toast.makeText(context!!, "Project picture added to the project ${projectName.text}", Toast.LENGTH_SHORT).show()

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

}