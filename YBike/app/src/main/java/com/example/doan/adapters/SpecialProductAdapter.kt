package com.example.doan.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.doan.data.Product
import com.example.doan.databinding.SpecialRvItemBinding
import java.text.NumberFormat
import java.util.*

/* Tạo bộ điều hợp cho màn hình home hiển thị */
class SpecialProductAdapter :
    RecyclerView.Adapter<SpecialProductAdapter.SpecialProductsViewHolder>() {

    inner class SpecialProductsViewHolder(private val binding: SpecialRvItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.apply {
                /* Tải ảnh từ product.images vào id để hiển thị trong specialRvItem layout xml */
                Glide.with(itemView).load(product.images[0]).into(imgSpecialRvItem)
                tvSpecialProductName.text = product.name

                // Định dạng giá tiền theo Việt Nam
                val COUNTRY = "VN"
                val LANGUAGE = "vi"
                val newPrice: String =
                    NumberFormat.getCurrencyInstance(Locale(LANGUAGE, COUNTRY))
                        .format(product.price)
                tvSpecialProductPrice.text = newPrice
            }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Product>() {
        /* So sánh ID của hai đối tượng */
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            /*Là một cơ chế giúp trình tái chế xem nhanh hơn*/
            return oldItem.id == newItem.id
        }

        /* So sánh toàn bộ đối tượng */
        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }

    }


    /* Cập nhật và liệt kê danh sách */
    val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpecialProductsViewHolder {
        return SpecialProductsViewHolder(
            SpecialRvItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: SpecialProductsViewHolder, position: Int) {
        val product = differ.currentList[position]
        holder.bind(product)

        /* Khi click vào item nào thì gửi product. */
        holder.itemView.setOnClickListener {
            onClick?.invoke(product)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    var onClick: ((Product) -> Unit)? = null

}
