package com.example.joni.mobileproject.fragments

import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.example.joni.mobileproject.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.register_fragment_layout.*
import kotlinx.android.synthetic.main.update_user_data_fragment_layout.*

class UpdateUserDataFragment: Fragment() {

    private val firebaseDatabaseRef = FirebaseDatabase.getInstance().reference
    private val firebaseAuth = FirebaseAuth.getInstance()

    private var userInfoArray: ArrayList<String>? = null
    private var dialog: AlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        userInfoArray = this.arguments!!.getStringArrayList("userInfo")
        Log.d("UpdateSADFF", "list: $userInfoArray")


        return inflater.inflate(R.layout.update_user_data_fragment_layout, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("UpdateUserDataFragment", "UpdateUserDataFragment created")

        if (userInfoArray != null){
            Log.d("Update", "list: ${userInfoArray!![0]}")
            edtUpdateFirst.text = Editable.Factory.getInstance().newEditable(userInfoArray!![0])
            edtUpdateLast.text = Editable.Factory.getInstance().newEditable(userInfoArray!![1])
            edtUpdateEmail.hint = Editable.Factory.getInstance().newEditable("New email (old: ${userInfoArray!![2]})")
        }

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
            Toast.makeText(context, "User data updated", Toast.LENGTH_SHORT).show()
        }

        if (!TextUtils.isEmpty(newLastName)){
            firebaseDatabaseRef.child("users")
                    .child(user)
                    .child("lastName")
                    .setValue(newLastName)
            Toast.makeText(context, "User data updated", Toast.LENGTH_SHORT).show()
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
                            Toast.makeText(context, "User data updated", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Log.d("UpdateUserDataFragment", "Foggening $it")
                            Toast.makeText(context, "Email not updated!", Toast.LENGTH_SHORT).show()
                        }
            }
            else {
                Toast.makeText(context, "New emails does not match", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updatePassword(newPassword: String, confirmPassword: String){
        if (!TextUtils.isEmpty(newPassword) || !TextUtils.isEmpty(confirmPassword)){
            if (TextUtils.equals(newPassword, confirmPassword)){
                firebaseAuth.currentUser!!.updatePassword(newPassword)
                        .addOnSuccessListener {
                            Log.d("UpdateUserDataFragment", "Password updated!")
                            Toast.makeText(context, "User data updated", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Log.d("UpdateUserDataFragment", "Foggening $it")
                            Toast.makeText(context, "Password not updated!", Toast.LENGTH_SHORT).show()
                        }
            }
            else{
                Toast.makeText(context, "New passwords does not match", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun reAuthenticate(password: String){
        if (!TextUtils.isEmpty(password)){
            showUploadDialog("Updating user information")
            val credentials = EmailAuthProvider.getCredential(firebaseAuth.currentUser!!.email!!, password)
            firebaseAuth.currentUser!!.reauthenticate(credentials)
                    .addOnSuccessListener {
                        updateData(firebaseAuth.uid!!, edtUpdateFirst.text.toString(), edtUpdateLast.text.toString())
                        updateEmail(firebaseAuth.uid!!, edtUpdateEmail.text.toString(), edtConfirmUpdateEmail.text.toString())
                        updatePassword(edtUpdatePass.text.toString(), edtConfirmUpdatePass.text.toString())
                        Handler().post { dialog!!.dismiss() }
                        fragmentManager!!.beginTransaction().replace(R.id.fragmentContainer, ProfileFragment()).commit()
                        //Toast.makeText(context, "User info updated", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Log.d("UpdateUserDataFragment", "Foggening $it")
                        Handler().post { dialog!!.dismiss() }
                        Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show()
                    }
        }
        else{
            Toast.makeText(context, "Enter password", Toast.LENGTH_SHORT).show()
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