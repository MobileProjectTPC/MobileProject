package com.example.joni.mobileproject.adapters

import android.databinding.DataBindingUtil
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.joni.mobileproject.R
import com.example.joni.mobileproject.databinding.ListItemBinding
import com.example.joni.mobileproject.models.Tool
import com.squareup.picasso.Picasso
import java.io.Serializable
import java.lang.ref.WeakReference
import java.util.ArrayList

class TransitionListAdapter(
        navigation: TransitionNavigation,
        val page: Int,
        myList: Serializable
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val navigationRef = WeakReference(navigation)
    private val newList = myList as ArrayList<Tool>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false))
    }

    override fun getItemCount(): Int {
        return newList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as ViewHolder
        val context = holder.itemView.context

        holder.binding.text.transitionName = "${context.getString(R.string.transition_text)}_${page}_$position"
        holder.binding.image.transitionName = "${context.getString(R.string.transition_image)}_${page}_$position"

        holder.binding.text.text = newList[position].name

        val imageUri = Uri.parse(newList[position].image)
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
                        listOf(binding.image, binding.text),
                        layoutPosition,
                        page
                )
            }
        }
    }
}