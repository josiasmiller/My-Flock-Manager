package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class OffLabelDrugDose(
    override val id: EntityId,
    val drugId: EntityId,
    val drugTradeName: String,
    val speciesId: EntityId,
    val speciesName: String,
    val vetContactId: EntityId,
    val vetLastName: String,
    val drugDose: String
): Parcelable, HasIdentity, HasName {
    @IgnoredOnParcel
    override val name: String by lazy {
        "$vetLastName - $drugTradeName : $speciesName $drugDose"
    }
}
