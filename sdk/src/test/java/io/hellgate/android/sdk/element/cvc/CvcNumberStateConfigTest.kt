package io.hellgate.android.sdk.element.cvc

import io.hellgate.android.sdk.element.FieldError
import io.hellgate.android.sdk.element.TextFieldStateConstants
import io.hellgate.android.sdk.model.CardBrand
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CvcNumberStateConfigTest {
    private val cvcConfig = CvcNumberStateConfig

    @Test
    fun `blank Number returns blank state`() {
        assertThat(cvcConfig.determineState(CvcNumberStateInput("", CardBrand.Visa.maxCvcLength)))
            .isEqualTo(TextFieldStateConstants.Error.Blank)
    }

    @Test
    fun `card brand is invalid`() {
        val state = cvcConfig.determineState(CvcNumberStateInput("0", CardBrand.Unknown.maxCvcLength))
        assertThat(state).isInstanceOf(TextFieldStateConstants.Error.Incomplete::class.java)
    }

    @Test
    fun `incomplete number is in incomplete state`() {
        val state = cvcConfig.determineState(CvcNumberStateInput("12", CardBrand.Visa.maxCvcLength))
        assertThat(state).isInstanceOf(TextFieldStateConstants.Error.Incomplete::class.java)
        assertThat(state.getError()?.errorType).isEqualTo(FieldError.ErrorType.INCOMPLETE)
    }

    @Test
    fun `cvc is too long`() {
        val state = cvcConfig.determineState(CvcNumberStateInput("1234567890123456789", CardBrand.Visa.maxCvcLength))
        assertThat(state).isInstanceOf(TextFieldStateConstants.Error.Invalid::class.java)
        assertThat(state.getError()?.errorType).isEqualTo(FieldError.ErrorType.INVALID)
    }

    @Test
    fun `cvc is valid`() {
        var state = cvcConfig.determineState(CvcNumberStateInput("123", CardBrand.Visa.maxCvcLength))
        assertThat(state).isInstanceOf(TextFieldStateConstants.Valid.Full::class.java)

        state = cvcConfig.determineState(CvcNumberStateInput("1234", CardBrand.AmericanExpress.maxCvcLength))
        assertThat(state).isInstanceOf(TextFieldStateConstants.Valid.Full::class.java)

        state = cvcConfig.determineState(CvcNumberStateInput("1234", CardBrand.Unknown.maxCvcLength))
        assertThat(state).isInstanceOf(TextFieldStateConstants.Valid.Full::class.java)
    }
}
