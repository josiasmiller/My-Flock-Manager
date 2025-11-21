package com.weyr_associates.animaltrakkerfarmmobile.app.core

import android.content.SharedPreferences
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId

fun SharedPreferences.getEntityId(key: String): EntityId {
    return EntityId(requireNotNull(getString(key, EntityId.UNKNOWN_RAW)))
}

fun SharedPreferences.getEntityId(key: String, defValue: EntityId): EntityId {
    return if (contains(key)) { getEntityId(key) } else defValue
}

fun SharedPreferences.Editor.putEntityId(key: String, value: EntityId): SharedPreferences.Editor {
    putString(key, value.raw.toString())
    return this
}
