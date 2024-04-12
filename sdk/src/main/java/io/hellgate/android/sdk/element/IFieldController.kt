package io.hellgate.android.sdk.element

import kotlinx.coroutines.flow.Flow

internal interface IFieldController {
    val fieldValue: Flow<String>
    val fieldState: Flow<TextFieldState>
    val hasFocus: Flow<Boolean>
    val visibleError: Flow<Boolean>

    fun onValueChanged(value: String)

    fun onFocusChange(newHasFocus: Boolean)
}
