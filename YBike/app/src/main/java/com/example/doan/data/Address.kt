package com.example.doan.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Vì nó di chuyển giữa các Fragment
@Parcelize
data class Address(
    val addressTitle: String,
    val fullName: String,
    val street: String,
    val phone: String,
    val city: String,
    val state: String
) : Parcelable {

    constructor() : this("", "", "", "", "", "")
}