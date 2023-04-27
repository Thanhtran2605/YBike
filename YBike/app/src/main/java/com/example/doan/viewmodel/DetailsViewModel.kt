package com.example.doan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doan.data.CartProduct
import com.example.doan.firebase.FirebaseCommon
import com.example.doan.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    /* Xác thực Firebase để có thể lấy Uid của người dùng. */
    private val firestore: FirebaseFirestore,
    val auth: FirebaseAuth,

    private val firebaseCommon: FirebaseCommon
) : ViewModel() {

    private val _addToCart = MutableStateFlow<Resource<CartProduct>>(Resource.Unspecified())
    val addToCart = _addToCart.asStateFlow()

    fun addUpdateProductInCart(cartProduct: CartProduct) {

        viewModelScope.launch {
            _addToCart.emit(Resource.Loading())
        }

        /* Sản phẩm có tồn tại. Nếu mà ''cart'' chưa được tạo thì ta phải đi so sánh.
        * product.id bởi vì trong CartProduct ta gọi cả một Product, nên là lớp lồng nhau, nên
        * chỉ định ra, tham số thứ hai(cartProduct.product.id) là value.
        * */
        firestore.collection("user").document(auth.uid!!).collection("cart").whereEqualTo(
            "product.id",
            cartProduct.product.id
        ).get()
            .addOnSuccessListener {
                /* Nếu trả về documents thì có một sản phẩm vì mỗi sản phẩm có một ID. */
                it.documents.let {
                    /* Nếu đúng là chưa thêm sản phẩm vào giỏ hàng. */
                    if (it.isEmpty()) {
                        addNewProduct(cartProduct)
                    } else {
                        /* Luôn là first() bởi vì chỉ có một sản phẩm.
                        * So sánh product bên dưới với product trong đối số hàm, nếu
                        * trùng nhau thì tăng lên, còn nếu không thì thêm vào giỏ hàng. */
                        val product = it.first().toObject(CartProduct::class.java)

                        if (product == cartProduct) {
                            val documentId = it.first().id
                            increaseQuantity(documentId, cartProduct)

                        } else {
                            addNewProduct(cartProduct)
                        }

                    }
                }
            }.addOnFailureListener {
                viewModelScope.launch {
                    _addToCart.emit(Resource.Error(it.message.toString()))
                }

            }
    }

    /* addedProduct, e is mean exception.
     * We will call this function from firebaseCommon. */
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
        firebaseCommon.increaseQuantity(documentId) { _, exception ->
            viewModelScope.launch {
                if (exception == null) {
                    _addToCart.emit(Resource.Success(cartProduct!!))
                } else {
                    _addToCart.emit(Resource.Error(exception.message.toString()))
                }
            }
        }
    }
}