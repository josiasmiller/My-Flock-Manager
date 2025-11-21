package com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.coroutines.flow.observeOneTimeEvents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

fun LifecycleOwner.launchRepeatingOnStart(block: suspend CoroutineScope.() -> Unit): Job {
    return lifecycleScope.launch { repeatOnLifecycle(Lifecycle.State.STARTED, block) }
}

fun <T> LifecycleOwner.collectLatestOnStart(flow: Flow<T>, block: suspend (T) -> Unit): Job {
    return launchRepeatingOnStart { flow.collectLatest(block) }
}

fun <T> LifecycleOwner.observeOneTimeEventsOnStart(events: Flow<T>, block: suspend (T) -> Unit): Job {
    return launchRepeatingOnStart { events.observeOneTimeEvents(block) }
}
