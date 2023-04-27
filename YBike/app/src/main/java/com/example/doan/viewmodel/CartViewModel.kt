package com.example.doan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doan.data.CartProduct
import com.example.doan.firebase.FirebaseCommon
import com.example.doan.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import helper.getProductPrice
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,

    /* Cần các hàm tăng giảm số lượng */
    private val firebaseCommon: FirebaseCommon
) : ViewModel() {

    private val _cartProducts =
        MutableStateFlow<Resource<List<CartProduct>>>(Resource.Unspecified())

    /* Tạo trạng thái bằng cách tạo trạng thái công khai. */
    val cartProducts = _cartProducts.asStateFlow()

    /* Để lưu giá trị vào document khi dùng hàm [getCartProducts] */
    private var cartProductDocuments = emptyList<DocumentSnapshot>()

    /* Tính số tiền trong giỏ hàng */
    val productsPrice = cartProducts.map {
        when (it) {
            is Resource.Success -> {
                calculatePrice(it.data!!)
            }

            else -> null
        }
    }

    /* Tạo luồng hiển thị hộp thoại nếu giảm sản phẩm về 0. Yêu cầu người dùng có
     * muốn xóa sản phẩm ra khỏi giỏ hàng hay không. */
    private val _deleteDialog = MutableSharedFlow<CartProduct>()
    val deleteDialog = _deleteDialog.asSharedFlow()


    //TODO: this method is using to calculate price
    private fun calculatePrice(data: List<CartProduct>): Float {
        return data.sumByDouble { cartProduct ->
            (cartProduct.product.offerPercentage.getProductPrice(cartProduct.product.price) * cartProduct.quantity).toDouble()
        }.toFloat()
    }

    init {
        getCartProducts()
    }

    //TODO: this method is using to get all cart products !
    private fun getCartProducts() {
        viewModelScope.launch { _cartProducts.emit(Resource.Loading()) }

        /* addSnapshotListener : giống như callback được thực thi bất cứ khi nào các thẻ trong
         * collection thay đổi. Dùng để làm mới UI khi mà người dùng thêm một sản phẩm.
         * Quy trình: khi thêm vào nó sẽ lấy và dùng dòng launch sau để cập nhật, sau đó qua bên
         * ShoppingActivity dùng lifecycler nếu mà Success thì hiển thị số lượng sản phẩm
         * có trong giỏ hàng. */
        firestore.collection("user").document(auth.uid!!).collection("cart")

            .addSnapshotListener { value, error ->
                if (error != null || value == null) {
                    viewModelScope.launch { _cartProducts.emit(Resource.Error(error?.message.toString())) }
                } else {
                    cartProductDocuments = value.documents
                    /* Chuyển đổi đối tượng đó thành sản phẩm giỏ hàng. */
                    val cartProducts = value.toObjects(CartProduct::class.java)
                    viewModelScope.launch { _cartProducts.emit(Resource.Success(cartProducts)) }
                }
            }
    }

    //TODO: this method is using to change quantity
    fun changeQuantity(
        cartProduct: CartProduct,
        quantityChanging: FirebaseCommon.QuantityChanging
    ) {

        /* Sử dụng chỉ mục để lấy document từ cartProductDocuments danh sách, sau đó
         * ta có thể lấy được document id. */
        val index = cartProducts.value.data?.indexOf(cartProduct)

        /* Nếu index = -1 thì khi người dùng spam tăng giảm số lượng, hàm getCartProducts sẽ bị
         * delay, điều này sẽ làm trễ kết quả mà ta mong đợi ở bên trong [_cartProducts] và để ngăn
         * cho ứng dụng không lỗi, ta cần phải kiểm tra. */
        if (index != null && index != -1) {
            val documentId = cartProductDocuments[index].id

            when (quantityChanging) {
                FirebaseCommon.QuantityChanging.INCREASE -> {
                    /* Hiển thị thanh tiến trình khi tăng số lượng. */
                    viewModelScope.launch { _cartProducts.emit(Resource.Loading()) }
                    increaseQuantity(documentId)
                }

                FirebaseCommon.QuantityChanging.DECREASE -> {
                    /* Nếu sản phẩm bằng 1 mà nhấn nút DECREASE thì hiển thị dialog */
                    if (cartProduct.quantity == 1) {
                        viewModelScope.launch { _deleteDialog.emit(cartProduct) }
                        return
                    }
                    viewModelScope.launch { _cartProducts.emit(Resource.Loading()) }
                    decreaseQuantity(documentId)
                }
            }
        }
    }

    // TODO: this method is using to delete cart product
    fun deleteCartProduct(cartProduct: CartProduct) {
        /* Lấy chỉ mục của sản phẩm, lấy id của document, truy suất tìm trong collection tập hợp
         * firestore */
        val index = cartProducts.value.data?.indexOf(cartProduct)
        if (index != null && index != -1) {
            val documentId = cartProductDocuments[index].id
            firestore.collection("user").document(auth.uid!!).collection("cart")
                .document(documentId).delete()
        }
    }

    // TODO: this method is using to decrease quantity
    private fun decreaseQuantity(documentId: String) {
        /* Sẽ không cần phải phát ra trạng thái thành công, vì [function getCartProducts]
         * chỉ được thực thi khi có một sản phẩm mới được thêm vào. */
        firebaseCommon.decreaseQuantity(documentId) { result, exception ->
            if (exception != null)
                viewModelScope.launch { _cartProducts.emit(Resource.Error(exception.message.toString())) }
        }
    }

    //TODO: this method is using to increase quantity
    private fun increaseQuantity(documentId: String) {
        firebaseCommon.increaseQuantity(documentId) { result, exception ->
            if (exception != null)
                viewModelScope.launch { _cartProducts.emit(Resource.Error(exception.message.toString())) }
        }
    }

}
