package com.example.joni.mobileproject.adapters

import android.view.View

interface TransitionNavigation {

    fun goToDetail(transitionItems: List<View>, position: Int, page: Int)

}