package com.example.joni.mobileproject.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.media.ThumbnailUtils
import android.os.Handler
import android.os.Parcelable
import android.provider.MediaStore
import android.support.v4.view.PagerAdapter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.joni.mobileproject.PdfActivity
import com.example.joni.mobileproject.R
import com.example.joni.mobileproject.VideoActivity
import com.example.joni.mobileproject.fragments.DetailPortfolioFragment
import com.example.joni.mobileproject.fragments.HomeFragment
import com.example.joni.mobileproject.models.ImageModel
import com.example.joni.mobileproject.models.ImageVideo
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.add_video.*
import java.io.File
import java.util.ArrayList
import com.example.joni.mobileproject.R.id.imgView
import com.bumptech.glide.Glide
//import sun.awt.windows.ThemeReader.getPosition
import android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC
import android.graphics.Bitmap
import android.os.Build
import android.media.MediaMetadataRetriever








class SlidingImageVideoAdapter(context: Context, private val imageVideoArrayList: ArrayList<ImageVideo>, var fragment: DetailPortfolioFragment) : PagerAdapter() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }

    override fun getCount(): Int {
        return imageVideoArrayList.size
    }

    override fun instantiateItem(view: ViewGroup, position: Int): Any {
        val imageLayout = inflater.inflate(R.layout.slidingimagesvideos_layout, view, false)!!

        val image = imageLayout.findViewById(R.id.image) as ImageView
        val play = imageLayout.findViewById(R.id.play) as ImageView

        if (imageVideoArrayList[position].video == false) {
            play.visibility = View.INVISIBLE
            Picasso.get().load(imageVideoArrayList[position].url).into(image)
            //image.setImageResource(imageVideoArrayList[position].url.getImageDrawables())
            //image.setImageUri()
        }
        else{
            play.visibility = View.VISIBLE
            /*
            val thumbnail = ThumbnailUtils.createVideoThumbnail(imageVideoArrayList[position].url!!, MediaStore.Video.Thumbnails.MINI_KIND)
            if (thumbnail == null) {
                Log.d("SlidingImageVideoAdapter_bitmap", "NULL")
            }
            image.setImageBitmap(thumbnail)
            //image.background = null
            */
            //val thumbnail = retriveVideoFrameFromVideo(imageVideoArrayList[position].url)
            //image.setImageBitmap(thumbnail)
    }

        imageLayout.setOnClickListener {
            if (imageVideoArrayList[position].video == true) {
                this.fragment = fragment
                fragment.createTempFile("videos", "df3ba79c-7ec2-4136-ab10-e9f52b78f683")
            }
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

    @Throws(Throwable::class)
    fun retriveVideoFrameFromVideo(videoPath: String): Bitmap? {
        var bitmap: Bitmap? = null
        var mediaMetadataRetriever: MediaMetadataRetriever? = null
        try {
            mediaMetadataRetriever = MediaMetadataRetriever()
            if (Build.VERSION.SDK_INT >= 14)
                mediaMetadataRetriever.setDataSource(videoPath, HashMap())
            else
                mediaMetadataRetriever.setDataSource(videoPath)
            //   mediaMetadataRetriever.setDataSource(videoPath);
            bitmap = mediaMetadataRetriever.frameAtTime
        } catch (e: Exception) {
            e.printStackTrace()
            throw Throwable("Exception in retriveVideoFrameFromVideo(String videoPath)" + e.message)

        } finally {
            mediaMetadataRetriever?.release()
        }
        return bitmap
    }
}