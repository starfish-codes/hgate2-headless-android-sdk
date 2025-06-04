package io.hellgate.android.sdk.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.fasterxml.jackson.databind.JsonNode
import io.hellgate.android.sdk.Constants
import io.hellgate.android.sdk.client.HttpClientError
import io.hellgate.android.sdk.client.hellgate.*
import io.hellgate.android.sdk.client.hellgate.NextAction
import io.hellgate.android.sdk.model.CardData
import io.hellgate.android.sdk.model.TokenizeCardResponse
import io.hellgate.android.sdk.util.jsonDeserialize
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import java.net.SocketTimeoutException

@RunWith(Enclosed::class)
class TokenServiceKtTest {
    class Tokenize {
        private val number = "4242424242424242"
        private val sessionId = "abc123"
        private val tokenId = "HG_TOKEN_ID"
        private val jwk = """{"e": "AQAB","kty": "RSA","n": "yv06FFr964RImD0uv4d9b79LHedbR_hdeX8wydUrWcrjgy9q63VBWM3TF1NeSFNGEW2Zcl3vMXTCIqarI5APj2KKpwkRmxS4e9Qs1bXGlyZHAsx6hjPvqa3roYCpXFOCmBDO7TaQjJzWxibAfNyAijWVbsZtsCTPeVXvc2xSvBzSNixO3EVdqXfNGCaCHIV2FZhEM_7PKrERrVMA4H-r6n8tQYr-joqQVEDUC3DdRNV9Lsaf4YHh1Lej0nioQ4lnAOenlg036GNfaZhtdxpYM2LK_V3Tqxut0S48XSJTvwvG3yv3MwN26w7cOk7i6oPcMOeKGlHWvK3_suCcFx_KJQ"}"""
            .jsonDeserialize<JsonNode>()
        private val invalidSessionResponse = SessionResponse(SessionResponse.Data.TokenizationParam(jwk), NextAction.WAIT, null)

        private val guardianData = SessionResponse.Data.TokenizationParam(jwk)

        @Test
        fun `Process valid input for a hg-token-session, Returns Success`() =
            runTest {
                val successMock = object : HgClient {
                    override suspend fun fetchSession(sessionId: String): Either<HttpClientError, SessionResponse> = SessionResponse(guardianData, NextAction.TOKENIZE_CARD, null).right()

                    override suspend fun completeTokenizeCard(
                        sessionId: String,
                        encryptedData: String,
                    ): Either<HttpClientError, SessionResponse> = SessionResponse(SessionResponse.Data.TokenId(tokenId), null, "success").right()

                    override fun close() = Unit
                }
                val tokenService = tokenService(Constants.HG_URL) { successMock }
                val result = tokenService.tokenize(sessionId, CardData(number, "30", "12", "123"), emptyMap())

                assertThat(result).isInstanceOf(TokenizeCardResponse.Success::class.java)
                assertThat((result as TokenizeCardResponse.Success).id).isEqualTo("HG_TOKEN_ID")
            }

        @Test
        fun `Process invalid inputs, Returns ResponseFailure`() =
            runTest {
                val errorMock = object : HgClient {
                    override suspend fun fetchSession(sessionId: String): Either<HttpClientError, SessionResponse> = HttpClientError("Error$sessionId").left()

                    override suspend fun completeTokenizeCard(
                        sessionId: String,
                        encryptedData: String,
                    ): Either<HttpClientError, SessionResponse> = HttpClientError("Error").left()

                    override fun close() = Unit
                }
                val tokenService = tokenService(Constants.HG_URL) { errorMock }

                val result = tokenService.tokenize("123", CardData("4242424242424242", "30", "12", "123"), emptyMap())

                assertThat(result).isInstanceOf(TokenizeCardResponse.Failure::class.java)
                assertThat((result as TokenizeCardResponse.Failure).message).isEqualTo("Error123")
            }

        @Test
        fun `Process valid data but session returns invalid state, Return Tokenization failed no mockk`() =
            runTest {
                val invalidMock = object : HgClient {
                    override suspend fun fetchSession(sessionId: String): Either<HttpClientError, SessionResponse> = invalidSessionResponse.right()

                    override suspend fun completeTokenizeCard(
                        sessionId: String,
                        encryptedData: String,
                    ): Either<HttpClientError, SessionResponse> = HttpClientError("Error").left()

                    override fun close() = Unit
                }
                val tokenService = tokenService(Constants.HG_URL) { invalidMock }

                val result = tokenService.tokenize("123", CardData("4242424242424242", "30", "12", "123"), emptyMap())

                assertThat(result).isInstanceOf(TokenizeCardResponse.Failure::class.java)
                assertThat((result as TokenizeCardResponse.Failure).message).isEqualTo("Unexpected Hellgate response, next action is not TOKENIZE_CARD, actual: WAIT")
            }

        @Test
        fun `Process valid data but session data is null, Return Tokenization failed`() =
            runTest {
                val nullDataMock = object : HgClient {
                    override suspend fun fetchSession(sessionId: String): Either<HttpClientError, SessionResponse> = SessionResponse(null, NextAction.TOKENIZE_CARD, null).right()

                    override suspend fun completeTokenizeCard(
                        sessionId: String,
                        encryptedData: String,
                    ): Either<HttpClientError, SessionResponse> = HttpClientError("Error").left()

                    override fun close() = Unit
                }

                val tokenService = tokenService(Constants.HG_URL) { nullDataMock }

                val result = tokenService.tokenize("123", CardData("4242424242424242", "30", "12", "123"), emptyMap())

                assertThat(result).isInstanceOf(TokenizeCardResponse.Failure::class.java)
                assertThat((result as TokenizeCardResponse.Failure).message).isEqualTo("Session is not in correct state to tokenize card")
            }

        @Test
        fun `Process valid data but completeTokenization data is null, Return Tokenization failed`() =
            runTest {
                val nullDataMock = object : HgClient {
                    override suspend fun fetchSession(sessionId: String): Either<HttpClientError, SessionResponse> = SessionResponse(guardianData, NextAction.TOKENIZE_CARD, null).right()

                    override suspend fun completeTokenizeCard(
                        sessionId: String,
                        encryptedData: String,
                    ): Either<HttpClientError, SessionResponse> = SessionResponse(null, null, "success").right()

                    override fun close() = Unit
                }
                val tokenService = tokenService(Constants.HG_URL) { nullDataMock }

                val result = tokenService.tokenize(sessionId, CardData("4242424242424242", "30", "12", "123"), emptyMap())

                assertThat(result).isInstanceOf(TokenizeCardResponse.Failure::class.java)
                assertThat((result as TokenizeCardResponse.Failure).message).isEqualTo("Unexpected response from Hellgate, tokenization failed or session data is invalid")
            }

        @Test
        fun `Process valid data but hellgate will come back with 200 failed invalid_test_card, Return Tokenization failed`() =
            runTest {
                val invalidCardMock = object : HgClient {
                    override suspend fun fetchSession(sessionId: String): Either<HttpClientError, SessionResponse> = SessionResponse(guardianData, NextAction.TOKENIZE_CARD, null).right()

                    override suspend fun completeTokenizeCard(
                        sessionId: String,
                        encryptedData: String,
                    ): Either<HttpClientError, SessionResponse> = SessionResponse(SessionResponse.Data.Error("Invalid test card", "invalid_test_card"), null, "failed").right()

                    override fun close() = Unit
                }
                val tokenService = tokenService(Constants.HG_URL) { invalidCardMock }

                val result = tokenService.tokenize(sessionId, CardData("4242424242424242", "30", "12", "123"), emptyMap())

                assertThat(result).isInstanceOf(TokenizeCardResponse.Failure::class.java)
                assertThat(result).isEqualTo(
                    TokenizeCardResponse.Failure(
                        message = "Tokenization failed: Invalid test card, reasonCode: invalid_test_card",
                        validationErrors = emptyList(),
                    ),
                )
            }

        @Test
        fun `Process data but hgClient returns timeout error, Returns tokenization failed`() =
            runTest {
                val timeoutMock = object : HgClient {
                    override suspend fun fetchSession(sessionId: String): Either<HttpClientError, SessionResponse> = HttpClientError("Timeout", SocketTimeoutException("Timeout reached, 2000ms")).left()

                    override suspend fun completeTokenizeCard(
                        sessionId: String,
                        encryptedData: String,
                    ): Either<HttpClientError, SessionResponse> = TODO("Not needed for this test")

                    override fun close() = Unit
                }
                val tokenService = tokenService(Constants.HG_URL) { timeoutMock }

                val result = tokenService.tokenize(sessionId, CardData("4242424242424242", "30", "12", "123"), emptyMap())

                assertThat(result).isInstanceOf(TokenizeCardResponse.Failure::class.java)
                assertThat((result as TokenizeCardResponse.Failure).message).isEqualTo("Timeout, exception: Timeout reached, 2000ms")
                assertThat(result.validationErrors).isEmpty()
            }
    }
}
