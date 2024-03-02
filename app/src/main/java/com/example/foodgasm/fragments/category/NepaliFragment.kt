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
import com.example.foodgasm.R
import com.example.foodgasm.adapters.ChineseAdapter
import com.example.foodgasm.adapters.NepaliAdapter
import com.example.foodgasm.databinding.FragmentChineseBinding
import com.example.foodgasm.databinding.FragmentNepaliBinding
import com.example.foodgasm.utils.Resource
import com.example.foodgasm.viewmodel.OtherCategoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
@AndroidEntryPoint
class NepaliFragment:Fragment(R.layout.fragment_nepali) {
    private lateinit var binding: FragmentNepaliBinding
    private lateinit var nepaliAdapter: NepaliAdapter
    private val viewModel by viewModels<OtherCategoryViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentNepaliBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRv()
        lifecycleScope.launch {
            viewModel.nepaliRes.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        showLoading()
                    }
                    is Resource.Success -> {
                        nepaliAdapter.differ.submitList(it.data)
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
        nepaliAdapter= NepaliAdapter()
        binding.nepalirv.apply {
            layoutManager=
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL,false)
            adapter=nepaliAdapter
        }
    }
}