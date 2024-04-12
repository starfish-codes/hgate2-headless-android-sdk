package io.hellgate.android.sdk.demo.client

import arrow.core.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import kotlinx.coroutines.*

internal fun httpClient(logging: Boolean = true) = HttpClient(OkHttp) {
    expectSuccess = false
    install(ContentNegotiation) {
        registerJacksonConverter()
    }
    if (logging) {
        install(Logging) {
            logger = Logger.ANDROID
            level = LogLevel.ALL
        }
    }
}

internal suspend inline fun <reified ResponseType, reified BodyType> HttpClient.call(
    method: HttpMethod,
    baseUrl: String,
    pathSegments: List<String>,
    body: BodyType? = null,
    additionalHeaders: Headers = headersOf(),
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) = withContext(dispatcher) {
    Either.catch {
        val response = this@call.request(baseUrl) {
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
            HttpClientError(response.status.toString() + " : " + resp).left()
        }
    }.getOrElse {
        HttpClientError(it.message.orEmpty(), it).left()
    }
}

data class HttpClientError(
    override val message: String = "",
    override val throwable: Throwable? = null
) : Throwable(message, throwable), Problem

interface Problem {
    val message: String
    val throwable: Throwable?
}