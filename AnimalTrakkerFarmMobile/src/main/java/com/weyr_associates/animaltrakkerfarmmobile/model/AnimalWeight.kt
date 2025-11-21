package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class AnimalWeight(
    val animalId: Int,
    val weight: Float,
    val unitsId: Int,
    val unitsName: String,
    val unitsAbbreviation: String,
    val weighedOn: LocalDate
) : Parcelable {
    companion object {
        fun from(
            animalId: Int?,
            weight: Float?,
            unitsId: Int?,
            unitsName: String?,
            unitsAbbreviation: String?,
            weighedOn: LocalDate?
        ): AnimalWeight? {
            return if (
                animalId == null ||
                weight == null ||
                unitsId == null ||
                unitsName == null ||
                unitsAbbreviation == null ||
                weighedOn == null
            ) {
                null
            } else {
                AnimalWeight(animalId, weight, unitsId, unitsName, unitsAbbreviation, weighedOn)
            }
        }
    }
}
