package com.example.foodgasm.order

import android.os.Parcelable
import com.example.foodgasm.data.Address
import com.example.foodgasm.data.CartProduct
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*

@Parcelize
data class Order(
    val orderStatus: String = "",
    val orderBy: String = "",
    val totalPrice: Float = 0f,
    val products: List<CartProduct> = emptyList(),
    val address: Address = Address(),
    val date: String = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date()),
    val orderId: Long = System.currentTimeMillis(),
    val paid: Boolean = false,
    val txnId: String = "",
    val txnToken: String = ""
): Parcelable