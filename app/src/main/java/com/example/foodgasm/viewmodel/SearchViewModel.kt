package com.example.foodgasm.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodgasm.data.Restaurant
import com.example.foodgasm.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {
    private val TAG: String = SearchViewModel::class.java.simpleName

    private var allRestaurants = emptyList<Restaurant>()
    private val _restaurants = MutableStateFlow<Resource<List<Restaurant>>>(Resource.Initial())
    val restaurants: StateFlow<Resource<List<Restaurant>>> = _restaurants

    init {
        findRestaurants()
    }

    private fun findRestaurants() {
        viewModelScope.launch {
            firestore.collection("Restaurant")
                .limit(50)
                .get()
                .addOnSuccessListener {
                    allRestaurants = it.toObjects(Restaurant::class.java)
                    _restaurants.tryEmit(Resource.Success(allRestaurants))
                }.addOnFailureListener {
                    Log.d(TAG, it.stackTraceToString())
                }
        }
    }

    fun findRestaurants(query: String?) {
        if (query.isNullOrEmpty()) return

        _restaurants.tryEmit(Resource.Loading())
        val filteredList = allRestaurants.filter { it.name.contains(query, true)}
        _restaurants.tryEmit(Resource.Success(filteredList))
    }
}