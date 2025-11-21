package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AnimalDetails(
    val basicInfo: AnimalBasicInfo,
    val lifetime: AnimalLifetime,
    val rearing: AnimalRearing,
    val parentage: AnimalParentage?,
    val breeders: AnimalBreeders?,
    val weight: AnimalWeight?
) : Parcelable, HasIdentity {
    override val id: EntityId
        get() = basicInfo.id
}
