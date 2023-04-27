package com.example.doan.fragments.shopping

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doan.R
import com.example.doan.adapters.AddressAdapter
import com.example.doan.adapters.BillingProductAdapter
import com.example.doan.data.Address
import com.example.doan.data.CartProduct
import com.example.doan.data.Order
import com.example.doan.data.OrderStatus
import com.example.doan.databinding.FragmentBillingBinding
import com.example.doan.util.HorizontalItemDecoration
import com.example.doan.util.Resource
import com.example.doan.viewmodel.BillingViewModel
import com.example.doan.viewmodel.OrderViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.text.NumberFormat
import java.util.*

@AndroidEntryPoint
class BillingFragment : Fragment() {
    private lateinit var binding: FragmentBillingBinding

    /* Khai báo hai Adapter cần sử dụng */
    private val addressAdapter by lazy { AddressAdapter() }
    private val billingProductAdapter by lazy { BillingProductAdapter() }

    /* Khai báo viewmodel */
    private val billingViewModels by viewModels<BillingViewModel>()

    /* Nhận các sản phẩm từ CartFragment để điều hướng */
    private val args by navArgs<BillingFragmentArgs>()
    private var products = emptyList<CartProduct>()
    private var totalPrice = 0f

    /* Lấy địa chỉ đã chọn */
    private var selectedAddress: Address? = null

    /* Khai báo Order View Model */
    private val orderViewModel by viewModels<OrderViewModel>()

    /* Lấy giá trị và gán vào danh sách sản phẩm và tổng tiền */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        products = args.products.toList()
        totalPrice = args.totalPrice
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBillingBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBillingProductRv()
        setupAddressRv()

        /**
         * Khi nhấp chuột vào image cộng sẽ hiển thị địa chỉ fragment.xml để thêm vào
         * */
        binding.imageAddAddress.setOnClickListener {
            findNavController().navigate(R.id.action_billingFragment_to_addressFragment)
        }

        /**
         * Đây là gọi viewModel để xem hiển thị địa chỉ
         * */
        lifecycleScope.launchWhenStarted {
            billingViewModels.address.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.progressbarAddress.visibility = View.VISIBLE
                    }

                    is Resource.Success -> {
                        binding.progressbarAddress.visibility = View.GONE

                        /* Hiển thị địa chỉ */
                        addressAdapter.differ.submitList(it.data)
                    }

                    is Resource.Error -> {
                        binding.progressbarAddress.visibility = View.VISIBLE
                        Toast.makeText(requireContext(), "Lỗi ${it.message}", Toast.LENGTH_SHORT)
                            .show()
                    }

                    else -> Unit

                }
            }
        }

        /**
         * Đây là viewModel để hoạt ảnh button khi order
         * */
        lifecycleScope.launchWhenStarted {
            orderViewModel.order.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.buttonPlaceOrder.startAnimation()
                    }
                    is Resource.Success -> {
                        binding.buttonPlaceOrder.revertAnimation()
                        findNavController().navigateUp()
                        Snackbar.make(
                            requireView(),
                            "Bạn đã đặt hàng thành công",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    is Resource.Error -> {
                        binding.buttonPlaceOrder.revertAnimation()
                        Toast.makeText(requireContext(), "Lỗi ${it.message}", Toast.LENGTH_SHORT)
                            .show()
                    }
                    else -> Unit
                }
            }
        }

        /**
         * Gán giá trị danh sách sản phẩm và tổng tiền cho Adapter để hiển thị lên recycler view
         * */
        billingProductAdapter.differ.submitList(products)

        // Định dạng giá tiền theo Việt Nam
        val COUNTRY = "VN"
        val LANGUAGE = "vi"
        val totalPrice: String =
            NumberFormat.getCurrencyInstance(Locale(LANGUAGE, COUNTRY))
                .format(totalPrice)

        binding.tvTotalPrice.text = "$totalPrice"

        /**
         * Lấy địa chỉ đã chọn
         * */
        addressAdapter.onClick = {
            selectedAddress = it
        }

        /**
         * Khi người dùng click vào đặt hàng [placeOrder]
         * */
        binding.buttonPlaceOrder.setOnClickListener {
            if (selectedAddress == null) {
                Toast.makeText(requireContext(), "Vui lòng chọn địa chỉ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            showOrderConfirmationDialog()
        }

        /** Sử dụng khi click vào image close sẽ đóng và thoát ra. */
        binding.imageCloseBilling.setOnClickListener {
            findNavController().navigateUp()
        }

    }

    //TODO: this method is using to show when we click place order
    private fun showOrderConfirmationDialog() {
        val alertDialog = AlertDialog.Builder(requireContext()).apply {
            setTitle("Đặt hàng")
            setMessage("Bạn có muốn đặt mặt hàng này ?")
            setNegativeButton("Hủy") { dialog, _ ->
                dialog.dismiss()
            }
            setPositiveButton("Đồng ý") { dialog, _ ->
                val order = Order(
                    OrderStatus.Ordered.status,
                    totalPrice,
                    products,
                    selectedAddress!!
                )
                orderViewModel.placeOrder(order)
                dialog.dismiss()
            }
        }
        alertDialog.create()
        alertDialog.show()
    }


    //TODO: this method is using to adapter address for recycler view
    private fun setupAddressRv() {
        binding.rvAddress.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = addressAdapter

            addItemDecoration(HorizontalItemDecoration())
        }
    }

    //TODO: this method is using to adapter billing for recycler view
    private fun setupBillingProductRv() {
        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = billingProductAdapter

            addItemDecoration(HorizontalItemDecoration())
        }
    }


}