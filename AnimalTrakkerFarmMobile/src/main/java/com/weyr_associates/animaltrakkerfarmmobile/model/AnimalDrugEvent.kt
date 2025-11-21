package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalTime

@Parcelize
data class AnimalDrugEvent(
    override val id: EntityId,
    val animalId: Int,
    val drugId: Int,
    val eventDate: LocalDate,
    val eventTime: LocalTime?,
    val tradeDrugName: String,
    val drugLot: String
) : Parcelable, HasIdentity

