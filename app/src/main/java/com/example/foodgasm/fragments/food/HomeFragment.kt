package com.example.foodgasm.fragments.food

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.foodgasm.R
import com.example.foodgasm.adapters.ViewPagerAdapter
import com.example.foodgasm.databinding.FragmentHomeBinding
import com.example.foodgasm.fragments.category.ChineseFragment
import com.example.foodgasm.fragments.category.HealthFoodFragment
import com.example.foodgasm.fragments.category.MainCategoryFragment
import com.example.foodgasm.fragments.category.NepaliFragment
import com.example.foodgasm.utils.showBottomNavigationView
import com.google.android.material.tabs.TabLayoutMediator

class HomeFragment : Fragment(R.layout.fragment_home) {
    private val binding by lazy {
        FragmentHomeBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        showBottomNavigationView()
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoryList = arrayListOf(
            MainCategoryFragment(),
            ChineseFragment(),
            NepaliFragment(),
            HealthFoodFragment()

        )
        binding.viewpagerHome.isUserInputEnabled = false
        binding.viewpagerHome.offscreenPageLimit = 3

        val viewPagerAdapter = ViewPagerAdapter(categoryList, childFragmentManager, lifecycle)
        binding.viewpagerHome.adapter = viewPagerAdapter
        TabLayoutMediator(binding.tablayout, binding.viewpagerHome) { tab, position ->
            when (position) {
                0 -> tab.text = "Main"
                1 -> tab.text = "Chinese"
                2 -> tab.text = "Nepali"
                3 -> tab.text = "HealthFoods"
            }

        }.attach()

    }
}