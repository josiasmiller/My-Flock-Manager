package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalTime

@Parcelize
data class BreedingSummary(
    val born: List<Total>,
    val weaned: List<Total>,
    val offspring: List<Offspring>
) : Parcelable {
    sealed interface Total : Parcelable {
        val name: String
        val value: Int
    }
    @Parcelize
    data class TotalBySex(val sex: Sex, override val value: Int) : Total {
        @IgnoredOnParcel
        override val name: String = sex.name
    }
    @Parcelize
    data class Offspring(
        val animalId: EntityId,
        val name: String,
        val sex: Sex,
        val birthDate: LocalDate?,
        val birthTime: LocalTime?,
        val registrationNumber: String?,
        val flockPrefix: String?
    ) : Parcelable
}
