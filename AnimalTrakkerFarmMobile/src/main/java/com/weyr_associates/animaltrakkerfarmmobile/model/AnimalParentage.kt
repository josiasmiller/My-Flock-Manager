package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AnimalParentage(
    val sireInfo: ParentInfo?,
    val damInfo: ParentInfo?
) : Parcelable

@Parcelize
data class ParentInfo(
    val animalId: EntityId,
    val name: String,
    val ownerType: Owner.Type,
    val ownerId: Int,
    val ownerName: String,
    val flockPrefixId: Int,
    val flockPrefix: String,
) : Parcelable {
    companion object {
        fun from(
            animalId: EntityId?,
            name: String?,
            ownerType: Owner.Type?,
            ownerId: Int?,
            ownerName: String?,
            flockPrefixId: Int?,
            flockPrefix: String?,
        ): ParentInfo? {
            return if (
                animalId == null ||
                name == null ||
                ownerId == null ||
                ownerType == null ||
                ownerName == null ||
                flockPrefixId == null ||
                flockPrefix == null
            ) {
                null
            } else {
                ParentInfo(
                    animalId = animalId,
                    name = name,
                    ownerId = ownerId,
                    ownerType = ownerType,
                    ownerName = ownerName,
                    flockPrefixId = flockPrefixId,
                    flockPrefix = flockPrefix
                )
            }
        }
    }
}
