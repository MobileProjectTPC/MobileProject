package com.example.joni.mobileproject.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.joni.mobileproject.R
import com.example.joni.mobileproject.models.PDF
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.add_pdf.*
import java.util.*

class AddPdfFragment: Fragment() {

    private val pdfRequest = 1
    private lateinit var projectName: TextView
    private lateinit var pdfButton: Button
    private val fireBaseStorage = FirebaseStorage.getInstance()
    private val fireBaseDatabase = FirebaseDatabase.getInstance()
    private var dialog: AlertDialog? = null
    private val viewGroup: ViewGroup? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.add_pdf, container, false)

        projectName = activity!!.findViewById(R.id.final_project_name)

        pdfButton = rootView.findViewById(R.id.add_pdf)
        pdfButton.setOnClickListener {
            if (text_pdf_title.text.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "application/pdf"
                startActivityForResult(intent, pdfRequest)
            }
            else {
                Toast.makeText(context!!, "Give a title first", Toast.LENGTH_SHORT).show()
            }
        }
        return rootView
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pdfRequest && resultCode == Activity.RESULT_OK && data != null) {
            val pdf = text_pdf_title.text.toString()
            uploadFileToFireBase(data.data, pdf)
        }

    }

    private fun uploadFileToFireBase(uri: Uri, title: String) {
        val project = projectName.text.toString()
        val filename = UUID.randomUUID().toString()
        val ref = fireBaseStorage.getReference("/portfolio/$project/$filename")

        ref.putFile(uri)
                .addOnSuccessListener { it ->
                    Log.d("TAG", "Successfully uploaded file: ${it.metadata?.path}")

                    ref.downloadUrl.addOnSuccessListener {
                        Log.d("TAG", "File location: $it")
                        saveFileToDatabase(filename, it.toString(), title, project)
                        Handler().post { dialog!!.dismiss() }
                    }

                }
                .addOnFailureListener {
                    Handler().post { dialog!!.dismiss() }
                    Log.d("TAG", "Something went wrong when loading the file")
                }
        activity!!.supportFragmentManager.beginTransaction().remove(this).commit()
        showUploadDialog("Uploading file")
        //Toast.makeText(context!!, "Pdf added to the project ${projectName.text.toString()}", Toast.LENGTH_SHORT).show()
    }


    private fun saveFileToDatabase(fileId: String, fileUrl: String, title: String, project: String) {
        val ref = fireBaseDatabase.getReference("/portfolio/$project/pdfs/$fileId")

        val pdf = PDF(fileId, fileUrl, title)

        ref.setValue(pdf)
                .addOnSuccessListener {
                    Log.d("TAG", "Image to tool database")
                }
                .addOnFailureListener {
                    Log.d("TAG", "Something went wrong with the database")
                }
    }

    private fun showUploadDialog(message: String) {
        val builder = AlertDialog.Builder(context!!)
        val dialogView = layoutInflater.inflate(R.layout.progress_dialog_layout, viewGroup)
        val dialogTxtView = dialogView.findViewById<TextView>(R.id.txtUploadProgress)
        dialogTxtView.text = message
        builder.setView(dialogView)
        builder.setCancelable(false)
        dialog = builder.create()
        dialog!!.show()
    }

}