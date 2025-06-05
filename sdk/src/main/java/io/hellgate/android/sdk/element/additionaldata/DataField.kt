package io.hellgate.android.sdk.element.additionaldata

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.hellgate.android.sdk.element.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * A composable representing a data field.
 * @param additionalDataTypes The type of data field.
 * @param fieldLabel The label of the field. Default is the label of the data type. Provide a custom label / i18n if needed.
 */
class DataField(
    val additionalDataTypes: AdditionalDataTypes,
    private val fieldLabel: String = additionalDataTypes.getLabel(),
) {
    private val controller = DataFieldController()

    internal suspend fun value(): String = controller.fieldValue.first()

    @Composable
    fun ComposeUI(
        onValueChange: (AdditionalDataFieldState) -> Unit,
        isErrorVisible: Boolean,
        modifier: Modifier = Modifier,
        onFocused: () -> Unit = {},
        onBlur: () -> Unit = {},
        colors: TextFieldColors = TextFieldDefaults.colors(errorTextColor = MaterialTheme.colorScheme.error),
        shape: Shape = OutlinedTextFieldDefaults.shape,
    ) {
        val value by controller.fieldValue.collectAsState("")
        val scope = rememberCoroutineScope()

        var hasFocus by remember { mutableStateOf(false) }

        DisposableEffect(LocalLifecycleOwner.current) {
            scope.launch {
                controller.fieldState.collect { onValueChange(it) }
            }
            onDispose { scope.cancel() }
        }

        OutlinedTextField(
            value = value,
            isError = isErrorVisible,
            colors = colors,
            shape = shape,
            label = { Text(fieldLabel) },
            modifier = modifier
                .testTag(TEST_TAG + additionalDataTypes.name)
                .onFocusChanged {
                    if (hasFocus != it.isFocused) {
                        if (it.isFocused) onFocused()
                        if (!it.isFocused) onBlur()
                    }
                    hasFocus = it.isFocused
                },
            onValueChange = { controller.onValueChanged(it) },
            singleLine = true,
        )
    }

    internal companion object {
        internal const val TEST_TAG = "AdditionalDataField_"
    }
}
