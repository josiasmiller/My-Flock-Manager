package com.weyr_associates.animaltrakkerfarmmobile.app.animal.action

import androidx.core.view.isInvisible
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care.hooves.HoofCheckAction
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ItemAnimalActionHoofCheckBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.Hoof
import com.weyr_associates.animaltrakkerfarmmobile.model.HoofCheck
import com.weyr_associates.animaltrakkerfarmmobile.model.Hooves
import com.weyr_associates.animaltrakkerfarmmobile.model.hasAll
import com.weyr_associates.animaltrakkerfarmmobile.model.hasNone

class HoofCheckActionViewHolder(
    private val binding: ItemAnimalActionHoofCheckBinding,
    private val onActionActivated: (HoofCheckAction) -> Unit,
    private val onActionMenuActivated: (HoofCheckAction) -> Unit
) : AnimalActionViewHolder(binding.root) {

    fun bind(hoofCheckAction: HoofCheckAction) {
        binding.root.background = if (hoofCheckAction.isComplete)
            backgroundDrawableActive else backgroundDrawableInactive
        binding.imageCompleteness.setImageDrawable(
            if (hoofCheckAction.isComplete) checkBoxDrawableChecked
            else checkBoxDrawableUnchecked
        )
        binding.root.setOnClickListener { onActionActivated(hoofCheckAction) }
        binding.imageMoreOptions.setOnClickListener { onActionMenuActivated(hoofCheckAction) }
        binding.containerHoofCheckSummary.isInvisible = !hoofCheckAction.isComplete
        summarizeHoofCheck(hoofCheckAction.hoofCheck)
    }

    private fun summarizeHoofCheck(hoofCheck: HoofCheck?) {
        binding.textTrimmedSummary.text = hoofCheck?.let { listHooves(it.trimmed) } ?: ""
        binding.textRotSummary.text = hoofCheck?.let { listHooves(it.withFootRotObserved) } ?: ""
        binding.textScaldSummary.text = hoofCheck?.let { listHooves(it.withFootScaldObserved) } ?: ""
    }

    private fun listHooves(hooves: Hooves): String {
        return when {
            hooves.hasNone() -> "None"
            hooves.hasAll() -> "All"
            else -> Hoof.entries.filter { hooves.contains(it) }.sortedBy { it.ordinal }
                .joinToString(",") { it.abbreviation }
        }
    }
}
