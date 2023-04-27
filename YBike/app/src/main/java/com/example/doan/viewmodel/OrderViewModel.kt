package com.example.doan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doan.data.Order
import com.example.doan.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/* Lớp này sẽ tạo một đơn đặt hàng. */
@HiltViewModel
class OrderViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _order = MutableStateFlow<Resource<Order>>(Resource.Unspecified())
    val order = _order.asStateFlow()

    //TODO: this method is using to create a order
    fun placeOrder(order: Order) {
        viewModelScope.launch {
            _order.emit(Resource.Loading())
        }

        /* Khi người dùng click thì thêm bộ sưu tập đơn hàng cho khách hàng, sau đó
         * tạo một bộ sưu tập chi tiết trong đơn hàng đó. THÊM ĐƠN HÀNG VÀO BỘ SƯU TẬP
         * ĐƠN HÀNG CHUNG. Ta sẽ không dùng Transaction() vì chỉ đọc. Mục đích của ta là
         * GHI runBatch() */

        firestore.runBatch {
            //TODO: Add the order into user-orders collection
            firestore.collection("user")
                .document(auth.uid!!)
                .collection("orders")
                .document()
                .set(order)

            //TODO: Add the order into orders collection
            firestore.collection("orders").document().set(order)

            //TODO: Delete the products from user-cart collection
            /* Chúng ta không thể xóa tất cả, mà phải dùng vòng lặp xóa từng item. */
            firestore.collection("user").document(auth.uid!!).collection("cart").get()
                .addOnSuccessListener {
                    it.documents.forEach {
                        it.reference.delete()
                    }
                }
        }.addOnSuccessListener {
            viewModelScope.launch {
                _order.emit(Resource.Success(order))
            }
        }.addOnFailureListener {
            viewModelScope.launch {
                _order.emit(Resource.Error(it.message.toString()))
            }
        }

    }

}