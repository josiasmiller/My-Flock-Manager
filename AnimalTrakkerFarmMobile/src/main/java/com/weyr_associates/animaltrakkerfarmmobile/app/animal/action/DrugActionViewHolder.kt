package com.weyr_associates.animaltrakkerfarmmobile.app.animal.action

import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.drug.DrugAction
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ItemAnimalActionDrugBinding

class DrugActionViewHolder(
    private val binding: ItemAnimalActionDrugBinding,
    private val onActionActivated: (DrugAction) -> Unit,
    private val onActionMenuActivated: (DrugAction) -> Unit
) : AnimalActionViewHolder(binding.root) {

    fun bind(drugAction: DrugAction) {
        binding.root.isEnabled = drugAction.isActionable
        binding.root.background = if (drugAction.isComplete)
            backgroundDrawableActive else backgroundDrawableInactive
        binding.imageCompleteness.setImageDrawable(
            if (drugAction.isComplete) checkBoxDrawableChecked
            else checkBoxDrawableUnchecked
        )
        binding.root.background = when {
            !drugAction.isActionable -> backgroundDrawableDisabled
            drugAction.isComplete -> backgroundDrawableActive
            else -> backgroundDrawableInactive
        }
        binding.textDrugName.text = drugAction.configuration.drugApplicationInfo.tradeDrugName
        binding.textDrugLocation.text = drugAction.configuration.location.abbreviation
        binding.textLot.text = drugAction.configuration.drugApplicationInfo.lot
        val offLabelDrugDose = drugAction.offLabelDrugDose
        val drugDosageSpec = drugAction.drugDosageSpec
        binding.textDrugDose.text = if (offLabelDrugDose != null) {
            "${offLabelDrugDose.vetLastName}:\n${offLabelDrugDose.drugDose}"
        } else drugDosageSpec?.effectiveDrugDosage ?: "Unknown Drug Dose"
        binding.root.setOnClickListener {
            onActionActivated.invoke(drugAction)
        }
        binding.imageMoreOptions.setOnClickListener {
            onActionMenuActivated.invoke(drugAction)
        }
    }
}
