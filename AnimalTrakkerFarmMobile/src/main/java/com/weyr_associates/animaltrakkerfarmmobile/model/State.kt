package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class State(
    override val id: EntityId,
    override val name: String,
    override val abbreviation: String,
    val countryId: EntityId,
    val order: Int
) : Parcelable, HasIdentity, HasName, HasAbbreviation
