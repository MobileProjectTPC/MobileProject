package com.example.joni.mobileproject

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_project_create.*
import com.example.joni.mobileproject.fragments.*


class ProjectCreateActivity : AppCompatActivity() {

    private val addProjectFragment = AddProjectFragment()
    private val addPictureFragment = AddPictureFragment()
    private val addVideoFragment = AddVideoFragment()
    //private val addPdfFragment = AddPdfFragment()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_create)

        supportFragmentManager.beginTransaction()
                .replace(R.id.container_add_project, addProjectFragment).commit()

        btnAddFile.isEnabled = false



        btnAddFile.setOnClickListener { view ->
            chooseFile(view)
        }

    }


    private fun chooseFile(view: View) {
        val popup: PopupMenu?
        popup = PopupMenu(this, view)
        popup.inflate(R.menu.add_file)

        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->

            when (item!!.itemId) {
                R.id.choose_picture -> {
                    setupFragment(addPictureFragment)
                }
                R.id.choose_video -> {
                    setupFragment(addVideoFragment)
                }
                R.id.choose_pdf -> {
                    //setupFragment(addPdfFragment)
                }
            }
            true
        })

        popup.show()
    }

    private fun setupFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.container_add_file, fragment).commit()
    }
}
