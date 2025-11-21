package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class AnimalLifetime(
    val birthDate: LocalDate?,
    val death: AnimalDeath?
) : Parcelable
