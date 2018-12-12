package com.example.joni.mobileproject.adapters

import android.app.AlertDialog
import android.content.Context
import android.os.Parcelable
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.joni.mobileproject.R
import com.example.joni.mobileproject.fragments.DetailPortfolioFragment
import com.example.joni.mobileproject.models.ImageVideo
import com.squareup.picasso.Picasso
import java.util.ArrayList
import android.graphics.Bitmap
import android.os.Build
import android.media.MediaMetadataRetriever
import android.util.Log
import android.widget.Toast
import com.example.joni.mobileproject.models.Portfolio
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class SlidingImageVideoAdapter(var context: Context, private val imageVideoArrayList: ArrayList<ImageVideo>, var fragment: DetailPortfolioFragment, var userCreated: Boolean, var project: Portfolio) : PagerAdapter() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    final lateinit var idOfImage:String
    final var realPosition: Int = -1

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
        val delete = imageLayout.findViewById(R.id.btnDelete) as ImageView

        realPosition = position

        Log.d("SlidingImageVideoAdapter_test", "imageVideoArrayList: " + imageVideoArrayList.toString())
        Log.d("SlidingImageVideoAdapter_test","Position: " + position)


        if (userCreated == false){
            delete.visibility = View.INVISIBLE
        }

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
            Log.d("SlidingImageVideoAdapter_test","imageVideoArrayList.toString(): " + imageVideoArrayList.toString())
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

        delete.setOnClickListener {
            Log.d("SlidingImageVideoAdapter_test","imageVideoArrayList.toString(): " + imageVideoArrayList.toString())
            val builder: AlertDialog.Builder
            idOfImage = imageVideoArrayList[position].id
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert)
            } else {
                builder = AlertDialog.Builder(context)
            }
            builder.setTitle("Delete entry")
                    .setMessage("Are you sure you want to delete this entry? \nThis cannot be undone!")
                    .setPositiveButton(android.R.string.yes) { dialog, which ->
                        // continue with delete
                        var firebaseData = FirebaseDatabase.getInstance().reference
                        var query = firebaseData.child("portfolio").child(project.uid).child("images")
                        query.addListenerForSingleValueEvent(object: ValueEventListener {
                            override fun onDataChange(p0: DataSnapshot) {
                                var numberOfImages: Long = p0.childrenCount
                                Log.d("SlidingImageVideoAdapter_test","idOfImage: " + idOfImage)
                                Log.d("SlidingImageVideoAdapter_test","realPosition: " + realPosition)
                                Log.d("SlidingImageVideoAdapter_test","numberOfImages: " + numberOfImages)
                                //Log.d("SlidingImageVideoAdapter_test","project.uid: " + project.uid)
                                //Log.d("SlidingImageVideoAdapter_test","imageVideoArrayList.toString(): " + imageVideoArrayList.toString())
                                //Log.d("SlidingImageVideoAdapter_test","imageVideoArrayList[position].uid: " + imageVideoArrayList[position].id)

                                if (realPosition < numberOfImages){
                                    firebaseData.child("portfolio").child(project.uid).child("images").child(idOfImage).removeValue()
                                    Toast.makeText(context, "The image has been deleted", Toast.LENGTH_LONG).show()
                                }
                                else {
                                    firebaseData.child("portfolio").child(project.uid).child("videos").child(idOfImage).removeValue()
                                    Toast.makeText(context, "The video has been deleted", Toast.LENGTH_LONG).show()
                                }
                            }
                            override fun onCancelled(p0: DatabaseError) {
                            }
                        })

                        imageVideoArrayList.removeAt(position)
                        this.notifyDataSetChanged()
                    }
                    .setNegativeButton(android.R.string.no) { dialog, which ->
                        // do nothing
                    }
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
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