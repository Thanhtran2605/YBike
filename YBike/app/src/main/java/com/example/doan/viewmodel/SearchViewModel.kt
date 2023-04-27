package com.example.doan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doan.data.Product
import com.example.doan.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SearchViewModel @Inject constructor(
    /* Xác thực Firebase để có thể lấy Uid của người dùng. */
    private val firestore: FirebaseFirestore,
    val auth: FirebaseAuth
) : ViewModel() {

    private val _bestProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val bestProducts: StateFlow<Resource<List<Product>>> = _bestProducts

    private val pagingInfo = MainCategoryViewModel.PagingInfo()

    /**
     * Khởi tạo để lưu giá trị tìm kiếm [product.name]
     * */
    var products: Product = Product()

//    init {
//        fetchBestProducts()
//    }

    //TODO: This is method is using to find and push (nạp) best product
    fun fetchBestProducts() {
        if (!pagingInfo.isPagingEnd) {
            viewModelScope.launch {
                _bestProducts.emit(Resource.Loading())
            }

            var product = firestore.collection("Products")
            product.orderBy("name").startAt(products.name)
                .endAt(products.name + "\uf8ff")

                .limit(pagingInfo.bestProductsPage * 10).get()
                .addOnSuccessListener { result ->
                    val bestProducts = result.toObjects(Product::class.java)
                    pagingInfo.isPagingEnd = bestProducts == pagingInfo.oldBestProducts
                    pagingInfo.oldBestProducts = bestProducts

                    viewModelScope.launch {
                        _bestProducts.emit(Resource.Success(bestProducts))
                    }

                    pagingInfo.bestProductsPage++

                }.addOnFailureListener {
                    viewModelScope.launch {
                        _bestProducts.emit(Resource.Error(it.message.toString()))
                    }
                }

        }

    }

    /*
    * Ký tự \uf8ffđược sử dụng trong truy vấn là một điểm mã rất cao trong phạm vi Unicode (nó là mã Khu vực sử dụng riêng [PUA]).
    * Vì nó đứng sau hầu hết các ký tự thông thường trong Unicode, nên truy vấn khớp với tất cả các giá trị bắt đầu bằng queryText.
    * Bằng cách này, tìm kiếm theo "Fre", tôi có thể nhận được các bản ghi có "Fred, Freddy, Frey"
    * làm giá trị _searchLastName tài sản từ cơ sở dữ liệu.
    * */

    internal data class PagingInfo(
        var bestProductsPage: Long = 1,
        var oldBestProducts: List<Product> = emptyList(),
        var isPagingEnd: Boolean = false
    )

    /* Tạo ra lớp PagingInfo sau đó khởi tạo đối tượng.
    * lấy tên biến khởi tạo đối tượng gọi đến biến bằng 1 * 10 = 10
    * tăng pageInfo.bestProductsPage lên thì page = 2 * 10 == 20 */

    /* Ta so sánh sản phẩm mới với danh sách các sản phẩm cũ.
    *  Nếu sản phẩm cũ bằng sản phẩm mới, có nghĩa là không còn sản phẩm nào để tải.*/
}

