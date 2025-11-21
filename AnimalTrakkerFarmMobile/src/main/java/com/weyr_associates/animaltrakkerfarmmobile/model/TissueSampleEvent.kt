package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalTime

@Parcelize
data class TissueSampleEvent(
    override val id: EntityId,
    val animalId: Int,
    val tissueSampleTypeId: Int,
    val tissueSampleName: String,
    val eventDate: LocalDate,
    val eventTime: LocalTime?
) : Parcelable, HasIdentity
