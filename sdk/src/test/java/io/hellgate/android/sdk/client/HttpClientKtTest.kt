package io.hellgate.android.sdk.client

import io.hellgate.android.sdk.client.hellgate.NextAction
import io.hellgate.android.sdk.client.hellgate.SessionResponse
import io.hellgate.android.sdk.testutil.*
import io.hellgate.android.sdk.testutil.assertLeft
import io.hellgate.android.sdk.testutil.assertRight
import io.ktor.http.HttpMethod
import kotlinx.coroutines.test.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

const val TOKENIZE_CARD_RESPONSE = """{"data":{"api_key":"key_eu_pub","provider":"guardian","base_url":"test123"},"next_action":"tokenize_card"}"""

class HttpClientKtTest : WiremockTest() {
    @Test
    fun `Call Wiremock for a sessionId but receive 401, Returns correct status and client error`() = runTest {
        val client = simpleLoggerClient()
        wiremock.mockPostRequest("/sessionId", "", 401)

        client.eitherRequest<SessionResponse, String>(
            HttpMethod.Post,
            baseUrl,
            listOf("sessionId"),
            "test123",
        ).assertLeft {
            assertThat(it).isEqualTo(HttpClientError("401 Unauthorized : Empty body", null))
        }
    }

    @Test
    fun `CallHellgate for a sessionId, Returns correct type with session id`() = runTest {
        val client = simpleLoggerClient()
        wiremock.mockGetRequest("/sessionId/123", TOKENIZE_CARD_RESPONSE, 200)

        client.eitherRequest<SessionResponse, Unit>(
            HttpMethod.Get,
            baseUrl,
            listOf("sessionId", "123"),
        ).assertRight {
            assertThat(it).isEqualTo(
                SessionResponse(
                    SessionResponse.Data.TokenizationParam(
                        "key_eu_pub",
                        SessionResponse.Provider.Guardian,
                        "test123",
                    ),
                    NextAction.TOKENIZE_CARD,
                    null,
                ),
            )
        }
    }
}
