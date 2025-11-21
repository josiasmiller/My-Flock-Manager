package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FlockPrefix(
    override val id: EntityId,
    override val name: String,
    val ownerId: EntityId,
    val ownerType: Owner.Type,
    val registryCompanyId: EntityId
): Parcelable, HasIdentity, HasName
