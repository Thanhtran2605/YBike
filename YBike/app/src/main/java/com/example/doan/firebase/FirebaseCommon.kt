package com.example.doan.firebase

import com.example.doan.data.CartProduct
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class FirebaseCommon(
    /* ĐÂY LÀ LỚP TẠO RIÊNG CÁC HÀM THÊM VÀ TĂNG VÀO GIỎ HÀNG. */
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    private val cartCollection =
        firestore.collection("user").document(auth.uid!!).collection("cart")

    // TODO: this method is using to addProductToCart()
    fun addProductToCart(cartProduct: CartProduct, onResult: (CartProduct?, Exception?) -> Unit) {
        cartCollection.document().set(cartProduct)
            .addOnSuccessListener {
                /* Thành công trả về cartProduct và null exception. */
                onResult(cartProduct, null)
            }.addOnFailureListener {
                onResult(null, it)
            }


    }

    // TODO: this method is using to increaseQuantity()
    fun increaseQuantity(documentId: String, onResult: (String?, Exception?) -> Unit) {
        /* firestore.runBatch() : sử dụng để chỉ đọc.
         * firestore.runTransaction() : sử dụng để đọc và ghi cùng một lúc.
         * Đọc trước sau đó cập nhật số lượng. */

        firestore.runTransaction { transaction ->
            val documentRef = cartCollection.document(documentId)
            val document = transaction.get(documentRef)
            val productObject = document.toObject(CartProduct::class.java)

            productObject?.let { cartProduct ->
                val newQuantity = cartProduct.quantity + 1

                /* Sao chép đối tượng và có thể chỉ thay đổi một đối số. */
                val newProductObject = cartProduct.copy(quantity = newQuantity)
                transaction.set(documentRef, newProductObject)
            }


        }.addOnSuccessListener {
            onResult(documentId, null)
        }.addOnFailureListener {
            onResult(null, it)
        }

    }

    // TODO: this method is using to decreaseQuantity()
    fun decreaseQuantity(documentId: String, onResult: (String?, Exception?) -> Unit) {
        firestore.runTransaction { transaction ->
            val documentRef = cartCollection.document(documentId)
            val document = transaction.get(documentRef)
            val productObject = document.toObject(CartProduct::class.java)

            productObject?.let { cartProduct ->
                val newQuantity = cartProduct.quantity - 1

                val newProductObject = cartProduct.copy(quantity = newQuantity)
                transaction.set(documentRef, newProductObject)
            }
        }.addOnSuccessListener {
            onResult(documentId, null)
        }.addOnFailureListener {
            onResult(null, it)
        }

    }

    /* Lớp sẽ phân biệt giữa lựa chọn tăng hoặc giảm số lượng. */
    enum class QuantityChanging {
        INCREASE, DECREASE
    }
}

