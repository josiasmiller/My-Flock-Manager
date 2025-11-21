package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Parcelize
data class AnimalBasicInfo(
    override val id: EntityId,
    override val name: String,
    val flockPrefix: String?,
    val ownerName: String?,
    val ids: List<IdInfo> = emptyList(),
    val speciesId: EntityId,
    val speciesCommonName: String,
    val breedId: EntityId,
    val breedName: String,
    val breedAbbreviation: String,
    val breedPercentage: Float,
    val sexId: EntityId,
    val sexName: String,
    val sexAbbreviation: String,
    val sexStandardName: String,
    val sexStandardAbbreviation: String,
    val birthDate: LocalDate?,
    val weanedDate: LocalDate? = null,
    val deathDate: LocalDate? = null,
    val alerts: List<AnimalAlert> = emptyList()
) : Parcelable, HasIdentity, HasName {

    @IgnoredOnParcel
    val isWeaned: Boolean get() = weanedDate != null

    @IgnoredOnParcel
    val isDead: Boolean get() = deathDate != null

    @IgnoredOnParcel
    val animalAge: AnimalAge? by lazy {
        deathDate?.let { birthDate?.extractAnimalAgeOn(it) }
            ?: birthDate?.extractAnimalAge()
    }

    fun ageInDays(): Long {
        return ChronoUnit.DAYS.between(birthDate, LocalDate.now())
    }

    fun ageInYears(): Long {
        return ChronoUnit.YEARS.between(birthDate, LocalDate.now())
    }
}
