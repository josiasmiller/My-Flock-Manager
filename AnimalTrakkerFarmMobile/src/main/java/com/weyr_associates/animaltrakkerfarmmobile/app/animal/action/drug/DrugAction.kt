package com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.drug

import android.os.Parcelable
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.AnimalAction
import com.weyr_associates.animaltrakkerfarmmobile.model.DrugApplicationInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.DrugDosageSpec
import com.weyr_associates.animaltrakkerfarmmobile.model.DrugLocation
import com.weyr_associates.animaltrakkerfarmmobile.model.DrugWithdrawalSpec
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.OffLabelDrugDose
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class DrugAction(
    val configuration: Configuration,
    val targetSpeciesId: EntityId?,
    val isDrugApplied: Boolean = configuration.autoApplyDrug,
    override val actionId: UUID = UUID.randomUUID(),
) : AnimalAction, Parcelable {

    @Parcelize
    data class Configuration(
        val drugApplicationInfo: DrugApplicationInfo,
        val location: DrugLocation,
        val offLabelDrugDose: OffLabelDrugDose? = null,
        val autoApplyDrug: Boolean = true
    ) : Parcelable {
        init {
            require(offLabelDrugDose == null || drugApplicationInfo.drugId != offLabelDrugDose.speciesId) {
                "Off label drug dose drug id must match the configuration's drug id."
            }
        }
    }

    @IgnoredOnParcel
    val drugDosageSpec: DrugDosageSpec? by lazy {
        configuration.drugApplicationInfo.drugDosageSpecs.firstOrNull { it.speciesId == targetSpeciesId }
    }

    @IgnoredOnParcel
    val offLabelDrugDose: OffLabelDrugDose? by lazy {
        configuration.offLabelDrugDose?.takeIf { it.speciesId == targetSpeciesId }
    }

    @IgnoredOnParcel
    val isActionable: Boolean by lazy {
        drugDosageSpec != null || offLabelDrugDose != null
    }

    @IgnoredOnParcel
    override val isComplete: Boolean by lazy {
        isDrugApplied && isActionable
    }

    @IgnoredOnParcel
    val isOffLabel: Boolean by lazy {
        offLabelDrugDose != null
    }

    @IgnoredOnParcel
    val effectiveDrugDose: String? by lazy {
        offLabelDrugDose?.drugDose
            ?: drugDosageSpec?.effectiveDrugDosage
    }

    @IgnoredOnParcel
    val drugMeatWithdrawal: DrugWithdrawalSpec? by lazy {
        drugDosageSpec?.meatWithdrawalSpec
    }

    @IgnoredOnParcel
    val drugMilkWithdrawal: DrugWithdrawalSpec? by lazy {
        drugDosageSpec?.milkWithdrawalSpec
    }

    fun reset(isApplied: Boolean? = null): DrugAction {
        return copy(
            isDrugApplied = isApplied ?: configuration.autoApplyDrug
        )
    }
}
