package com.example.joni.mobileproject.adapters

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Parcelable
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.example.joni.mobileproject.R
import com.example.joni.mobileproject.fragments.DetailPortfolioFragment
import com.example.joni.mobileproject.models.ImageVideo
import com.squareup.picasso.Picasso
import java.util.ArrayList
import com.example.joni.mobileproject.models.Portfolio
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class SlidingImageVideoAdapter(var context: Context, private val imageVideoArrayList: ArrayList<ImageVideo>, var fragment: DetailPortfolioFragment, private var userCreated: Boolean, var project: Portfolio) : PagerAdapter() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    lateinit var idOfImage:String
    var realPosition: Int = -1

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

        if (!userCreated){
            delete.visibility = View.INVISIBLE
        }

        if (!imageVideoArrayList[position].video) {
            play.visibility = View.INVISIBLE
            Picasso.get().load(imageVideoArrayList[position].url).into(image)
        }
        else{
            play.visibility = View.VISIBLE
        }

        imageLayout.setOnClickListener {
            if (imageVideoArrayList[position].video) {
                fragment.createTempFile("videos", "df3ba79c-7ec2-4136-ab10-e9f52b78f683")
            }
        }

        delete.setOnClickListener {
            val builder: AlertDialog.Builder = if (Build.VERSION.SDK_INT >= 26) {
                AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert)
            } else {
                AlertDialog.Builder(context)
            }
            idOfImage = imageVideoArrayList[position].id
            builder.setTitle("Delete entry")
                    .setMessage("Are you sure you want to delete this entry? \nThis cannot be undone!")
                    .setPositiveButton(android.R.string.yes) { _, _ ->
                        // continue with delete
                        val firebaseData = FirebaseDatabase.getInstance().reference
                        val query = firebaseData.child("portfolio").child(project.uid).child("images")
                        query.addListenerForSingleValueEvent(object: ValueEventListener {
                            override fun onDataChange(p0: DataSnapshot) {
                                val numberOfImages: Long = p0.childrenCount

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
                    .setNegativeButton(android.R.string.no) { _, _ ->
                        // do nothing
                    }
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
            }


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