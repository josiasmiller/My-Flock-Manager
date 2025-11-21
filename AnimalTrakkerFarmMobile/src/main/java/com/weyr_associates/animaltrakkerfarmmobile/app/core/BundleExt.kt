package com.weyr_associates.animaltrakkerfarmmobile.app.core

import android.os.Bundle
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.requireAs
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId

fun Bundle.putEntityId(key: String, value: EntityId?) {
    putParcelable(key, value)
}

fun Bundle.putEntityIdSet(key: String, value: Set<EntityId>) {
    putParcelableArray(key, value.toTypedArray())
}

fun Bundle.getEntityId(key: String): EntityId {
    return requireNotNull(getParcelable(key))
}

fun Bundle.getEntityId(key: String, defValue: EntityId): EntityId {
    return getParcelable(key) ?: defValue
}

fun Bundle.getEntityIdSet(key: String): Set<EntityId> {
    return getParcelableArray(key)
        ?.map { it.requireAs<EntityId>() }
        ?.toSet() ?: emptySet()
}
