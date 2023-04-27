package com.example.doan.fragments.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.doan.R
import com.example.doan.adapters.HomeViewPagerAdapter
import com.example.doan.databinding.FragmentHomeBinding
import com.example.doan.fragments.categories.*
import com.google.android.material.tabs.TabLayoutMediator

class HomeFragment: Fragment(R.layout.fragment_home) {
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val categoriesFragments = arrayListOf(
            MainCategoryFragment(),
            WaveFragment(),
            LeadFragment(),
            SHFragment(),
            AirBladeFragment(),
            VisionFragment()
        )

        binding.viewPagerHome.isUserInputEnabled = false

        val viewPager2Adapter =
            HomeViewPagerAdapter(categoriesFragments, childFragmentManager, lifecycle)

        binding.viewPagerHome.adapter = viewPager2Adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPagerHome) { tab, position ->
            when (position) {
                0 -> tab.text = "Trang chủ"
                1 -> tab.text = "Wave"
                2 -> tab.text = "Lead"
                3 -> tab.text = "SH"
                4 -> tab.text = "Air Blade"
                5 -> tab.text = "Vision"
            }
        }.attach()

    }
}