package io.hellgate.android.sdk.client.hellgate

import arrow.fx.coroutines.closeable
import arrow.fx.coroutines.resourceScope
import io.hellgate.android.sdk.TestFactory.JWK
import io.hellgate.android.sdk.TestFactory.TOKENIZE_CARD_RESPONSE
import io.hellgate.android.sdk.client.HttpClientError
import io.hellgate.android.sdk.testutil.*
import io.hellgate.android.sdk.util.jsonDeserialize
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith

@RunWith(Enclosed::class)
class HgClientKtTest {
    class FetchSession : WiremockTest() {
        @Test
        fun `Try to fetch an existing session with correct id, Returns BT Api Key`() =
            runTest {
                wiremock.mockGetRequest("/sessions/123", TOKENIZE_CARD_RESPONSE, 200)

                resourceScope {
                    val hgClient = closeable { hgClient(baseUrl) }
                    val info = hgClient.fetchSession("123")

                    info.assertRight {
                        assertThat(it).isEqualTo(SessionResponse(SessionResponse.Data.TokenizationParam(JWK.jsonDeserialize()), NextAction.TOKENIZE_CARD, null))
                    }
                }
            }

        @Test
        fun `Try to fetch a non-existing session with incorrect id, Returns correct status and client error`() =
            runTest {
                wiremock.mockGetRequest("/sessions/404", "{\"code\":404,\"message\":\"The session was not found\",\"classifier\":\"NOT_FOUND\"}", 404)

                resourceScope {
                    val hgClient = closeable { hgClient(baseUrl) }
                    val info = hgClient.fetchSession("404")

                    info.assertLeft {
                        assertThat(it).isEqualTo(HttpClientError("404 Not Found : {\"code\":404,\"message\":\"The session was not found\",\"classifier\":\"NOT_FOUND\"}", null))
                    }
                }
            }
    }

    class CompleteTokenizeCard : WiremockTest() {
        @Test
        fun `Try to complete a session with correct id and BT Token Id, Returns Token Id`() =
            runTest {
                wiremock.mockPostRequest("/sessions/123/complete-action", """{"data":{"token_id":"eaac6eac-6d09-469b-a981-f1be82b155f9"},"status":"success"}""", 200)

                resourceScope {
                    val hgClient = closeable { hgClient(baseUrl) }
                    val result = hgClient.completeTokenizeCard("123", "token_123")

                    result.assertRight {
                        assertThat(it).isEqualTo(SessionResponse(SessionResponse.Data.TokenId("eaac6eac-6d09-469b-a981-f1be82b155f9"), null, "success"))
                    }
                }
            }

        @Test
        fun `Try to complete a session with correct id and BT Token Id and addData, Returns Token Id`() =
            runTest {
                wiremock.mockPostRequest("/sessions/123/complete-action", """{"data":{"token_id":"eaac6eac-6d09-469b-a981-f1be82b155f9","additional_data": {"cardholder_name": "John Doe"}},"status":"success"}""", 200)

                resourceScope {
                    val hgClient = closeable { hgClient(baseUrl) }
                    val result = hgClient.completeTokenizeCard("123", "token_123")

                    result.assertRight {
                        assertThat(it).isEqualTo(SessionResponse(SessionResponse.Data.TokenId("eaac6eac-6d09-469b-a981-f1be82b155f9"), null, "success"))
                    }
                }
            }

        @Test
        fun `Try to complete a session with incorrect id and BT Token Id, Returns correct status and client error`() =
            runTest {
                wiremock.mockPostRequest("/sessions/404/complete-action", "{\"code\":404,\"message\":\"The session_context was not found\",\"classifier\":\"NOT_FOUND\"}", 404)

                resourceScope {
                    val hgClient = closeable { hgClient(baseUrl) }
                    val result = hgClient.completeTokenizeCard("404", "token_123")

                    result.assertLeft {
                        assertThat(it).isEqualTo(HttpClientError("404 Not Found : {\"code\":404,\"message\":\"The session_context was not found\",\"classifier\":\"NOT_FOUND\"}", null))
                    }
                }
            }
    }
}
