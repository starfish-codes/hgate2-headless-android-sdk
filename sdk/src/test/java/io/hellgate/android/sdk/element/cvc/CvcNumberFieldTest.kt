package io.hellgate.android.sdk.element.cvc

import androidx.compose.material3.TextField
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import io.hellgate.android.sdk.*
import io.hellgate.android.sdk.element.FieldError
import io.hellgate.android.sdk.element.FieldState
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CvcNumberFieldTest : ComposeRoboTest() {
    @Test
    fun `Scenario, Outcome`() = runTest {
        val cvcNumberField = CvcNumberField()
        var cvcNumberFieldState: FieldState? = null
        var onFocusedCount = 0
        var onBlurCount = 0

        composeTestRule.setContent {
            cvcNumberField.ComposeUI(
                onValueChange = { cvcNumberFieldState = it },
                onFocused = { onFocusedCount++ },
                onBlur = { onBlurCount++ },
                modifier = Modifier,
            )
            TextField(value = "Hello, World!", modifier = Modifier.testTag("AnotherTag"), onValueChange = { })
        }

        // Check initial state
        assertThat(cvcNumberField).isNotNull
        assertThat(cvcNumberFieldState).isEqualTo(FieldState())
        assertThat(onBlurCount).isEqualTo(0)
        assertThat(onFocusedCount).isEqualTo(0)

        // Perform input of valid CVC number
        field().performTextInput("123")

        field().assertIsFocused()
            .assertTextEquals("CVC", "123")
            .assertIsNotTextFieldError()

        // Check final state
        assertThat(cvcNumberFieldState).isEqualTo(FieldState(valid = true, empty = false, error = emptyList()))
        assertThat(onFocusedCount).isEqualTo(1)
        assertThat(onBlurCount).isEqualTo(0)

        // Loose focus
        composeTestRule.onNodeWithTag("AnotherTag").performClick()
        assertThat(onBlurCount).isEqualTo(1)
        field().assertIsNotFocused()
    }

    @Test
    fun `Type 3 digits of a 4 digit cvv and lose focus, Field is displayed with error and state is updated accordingly`() = runTest {
        val cvcNumberField = CvcNumberField(flowOf(4))
        var cvcNumberFieldState: FieldState? = null
        var onFocusedCount = 0
        var onBlurCount = 0

        composeTestRule.setContent {
            cvcNumberField.ComposeUI(
                onValueChange = { cvcNumberFieldState = it },
                onFocused = { onFocusedCount++ },
                onBlur = { onBlurCount++ },
                modifier = Modifier,
            )
            TextField(value = "Hello, World!", modifier = Modifier.testTag("AnotherTag"), onValueChange = { })
        }

        // Check initial state
        assertThat(cvcNumberField).isNotNull
        assertThat(cvcNumberFieldState).isEqualTo(FieldState())
        assertThat(onBlurCount).isEqualTo(0)
        assertThat(onFocusedCount).isEqualTo(0)

        // Perform input of valid CVC number
        field().performTextInput("123")

        field().assertIsFocused()
            .assertTextEquals("CVV", "123")
            .assertIsNotTextFieldError()

        // Loose focus
        composeTestRule.onNodeWithTag("AnotherTag").performClick()
        assertThat(onBlurCount).isEqualTo(1)
        field().assertIsNotFocused()
            .assertIsTextFieldError()

        // Check final state
        assertThat(cvcNumberFieldState).isEqualTo(FieldState(valid = false, empty = false, error = listOf(FieldError(FieldError.ErrorType.INCOMPLETE))))
        assertThat(onFocusedCount).isEqualTo(1)
    }

    private fun field() = composeTestRule.onNodeWithTag(CvcNumberField.TEST_TAG)
}
