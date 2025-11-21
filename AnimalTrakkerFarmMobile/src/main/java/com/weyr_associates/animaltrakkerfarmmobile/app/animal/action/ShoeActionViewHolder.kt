package com.weyr_associates.animaltrakkerfarmmobile.app.animal.action

import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care.shoe.ShoeAction
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ItemAnimalActionCheckoffBinding

class ShoeActionViewHolder(
    private val binding: ItemAnimalActionCheckoffBinding,
    private val onActionActivated: (ShoeAction) -> Unit,
    private val onActionMenuActivated: (ShoeAction) -> Unit
) : AnimalActionViewHolder(binding.root) {

    init {
        binding.textActionName.setText(R.string.text_shod)
    }

    fun bind(shoeAction: ShoeAction) {
        binding.root.background = if (shoeAction.isComplete)
            backgroundDrawableActive else backgroundDrawableInactive
        binding.imageCompleteness.setImageDrawable(if (shoeAction.isComplete)
            checkBoxDrawableChecked else checkBoxDrawableUnchecked
        )
        binding.root.setOnClickListener {
            onActionActivated.invoke(shoeAction)
        }
        binding.imageMoreOptions.setOnClickListener {
            onActionMenuActivated.invoke(shoeAction)
        }
    }
}
