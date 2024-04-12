package io.hellgate.android.sdk.element.additionaldata

import androidx.compose.material3.TextField
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import io.hellgate.android.sdk.*
import io.hellgate.android.sdk.element.AdditionalDataFieldState
import io.hellgate.android.sdk.element.additionaldata.DataField.Companion.TEST_TAG
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.*
import org.junit.Test

class DataFieldTest : ComposeRoboTest() {
    private fun field() = composeTestRule.onNodeWithTag(TEST_TAG + AdditionalDataTypes.CARDHOLDER_NAME.name)

    @Test
    fun `Type in some text, events get emitted`() = runTest {
        val cardholderField = DataField(additionalDataTypes = AdditionalDataTypes.CARDHOLDER_NAME)
        var cardholderFieldState: AdditionalDataFieldState? = null
        var onFocusedCount = 0
        var onBlurCount = 0

        composeTestRule.setContent {
            cardholderField.ComposeUI(
                onValueChange = { cardholderFieldState = it },
                isErrorVisible = false,
                modifier = Modifier,
                onFocused = { onFocusedCount++ },
                onBlur = { onBlurCount++ },
            )

            TextField(value = "Hello, World!", modifier = Modifier.testTag("AnotherTag"), onValueChange = { })
        }

        // Check initial state
        assertThat(cardholderField).isNotNull
        assertThat(cardholderFieldState).isEqualTo(AdditionalDataFieldState())
        assertThat(onBlurCount).isEqualTo(0)
        assertThat(onFocusedCount).isEqualTo(0)
        field()
            .assertIsDisplayed()
            .assertTextEquals("Cardholder Name", "")

        field().apply { performTextInput("John Doe") }
            .assertTextEquals("Cardholder Name", "John Doe")
            .assertIsNotTextFieldError()

        // Check final state
        assertThat(cardholderField.value()).isEqualTo("John Doe")
        assertThat(cardholderFieldState).isEqualTo(AdditionalDataFieldState(empty = false, value = "John Doe"))
        assertThat(onFocusedCount).isEqualTo(1)
        assertThat(onBlurCount).isEqualTo(0)

        // Loose focus
        composeTestRule.onNodeWithTag("AnotherTag").performSemanticsAction(SemanticsActions.RequestFocus)
        assertThat(onFocusedCount).isEqualTo(1)
        assertThat(onBlurCount).isEqualTo(1)
    }

    @Test
    fun `Test setting isErrorVisible, Property error is in semantics tree`() = runTest {
        val cardholderField = DataField(additionalDataTypes = AdditionalDataTypes.CARDHOLDER_NAME)
        var cardholderFieldState: AdditionalDataFieldState? = null
        var onFocusedCount = 0
        var onBlurCount = 0

        composeTestRule.setContent {
            cardholderField.ComposeUI(
                onValueChange = { cardholderFieldState = it },
                isErrorVisible = true,
                modifier = Modifier,
                onFocused = { onFocusedCount++ },
                onBlur = { onBlurCount++ },
            )

            TextField(value = "Hello, World!", modifier = Modifier.testTag("AnotherTag"), onValueChange = { })
        }

        field().apply { performTextInput("John Doe") }
            .assertTextEquals("Cardholder Name", "John Doe")
            .assertIsTextFieldError()

        // Check final state
        assertThat(cardholderField.value()).isEqualTo("John Doe")
        assertThat(cardholderFieldState).isEqualTo(AdditionalDataFieldState(empty = false, value = "John Doe"))
    }

    @Test
    fun `Set field label manual to specific value, Field is drawn with specified value`() = runTest {
        val testLabel = "Expected Label"
        val cardholderField = DataField(additionalDataTypes = AdditionalDataTypes.CARDHOLDER_NAME, testLabel)
        var cardholderFieldState: AdditionalDataFieldState? = null

        composeTestRule.setContent {
            cardholderField.ComposeUI({ cardholderFieldState = it }, true)
        }

        field().apply { performTextInput("John Doe") }
            .assertTextEquals(testLabel, "John Doe")
            .assertIsTextFieldError()

        // Check final state
        assertThat(cardholderField.value()).isEqualTo("John Doe")
        assertThat(cardholderFieldState).isEqualTo(AdditionalDataFieldState(empty = false, value = "John Doe"))
    }
}
