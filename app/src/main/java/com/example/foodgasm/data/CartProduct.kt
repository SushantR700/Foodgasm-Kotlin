package com.example.foodgasm.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CartProduct(
    var id: String,
    var restaurantId: String,
    var name: String,
    var price: Float,
    val quantity: Int,
    var image: String,
    var offerPercentage: Float = 0.0f
) : Parcelable {
    constructor() : this("-1", "-1", "-", 0.0F, 0, "")

    fun getDiscountedProductPrice(): Float {
        val remainingPricePercentage = 1f - this.offerPercentage
        return remainingPricePercentage * price
    }
}
