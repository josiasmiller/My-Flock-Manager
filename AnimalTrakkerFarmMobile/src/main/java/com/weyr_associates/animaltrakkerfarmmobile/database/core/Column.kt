package com.weyr_associates.animaltrakkerfarmmobile.database.core

sealed interface Column {

    val name: String

    fun qualifiedBy(qualifier: String?): Column

    @JvmInline
    value class NotNull(override val name: String) : Column {
        override fun toString(): String {
            return name
        }

        override fun qualifiedBy(qualifier: String?): NotNull {
            return qualifier?.let { NotNull(qualifiedName(it)) } ?: this
        }

        fun toNullable(qualifier: String? = null): Nullable {
            return Nullable(qualifier?.let { qualifiedName(it) } ?: name)
        }
    }

    @JvmInline
    value class Nullable(override val name: String) : Column{
        override fun toString(): String {
            return name
        }

        override fun qualifiedBy(qualifier: String?): Nullable {
            return qualifier?.let { Nullable(qualifiedName(it)) } ?: this
        }

        fun toNotNull(qualifier: String? = null): NotNull {
            return NotNull(qualifier?.let { qualifiedName(it) } ?: name)
        }
    }

    fun qualifiedName(qualifier: String?): String {
        return Sql.applyPrefix(name, qualifier)
    }
}