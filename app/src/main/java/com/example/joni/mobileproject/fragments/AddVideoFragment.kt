package com.example.joni.mobileproject.fragments

import android.app.Activity
import android.content.Intent
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.support.v7.widget.AppCompatImageView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.joni.mobileproject.R
import kotlinx.android.synthetic.main.activity_project_create.*
import kotlinx.android.synthetic.main.add_video.*
import java.io.File

class AddVideoFragment: Fragment() {

    val REQUEST_VIDEO_CAPTURE = 2
    var mCurrentVideoPath: String? = null
    val vFileName = "temp_video"
    lateinit var videoFile: File
    lateinit var videoButton: AppCompatImageView
    lateinit var addVideoButton: Button
    lateinit var projectName: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.add_video, container, false)

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

        projectName = activity!!.findViewById(R.id.final_project_name)

        addVideoButton = rootView.findViewById(R.id.add_video)
        addVideoButton.setOnClickListener {
            if (video_image.drawable != null &&
                    text_video_title.text.isNotEmpty() &&
                    text_video_description.text.isNotEmpty()) {

                //PUT FILE TO THE DATABASE HERE!!

                activity!!.supportFragmentManager.beginTransaction().remove(this).commit()
                Toast.makeText(context!!, "Project picture added to the project ${projectName.text.toString()}", Toast.LENGTH_SHORT).show()

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

}