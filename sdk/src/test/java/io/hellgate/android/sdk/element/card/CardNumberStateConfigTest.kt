package io.hellgate.android.sdk.element.card

import io.hellgate.android.sdk.element.TextFieldStateConstants
import io.hellgate.android.sdk.model.CardBrand
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CardNumberStateConfigTest {
    @Test
    fun `blank Number returns blank state`() {
        assertThat(CardNumberStateConfig.determineState(CardNumberStateInput(CardBrand.Visa, "")))
            .isEqualTo(TextFieldStateConstants.Error.Blank)
    }

    @Test
    fun `card brand is invalid`() {
        val state = CardNumberStateConfig.determineState(CardNumberStateInput(CardBrand.Unknown, "0"))
        assertThat(state).isInstanceOf(TextFieldStateConstants.Error.Invalid::class.java)
    }

    @Test
    fun `incomplete number is in incomplete state`() {
        val state = CardNumberStateConfig.determineState(CardNumberStateInput(CardBrand.Visa, "12"))
        assertThat(state).isInstanceOf(TextFieldStateConstants.Error.Incomplete::class.java)
    }

    @Test
    fun `card number is too long`() {
        val state = CardNumberStateConfig.determineState(CardNumberStateInput(CardBrand.Visa, "1234567890123456789"))
        assertThat(state).isInstanceOf(TextFieldStateConstants.Error.Invalid::class.java)
    }

    @Test
    fun `card number has invalid luhn`() {
        val state = CardNumberStateConfig.determineState(CardNumberStateInput(CardBrand.Visa, "4242424242424243"))
        assertThat(state).isInstanceOf(TextFieldStateConstants.Error.Invalid::class.java)
    }

    @Test
    fun `card number is valid`() {
        val state = CardNumberStateConfig.determineState(CardNumberStateInput(CardBrand.Visa, "4242424242424242"))
        assertThat(state).isInstanceOf(TextFieldStateConstants.Valid.Full::class.java)
    }
}
