package io.hellgate.android.sdk.client.guardian

import com.marcinziolo.kotlin.wiremock.contains
import com.marcinziolo.kotlin.wiremock.equalTo
import io.hellgate.android.sdk.model.CardData
import io.hellgate.android.sdk.testutil.*
import io.ktor.client.HttpClient
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith

@RunWith(Enclosed::class)
class GuardianClientKtTest {
    class Close {
        @Test
        fun `Close client, Closes client`() = runTest {
            val httpClient = mockk<HttpClient> { every { close() } returns Unit }
            val client = guardianClient("baseUrl", httpClient)

            client.close()

            verify { httpClient.close() }
            confirmVerified(httpClient)
        }
    }

    class TokenizeCard : WiremockTest() {
        private val tokenId = "16ac8ac0-4ad8-451b-aa72-a7f5e5764daf"
        private val hellgateResponse = """
            {
                "id": "$tokenId",
                "security_code": true,
                "expiry_year": 2030,
                "expiry_month": 3,
                "cardholder_name": "John Doe",
                "created_at": "2024-04-10T16:09:32.525575Z",
                "issuer_identification_number": "41111111",
                "masked_account_number": "41111111****1142"
            }
        """.trimIndent()

        @Test
        fun `Call Hellgate with valid payload, Returns token id`() = runTest {
            wiremock.mockPostRequest("/tokenize", hellgateResponse) {
                it.headers contains X_API_KEY_HEADER equalTo "hgApiKey"
                it.body contains "account_number" equalTo "cardNumber"
                it.body contains "expiry_year" equalTo 2045
                it.body contains "expiry_month" equalTo 3
                it.body contains "security_code" equalTo "123"
            }

            val response = guardianClient(wiremock.baseUrl()).tokenizeCard("hgApiKey", CardData("cardNumber", "45", "03", "123"))

            response.assertRight {
                assertThat(it.id).isEqualTo(tokenId)
            }
        }
    }
}
