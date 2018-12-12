package com.example.joni.mobileproject.fragments

import android.app.Activity
import android.content.Intent
import android.media.ThumbnailUtils
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
import com.example.joni.mobileproject.ProjectCreateActivity
import com.example.joni.mobileproject.R
import com.example.joni.mobileproject.models.Image
import com.example.joni.mobileproject.models.Portfolio
import com.example.joni.mobileproject.models.Video
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_project_create.*
import kotlinx.android.synthetic.main.add_video.*
import java.io.File
import java.util.*

class AddVideoFragment: Fragment() {

    val REQUEST_VIDEO_CAPTURE = 2
    var mCurrentVideoPath: String? = null
    val vFileName = "temp_video"
    lateinit var videoFile: File
    lateinit var videoButton: AppCompatImageView
    lateinit var addVideoButton: Button
    lateinit var projectName: TextView

    private val firebaseStorage = FirebaseStorage.getInstance()
    private val firebaseDatabase = FirebaseDatabase.getInstance()

    private var dialog: AlertDialog? = null
    private val viewGroup: ViewGroup? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.add_video, container, false)

        var mode: Int = arguments!!.getInt("Mode")
        var myproject: Portfolio? = null
        if (mode == 1){
            myproject = arguments!!.getSerializable("Project") as Portfolio
        }

        videoButton = rootView.findViewById(R.id.video_image)
        videoButton.setOnClickListener {
            val vdPath = activity!!.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
            videoFile = File.createTempFile(vFileName, ".mkv", vdPath )
            mCurrentVideoPath = videoFile.absolutePath
            val imageURI: Uri = FileProvider.getUriForFile(context!!,
                    "com.example.joni.mobileproject",
                    videoFile)
            val myIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)

            val iIntent = Intent(Intent.ACTION_VIEW)
            //iIntent.addFlags(FLAG_GRANT_READ_URI_PERMISSION)

            if (myIntent.resolveActivity(activity!!.packageManager) != null) {
                myIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI)
                myIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30)
                startActivityForResult(myIntent, REQUEST_VIDEO_CAPTURE)
            }
        }

        if (mode == 0){
            projectName = activity!!.findViewById(R.id.final_project_name)
        }

        addVideoButton = rootView.findViewById(R.id.add_video)
        addVideoButton.setOnClickListener {
            if (video_image.drawable != null &&
                    text_video_title.text.isNotEmpty() &&
                    text_video_description.text.isNotEmpty()) {

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

                val title = text_video_title.text.toString()
                val description = text_video_description.text.toString()

                val filename = UUID.randomUUID().toString()
                val ref = firebaseStorage.getReference("/videos/$filename")

                val file = File(mCurrentVideoPath!!)
                val videoUri = Uri.fromFile(file)
                ref.putFile(videoUri)
                        .addOnSuccessListener {
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
                //Toast.makeText(context!!, "Video added to the project ${projectName.text.toString()}", Toast.LENGTH_SHORT).show()

            }
            else {
                Toast.makeText(context!!, "null", Toast.LENGTH_SHORT).show()
            }
        }
        return rootView

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, recIntent: Intent?) {


        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == Activity.RESULT_OK) {

            val bitmap = ThumbnailUtils.createVideoThumbnail(mCurrentVideoPath!!, MediaStore.Video.Thumbnails.MINI_KIND)
            video_image.setImageBitmap(bitmap)
            video_image.background = null

        }

    }


    private fun saveFileToDatabase(fileId: String, fileUrl: String, title: String, description: String, project: String) {
        val ref = firebaseDatabase.getReference("/portfolio/$project/videos/$fileId")

        //val filename = UUID.randomUUID().toString()
        //val video = Video(filename, fileUrl, title)
        val video = Video(fileId, fileUrl, title)
        //val arrayList = ArrayList<Image>()
        //arrayList.add(image)

        ref.setValue(video)
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