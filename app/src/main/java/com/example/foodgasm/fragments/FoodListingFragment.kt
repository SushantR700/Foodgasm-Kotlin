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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.foodgasm.R
import com.example.foodgasm.adapters.FoodListingAdapter
import com.example.foodgasm.databinding.FragmentFoodListingBinding
import com.example.foodgasm.utils.Resource
import com.example.foodgasm.utils.hideBottomNavigationView
import com.example.foodgasm.viewmodel.MenuViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FoodListingFragment : Fragment(R.layout.fragment_food_listing) {
    private lateinit var binding: FragmentFoodListingBinding
    private lateinit var foodListingAdapter: FoodListingAdapter
    private val args by navArgs<FoodListingFragmentArgs>()
    private val viewModel by viewModels<MenuViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        hideBottomNavigationView()
        binding = FragmentFoodListingBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val item = args.item
        val restaurantId = args.restaurantId

        viewModel.fetchFood(item.id, restaurantId)

        setUpRv()

        foodListingAdapter.onClick = { food ->
            val action =
                FoodListingFragmentDirections.actionFoodListingFragmentToFoodDetailsFragment(
                    food,
                    restaurantId
                )
            Log.d("Navigation", "Action ID: ${action.actionId}")
            findNavController().navigate(action)
        }

        lifecycleScope.launch {
            viewModel.food.collectLatest { result ->
                when (result) {
                    is Resource.Success -> {
                        foodListingAdapter.differ2.submitList(result.data)
                        binding.pgbarFoodItem.visibility = View.GONE
                    }

                    is Resource.Error -> {
                        Toast.makeText(requireContext(), item.id, Toast.LENGTH_SHORT).show()
                        binding.pgbarFoodItem.visibility = View.GONE
                    }

                    is Resource.Loading -> binding.pgbarFoodItem.visibility = View.VISIBLE
                    else -> Unit
                }
            }
        }

    }

    private fun setUpRv() {
        foodListingAdapter = FoodListingAdapter()
        binding.foodrv.adapter = foodListingAdapter
    }

    override fun onResume() {
        super.onResume()
        // Add back button listener
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // Navigate up when back button is pressed
            findNavController().navigateUp()
        }
    }


}