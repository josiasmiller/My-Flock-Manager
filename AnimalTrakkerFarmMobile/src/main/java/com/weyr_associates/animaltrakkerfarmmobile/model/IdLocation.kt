package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class IdLocation(
    override val id: EntityId,
    override val name: String,
    override val abbreviation: String,
    val order: Int
) : Parcelable, HasIdentity, HasName, HasAbbreviation {
    companion object {
        const val ID_LOCATION_ID_UNKNOWN_RAW = "f022b548-7daf-470a-9299-a61be69d059e" //LEGACY ID = 6
        val ID_LOCATION_ID_UNKNOWN = EntityId(ID_LOCATION_ID_UNKNOWN_RAW)
    }
}
