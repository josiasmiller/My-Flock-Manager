package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Drug(
    override val id: EntityId,
    val typeId: EntityId,
    val tradeName: String,
    val genericName: String,
    val isRemovable: Boolean
) : Parcelable, HasIdentity, HasName {

    override val name: String
        get() = tradeName
}
