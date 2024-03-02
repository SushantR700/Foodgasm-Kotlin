package com.example.foodgasm.fragments

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.foodgasm.R
import com.example.foodgasm.data.User
import com.example.foodgasm.databinding.FragmentSignupBinding
import com.example.foodgasm.utils.RegisterValidation
import com.example.foodgasm.utils.Resource
import com.example.foodgasm.viewmodel.SignUpViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignupFragment:Fragment() {
    private lateinit var binding: FragmentSignupBinding
    private val viewModel by viewModels<SignUpViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignupBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.textView2.setOnClickListener {
            findNavController().navigate(R.id.action_signupFragment_to_loginFragment)
        }

        binding.apply {
            register.setOnClickListener {
                val user = User(
                    editText.text.toString().trim(),
                    editText2.text.toString().trim(),
                    editText3.text.toString().trim()
                )
                val password = editText4.text.toString()
                viewModel.createAccountWithEmailAndPassword(user, password)
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.register.collect {
                when (it) {
                    is Resource.Success -> {
                        Log.d(TAG, it.data.toString())
                        Toast.makeText(context, "Check your mail to verify!", Toast.LENGTH_SHORT)
                            .show()
                        binding.register.revertAnimation()
                        findNavController().navigate(R.id.loginFragment)
                    }

                    is Resource.Error -> {
                        Log.e(TAG, it.message.toString())
                        Toast.makeText(context, "Registration Failed", Toast.LENGTH_SHORT).show()
                        binding.register.revertAnimation()
                    }

                    is Resource.Loading -> {
                        binding.register.startAnimation()
                    }

                    is Resource.Verify -> {
                        Toast.makeText(context, "Check your mail to verify! ", Toast.LENGTH_SHORT)
                            .show()
                        binding.register.revertAnimation()
                    }

                    else -> Unit
                }
            }
            lifecycleScope.launchWhenStarted {
                viewModel.validation.collect {
                    if (it.email is RegisterValidation.Failed) {
                        binding.editText3.apply {
                            requestFocus()
                            error = it.email.message
                        }
                    }

                    if (it.password is RegisterValidation.Failed) {
                        binding.editText4.apply {
                            requestFocus()
                            error = it.password.message
                        }
                    }

                }

            }
        }
    }
}