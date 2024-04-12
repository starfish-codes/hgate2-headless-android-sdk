package io.hellgate.android.sdk.testutil

import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.marcinziolo.kotlin.wiremock.*
import io.hellgate.android.sdk.util.registerJacksonConverter
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.*
import org.junit.Rule
import java.net.ServerSocket

fun simpleLoggerClient(): HttpClient = HttpClient(OkHttp) {
    expectSuccess = false
    install(ContentNegotiation) {
        registerJacksonConverter()
    }
    install(Logging) {
        logger = Logger.SIMPLE
        level = LogLevel.ALL
    }
}

fun findRandomPort(): Int {
    ServerSocket(0).use { socket -> return socket.localPort }
}

open class WiremockTest {
    private val port = findRandomPort()
    val baseUrl = "http://localhost:$port"

    @Rule
    @JvmField
    val wiremock = WireMockRule(port)
}

fun WireMockRule.mockGetRequest(
    path: String,
    response: String,
    responseStatusCode: Int = 200,
) {
    this.get {
        url equalTo path
    } returns {
        header = "Content-Type" to "application/json"
        statusCode = responseStatusCode
        body = response
    }
}

fun WireMockRule.mockPostRequest(
    path: String,
    response: String,
    responseStatusCode: Int = 200,
    additionalChecks: (RequestSpecification) -> Unit = {},
) {
    this.post {
        url equalTo path
        additionalChecks(this)
    } returnsJson {
        statusCode = responseStatusCode
        body = response
    }
}
