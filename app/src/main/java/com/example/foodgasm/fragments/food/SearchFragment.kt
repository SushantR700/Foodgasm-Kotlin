package com.example.foodgasm.fragments.food

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.foodgasm.R
import com.example.foodgasm.adapters.FeaturedResAdapter
import com.example.foodgasm.databinding.FragmentSearchBinding
import com.example.foodgasm.utils.Resource
import com.example.foodgasm.viewmodel.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
open class SearchFragment : Fragment() {
    private var binding: FragmentSearchBinding? = null
    private var adapter: FeaturedResAdapter? = null
    private val viewModel by viewModels<SearchViewModel>()

    private val REQUEST_PERMISSION = 3000
    private val REQUEST_SPEECH_RECOGNIZER = 3001

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
    }

    private fun setupViews() {
        adapter = FeaturedResAdapter(smallItem = true) {
            val b = Bundle().apply { putParcelable("restaurant", it) }
            val actionId = R.id.action_searchFragment_to_itemListingFragment
            Log.d("Navigation", "Action ID: $actionId")
            findNavController().navigate(actionId, b)
        }
        binding?.rvRestaurants?.setHasFixedSize(true)
        binding?.rvRestaurants?.adapter = adapter

        lifecycleScope.launch {
            viewModel.restaurants.collectLatest {
                when (it) {
                    is Resource.Loading -> binding?.pgbarSearch?.visibility = View.VISIBLE
                    is Resource.Success -> {
                        adapter?.differ?.submitList(it.data)
                        binding?.pgbarSearch?.visibility = View.GONE
                    }

                    is Resource.Error -> {
                        binding?.pgbarSearch?.visibility = View.GONE
                    }

                    else -> Unit
                }
            }
        }

        binding?.etSearch?.doAfterTextChanged {
            viewModel.findRestaurants(it?.toString())
        }

        binding?.ivMicrophone?.setOnClickListener {
            if (isRecordAudioPermissionGranted()) {
                requestRecordAudioPermission()
            } else {
                startSpeechRecognizer()
            }
        }
    }

    private fun isRecordAudioPermissionGranted() = ContextCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.RECORD_AUDIO
    ) != PackageManager.PERMISSION_GRANTED

    private fun requestRecordAudioPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.RECORD_AUDIO),
            REQUEST_PERMISSION
        )
    }

    private fun startSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(requireContext())) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            startActivityForResult(intent, REQUEST_SPEECH_RECOGNIZER)
        } else {
            Toast.makeText(requireContext(), "Speech recognizer not available!", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PERMISSION) {
            if (resultCode == PackageManager.PERMISSION_GRANTED) {
                startSpeechRecognizer()
            }
        }
        if (requestCode == REQUEST_SPEECH_RECOGNIZER) {
            if (resultCode == RESULT_OK) {
                val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                results?.firstOrNull()?.let {
                    binding?.etSearch?.setText(it)
                    viewModel.findRestaurants(it)
                }
            }
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}