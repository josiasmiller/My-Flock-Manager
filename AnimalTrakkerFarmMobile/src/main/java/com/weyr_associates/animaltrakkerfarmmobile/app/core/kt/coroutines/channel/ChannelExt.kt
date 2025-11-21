package com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.coroutines.channel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

fun <T> Channel<T>.sendIn(scope: CoroutineScope, value: T) {
    scope.launch { send(value) }
}
