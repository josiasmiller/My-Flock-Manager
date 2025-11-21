package com.weyr_associates.animaltrakkerfarmmobile.model

import java.time.LocalDate
import java.time.LocalTime

data class AnimalEvaluation(
    override val id: EntityId,
    val animalId: Int,
    val traits: List<Entry>,
    val animalRank: Rank?,
    val evalDate: LocalDate,
    val evalTime: LocalTime,
) : HasIdentity {

    sealed interface Entry : HasIdentity {
        override val id get() = traitId
        val traitId: EntityId
        val traitName: String
    }

    data class ScoreEntry(
        override val traitId: EntityId,
        override val traitName: String,
        val traitScore: Int
    ) : Entry

    data class UnitsEntry(
        override val traitId: EntityId,
        override val traitName: String,
        val traitScore: Float,
        val unitsId: Int,
        val unitsAbbreviation: String
    ) : Entry

    data class OptionEntry(
        override val traitId: EntityId,
        override val traitName: String,
        val optionId: Int,
        val optionName: String
    ) : Entry

    data class Rank(
        val rank: Int,
        val numberRanked: Int
    ) {
        companion object {
            fun from(
                rank: Int?,
                numberRanked: Int?
            ): Rank? {
                return if (rank == null || numberRanked == null) {
                    null
                } else {
                    Rank(rank = rank, numberRanked = numberRanked)
                }
            }
        }
    }
}
