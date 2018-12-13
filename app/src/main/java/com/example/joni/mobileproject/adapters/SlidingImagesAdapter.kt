package com.example.joni.mobileproject.adapters

import android.content.Context
import android.os.Parcelable
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.joni.mobileproject.R
import com.squareup.picasso.Picasso

class SlidingImagesAdapter(context: Context, private val urls: Array<String>): PagerAdapter() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }

    override fun getCount(): Int {
        return urls.size
    }

    override fun instantiateItem(view: ViewGroup, position: Int): Any {
        val imageLayout = inflater.inflate(R.layout.slidingimages_layout, view, false)


        val imageView = imageLayout
                .findViewById(R.id.image) as ImageView


        Picasso.get()
                .load(urls[position])
                .placeholder(R.drawable.progress_animation)
                .error(R.drawable.workshop_tutor_logo_text)
                .into(imageView)

        view.addView(imageLayout, 0)

        return imageLayout
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {}

    override fun saveState(): Parcelable? {
        return null
    }
}