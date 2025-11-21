package com.weyr_associates.animaltrakkerfarmmobile.app.animal.action

import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care.shear.ShearAction
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ItemAnimalActionCheckoffBinding

class ShearActionViewHolder(
    private val binding: ItemAnimalActionCheckoffBinding,
    private val onActionActivated: (ShearAction) -> Unit,
    private val onActionMenuActivated: (ShearAction) -> Unit
) : AnimalActionViewHolder(binding.root) {

    init {
        binding.textActionName.setText(R.string.text_shear)
    }

    fun bind(shearAction: ShearAction) {
        binding.root.background = if (shearAction.isComplete)
            backgroundDrawableActive else backgroundDrawableInactive
        binding.imageCompleteness.setImageDrawable(if (shearAction.isComplete)
            checkBoxDrawableChecked else checkBoxDrawableUnchecked
        )
        binding.root.setOnClickListener {
            onActionActivated.invoke(shearAction)
        }
        binding.imageMoreOptions.setOnClickListener {
            onActionMenuActivated.invoke(shearAction)
        }
    }
}
