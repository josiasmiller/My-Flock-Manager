package com.weyr_associates.animaltrakkerfarmmobile.app.animal.action

import android.annotation.SuppressLint
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.menu.MenuOption
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.weight.WeightAction
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ItemAnimalActionWeightBinding

class WeightActionViewHolder(
    private val binding: ItemAnimalActionWeightBinding,
    private val onActionActivated: (WeightAction) -> Unit,
    private val onActionMenuActivated: (WeightAction) -> Unit,
    private val onActionMenuOptionTriggered: (WeightAction, MenuOption) -> Unit
) : AnimalActionViewHolder(binding.root) {

    @SuppressLint("DefaultLocale")
    fun bind(weightAction: WeightAction) {
        binding.root.background = if (weightAction.isComplete)
            backgroundDrawableActive else backgroundDrawableInactive
        binding.root.setOnClickListener {
            onActionActivated.invoke(weightAction)
        }
        binding.buttonEdit.setOnClickListener {
            onActionMenuOptionTriggered.invoke(weightAction, MenuOption.EDIT)
        }
        binding.imageMoreOptions.setOnClickListener {
            onActionMenuActivated.invoke(weightAction)
        }
        binding.textTapToScanWeight.isInvisible = weightAction.weight != null
        binding.textWeight.isGone = weightAction.weight == null
        binding.textWeight.text = weightAction.weight?.let {
            "${String.format("%.2f", it)} ${weightAction.units.abbreviation}"
        }
    }
}
