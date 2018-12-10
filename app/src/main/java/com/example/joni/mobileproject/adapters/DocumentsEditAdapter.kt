package com.example.joni.mobileproject.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.joni.mobileproject.R
import com.example.joni.mobileproject.models.PDF
import com.squareup.picasso.Picasso
import android.app.Activity
import android.app.AlertDialog
import android.app.Fragment
import android.support.design.widget.CoordinatorLayout.Behavior.setTag
import android.databinding.DataBindingUtil
import android.databinding.adapters.TextViewBindingAdapter.setText
import android.net.Uri
import android.os.Build
import android.support.v7.widget.RecyclerView.ViewHolder
import android.support.design.widget.CoordinatorLayout.Behavior.setTag

class DocumentsEditAdapter(var activity: Context, var listPDF: ArrayList<PDF>) : BaseAdapter() {

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
        val view: View = View.inflate(activity, R.layout.list_item_documents_edit,null)

        val PDFname = view.findViewById<TextView>(R.id.document_name) as TextView
        val PDFicon = view.findViewById<ImageView>(R.id.document_icon) as ImageView
        val PDFfilename = view.findViewById<TextView>(R.id.document_file_name) as TextView
        val PDFdelete = view.findViewById(R.id.btnDelete) as ImageView


        PDFname.text = listPDF[position].title

        Picasso.get().load(R.drawable.pdf_icon).into(PDFicon)

        PDFfilename.text = listPDF[position].PDFId

        PDFdelete.setOnClickListener {
            val builder: AlertDialog.Builder
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = AlertDialog.Builder(activity, android.R.style.Theme_Material_Dialog_Alert)
            } else {
                builder = AlertDialog.Builder(activity)
            }
            builder.setTitle("Delete entry")
                    .setMessage("Are you sure you want to delete this entry? \nIt cannot be undone!")
                    .setPositiveButton(android.R.string.yes) { dialog, which ->
                        // continue with delete
                    }
                    .setNegativeButton(android.R.string.no) { dialog, which ->
                        // do nothing
                    }
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
        }
        return view
    }
}
