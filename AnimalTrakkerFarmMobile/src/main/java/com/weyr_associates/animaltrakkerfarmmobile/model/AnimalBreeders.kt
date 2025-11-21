package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AnimalBreeders(
    val animalBreederInfo: BreederInfo?,
    val sireBreederInfo: BreederInfo?,
    val damBreederInfo: BreederInfo?
) : Parcelable

@Parcelize
data class BreederInfo(
    val animalId: EntityId,
    val breederId: EntityId,
    val breederType: Breeder.Type,
    val breederName: String,
) : Parcelable {
    companion object {
        fun from(
            animalId: EntityId?,
            breederId: EntityId?,
            breederType: Breeder.Type?,
            breederName: String?,
        ): BreederInfo? {
            return if (animalId == null ||
                breederId == null ||
                breederType == null ||
                breederName == null) {
                null
            } else {
                BreederInfo(
                    animalId,
                    breederId,
                    breederType,
                    breederName
                )
            }
        }
    }
}
