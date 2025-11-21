package com.weyr_associates.animaltrakkerfarmmobile.app.core.widget.recyclerview

import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.weyr_associates.animaltrakkerfarmmobile.R

abstract class PlacardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    protected val checkBoxDrawableChecked: Drawable by lazy {
        requireNotNull(
            ContextCompat.getDrawable(
                itemView.context,
                R.drawable.ic_check_box_checked
            )
        )
    }
    protected val checkBoxDrawableUnchecked: Drawable by lazy {
        requireNotNull(
            ContextCompat.getDrawable(
                itemView.context,
                R.drawable.ic_check_box_outline_blank
            )
        )
    }
    protected val radioDrawableChecked: Drawable by lazy {
        requireNotNull(
            ContextCompat.getDrawable(
                itemView.context,
                R.drawable.ic_radio_button_checked
            )
        )
    }
    protected val radioBoxDrawableUnchecked: Drawable by lazy {
        requireNotNull(
            ContextCompat.getDrawable(
                itemView.context,
                R.drawable.ic_radio_button_unchecked
            )
        )
    }
    protected val backgroundDrawableActive: Drawable by lazy {
        requireNotNull(
            ContextCompat.getDrawable(
                itemView.context,
                R.drawable.background_placard_item_active
            )
        )
    }
    protected val backgroundDrawableInactive: Drawable by lazy {
        requireNotNull(
            ContextCompat.getDrawable(
                itemView.context,
                R.drawable.background_placard_item_inactive
            )
        )
    }
    protected val backgroundDrawableDisabled: Drawable by lazy {
        requireNotNull(
            ContextCompat.getDrawable(
                itemView.context,
                R.drawable.background_placard_item_disabled
            )
        )
    }
}
