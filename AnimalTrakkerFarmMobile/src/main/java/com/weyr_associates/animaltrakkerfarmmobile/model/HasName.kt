package com.weyr_associates.animaltrakkerfarmmobile.model

interface HasName {
    val name: String
}

fun <T> List<T>.filterByName(filterText: String): List<T> where T : HasName {
    return filter { it.name.lowercase().contains(filterText.lowercase()) }
}
