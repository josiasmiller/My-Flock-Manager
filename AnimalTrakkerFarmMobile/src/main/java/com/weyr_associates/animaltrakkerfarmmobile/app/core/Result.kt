package com.weyr_associates.animaltrakkerfarmmobile.app.core

sealed interface Result<out T, out E> {
    data class Success<out T, out E>(val data: T) : Result<T, E>
    data class Failure<out T, out E>(val error: E) : Result<T, E>
}
