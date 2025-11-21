package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class SexStandard(val code: String): Parcelable {
    UNKNOWN("U"),
    MALE("M"),
    FEMALE("F"),
    CASTRATE("C")
}
