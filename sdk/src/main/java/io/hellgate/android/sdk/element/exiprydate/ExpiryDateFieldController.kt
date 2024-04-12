package io.hellgate.android.sdk.element.exiprydate

import io.hellgate.android.sdk.element.IFieldController
import io.hellgate.android.sdk.element.TextFieldState
import kotlinx.coroutines.flow.*

internal class ExpiryDateFieldController : IFieldController {
    private val stateConfig = DateStateConfig

    private val _fieldValue: MutableStateFlow<String> = MutableStateFlow("")
    private val _hasFocus: MutableStateFlow<Boolean> = MutableStateFlow(false)

    override val fieldValue: Flow<String> = _fieldValue.asStateFlow()
    override val fieldState: Flow<TextFieldState> = _fieldValue.map { stateConfig.determineState(it) }
    override val hasFocus: Flow<Boolean> = _hasFocus.asStateFlow()

    override val visibleError: Flow<Boolean> =
        combine(fieldState, _hasFocus) { fieldState, hasFocus ->
            fieldState.shouldShowError(hasFocus)
        }

    override fun onValueChanged(value: String) {
        _fieldValue.value = value.filter { it.isDigit() }
    }

    override fun onFocusChange(newHasFocus: Boolean) {
        _hasFocus.value = newHasFocus
    }
}
