package com.example.joni.mobileproject.adapters

import android.content.Context
import android.os.Parcelable
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.joni.mobileproject.R
import com.example.joni.mobileproject.models.ImageModel
import java.util.ArrayList


class SlidingImageAdapter(context: Context, private val imageModelArrayList: ArrayList<ImageModel>) : PagerAdapter() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }

    override fun getCount(): Int {
        return imageModelArrayList.size
    }

    override fun instantiateItem(view: ViewGroup, position: Int): Any {
        val imageLayout = inflater.inflate(R.layout.slidingimages_layout, view, false)!!

        val imageView = imageLayout
            .findViewById(R.id.image) as ImageView

        imageView.setImageResource(imageModelArrayList[position].getImageDrawables())



        imageLayout.setOnClickListener {

            /*
            val intent = Intent(inflater.context, ToolDetailActivity::class.java)
            // create the transition animation - the images in the layouts
            // of both activities are defined with android:transitionName="robot"

            //intent.putExtra("app", apps.get(getAdapterPosition()))

            val options = ActivityOptions
                    .makeSceneTransitionAnimation(inflater.context as Activity,
                            imageView,
                            "shared_char_mainpage")
            // start the new activity
            startActivity(inflater.context, intent, options.toBundle())
            */

            /*
            val activity = inflater.context as MainActivity
            activity.supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, ToolDetailFragment())
                    .addSharedElement(imageView, imageView.transitionName)
                    .commitAllowingStateLoss() // or commit()
            */






        }

        view.addView(imageLayout, 0)

        return imageLayout
    }


/*
    fun goToDetails(url: String, imageView: View) {
        val activity = inflater.context as MainActivity
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity, imageView, imageView.transitionName).toBundle()
        startActivity(inflater.context, Intent(inflater.context, ToolDetailActivity::class.java)
    .putExtra(IMAGE_URL_KEY, url), options)
    }

    */







    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {}

    override fun saveState(): Parcelable? {
        return null
    }




}