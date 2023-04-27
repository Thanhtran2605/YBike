package com.example.doan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doan.data.Product
import com.example.doan.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainCategoryViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _specialProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())

    /* Tầng trạng thái này không thể thay đổi được */
    val specialProduct: StateFlow<Resource<List<Product>>> = _specialProducts

    private val _bestDealProducts =
        MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val bestDealProducts: StateFlow<Resource<List<Product>>> = _bestDealProducts

    private val _bestProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val bestProducts: StateFlow<Resource<List<Product>>> = _bestProducts

    private val pagingInfo = PagingInfo()

    /* Dùng init bởi vì ta luôn muốn tìm nạp sản phẩm bên trong danh mục chính. */
    init {
        fetchSpecialProducts()
        fetchBestDeals()
        fetchBestProducts()
    }

    //TODO:  This is method is using to find and push (nạp) special product
    fun fetchSpecialProducts() {

        viewModelScope.launch {
            _specialProducts.emit(Resource.Loading())
        }

        /* Lọc sản phẩm */
        firestore.collection("Products").whereEqualTo("category", "Special Products")
            .get().addOnSuccessListener { result ->
                /* Chuyển đổi result thành object */
                val specialProductsList = result.toObjects(Product::class.java)

                viewModelScope.launch {
                    _specialProducts.emit(Resource.Success(specialProductsList))
                }

            }.addOnFailureListener {
                viewModelScope.launch {
                    _specialProducts.emit(Resource.Error(it.message.toString()))
                }
            }
    }

    //TODO: This is method is using to find and push (nạp) best deal
    fun fetchBestDeals() {
        viewModelScope.launch {
            _specialProducts.emit(Resource.Loading())
        }

        firestore.collection("Products").whereEqualTo("category", "Best Deals") 
            .get().addOnSuccessListener { result ->
                val bestDealsProducts = result.toObjects(Product::class.java)

                viewModelScope.launch {
                    _bestDealProducts.emit(Resource.Success(bestDealsProducts))
                }

            }.addOnFailureListener {
                viewModelScope.launch {
                    _bestDealProducts.emit(Resource.Error(it.message.toString()))
                }
            }
    }

    //TODO: This is method is using to find and push (nạp) best product
    fun fetchBestProducts() {
        if (!pagingInfo.isPagingEnd) {
            viewModelScope.launch {
                _bestProducts.emit(Resource.Loading())
            }

            firestore.collection("Products").limit(pagingInfo.bestProductsPage * 10).get()
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