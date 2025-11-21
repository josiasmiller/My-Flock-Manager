package com.weyr_associates.animaltrakkerfarmmobile.database.core

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.chrono.IsoChronology
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

object Sql {

    //TODO: There is likely a more performant check to execute here.
    const val DB_PRESENCE_CHECK_SQL =
        "SELECT * FROM animaltrakker_default_settings_table"

    const val QUERY_SQLITE_USER_VERSION =
        """SELECT user_version FROM pragma_user_version"""

    const val QUERY_ANIMALTRAKKER_DB_VERSION =
        """SELECT database_version FROM animaltrakker_metadata_table LIMIT 1"""

    const val UNKNOWN = "Unknown"

    const val ESCAPE_CHAR = '\\'
    const val ESCAPE_CLAUSE = "ESCAPE '$ESCAPE_CHAR'"

    object Columns {
        val COUNT = Column.NotNull("count")
        val PRIORITY = Column.NotNull("priority")
        val YEAR = Column.NotNull("year")
    }

    val FORMAT_DATE: DateTimeFormatter = DateTimeFormatterBuilder()
        .appendValue(ChronoField.YEAR, 4)
        .appendLiteral('-')
        .appendValue(ChronoField.MONTH_OF_YEAR, 2)
        .appendLiteral('-')
        .appendValue(ChronoField.DAY_OF_MONTH, 2)
        .parseStrict()
        .toFormatter()
        .withChronology(IsoChronology.INSTANCE)

    val FORMAT_TIME: DateTimeFormatter = DateTimeFormatterBuilder()
        .appendValue(ChronoField.HOUR_OF_DAY, 2)
        .appendLiteral(':')
        .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
        .appendLiteral(':')
        .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
        .parseStrict()
        .toFormatter()
        .withChronology(IsoChronology.INSTANCE)

    val FORMAT_DATETIME: DateTimeFormatter = DateTimeFormatterBuilder()
        .append(FORMAT_DATE)
        .appendLiteral(' ')
        .append(FORMAT_TIME)
        .parseStrict()
        .toFormatter()
        .withChronology(IsoChronology.INSTANCE)

    val DEFAULT_TIME = LocalTime.of(0, 0, 0)

    const val FLOAT_PRECISION_DEFAULT = 2
    const val FLOAT_PRECISION_FOR_TRAITS = 2

    fun applyPrefix(name: String, prefix: String?): String {
        return prefix?.let { "${it}_${name}" } ?: name
    }

    fun columnProjection(tableIdentifier: String, columnIdentifier: String, columnAlias: String?): String {
        return """${tableIdentifier}.${columnIdentifier} AS ${columnAlias ?: columnIdentifier}"""
    }

    fun join(
        joinType: JoinType,
        tableName: String,
        tableAlias: String,
        columnName: String,
        foreignTableName: String,
        foreignColumnName: String
    ) : String {
        return """
            |${joinType.sqlClause()} $tableName AS $tableAlias
            |ON ${tableAlias}.${columnName} = ${foreignTableName}.${foreignColumnName}
        """.trimMargin()
    }

    /**
     * Escapes SQLite LIKE wildcard characters and
     * the escape character itself.
     *
     * Use for arguments that will determine
     * the pattern for LIKE usages in Sql.
     */
    fun escapeWildcards(string: String): String {
        return string.replace(
            "$ESCAPE_CHAR",
            "$ESCAPE_CHAR$ESCAPE_CHAR"
        ).replace(
            "_",
            "${ESCAPE_CHAR}_"
        ).replace(
            "%",
            "$ESCAPE_CHAR%"
        )
    }

    fun booleanValue(value: Boolean): Int {
        return if (value) 1 else 0
    }

    fun formatDate(date: LocalDate): String {
        return date.format(FORMAT_DATE)
    }

    fun formatDate(dateTime: LocalDateTime): String {
        return dateTime.format(FORMAT_DATE)
    }

    fun formatTime(time: LocalTime): String {
        return time.format(FORMAT_TIME)
    }

    fun formatTime(dateTime: LocalDateTime): String {
        return dateTime.format(FORMAT_TIME)
    }

    fun formatDateTime(dateTime: LocalDateTime): String {
        return dateTime.format(FORMAT_DATETIME)
    }

    fun floatWithPrecision(value: Float, places: Int = FLOAT_PRECISION_DEFAULT): String {
        require(0 < places)
        return String.format("%.${places}f" ,value)
    }

    fun floatForUnitEvalTrait(value: Float) = floatWithPrecision(value, FLOAT_PRECISION_FOR_TRAITS)
    fun optFloatForUnitEvalTrait(value: Float?) = value?.let { floatForUnitEvalTrait(it) }
}