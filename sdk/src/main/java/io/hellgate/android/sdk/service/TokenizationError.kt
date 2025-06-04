package io.hellgate.android.sdk.service

import io.hellgate.android.sdk.model.Problem

internal data class TokenizationError(
    override val message: String,
    override val throwable: Throwable? = null,
) : Problem {
    companion object {
        fun fromErrorResponse(
            reason: String,
            reasonCode: String?,
        ) = UnexpectedHellgateResponse("Tokenization failed: $reason" + if (reasonCode != null) ", reasonCode: $reasonCode" else "")
    }
}
