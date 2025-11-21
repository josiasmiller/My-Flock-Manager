package com.weyr_associates.animaltrakkerfarmmobile.app.core.select

fun interface ItemDisplayTextProvider<in T> {
    fun displayTextForItem(item: T): String
}

fun interface NoItemDisplayTextProvider {
    fun displayTextForNoItem(): String
}

class ToStringItemDisplayTextProvider<T> : ItemDisplayTextProvider<T> {
    override fun displayTextForItem(item: T): String {
        return item.toString()
    }
}

object EmptyNoItemDisplayTextProvider : NoItemDisplayTextProvider {
    override fun displayTextForNoItem(): String = ""
}
