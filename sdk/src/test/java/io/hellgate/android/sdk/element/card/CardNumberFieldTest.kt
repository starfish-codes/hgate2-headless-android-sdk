package io.hellgate.android.sdk.element.card

import androidx.compose.material3.TextField
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import io.hellgate.android.sdk.*
import io.hellgate.android.sdk.R
import io.hellgate.android.sdk.element.*
import io.hellgate.android.sdk.element.card.CardNumberField.Companion.TEST_TAG
import io.hellgate.android.sdk.model.CardNumber
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CardNumberFieldTest : ComposeRoboTest() {
    @Test
    fun `Type in valid Visa Card number, Returns correct states and value`() = runTest {
        val cardNumberField = CardNumberField()
        var cardNumberFieldState: FieldState? = null
        var onFocusedCount = 0
        var onBlurCount = 0

        composeTestRule.setContent {
            cardNumberField.ComposeUI(
                onValueChange = { cardNumberFieldState = it },
                onFocused = { onFocusedCount++ },
                onBlur = { onBlurCount++ },
                modifier = Modifier,
            )

            TextField(value = "Hello, World!", modifier = Modifier.testTag("AnotherTag"), onValueChange = { })
        }

        // Check initial state
        assertThat(cardNumberField).isNotNull
        assertThat(cardNumberFieldState).isEqualTo(FieldState())
        assertThat(onBlurCount).isEqualTo(0)
        assertThat(onFocusedCount).isEqualTo(0)
        field().assertIsDisplayed()
            .assertTextEquals("Card Number", "")

        // Perform input of valid Visa Card number
        field().performTextInput("4111111111111111")

        field().assertIsFocused()
            .assertTextEquals("Card Number", "4111 1111 1111 1111")
            .assertIsNotTextFieldError()
        assertCardBrandIconIsDisplayed(R.drawable.ic_cards_visa.toString())

        // Check final state
        assertThat(cardNumberField.validatedValue()).isEqualTo(CardNumber.Validated("4111111111111111"))
        assertThat(cardNumberFieldState).isEqualTo(FieldState(valid = true, empty = false, error = emptyList()))
        assertThat(onFocusedCount).isEqualTo(1)
        assertThat(onBlurCount).isEqualTo(0)
        printTree(TEST_TAG)

        // Loose focus
        composeTestRule.onNodeWithTag("AnotherTag").performClick()
        assertThat(onBlurCount).isEqualTo(1)
        field().assertIsNotFocused()
    }

    @Test
    fun `Type in valid Amex Card number, Returns correct states and value`() = runTest {
        val cardNumberField = CardNumberField()
        var cardNumberFieldState: FieldState? = null
        var onFocusedCount = 0
        var onBlurCount = 0

        composeTestRule.setContent {
            cardNumberField.ComposeUI(
                onValueChange = { cardNumberFieldState = it },
                onFocused = { onFocusedCount++ },
                onBlur = { onBlurCount++ },
                modifier = Modifier,
            )

            TextField(value = "Hello, World!", modifier = Modifier.testTag("AnotherTag"), onValueChange = { })
        }

        // Check initial state
        assertThat(cardNumberField).isNotNull
        assertThat(cardNumberFieldState).isEqualTo(FieldState())
        assertThat(onBlurCount).isEqualTo(0)
        assertThat(onFocusedCount).isEqualTo(0)
        field()
            .assertIsDisplayed()
            .assertTextEquals("Card Number", "")

        // Perform input of valid AMEX Card number
        field().apply { performTextInput("345554445554413") }
            .assertIsFocused()
            .assertTextEquals("Card Number", "3455 544455 54413")
            .assertIsNotTextFieldError()
        assertCardBrandIconIsDisplayed(R.drawable.ic_cards_amex.toString())

        // Check final state
        assertThat(cardNumberField.validatedValue()).isEqualTo(CardNumber.Validated("345554445554413"))
        assertThat(cardNumberFieldState).isEqualTo(FieldState(valid = true, empty = false, error = emptyList()))
        assertThat(onFocusedCount).isEqualTo(1)
        assertThat(onBlurCount).isEqualTo(0)
        printTree(TEST_TAG)

        // Loose focus
        composeTestRule.onNodeWithTag("AnotherTag").performClick()
        assertThat(onBlurCount).isEqualTo(1)
        field().assertIsNotFocused()
    }

    private fun field() = composeTestRule.onNodeWithTag(TEST_TAG)

    @Test
    fun `Type in invalid Card starting with 1, No further inputs are accepted and field shows error`() = runTest {
        val cardNumberField = CardNumberField()
        var cardNumberFieldState: FieldState? = null
        var onFocusedCount = 0
        var onBlurCount = 0

        composeTestRule.setContent {
            cardNumberField.ComposeUI(
                onValueChange = { cardNumberFieldState = it },
                onFocused = { onFocusedCount++ },
                onBlur = { onBlurCount++ },
                modifier = Modifier,
            )
        }

        // Check initial state
        assertThat(cardNumberField).isNotNull
        assertThat(cardNumberFieldState).isEqualTo(FieldState())
        assertThat(onBlurCount).isEqualTo(0)
        assertThat(onFocusedCount).isEqualTo(0)
        field().assertIsDisplayed()
            .assertTextEquals("Card Number", "")

        // Perform input of invalid Card number
        field().apply { performTextInput("1") }
            .assertIsFocused()
            .assertTextEquals("Card Number", "1")
            .assertIsTextFieldError()

        println(R.drawable.ic_cards_error.toString())
        assertCardBrandIconIsDisplayed(R.drawable.ic_cards_error.toString())

        // Perform input of additional invalid number
        field().apply { performTextInput("1") }
            .assertTextEquals("Card Number", "1")
            .assertIsFocused()

        printTree(TEST_TAG)

        // Check final state
        assertThat(cardNumberField.validatedValue()).isNull()
        assertThat(cardNumberFieldState).isEqualTo(FieldState(valid = false, empty = false, error = listOf(FieldError(FieldError.ErrorType.INVALID))))
        assertThat(onFocusedCount).isEqualTo(1)
        assertThat(onBlurCount).isEqualTo(0)
    }

    private fun assertCardBrandIconIsDisplayed(resId: String) {
        composeTestRule.onNodeWithTag(resId, true).assertIsDisplayed()
    }

    @Test
    fun `Set field label manual to specific value, Field is drawn with specified value`() = runTest {
        val testLabel = "Expected Label"
        val cardNumberField = CardNumberField(testLabel)

        composeTestRule.setContent {
            cardNumberField.ComposeUI({ })
        }

        field().apply { performTextInput("4242424242") }
            .assertTextEquals(testLabel, "4242 4242 42")
            .assertIsNotTextFieldError()

        assertThat(cardNumberField.validatedValue()).isNull()
    }
}
