package com.example.foodgasm.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.foodgasm.R
import com.example.foodgasm.activities.ShoppingActivity
import com.example.foodgasm.data.User
import com.example.foodgasm.databinding.FragmentLoginBinding
import com.example.foodgasm.dialog.setupBottomSheetDialog
import com.example.foodgasm.utils.Resource
import com.example.foodgasm.utils.SharedPrefsUtils
import com.example.foodgasm.viewmodel.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private val viewModel by viewModels<LoginViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        // check if we are already logged in and user is already verified
        if (auth.currentUser != null && auth.currentUser?.isEmailVerified == true) {
            navigateToShopping()
        }

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1020521020125-v79bpbsi3797t1t7eo87k7qnh8f8cmhp.apps.googleusercontent.com")
            .requestEmail()
            .build()

        // Google Sign In
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), googleSignInOptions)

        binding.btnGoogle.setOnClickListener {
            val signIntent = googleSignInClient.signInIntent
            launcher.launch(signIntent)
            binding.register.startAnimation()
        }

        binding.textView2.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
        }
        binding.register.setOnClickListener {
            val email = binding.editText.text.toString().trim()
            val password = binding.editText2.text.toString()
            if (email != "" && password != "") {
                viewModel.login(email, password)
            }
        }
        binding.forgotpassword.setOnClickListener {
            setupBottomSheetDialog { email ->
                viewModel.resetPassword(email)

            }
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.resetPassword.collect {
                    when (it) {
                        is Resource.Loading -> Unit

                        is Resource.Success -> {
                            Snackbar.make(
                                requireView(),
                                "Link has been sent to the email address",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }

                        is Resource.Error -> {
                            Snackbar.make(
                                requireView(),
                                "Failed:${it.message.toString()}",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }

                        else -> Unit
                    }
                }
            }
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.login.collect {
                    when (it) {
                        is Resource.Loading -> {
                            binding.register.startAnimation()
                        }

                        is Resource.Success -> {
                            val email = binding.editText.text.toString()
                            val password = binding.editText2.text.toString()

                            SharedPrefsUtils.saveLogin(email, password)

                            binding.register.revertAnimation()
                            navigateToShopping()
                        }

                        is Resource.Error -> {
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                            binding.register.revertAnimation()
                        }

                        is Resource.Verify -> {
                            Toast.makeText(
                                requireContext(),
                                "Please verify your email",
                                Toast.LENGTH_LONG
                            ).show()
                            binding.register.revertAnimation()
                        }

                        else -> Unit
                    }
                }
            }
        }

    }

    private fun navigateToShopping() {
        Intent(requireContext(), ShoppingActivity::class.java).also {
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(it)
        }
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                if (task.isSuccessful) {
                    val account: GoogleSignInAccount = task.result
                    val credential: AuthCredential =
                        GoogleAuthProvider.getCredential(account.idToken, null)
                    auth.signInWithCredential(credential).addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            Toast.makeText(
                                requireActivity(),
                                "Login Successful with Google",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.register.stopAnimation()
                            navigateToShopping()
                        } else {
                            Toast.makeText(requireActivity(), "Login Failed", Toast.LENGTH_SHORT)
                                .show()
                        }

                    }
                } else {
                    Toast.makeText(requireActivity(), "Google sign-in failed", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(
                    requireActivity(),
                    "Google sign-in failed: Please check sha-1, sha-256 fingerprints",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
}