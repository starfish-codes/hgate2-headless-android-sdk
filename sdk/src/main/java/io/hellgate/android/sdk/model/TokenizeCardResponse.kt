package io.hellgate.android.sdk.model

import io.hellgate.android.sdk.handler.CardDataValidationError

sealed class TokenizeCardResponse {
    data class Success(
        val id: String,
    ) : TokenizeCardResponse()

    data class Failure(
        val message: String,
        val validationErrors: List<CardDataValidationError> = emptyList(),
    ) : TokenizeCardResponse()
}
