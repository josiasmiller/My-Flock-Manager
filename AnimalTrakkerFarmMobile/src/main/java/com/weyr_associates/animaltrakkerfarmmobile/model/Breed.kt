package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Breed(
    override val id: EntityId,
    override val name: String,
    override val abbreviation: String,
    val order: Int,
    val speciesId: EntityId
) : Parcelable, HasIdentity, HasName, HasAbbreviation {
    companion object {
        val ID_UNKNOWN_BREED_RAW = "159efdfe-d990-472b-867e-df4493431aef" //LEGACY ID = 6
        val ID_UNKNOWN_BREED = EntityId(ID_UNKNOWN_BREED_RAW)
    }
}
