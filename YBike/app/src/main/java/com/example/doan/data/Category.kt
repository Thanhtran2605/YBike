package com.example.doan.data

sealed class Category(val category: String) {

    object Wave: Category("Wave")
    object AirBlade: Category("Air Blade")
    object Lead: Category("Lead")
    object SH: Category("SH")
    object Vision: Category("Vision")
}

