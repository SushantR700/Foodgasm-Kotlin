package com.example.foodgasm.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.foodgasm.R
import com.example.foodgasm.databinding.FragmentIntroBinding

class IntroductionFragment:Fragment(R.layout.fragment_intro) {
    lateinit var binding: FragmentIntroBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentIntroBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.getStarted.setOnClickListener {
            findNavController().navigate(R.id.action_introductionFragment_to_accountOptionsFragment2)
        }
    }
}