package com.example.doan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doan.data.Category
import com.example.doan.data.Product
import com.example.doan.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoryViewModel constructor(
    private val firestore: FirebaseFirestore,

    /* We don't need use dagger hold, we can't pass argument with dagger hilt
    *, We actually want to create our own Factory for this class.*/
    private val category: Category
) : ViewModel() {

    private val _offerProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val offerProducts = _offerProducts.asStateFlow()

    private val _bestProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val bestProducts = _offerProducts.asStateFlow()

    init {
        fetchOfferProducts()
        fetchBestProducts()
    }

    /* Tìm nạp sản phẩm offer */
    fun fetchOfferProducts() {

        /* Tải trước khi thực thi bắt đầu function. */
        viewModelScope.launch {
            _offerProducts.emit(Resource.Loading())
        }

        /* chỉ nhận các sản phẩm có tỉ lệ phần trăm từ category. */
        firestore.collection("Products").whereEqualTo("category", category.category)
            .whereNotEqualTo("offerPercentage", null).get()
            .addOnSuccessListener {
                /* Ánh xạ hay thêm các sản phẩm vào đối tượng. */
                val products = it.toObjects(Product::class.java)

                viewModelScope.launch {
                    _offerProducts.emit(Resource.Success(products))
                }
            }.addOnFailureListener {
                viewModelScope.launch {
                    _offerProducts.emit(Resource.Error(it.message.toString()))
                }
            }
    }

    fun fetchBestProducts() {

        /* Tải trước khi thực thi bắt đầu function. */
        viewModelScope.launch {
            _bestProducts.emit(Resource.Loading())
        }

        /* chỉ nhận các sản phẩm có tỉ lệ phần trăm từ category. */
        firestore.collection("Products").whereEqualTo("category", category.category)
            .whereNotEqualTo("offerPercentage", null).get()
            .addOnSuccessListener {
                /* Ánh xạ hay thêm các sản phẩm vào đối tượng. */
                val products = it.toObjects(Product::class.java)

                viewModelScope.launch {
                    _bestProducts.emit(Resource.Success(products))
                }
            }.addOnFailureListener {
                viewModelScope.launch {
                    _bestProducts.emit(Resource.Error(it.message.toString()))
                }
            }
    }
}