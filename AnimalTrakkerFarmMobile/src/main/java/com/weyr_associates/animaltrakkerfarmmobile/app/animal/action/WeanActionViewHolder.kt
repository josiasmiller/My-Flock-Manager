package com.weyr_associates.animaltrakkerfarmmobile.app.animal.action

import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care.wean.WeanAction
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ItemAnimalActionCheckoffBinding

class WeanActionViewHolder(
    private val binding: ItemAnimalActionCheckoffBinding,
    private val onActionActivated: (WeanAction) -> Unit,
    private val onActionMenuActivated: (WeanAction) -> Unit
) : AnimalActionViewHolder(binding.root) {

    private val weanTitle = itemView.context.getString(R.string.text_wean)
    private val alreadyWeanedTitle = itemView.context.getString(R.string.text_already_weaned)

    fun bind(weanAction: WeanAction) {
        binding.root.isEnabled = weanAction.isActionable
        binding.imageMoreOptions.isEnabled = weanAction.isActionable
        binding.textActionName.text = if (weanAction.isActionable)
            weanTitle else alreadyWeanedTitle
        binding.root.background = when {
            !weanAction.isActionable -> backgroundDrawableDisabled
            weanAction.isComplete -> backgroundDrawableActive
            else -> backgroundDrawableInactive
        }
        binding.imageCompleteness.setImageDrawable(if (weanAction.isComplete)
            checkBoxDrawableChecked else checkBoxDrawableUnchecked
        )
        binding.root.setOnClickListener {
            onActionActivated.invoke(weanAction)
        }
        binding.imageMoreOptions.setOnClickListener {
            onActionMenuActivated.invoke(weanAction)
        }
    }
}
