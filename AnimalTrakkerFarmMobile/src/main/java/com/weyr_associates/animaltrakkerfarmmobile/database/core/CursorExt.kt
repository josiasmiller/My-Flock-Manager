@file:JvmName("Cursors")

package com.weyr_associates.animaltrakkerfarmmobile.database.core

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

/**
 * Retrieves an entity id value from the [Cursor] based on column index.
 * Should only be called with columns that are guaranteed to be non-null.
 *
 * [EntityId] corresponds to the standard Primary Key type used throughout the database.
 *
 * @param columnIndex The column index from which to retrieve an entity id value.
 * @return The entity id value for the given [columnIndex]
 */
private fun Cursor.getEntityId(columnIndex: Int): EntityId {
    val uuidStr = getString(columnIndex)
    return EntityId(UUID.fromString(uuidStr))
}

/**
 * Retrieves an entity id value from the [Cursor] based on column name.
 * Should only be called with columns that are guaranteed to be non-null.
 *
 * [EntityId] corresponds to the standard Primary Key type used throughout the database.
 *
 * @param columnName The column name from which to retrieve an entity id value.
 * @return The entity id value for the given [columnName]
 */
private fun Cursor.getEntityId(columnName: String): EntityId {
    return getEntityId(getColumnIndexOrThrow(columnName))
}

fun Cursor.getEntityId(column: Column.NotNull): EntityId {
    return getEntityId(column.name)
}

/**
 * Retrieves an entity id value from the [Cursor] based on column name.
 * Can be called for columns that allow null values.
 *
 * @param columnName The column name from which to retrieve an entity id value.
 * @return The entity id value for the given [columnName], or null if the
 * underlying value is null.
 */
private fun Cursor.getOptEntityId(columnName: String): EntityId? {
    return nullOrValueAtIndex(columnName) { columnIndex -> getEntityId(columnIndex) }
}

fun Cursor.getOptEntityId(column: Column): EntityId? {
    return getOptEntityId(column.name)
}

/**
 * Treats underlying integer values as booleans.
 *
 * @param columnIndex The index of the column from which to retrieve a boolean value.
 *
 * @return True if the underlying integer is greater than 0; false otherwise.
 */
private fun Cursor.getBoolean(columnIndex: Int): Boolean = 0 < getInt(columnIndex)

/**
 * Retrieves a boolean value from the [Cursor] based on column name.  Relies
 * on an underlying integral value in the column.  Should only be called with
 * columns that are guaranteed to be non-null.
 *
 * @param columnName The column name from which to retrieve a boolean value.
 * @return True if the underlying integer is greater than 0; false otherwise.
 */
private fun Cursor.getBoolean(columnName: String): Boolean =
    getBoolean(getColumnIndexOrThrow(columnName))

fun Cursor.getBoolean(column: Column.NotNull): Boolean =
    getBoolean(column.name)

fun Cursor.getOptBoolean(column: Column): Boolean? {
    return getOptBoolean(column.name)
}

/**
 * Retrieves a boolean value from the [Cursor] based on column name.  Relies
 * on an underlying integral value in the column.  Can be called for columns
 * that allow null values.
 *
 * @param columnName The column name from which to retrieve a boolean value.
 * @return True if the underlying integer is greater than 0; false if it is less
 * than or equal to 0, and null if the underlying value is null.
 */
private fun Cursor.getOptBoolean(columnName: String): Boolean? =
    nullOrValueAtIndex(columnName) { columnIndex -> getBoolean(columnIndex) }

/**
 * Retrieves an integer value from the [Cursor] based on column name.
 * Should only be called with columns that are guaranteed to be non-null.
 *
 * @param columnName The column name from which to retrieve an integer value.
 * @return The integer value for the given [columnName]
 */
private fun Cursor.getInt(columnName: String): Int =
    getInt(getColumnIndexOrThrow(columnName))

fun Cursor.getInt(column: Column.NotNull): Int = getInt(column.name)

/**
 * Retrieves an integer value from the [Cursor] based on column name.
 * Can be called for columns that allow null values.
 *
 * @param columnName The column name from which to retrieve an integer value.
 * @return The integer value for the given [columnName], or null if the
 * underlying value is null.
 */
private fun Cursor.getOptInt(columnName: String): Int? =
    nullOrValueAtIndex(columnName) { columnIndex -> getInt(columnIndex) }

fun Cursor.getOptInt(column: Column): Int? {
    return getOptInt(column.name)
}

/**
 * Retrieves an long value from the [Cursor] based on column name.
 * Should only be called with columns that are guaranteed to be non-null.
 *
 * @param columnName The column name from which to retrieve an long value.
 * @return The long value for the given [columnName]
 */
private fun Cursor.getLong(columnName: String): Long =
    getLong(getColumnIndexOrThrow(columnName))

/**
 * Retrieves an long value from the [Cursor] based on column name.
 * Can be called for columns that allow null values.
 *
 * @param columnName The column name from which to retrieve an long value.
 * @return The long value for the given [columnName], or null if the
 * underlying value is null.
 */
private fun Cursor.getOptLong(columnName: String): Long? =
    nullOrValueAtIndex(columnName) { columnIndex -> getLong(columnIndex) }

/**
 * Retrieves a float value from the [Cursor] based on column name.
 * Should only be called with columns that are guaranteed to be non-null.
 *
 * @param columnName The column name from which to retrieve a float value.
 * @return The float value for the given [columnName]
 */
private fun Cursor.getFloat(columnName: String): Float =
    getFloat(getColumnIndexOrThrow(columnName))

fun Cursor.getFloat(column: Column.NotNull): Float =
    getFloat(column.name)

/**
 * Retrieves a float value from the [Cursor] based on column name.
 * Can be called for columns that allow null values.
 *
 * @param columnName The column name from which to retrieve an float value.
 * @return The float value for the given [columnName], or null if the
 * underlying value is null.
 */
private fun Cursor.getOptFloat(columnName: String): Float? =
    nullOrValueAtIndex(columnName) { columnIndex -> getFloat(columnIndex) }

fun Cursor.getOptFloat(column: Column): Float? {
    return getOptFloat(column.name)
}

/**
 * Retrieves a string value from the [Cursor] based on column name.
 * Should only be called with columns that are guaranteed to be non-null.
 *
 * @param columnName The column name from which to retrieve a string value.
 * @return The string value for the given [columnName]
 */
private fun Cursor.getString(columnName: String): String =
    getString(getColumnIndexOrThrow(columnName))

fun Cursor.getString(column: Column.NotNull): String =
    getString(column.name)

fun Cursor.getOptString(column: Column): String? {
    return getOptString(column.name)
}

private fun Cursor.getLocalDate(columnName: String): LocalDate {
    return LocalDate.parse(getString(columnName), Sql.FORMAT_DATE)
}

fun Cursor.getLocalDate(column: Column.NotNull): LocalDate {
    return getLocalDate(column.name)
}

private fun Cursor.getOptLocalDate(columnName: String): LocalDate? {
    return if (!isNull(columnName)) {
        getString(columnName).takeIf { it.isNotBlank() }
            ?.let { LocalDate.parse(it, Sql.FORMAT_DATE) }
    } else { null }
}

fun Cursor.getOptLocalDate(column: Column): LocalDate? {
    return getOptLocalDate(column.name)
}

private fun Cursor.getLocalTime(columnName: String): LocalTime {
    return if (isNull(columnName)) Sql.DEFAULT_TIME
    else getString(columnName).takeIf { it.isNotEmpty() }
        ?.let { LocalTime.parse(it, Sql.FORMAT_TIME) } ?: Sql.DEFAULT_TIME
}

fun Cursor.getLocalTime(column: Column.NotNull): LocalTime {
    return getLocalTime(column.name)
}

private fun Cursor.getOptLocalTime(columnName: String): LocalTime? {
    return if (!isNull(columnName)) {
        getString(columnName).takeIf { it.isNotBlank() }
            ?.let { LocalTime.parse(it, Sql.FORMAT_TIME) }
    } else { null }
}

fun Cursor.getOptLocalTime(column: Column): LocalTime? {
    return getOptLocalTime(column.name)
}

private fun Cursor.getLocalDateTime(columnName: String): LocalDateTime {
    return LocalDateTime.parse(getString(columnName), Sql.FORMAT_DATETIME)
}

/**
 * Retrieves a string value from the [Cursor] based on column name.
 * Can be called for columns that allow null values.
 *
 * @param columnName The column name from which to retrieve an string value.
 * @return The string value for the given [columnName], or null if the
 * underlying value is null.
 */
private fun Cursor.getOptString(columnName: String): String? =
    nullOrValueAtIndex(columnName) { columnIndex -> getString(columnIndex) }

/**
 * Indicates whether or not the specified column
 * contains a null value.
 *
 * @param columnName The column name to check for null values.
 * @return true if the column contains a null value, false if
 * it does not.
 */
private fun Cursor.isNull(columnName: String): Boolean {
    return isNull(getColumnIndexOrThrow(columnName))
}

fun Cursor.isNull(column: Column): Boolean {
    return isNull(column.name)
}

fun Cursor.isNotNull(column: Column): Boolean {
    return !isNull(column.name)
}

/**
 * Converts column name to index and checks if the column is null.
 * This is a private helper to reduce boiler plate for providing
 * optional value lookup in [Cursor]s.
 *
 * @param columnName The name of the column in question.
 *
 * @param accessor A block given the resulting index of the column
 * to look up a value in the case a value is present.
 *
 * @return null if column is null, otherwise result of [accessor]
 */
private fun <T> Cursor.nullOrValueAtIndex(columnName: String, accessor: Cursor.(Int) -> T): T? {
    val columnIndex = getColumnIndexOrThrow(columnName)
    return if (!isNull(columnIndex)) accessor.invoke(this, columnIndex) else null
}

fun <T> Cursor.readAllItems(itemReader: (Cursor) -> T): List<T> {
    return takeIf { it.moveToFirst() }?.readList(itemReader) ?: emptyList()
}

fun <T> Cursor.readFirstItem(itemReader: (Cursor) -> T): T? {
    return takeIf { it.moveToFirst() }?.readItem(itemReader)
}

fun <T> Cursor.readList(itemReader: (Cursor) -> T): List<T> {
    assert(!isBeforeFirst && !isAfterLast)
    return buildList {
        do {
            add(readItem(itemReader))
        }
        while (moveToNext())
    }
}

fun <T> Cursor.readItem(itemReader: (Cursor) -> T): T {
    assert(!isBeforeFirst && !isAfterLast)
    return itemReader.invoke(this)
}
