package io.hellgate.android.sdk.element

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.*

internal object FieldDefaults {
    val keyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.None,
        keyboardType = KeyboardType.NumberPassword,
        imeAction = ImeAction.Next,
    )
}
