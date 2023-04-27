package com.example.doan.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.doan.R
import com.example.doan.databinding.ActivityShoppingBinding
import com.example.doan.util.Resource
import com.example.doan.viewmodel.CartViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ShoppingActivity : AppCompatActivity() {

    val binding by lazy {
        ActivityShoppingBinding.inflate(layoutInflater)
    }

    val viewModel by viewModels<CartViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        /* Tìm kiếm id của phần trên của nav, của activity_shopping.xml */
        val navController = findNavController(R.id.shoppingHostFragment)

        /* Cài đặt bottom navigation với phần trên nav */
        binding.bottomNavigation.setupWithNavController(navController)

        /* Hiển thị số lượng sản phẩm có ở trong giỏ hàng */
        lifecycleScope.launchWhenStarted {
            viewModel.cartProducts.collectLatest {
                when (it) {
                    is Resource.Success -> {
                        val count = it.data?.size ?: 0
                        val bottomNavigation =
                            findViewById<BottomNavigationView>(R.id.bottomNavigation)
                        /* Đây là id của từng item trong menu [R.id.cartFragment] */
                        bottomNavigation.getOrCreateBadge(R.id.cartFragment).apply {
                            number = count
                            backgroundColor = resources.getColor(R.color.g_button)
                        }
                    }
                    else -> Unit
                }
            }
        }
    }

}