package io.hellgate.android.sdk

import arrow.core.right
import io.hellgate.android.sdk.TestFactory.JWK
import io.hellgate.android.sdk.client.hellgate.NextAction
import io.hellgate.android.sdk.client.hellgate.SessionResponse
import io.hellgate.android.sdk.handler.CardHandler
import io.hellgate.android.sdk.util.jsonDeserialize
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import java.util.UUID

const val TEST_BASE_URL = "https://api.test.com"

private val test_uuid = UUID.randomUUID().toString()

@RunWith(Enclosed::class)
class HellgateKtTest {
    class InitHellgate {
        @Test
        fun `Run initHellgate, a hellgate object with correct params`() =
            runTest {
                val hellgate = initHellgate(test_uuid, TEST_BASE_URL)
                assertThat(hellgate).isInstanceOf(Hellgate::class.java)
            }
    }

    class FetchSessionStatus {
        @Test
        fun `FetchSessionStatus for tokenize session, returns SessionStateRequireTokenization`() {
            val testData = SessionResponse.Data.TokenizationParam(JWK.jsonDeserialize())
            runTest {
                val hellgate = internalHellgate(test_uuid, TEST_BASE_URL) {
                    mockk {
                        coEvery { fetchSession(test_uuid) } returns SessionResponse(testData, NextAction.TOKENIZE_CARD, null).right()
                        every { close() } just Runs
                    }
                }

                assertThat(hellgate.fetchSessionStatus()).isEqualTo(SessionState.REQUIRE_TOKENIZATION)
            }
        }

        @Test
        fun `FetchSessionStatus for waiting session, returns SessionStateWaiting`() =
            runTest {
                val hellgate = internalHellgate(test_uuid, TEST_BASE_URL) {
                    mockk {
                        coEvery { fetchSession(test_uuid) } returns SessionResponse(null, NextAction.WAIT, null).right()
                        every { close() } just Runs
                    }
                }

                assertThat(hellgate.fetchSessionStatus()).isEqualTo(SessionState.WAITING)
            }

        @Test
        fun `FetchSessionStatus for complete session, returns SessionStateComplete`() =
            runTest {
                val hellgate = internalHellgate(test_uuid, TEST_BASE_URL) {
                    mockk {
                        coEvery { fetchSession(test_uuid) } returns SessionResponse(null, null, "success").right()
                        every { close() } just Runs
                    }
                }

                assertThat(hellgate.fetchSessionStatus()).isEqualTo(SessionState.COMPLETED)
            }
    }

    class Cardhandler {
        @Test
        fun `Create a Cardhandler but session is not in right state, Returns invalid session response`() =
            runTest {
                val hellgate = internalHellgate(test_uuid, TEST_BASE_URL) {
                    mockk {
                        coEvery { fetchSession(test_uuid) } returns SessionResponse(null, null, "success").right()
                        every { close() } just Runs
                    }
                }
                hellgate.cardHandler().onFailure {
                    assertThat(it.message).isEqualTo("Session is not in correct state to tokenize card, actual state: COMPLETED")
                }.onSuccess {
                    fail("Should not be successful")
                }
            }

        @Test
        fun `Create a Cardhandler and session is in right state, Returns a CardHandler`() =
            runTest {
                val testData = SessionResponse.Data.TokenizationParam(JWK.jsonDeserialize())
                val hellgate = internalHellgate(test_uuid, TEST_BASE_URL) {
                    mockk {
                        coEvery { fetchSession(test_uuid) } returns SessionResponse(testData, NextAction.TOKENIZE_CARD, null).right()
                        every { close() } just Runs
                    }
                }
                hellgate.cardHandler().onSuccess {
                    assertThat(it).isInstanceOf(CardHandler::class.java)
                }.onFailure {
                    fail("Should not be failure")
                }
            }
    }
}
