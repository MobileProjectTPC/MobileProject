package com.example.joni.mobileproject.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.joni.mobileproject.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.email_not_verified_fragment_layout.*

class EmailNotVerifiedFragment: Fragment() {

    private val firebaseAuth = FirebaseAuth.getInstance()

    private val viewGroup: ViewGroup? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.email_not_verified_fragment_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("EmailNotVerifiedFragment", "EmailNotVerifiedFragment created")

        btnSendVerificationEmail.setOnClickListener {
            sendVerificationEmail()
        }
        btnSignout.setOnClickListener {
            signOut()
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("EmailNotVerifiedFragment", "onStart")
        firebaseAuth.currentUser!!.reload().addOnSuccessListener {
            if (firebaseAuth.currentUser!!.isEmailVerified) {
                Log.d("EmailNotVerifiedFragment", "onStartIf")
                fragmentManager!!.beginTransaction().replace(R.id.fragmentContainer, ProfileFragment()).commit()
            }
        }
    }

    private fun sendVerificationEmail() {
        firebaseAuth!!.currentUser!!.sendEmailVerification()
                .addOnCompleteListener {
                    Toast.makeText(context!!, "Verification email sent!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context!!, "Something went wrong, please try again!", Toast.LENGTH_SHORT).show()
                }
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