package com.weyr_associates.animaltrakkerfarmmobile.app.animal.action

import androidx.core.view.isInvisible
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care.horns.HornCheckAction
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ItemAnimalActionHornCheckBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.Horn
import com.weyr_associates.animaltrakkerfarmmobile.model.HornCheck
import com.weyr_associates.animaltrakkerfarmmobile.model.Horns
import com.weyr_associates.animaltrakkerfarmmobile.model.hasAll
import com.weyr_associates.animaltrakkerfarmmobile.model.hasNone

class HornCheckActionViewHolder(
    private val binding: ItemAnimalActionHornCheckBinding,
    private val onActionActivated: (HornCheckAction) -> Unit,
    private val onActionMenuActivated: (HornCheckAction) -> Unit
) : AnimalActionViewHolder(binding.root) {

    fun bind(hornCheckAction: HornCheckAction) {
        binding.root.background = if (hornCheckAction.isComplete)
            backgroundDrawableActive else backgroundDrawableInactive
        binding.imageCompleteness.setImageDrawable(
            if (hornCheckAction.isComplete) checkBoxDrawableChecked
            else checkBoxDrawableUnchecked
        )
        binding.root.setOnClickListener { onActionActivated(hornCheckAction) }
        binding.imageMoreOptions.setOnClickListener { onActionMenuActivated(hornCheckAction) }
        binding.containerHornCheckSummary.isInvisible = !hornCheckAction.isComplete
        summarizeHornCheck(hornCheckAction.hornCheck)
    }

    private fun summarizeHornCheck(hornCheck: HornCheck?) {
        binding.textBadSummary.text = hornCheck?.let { listHorns(it.badHorns) } ?: ""
        binding.textSawedSummary.text = hornCheck?.let { listHorns(it.sawedHorns) } ?: ""
    }

    private fun listHorns(horns: Horns): String {
        return when {
            horns.hasNone() -> "None"
            horns.hasAll() -> "Both"
            else -> Horn.entries.filter { horns.contains(it) }.sortedBy { it.ordinal }
                .joinToString(",") { it.abbreviation }
        }
    }
}
