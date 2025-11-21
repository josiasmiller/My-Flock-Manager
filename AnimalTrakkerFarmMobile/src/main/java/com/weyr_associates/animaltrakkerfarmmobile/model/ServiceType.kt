package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ServiceType(
    override val id: EntityId,
    override val name: String,
    override val abbreviation: String,
    val order: Int
) : Parcelable, HasIdentity, HasName, HasAbbreviation {
    companion object {
        const val ID_NATURAL_COVER_RAW = "dadd0840-cf4e-4257-867d-7e8a525edcf5" //LEGACY ID = 1
        val ID_NATURAL_COVER = EntityId(ID_NATURAL_COVER_RAW)
    }
}
