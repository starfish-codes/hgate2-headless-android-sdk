package io.hellgate.android.sdk.testutil

import arrow.core.Either
import org.assertj.core.api.Assertions

@Suppress("UnsafeCallOnNullableType", "UNCHECKED_CAST")
fun <A, B> Either<A, B>.assertLeft(action: (left: A) -> Unit = {}): A {
    Assertions.assertThat(this.isLeft()).withFailMessage("Expected Left but was: $this").isTrue()
    this.onLeft(action)
    return this.leftOrNull() as A
}

@Suppress("UnsafeCallOnNullableType", "UNCHECKED_CAST")
fun <A, B> Either<A, B>.assertRight(action: (right: B) -> Unit = {}): B {
    Assertions.assertThat(this.isRight()).withFailMessage("Expected Right but was: $this").isTrue()
    this.onRight(action)
    return this.getOrNull() as B
}

@Suppress("UnsafeCallOnNullableType", "UNCHECKED_CAST")
fun <A> Result<A>.assertSuccess(action: (right: A) -> Unit = {}): A {
    Assertions.assertThat(this.isSuccess).withFailMessage("Expected Success but was: $this").isTrue()
    this.onSuccess(action)
    return this.getOrNull() as A
}

fun <A> Result<A>.assertFailure(action: (left: Throwable) -> Unit = {}): Throwable {
    Assertions.assertThat(this.isFailure).withFailMessage("Expected Failure but was: $this").isTrue()
    this.onFailure(action)
    return this.exceptionOrNull()!!
}
