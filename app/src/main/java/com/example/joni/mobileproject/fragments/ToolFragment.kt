package com.example.joni.mobileproject.fragments

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.joni.mobileproject.R
import com.example.joni.mobileproject.adapters.TransitionPageAdapter
import com.example.joni.mobileproject.databinding.FragmentToolBinding

class ToolFragment : Fragment() {

    private lateinit var binding: FragmentToolBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val myList = arguments!!.getSerializable("Parcel")
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tool, container, false)
        binding.pager.adapter = TransitionPageAdapter(childFragmentManager, myList)
        return binding.root
    }
}
