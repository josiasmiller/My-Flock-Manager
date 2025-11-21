package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DrugApplicationInfo(
    val drugLotId: EntityId,
    val drugId: EntityId,
    val drugTypeId: EntityId,
    val tradeDrugName: String,
    val genericDrugName: String,
    val lot: String?,
    val isGone: Boolean,
    val drugDosageSpecs: List<DrugDosageSpec>
) : Parcelable, HasIdentity, HasName {

    override val id: EntityId
        get() = drugLotId

    override val name: String
        get() = "$tradeDrugName Lot $lot"
}
