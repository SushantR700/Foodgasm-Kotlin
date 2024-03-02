package com.example.foodgasm.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(
   val fragments: List<Fragment>,
    fm:FragmentManager,
    lifecycle: Lifecycle
) :FragmentStateAdapter(fm,lifecycle)   {
    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    override fun getItemId(position: Int): Long {
        // Return a stable ID for the fragment at the given position
        return fragments[position].javaClass.name.hashCode().toLong()
    }

}