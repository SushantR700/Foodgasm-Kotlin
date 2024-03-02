package com.example.foodgasm.fragments.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.denzcoskun.imageslider.constants.AnimationTypes
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.foodgasm.R
import com.example.foodgasm.adapters.ChineseAdapter
import com.example.foodgasm.adapters.FeaturedResAdapter
import com.example.foodgasm.adapters.NearByResAdapter
import com.example.foodgasm.databinding.FragmentChineseBinding
import com.example.foodgasm.databinding.FragmentMainCategoryBinding
import com.example.foodgasm.utils.Resource
import com.example.foodgasm.viewmodel.MainCategoryViewModel
import com.example.foodgasm.viewmodel.OtherCategoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
@AndroidEntryPoint
class ChineseFragment:Fragment(R.layout.fragment_chinese) {
    private lateinit var binding: FragmentChineseBinding
    private lateinit var chineseAdapter: ChineseAdapter
    private val viewModel by viewModels<OtherCategoryViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentChineseBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRv()
        lifecycleScope.launch {
            viewModel.chineseRes.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        showLoading()
                    }
                    is Resource.Success -> {
                        chineseAdapter.differ.submitList(it.data)
                        hideLoading()
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT).show()
                        hideLoading()
                    }
                    else -> Unit
                }
            }
        }



    }


    private fun hideLoading() {
        binding.progressBar.visibility= View.GONE
    }

    private fun showLoading() {
        binding.progressBar.visibility= View.VISIBLE
    }

    private fun setUpRv() {
        chineseAdapter= ChineseAdapter()
        binding.chineserv.apply {
            layoutManager=
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL,false)
            adapter=chineseAdapter
        }
    }
}