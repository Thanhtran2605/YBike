package com.example.doan.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.doan.data.CartProduct
import com.example.doan.databinding.CartProductItemBinding
import helper.getProductPrice
import java.text.NumberFormat
import java.util.*

class CartProductAdapter :
    RecyclerView.Adapter<CartProductAdapter.CartProductsViewHolder>() {

    inner class CartProductsViewHolder(val binding: CartProductItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(cartProduct: CartProduct) {
            binding.apply {
                /* Tải ảnh từ product.images vào id để hiển thị trong specialRvItem layout xml */
                Glide.with(itemView).load(cartProduct.product.images[0]).into(imageCartProduct)
                tvProductCartName.text = cartProduct.product.name
                tvCartProductQuantity.text = cartProduct.quantity.toString()

                val priceAfterPercentage =
                    cartProduct.product.offerPercentage.getProductPrice(cartProduct.product.price)

                // Định dạng giá tiền theo Việt Nam
                val COUNTRY = "VN"
                val LANGUAGE = "vi"
                val productPrice: String =
                    NumberFormat.getCurrencyInstance(Locale(LANGUAGE, COUNTRY))
                        .format(priceAfterPercentage)

                /* Số tiền lấy 2 chữ số sau hàng thập phân */
                tvProductCartPrice.text = "$productPrice"
            }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<CartProduct>() {
        /* So sánh ID của hai đối tượng */
        override fun areItemsTheSame(oldItem: CartProduct, newItem: CartProduct): Boolean {
            /*Là một cơ chế giúp trình tái chế xem nhanh hơn*/
            return oldItem.product.id == newItem.product.id
        }

        /* So sánh toàn bộ đối tượng */
        override fun areContentsTheSame(oldItem: CartProduct, newItem: CartProduct): Boolean {
            return oldItem == newItem
        }

    }


    /* Cập nhật và liệt kê danh sách */
    val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartProductsViewHolder {
        return CartProductsViewHolder(
            CartProductItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: CartProductsViewHolder, position: Int) {
        val cartProduct = differ.currentList[position]
        holder.bind(cartProduct)

        /* Khi click vào item nào thì gửi cho cart product. */
        holder.itemView.setOnClickListener {
            onProductClick?.invoke(cartProduct)
        }

        /* Khi click vào item image plus gửi cho cart product. */
        holder.binding.imagePlus.setOnClickListener {
            onPlusClick?.invoke(cartProduct)
        }

        /* Khi click vào item image minus gửi cho cart product. */
        holder.binding.imageMinus.setOnClickListener {
            onMinusClick?.invoke(cartProduct)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    var onProductClick: ((CartProduct) -> Unit)? = null
    var onPlusClick: ((CartProduct) -> Unit)? = null
    var onMinusClick: ((CartProduct) -> Unit)? = null

}