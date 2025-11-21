package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalTime

@Parcelize
data class MaleBreedingDetails(
    val sireId: EntityId,
    val events: List<BreedingEvent>,
    val nonEventBirthsBySex: List<TotalBySex>
) : Parcelable {
    @Parcelize
    data class BreedingEvent(
        val maleBreedingId: EntityId,
        val dateMaleIn: LocalDate,
        val timeMaleIn: LocalTime,
        val dateMaleOut: LocalDate?,
        val timeMaleOut: LocalTime?,
        val serviceType: ServiceType,
        val birthingDate: LocalDate?,
        val birthingTime: LocalTime?,
        val birthNotes: String?,
        val birthsBySex: List<TotalBySex>
    ) : Parcelable
    sealed interface Total : Parcelable {
        val name: String
        val value: Int
    }
    @Parcelize
    data class TotalBySex(val sex: Sex, override val value: Int) : Total {
        @IgnoredOnParcel
        override val name: String = sex.name
    }
}
