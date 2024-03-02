package com.example.foodgasm.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Food(
    var id: String,
    var name: String,
    var price: Float,
    var desc: String,
    var image: String,
    var offerPercentage: Float? = null
) : Parcelable {
    constructor() : this(
        id = "",
        name = "",
        desc = "",
        price = 0f,
        image = ""
    )
}
