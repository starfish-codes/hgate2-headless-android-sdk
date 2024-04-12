package io.hellgate.android.sdk.client.extokenize

import arrow.core.Either
import io.hellgate.android.sdk.client.*
import io.hellgate.android.sdk.model.CardData
import io.ktor.client.HttpClient
import io.ktor.http.HttpMethod
import io.ktor.http.headersOf
import java.io.Closeable

internal const val API_KEY_HEADER = "BT-API-KEY"
internal const val TOKENIZE_PATH = "tokenize"

internal interface ExTokenizeClient : Closeable {
    suspend fun tokenizeCard(
        apiKey: String,
        cardData: CardData,
    ): Either<HttpClientError, ExTokenizeResponse>
}

internal fun exTokenizeClient(
    baseUrl: String,
    client: HttpClient = httpClient(),
) = object : ExTokenizeClient {
    override fun close() {
        client.close()
    }

    override suspend fun tokenizeCard(
        apiKey: String,
        cardData: CardData,
    ): Either<HttpClientError, ExTokenizeResponse> =
        client.eitherRequest<ExTokenizeResponse, ExTokenizeRequest>(
            method = HttpMethod.Post,
            baseUrl = baseUrl,
            pathSegments = listOf(TOKENIZE_PATH),
            additionalHeaders = headersOf(API_KEY_HEADER, apiKey),
            body = ExTokenizeRequest(
                ExTokenizeRequest.Data.fromCardData(cardData),
            ),
        )
}
