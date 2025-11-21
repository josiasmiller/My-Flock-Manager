package com.weyr_associates.animaltrakkerfarmmobile.model

import java.time.LocalDate

sealed interface AnimalLocationEvent

data class BirthEvent(val date: LocalDate, val premise: Premise?) : AnimalLocationEvent

data class DeathEvent(val date: LocalDate, val premise: Premise?) : AnimalLocationEvent

data class MovementEvent(
    val movement: AnimalMovement,
    val chronology: Chronology
) : AnimalLocationEvent {

    companion object {
        private val LEGAL_PREMISE_TYPES = arrayOf(
            Premise.Type.ID_PHYSICAL,
            Premise.Type.ID_BOTH
        )
    }

    enum class Chronology {
        IN_LIFE_TIME,
        BEFORE_BIRTH,
        AFTER_DEATH
    }

    val hasIssues: Boolean
        get() = !isInAnimalLifetime ||
                isNonPhysicalPremise ||
                isMissingPremise

    val isInAnimalLifetime: Boolean
        get() = chronology == Chronology.IN_LIFE_TIME

    val isMissingPremise: Boolean
        get() = movement.toPremise == null

    val isNonPhysicalPremise: Boolean
        get() = movement.toPremise?.let { it.type.id !in LEGAL_PREMISE_TYPES } == true

    fun applyNicknames(fromPremiseNickname: String?, toPremiseNickname: String?): MovementEvent {
        return copy(
            movement = movement.copy(
                fromPremise = movement.fromPremise?.copy(nickname = fromPremiseNickname),
                toPremise = movement.toPremise?.copy(nickname = toPremiseNickname)
            )
        )
    }
}

data class Gap(
    val previousMovement: AnimalMovement,
    val nextMovement: AnimalMovement
) : AnimalLocationEvent {
    companion object {
        fun extractGapBetween(movement1: AnimalMovement, movement2: AnimalMovement): Gap? {
            return if (movement1.toPremise?.id != movement2.fromPremise?.id) {
                Gap(
                    previousMovement = movement1,
                    nextMovement = movement2
                )
            } else  null
        }
    }
}
