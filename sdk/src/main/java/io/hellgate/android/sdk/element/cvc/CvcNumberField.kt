package io.hellgate.android.sdk.element.cvc

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import io.hellgate.android.sdk.element.*
import io.hellgate.android.sdk.element.TextFieldStateConstants
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * A composable representing a CVC number field.
 * @param maxLengthInput A flow containing the maximum length of the CVC/CVV according
 * to the cardbrand identified and emitted by the numberField. Can only be 3 or 4. Other values will be ignored.
 * @see FieldComposable
 */
class CvcNumberField(
    private val maxLengthInput: Flow<Int> = flowOf(CVC_LENGTH),
) {
    private val controller = CvcNumberFieldController(maxLengthInput)

    internal suspend fun value(): String = controller.fieldValue.first()

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
        val fieldState by controller.fieldState.collectAsState(TextFieldStateConstants.Error.Blank)
        val maxLengthValue by controller.codeMaxLength.collectAsState(CVC_LENGTH)

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
            keyboardOptions = FieldDefaults.keyboardOptions,
            isError = isErrorVisible,
            colors = colors,
            shape = shape,
            label = { Text(if (maxLengthValue == CVV_LENGTH) CVV_HINT else CVC_HINT) },
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
                if (fieldState.canAcceptInput(value, it)) controller.onValueChanged(it)
            },
        )
    }

    internal companion object {
        private const val CVV_HINT: String = "CVV"
        private const val CVC_HINT: String = "CVC"

        internal const val CVC_LENGTH: Int = 3
        internal const val CVV_LENGTH: Int = 4
        internal const val TEST_TAG: String = "CardNumberTextField"
    }
}
