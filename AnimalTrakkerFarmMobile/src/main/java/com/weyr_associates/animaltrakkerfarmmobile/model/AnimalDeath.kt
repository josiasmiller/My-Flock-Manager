package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class AnimalDeath(
    val date: LocalDate,
    val reasonId: Int?,
    val reason: String?
) : Parcelable
