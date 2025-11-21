package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DrugLocation(
    override val id: EntityId,
    override val name: String,
    override val abbreviation: String,
    val order: Int
) : Parcelable, HasIdentity, HasName, HasAbbreviation
