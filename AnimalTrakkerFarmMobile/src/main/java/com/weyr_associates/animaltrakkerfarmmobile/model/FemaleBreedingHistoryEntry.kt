package com.weyr_associates.animaltrakkerfarmmobile.model

import java.time.LocalDate
import java.time.LocalTime

data class FemaleBreedingHistoryEntry(
    val femaleBreedingId: EntityId,
    val animalId: EntityId,
    val birthingNotes: String?,
    val eventDate: LocalDate,
    val eventTime: LocalTime,
) : HasIdentity {
    override val id: EntityId
        get() = femaleBreedingId
}
