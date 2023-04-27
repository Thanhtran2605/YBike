package com.example.doan.fragments.shopping

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doan.R
import com.example.doan.adapters.CartProductAdapter
import com.example.doan.databinding.FragmentCartBinding
import com.example.doan.firebase.FirebaseCommon
import com.example.doan.util.Resource
import com.example.doan.util.VerticalItemDecoration
import com.example.doan.viewmodel.CartViewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.NumberFormat
import java.util.*

class CartFragment : Fragment(R.layout.fragment_cart) {
    private lateinit var binding: FragmentCartBinding
    private val cartAdapter by lazy { CartProductAdapter() }

    // val viewModel by viewModels<CartViewModel>() đã sử dụng bên Sho
    // ppingActivity nếu tạo lại
    // thì sẽ khởi tạo đối tượng mới,ta sẽ kích hoạt chức năng lấy sản phẩm hai lần. Ta chỉ muốn kích hoạt
    // nó một lần nên sử dụng.
    private val viewModel by activityViewModels<CartViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCartBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCartRv()

        /**
         * Dưới đây để thoát khi nhấn vào x
         *
         * */
        binding.imageCloseCart.setOnClickListener {
            findNavController().navigateUp()
        }

        /**
         * Dưới đây là gọi tính toán sản phẩm
         * */
        var totalPrice = 0f
        lifecycleScope.launchWhenStarted {
            viewModel.productsPrice.collectLatest { price ->
                price?.let {
                    // Định dạng giá tiền theo Việt Nam
                    val COUNTRY = "VN"
                    val LANGUAGE = "vi"
                    val sumPrice: String =
                        NumberFormat.getCurrencyInstance(Locale(LANGUAGE, COUNTRY))
                            .format(price)

                    totalPrice = it

                    binding.tvTotalPrice.text = "$sumPrice"
                }
            }
        }

        /**
         * Dưới đây là click vào button để chuyển sang thanh toán billing và có thể gửi product sang và giá tiền
         * bằng cách tạo biến var [totalPrice] = 0f và [totalPrice] = it
         * */
        binding.buttonCheckout.setOnClickListener {
            var action = CartFragmentDirections.actionCartFragmentToBillingFragment(
                totalPrice,
                cartAdapter.differ.currentList.toTypedArray()
            )
            findNavController().navigate(action)
        }

        /**
         * Dưới đây là điều hướng sản phẩm ở trong giỏ hàng quay về trang chi tiết sản phẩm
         * id trong shopping_graph navigation.
         * */
        cartAdapter.onProductClick = {
            val b = Bundle().apply {
                putParcelable("product", it.product)
            }
            findNavController().navigate(R.id.action_cartFragment_to_productDetailsFragment, b)
        }

        /**
         * Dưới đây là khi chọn tăng sản phẩm
         * */
        cartAdapter.onPlusClick = {
            viewModel.changeQuantity(it, FirebaseCommon.QuantityChanging.INCREASE)
        }

        /**
         * Dưới đây là khi chọn giảm sản phẩm
         * */
        cartAdapter.onMinusClick = {
            viewModel.changeQuantity(it, FirebaseCommon.QuantityChanging.DECREASE)
        }


        /**
         * Dưới đây là gọi hộp thoại xóa sản phẩm nếu giảm sản phẩm về
         * 0.
         * */
        lifecycleScope.launchWhenStarted {
            viewModel.deleteDialog.collectLatest {
                val alertDialog = AlertDialog.Builder(requireContext()).apply {
                    setTitle("Xóa sản phẩm ra khỏi giỏ hàng")
                    setMessage("Bạn có muốn xóa sản phẩm này ra khỏi giỏ hàng ?")
                    setNegativeButton("Hủy") { dialog, _ ->
                        dialog.dismiss()
                    }
                    setPositiveButton("Đồng ý") { dialog, _ ->
                        viewModel.deleteCartProduct(it)
                        dialog.dismiss()
                    }
                }
                alertDialog.create()
                alertDialog.show()
            }
        }

        /**
         * Dưới đây là khi hiển thị giỏ hàng có sản phẩm hoặc không có sản phẩm
         * */
        lifecycleScope.launchWhenStarted {
            viewModel.cartProducts.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.progressbarCart.visibility = View.VISIBLE
                    }

                    is Resource.Success -> {
                        binding.progressbarCart.visibility = View.INVISIBLE

                        /* Nếu như không có sản phẩm thì hiển thị hộp trống.
                         * Nếu như có sản phẩm thì hiển thị dữ liệu. */
                        if (it.data!!.isEmpty()) {
                            showEmptyCart()

                            /* Phải tạo function để ẩn recyclerView. */
                            hideOtherViews()

                        } else {
                            hideEmptyCart()
                            showOtherViews()

                            /* Mặc định sẽ hiển thị các sản phẩm ở trong giỏ hàng,
                            vì đã gán dữ liệu ở CartProductAdapter.
                            Dòng phía dưới để thêm sản phẩm vào Adapter */
                            cartAdapter.differ.submitList(it.data)
                        }
                    }

                    is Resource.Error -> {
                        binding.progressbarCart.visibility = View.INVISIBLE
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }

                    else -> Unit
                }
            }
        }
    }

    // TODO: this method is using to show other views
    private fun showOtherViews() {
        binding.apply {
            rvCart.visibility = View.VISIBLE
            totalBoxContainer.visibility = View.VISIBLE
            buttonCheckout.visibility = View.VISIBLE
        }
    }

    // TODO: this method is using to hide other views
    private fun hideOtherViews() {
        binding.apply {
            rvCart.visibility = View.GONE
            totalBoxContainer.visibility = View.GONE
            buttonCheckout.visibility = View.GONE
        }
    }

    // TODO: this method is using to hide empty cart
    private fun hideEmptyCart() {
        binding.apply {
            layoutCartEmpty.visibility = View.GONE
        }
    }

    //TODO: this method is using to show empty cart
    private fun showEmptyCart() {
        binding.apply {
            layoutCartEmpty.visibility = View.VISIBLE
        }
    }

    //TODO: this method is using to set up recycler view
    private fun setupCartRv() {
        binding.rvCart.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = cartAdapter
            // margin bottom between item.
            addItemDecoration(VerticalItemDecoration())
        }
    }
}

