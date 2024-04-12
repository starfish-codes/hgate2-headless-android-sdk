package io.hellgate.android.sdk.model

import io.hellgate.android.sdk.element.card.CardNumberTestConstants
import io.hellgate.android.sdk.model.BinTestConstants.MASTERCARD
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class BinTest {
    @Test
    fun `create() with 2 digit partial card number should return null`() {
        assertThat(Bin.create("42")).isNull()
    }

    @Test
    fun `create() with 6 digit partial card number should return BIN`() {
        assertThat(BinTestConstants.VISA).isEqualTo(Bin(DEFAULT_BIN))
    }

    @Test
    fun `create() with full card number should return BIN`() {
        assertThat(Bin.create(CardNumberTestConstants.VISA_NO_SPACES))
            .isEqualTo(Bin(DEFAULT_BIN))
        assertThat(Bin.create(CardNumberTestConstants.MASTERCARD_NO_SPACES))
            .isEqualTo(MASTERCARD)
    }

    private companion object {
        private const val DEFAULT_BIN = "424242"
    }
}

internal object BinTestConstants {
    val VISA = requireNotNull(Bin.create(CardNumberTestConstants.VISA_NO_SPACES))
    val MASTERCARD = requireNotNull(Bin.create(CardNumberTestConstants.MASTERCARD_NO_SPACES))
}
