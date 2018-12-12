package com.example.joni.mobileproject.adapters

import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.joni.mobileproject.R
import com.example.joni.mobileproject.databinding.FragmentPageTransitionBinding
import java.io.Serializable


class TransitionPortfolioFragment : Fragment() {

    private lateinit var binding: FragmentPageTransitionBinding
    private lateinit var navigation: TransitionNavigation

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is TransitionNavigation) {
            navigation = context
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_page_transition, container, false)
        binding.listItem.layoutManager = GridLayoutManager(activity, 2)
        binding.listItem.adapter = TransitionPortfolioAdapter(navigation, arguments?.getInt(EXTRA_PAGE) ?: 0, arguments!!.getSerializable(MY_LIST))
        return binding.root
    }

    companion object {

        private const val EXTRA_PAGE = "PAGE"
        private const val MY_LIST = "myList"

        fun newInstance(page: Int, myList: Serializable): TransitionPortfolioFragment {
            return TransitionPortfolioFragment().apply {
                arguments = Bundle().apply {
                    putInt(EXTRA_PAGE, page)
                    putSerializable(MY_LIST, myList)
                }
            }
        }
    }
}
