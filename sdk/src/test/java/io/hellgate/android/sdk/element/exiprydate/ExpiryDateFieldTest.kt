package io.hellgate.android.sdk.element.exiprydate

import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.KEYCODE_1
import android.view.KeyEvent.KEYCODE_3
import android.view.KeyEvent.KEYCODE_5
import androidx.compose.material3.TextField
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import io.hellgate.android.sdk.*
import io.hellgate.android.sdk.element.FieldError
import io.hellgate.android.sdk.element.FieldState
import io.hellgate.android.sdk.element.exiprydate.ExpiryDateField.Companion.TEST_TAG
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ExpiryDateFieldTest : ComposeRoboTest() {
    @Test
    fun `Field is filled with 4 digit date from the past, No further input is accepted and field shows error`() = runTest {
        val expiryDateField = ExpiryDateField()
        var expiryDateFieldState: FieldState? = null
        var onFocusedCount = 0
        var onBlurCount = 0

        composeTestRule.setContent {
            expiryDateField.ComposeUI(
                onValueChange = { expiryDateFieldState = it },
                onFocused = { onFocusedCount++ },
                onBlur = { onBlurCount++ },
                modifier = Modifier,
            )

            TextField(value = "Hello, World!", modifier = Modifier.testTag("AnotherTag"), onValueChange = { })
        }

        // Check initial state
        assertThat(expiryDateField).isNotNull
        assertThat(expiryDateFieldState).isEqualTo(FieldState())
        assertThat(onBlurCount).isEqualTo(0)
        assertThat(onFocusedCount).isEqualTo(0)
        field().assertIsDisplayed()
            .assertTextEquals("MM / YY", "")

        // Perform input of invalid date
        field().performTextInput("1223")

        field().assertIsFocused()
            .assertTextEquals("MM / YY", "12 / 23")
            .assertIsTextFieldError()

        // Perform input of additional invalid number
        field().performTextInput("1")

        field().assertTextEquals("MM / YY", "12 / 23")
            .assertIsFocused()

        printTree(TEST_TAG)

        composeTestRule.onNodeWithTag("AnotherTag").performClick()
            .assertIsFocused()

        field()
            .assertIsNotFocused()

        // Check final state
        assertThat(expiryDateField.value()).isEqualTo("1223")
        assertThat(expiryDateFieldState).isEqualTo(FieldState(valid = false, empty = false, error = listOf(FieldError(FieldError.ErrorType.INVALID))))
        assertThat(onBlurCount).isEqualTo(1)
        assertThat(onFocusedCount).isEqualTo(1)
    }

    private fun field() = composeTestRule.onNodeWithTag(TEST_TAG)

    @Test
    fun `Valid Date 1,3,5 is typed in, Results in field with no error and valid state`() = runTest {
        val expiryDateField = ExpiryDateField()
        var expiryDateFieldState: FieldState? = null
        var onFocusedCount = 0
        var onBlurCount = 0

        composeTestRule.setContent {
            expiryDateField.ComposeUI(
                onValueChange = { expiryDateFieldState = it },
                onFocused = { onFocusedCount++ },
                onBlur = { onBlurCount++ },
                modifier = Modifier,
            )

            TextField(value = "Hello, World!", modifier = Modifier.testTag("AnotherTag"), onValueChange = { })
        }

        // Check initial state
        assertThat(expiryDateField).isNotNull
        assertThat(expiryDateFieldState).isEqualTo(FieldState(error = listOf(FieldError(FieldError.ErrorType.BLANK))))
        assertThat(onBlurCount).isEqualTo(0)
        assertThat(onFocusedCount).isEqualTo(0)
        field().assertIsDisplayed()
            .assertIsNotFocused()
            .assertTextEquals("MM / YY", "")

        // Perform input of digit 1
        field().performSemanticsAction(SemanticsActions.RequestFocus)
        field().performKeyPress(KeyEvent(android.view.KeyEvent(ACTION_DOWN, KEYCODE_1)))

        field().assertTextEquals("MM / YY", "1")
            .assertIsFocused()
            .assertIsNotTextFieldError()

        // Perform input of digit 3
        field().performKeyPress(KeyEvent(android.view.KeyEvent(ACTION_DOWN, KEYCODE_3)))
        field().assertTextEquals("MM / YY", "1 / 3")
            .assertIsFocused()
            .assertIsNotTextFieldError()
        assertThat(expiryDateFieldState).isEqualTo(FieldState(valid = false, empty = false, error = listOf(FieldError(FieldError.ErrorType.INCOMPLETE))))

        // Perform input of digit 5
        field().performKeyPress(KeyEvent(android.view.KeyEvent(ACTION_DOWN, KEYCODE_5)))
        field().assertTextEquals("MM / YY", "1 / 35")
            .assertIsFocused()
            .assertIsNotTextFieldError()
        assertThat(expiryDateFieldState).isEqualTo(FieldState(valid = true, empty = false, error = listOf()))

        val result = expiryDateField.value()
        assertThat(result).isEqualTo("0135")
    }
}
