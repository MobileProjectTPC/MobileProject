package com.example.joni.mobileproject.adapters


import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Parcelable
import android.support.v7.widget.RecyclerView
import android.text.style.URLSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.joni.mobileproject.R
import com.example.joni.mobileproject.ToolsActivity
import com.example.joni.mobileproject.databinding.ListItemBinding
import com.example.joni.mobileproject.fragments.HomeFragment
import com.example.joni.mobileproject.fragments.Image
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import junit.framework.Assert.assertEquals
import java.io.InputStream
import java.io.Serializable
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLStreamHandler
import java.util.ArrayList

class TransitionListAdapter(
        navigation: TransitionNavigation,
        val page: Int,
        myList: Serializable
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val navigationRef = WeakReference(navigation)

    val newlist = myList as ArrayList<Image>


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false))
    }

    override fun getItemCount(): Int {
        return newlist.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as ViewHolder
        val context = holder.itemView.context


        //viewHolder.txtViewTitle.setText(itemsData[position].getTitle());
        //viewHolder.imgViewIcon.setImageResource(itemsData[position].getImageUrl());

        holder.binding.text.transitionName = "${context.getString(R.string.transition_text)}_${page}_$position"
        holder.binding.image.transitionName = "${context.getString(R.string.transition_image)}_${page}_$position"

        holder.binding.text.text = newlist[position].title

        val imageUri = Uri.parse(newlist[position].imageUrl)

        Picasso.get()
                .load(imageUri)
                .placeholder(R.drawable.progress_animation)
                .error(R.drawable.workshop_tutor_logo_text)
                .into(holder.binding.image)




    }

    private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val binding: ListItemBinding = DataBindingUtil.bind(itemView)!!

        init {
            binding.root.setOnClickListener {
                navigationRef.get()?.goToDetail(
                        listOf(
                                binding.image,
                                binding.text
                        ),
                        layoutPosition,
                        page
                )
            }
        }
    }

}
