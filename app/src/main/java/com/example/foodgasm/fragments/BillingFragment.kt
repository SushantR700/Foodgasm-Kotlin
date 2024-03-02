package com.example.foodgasm.fragments


import android.app.AlertDialog
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
import androidx.recyclerview.widget.RecyclerView
import com.example.foodgasm.R
import com.example.foodgasm.adapters.AddressAdapter
import com.example.foodgasm.adapters.BillingProductsAdapter
import com.example.foodgasm.data.Address
import com.example.foodgasm.data.CartProduct
import com.example.foodgasm.data.PaymentMethod
import com.example.foodgasm.databinding.FragmentBillingBinding
import com.example.foodgasm.order.Order
import com.example.foodgasm.order.OrderStatus
import com.example.foodgasm.utils.Constants
import com.example.foodgasm.utils.HorizontalItemDecoration
import com.example.foodgasm.utils.Resource
import com.example.foodgasm.viewmodel.BillingViewModel
import com.example.foodgasm.viewmodel.OrderViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.khalti.checkout.helper.Config
import com.khalti.checkout.helper.KhaltiCheckOut
import com.khalti.checkout.helper.OnCheckOutListener
import com.khalti.checkout.helper.PaymentPreference
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class BillingFragment : Fragment() {
    private lateinit var binding: FragmentBillingBinding
    private val addressAdapter by lazy { AddressAdapter() }
    private val billingProductsAdapter by lazy { BillingProductsAdapter() }
    private val billingViewModel by viewModels<BillingViewModel>()
    private val args by navArgs<BillingFragmentArgs>()
    private var products = emptyList<CartProduct>()
    private var totalPrice = 0f
    private var paymentMethod = PaymentMethod.Khalti

    private var selectedAddress: Address? = null
    private val orderViewModel by viewModels<OrderViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        products = args.products.toList()
        totalPrice = args.price
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentBillingBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBillingProductsRv()
        setupAddressRv()

        binding.imageCloseBilling.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.imageAddAddress.setOnClickListener {
            findNavController().navigate(R.id.action_billingFragment_to_addressFragment)
        }

        addressAdapter.onClick = {
            selectedAddress = it
            if (!args.payment) {
                val b = Bundle().apply { putParcelable("address", selectedAddress) }
                findNavController().navigate(R.id.action_billingFragment_to_addressFragment, b)
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                billingViewModel.address.collectLatest {
                    when (it) {
                        is Resource.Loading -> {
                            binding.progressbarAddress.visibility = View.VISIBLE
                        }

                        is Resource.Success -> {
                            addressAdapter.differ.submitList(it.data)
                            binding.progressbarAddress.visibility = View.GONE
                        }

                        is Resource.Error -> {
                            binding.progressbarAddress.visibility = View.GONE
                            Toast.makeText(
                                requireContext(),
                                "Error ${it.message}",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }

                        else -> Unit
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                orderViewModel.order.collectLatest {
                    when (it) {
                        is Resource.Loading -> {
                            binding.buttonPlaceOrder.startAnimation()
                        }

                        is Resource.Success -> {
                            binding.buttonPlaceOrder.revertAnimation()
                            findNavController().navigateUp()
                            Snackbar.make(
                                requireView(),
                                "Your order was placed",
                                Snackbar.LENGTH_LONG
                            )
                                .show()
                        }

                        is Resource.Error -> {
                            binding.buttonPlaceOrder.revertAnimation()
                            Toast.makeText(
                                requireContext(),
                                "Error ${it.message}",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }

                        else -> Unit
                    }
                }
            }
        }

        billingProductsAdapter.differ.submitList(products)

        totalPrice += 100 // add delivery charges
        binding.tvTotalPrice.text = "Rs. $totalPrice"

        binding.btnCashOnDelivery.setOnClickListener {
            paymentMethod = PaymentMethod.CashOnDelivery
        }
        binding.btnKhaltiPay.setOnClickListener { paymentMethod = PaymentMethod.Khalti }

        binding.buttonPlaceOrder.setOnClickListener {
            if (selectedAddress == null) {
                Toast.makeText(
                    requireContext(),
                    "Please select a delivery address!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            if (totalPrice == 0.0f || products.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Please add some food items in the cart!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            showOrderConfirmationDialog()
        }
    }

    private fun proceedOrderPlacement() {
        if (paymentMethod == PaymentMethod.Khalti) {
            initiateKhaltiPayment()
        } else {
            val order = Order(
                orderStatus = OrderStatus.Ordered.status,
                orderBy = Firebase.auth.uid!!,
                totalPrice = totalPrice,
                products = products,
                address = selectedAddress!!
            )
            orderViewModel.placeOrder(order)
        }
    }

    private fun initiateKhaltiPayment() {
        val config =
            Config.Builder(
                Constants.TEST_PUBLIC_KEY,
                Constants.PRODUCT_ID,
                Constants.PRODUCT_NAME,
                1100L,
                object : OnCheckOutListener {
                    override fun onError(
                        action: String,
                        errorMap: Map<String, String>
                    ) {
                        Log.i(action, errorMap.toString())
                    }

                    override fun onSuccess(data: Map<String, Any>) {
                        Log.i("success", data.toString())
                        val order = Order(
                            orderStatus = OrderStatus.Ordered.status,
                            orderBy = Firebase.auth.uid!!,
                            totalPrice = totalPrice,
                            products = products,
                            address = selectedAddress!!,
                            paid = true,
                            txnId = data["idx"] as? String ?: "",
                            txnToken = data["token"] as? String ?: ""
                        )
                        orderViewModel.placeOrder(order)
                    }
                })
                .paymentPreferences(object : ArrayList<PaymentPreference?>() {
                    init {
                        add(PaymentPreference.KHALTI)
                    }
                })
                .mobile("9800000000")
                .build()

        val checkOut = KhaltiCheckOut(requireContext(), config)
        checkOut.show()
    }

    private fun showOrderConfirmationDialog() {
        val alertDialog = AlertDialog.Builder(requireContext()).apply {
            setTitle("Order items")
            setMessage("Do you want to order your cart items?")
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            setPositiveButton("Yes") { dialog, _ ->
                proceedOrderPlacement()
                dialog.dismiss()
            }
        }
        alertDialog.create()
        alertDialog.show()
    }

    private fun setupAddressRv() {
        binding.rvAddress.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = addressAdapter
            addItemDecoration(HorizontalItemDecoration())
        }
    }


    private fun setupBillingProductsRv() {
        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = billingProductsAdapter
            addItemDecoration(HorizontalItemDecoration())
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



