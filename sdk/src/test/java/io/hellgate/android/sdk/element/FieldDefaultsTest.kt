package io.hellgate.android.sdk.element

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FieldDefaultsTest {
    @Test
    fun getKeyboardOptions() {
        val keyboardOptions = FieldDefaults.keyboardOptions
        assertThat(keyboardOptions).isNotNull.isEqualTo(
            KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Next,
            ),
        )
    }
}
