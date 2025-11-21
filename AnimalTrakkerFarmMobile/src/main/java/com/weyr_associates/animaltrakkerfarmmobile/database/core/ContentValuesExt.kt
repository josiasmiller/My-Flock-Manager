package com.weyr_associates.animaltrakkerfarmmobile.database.core

import android.content.ContentValues
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId

fun ContentValues.put(key: String, value: EntityId) {
    put(key, value.raw.toString())
}

fun ContentValues.put(column: Column.NotNull, value: EntityId) {
    put(column.name, value)
}

fun ContentValues.put(column: Column.Nullable, value: EntityId?) {
    value?.let { put(column.name, it) } ?: putNull(column.name)
}

fun ContentValues.put(column: Column.NotNull, value: String) {
    put(column.name, value)
}

fun ContentValues.put(column: Column.Nullable, value: String?) {
    value?.let { put(column.name, value) } ?: putNull(column.name)
}

fun ContentValues.put(column: Column.NotNull, value: Int) {
    put(column.name, value)
}

fun ContentValues.put(column: Column.Nullable, value: Int?) {
    value?.let { put(column.name, it) } ?: putNull(column.name)
}

fun ContentValues.put(column: Column.NotNull, value: Long) {
    put(column.name, value)
}

fun ContentValues.put(column: Column.Nullable, value: Long?) {
    value?.let { put(column.name, it) } ?: putNull(column.name)
}

fun ContentValues.put(column: Column.NotNull, value: Float) {
    put(column.name, value)
}

fun ContentValues.put(column: Column.Nullable, value: Float?) {
    value?.let { put(column.name, it) } ?: putNull(column.name)
}

fun ContentValues.put(column: Column.NotNull, value: Boolean) {
    put(column.name, Sql.booleanValue(value))
}

fun ContentValues.put(column: Column.Nullable, value: Boolean?) {
    value?.let { put(column.name, Sql.booleanValue(it)) } ?: putNull(column.name)
}

fun ContentValues.putNull(column: Column.Nullable) {
    putNull(column.name)
}
