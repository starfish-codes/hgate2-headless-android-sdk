package io.hellgate.android.sdk.element.exiprydate

import io.hellgate.android.sdk.element.FieldError
import io.hellgate.android.sdk.element.TextFieldStateConstants
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.*

class DateStateConfigTest {
    @Test
    fun `blank number returns blank state`() {
        assertThat(DateStateConfig.determineState("")).isEqualTo(TextFieldStateConstants.Error.Blank)
    }

    @Test
    fun `incomplete number is in incomplete state`() {
        val state = DateStateConfig.determineState("12")
        assertThat(state).isInstanceOf(TextFieldStateConstants.Error.Incomplete::class.java)
        assertThat(state.getError()?.errorType).isEqualTo(FieldError.ErrorType.INCOMPLETE)
    }

    @Test
    fun `date is too long`() {
        val state = DateStateConfig.determineState("1234567890123456789")
        assertThat(state).isInstanceOf(TextFieldStateConstants.Error.Invalid::class.java)
        assertThat(state.getError()?.errorType).isEqualTo(FieldError.ErrorType.INVALID)
    }

    @Test
    fun `date invalid month and 2 digit year`() {
        val state = DateStateConfig.determineState("1955")
        assertThat(state).isInstanceOf(TextFieldStateConstants.Error.Invalid::class.java)
        assertThat(state.getError()?.errorType).isEqualTo(FieldError.ErrorType.INVALID)
    }

    @Test
    fun `date in the past`() {
        val state = DateStateConfig.determineState("1299")
        assertThat(state).isInstanceOf(TextFieldStateConstants.Error.Invalid::class.java)
        assertThat(state.getError()?.errorType).isEqualTo(FieldError.ErrorType.INVALID)
    }

    @Test
    fun `current month and year`() {
        val input = produceInput(
            month = get1BasedCurrentMonth(),
            year = Calendar.getInstance().get(Calendar.YEAR) % 100,
        )

        val state = DateStateConfig.determineState(input)
        assertThat(state).isInstanceOf(TextFieldStateConstants.Valid.Full::class.java)
    }

    @Test
    fun `next month`() {
        var month = get1BasedCurrentMonth()
        var year = Calendar.getInstance().get(Calendar.YEAR) % 100

        if (month == 12) {
            month = 1
            year += 1
        }

        val input = produceInput(month, year)
        val state = DateStateConfig.determineState(input)

        assertThat(state).isInstanceOf(TextFieldStateConstants.Valid.Full::class.java)
    }

    @Test
    fun `current month and year + 1`() {
        val input = produceInput(
            month = get1BasedCurrentMonth(),
            year = (Calendar.getInstance().get(Calendar.YEAR) + 1) % 100,
        )
        val state = DateStateConfig.determineState(input)

        assertThat(state).isInstanceOf(TextFieldStateConstants.Valid.Full::class.java)
    }

    @Test
    fun `current month - 1 and year, Returns Invalid`() {
        var previousMonth = get1BasedCurrentMonth() - 1
        var year = Calendar.getInstance().get(Calendar.YEAR) % 100
        var expected = FieldError.ErrorType.INVALID

        // On January, use December of previous year.
        if (previousMonth == 0) {
            previousMonth = 12
            year -= 1
            expected = FieldError.ErrorType.INVALID
        }

        val input = produceInput(
            month = previousMonth,
            year = year,
        )

        val state = DateStateConfig.determineState(input)

        assertThat(state).isInstanceOf(TextFieldStateConstants.Error.Invalid::class.java)
        assertThat(state.getError()?.errorType).isEqualTo(expected)
    }

    @Test
    fun `current month and year - 1`() {
        val input = produceInput(
            month = get1BasedCurrentMonth(),
            year = (Calendar.getInstance().get(Calendar.YEAR) - 1) % 100,
        )

        val state = DateStateConfig.determineState(input)

        assertThat(state).isInstanceOf(TextFieldStateConstants.Error.Invalid::class.java)
        assertThat(state.getError()?.errorType).isEqualTo(FieldError.ErrorType.INVALID)
    }

    @Test
    fun `card expire 51 years from now`() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val state = DateStateConfig.determineExpiryState(
            month1Based = 3,
            twoDigitYear = (currentYear + 51) % 100,
            current1BasedMonth = 2,
            currentYear = currentYear,
        )

        assertThat(state).isInstanceOf(TextFieldStateConstants.Error.Invalid::class.java)
        assertThat(state.getError()?.errorType).isEqualTo(FieldError.ErrorType.INVALID)
    }

    @Test
    fun `card expire 50 years from now`() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val state = DateStateConfig.determineExpiryState(
            month1Based = 3,
            twoDigitYear = (currentYear + 50) % 100,
            current1BasedMonth = 2,
            currentYear = currentYear,
        )

        assertThat(state).isInstanceOf(TextFieldStateConstants.Valid.Full::class.java)
    }

    @Test
    fun `Input invalid month, returns Incomplete`() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val state = DateStateConfig.determineExpiryState(
            month1Based = -2,
            twoDigitYear = (currentYear + 50) % 100,
            current1BasedMonth = 2,
            currentYear = currentYear,
        )

        assertThat(state).isInstanceOf(TextFieldStateConstants.Error.Incomplete::class.java)
    }

    private fun get1BasedCurrentMonth() = Calendar.getInstance().get(Calendar.MONTH) + 1

    @Test
    fun `date is valid 2 digit month and 2 digit year`() {
        val state = DateStateConfig.determineState("1255")
        assertThat(state).isInstanceOf(TextFieldStateConstants.Valid.Full::class.java)
    }

    @Test
    fun `date is invalid 2X month and 2 digit year`() {
        val state = DateStateConfig.determineState("2123")
        assertThat(state).isInstanceOf(TextFieldStateConstants.Error.Invalid::class.java)
        assertThat(state.getError()?.errorType).isEqualTo(FieldError.ErrorType.INVALID)
    }

    @Test
    fun `date is valid one digit month two digit year`() {
        val state = DateStateConfig.determineState("130")
        assertThat(state).isInstanceOf(TextFieldStateConstants.Valid.Full::class.java)
    }

    @Test
    fun `date is valid 0X month two digit year`() {
        val state = DateStateConfig.determineState("0130")
        assertThat(state).isInstanceOf(TextFieldStateConstants.Valid.Full::class.java)
    }

    @Test
    fun `date is valid 2X month and 2 digit year`() {
        val state = DateStateConfig.determineState("230")
        assertThat(state).isInstanceOf(TextFieldStateConstants.Valid.Full::class.java)
    }

    private fun produceInput(
        month: Int,
        year: Int,
    ): String {
        val formattedMonth = month.toString().padStart(length = 2, padChar = '0')
        val formattedYear = year.toString().padStart(length = 2, padChar = '0')
        return formattedMonth + formattedYear
    }
}
