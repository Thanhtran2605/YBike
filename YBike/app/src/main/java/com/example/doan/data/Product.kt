package com.example.doan.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Cá nhân hóa
@Parcelize
data class Product(
    val id: String,
    var name: String,
    val category: String,
    val price: Float,
    val offerPercentage: Float? = null,
    val description: String? = null,
//    val colors: List<Int>? = null,
//    val sizes: List<String>? = null,
    val images: List<String>
) : Parcelable {
    constructor() : this("0", "", "", 0f, images = emptyList())
}
