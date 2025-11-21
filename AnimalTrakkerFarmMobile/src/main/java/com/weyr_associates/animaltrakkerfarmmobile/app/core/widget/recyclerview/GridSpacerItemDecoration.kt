package com.weyr_associates.animaltrakkerfarmmobile.app.core.widget.recyclerview

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.weyr_associates.animaltrakkerfarmmobile.R

class GridSpacerItemDecoration(context: Context, private val spanCount: Int) : ItemDecoration() {

    private val inset = context.resources.getDimensionPixelOffset(R.dimen.activity_margin)
    private val insetInterior = inset / 2

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val itemCount = requireNotNull(parent.adapter).itemCount
        val rowCount = if (0 < itemCount) ((itemCount - 1) / spanCount) + 1 else 0
        val itemPosition = parent.getChildAdapterPosition(view)
        val row =  itemPosition / spanCount
        val column = itemPosition % spanCount
        val isFirstRow = itemPosition < spanCount
        val isFirstCol = column == 0
        val isLastRow = row == rowCount - 1
        val isLastCol = column == spanCount - 1

        outRect.left = if (isFirstCol) inset else insetInterior
        outRect.right = if (isLastCol) inset else insetInterior
        outRect.top = if (isFirstRow) inset else insetInterior
        outRect.bottom = if (isLastRow) inset else insetInterior
    }
}
