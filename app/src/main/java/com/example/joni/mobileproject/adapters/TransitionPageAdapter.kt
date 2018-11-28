package com.example.joni.mobileproject.adapters

import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.example.joni.mobileproject.fragments.TransitionPageFragment
import java.io.Serializable
import java.util.ArrayList

class TransitionPageAdapter(fm: FragmentManager, val myList: Serializable) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return TransitionPageFragment.newInstance(position, myList)
    }

    override fun getCount(): Int {
        return 1
    }
}
