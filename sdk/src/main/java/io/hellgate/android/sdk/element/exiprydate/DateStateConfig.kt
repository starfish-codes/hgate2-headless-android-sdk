package io.hellgate.android.sdk.element.exiprydate

import io.hellgate.android.sdk.element.*
import io.hellgate.android.sdk.element.TextFieldState
import io.hellgate.android.sdk.element.TextFieldStateConstants
import org.jetbrains.annotations.VisibleForTesting
import java.util.*

@Suppress("MagicNumber")
internal object DateStateConfig : IStateConfig<String> {
    override fun determineState(input: String): TextFieldState {
        if (input.isBlank()) return TextFieldStateConstants.Error.Blank

        val newDate = convertTo4DigitDate(input)
        return when {
            newDate.length < 4 -> TextFieldStateConstants.Error.Incomplete(FieldError.ErrorType.INCOMPLETE)
            newDate.length > 4 -> TextFieldStateConstants.Error.Invalid(FieldError.ErrorType.INVALID)
            else -> determineExpiryState(
                month1Based = requireNotNull(newDate.take(2).toIntOrNull()),
                twoDigitYear = requireNotNull(newDate.takeLast(2).toIntOrNull()),
                // Calendar.getInstance().get(Calendar.MONTH) is 0-based, so add 1
                current1BasedMonth = Calendar.getInstance().get(Calendar.MONTH) + 1,
                currentYear = Calendar.getInstance().get(Calendar.YEAR),
            )
        }
    }

    @VisibleForTesting
    fun determineExpiryState(
        month1Based: Int,
        twoDigitYear: Int,
        current1BasedMonth: Int,
        currentYear: Int,
    ): TextFieldState {
        val twoDigitCurrentYear = currentYear % 100

        val isExpiredYear = (twoDigitYear - twoDigitCurrentYear) < 0
        val isYearTooLarge = (twoDigitYear - twoDigitCurrentYear) > 50

        val isExpiredMonth = (twoDigitYear - twoDigitCurrentYear) == 0 && current1BasedMonth > month1Based
        val isMonthInvalid = month1Based !in 1..12

        return when {
            isExpiredYear -> TextFieldStateConstants.Error.Invalid(FieldError.ErrorType.INVALID, preventMoreInput = true)
            isYearTooLarge -> TextFieldStateConstants.Error.Invalid(FieldError.ErrorType.INVALID, preventMoreInput = true)
            isExpiredMonth -> TextFieldStateConstants.Error.Invalid(FieldError.ErrorType.INVALID, preventMoreInput = true)
            isMonthInvalid -> TextFieldStateConstants.Error.Incomplete()
            else -> TextFieldStateConstants.Valid.Full
        }
    }

    fun convertTo4DigitDate(input: String) =
        "0$input".takeIf {
            (input.isNotBlank() && !(input[0] == '0' || input[0] == '1')) ||
                ((input.length > 1) && (input[0] == '1' && requireNotNull(input[1].digitToInt()) > 2))
        } ?: input
}
