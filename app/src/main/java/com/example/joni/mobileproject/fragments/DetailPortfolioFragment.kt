package com.example.joni.mobileproject.fragments

import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.joni.mobileproject.R
import com.example.joni.mobileproject.adapters.SlidingImageAdapter
import com.example.joni.mobileproject.databinding.FragmentPortfolioDetailBinding
import com.example.joni.mobileproject.models.Image
import com.example.joni.mobileproject.models.ImageModel
import com.example.joni.mobileproject.models.Portfolio
import com.squareup.picasso.Picasso
import com.viewpagerindicator.CirclePageIndicator
import java.io.Serializable

class DetailPortfolioFragment : Fragment() {

    private lateinit var binding: FragmentPortfolioDetailBinding
    private var position: Int = 0
    private var page: Int = 0
    private var myList: ArrayList<Image> = java.util.ArrayList()
    private lateinit var imageUri: Uri
    private var nfcTrue = false

    private var summaryImageModelArrayList: java.util.ArrayList<ImageModel>? = null
    val mySummaryImageList = intArrayOf(
            R.drawable.workshop_tutor_logo_text,
            R.drawable.workshop_tutor_logo_text,
            R.drawable.workshop_tutor_logo_text,
            R.drawable.workshop_tutor_logo_text,
            R.drawable.workshop_tutor_logo_text,
            R.drawable.workshop_tutor_logo_text
    )
    private var progressImageModelArrayList: java.util.ArrayList<ImageModel>? = null
    val myProgressImageList = intArrayOf(
            R.drawable.workshop_tutor_logo_text,
            R.drawable.workshop_tutor_logo_text,
            R.drawable.workshop_tutor_logo_text,
            R.drawable.workshop_tutor_logo_text,
            R.drawable.workshop_tutor_logo_text,
            R.drawable.workshop_tutor_logo_text
    )
    private lateinit var mSummaryPager: ViewPager
    private lateinit var summaryIndicator: CirclePageIndicator
    private lateinit var mProgressPager: ViewPager
    private lateinit var progressIndicator: CirclePageIndicator


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("DetailPortfolioFragment_test", "onCreate()")
        arguments?.let {
            position = it.getInt(EXTRA_POSITION)
            page = it.getInt(EXTRA_PAGE)
            myList = it.getSerializable(MY_LIST) as java.util.ArrayList<Image>
            nfcTrue = it.getBoolean(NFC_TRUE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (nfcTrue) { activity!!.finish() }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_portfolio_detail, container, false)
        binding.image.transitionName = "${getString(R.string.transition_image)}_${page}_$position"
        binding.text.transitionName = "${getString(R.string.transition_text)}_${page}_$position"
        binding.text.text = myList[position].title

        imageUri = Uri.parse(myList[position].imageUrl)
        Picasso.get()
                .load(imageUri)
                .placeholder(R.drawable.progress_animation)
                .error(R.drawable.workshop_tutor_logo_text)
                .into(binding.image)

        val rootView = inflater.inflate(R.layout.fragment_portfolio_detail, container, false)

        summaryImageModelArrayList = ArrayList()
        summaryImageModelArrayList = populateList(mySummaryImageList)
        progressImageModelArrayList = ArrayList()
        progressImageModelArrayList = populateList(myProgressImageList)

        mSummaryPager = rootView.findViewById(R.id.summaryImagePager)
        mSummaryPager.adapter = SlidingImageAdapter(
                context!!,
                this.summaryImageModelArrayList!!
        )

        summaryIndicator = rootView.findViewById(R.id.summaryImageIndicator)


        mProgressPager = rootView.findViewById(R.id.progressImagePager)
        mProgressPager.adapter = SlidingImageAdapter(
                context!!,
                this.progressImageModelArrayList!!
        )

        progressIndicator = rootView.findViewById(R.id.progressImageIndicator)

        return binding.root
    }


    private fun populateList(imagelist: IntArray): java.util.ArrayList<ImageModel> {

        val list = java.util.ArrayList<ImageModel>()

        for (i in 0..5) {
            val imageModel = ImageModel()
            imageModel.setImageDrawables(imagelist[i])
            list.add(imageModel)
        }
        return list
    }

    companion object {

        private const val EXTRA_POSITION = "com.example.joni.mobileproject#POSITION"
        private const val EXTRA_PAGE = "com.example.joni.mobileproject#PAGE"
        private const val MY_LIST = "mylist"
        private const val NFC_TRUE = "nfc"

        fun newInstance(position: Int, page: Int, myList: Serializable, nfcTrue: Boolean): DetailPortfolioFragment {
            return DetailPortfolioFragment().apply {
                arguments = Bundle().apply {
                    putInt(EXTRA_POSITION, position)
                    putInt(EXTRA_PAGE, page)
                    putSerializable(MY_LIST, myList)
                    putBoolean(NFC_TRUE, nfcTrue)
                }
            }
        }
    }
}