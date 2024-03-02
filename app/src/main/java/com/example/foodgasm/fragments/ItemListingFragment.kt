package com.example.foodgasm.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodgasm.R
import com.example.foodgasm.adapters.ItemListingAdapter
import com.example.foodgasm.databinding.FragmentItemListingBinding
import com.example.foodgasm.utils.Resource
import com.example.foodgasm.utils.hideBottomNavigationView
import com.example.foodgasm.viewmodel.MenuViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ItemListingFragment : Fragment(R.layout.fragment_item_listing) {
    private lateinit var binding: FragmentItemListingBinding
    private lateinit var itemListingAdapter: ItemListingAdapter
    private val args by navArgs<ItemListingFragmentArgs>()
    private val viewModel by viewModels<MenuViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        hideBottomNavigationView()
        binding = FragmentItemListingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val restaurant = args.restaurant
        viewModel.fetchItem(restaurant.id)

        setUpRv()

        itemListingAdapter.onClick = { item ->
            val action =
                ItemListingFragmentDirections.actionItemListingFragmentToFoodListingFragment(
                    item,
                    restaurant.id
                )
            Log.d("Navigation", "Action ID: ${action.actionId}")
            findNavController().navigate(action)
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.item.collectLatest {
                    when (it) {
                        is Resource.Success -> {
                            binding.pgbarFoodItem.visibility = View.GONE
                            itemListingAdapter.differ2.submitList(it.data)
                        }

                        is Resource.Error -> {
                            binding.pgbarFoodItem.visibility = View.GONE
                            Toast.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT).show()
                        }

                        is Resource.Loading -> binding.pgbarFoodItem.visibility = View.VISIBLE
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun setUpRv() {
        itemListingAdapter = ItemListingAdapter()
        binding.itemrv.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = itemListingAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        Log.e("PRessed", "Error")
        // Add back button listener
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // Navigate up when back button is pressed
            findNavController().navigate(R.id.homeFragment)
        }
    }


}