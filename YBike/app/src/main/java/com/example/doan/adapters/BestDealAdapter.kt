package com.example.doan.adapters

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.doan.data.Product
import com.example.doan.databinding.BestDealsRvItemBinding
import java.text.NumberFormat
import java.util.*

class BestDealAdapter : RecyclerView.Adapter<BestDealAdapter.BestDealViewHolder>() {
    inner class BestDealViewHolder(private val binding: BestDealsRvItemBinding) :
        ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.apply {
                val COUNTRY = "VN"
                val LANGUAGE = "vi"
                Glide.with(itemView).load(product.images[0]).into(imgBestDeal)
                product.offerPercentage?.let {
                    val remainingPricePercentage = 1 - it
                    val priceAfterOffer = remainingPricePercentage * product.price

                    // Định dạng giá tiền theo Việt Nam
                    val newPrice: String =
                        NumberFormat.getCurrencyInstance(Locale(LANGUAGE, COUNTRY))
                            .format(priceAfterOffer)

                    tvNewPrice.text = "$newPrice"
                }

                val oldPrice: String =
                    NumberFormat.getCurrencyInstance(Locale(LANGUAGE, COUNTRY))
                        .format(product.price)

                tvOldPrice.text = "$oldPrice"
                /* Sử dụng paintFlags để gạch giá tiền cũ */
                tvOldPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG

                tvDealProductName.text = product.name
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BestDealViewHolder {
        return BestDealViewHolder(
            BestDealsRvItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: BestDealViewHolder, position: Int) {
        val product = differ.currentList[position]
        holder.bind(product)

        /* Khi click vào item nào thì gửi product. */
        holder.itemView.setOnClickListener {
            onClick?.invoke(product)
        }
    }

    var onClick: ((Product) -> Unit)? = null

}