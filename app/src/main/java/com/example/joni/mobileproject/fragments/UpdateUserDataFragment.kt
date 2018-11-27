package com.example.joni.mobileproject.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.joni.mobileproject.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.update_user_data_fragment_layout.*

class UpdateUserDataFragment: Fragment() {

    private val firebaseDatabaseRef = FirebaseDatabase.getInstance().reference
    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.update_user_data_fragment_layout, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("UpdateUserDataFragment", "UpdateUserDataFragment created")

        btnUpdateData.setOnClickListener {
            reAuthenticate(edtOldPass.text.toString())
        }

        btnCancel.setOnClickListener {
            fragmentManager!!.beginTransaction().replace(R.id.fragmentContainer, ProfileFragment()).commit()
        }
    }

    private fun updateData(user: String, newFirstName: String, newLastName: String){
        if (!TextUtils.isEmpty(newFirstName)){
            firebaseDatabaseRef.child("users")
                    .child(user)
                    .child("firstName")
                    .setValue(newFirstName)
        }

        if (!TextUtils.isEmpty(newLastName)){
            firebaseDatabaseRef.child("users")
                    .child(user)
                    .child("lastName")
                    .setValue(newLastName)
        }
    }

    private fun updateEmail(user: String, newEmail: String, confirmEmail: String){
        if (!TextUtils.isEmpty(newEmail) || !TextUtils.isEmpty(confirmEmail)){
            if (TextUtils.equals(newEmail, confirmEmail)){
                firebaseAuth.currentUser!!.updateEmail(newEmail)
                        .addOnSuccessListener {
                            Log.d("UpdateUserDataFragment", "Email updated")
                            firebaseDatabaseRef.child("users")
                                    .child(user)
                                    .child("email")
                                    .setValue(newEmail)
                            firebaseAuth.currentUser!!.sendEmailVerification()
                        }
                        .addOnFailureListener {
                            Log.d("UpdateUserDataFragment", "Foggening $it")
                        }
            }
            else {
                Toast.makeText(context, "Emails does not match", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updatePassword(newPassword: String, confirmPassword: String){
        if (!TextUtils.isEmpty(newPassword) || !TextUtils.isEmpty(confirmPassword)){
            if (TextUtils.equals(newPassword, confirmPassword)){
                firebaseAuth.currentUser!!.updatePassword(newPassword)
                        .addOnSuccessListener {
                            Log.d("UpdateUserDataFragment", "Password updated!")
                        }
                        .addOnFailureListener {
                            Log.d("UpdateUserDataFragment", "Foggening $it")
                        }
            }
            else{
                Toast.makeText(context, "Passwords does not match", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun reAuthenticate(password: String){
        if (!TextUtils.isEmpty(password)){
            val credentials = EmailAuthProvider.getCredential(firebaseAuth.currentUser!!.email!!, password)
            firebaseAuth.currentUser!!.reauthenticate(credentials)
                    .addOnSuccessListener {
                        updateData(firebaseAuth.uid!!, edtUpdateFirst.text.toString(), edtUpdateLast.text.toString())
                        updateEmail(firebaseAuth.uid!!, edtUpdateEmail.text.toString(), edtConfirmUpdateEmail.text.toString())
                        updatePassword(edtUpdatePass.text.toString(), edtConfirmUpdatePass.text.toString())
                    }
                    .addOnFailureListener {
                        Log.d("UpdateUserDataFragment", "Foggening $it")
                        Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show()
                    }
        }
        else{
            Toast.makeText(context, "Enter password", Toast.LENGTH_SHORT).show()
        }
    }
}