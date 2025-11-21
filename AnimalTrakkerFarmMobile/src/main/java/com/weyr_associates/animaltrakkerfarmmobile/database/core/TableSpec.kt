package com.weyr_associates.animaltrakkerfarmmobile.database.core

interface TableSpec<T> {
    val name: String
    val columns: T
    val primaryKeyColumnName: Column.NotNull

    fun prefixWith(prefix: String?): String {
        return prefix?.let { Sql.applyPrefix(name, it) } ?: name
    }

    fun join(
        joinType: JoinType,
        foreignTableName: String,
        foreignColumn: Column,
        tableAliasPrefix: String? = null,
        localColumn: (T.() -> Column)? = null
    ): String {
        return Sql.join(
            joinType,
            name,
            prefixWith(tableAliasPrefix),
            localColumn?.invoke(columns)?.name
                ?: primaryKeyColumnName.name,
            foreignTableName,
            foreignColumn.name
        )
    }

    fun project(
        tableQualifier: String? = null,
        colQualifier: String? = null,
        projector: T.() -> Array<Column>
    ): String {
        return projectIn(
            resultsIdentifier = prefixWith(tableQualifier),
            colQualifier = colQualifier,
            projector = projector
        )
    }

    fun projectIn(
        resultsIdentifier: String,
        colQualifier: String? = null,
        projector: T.() -> Array<Column>
    ): String {
        return buildString {
            val projection = projector(columns)
            projection.forEachIndexed { index, column ->
                append(
                    Sql.columnProjection(
                        tableIdentifier = resultsIdentifier,
                        columnIdentifier = column.name,
                        columnAlias = column.qualifiedName(colQualifier)
                    )
                )
                if (index != projection.lastIndex) {
                    append(","); appendLine()
                }
            }
        }
    }
}

