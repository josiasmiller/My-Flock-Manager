package com.weyr_associates.animaltrakkerfarmmobile.app.animal.action

import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care.hooves.HoofCheckAction
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care.horns.HornCheckAction
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care.shear.ShearAction
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care.shoe.ShoeAction
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care.wean.WeanAction
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.drug.DrugAction
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.weight.WeightAction
import com.weyr_associates.animaltrakkerfarmmobile.model.DrugApplicationInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.DrugType
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import java.util.UUID
import kotlin.collections.map

data class ActionSet(
    val targetSpeciesId: EntityId?,
    val dewormers: List<DrugAction> = emptyList(),
    val vaccines: List<DrugAction> = emptyList(),
    val otherDrugs: List<DrugAction> = emptyList(),
    val weight: WeightAction? = null,
    val hoofCheck: HoofCheckAction? = null,
    val hornCheck: HornCheckAction? = null,
    val shoeing: ShoeAction? = null,
    val weaning: WeanAction? = null,
    val shearing: ShearAction? = null
) {
    companion object {
        const val MAX_NUM_DEWORMERS = 4
        const val MAX_NUM_VACCINES = 10
    }

    init {
        require(
            dewormers.all { it.targetSpeciesId == targetSpeciesId } &&
            vaccines.all { it.targetSpeciesId == targetSpeciesId } &&
            otherDrugs.all { it.targetSpeciesId == targetSpeciesId }
        ) {
            "All dewormers, vaccines, and other drugs must have the same target species id as the action set."
        }
    }

    val isConfigured: Boolean
        get() = dewormers.isNotEmpty() ||
                vaccines.isNotEmpty() ||
                otherDrugs.isNotEmpty() ||
                weight != null ||
                hoofCheck != null ||
                hornCheck != null ||
                shoeing != null ||
                shearing != null ||
                weaning != null

    val isAnimalCareConfigured: Boolean
        get() = weight != null ||
                hoofCheck != null ||
                hornCheck != null ||
                shoeing != null ||
                shearing != null ||
                weaning != null

    val areDrugsConfigured: Boolean
        get() = dewormers.isNotEmpty() ||
                vaccines.isNotEmpty() ||
                otherDrugs.isNotEmpty()

    val canAddDewormers: Boolean
        get() = dewormers.size < MAX_NUM_DEWORMERS

    val canAddVaccines: Boolean
        get() = vaccines.size < MAX_NUM_VACCINES

    fun containsDrugAction(drugAction: DrugAction): Boolean {
        return drugActionsForDrug(drugAction.configuration.drugApplicationInfo.drugTypeId)
            .any { it.actionId == drugAction.actionId }
    }

    fun addDrugAction(configuration: DrugAction.Configuration): ActionSet {
        if (isConfigured(configuration)) {
            return this
        }
        return when {
            configuration.drugApplicationInfo.drugTypeId == DrugType.ID_DEWORMER && canAddDewormers -> {
                copy(dewormers = addDrugActionTo(configuration, dewormers))
            }
            configuration.drugApplicationInfo.drugTypeId == DrugType.ID_VACCINE && canAddVaccines -> {
                copy(vaccines = addDrugActionTo(configuration, vaccines))
            }
            else -> copy(otherDrugs = addDrugActionTo(configuration, otherDrugs))
        }
    }

    fun updateDrugAction(
        actionId: UUID,
        configuration: DrugAction.Configuration
    ): ActionSet {
        return when {
            configuration.drugApplicationInfo.drugTypeId == DrugType.ID_VACCINE &&
                    vaccines.any { it.actionId == actionId } -> {
                copy(vaccines = updateDrugAction(vaccines, actionId, configuration))
            }
            configuration.drugApplicationInfo.drugTypeId == DrugType.ID_DEWORMER &&
                    dewormers.any { it.actionId == actionId } -> {
                copy(dewormers = updateDrugAction(dewormers, actionId, configuration))
            }
            otherDrugs.any { it.actionId == actionId } -> {
                copy(otherDrugs = updateDrugAction(otherDrugs, actionId, configuration))
            }
            else -> this
        }
    }

    fun removeDrugAction(drugAction: DrugAction): ActionSet {
        return when (drugAction.configuration.drugApplicationInfo.drugTypeId) {
            DrugType.ID_VACCINE -> copy(vaccines = removeDrugActionFrom(drugAction, vaccines))
            DrugType.ID_DEWORMER -> copy(dewormers = removeDrugActionFrom(drugAction, dewormers))
            else -> copy(otherDrugs = removeDrugActionFrom(drugAction, otherDrugs))
        }
    }

    fun markDrugAppliedForAction(drugAction: DrugAction, drugApplied: Boolean): ActionSet {
        return when (drugAction.configuration.drugApplicationInfo.drugTypeId) {
            DrugType.ID_VACCINE -> copy(vaccines = markDrugAppliedIn(drugAction, drugApplied, vaccines))
            DrugType.ID_DEWORMER -> copy(dewormers = markDrugAppliedIn(drugAction, drugApplied, dewormers))
            else -> copy(otherDrugs = markDrugAppliedIn(drugAction, drugApplied, otherDrugs))
        }
    }

    fun resetAllActions(): ActionSet {
        return copy(
            vaccines = vaccines.reset(),
            dewormers = dewormers.reset(),
            otherDrugs = otherDrugs.reset(),
            weight = weight?.copy(weight = null),
            hoofCheck = hoofCheck?.copy(hoofCheck = null),
            hornCheck = hornCheck?.copy(hornCheck = null),
            shoeing = shoeing?.copy(isComplete = false),
            shearing = shearing?.copy(isComplete = false),
            weaning = weaning?.copy(isComplete = false)
        )
    }

    private fun isConfigured(drugConfiguration: DrugAction.Configuration): Boolean {
        return drugActionsForDrug(drugConfiguration.drugApplicationInfo.drugTypeId)
            .any {
                it.configuration.drugApplicationInfo.id == drugConfiguration.drugApplicationInfo.id &&
                        it.configuration.offLabelDrugDose?.id == drugConfiguration.offLabelDrugDose?.id
            }
    }

    private fun addDrugActionTo(
        configuration: DrugAction.Configuration,
        drugActions: List<DrugAction>
    ): List<DrugAction> {
        val newDrugAction = DrugAction(
            configuration = configuration,
            targetSpeciesId = targetSpeciesId,
            isDrugApplied = configuration.autoApplyDrug
        )
        val updatedDrugActions = drugActions.toMutableList().apply {
            add(newDrugAction)
        }
        return if (newDrugAction.isDrugApplied) {
            deactivateDrugsThatAreSameAs(newDrugAction, updatedDrugActions)
        } else {
            updatedDrugActions
        }
    }

    private fun updateDrugAction(
        list: List<DrugAction>,
        actionId: UUID,
        configuration: DrugAction.Configuration
    ): List<DrugAction> {
        return list.toMutableList().also {
            it.replaceAll { drugAction ->
                if (drugAction.actionId != actionId) {
                    drugAction
                } else {
                    DrugAction(
                        actionId = actionId,
                        configuration = configuration,
                        targetSpeciesId = targetSpeciesId,
                        isDrugApplied = drugAction.isComplete
                    )
                }
            }
        }
    }

    private fun removeDrugActionFrom(drugAction: DrugAction, drugActions: List<DrugAction>): List<DrugAction> {
        return drugActions.toMutableList().apply {
            removeIf { it.actionId == drugAction.actionId }
        }
    }

    private fun markDrugAppliedIn(drugAction: DrugAction, drugApplied: Boolean, drugActions: List<DrugAction>): List<DrugAction> {
        val index = drugActions.indexOfFirst { it.actionId == drugAction.actionId }
        return if (-1 < index && drugActions[index].isActionable && drugActions[index].isComplete != drugApplied) {
            val updatedDrugActions = drugActions.toMutableList().apply {
                this[index] = this[index].copy(isDrugApplied = drugApplied)
            }
            if (drugApplied) {
                deactivateDrugsThatAreSameAs(drugAction, updatedDrugActions)
            } else {
                updatedDrugActions
            }
        } else drugActions
    }

    private fun deactivateDrugsThatAreSameAs(drugAction: DrugAction, drugActions: List<DrugAction>): List<DrugAction> {
        return drugActions.map {
            if (it.actionId != drugAction.actionId &&
                it.configuration.drugApplicationInfo.drugId == drugAction.configuration.drugApplicationInfo.drugId) {
                it.copy(isDrugApplied = false)
            } else { it }
        }
    }

    private fun List<DrugAction>.reset(): List<DrugAction> {
        val visitedDrugIds = mutableMapOf<EntityId, Boolean>()
        forEach { drugAction ->
            val drugId = drugAction.configuration.drugApplicationInfo.drugId
            visitedDrugIds.put(drugId, visitedDrugIds.contains(drugId))
        }
        return map {
            val isMultipleOfSameDrug = visitedDrugIds[it.configuration.drugApplicationInfo.drugId] == true
            it.reset(isApplied = if (isMultipleOfSameDrug) false else null)
        }
    }

    private fun drugActionsForDrug(drugTypeId: EntityId): List<DrugAction> = when (drugTypeId) {
        DrugType.ID_VACCINE -> vaccines
        DrugType.ID_DEWORMER -> dewormers
        else -> otherDrugs
    }
}
