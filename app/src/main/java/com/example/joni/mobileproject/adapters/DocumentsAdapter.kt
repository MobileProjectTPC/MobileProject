package com.example.joni.mobileproject.adapters

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.joni.mobileproject.R
import com.example.joni.mobileproject.models.PDF
import com.example.joni.mobileproject.models.Portfolio
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class DocumentsAdapter(var activity: Context, private var listPDF: ArrayList<PDF>, private var userCreated: Boolean, var project: Portfolio) : BaseAdapter() {

    override fun getCount(): Int {
        return listPDF.size
    }

    override fun getItem(position: Int): PDF {
        return listPDF[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = View.inflate(activity, R.layout.list_item_documents,null)

        val firebaseData = FirebaseDatabase.getInstance().reference

        val pdfName: TextView = view.findViewById(R.id.document_name)
        val pdfIcon: ImageView = view.findViewById(R.id.document_icon)
        val pdfFilename: TextView = view.findViewById(R.id.document_file_name)
        val pdfDelete: ImageView = view.findViewById(R.id.btnDelete)

        if (!userCreated){
            pdfDelete.visibility = View.INVISIBLE
        }

        pdfName.text = listPDF[position].title

        Picasso.get().load(R.drawable.pdf_icon).into(pdfIcon)

        pdfFilename.text = listPDF[position].PDFId

        pdfDelete.setOnClickListener {
            val builder: AlertDialog.Builder = if (Build.VERSION.SDK_INT >= 26) {
                AlertDialog.Builder(activity, android.R.style.Theme_Material_Dialog_Alert)
            } else {
                AlertDialog.Builder(activity)
            }
            builder.setTitle("Delete entry")
                    .setMessage("Are you sure you want to delete this entry? \nIt cannot be undone!")
                    .setPositiveButton(android.R.string.yes) { _, _ ->
                        // continue with delete
                        Log.d("DocumentAdapter_test", "Project.uid: " + project.uid)
                        Log.d("DocumentAdapter_test", "listPDF[position].PDFId: " + listPDF[position].PDFId)
                        //Log.d("DocumentAdapter_test", "listPDF[position].PDFId: " + listPDF[position].PDFId)
                        firebaseData.child("portfolio").child(project.uid).child("pdfs").child(listPDF[position].PDFId).removeValue()
                        Toast.makeText(activity, "The PDF has been deleted", Toast.LENGTH_LONG).show()
                    }
                    .setNegativeButton(android.R.string.no) { _, _ ->
                        // do nothing
                    }
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
        }

        return view
    }
}
