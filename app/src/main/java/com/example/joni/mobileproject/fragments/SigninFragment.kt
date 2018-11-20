package com.example.joni.mobileproject.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.example.joni.mobileproject.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.signin_fragment_layout.*

class SigninFragment: Fragment() {

    private val firebaseAuth = FirebaseAuth.getInstance()

    private var dialog: AlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.signin_fragment_layout, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("SigninFragment", "SigninFragment created")

        btnSignin.setOnClickListener {
            signIn(edtEmail.text.toString(), edtPassword.text.toString())
        }

        txtCreate.setOnClickListener {
            fragmentManager!!.beginTransaction().replace(R.id.fragmentContainer, RegisterFragment()).commit()
        }
    }

    private fun signIn(email: String, password: String) {
        Log.e("SigninFragment", "signIn: $email")
        if (!validateForm(email, password)) {
            return
        }
        showLoadingDialog("Signing in")
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.e("SigninFragment", "signIn: Success!")
                        Handler().post { dialog?.dismiss() }
                        fragmentManager!!.beginTransaction().replace(R.id.fragmentContainer, ProfileFragment()).commit()
                    } else {
                        Log.e("SigninFragment", "signIn: Fail!", task.exception)
                        Handler().post { dialog?.dismiss() }
                        Toast.makeText(context!!, "Authentication failed!", Toast.LENGTH_SHORT).show()
                    }

                }
    }

    private fun validateForm(email: String, password: String): Boolean {

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(context!!, "Enter email address!", Toast.LENGTH_SHORT).show()
            return false
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(context!!, "Enter password!", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    // use to dissmis dialog
    // Handler().post { dialog?.dismiss() }
    private fun showLoadingDialog(message: String) {
        val builder = AlertDialog.Builder(context)
        val dialogView = layoutInflater.inflate(R.layout.progress_dialog_layout, null)
        val dialogTxtView = dialogView.findViewById<TextView>(R.id.txtUploadProgress)
        dialogTxtView.text = message
        builder.setView(dialogView)
        builder.setCancelable(false)
        dialog = builder.create()
        dialog!!.show()
    }

}