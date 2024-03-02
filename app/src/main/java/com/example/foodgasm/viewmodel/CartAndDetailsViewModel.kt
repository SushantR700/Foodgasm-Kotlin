package com.example.foodgasm.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodgasm.Map.DistanceMatrixResponse
import com.example.foodgasm.data.CartProduct
import com.example.foodgasm.firebase.FirebaseCommon
import com.example.foodgasm.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

@HiltViewModel
class CartAndDetailsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val firebaseCommon: FirebaseCommon,
) : ViewModel() {
    private val _cartProducts =
        MutableStateFlow<Resource<List<CartProduct>>>(Resource.Initial())
    val cartProducts = _cartProducts.asStateFlow()

    private val _addToCart = MutableStateFlow<Resource<CartProduct>>(Resource.Initial())
    val addToCart = _addToCart.asStateFlow()

    fun isLoggedIn(): Boolean = auth.currentUser?.uid != null

//    fun addUpdateProductInCart(cartProduct: CartProduct){
//        viewModelScope.launch { _addToCart.emit(Resource.Loading()) }
//
//        firestore.collection("user")
//            .document(auth.uid!!)
//            .collection("cart")
//            .whereEqualTo("restaurant.id",cartProduct.restaurant.id)
//            .get()
//            .addOnSuccessListener { result ->
//                val cartItems = result.toObjects(CartProduct::class.java)
//                if (cartItems.isNotEmpty()) {
//                    // Cart is not empty
//                    val firstCartItem = cartItems.first()
//                    if (cartRestaurantName.isEmpty() || cartRestaurantId.isEmpty()) {
//                        // Set values if not already set
//                        cartRestaurantName = firstCartItem.ownerCart.name
//                        cartRestaurantId = firstCartItem.ownerCart.id
//                    }
//
//                    if (firstCartItem.ownerCart.id == secondRestaurantId) {
//                        Log.e("First item above",firstCartItem.ownerCart.name)
//                        Log.e("First item second", secondRestaurantName)
//                        if (firstCartItem.restaurant.id == cartProduct.restaurant.id) {
//                            // Increase quantity or add new product based on your logic
//                            val documentId = result.documents.first().id
//                            increaseQuantity(documentId, cartProduct)
//                        } else {
//                            viewModelScope.launch {
//                                _addToCart.emit(Resource.Error("Different Restaurants cannot be added"))
//                            }
//                        }
//                    } else {
//                        viewModelScope.launch {
//                            _addToCart.emit(Resource.Error("Cannot be added"))
//                        }
//                    }
//                } else {
//                    // Cart is empty, add the new product
//                    if (cartRestaurantName.isEmpty() || cartRestaurantId.isEmpty()) {
//
//                        cartRestaurantName = cartProduct.ownerCart.name
//                        cartRestaurantId = cartProduct.ownerCart.id
//                        Log.e("First Item",cartRestaurantName)
//                    }
//                    addNewProduct(cartProduct)
//                }
//            }
//
//                .addOnFailureListener {
//                viewModelScope.launch { _addToCart.emit(Resource.Error(it.message.toString())) }
//            }
//    }

    fun addUpdateProductInCart(newCartProduct: CartProduct) {
        viewModelScope.launch {
            try {
                _addToCart.emit(Resource.Loading())

                val cartItems = firestore.collection("user")
                    .document(auth.uid!!)
                    .collection("cart")
                    .orderBy("id", Query.Direction.ASCENDING)
                    .get().await().toObjects(CartProduct::class.java)

                val firstCartItem = cartItems.firstOrNull()

                if (firstCartItem == null) {
                    addNewProduct(newCartProduct)
                } else {
                    val firstRestaurantId = firstCartItem.restaurantId
                    val currentRestaurantId = newCartProduct.restaurantId

                    (Dispatchers.IO) {
                        if (isDistanceLessThan200M(firstRestaurantId, currentRestaurantId)) {
                            if (firstCartItem.id == newCartProduct.id) { // same product
                                increaseQuantity(firstCartItem.id)
                            } else { // different product
                                addNewProduct(newCartProduct)
                            }
                        } else {
                            (Dispatchers.Main){
                                _addToCart.emit(Resource.Error("You cannot order from two different restaurant that are more than 200 metres apart!"))
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                _addToCart.emit(Resource.Error(e.message.toString()))
            }
        }
    }

    private suspend fun isDistanceLessThan200M(firstId: String, secondId: String): Boolean {
        if (firstId == secondId) return true // same restaurant

        val address1 = fetchRestaurantAddressById(firstId)
        val address2 = fetchRestaurantAddressById(secondId)

        val distance = if (address1.isNullOrEmpty().not() && address2.isNullOrEmpty().not()) {
            fetchDistance(address1!!, address2!!)?.toDouble()
        } else {
            null
        }
        return if (distance == null) false else (distance * 1000) <= 200.0
    }

    private suspend fun fetchRestaurantAddressById(id: String) =
        firebaseCommon.fetchRestaurantById(id)?.let { "${it.name} ${it.address}" }

    private fun addNewProduct(cartProduct: CartProduct) {
        firebaseCommon.addProductToCart(cartProduct) { addedProduct, e ->
            viewModelScope.launch {
                if (e == null) {
                    _addToCart.emit(Resource.Success(addedProduct!!))
                } else {
                    _addToCart.emit(Resource.Error(e.message.toString()))
                }
            }
        }
    }

    private fun increaseQuantity(documentId: String, cartProduct: CartProduct) {
        firebaseCommon.increaseQuantity(documentId) { _, e ->
            viewModelScope.launch {
                if (e == null) {
                    _addToCart.emit(Resource.Success(cartProduct!!))
                } else {
                    _addToCart.emit(Resource.Error(e.message.toString()))
                }
            }
        }
    }

    val productsPrice = cartProducts.map {
        when (it) {
            is Resource.Success -> {
                calculatePrice(it.data!!)
            }

            else -> null
        }
    }

    private fun calculatePrice(data: List<CartProduct>): Float {
        return data.sumOf { cartProduct ->
            cartProduct.getDiscountedProductPrice() * cartProduct.quantity.toDouble()
        }.toFloat()
    }

    private val _deleteDialog = MutableSharedFlow<CartProduct>()
    val deleteDialog = _deleteDialog.asSharedFlow()

    fun deleteCartProduct(cartProduct: CartProduct) {
        val index = cartProducts.value.data?.indexOf(cartProduct)
        if (index != null && index != -1) {
            val documentId = cartProductDocuments[index].id
            firestore.collection("user").document(auth.uid!!).collection("cart")
                .document(documentId).delete()
        }
    }

    private var cartProductDocuments = emptyList<DocumentSnapshot>()

    init {
        getCartProducts()
    }


    private fun getCartProducts() {
        if (auth.uid == null) return
        viewModelScope.launch { _cartProducts.emit(Resource.Loading()) }
        firestore.collection("user").document(auth.uid!!).collection("cart")
            .addSnapshotListener { value, error ->
                if (error != null || value == null) {
                    viewModelScope.launch { _cartProducts.emit(Resource.Error(error?.message.toString())) }
                } else {
                    cartProductDocuments = value.documents
                    val cartProducts = value.toObjects(CartProduct::class.java)
                    viewModelScope.launch { _cartProducts.emit(Resource.Success(cartProducts)) }
                }
            }
    }


    fun changeQuantity(
        cartProduct: CartProduct,
        quantityChanging: FirebaseCommon.QuantityChanging
    ) {

        val index = cartProducts.value.data?.indexOf(cartProduct)

        if (index != null && index != -1) {
            val documentId = cartProductDocuments[index].id
            when (quantityChanging) {
                FirebaseCommon.QuantityChanging.INCREASE -> {
                    viewModelScope.launch { _cartProducts.emit(Resource.Loading()) }
                    increaseQuantity(documentId)
                }

                FirebaseCommon.QuantityChanging.DECREASE -> {
                    if (cartProduct.quantity == 1) {
                        viewModelScope.launch { _deleteDialog.emit(cartProduct) }
                        return
                    }
                    viewModelScope.launch { _cartProducts.emit(Resource.Loading()) }
                    decreaseQuantity(documentId)
                }
            }
        }
    }

    private fun decreaseQuantity(documentId: String) {
        firebaseCommon.decreaseQuantity(documentId) { result, exception ->
            if (exception != null)
                viewModelScope.launch { _cartProducts.emit(Resource.Error(exception.message.toString())) }
        }
    }

    private fun increaseQuantity(documentId: String) {
        firebaseCommon.increaseQuantity(documentId) { result, exception ->
            if (exception != null)
                viewModelScope.launch { _cartProducts.emit(Resource.Error(exception.message.toString())) }
        }
    }


    // Map distance calculator

    private fun fetchDistance(
        origin: String,
        destination: String,
    ): Int? {
        return try {
            val response = fetchData(origin, destination)
            val distanceMatrixResponse =
                Gson().fromJson(response, DistanceMatrixResponse::class.java)

            distanceMatrixResponse.rows[0].elements[0].distance.value
        } catch (e: Exception) {
            Log.e("Calculate Distance", "Error: ${e.message}")
            null
        }
    }

    private fun fetchData(origin: String, destination: String): String {
        val apiKey = "AIzaSyBsECrIWT9rrXw-OsU6JVgzusuE_6ZiFiI"
        val urlString =
            "https://maps.googleapis.com/maps/api/distancematrix/json?origins=$origin&destinations=$destination&key=$apiKey"
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.connect()

        val reader = BufferedReader(InputStreamReader(conn.inputStream))
        val response = reader.readText()
        reader.close()

        return response
    }

}