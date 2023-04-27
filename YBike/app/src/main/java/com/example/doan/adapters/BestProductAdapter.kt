package com.example.doan.adapters

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.doan.data.Product
import com.example.doan.databinding.ProductRvItemBinding
import helper.getProductPrice
import java.text.NumberFormat
import java.util.*

class BestProductAdapter : RecyclerView.Adapter<BestProductAdapter.BestProductViewHolder>() {

    inner class BestProductViewHolder(private val binding: ProductRvItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.apply {
                Glide.with(itemView).load(product.images[0]).into(imgProduct)

                val priceAfterOffer =
                    product.offerPercentage.getProductPrice(product.price)

                val COUNTRY = "VN"
                val LANGUAGE = "vi"
                val newPrice: String =
                    NumberFormat.getCurrencyInstance(Locale(LANGUAGE, COUNTRY))
                        .format(priceAfterOffer)

                /* Định dạng giá tiền theo Việt Nam */
                tvNewPrice.text = "$newPrice"


                /* Sử dụng paintFlags để gạch giá tiền cũ */
                tvPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG

                if (product.offerPercentage == null) {
                    tvPrice.visibility = View.INVISIBLE
                }

                val oldPrice: String =
                    NumberFormat.getCurrencyInstance(Locale(LANGUAGE, COUNTRY))
                        .format(product.price)

                tvPrice.text = "$oldPrice"
                tvName.text = product.name
            }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id

        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BestProductViewHolder {
        return BestProductViewHolder(
            ProductRvItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: BestProductViewHolder, position: Int) {
        val product = differ.currentList[position]
        holder.bind(product)


        /* Khi click vào item nào thì gửi product. */
        holder.itemView.setOnClickListener {
            onClick?.invoke(product)
        }
    }

    var onClick: ((Product) -> Unit)? = null

}