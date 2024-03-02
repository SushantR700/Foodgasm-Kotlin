package com.example.foodgasm.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.foodgasm.R
import com.example.foodgasm.databinding.FragmentAccountOptionsBinding

class AccountOptionsFragment : Fragment(R.layout.fragment_account_options) {
    private lateinit var binding: FragmentAccountOptionsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAccountOptionsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.register.setOnClickListener {
            findNavController().navigate(R.id.action_accountOptionsFragment2_to_signupFragment)
        }

        binding.login.setOnClickListener {
            findNavController().navigate(R.id.action_accountOptionsFragment2_to_loginFragment)
        }

    }
}