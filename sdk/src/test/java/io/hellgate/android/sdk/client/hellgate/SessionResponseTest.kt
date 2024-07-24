package io.hellgate.android.sdk.client.hellgate

import io.hellgate.android.sdk.TestFactory.JWK
import io.hellgate.android.sdk.TestFactory.TOKENIZE_CARD_RESPONSE
import io.hellgate.android.sdk.util.jsonDeserialize
import io.hellgate.android.sdk.util.toObject
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val TOKEN_COMPLETE = """{"data": {"token_id": "66ca4bea-04ea-48d7-94c5-970cb7b93f03"},"status": "success"}"""

class SessionResponseTest {
    @Test
    fun `SessionResponse, Check if data is correctly parsed`() {
        val sessionResponse = TOKEN_COMPLETE.toObject<SessionResponse>().getOrNull()!!
        println(sessionResponse)

        assertThat(sessionResponse.data).isNotNull()
        assertThat(sessionResponse.data).isInstanceOf(SessionResponse.Data.TokenId::class.java)
        assertThat(sessionResponse.data).isEqualTo(SessionResponse.Data.TokenId("66ca4bea-04ea-48d7-94c5-970cb7b93f03"))
    }

    @Test
    fun `SessionResponse with Guardian_Session, data is correctly parsed`() {
        val sessionResponse = TOKENIZE_CARD_RESPONSE.toObject<SessionResponse>().getOrNull()!!
        println(sessionResponse)

        assertThat(sessionResponse.data).isNotNull()
        assertThat(sessionResponse.data).isInstanceOf(SessionResponse.Data.TokenizationParam::class.java)
        assertThat(sessionResponse.data).isEqualTo(SessionResponse.Data.TokenizationParam(JWK.jsonDeserialize()))
    }
}
