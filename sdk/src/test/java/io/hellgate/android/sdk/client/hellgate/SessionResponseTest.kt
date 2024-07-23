package io.hellgate.android.sdk.client.hellgate

import io.hellgate.android.sdk.testutil.assertLeft
import io.hellgate.android.sdk.util.toObject
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val EX_SESSION = """{"data":{"api_key": "key_eu_pub_CWAhmWiYstDJtxGcfZJ4vX","provider":"basis_theory","base_url":"https://api.test.com"},"next_action": "tokenize_card"}"""
private const val GUARDIAN_SESSION = """{"data":{"api_key":"key_eu_pub_123","provider":"guardian","base_url":"https://api.guardian.dev"},"next_action":"tokenize_card"}"""
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
    fun `SessionResponse with BT_Session, Check if data is correctly parsed`() {
        val sessionResponse = EX_SESSION.toObject<SessionResponse>().assertLeft {
            println(it)
        }
        println(sessionResponse)
    }

    @Test
    fun `SessionResponse with Guardian_Session, data is correctly parsed`() {
        val sessionResponse = GUARDIAN_SESSION.toObject<SessionResponse>().getOrNull()!!
        println(sessionResponse)

        assertThat(sessionResponse.data).isNotNull()
        assertThat(sessionResponse.data).isInstanceOf(SessionResponse.Data.TokenizationParam::class.java)
        assertThat(sessionResponse.data).isEqualTo(SessionResponse.Data.TokenizationParam("key_eu_pub_123", SessionResponse.Provider.Guardian, "https://api.guardian.dev"))
    }
}
