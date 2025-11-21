package com.weyr_associates.animaltrakkerfarmmobile.app.core

import android.content.Intent
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.requireAs
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId

fun Intent.putExtra(name: String, value: Set<EntityId>): Intent {
    putExtra(name, value.toTypedArray())
    return this
}

fun Intent.getEntityIdSet(name: String): Set<EntityId> {
    return getParcelableArrayExtra(name)
        ?.map { it.requireAs<EntityId>() }
        ?.toSet() ?: emptySet()
}
