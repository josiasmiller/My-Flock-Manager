package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GeneticCoatColor(
    override val id: EntityId,
    val registryCompanyId: EntityId,
    val color: String,
    val colorAbbreviation: String,
    val order: Int
) : Parcelable, HasIdentity, HasName, HasAbbreviation {

    override val name: String
        get() = color

    override val abbreviation: String
        get() = colorAbbreviation
}
