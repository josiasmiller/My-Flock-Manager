package com.weyr_associates.animaltrakkerfarmmobile.database.core

enum class JoinType {
    INNER,
    OUTER_LEFT;

    fun sqlClause(): String = when (this) {
        INNER -> "INNER JOIN"
        OUTER_LEFT -> "LEFT OUTER JOIN"
    }
}
