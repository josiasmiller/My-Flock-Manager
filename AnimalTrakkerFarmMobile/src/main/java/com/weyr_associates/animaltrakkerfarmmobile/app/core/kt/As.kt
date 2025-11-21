package com.weyr_associates.animaltrakkerfarmmobile.app.core.kt

inline fun <reified T> Any?.takeAs(): T? {
    return this as? T
}

inline fun <reified T> Any?.requireAs(): T {
    return requireNotNull(this) { "Target object cannot be null." }.run {
        requireNotNull(takeAs<T>()) { "Target object cannot be cast to ${T::class.simpleName}" }
    }
}
