package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RearType(
    override val id: EntityId,
    override val name: String,
    val abbreviation: String,
    val order: Int
) : Parcelable, HasIdentity, HasName {
    companion object {

        const val ID_UNKNOWN_RAW = "7585ea2e-dcdf-41cb-94c1-4d133d624c1e" //LEGACY ID = 42
        val ID_UNKNOWN = EntityId(ID_UNKNOWN_RAW)

        const val REAR_TYPE_MISSING = "Missing Rear Type"
    }
}
