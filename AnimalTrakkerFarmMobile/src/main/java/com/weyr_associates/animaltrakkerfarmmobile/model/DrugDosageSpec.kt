package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class DrugDosageSpec(
    override val id: EntityId,
    val drugId: EntityId,
    val drugTradeName: String,
    val drugGenericName: String,
    val speciesId: EntityId,
    val speciesName: String,
    val officialDrugDosage: String,
    val userDrugDosage: String?,
    val meatWithdrawalSpec: DrugWithdrawalSpec?,
    val milkWithdrawalSpec: DrugWithdrawalSpec?
) : Parcelable, HasIdentity {
    @IgnoredOnParcel
    val effectiveDrugDosage: String =
        userDrugDosage ?: officialDrugDosage
}
