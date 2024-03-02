package com.example.foodgasm.utils

import android.view.View
import androidx.fragment.app.Fragment
import com.example.foodgasm.activities.ShoppingActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

fun Fragment.hideBottomNavigationView(){
    val bottomNavigationView =
        (activity as ShoppingActivity).findViewById<BottomNavigationView>(
            com.example.foodgasm.R.id.bottom_navigation
        )
    bottomNavigationView.visibility = View.GONE
}

fun Fragment.showBottomNavigationView(){
    val bottomNavigationView =
        (activity as ShoppingActivity).findViewById<BottomNavigationView>(
            com.example.foodgasm.R.id.bottom_navigation
        )
    bottomNavigationView?.visibility = View.VISIBLE
}