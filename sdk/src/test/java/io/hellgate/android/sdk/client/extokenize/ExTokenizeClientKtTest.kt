package io.hellgate.android.sdk.client.extokenize

import com.marcinziolo.kotlin.wiremock.contains
import com.marcinziolo.kotlin.wiremock.equalTo
import io.hellgate.android.sdk.model.CardData
import io.hellgate.android.sdk.testutil.*
import io.hellgate.android.sdk.testutil.assertLeft
import io.hellgate.android.sdk.testutil.assertRight
import io.ktor.client.HttpClient
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith

@RunWith(Enclosed::class)
class ExTokenizeClientKtTest {
    class Close : WiremockTest() {
        @Test
        fun `Close client, Closes client`() = runTest {
            val httpClient = mockk<HttpClient> { every { close() } returns Unit }
            val client = exTokenizeClient(wiremock.baseUrl(), httpClient)

            client.close()

            verify { httpClient.close() }
        }
    }

    class TokenizeCard : WiremockTest() {
        @Test
        fun `Call external with valid payload, Returns token id`() = runTest {
            wiremock.mockPostRequest("/tokenize", """{"id": "token-uuid"}""") {
                it.headers contains API_KEY_HEADER equalTo "apiKey123"
                it.body contains "data.number" equalTo "cardNumber"
                it.body contains "data.expiration_year" equalTo 2045
                it.body contains "data.expiration_month" equalTo 3
                it.body contains "data.cvc" equalTo "123"
            }

            val response = exTokenizeClient(wiremock.baseUrl()).tokenizeCard("apiKey123", CardData("cardNumber", "45", "03", "123"))

            response.assertRight {
                assertThat(it.id).isEqualTo("token-uuid")
            }
        }

        @Test
        fun `Call Bt with wrong api key, Returns error`() = runTest {
            val response = exTokenizeClient("https://api.basistheory.com", simpleLoggerClient()).tokenizeCard("apiKey123", CardData("invalidCard", "45", "03", "123"))
            response.assertLeft {
                assertThat(it.message).isEqualTo("401 Unauthorized : Empty body")
            }
        }
    }
}
