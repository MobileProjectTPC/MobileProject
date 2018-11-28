package com.example.joni.mobileproject.fragments


import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.joni.mobileproject.R
import com.example.joni.mobileproject.databinding.FragmentDetailBinding
import com.squareup.picasso.Picasso
import java.io.Serializable
import com.example.joni.mobileproject.MainActivity
import kotlinx.android.synthetic.main.fragment_detail.*
import java.lang.Exception
import android.graphics.BitmapFactory
import com.example.joni.mobileproject.models.Image


class DetailFragment : Fragment() {

    private lateinit var binding: FragmentDetailBinding
    private var position: Int = 0
    private var page: Int = 0
    private var myList = java.util.ArrayList<Image>()
    private lateinit var imageUri: Uri
    private var byteArray: ByteArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            position = it.getInt(EXTRA_POSITION)
            page = it.getInt(EXTRA_PAGE)
            myList = it.getSerializable(MY_LIST) as java.util.ArrayList<Image>
            /*
            if (it.getByteArray(BYTE_ARRAY) != null) {
                byteArray = it.getByteArray(BYTE_ARRAY)
            }
            */
        }
        Log.d("tää", "täs on position = $position")



        //imageUri = Uri.parse(myList[position].imageUrl)


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail, container, false)
        binding.image.transitionName = "${getString(R.string.transition_image)}_${page}_$position"

        //val byteArray = arguments!!.getByteArray("image")
        /*
        if (byteArray != null) {
            val bmp = BitmapFactory.decodeByteArray(byteArray!!, 0, byteArray!!.size)
            binding.image.setImageBitmap(bmp)
        }
        else {
            imageUri = Uri.parse(myList[position].imageUrl)
            Picasso.get()
                    .load(imageUri)
                    .placeholder(R.drawable.workshop_tutor_icon)
                    .error(R.drawable.workshop_tutor_logo_text)
                    .into(binding.image)
        }
        */

        imageUri = Uri.parse(myList[position].imageUrl)
        Picasso.get()
                .load(imageUri)
                .placeholder(R.drawable.progress_animation)
                .error(R.drawable.workshop_tutor_logo_text)
                .into(binding.image)
        /*
        Picasso.get()
                .load(imageUri)
                .into(binding.image, object : com.squareup.picasso.Callback {
                    override fun onError(e: Exception?) {
                        //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onSuccess() {
                        startPostponedEnterTransition()
                        //do smth when picture is loaded successfully

                    }
                })
        */



        binding.text.transitionName = "${getString(R.string.transition_text)}_${page}_$position"
        binding.text.text = myList[position].title



        return binding.root
    }

    companion object {

        private const val EXTRA_POSITION = "com.example.joni.mobileproject#POSITION"
        private const val EXTRA_PAGE = "com.example.joni.mobileproject#PAGE"
        private const val MY_LIST = "mylist"
        private const val BYTE_ARRAY = "bytearray"


        fun newInstanceB(position: Int, page: Int, myList: Serializable, byteArray: ByteArray): DetailFragment {
            return DetailFragment().apply {
                arguments = Bundle().apply {
                    putInt(EXTRA_POSITION, position)
                    putInt(EXTRA_PAGE, page)
                    putSerializable(MY_LIST, myList)
                    putByteArray(BYTE_ARRAY, byteArray)
                    Log.d("tää", "null? $myList")
                }
            }
        }

        fun newInstance(position: Int, page: Int, myList: Serializable): DetailFragment {
            return DetailFragment().apply {
                arguments = Bundle().apply {
                    putInt(EXTRA_POSITION, position)
                    putInt(EXTRA_PAGE, page)
                    putSerializable(MY_LIST, myList)
                    Log.d("tää", "null? $myList")
                    Log.d("tää", "täs on position?? = $position")
                }
            }
        }
    }

}
