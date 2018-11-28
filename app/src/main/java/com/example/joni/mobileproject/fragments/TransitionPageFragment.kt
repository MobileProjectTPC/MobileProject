package com.example.joni.mobileproject.fragments

import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.joni.mobileproject.R
import com.example.joni.mobileproject.adapters.TransitionListAdapter
import com.example.joni.mobileproject.adapters.TransitionNavigation
import com.example.joni.mobileproject.databinding.FragmentPageTransitionBinding
import java.io.Serializable
import java.util.ArrayList


class TransitionPageFragment : Fragment() {

    private lateinit var binding: FragmentPageTransitionBinding
    private lateinit var navigation: TransitionNavigation

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is TransitionNavigation) {
            navigation = context
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //val myList: ArrayList<String> = arguments!!.getStringArrayList("Parcel");
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_page_transition, container, false)
        binding.listItem.layoutManager = GridLayoutManager(activity, 2)
        binding.listItem.adapter = TransitionListAdapter(navigation, arguments?.getInt(EXTRA_PAGE) ?: 0, arguments!!.getSerializable(MY_LIST))
        return binding.root
    }

    companion object {

        private const val EXTRA_PAGE = "com.example.joni.mobileproject#PAGE"
        private const val MY_LIST = "mylist"

        fun newInstance(page: Int, myList: Serializable): TransitionPageFragment {
            return TransitionPageFragment().apply {
                arguments = Bundle().apply {
                    putInt(EXTRA_PAGE, page)
                    putSerializable(MY_LIST, myList)
                    //putParcelableArrayList(MY_LIST, myList)
                }
            }
        }

    }

}
