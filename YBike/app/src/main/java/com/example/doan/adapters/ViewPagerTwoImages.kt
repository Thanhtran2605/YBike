package com.example.doan.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.doan.databinding.ViewpagerImageItemBinding

class ViewPagerTwoImages : RecyclerView.Adapter<ViewPagerTwoImages.ViewPagerTwoImagesViewHolder>() {
    /* Tạo Adapter cho Image giống như cho Recycler View */

    inner class ViewPagerTwoImagesViewHolder(val binding: ViewpagerImageItemBinding) :
        ViewHolder(binding.root) {

        fun bind(imagePath: String) {
            /* Tải hình ảnh và hiển thị trong imageProductDetails trong layout viewpager_image_item.xml */
            Glide.with(itemView).load(imagePath).into(binding.imageProductDetails)
        }

    }

    /* Cho các mục của chuỗi Adapter này là URL của hình ảnh.
    * So sánh hai đối tượng vì nó là String. */
    private val diffCalback = object : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCalback)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewPagerTwoImagesViewHolder {
        return ViewPagerTwoImagesViewHolder(
            ViewpagerImageItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ViewPagerTwoImagesViewHolder, position: Int) {
        val image = differ.currentList[position]
        holder.bind(image)
    }
}