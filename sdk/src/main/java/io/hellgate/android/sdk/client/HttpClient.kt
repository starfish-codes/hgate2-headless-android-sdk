package io.hellgate.android.sdk.client

import arrow.core.*
import io.hellgate.android.sdk.util.registerJacksonConverter
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import kotlinx.coroutines.*

internal fun httpClient() =
    HttpClient(OkHttp) {
        expectSuccess = false
        install(ContentNegotiation) {
            registerJacksonConverter()
        }
    }

internal suspend inline fun <reified ResponseType, reified BodyType> HttpClient.eitherRequest(
    method: HttpMethod,
    baseUrl: String,
    pathSegments: List<String>,
    body: BodyType? = null,
    additionalHeaders: Headers = headersOf(),
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
) = withContext(dispatcher) {
    Either.catch {
        val response = this@eitherRequest.request(baseUrl) {
            this.method = method
            url { appendPathSegments(pathSegments) }
            headers { appendAll(additionalHeaders) }
            if (body != null) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }

        if (response.status.isSuccess()) {
            response.body<ResponseType>().right()
        } else {
            val resp = response.bodyAsText()
            val statusCode = response.status.lookupEmptyDescription()
            HttpClientError(statusCode.toString() + " : " + resp.ifEmpty { "Empty body" }).left()
        }
    }.getOrElse {
        HttpClientError(it.message.orEmpty(), it).left()
    }
}

private fun HttpStatusCode.lookupEmptyDescription(): HttpStatusCode =
    if (this.description.isEmpty()) HttpStatusCode.fromValue(this.value) else this
