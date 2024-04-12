package io.hellgate.android.sdk.client.extokenize

import io.hellgate.android.sdk.model.CardData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ExTokenizeRequestTest {
    @Test
    fun getType() {
        assertThat(ExTokenizeRequest(ExTokenizeRequest.Data("", 1, 2, "")).type)
            .isEqualTo("card")
    }

    @Test
    fun getMask() {
        val request = ExTokenizeRequest(ExTokenizeRequest.Data("", 1, 2, ""))
        assertThat(request.mask.expirationMonth)
            .isEqualTo("{{ data.expiration_month }}")
        assertThat(request.mask.expirationYear)
            .isEqualTo("{{ data.expiration_year }}")
        assertThat(request.mask.number)
            .isEqualTo("{{ data.number | card_mask: 'true', 'true' }}")
    }

    @Test
    fun fromStringValues() {
        val data = ExTokenizeRequest.Data.fromCardData(CardData("4242424242424242", "21", "12", "123"))
        assertThat(data.cvc).isEqualTo("123")
        assertThat(data.expirationMonth).isEqualTo(12)
        assertThat(data.expirationYear).isEqualTo(2021)
        assertThat(data.number).isEqualTo("4242424242424242")
    }

    @Test
    fun fromStringValues2() {
        val data = ExTokenizeRequest.Data.fromCardData(CardData("4242424242424242", "21", "03", "123"))
        assertThat(data.cvc).isEqualTo("123")
        assertThat(data.expirationMonth).isEqualTo(3)
        assertThat(data.expirationYear).isEqualTo(2021)
        assertThat(data.number).isEqualTo("4242424242424242")
    }
}
