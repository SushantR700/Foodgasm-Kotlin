package com.example.foodgasm.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Restaurant(
    var id: String,
    var category: String,
    var name: String,
    var address: String,
    var duration: String,
    var discount: String,
    var price: Float,
    var desc:String,
    var image: String,
    var offerPercentage : Float? = null
) : Parcelable {
    constructor() : this(
        id = "",
        category = "",
        name = "",
        address = "",
        duration = "",
        desc = "",
        discount = "",
        price = 0f,
        image = ""
    )
}
