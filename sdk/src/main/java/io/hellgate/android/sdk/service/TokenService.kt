package io.hellgate.android.sdk.service

import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.fx.coroutines.closeable
import arrow.fx.coroutines.resourceScope
import io.hellgate.android.sdk.client.hellgate.*
import io.hellgate.android.sdk.client.hellgate.HgClient
import io.hellgate.android.sdk.client.hellgate.SessionResponse
import io.hellgate.android.sdk.client.hellgate.hgClient
import io.hellgate.android.sdk.element.additionaldata.AdditionalDataTypes
import io.hellgate.android.sdk.model.CardData
import io.hellgate.android.sdk.model.InvalidSessionState
import io.hellgate.android.sdk.model.TokenizeCardResponse
import io.hellgate.android.sdk.service.UnexpectedHellgateResponse.Companion.nextActionNotTokenizeCard

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

                    ensure(session.nextAction == NextAction.TOKENIZE_CARD) { nextActionNotTokenizeCard(session.nextAction) }
                    ensure(session.data != null) { InvalidSessionState.genericError() }
                    ensure(session.data is SessionResponse.Data.TokenizationParam) { InvalidSessionState.genericError() }

                    val encryptedData = createJWE(cardData, additionalData, session.data.jwk)

                    val result = hgClient.completeTokenizeCard(sessionId, encryptedData).bind()
                    if (result.data is SessionResponse.Data.Error) {
                        raise(TokenizationError.fromErrorResponse(result.data.reason, result.data.reasonCode))
                    }
                    ensure(result.status == SUCCESS) { UnexpectedHellgateResponse.genericError() }
                    ensure(result.data != null) { UnexpectedHellgateResponse.genericError() }
                    ensure(result.data is SessionResponse.Data.TokenId) { UnexpectedHellgateResponse.genericError() }
                    TokenizeCardResponse.Success(result.data.tokenId)
                }
            }.getOrElse {
                val throwable = it.throwable
                TokenizeCardResponse.Failure(
                    message = it.message + if (throwable != null) ", exception: ${throwable.message}" else "",
                )
            }
    }
