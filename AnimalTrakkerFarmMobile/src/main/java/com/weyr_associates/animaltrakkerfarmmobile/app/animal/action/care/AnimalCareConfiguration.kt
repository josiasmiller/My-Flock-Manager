package com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AnimalCareConfiguration(
    val hooves: Boolean = false,
    val horns: Boolean = false,
    val shoe: Boolean = false,
    val shear: Boolean = false,
    val wean: Boolean = false,
    val weight: Boolean = false
) : Parcelable
