package com.weyr_associates.animaltrakkerfarmmobile.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period

data class AnimalAge(
    val yearsOfAge: Int,
    val monthsOfAge: Int
)

fun LocalDate.extractAnimalAge(): AnimalAge {
    return extractAnimalAgeOn(LocalDate.now())
}

fun LocalDate.extractAnimalAgeOn(date: LocalDate): AnimalAge {
    return Period.between(this, date).let { periodBetween ->
        AnimalAge(periodBetween.years, periodBetween.months)
    }
}

fun LocalDateTime.extractAnimalAge(): AnimalAge {
    return this.toLocalDate().extractAnimalAge()
}

fun LocalDate.animalBirthDateFrom(ageYears: Int, ageMonths: Int): LocalDate {
    return minusYears(ageYears.toLong())
        .minusMonths(ageMonths.toLong())
        .withDayOfMonth(1)
}

fun LocalDateTime.animalBirthDateFrom(ageYears: Int, ageMonths: Int): LocalDate {
    return this.toLocalDate().animalBirthDateFrom(ageYears, ageMonths)
}
