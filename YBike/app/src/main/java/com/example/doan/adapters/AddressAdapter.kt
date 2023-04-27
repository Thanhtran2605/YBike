package com.example.doan.adapters

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.doan.R
import com.example.doan.data.Address
import com.example.doan.databinding.AddressRvItemBinding

class AddressAdapter : RecyclerView.Adapter<AddressAdapter.AddressViewHolder>() {

    inner class AddressViewHolder(val binding: AddressRvItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(address: Address, isSelected: Boolean) {
            binding.apply {
                /* Thay đổi text của button là tên địa chỉ */
                buttonAddress.text = address.addressTitle

                if (isSelected) {
                    // Thay đổi màu nền khi click
                    buttonAddress.background =
                        ColorDrawable(itemView.context.resources.getColor(R.color.g_button))

                    // Thay đổi màu chữ khi click
                    buttonAddress.setTextColor(Color.parseColor("#FFFFFF"))
                } else {
                    buttonAddress.background =
                        ColorDrawable(itemView.context.resources.getColor(R.color.g_white))
                }
            }
        }

    }

    private val diffUtil = object : DiffUtil.ItemCallback<Address>() {
        override fun areItemsTheSame(oldItem: Address, newItem: Address): Boolean {
            return oldItem.addressTitle == newItem.addressTitle && oldItem.fullName == newItem.fullName
        }

        override fun areContentsTheSame(oldItem: Address, newItem: Address): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        return AddressViewHolder(
            AddressRvItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    /**
     * Thử xem địa chỉ đã được chọn hay chưa?. Nếu được chọn thì đổi màu qua
     * màu xanh biển, nếu không được chọn giữ nguyên màu trắng.
     * Nếu địa chỉ bằng position đúng thì đã lựa chọn địa chỉ.
     * */

    var selectedAddress = -1
    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        val address = differ.currentList[position]

        /* Gửi địa chỉ */
        holder.bind(address, selectedAddress == position)

        holder.binding.buttonAddress.setOnClickListener {
            if (selectedAddress >= 0)
                notifyItemChanged(selectedAddress)
            /* gán selectedAddress bằng với vị trị trong Adapter */
            selectedAddress = holder.adapterPosition
            notifyItemChanged(selectedAddress)
            onClick?.invoke(address)
        }
    }

    /**
     * Dòng này sẽ giải quyết nếu có lỗi khi bấm vào thêm địa chỉ thoát ra
     * hai địa chỉ khác đã được chọn màu xanh.
     * */
    init {
        differ.addListListener { _, _ ->
            notifyItemChanged(selectedAddress)
        }
    }

    var onClick: ((Address) -> Unit)? = null


}