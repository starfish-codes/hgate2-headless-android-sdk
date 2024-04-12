package io.hellgate.android.sdk.element.card

import io.hellgate.android.sdk.element.IStateConfig
import io.hellgate.android.sdk.element.TextFieldState
import io.hellgate.android.sdk.element.TextFieldStateConstants
import io.hellgate.android.sdk.model.CardBrand
import io.hellgate.android.sdk.model.CardUtils

internal object CardNumberStateConfig : IStateConfig<CardNumberStateInput> {
    override fun determineState(input: CardNumberStateInput): TextFieldState {
        val (brand, number) = input
        if (number.isBlank()) return TextFieldStateConstants.Error.Blank
        if (brand == CardBrand.Unknown) return TextFieldStateConstants.Error.Invalid(preventMoreInput = true)

        val numberAllowedDigits = brand.getMaxLengthForCardNumber(number)
        val luhnValid = CardUtils.isValidLuhnNumber(number)
        val isDigitLimit = brand.getMaxLengthForCardNumber(number) != -1

        return when {
            isDigitLimit && number.length < numberAllowedDigits -> TextFieldStateConstants.Error.Incomplete()
            !luhnValid -> TextFieldStateConstants.Error.Invalid(preventMoreInput = true)
            isDigitLimit && number.length == numberAllowedDigits -> TextFieldStateConstants.Valid.Full
            else -> TextFieldStateConstants.Error.Invalid()
        }
    }
}

internal data class CardNumberStateInput(
    val brand: CardBrand,
    val number: String,
)
