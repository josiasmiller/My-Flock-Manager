package com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.coroutines

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll

suspend fun <A, B, R> awaitAll(
    a: Deferred<A>,
    b: Deferred<B>,
    transform: suspend (A, B) -> R): R {
    awaitAll(a, b)
    return transform(
        a.await(),
        b.await()
    )
}

suspend fun <A, B, C, R> awaitAll(
    a: Deferred<A>,
    b: Deferred<B>,
    c: Deferred<C>,
    transform: suspend (A, B, C) -> R): R {
    awaitAll(a, b, c)
    return transform(
        a.await(),
        b.await(),
        c.await()
    )
}

suspend fun <A, B, C, D, R> awaitAll(
    a: Deferred<A>,
    b: Deferred<B>,
    c: Deferred<C>,
    d: Deferred<D>,
    transform: suspend (A, B, C, D) -> R): R {
    awaitAll(a, b, c, d)
    return transform(
        a.await(),
        b.await(),
        c.await(),
        d.await()
    )
}

suspend fun <A, B, C, D, E, R> awaitAll(
    a: Deferred<A>,
    b: Deferred<B>,
    c: Deferred<C>,
    d: Deferred<D>,
    e: Deferred<E>,
    transform: suspend (A, B, C, D, E) -> R): R {
    awaitAll(a, b, c, d, e)
    return transform(
        a.await(),
        b.await(),
        c.await(),
        d.await(),
        e.await()
    )
}

suspend fun <A, B, C, D, E, F, R> awaitAll(
    a: Deferred<A>,
    b: Deferred<B>,
    c: Deferred<C>,
    d: Deferred<D>,
    e: Deferred<E>,
    f: Deferred<F>,
    transform: suspend (A, B, C, D, E, F) -> R): R {
    awaitAll(a, b, c, d, e, f)
    return transform(
        a.await(),
        b.await(),
        c.await(),
        d.await(),
        e.await(),
        f.await()
    )
}
