package io.hellgate.android.sdk.service

import io.hellgate.android.sdk.client.hellgate.NextAction
import io.hellgate.android.sdk.model.Problem

internal data class UnexpectedHellgateResponse(
    override val message: String,
    override val throwable: Throwable? = null,
) : Problem {
    companion object {
        fun nextActionNotTokenizeCard(nextAction: NextAction?) =
            UnexpectedHellgateResponse("Unexpected Hellgate response, next action is not TOKENIZE_CARD, actual: $nextAction")

        fun genericError() = UnexpectedHellgateResponse("Unexpected response from Hellgate, tokenization failed or session data is invalid")
    }
}
