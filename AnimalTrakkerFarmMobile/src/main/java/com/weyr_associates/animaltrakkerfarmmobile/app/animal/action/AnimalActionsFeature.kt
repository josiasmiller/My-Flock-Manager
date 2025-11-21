package com.weyr_associates.animaltrakkerfarmmobile.app.animal.action

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class AnimalActionsFeature : Parcelable {
    VACCINES_AND_DEWORMERS,
    ADMINISTER_DRUGS,
    GENERAL_ANIMAL_CARE
}