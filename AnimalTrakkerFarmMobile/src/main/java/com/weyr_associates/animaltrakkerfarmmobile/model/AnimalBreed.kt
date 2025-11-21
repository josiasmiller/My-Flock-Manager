package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AnimalBreed(
    val id: EntityId,
    val animalId: EntityId,
    val breedId: EntityId,
    val breedName: String,
    val breedAbbreviation: String,
    val percentage: Float
) : Parcelable
