package com.example.joni.mobileproject.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.example.joni.mobileproject.fragments.TransitionPageFragment
import java.io.Serializable

class TransitionPortfolioPageAdapter(fm: FragmentManager, private val myList: Serializable) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return TransitionPortfolioFragment.newInstance(position, myList)
    }

    override fun getCount(): Int {
        return 1
    }
}
