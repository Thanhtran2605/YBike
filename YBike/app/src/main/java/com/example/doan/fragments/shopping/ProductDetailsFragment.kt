package com.example.doan.fragments.shopping

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
import com.example.doan.R
import com.example.doan.adapters.ViewPagerTwoImages
import com.example.doan.data.CartProduct
import com.example.doan.databinding.FragmentProductDetailsBinding
import com.example.doan.util.Resource
import com.example.doan.util.hideBottomNavigationView
import com.example.doan.viewmodel.DetailsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.text.NumberFormat
import java.util.*

@AndroidEntryPoint
class ProductDetailsFragment : Fragment() {
    /* Sử dụng để lấy sản phẩm */
    private val args by navArgs<ProductDetailsFragmentArgs>()
    private lateinit var binding: FragmentProductDetailsBinding
    private val viewPagerAdapter by lazy { ViewPagerTwoImages() }

    /* Khởi tạo viewModel */
    private val viewModel by viewModels<DetailsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        /* This is to hideBottomNavigationView
         * from package util (ShowHideBottomNavigation)
         * when we see the product description.
         */
        hideBottomNavigationView()

        binding = FragmentProductDetailsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* Chúng ta sẽ nhận được một sản phẩm ở đây vì đã đặt tên cho nó
        * là sản phẩm bên trong navigation graph. */
        val product = args.product

        /* Sử dụng để cài đặt Recycler View */
        setupViewPager()

        /* Sử dụng khi click vào image close sẽ đóng và thoát ra mô tả sản phẩm. */
        binding.imageClose.setOnClickListener {
            findNavController().navigateUp()
        }

        /* Gọi bắt sự kiện khi click vào button thêm vào giỏ hàng. */
        binding.btnAddToCart.setOnClickListener {
            viewModel.addUpdateProductInCart(CartProduct(product, 1))
        }

        /* Sử dụng lifecycleScope */
        lifecycleScope.launchWhenStarted {
            viewModel.addToCart.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.btnAddToCart.startAnimation()
                    }

                    is Resource.Success -> {
                        binding.btnAddToCart.revertAnimation()
                        binding.btnAddToCart.setBackgroundColor(resources.getColor(R.color.black))
                    }

                    is Resource.Error -> {
                        binding.btnAddToCart.stopAnimation()
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }

                    else -> Unit
                }
            }
        }

        /* Hiển thị thông tin sản phẩm vào Adapter */
        binding.apply {
            tvProductName.text = product.name

            // Hiển thị tiền là số tiền đã giảm giá
            product.offerPercentage?.let {
                val remainingPricePercentage = 1 - it
                val priceAfterOffer = remainingPricePercentage * product.price


                // Định dạng giá tiền theo Việt Nam
                val COUNTRY = "VN"
                val LANGUAGE = "vi"
                val newPrice: String =
                    NumberFormat.getCurrencyInstance(Locale(LANGUAGE, COUNTRY))
                        .format(priceAfterOffer)
                tvProductPrice.text = "$newPrice"
            }

                tvProductDescription.text = product.description

        }

        /* Dùng để Cập nhật Adapter */
        viewPagerAdapter.differ.submitList(product.images)

    }

    //TODO: this method is using to set up [View_Pager]
    private fun setupViewPager() {
        binding.apply {
            viewPagerProductImages.adapter = viewPagerAdapter
        }
    }

}
