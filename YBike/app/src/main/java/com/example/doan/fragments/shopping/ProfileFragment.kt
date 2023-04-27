package com.example.doan.fragments.shopping

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.doan.BuildConfig
import com.example.doan.R
import com.example.doan.activities.LoginRegisterActivity
import com.example.doan.databinding.FragmentProfileBinding
import com.example.doan.util.Resource
import com.example.doan.util.showBottomNavigationView
import com.example.doan.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    val viewModel by viewModels<ProfileViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Khi click vào khối người dùng liên kết bên navigation nav để qua [userAccountFragment]
        binding.constraintProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_userAccountFragment)
        }

        // Khi click vào Tất cả đơn hàng liên kết bên navigation nav để qua [allOldersFragment]
        binding.linearAllOrders.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_allOrdersFragment)
        }

        // Sử dụng cách liên kết khác vì phải chuyển một đối số
        // Khi click vào Hóa đơn liên kết bên navigation nav để qua [billingFragment]
        binding.linearBilling.setOnClickListener {
            // Ban đầu là không có bất kỳ sản phẩm và giá nào. Là một mảng rỗng sau đó link
            val action = ProfileFragmentDirections.actionProfileFragmentToBillingFragment(
                0f,
                emptyArray()
            )
            findNavController().navigate(action)
        }

        // Khi click vào Logout tạo một function và Intent trở về trang [LoginRegisterActivity]
        binding.linearLogOut.setOnClickListener {
            viewModel.logout()
            val intent = Intent(requireActivity(), LoginRegisterActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        // Cài đặt tên text view [version]
        binding.tvVersion.text = "Phiên bản ${BuildConfig.VERSION_CODE}"

        /**
         * Cho life cycler trạng thái người dùng
         * */
        lifecycleScope.launchWhenStarted {
            viewModel.user.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.progressbarSettings.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        binding.progressbarSettings.visibility = View.GONE

                        // Sử dụng Glide hiển thị hình ảnh của người dùng
                        // into : tải vào hình ành người dùng trong fragment.xmkl
                        Glide.with(requireView()).load(it.data!!.imagePath).error(
                            ColorDrawable(
                                Color.BLACK
                            )
                        ).into(binding.imageUser)

                        binding.tvUserName.text = "${it.data.firstName} ${it.data.lastName}"
                    }

                    is Resource.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        binding.progressbarSettings.visibility = View.GONE
                    }
                    else -> Unit
                }
            }
        }

    }

    // Khi chạy thì hiển thị [NavigationViewBottom]
    override fun onResume() {
        super.onResume()

        showBottomNavigationView()
    }


}