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
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.register_fragment_layout.*

class RegisterFragment: Fragment() {

    private val firebaseAuth = FirebaseAuth.getInstance()

    private var dialog: AlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        if (firebaseAuth.currentUser != null && firebaseAuth.currentUser!!.isEmailVerified){
            fragmentManager!!.beginTransaction().replace(R.id.fragmentContainer, ProfileFragment()).commit()
        }
        else if (firebaseAuth.currentUser != null && !firebaseAuth.currentUser!!.isEmailVerified){
            fragmentManager!!.beginTransaction().replace(R.id.fragmentContainer, EmailNotVerifiedFragment()).commit()
        }

        return inflater.inflate(R.layout.register_fragment_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("RegisterFragment", "RegisterFragment created")

        btnRegister.setOnClickListener {
            createAccount(edtEmail.text.toString(), edtPassword.text.toString(), edtConfirmPassword.text.toString(), edtFirstName.text.toString(), edtLastName.text.toString())
        }

        txtAlready.setOnClickListener {
            fragmentManager!!.beginTransaction().replace(R.id.fragmentContainer, SigninFragment()).commit()
        }
    }

    private fun createAccount(email: String, password: String, confirmPassword: String, firstName: String, lastName: String) {
        Log.e("RegisterFragment", "createAccount: $email")
        if (!validateForm(email, password, confirmPassword, firstName, lastName)) {
            return
        }
        showLoadingDialog("Registering user")
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.e("RegisterFragment", "createAccount: Success!")
                        saveUserToFirebaseDatabase(email, firstName, lastName)
                    } else {
                        Log.e("RegisterFragment", "createAccount: Fail!", task.exception)
                        Toast.makeText(context!!, "Authentication failed!", Toast.LENGTH_SHORT).show()
                        Handler().post { dialog?.dismiss() }
                    }
                }
    }

    private fun saveUserToFirebaseDatabase(email: String, firstName: String, lastName: String){
        val uid = firebaseAuth.uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, email, firstName, lastName)

        ref.setValue(user)
                .addOnSuccessListener {
                    Log.d("RegisterFragment", "Saved the user to Firebase database")
                    Handler().post { dialog?.dismiss() }
                    firebaseAuth.currentUser!!.sendEmailVerification()
                    fragmentManager!!.beginTransaction().replace(R.id.fragmentContainer, EmailNotVerifiedFragment()).commit()
                }
                .addOnFailureListener{
                    Log.d("RegisterFragment", "Saving user to database failed: ${it.message}")
                    Handler().post { dialog?.dismiss() }
                }
    }

    private fun validateForm(email: String, password: String, confirmPassword: String, firstName: String, lastName: String): Boolean {

        if (TextUtils.isEmpty(firstName)) {
            Toast.makeText(context!!, "Enter first name address!", Toast.LENGTH_SHORT).show()
            return false
        }

        if (TextUtils.isEmpty(lastName)) {
            Toast.makeText(context!!, "Enter last name address!", Toast.LENGTH_SHORT).show()
            return false
        }

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(context!!, "Enter email address!", Toast.LENGTH_SHORT).show()
            return false
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(context!!, "Enter password!", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 6) {
            Toast.makeText(context!!, "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password != confirmPassword) {
            Toast.makeText(context!!, "Password fields does not match!", Toast.LENGTH_SHORT).show()
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

class User(val uid: String, val email: String, val firstName: String, val lastName: String)