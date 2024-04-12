package io.hellgate.android.sdk.util

import io.hellgate.android.sdk.client.hellgate.SessionResponse
import io.hellgate.android.sdk.testutil.assertLeft
import io.hellgate.android.sdk.testutil.assertRight
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class JacksonSerializationKtTest {
    @Test
    fun toObject() {
        """{"data":{"token_id":"51e7123e-b5de-4817-a4de-f5f99d61a022"},"status":"success"}""".toObject<SessionResponse>()
            .assertRight {
                assertThat(it.data).isInstanceOf(SessionResponse.Data.TokenId::class.java)
                assertThat((it.data as SessionResponse.Data.TokenId).tokenId).isEqualTo("51e7123e-b5de-4817-a4de-f5f99d61a022")
                assertThat(it.status).isEqualTo("success")
            }
    }

    @Test
    fun toJson() {
        val response = SessionResponse(SessionResponse.Data.TokenId("51e7123e-b5de-4817-a4de-f5f99d61a022"), null, "success")
        response.toJson()
            .assertRight {
                assertThat(it).isEqualTo("""{"data":{"token_id":"51e7123e-b5de-4817-a4de-f5f99d61a022"},"status":"success"}""")
            }
    }

    @Test
    fun `toObject with invalid json input, Returns Left Deserialization Error`() {
        """{"data":{"token_id":"51e7123e-b5de-4817-a4de-f5f99d61a022"},"status":"success""".toObject<SessionResponse>()
            .assertLeft {
                assertThat(it).isInstanceOf(DeserializationError::class.java)
                assertThat(it.message).isEqualTo("Error while Parsing Json String")
                assertThat(it.throwable).isInstanceOf(com.fasterxml.jackson.databind.JsonMappingException::class.java)
            }
    }
}
