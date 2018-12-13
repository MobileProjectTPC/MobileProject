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
import android.widget.TextView
import android.widget.Toast
import com.example.joni.mobileproject.PortfolioActivity
import com.example.joni.mobileproject.R
import com.example.joni.mobileproject.models.Image
import com.example.joni.mobileproject.models.Portfolio
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.core.Context
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.add_image.*
import java.io.File
import java.util.*

class AddPictureFragment: Fragment() {

    private val REQUEST_IMAGE_CAPTURE = 1
    private var mCurrentPhotoPath: String? = null
    private val pFileName = "temp_photo"
    private lateinit var imageFile: File
    private lateinit var pictureButton: AppCompatImageView
    private lateinit var addPictureButton: Button
    private lateinit var projectName: TextView

    private val firebaseStorage = FirebaseStorage.getInstance()
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private var dialog: AlertDialog? = null
    private val viewGroup: ViewGroup? = null

    var cont: android.content.Context? = null
    var act: Activity? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.add_image, container, false)


        cont = context!!
        act = activity!!

        val mode: Int = arguments!!.getInt("Mode")
        var myproject: Portfolio? = null
        if (mode == 1){
            myproject = arguments!!.getSerializable("Project") as Portfolio
        }


        val pos = arguments?.getString("position")
        if (pos != null){
            position = pos.toInt()
        }
        Log.d("tää", "$pos")
        Log.d("tää", position.toString())


        pictureButton = rootView.findViewById(R.id.picture)
        pictureButton.setOnClickListener {
            val imgPath = activity!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            imageFile = File.createTempFile(pFileName, ".jpg", imgPath )
            mCurrentPhotoPath = imageFile.absolutePath
            val imageURI: Uri = FileProvider.getUriForFile(context!!,
                    "com.example.joni.mobileproject",
                    imageFile)
            val myIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            if (myIntent.resolveActivity(activity!!.packageManager) != null) {
                myIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI)
                startActivityForResult(myIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
            if (mode == 0) {
            projectName = activity!!.findViewById(R.id.final_project_name)
            }


        addPictureButton = rootView.findViewById(R.id.add_picture)
        addPictureButton.setOnClickListener { _ ->
            if (picture.drawable != null &&
                    text_picture_title.text.isNotEmpty() &&
                    text_picture_description.text.isNotEmpty()) {

                //PUT FILE TO THE DATABASE HERE!!
                var project: String? = null
                if (mode == 0){
                    project = projectName.text.toString()
                }
                else{
                    if (myproject != null) {
                        project = myproject.uid
                    }
                }

                val title = text_picture_title.text.toString()
                val description = text_picture_description.text.toString()

                val filename = UUID.randomUUID().toString()
                val ref = firebaseStorage.getReference("/portfolio/$project/$filename")

                val file = File(mCurrentPhotoPath!!)
                val imageUri = Uri.fromFile(file)
                ref.putFile(imageUri)
                        .addOnSuccessListener { it ->
                            Log.d("TAG", "Successfully uploaded file: ${it.metadata?.path}")

                            ref.downloadUrl.addOnSuccessListener {
                                Log.d("TAG", "File location: $it")
                                saveFileToDatabase(filename, it.toString(), title, description, project!!)
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

        val image = Image(fileId, fileUrl, title)

        ref.setValue(image)
                .addOnSuccessListener {
                    Log.d("TAG", "Image to tool database")
                }
                .addOnFailureListener {
                    Log.d("TAG", "Something went wrong with the database")
                }
    }


    private var position: Int = 100

    private fun showUploadDialog(message: String) {
        val builder = AlertDialog.Builder(context!!)
        val dialogView = layoutInflater.inflate(R.layout.progress_dialog_layout, viewGroup)
        val dialogTxtView = dialogView.findViewById<TextView>(R.id.txtUploadProgress)
        dialogTxtView.text = message
        builder.setView(dialogView)
        builder.setCancelable(false)
        dialog = builder.create()
        dialog!!.show()
        dialog!!.setOnDismissListener {


        }
    }

}