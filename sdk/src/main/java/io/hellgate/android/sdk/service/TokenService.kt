package io.hellgate.android.sdk.service

import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.fx.coroutines.closeable
import arrow.fx.coroutines.resourceScope
import io.hellgate.android.sdk.client.HttpClientError
import io.hellgate.android.sdk.client.hellgate.*
import io.hellgate.android.sdk.client.hellgate.HgClient
import io.hellgate.android.sdk.client.hellgate.SessionResponse
import io.hellgate.android.sdk.client.hellgate.hgClient
import io.hellgate.android.sdk.element.additionaldata.AdditionalDataTypes
import io.hellgate.android.sdk.model.CardData
import io.hellgate.android.sdk.model.TokenizeCardResponse

internal interface ITokenService {
    /**
     * Tokenize a card
     * @param sessionId The session id created with a x-api-key on the hg-backend
     * @param cardData The card data to tokenize
     * @param additionalData Additional data to send to the hg-backend
     */
    suspend fun tokenize(
        sessionId: String,
        cardData: CardData,
        additionalData: Map<AdditionalDataTypes, String>,
    ): TokenizeCardResponse
}

private const val SUCCESS = "success"

internal fun tokenService(
    hgUrl: String,
    hellgateClient: () -> HgClient = { hgClient(hgUrl) },
): ITokenService =
    object : ITokenService {
        override suspend fun tokenize(
            sessionId: String,
            cardData: CardData,
            additionalData: Map<AdditionalDataTypes, String>,
        ): TokenizeCardResponse =
            either {
                resourceScope {
                    val hgClient = closeable { hellgateClient() }
                    val session = hgClient.fetchSession(sessionId).bind()

                    ensure(session.nextAction == NextAction.TOKENIZE_CARD) { failed() }
                    ensure(session.data != null) { failed() }
                    ensure(session.data is SessionResponse.Data.TokenizationParam) { failed() }

                    val encryptedData = createJWE(cardData, additionalData, session.data.jwk)

                    val result = hgClient.completeTokenizeCard(sessionId, encryptedData).bind()
                    ensure(result.status == SUCCESS) { failed() }
                    ensure(result.data != null) { failed() }
                    ensure(result.data is SessionResponse.Data.TokenId) { failed() }
                    TokenizeCardResponse.Success(result.data.tokenId)
                }
            }.getOrElse {
                TokenizeCardResponse.Failure(it.message, it.throwable)
            }

        private fun failed() = HttpClientError("Tokenization failed")
    }
