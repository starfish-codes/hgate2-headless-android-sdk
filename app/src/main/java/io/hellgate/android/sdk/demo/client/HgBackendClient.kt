package io.hellgate.android.sdk.demo.client

import arrow.core.Either
import io.hellgate.android.sdk.demo.BuildConfig
import io.hellgate.android.sdk.demo.HELLGATE_BASE_URL
import io.hellgate.android.sdk.demo.client.Segments.SESSION
import io.hellgate.android.sdk.demo.client.Segments.TOKENS
import io.ktor.client.HttpClient
import io.ktor.http.HttpMethod
import io.ktor.http.headersOf
import java.io.Closeable

internal object Segments {
    const val TOKENS = "tokens"
    const val SESSION = "session"
}

interface HgBackendClient : Closeable {
    suspend fun createSession(): Either<HttpClientError, SessionResponse>
}

fun hgBackendClient(
    baseUrl: String = HELLGATE_BASE_URL,
    client: HttpClient = httpClient(),
    apiKey: String = BuildConfig.HG_BACKEND_API_KEY
) = object : HgBackendClient {

    override fun close() {
        client.close()
    }

    override suspend fun createSession(): Either<HttpClientError, SessionResponse> =
        client.call<SessionResponse, Unit>(
            HttpMethod.Post,
            baseUrl,
            listOf(TOKENS, SESSION),
            additionalHeaders = headersOf("x-api-key", apiKey)
        )
}


