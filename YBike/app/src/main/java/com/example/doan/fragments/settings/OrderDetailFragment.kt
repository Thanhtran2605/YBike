package com.example.doan.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doan.adapters.BillingProductAdapter
import com.example.doan.data.Order
import com.example.doan.data.OrderStatus
import com.example.doan.data.getOrderStatus
import com.example.doan.databinding.FragmentOrderDetailBinding
import com.example.doan.util.VerticalItemDecoration
import java.text.NumberFormat
import java.util.*

class OrderDetailFragment : Fragment() {
    private lateinit var binding: FragmentOrderDetailBinding

    // Gọi billing product adapter để sử dụng
    private val billingProductsAdapter by lazy { BillingProductAdapter() }

    private val args by navArgs<OrderDetailFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrderDetailBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupOrderRv()

        val order = args.order

        binding.apply {
            tvOrderId.text = "Đặt hàng #${order.orderId}"

            stepView.setSteps(
                mutableListOf(
                    OrderStatus.Ordered.status,
                    OrderStatus.Confirmed.status,
                    OrderStatus.Shipped.status,
                    OrderStatus.Delivered.status
                )
            )

            // Xác định xem trạng thái nào.
            val currentOrderState = when (getOrderStatus(order.orderStatus)) {
                is OrderStatus.Ordered -> 0
                is OrderStatus.Confirmed -> 1
                is OrderStatus.Shipped -> 2
                is OrderStatus.Delivered -> 3
                else -> 0
            }

            // Để biết đang ở trạng thái nào
            stepView.go(currentOrderState, false)

            // Nếu trạng thái bằng ba tức là đang ở vị trí cuối
            if (currentOrderState == 3) {
                stepView.done(true)
            }

            // Hiển thị địa chỉ người dùng
            tvFullName.text = order.address.fullName
            tvAddress.text = "${order.address.street} ${order.address.city}"
            tvPhoneNumber.text = order.address.phone

            // Định dạng giá tiền theo Việt Nam
            val COUNTRY = "VN"
            val LANGUAGE = "vi"
            val totalPrice: String =
                NumberFormat.getCurrencyInstance(Locale(LANGUAGE, COUNTRY))
                    .format(order.totalPrice)
            tvTotalPrice.text = "$totalPrice"

        }


        // Cập nhật sản phẩm trong danh sách
        billingProductsAdapter.differ.submitList(order.products)

        /** Sử dụng khi click vào image close sẽ đóng và thoát ra. */
        binding.imageCloseOrder.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    //TODO: this method is using to set up order adapter with recycler view
    private fun setupOrderRv() {
        binding.rvProducts.apply {
            adapter = billingProductsAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            addItemDecoration(VerticalItemDecoration())
        }
    }


}