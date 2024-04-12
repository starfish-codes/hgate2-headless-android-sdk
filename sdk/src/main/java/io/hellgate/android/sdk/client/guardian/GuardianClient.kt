package io.hellgate.android.sdk.client.guardian

import arrow.core.Either
import io.hellgate.android.sdk.client.*
import io.hellgate.android.sdk.model.CardData
import io.ktor.client.HttpClient
import io.ktor.http.HttpMethod
import io.ktor.http.headersOf
import java.io.Closeable

internal const val X_API_KEY_HEADER = "x-api-key"
internal const val TOKENIZE_PATH = "tokenize"

internal interface GuardianClient : Closeable {
    suspend fun tokenizeCard(
        apiKey: String,
        cardData: CardData,
    ): Either<HttpClientError, GuardianTokenizeResponse>
}

internal fun guardianClient(
    baseUrl: String,
    client: HttpClient = httpClient(),
): GuardianClient =
    object : GuardianClient {
        override fun close() {
            client.close()
        }

        override suspend fun tokenizeCard(
            apiKey: String,
            cardData: CardData,
        ): Either<HttpClientError, GuardianTokenizeResponse> =
            client.eitherRequest<GuardianTokenizeResponse, GuardianTokenizeRequest>(
                method = HttpMethod.Post,
                baseUrl = baseUrl,
                pathSegments = listOf(TOKENIZE_PATH),
                additionalHeaders = headersOf(X_API_KEY_HEADER, apiKey),
                body = GuardianTokenizeRequest.fromCardData(cardData),
            )
    }
