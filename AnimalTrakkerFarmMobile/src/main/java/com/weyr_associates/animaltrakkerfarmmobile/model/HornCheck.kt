package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HornCheck(
    val badHorns: Horns = Horn.none(),
    val sawedHorns: Horns = Horn.none()
) : Parcelable
