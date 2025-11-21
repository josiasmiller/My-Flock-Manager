package com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.coroutines.flow

import com.weyr_associates.animaltrakkerfarmmobile.app.core.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

@Suppress("USELESS_CAST")
fun <T> Flow<T>.mapToResult(): Flow<Result<T, Throwable>> {
    return map { Result.Success<T, Throwable>(it) as Result<T, Throwable>}
        .catch { emit(Result.Failure(it)) }
}

@Suppress("USELESS_CAST")
fun <T, R> Flow<T>.mapToResult(errorProducer: (Throwable) -> R): Flow<Result<T, R>> {
    return map { Result.Success<T, R>(it) as Result<T, R> }
        .catch { emit(Result.Failure(errorProducer.invoke(it))) }
}
