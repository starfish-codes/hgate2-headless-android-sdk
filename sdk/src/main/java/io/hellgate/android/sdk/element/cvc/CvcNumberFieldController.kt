package io.hellgate.android.sdk.element.cvc

import io.hellgate.android.sdk.element.IFieldController
import io.hellgate.android.sdk.element.TextFieldState
import kotlinx.coroutines.flow.*

internal class CvcNumberFieldController(
    maxLengthInput: Flow<Int>,
) : IFieldController {
    private val stateConfig = CvcNumberStateConfig

    private val _fieldValue = MutableStateFlow("")
    private val _hasFocus = MutableStateFlow(false)

    val codeMaxLength: Flow<Int> = maxLengthInput.map { it.coerceIn(CvcNumberField.CVC_LENGTH, CvcNumberField.CVV_LENGTH) }

    override val fieldValue: Flow<String> = _fieldValue
    override val fieldState: Flow<TextFieldState> = combine(_fieldValue, codeMaxLength) { number, numberAllowedDigits ->
        stateConfig.determineState(CvcNumberStateInput(number, numberAllowedDigits))
    }
    override val hasFocus: Flow<Boolean> = _hasFocus
    override val visibleError: Flow<Boolean> = combine(fieldState, _hasFocus) { fieldState, hasFocus ->
        fieldState.shouldShowError(hasFocus)
    }

    override fun onValueChanged(value: String) {
        _fieldValue.value = value.filter { it.isDigit() }
    }

    override fun onFocusChange(newHasFocus: Boolean) {
        _hasFocus.value = newHasFocus
    }
}
