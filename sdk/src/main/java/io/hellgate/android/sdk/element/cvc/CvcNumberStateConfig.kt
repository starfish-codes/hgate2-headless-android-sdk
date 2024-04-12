package io.hellgate.android.sdk.element.cvc

import io.hellgate.android.sdk.element.IStateConfig
import io.hellgate.android.sdk.element.TextFieldState
import io.hellgate.android.sdk.element.TextFieldStateConstants

internal object CvcNumberStateConfig : IStateConfig<CvcNumberStateInput> {
    override fun determineState(input: CvcNumberStateInput): TextFieldState {
        val (number, numberAllowedDigits) = input
        return when {
            number.isEmpty() -> TextFieldStateConstants.Error.Blank
            number.length == numberAllowedDigits -> TextFieldStateConstants.Valid.Full
            number.length < numberAllowedDigits -> TextFieldStateConstants.Error.Incomplete()
            else -> TextFieldStateConstants.Error.Invalid(preventMoreInput = true)
        }
    }
}

internal data class CvcNumberStateInput(
    val number: String,
    val numberAllowedDigits: Int,
)
