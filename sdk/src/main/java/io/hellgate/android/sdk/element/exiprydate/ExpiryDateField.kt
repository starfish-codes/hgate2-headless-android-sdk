package io.hellgate.android.sdk.element.exiprydate

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.hellgate.android.sdk.element.*
import io.hellgate.android.sdk.element.FieldDefaults.keyboardOptions
import io.hellgate.android.sdk.element.TextFieldStateConstants.Error
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * A composable representing an expiry date field.
 */
class ExpiryDateField {
    private val controller = ExpiryDateFieldController()

    internal suspend fun value(): String = controller.fieldValue.map { DateStateConfig.convertTo4DigitDate(it) }.first()

    /**
     * Composable function to render the card number field.
     * @param onValueChange The callback to be invoked when the value of the field changes.
     * @param modifier The modifier to be applied to the field.
     * @param onFocused The callback to be invoked when the field gains focus.
     * @param onBlur The callback to be invoked when the field loses focus.
     * @param colors The colors to be applied to the field.
     * @param shape The shape to be applied to the field.
     */
    @Composable
    fun ComposeUI(
        onValueChange: (FieldState) -> Unit,
        modifier: Modifier = Modifier,
        onFocused: () -> Unit = {},
        onBlur: () -> Unit = {},
        colors: TextFieldColors = TextFieldDefaults.colors(errorTextColor = MaterialTheme.colorScheme.error),
        shape: Shape = OutlinedTextFieldDefaults.shape,
    ) {
        val value by controller.fieldValue.collectAsState("")
        val isErrorVisible by controller.visibleError.collectAsState(false)
        val fieldState = controller.fieldState.collectAsState(Error.Blank)

        var hasFocus by rememberSaveable { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        DisposableEffect(LocalLifecycleOwner.current) {
            scope.launch {
                controller.fieldState.collect { onValueChange(it.toPublicFieldState()) }
            }
            onDispose { scope.cancel() }
        }

        // TODO Using a OutlinedTextField instead of a TextField for now to avoid this issue: https://issuetracker.google.com/issues/286260340
        OutlinedTextField(
            value = value,
            keyboardOptions = keyboardOptions,
            visualTransformation = ExpiryDateVisualTransformation(),
            isError = isErrorVisible,
            colors = colors,
            shape = shape,
            label = { Text(LABEL) },
            modifier = modifier
                .testTag(TEST_TAG)
                .onFocusChanged {
                    if (hasFocus != it.isFocused) {
                        controller.onFocusChange(it.isFocused)
                        if (it.isFocused) onFocused()
                        if (!it.isFocused) onBlur()
                    }
                    hasFocus = it.isFocused
                },
            onValueChange = {
                if (fieldState.value.canAcceptInput(value, it)) {
                    controller.onValueChanged(it)
                }
            },
        )
    }

    internal companion object {
        internal const val TEST_TAG: String = "ExpiryDateTextField"
        private const val LABEL: String = "MM / YY"
    }
}
