package com.example.doan.util

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/* This class to margin right between item. */
class HorizontalItemDecoration(private val amount: Int = 30) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.right = amount
    }

}