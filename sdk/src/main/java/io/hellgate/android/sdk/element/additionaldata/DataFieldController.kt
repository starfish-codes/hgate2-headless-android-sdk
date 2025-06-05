package io.hellgate.android.sdk.element.additionaldata

import io.hellgate.android.sdk.element.AdditionalDataFieldState
import kotlinx.coroutines.flow.*

internal class DataFieldController {
    private val _fieldValue = MutableStateFlow("")

    val fieldValue: Flow<String> = _fieldValue
    val fieldState: Flow<AdditionalDataFieldState> = _fieldValue.map { determineState(it) }

    internal fun onValueChanged(value: String) {
        _fieldValue.value = value
    }

    internal fun determineState(value: String): AdditionalDataFieldState =
        AdditionalDataFieldState(
            empty = value.isEmpty(),
            value = value,
        )
}
