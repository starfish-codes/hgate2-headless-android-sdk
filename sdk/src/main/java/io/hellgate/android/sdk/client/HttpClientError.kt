package io.hellgate.android.sdk.client

import io.hellgate.android.sdk.model.Problem

internal data class HttpClientError(
    override val message: String = "",
    override val throwable: Throwable? = null,
) : Throwable(message, throwable), Problem
