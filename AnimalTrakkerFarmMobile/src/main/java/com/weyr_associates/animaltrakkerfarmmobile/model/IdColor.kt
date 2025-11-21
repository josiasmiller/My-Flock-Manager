package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class IdColor(
    override val id: EntityId,
    override val name: String,
    override val abbreviation: String,
    val order: Int
) : Parcelable, HasIdentity, HasName, HasAbbreviation {
    companion object {

        const val ID_COLOR_ID_UNKNOWN_RAW = "150a3eaf-3f93-4e98-8246-d3b26856dfb8" //LEGACY ID = 14
        const val ID_COLOR_ID_NOT_APPLICABLE_RAW = "066e24f7-a1ef-4c7e-b388-f9b0223c8053" //LEGACY ID = 15

        val ID_COLOR_ID_UNKNOWN = EntityId(ID_COLOR_ID_UNKNOWN_RAW)
        val ID_COLOR_ID_NOT_APPLICABLE = EntityId(ID_COLOR_ID_NOT_APPLICABLE_RAW)
    }
}
