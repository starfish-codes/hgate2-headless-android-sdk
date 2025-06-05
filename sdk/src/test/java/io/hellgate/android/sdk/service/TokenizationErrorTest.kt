package io.hellgate.android.sdk.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import org.junit.Test

class TokenizationErrorTest {
    @Test
    fun testTokenizationErrorCreation() {
        val errorMessage = "Tokenization failed"
        val error = TokenizationError(errorMessage)

        assertThat(errorMessage).isEqualTo(error.message)
        assertThat(error.throwable).isNull()
    }

    @Test
    fun testTokenizationErrorWithThrowable() {
        val errorMessage = "Tokenization failed with exception"
        val throwable = Exception("Network error")
        val error = TokenizationError(errorMessage, throwable)

        assertThat(error.message).isEqualTo(errorMessage)
        assertThat(error.throwable).isEqualTo(throwable)
    }

    @Test
    fun testFromErrorResponse() {
        val reason = "Invalid card details"
        val reasonCode = "400"
        val error = TokenizationError.fromErrorResponse(reason, reasonCode)

        assertThat(error).isInstanceOf(TokenizationError::class)
        assertThat(error.message).isEqualTo("Tokenization failed: $reason, reasonCode: $reasonCode")
        assertThat(error.throwable).isNull()
    }

    @Test
    fun testFromErrorResponseWithoutReasonCode() {
        val reason = "Invalid card details"
        val error = TokenizationError.fromErrorResponse(reason, null)

        assertThat(error).isInstanceOf(TokenizationError::class)
        assertThat(error.message).isEqualTo("Tokenization failed: $reason")
        assertThat(error.throwable).isNull()
    }
}
