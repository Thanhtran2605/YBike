package com.example.doan.fragments.categories

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doan.R
import com.example.doan.adapters.BestDealAdapter
import com.example.doan.adapters.BestProductAdapter
import com.example.doan.adapters.SpecialProductAdapter
import com.example.doan.databinding.FragmentMainCategoryBinding
import com.example.doan.util.Resource
import com.example.doan.util.showBottomNavigationView
import com.example.doan.viewmodel.MainCategoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

private val TAG = "MainCategoryFragment"

@AndroidEntryPoint
class MainCategoryFragment : Fragment(R.layout.fragment_main_category) {
    private lateinit var binding: FragmentMainCategoryBinding

    /* Khai báo bộ điều hợp */
    private lateinit var specialProductAdapter: SpecialProductAdapter
    private lateinit var bestDealAdapter: BestDealAdapter
    private lateinit var bestProductAdapter: BestProductAdapter

    /* Khai báo View Model */
    private val viewModel by viewModels<MainCategoryViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainCategoryBinding.inflate(inflater)
        return binding.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* Function set up recycler view for special product is here. */
        setupSpecialProductRv()

        /* Function set up recycler view for best deal is here */
        setupBestDealRv()

        /* Function set up recycler view for best deal is here */
        setupBestProductRv()

        /* Dùng để lấy dữ liệu và điều hướng từ homeFragment qua productDetailsFragment. */
        specialProductAdapter.onClick = {
            /* Lấy dữ liệu sử dụng Bundle và tên là product vào ''it là product''*/
            val b = Bundle().apply { putParcelable("product", it) }
            findNavController().navigate(R.id.action_homeFragment_to_productDetailsFragment, b)
        }

        bestDealAdapter.onClick = {
            val b = Bundle().apply { putParcelable("product", it) }
            findNavController().navigate(R.id.action_homeFragment_to_productDetailsFragment, b)
        }

        bestProductAdapter.onClick = {
            val b = Bundle().apply { putParcelable("product", it) }
            findNavController().navigate(R.id.action_homeFragment_to_productDetailsFragment, b)
        }

        /* This is life cycle for special product */
        lifecycleScope.launchWhenStarted {
            viewModel.specialProduct.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        showLoading()
                    }

                    /* Cập nhật danh sách */
                    is Resource.Success -> {
                        specialProductAdapter.differ.submitList(it.data)
                        hideLoading()
                    }

                    is Resource.Error -> {
                        hideLoading()
                        Log.e(TAG, it.message.toString())
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }

                    else -> Unit
                }
            }
        }

        /* This is life cycle for best deals products */
        lifecycleScope.launchWhenStarted {
            viewModel.bestDealProducts.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        showLoading()
                    }

                    is Resource.Success -> {
                        bestDealAdapter.differ.submitList(it.data)
                        hideLoading()
                    }

                    is Resource.Error -> {
                        hideLoading()
                        Log.e(TAG, it.message.toString())
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }

                    else -> Unit
                }
            }
        }

        /* This is life cycle for products */
        lifecycleScope.launchWhenStarted {
            viewModel.bestProducts.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.bestProductsProgressbar.visibility = View.VISIBLE
                    }

                    is Resource.Success -> {
                        bestProductAdapter.differ.submitList(it.data)
                        binding.bestProductsProgressbar.visibility = View.GONE
                    }

                    is Resource.Error -> {
                        binding.bestProductsProgressbar.visibility = View.GONE
                        Log.e(TAG, it.message.toString())
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }

                    else -> Unit
                }
            }
        }

        /* Paging and queries */
        binding.nestedScrollMainCategory.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
                /* Nếu đã chạm đến dưới cùng thì tải
                thêm sản phẩm đã tính toán từ MainCategoryViewModel. Tìm trên mạng */
                if (v.getChildAt(0).bottom <= v.height + scrollY) {
                    viewModel.fetchBestProducts()
                }
            })
    }

    private fun setupBestProductRv() {
        bestProductAdapter = BestProductAdapter()
        binding.rvBestProducts.apply {
            layoutManager =
                GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
            adapter = bestProductAdapter
        }
    }

    // TODO: this method is using to set up recycler view
    private fun setupBestDealRv() {
        bestDealAdapter = BestDealAdapter()
        binding.rvBestDealsProducts.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = bestDealAdapter
        }
    }

    // TODO: this method is using to hide progress bar
    private fun hideLoading() {
        binding.mainCategoryProgressbar.visibility = View.GONE
    }

    // TODO: this method is using to show progress bar
    private fun showLoading() {
        binding.mainCategoryProgressbar.visibility = View.VISIBLE
    }

    // TODO: this method is using to set utorecycler view
    private fun setupSpecialProductRv() {
        specialProductAdapter = SpecialProductAdapter()
        binding.rvSpecialProducts.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = specialProductAdapter
        }
    }

    override fun onResume() {
        super.onResume()

        showBottomNavigationView()
    }
}