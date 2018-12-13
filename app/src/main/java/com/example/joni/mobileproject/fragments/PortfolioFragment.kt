package com.example.joni.mobileproject.fragments

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.joni.mobileproject.PortfolioActivity
import com.example.joni.mobileproject.R
import com.example.joni.mobileproject.adapters.TransitionPageAdapter
import com.example.joni.mobileproject.adapters.TransitionPortfolioPageAdapter
import com.example.joni.mobileproject.databinding.FragmentToolBinding
import com.example.joni.mobileproject.models.Image
import com.example.joni.mobileproject.models.Portfolio
import com.google.firebase.auth.FirebaseUser


class PortfolioFragment : Fragment() {
    private lateinit var binding: FragmentToolBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val myList = arguments!!.getSerializable("Parcel")
        val portfolioPosition = arguments!!.getString("portfolio")
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tool, container, false)
        binding.pager.adapter = TransitionPortfolioPageAdapter(childFragmentManager, myList)

        val list = myList as java.util.ArrayList<Image>
        val port = arguments!!.getSerializable("Portfolios") as java.util.ArrayList<Portfolio>
        val arr = arguments?.getSerializable("fireBaseUser") as java.util.ArrayList<FirebaseUser>?
        var fireBaseUser: FirebaseUser? = null

        if (arr != null) {
            fireBaseUser = arr[0]
        }

        if (portfolioPosition.toInt() > -1) {
            //PortfolioActivity().goToDetailFromEdit(portfolioPosition.toInt(), list, port, fireBaseUser)


            val detailPortfolioFragment = DetailPortfolioFragment.newInstance(portfolioPosition.toInt(), 1, list, port, fireBaseUser!!.uid, false)

            val transaction = activity!!.supportFragmentManager.beginTransaction()

            transaction.replace(R.id.root, detailPortfolioFragment, PortfolioActivity.DETAIL_FRAGMENT_TAG)
            transaction.addToBackStack(null)
            transaction.commit()

        }

        return binding.root
    }
}