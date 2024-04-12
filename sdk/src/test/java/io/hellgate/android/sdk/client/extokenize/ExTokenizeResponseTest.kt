package io.hellgate.android.sdk.client.extokenize

import io.hellgate.android.sdk.util.toObject
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ExTokenizeResponseTest {
    @Test
    fun getId() {
        val response = ExTokenizeResponse("123")
        assertThat(response.id).isEqualTo("123")
    }

    @Test
    fun `Parse json to BtResponse, Outcome`() {
        val testJson = """
            {"id":"6429d6b6-cfe7-4c02-ae37-e2b40d145a57","type":"card","tenant_id":"6823acd9-2955-4e92-a45e-2d4d675ced25","data":{"expiration_month":2,"expiration_year":2031,"number":"42424242XXXX4242"},"created_by":"eb277759-188f-440a-a9e0-9e02401e391a","created_at":"2024-03-01T14:34:18.3311001+00:00","mask":{"expiration_month":"{{ data.expiration_month }}","expiration_year":"{{ data.expiration_year }}","number":"{{ data.number | card_mask: 'true', 'true' }}"},"privacy":{"classification":"pci","impact_level":"high","restriction_policy":"mask"},"search_indexes":[],"containers":["/pci/high/"],"aliases":["6429d6b6-cfe7-4c02-ae37-e2b40d145a57"]}
        """.trimIndent()

        val response = testJson.toObject<ExTokenizeResponse>().getOrNull()?.id.orEmpty()

        assertThat(response).isEqualTo("6429d6b6-cfe7-4c02-ae37-e2b40d145a57")
    }
}
