package com.example.foodgasm.fragments

import android.os.Bundle
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
import com.example.foodgasm.data.CartProduct
import com.example.foodgasm.databinding.FragmentFoodDetailsBinding
import com.example.foodgasm.utils.Resource
import com.example.foodgasm.utils.hideBottomNavigationView
import com.example.foodgasm.viewmodel.CartAndDetailsViewModel
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FoodDetailsFragment : Fragment(R.layout.fragment_food_details) {
    private lateinit var binding: FragmentFoodDetailsBinding
    private val args by navArgs<FoodDetailsFragmentArgs>()
    private val viewModel by viewModels<CartAndDetailsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        hideBottomNavigationView()
        binding = FragmentFoodDetailsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        args.food.let { food ->
            binding.tvFoodName.text = food.name
            binding.tvFoodDesc.text = food.desc

            Picasso.get().load(food.image)
                .placeholder(R.drawable.gray_background)
                .error(R.mipmap.ic_launcher_foreground)
                .into(binding.ivFood)

            binding.btnAddToCart.setOnClickListener {
                val product = CartProduct(
                    id = System.currentTimeMillis().toString(),
                    restaurantId = args.restaurantId,
                    name = food.name,
                    price = food.price,
                    quantity = 1,
                    image = food.image
                )
                viewModel.addUpdateProductInCart(product)
            }
        }

        lifecycleScope.launch {
            viewModel.addToCart.collectLatest { result ->
                when (result) {
                    is Resource.Loading -> {
                        binding.btnAddToCart.startAnimation()
                    }

                    is Resource.Success -> {
                        binding.btnAddToCart.revertAnimation()
                        Toast.makeText(
                            requireContext(),
                            "Added to cart successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().popBackStack()
                    }

                    is Resource.Error -> {
                        binding.btnAddToCart.revertAnimation()
                        Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                    }

                    else -> Unit
                }
            }
        }


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