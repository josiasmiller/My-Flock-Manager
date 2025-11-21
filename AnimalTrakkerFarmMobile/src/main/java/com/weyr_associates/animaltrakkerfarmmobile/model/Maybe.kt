package com.weyr_associates.animaltrakkerfarmmobile.model

sealed interface Maybe<T>
class Unknown<T> : Maybe<T>
data class Known<T>(val value: T) : Maybe<T>
