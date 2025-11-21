package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalTime

@Parcelize
data class FemaleBreeding(
    override val id: EntityId,
    val animalId: EntityId,
    val birthingDate: LocalDate?,
    val birthingTime: LocalTime?,
    val birthingNotes: String,
    val numberOfAnimalsBorn: Int,
    val numberOfAnimalsWeaned: Int,
    val gestationLength: Int?,
    val maleBreeding: MaleBreeding?
) : Parcelable, HasIdentity
