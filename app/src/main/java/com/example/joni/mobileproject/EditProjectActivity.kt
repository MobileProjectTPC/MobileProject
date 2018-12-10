package com.example.joni.mobileproject

import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Vibrator
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.transition.ChangeBounds
import android.transition.ChangeClipBounds
import android.transition.ChangeTransform
import android.transition.TransitionSet
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.example.joni.mobileproject.adapters.TransitionNavigation
import com.example.joni.mobileproject.fragments.*
import com.example.joni.mobileproject.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class EditProjectActivity : AppCompatActivity() {

    private val addPictureFragment = AddPictureFragment()
    private val addVideoFragment = AddVideoFragment()
    //private val addPdfFragment = AddPdfFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_project)


        var project: Portfolio = intent.getSerializableExtra("Project") as Portfolio

        val view = layoutInflater.inflate(R.layout.activity_edit_project, null)

        val image: ImageView = view.findViewById(R.id.image)
        Picasso.get().load(project.images[0].imageUrl).into(image)

        val editMainImage: ImageView = view.findViewById(R.id.editMainImage)
        editMainImage.setOnClickListener {

            supportFragmentManager.beginTransaction()
                    .replace(R.id.placeholder, addPictureFragment).commit()

        }

        val title: EditText = view.findViewById(R.id.title)
        title.setText(project.name)

        val summaryText: EditText = view.findViewById(R.id.summaryText)
        summaryText.setText(project.summary)

    }
}