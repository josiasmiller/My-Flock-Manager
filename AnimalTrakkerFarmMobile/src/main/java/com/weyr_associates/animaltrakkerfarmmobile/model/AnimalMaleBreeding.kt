package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalTime

@Parcelize
data class MaleBreeding(
    override val id: EntityId,
    val animalId: EntityId,
    val dateIn: LocalDate,
    val timeIn: LocalTime,
    val dateOut: LocalDate?,
    val timeOut: LocalTime?,
    val serviceType: ServiceType
) : Parcelable, HasIdentity
