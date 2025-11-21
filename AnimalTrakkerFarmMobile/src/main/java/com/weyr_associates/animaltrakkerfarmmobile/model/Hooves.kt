package com.weyr_associates.animaltrakkerfarmmobile.model

import java.util.EnumSet

typealias Hooves = EnumSet<Hoof>

fun Hooves.has(other: Hoof) = this.contains(other)
fun Hooves.has(others: Hooves) = this.containsAll(others)
fun Hooves.hasAll() = this.containsAll(Hoof.entries)
fun Hooves.hasNone() = this.isEmpty()

fun Hooves.withHoof(hoof: Hoof): Hooves = Hooves.copyOf(this).apply { add(hoof) }

fun Hooves.withoutHoof(hoof: Hoof): Hooves = Hooves.copyOf(this).apply { remove(hoof) }

enum class Hoof {
    FRONT_LEFT,
    FRONT_RIGHT,
    BACK_LEFT,
    BACK_RIGHT;

    companion object {
        fun all(): Hooves = Hooves.allOf(Hoof::class.java)
        fun none(): Hooves = Hooves.noneOf(Hoof::class.java)
    }

    val abbreviation: String get() = when (this) {
        FRONT_LEFT -> "FL"
        FRONT_RIGHT -> "FR"
        BACK_LEFT -> "BL"
        BACK_RIGHT -> "BR"
    }

    infix fun and(other: Hoof): Hooves = Hooves.of(this, other)
    infix fun and(others: Hooves): Hooves = Hooves.of(this, *others.toTypedArray())
}
