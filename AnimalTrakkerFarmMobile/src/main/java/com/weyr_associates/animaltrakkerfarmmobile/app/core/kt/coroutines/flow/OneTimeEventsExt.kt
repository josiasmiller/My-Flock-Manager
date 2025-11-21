package com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.coroutines.flow

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

suspend fun <T> Flow<T>.observeOneTimeEvents(observer: suspend (T) -> Unit) {
    withContext(Dispatchers.Main.immediate) { collect(observer) }
}
