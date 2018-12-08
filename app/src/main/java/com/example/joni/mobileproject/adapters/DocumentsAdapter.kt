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
import android.app.Fragment
import android.support.design.widget.CoordinatorLayout.Behavior.setTag
import android.databinding.DataBindingUtil
import android.databinding.adapters.TextViewBindingAdapter.setText
import android.net.Uri
import android.support.v7.widget.RecyclerView.ViewHolder
import android.support.design.widget.CoordinatorLayout.Behavior.setTag







class DocumentsAdapter(var activity: Context, var listPDF: ArrayList<PDF>) : BaseAdapter() {

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

        val PDFname = view.findViewById<TextView>(R.id.document_name) as TextView
        val PDFicon = view.findViewById<ImageView>(R.id.document_icon) as ImageView


        PDFname.text = listPDF[position].title

        Picasso.get().load(R.drawable.pdf_icon).into(PDFicon)

        return view
    }
}
