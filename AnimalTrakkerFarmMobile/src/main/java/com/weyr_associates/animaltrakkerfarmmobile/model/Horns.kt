package com.weyr_associates.animaltrakkerfarmmobile.model

import java.util.EnumSet

typealias Horns = EnumSet<Horn>

fun Horns.has(other: Horn) = this.contains(other)
fun Horns.has(others: Horns) = this.containsAll(others)
fun Horns.hasAll() = this.containsAll(Horn.entries)
fun Horns.hasNone() = this.isEmpty()

fun Horns.withHorn(horn: Horn): Horns = Horns.copyOf(this).apply { add(horn) }

fun Horns.withoutHorn(horn: Horn): Horns = Horns.copyOf(this).apply { remove(horn) }

enum class Horn {
    LEFT,
    RIGHT;

    companion object {
        fun all(): Horns = Horns.allOf(Horn::class.java)
        fun none(): Horns = Horns.noneOf(Horn::class.java)
    }

    val abbreviation: String get() = when (this) {
        LEFT -> "L"
        RIGHT -> "R"
    }

    infix fun and(other: Horn): Horns = Horns.of(this, other)
    infix fun and(others: Horns): Horns = Horns.of(this, *others.toTypedArray())
}
