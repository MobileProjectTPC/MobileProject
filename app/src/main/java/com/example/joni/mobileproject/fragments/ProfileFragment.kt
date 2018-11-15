package com.example.joni.mobileproject.fragments

import android.graphics.PorterDuff
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import com.example.joni.mobileproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.profile_fragment_layout.*

class ProfileFragment: Fragment() {

    private val TAG = "FirebaseEmailPassword"
    private lateinit var mAuth: FirebaseAuth
    private val viewGroup: ViewGroup? = null
    private var dialog: AlertDialog? = null

    companion object {
        fun newInstance(): ProfileFragment = ProfileFragment()
        const val REFRESH_TIME: Long = 2000
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.profile_fragment_layout, container, false)

        val btnEmailSignIn = rootView.findViewById<Button>(R.id.btn_email_sign_in)
        btnEmailSignIn.setOnClickListener { signIn(edtEmail.text.toString(), edtPassword.text.toString()) }
        val btnEmailCreateAccount = rootView.findViewById<Button>(R.id.btn_email_create_account)
        btnEmailCreateAccount.setOnClickListener { createAccount(edtEmail.text.toString(), edtPassword.text.toString()) }
        //val btnSignOut = rootView.findViewById<Button>(R.id.btn_sign_out)
        //btnSignOut.setOnClickListener { signOut() }
        val btnVerifyEmail = rootView.findViewById<Button>(R.id.btn_verify_email)
        btnVerifyEmail.setOnClickListener { sendEmailVerification() }
        val btnRefresh = rootView.findViewById<Button>(R.id.btn_refresh)
        btnRefresh.setOnClickListener {
            //mAuth.signOut()
            //mAuth.signInWithEmailAndPassword(edtEmail.text.toString(), edtPassword.text.toString())
            //updateUI(mAuth.currentUser)
            val currentUser = mAuth.currentUser
            if (currentUser != null) {
                progressbar.visibility = ProgressBar.VISIBLE
                object : CountDownTimer(REFRESH_TIME, REFRESH_TIME) {
                    override fun onTick(millisUntilFinished: Long) {
                    }
                    override fun onFinish() {
                        currentUser.reload()
                        if (!currentUser.isEmailVerified) {
                            Toast.makeText(context!!, "Email is not verified", Toast.LENGTH_SHORT).show()
                        }
                        progressbar.visibility = ProgressBar.GONE
                        updateUI(currentUser)
                    }
                }.start()
            }


        }

        mAuth = FirebaseAuth.getInstance()


        val toolbar = activity!!.findViewById<View>(R.id.toolbar) as Toolbar
        if (!toolbar.menu.hasVisibleItems()) {
            toolbar.inflateMenu(R.menu.logout)

            activity!!.findViewById<View>(R.id.log_out_button).visibility = View.INVISIBLE

            val drawable = toolbar.menu.getItem(0).icon
            drawable.mutate()
            drawable.setColorFilter(resources.getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP)

            toolbar.setOnMenuItemClickListener {
                signOut()
                true
            }
        }

        return rootView
    }

    override fun onStart() {
        super.onStart()
        val currentUser = mAuth.currentUser
        updateUI(currentUser)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity!!.findViewById<View>(R.id.log_out_button).visibility = View.INVISIBLE
    }

    private fun createAccount(email: String, password: String) {
        Log.e(TAG, "createAccount: $email")
        if (!validateForm(email, password)) {
            return
        }
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.e(TAG, "createAccount: Success!")

                        // update UI with the signed-in user's information
                        val user = mAuth.currentUser
                        updateUI(user)
                    } else {
                        Log.e(TAG, "createAccount: Fail!", task.exception)
                        Toast.makeText(context!!, "Authentication failed!", Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }
                }
    }

    private fun signIn(email: String, password: String) {
        Log.e(TAG, "signIn: $email")
        if (!validateForm(email, password)) {
            return
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.e(TAG, "signIn: Success!")

                        // update UI with the signed-in user's information
                        val user = mAuth.currentUser
                        updateUI(user)
                    } else {
                        Log.e(TAG, "signIn: Fail!", task.exception)
                        Toast.makeText(context!!, "Authentication failed!", Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }

                }
    }

    private fun signOut() {
        val builder = AlertDialog.Builder(context!!)
        val dialogView = layoutInflater.inflate(R.layout.dialog_close_app, viewGroup)
        //val dialogText: TextView  = dialogView.findViewById(R.id.dialog_text)
        //dialogText.text = "Do you want to log out?"
        builder.setView(dialogView)
                .setPositiveButton("Yes") { _, _ ->
                    mAuth.signOut()
                    updateUI(null)
                }
                .setNegativeButton("No") { _, _ ->
                }.show()
    }

    private fun sendEmailVerification() {
        // Disable Verify Email button
        btn_verify_email.isEnabled = false

        val user = mAuth.currentUser
        user!!.sendEmailVerification()
                .addOnCompleteListener { task ->
                    // Re-enable Verify Email button
                    btn_verify_email.isEnabled = true

                    if (task.isSuccessful) {
                        Toast.makeText(context!!, "Verification email sent to " + user.email!!, Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e(TAG, "sendEmailVerification failed!", task.exception)
                        Toast.makeText(context!!, "Failed to send verification email.", Toast.LENGTH_SHORT).show()
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

        if (password.length < 6) {
            Toast.makeText(context!!, "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun updateUI(user: FirebaseUser?) {

        if (user != null && !user.isEmailVerified) {
            //tvStatus.text = "User Email: " + user.email + "(verified: " + user.isEmailVerified + ")"
            //tvDetail.text = "Firebase User ID: " + user.uid

            email_password_buttons.visibility = View.GONE
            email_password_fields.visibility = View.GONE
            layout_signed_in_buttons.visibility = View.VISIBLE
            authentication.visibility = View.VISIBLE
            profile.visibility = View.GONE

            btn_verify_email.isEnabled = !user.isEmailVerified
            activity!!.findViewById<View>(R.id.log_out_button).visibility = View.VISIBLE
        }
        else if (user != null && user.isEmailVerified) {
            //tvStatus.text = "User Email: " + user.email + "(verified: " + user.isEmailVerified + ")"
            //tvDetail.text = "Firebase User ID: " + user.uid
            email_password_buttons.visibility = View.GONE
            email_password_fields.visibility = View.GONE
            layout_signed_in_buttons.visibility = View.GONE
            activity!!.findViewById<View>(R.id.log_out_button).visibility = View.VISIBLE
            authentication.visibility = View.GONE
            profile.visibility = View.VISIBLE
        }

        else {
            //tvStatus.text = "Signed Out"
            //tvDetail.text = null
            activity!!.findViewById<View>(R.id.log_out_button).visibility = View.INVISIBLE
            email_password_buttons.visibility = View.VISIBLE
            email_password_fields.visibility = View.VISIBLE
            layout_signed_in_buttons.visibility = View.GONE
            authentication.visibility = View.VISIBLE
            profile.visibility = View.GONE
        }
    }

    private fun refreshFragment(fragment: Fragment, fragmentName: String) {
        val transaction = fragmentManager!!.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment, fragmentName)
        transaction.addToBackStack(null)
        transaction.commit()
    }

}