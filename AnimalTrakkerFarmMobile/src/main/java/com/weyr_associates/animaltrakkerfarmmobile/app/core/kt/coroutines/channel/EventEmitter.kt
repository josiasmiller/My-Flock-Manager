package com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.coroutines.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

fun <T> ViewModel.eventEmitter(): EventEmitter<T> {
    return EventEmitter(viewModelScope)
}

class EventEmitter<T>(private val coroutineScope: CoroutineScope) {

    private val eventChannel = Channel<T>()
    val events = eventChannel.receiveAsFlow()

    fun emit(event: T) {
        coroutineScope.launch { eventChannel.send(event) }
    }

    fun forwardFrom(flow: Flow<T>) {
        coroutineScope.launch {
            flow.collectLatest { eventChannel.send(it) }
        }
    }
}
