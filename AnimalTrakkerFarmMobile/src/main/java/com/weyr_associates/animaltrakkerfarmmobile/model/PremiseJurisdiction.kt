package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PremiseJurisdiction(
    override val id: EntityId,
    override val name: String,
    val order: Int
) : Parcelable, HasIdentity, HasName
