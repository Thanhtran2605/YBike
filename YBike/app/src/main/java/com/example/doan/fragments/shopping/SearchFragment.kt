package com.example.doan.fragments.shopping


import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.example.doan.R
import com.example.doan.adapters.BestProductAdapter
import com.example.doan.databinding.FragmentSearchBinding
import com.example.doan.util.Resource
import com.example.doan.viewmodel.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest


@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.fragment_search) {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var bestProductAdapter: BestProductAdapter

    private val searchViewModel by viewModels<SearchViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater)
        return binding.root
    }


    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /**
         * Lấy dữ liệu từ edit text và lưu vào product name lấy từ viewModel
         * */
        binding.editTextSearch.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                searchViewModel.products.name = s.toString()
            }
        })


        setupBestProductRv()

        /**
         * Khi click vào từng product thì chuyển hướng [gửi dữ liệu] sang productDetailsFragment
         * id từ shopping_graph.xml
         * */
        bestProductAdapter.onClick = {
            val b = Bundle().apply { putParcelable("product", it) }
            findNavController().navigate(R.id.action_searchFragment_to_productDetailsFragment, b)
        }

        /**
         * Dưới đây là khi hiển thị giỏ hàng có sản phẩm hoặc không có sản phẩm
         * */
        lifecycleScope.launchWhenStarted {
            searchViewModel.bestProducts.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.bestProductsProgressbar.visibility = View.INVISIBLE
                    }

                    is Resource.Success -> {
                        binding.bestProductsProgressbar.visibility = View.INVISIBLE

                        /* Nếu như không có sản phẩm thì hiển thị hộp trống.
                         * Nếu như có sản phẩm thì hiển thị dữ liệu. */
                        if (it.data!!.isEmpty()) {
                            showEmptySearch()

                            /* Phải tạo function để ẩn recyclerView. */
                            hideOtherViews()

                        } else {
                            hideEmptySearch()
                            showOtherViews()

                            /* Mặc định sẽ hiển thị các sản phẩm ở trong giỏ hàng,
                            vì đã gán dữ liệu ở CartProductAdapter.
                            Dòng phía dưới để thêm sản phẩm vào Adapter */
                            bestProductAdapter.differ.submitList(it.data)
                        }
                    }

                    is Resource.Error -> {
                        binding.bestProductsProgressbar.visibility = View.INVISIBLE
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }

                    else -> Unit
                }
            }
        }

        /* Paging and queries */
        binding.nestedScrollSearchProduct.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener
            { v, _, scrollY, _, _ ->
                /* Nếu đã chạm đến dưới cùng thì tải
                        thêm sản phẩm đã tính toán từ MainCategoryViewModel. Tìm trên mạng */
                if (v.getChildAt(0).bottom <= v.height + scrollY) {
                    searchViewModel.fetchBestProducts()
                }
            })


        /**
         * Khi click vào button để tìm kiếm và làm mới adapter và recyclerView
         * */
        searchViewModel.fetchBestProducts()

        binding.btnSearch.setOnClickListener(View.OnClickListener {
            if (binding.btnSearch.isClickable) {
                bestProductAdapter.notifyDataSetChanged()
                binding.rvBestProducts.invalidate()
                searchViewModel.fetchBestProducts()
            } else {
                searchViewModel.fetchBestProducts()
            }
        })
    }


    // TODO: this method is using to show other views
    private fun showOtherViews() {
        binding.apply {
            tvBestProducts.visibility = View.VISIBLE
            rvBestProducts.visibility = View.VISIBLE
        }
    }

    // TODO: this method is using to hide other views
    private fun hideOtherViews() {
        binding.apply {
            tvBestProducts.visibility = View.GONE
            rvBestProducts.visibility = View.GONE
        }
    }

    // TODO: this method is using to hide empty cart
    private fun hideEmptySearch() {
        binding.apply {
            layoutSearchEmpty.visibility = View.GONE
        }
    }

    //TODO: this method is using to show empty cart
    private fun showEmptySearch() {
        binding.apply {
            layoutSearchEmpty.visibility = View.VISIBLE
        }
    }


    private fun setupBestProductRv() {
        bestProductAdapter = BestProductAdapter()
        binding.rvBestProducts.apply {
            layoutManager =
                GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
            adapter = bestProductAdapter
        }
//        bestProductAdapter.notifyDataSetChanged()

    }
}

