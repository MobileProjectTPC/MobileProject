package com.example.joni.mobileproject.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.joni.mobileproject.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.profile_fragment_layout.*

class ProfileFragment: Fragment() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val viewGroup: ViewGroup? = null
    private lateinit var toolbar: Toolbar
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        if (!firebaseAuth.currentUser!!.isEmailVerified) {
            fragmentManager!!.beginTransaction().replace(R.id.fragmentContainer, EmailNotVerifiedFragment()).commit()
        }

        val rootView = inflater.inflate(R.layout.profile_fragment_layout, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        btnSignout.setOnClickListener {
            signOut()
        }

        super.onViewCreated(view, savedInstanceState)
    }

    private fun signOut() {
        val builder = AlertDialog.Builder(context!!)
        val dialogView = layoutInflater.inflate(R.layout.dialog_close_app, viewGroup)
        builder.setView(dialogView)
                .setPositiveButton("Yes") { _, _ ->
                    firebaseAuth.signOut()
                    fragmentManager!!.beginTransaction().replace(R.id.fragmentContainer, SigninFragment()).commit()
                }
                .setNegativeButton("No") { _, _ ->
                }.show()
    }
}
