package com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.coroutines

import kotlin.coroutines.cancellation.CancellationException

@Suppress("NOTHING_TO_INLINE")
inline fun Exception.rethrowIfCancellation() {
    if (this is CancellationException) {
        throw this
    }
}
