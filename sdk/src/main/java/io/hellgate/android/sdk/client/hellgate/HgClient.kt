package io.hellgate.android.sdk.client.hellgate

import arrow.core.Either
import io.hellgate.android.sdk.client.*
import io.hellgate.android.sdk.client.hellgate.Segments.COMPLETE_ACTION
import io.hellgate.android.sdk.client.hellgate.Segments.SESSIONS
import io.hellgate.android.sdk.client.hellgate.SessionCompleteTokenizeCard.*
import io.hellgate.android.sdk.client.hellgate.SessionCompleteTokenizeCard.AdditionalData.Companion.toDTO
import io.hellgate.android.sdk.element.additionaldata.AdditionalDataTypes
import io.ktor.client.HttpClient
import io.ktor.http.HttpMethod
import java.io.Closeable

internal object Segments {
    const val SESSIONS = "sessions"
    const val COMPLETE_ACTION = "complete-action"
}

internal interface HgClient : Closeable {
    suspend fun fetchSession(sessionId: String): Either<HttpClientError, SessionResponse>

    suspend fun completeTokenizeCard(
        sessionId: String,
        encryptedData: String,
        additionalData: Map<AdditionalDataTypes, String>,
    ): Either<HttpClientError, SessionResponse>
}

internal fun hgClient(
    baseUrl: String,
    client: HttpClient = httpClient(),
) = object : HgClient {
    override fun close() {
        client.close()
    }

    override suspend fun fetchSession(sessionId: String): Either<HttpClientError, SessionResponse> =
        client.eitherRequest<SessionResponse, Unit>(
            HttpMethod.Get,
            baseUrl,
            listOf(SESSIONS, sessionId),
        )

    override suspend fun completeTokenizeCard(
        sessionId: String,
        encryptedData: String,
        additionalData: Map<AdditionalDataTypes, String>,
    ): Either<HttpClientError, SessionResponse> =
        client.eitherRequest<SessionResponse, SessionCompleteTokenizeCard>(
            HttpMethod.Post,
            baseUrl,
            listOf(SESSIONS, sessionId, COMPLETE_ACTION),
            body = SessionCompleteTokenizeCard(
                result = Result(
                    encryptedData,
                    if (additionalData.isEmpty()) null else additionalData.toDTO(),
                ),
            ),
        )
}
