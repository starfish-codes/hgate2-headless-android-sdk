package io.hellgate.android.sdk.model

import io.hellgate.android.sdk.element.card.CardNumberTestConstants
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CardNumberTest {
    @Test
    fun `getFormatted() with panLength 16 with empty number`() {
        assertThat(CardNumber.Unvalidated("   ").getFormatted(16)).isEqualTo("")
    }

    @Test
    fun `getFormatted() with panLength 16 with 3 digit number`() {
        assertThat(
            CardNumber.Unvalidated("4 2 4").getFormatted(16),
        ).isEqualTo("424")
    }

    @Test
    fun `getFormatted() with panLength 16 with full number`() {
        assertThat(
            CardNumber.Unvalidated("42424242 42424242").getFormatted(16),
        ).isEqualTo("4242 4242 4242 4242")
    }

    @Test
    fun `getFormatted() with panLength 16 and extraneous digits should format and remove extraneous digits`() {
        assertThat(
            CardNumber.Unvalidated("42424242 42424242 42424").getFormatted(16),
        ).isEqualTo("4242 4242 4242 4242")
    }

    @Test
    fun `getFormatted() with panLength 19 with partial number`() {
        assertThat(
            CardNumber.Unvalidated("6216828050").getFormatted(19),
        ).isEqualTo("6216 8280 50")
    }

    @Test
    fun `getFormatted() with panLength 19 with full number`() {
        assertThat(
            CardNumber.Unvalidated("6216828050123456789").getFormatted(19),
        ).isEqualTo("6216 8280 5012 3456 789")
    }

    @Test
    fun `getFormatted() with panLength 14 with partial number`() {
        assertThat(
            CardNumber.Unvalidated("3622720").getFormatted(14),
        ).isEqualTo("3622 720")
    }

    @Test
    fun `getFormatted() with panLength 14 with full number`() {
        assertThat(
            CardNumber.Unvalidated("36227206271667").getFormatted(14),
        ).isEqualTo("3622 720627 1667")
    }

    @Test
    fun `bin with empty number should return null`() {
        assertThat(
            CardNumber.Unvalidated("   ").bin,
        ).isNull()
    }

    @Test
    fun `bin with 5 digits should return null`() {
        assertThat(
            CardNumber.Unvalidated("12 3 4 5").bin,
        ).isNull()
    }

    @Test
    fun `bin with 6 digits should return bin`() {
        assertThat(
            CardNumber.Unvalidated("12 3 4 56").bin,
        ).isEqualTo(
            Bin.create("123456"),
        )
    }

    @Test
    fun `bin with full number should return bin`() {
        assertThat(
            CardNumber.Unvalidated(CardNumberTestConstants.VISA_WITH_SPACES).bin,
        ).isEqualTo(BinTestConstants.VISA)
    }

    @Test
    fun `validate() with valid visa card number should return Validated object`() {
        val panLength = CardBrand.getCardBrands("42424242 ").first().getMaxLengthForCardNumber("4242 4242 4242 4242")
        assertThat(
            CardNumber.Unvalidated("4242 4242 4242 4242").validate(panLength),
        ).isEqualTo(
            CardNumber.Validated("4242424242424242"),
        )
    }

    @Test
    fun `validate() with valid amex card number should return Validated object`() {
        val panLength = CardBrand.getCardBrands(CardNumberTestConstants.AMEX_BIN).first().getMaxLengthForCardNumber(CardNumberTestConstants.AMEX_WITH_SPACES)
        assertThat(
            CardNumber.Unvalidated(CardNumberTestConstants.AMEX_WITH_SPACES).validate(panLength),
        ).isEqualTo(
            CardNumber.Validated(CardNumberTestConstants.AMEX_NO_SPACES),
        )
    }

    @Test
    fun `validate() with invalid Luhn card number should return null`() {
        assertThat(
            CardNumber.Unvalidated("4242 4242 4242 4243").validate(16),
        ).isNull()
    }

    @Test
    fun `validate() with valid Luhn card number but incorrect length should return null`() {
        assertThat(
            CardNumber.Unvalidated("4242 4242 4242 4242").validate(19),
        ).isNull()
    }

    @Test
    fun `validate() with empty number should return null`() {
        assertThat(
            CardNumber.Unvalidated("").validate(0),
        ).isNull()
    }
}
