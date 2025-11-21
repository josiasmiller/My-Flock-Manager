package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BreedPart(
    val breedId: EntityId,
    val percentage: Float
) : Parcelable {
    companion object {
        const val BREED_NAME_UNKNOWN = "Unknown"
        const val BREED_ABBREVIATION_UNKNOWN = "UNK"
        const val BREED_NAME_MIXED = "Mixed"
        const val BREED_ABBREVIATION_MIXED = "MIX"
    }
}
