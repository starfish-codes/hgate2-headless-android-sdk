package io.hellgate.android.sdk.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CardUtilsTest {
    @Test
    fun `Provide valid cardnumber for luhnCheck, Returns true`() {
        val cardNumber = "1234567890123452"
        assertThat(CardUtils.isValidLuhnNumber(cardNumber)).isTrue()

        val cardNumber2 = "2222690420064574"
        assertThat(CardUtils.isValidLuhnNumber(cardNumber2)).isTrue()
    }

    @Test
    fun `Provide invalid cardnumber for luhnCheck, Returns false`() {
        val cardNumber = "1234567890123456"
        assertThat(CardUtils.isValidLuhnNumber(cardNumber)).isFalse()
    }

    @Test
    fun `Provide null cardnumber for luhnCheck, Returns false`() {
        val cardNumber: String? = null
        assertThat(CardUtils.isValidLuhnNumber(cardNumber)).isFalse()
    }

    @Test
    fun `Provide cardnumber with non digit characters for luhnCheck, Returns false`() {
        val cardNumber = "12345b789012345a"
        assertThat(CardUtils.isValidLuhnNumber(cardNumber)).isFalse()
    }
}
