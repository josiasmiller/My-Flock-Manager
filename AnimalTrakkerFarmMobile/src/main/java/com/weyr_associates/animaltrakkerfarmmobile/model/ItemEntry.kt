package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ItemEntry(
    override val id: EntityId,
    override val name: String
) : Parcelable, HasIdentity, HasName
