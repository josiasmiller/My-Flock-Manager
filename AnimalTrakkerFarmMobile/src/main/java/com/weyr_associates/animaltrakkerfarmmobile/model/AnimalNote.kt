package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalTime

@Parcelize
data class AnimalNote(
    override val id: EntityId,
    val animalId: Int,
    val noteText: String,
    val noteDate: LocalDate,
    val noteTime: LocalTime,
    val predefinedNoteId: EntityId? = null
) : Parcelable, HasIdentity
