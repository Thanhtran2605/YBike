package com.example.doan.fragments.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doan.R
import com.example.doan.adapters.BestDealAdapter
import com.example.doan.adapters.BestProductAdapter
import com.example.doan.databinding.FragmentBaseCategoryBinding
import com.example.doan.util.HorizontalItemDecoration
import com.example.doan.util.showBottomNavigationView

open class BaseCategoryFragment : Fragment(R.layout.fragment_base_category) {
    private lateinit var binding: FragmentBaseCategoryBinding

    /* By Lazy có nghĩa là ta chỉ khởi tạo nó khi nó gọi lần đầu tiên. */
    protected val offerAdapter: BestDealAdapter by lazy { BestDealAdapter() }

    /* Khai báo Adapter, các biến đều sử dụng là BestProductAdapter */
    protected val bestProductAdapter: BestProductAdapter by lazy { BestProductAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBaseCategoryBinding.inflate(inflater)
        return binding.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupOfferRv()
        setupBestProductsRv()

        /* Dùng để lấy dữ liệu và điều hướng từ homeFragment qua productDetailsFragment. */
        offerAdapter.onClick = {
            val b = Bundle().apply { putParcelable("product", it) }
            findNavController().navigate(R.id.action_homeFragment_to_productDetailsFragment, b)
        }

        bestProductAdapter.onClick = {
            val b = Bundle().apply { putParcelable("product", it) }
            findNavController().navigate(R.id.action_homeFragment_to_productDetailsFragment, b)
        }

        binding.rvBestProducts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                /* Có thể cuộn theo chiều ngang, dx vị trí x = 0. */
                if (!recyclerView.canScrollHorizontally(1) && dx != 0) {
                    onOfferPagingRequest()
                }
            }
        })

        /* Paging and queries */
        binding.nestedScrollBaseCategory.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
                /* Nếu đã chạm đến dưới cùng thì tải
                thêm sản phẩm đã tính toán từ MainCategoryViewModel. Tìm trên mạng */
                if (v.getChildAt(0).bottom <= v.height + scrollY) {
                    onBestProductsPagingRequest()
                }
            })
    }

    fun showOfferLoading() {
        binding.offerProductsProgressBar.visibility = View.VISIBLE
    }

    fun hideOfferLoading() {
        binding.offerProductsProgressBar.visibility = View.GONE
    }

    fun showBestProductsLoading() {
        binding.bestProductsProgressBar.visibility = View.VISIBLE
    }

    fun hideBestProductsLoading() {
        binding.bestProductsProgressBar.visibility = View.GONE
    }


    open fun onOfferPagingRequest() {
    }

    open fun onBestProductsPagingRequest() {

    }


    /* Gán Adapter cho Recycler View giống MainCategoryFragment */
    private fun setupBestProductsRv() {
        binding.rvBestProducts.apply {
            layoutManager =
                GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
            adapter = bestProductAdapter
        }
    }

    private fun setupOfferRv() {
        binding.rvOffer.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = offerAdapter

            addItemDecoration(HorizontalItemDecoration())
        }

    }


    override fun onResume() {
        super.onResume()
        showBottomNavigationView()
    }


}