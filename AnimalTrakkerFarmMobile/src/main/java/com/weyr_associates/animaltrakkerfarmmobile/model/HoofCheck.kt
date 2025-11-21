package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HoofCheck(
    val trimmed: Hooves = Hoof.all(),
    val withFootRotObserved: Hooves = Hoof.none(),
    val withFootScaldObserved: Hooves = Hoof.none()
) : Parcelable
