package io.hellgate.android.sdk.service

import arrow.core.left
import arrow.core.right
import io.hellgate.android.sdk.Constants
import io.hellgate.android.sdk.client.HttpClientError
import io.hellgate.android.sdk.client.extokenize.ExTokenizeClient
import io.hellgate.android.sdk.client.extokenize.ExTokenizeResponse
import io.hellgate.android.sdk.client.guardian.GuardianClient
import io.hellgate.android.sdk.client.guardian.GuardianTokenizeResponse
import io.hellgate.android.sdk.client.hellgate.*
import io.hellgate.android.sdk.client.hellgate.NextAction
import io.hellgate.android.sdk.model.CardData
import io.hellgate.android.sdk.model.TokenizeCardResponse
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith

@RunWith(Enclosed::class)
class TokenServiceKtTest {
    class Tokenize {
        private val apikey = "guardian_key"
        private val number = "4242"
        private val sessionId = "abc123"
        private val tokenId = "HG_TOKEN_ID"
        private val btKey = "bt_key"
        private val btSessionData = SessionResponse.Data.TokenizationParam(btKey, SessionResponse.Provider.External, "https://api.test.com")
        private val guardianData = SessionResponse.Data.TokenizationParam(apikey, SessionResponse.Provider.Guardian, "https://guardian.com")
        private val invalidSessionResponse = SessionResponse(SessionResponse.Data.TokenizationParam("apikey", SessionResponse.Provider.External, "https://api.test.com"), NextAction.WAIT, null)

        @Test
        fun `Process valid inputs, Returns ResponseSuccess`() = runTest {
            val tokenService = tokenService(
                Constants.HG_STAGING_URL,
                {
                    mockk<HgClient> {
                        coEvery { fetchSession("123") } returns
                            SessionResponse(btSessionData, NextAction.TOKENIZE_CARD, null).right()
                        coEvery { completeTokenizeCard("123", "123", emptyMap()) } returns
                            SessionResponse(SessionResponse.Data.TokenId("HG_TOKEN_ID"), null, "success").right()
                        coEvery { close() } just runs
                    }
                },
                {
                    mockk<ExTokenizeClient> {
                        coEvery { tokenizeCard(btKey, CardData(number, "30", "12", "123")) } returns
                            ExTokenizeResponse("123").right()
                        coEvery { close() } just runs
                    }
                },
            )

            val result = tokenService.tokenize("123", CardData(number, "30", "12", "123"), emptyMap())

            assertThat(result).isInstanceOf(TokenizeCardResponse.Success::class.java)
            assertThat((result as TokenizeCardResponse.Success).id).isEqualTo("HG_TOKEN_ID")
        }

        @Test
        fun `Process valid input for a hg-token-session, Returns Success`() {
            runTest {
                val tokenService = tokenService(
                    Constants.HG_STAGING_URL,
                    {
                        mockk<HgClient> {
                            coEvery { fetchSession(sessionId) } returns
                                SessionResponse(guardianData, NextAction.TOKENIZE_CARD, null).right()
                            coEvery { completeTokenizeCard(sessionId, "123", emptyMap()) } returns
                                SessionResponse(SessionResponse.Data.TokenId(tokenId), null, "success").right()
                            coEvery { close() } just runs
                        }
                    },
                    guardClient = {
                        mockk<GuardianClient> {
                            coEvery { tokenizeCard(apikey, CardData(number, "30", "12", "123")) } returns
                                GuardianTokenizeResponse("123").right()
                            coEvery { close() } just runs
                        }
                    },
                )

                val result = tokenService.tokenize(sessionId, CardData(number, "30", "12", "123"), emptyMap())

                assertThat(result).isInstanceOf(TokenizeCardResponse.Success::class.java)
                assertThat((result as TokenizeCardResponse.Success).id).isEqualTo("HG_TOKEN_ID")
            }
        }

        @Test
        fun `Process invalid inputs, Returns ResponseFailure`() = runTest {
            val tokenService = tokenService(
                Constants.HG_STAGING_URL,
                {
                    mockk {
                        coEvery { fetchSession("123") } returns
                            HttpClientError("Error").left()
                        coEvery { close() } just runs
                    }
                },
                {
                    mockk {
                        coEvery { close() } just runs
                    }
                },
            )

            val result = tokenService.tokenize("123", CardData("4242", "30", "12", "123"), emptyMap())

            assertThat(result).isInstanceOf(TokenizeCardResponse.Failure::class.java)
            assertThat((result as TokenizeCardResponse.Failure).message).isEqualTo("Error")
        }

        @Test
        fun `Process valid data but session returns invalid state, Return Tokenization failed`() = runTest {
            val tokenService = tokenService(
                Constants.HG_STAGING_URL,
                {
                    mockk {
                        coEvery { fetchSession("123") } returns invalidSessionResponse.right()
                        coEvery { close() } just runs
                    }
                },
            )

            val result = tokenService.tokenize("123", CardData("4242", "30", "12", "123"), emptyMap())

            assertThat(result).isInstanceOf(TokenizeCardResponse.Failure::class.java)
            assertThat((result as TokenizeCardResponse.Failure).message).isEqualTo("Tokenization failed")
        }

        @Test
        fun `Process valid data but session data is null, Return Tokenization failed`() = runTest {
            val tokenService = tokenService(
                Constants.HG_STAGING_URL,
                {
                    mockk {
                        coEvery { fetchSession("123") } returns
                            SessionResponse(null, NextAction.TOKENIZE_CARD, null).right()
                        coEvery { close() } just runs
                    }
                },
            )

            val result = tokenService.tokenize("123", CardData("4242", "30", "12", "123"), emptyMap())

            assertThat(result).isInstanceOf(TokenizeCardResponse.Failure::class.java)
            assertThat((result as TokenizeCardResponse.Failure).message).isEqualTo("Tokenization failed")
        }

        @Test
        fun `Process valid data but completeTokenization data is null, Return Tokenization failed`() = runTest {
            val tokenService = tokenService(
                Constants.HG_STAGING_URL,
                {
                    mockk {
                        coEvery { fetchSession(sessionId) } returns
                            SessionResponse(guardianData, NextAction.TOKENIZE_CARD, null).right()
                        coEvery { completeTokenizeCard(sessionId, "123", emptyMap()) } returns
                            SessionResponse(null, null, "success").right()
                        coEvery { close() } just runs
                    }
                },
                guardClient = {
                    mockk<GuardianClient> {
                        coEvery { tokenizeCard(apikey, CardData(number, "30", "12", "123")) } returns
                            GuardianTokenizeResponse("123").right()
                        coEvery { close() } just runs
                    }
                },
            )

            val result = tokenService.tokenize(sessionId, CardData("4242", "30", "12", "123"), emptyMap())

            assertThat(result).isInstanceOf(TokenizeCardResponse.Failure::class.java)
            assertThat((result as TokenizeCardResponse.Failure).message).isEqualTo("Tokenization failed")
        }
    }
}
