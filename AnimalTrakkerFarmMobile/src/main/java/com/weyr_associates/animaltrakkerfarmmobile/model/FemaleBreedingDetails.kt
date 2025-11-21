package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalTime

@Parcelize
data class FemaleBreedingDetails(
    val damId: EntityId,
    val yearly: List<BreedingEventsByYear>
) : Parcelable {
    @Parcelize
    data class BreedingEventsByYear(
        val year: Int?,
        val events: List<BreedingEvent>,
        val nonEventBirthsBySex: List<TotalBySex>
    ) : Parcelable
    @Parcelize
    data class BreedingEvent(
        val maleBreedingInfo: MaleBreedingInfo?,
        val birthingDate: LocalDate?,
        val birthingTime: LocalTime?,
        val birthNotes: String?,
        val birthsBySex: List<TotalBySex>
    ) : Parcelable
    @Parcelize
    data class MaleBreedingInfo(
        val maleBreedingId: EntityId,
        val sireId: EntityId,
        val sireName: String,
        val sireFlockPrefix: String?,
        val dateMaleIn: LocalDate,
        val timeMaleIn: LocalTime,
        val dateMaleOut: LocalDate?,
        val timeMaleOut: LocalTime?,
        val serviceType: ServiceType
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
