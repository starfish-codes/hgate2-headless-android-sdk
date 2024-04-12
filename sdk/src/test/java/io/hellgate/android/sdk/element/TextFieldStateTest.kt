package io.hellgate.android.sdk.element

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith

@RunWith(Enclosed::class)
class TextFieldStateTest {
    class CanAcceptInput {
        @Test
        fun `ProposedValue is greater than currentValue, Returns false`() {
            val textFieldState = TextFieldStateConstants.Valid.Full
            val currentValue = "424242"
            val proposedValue = "4242424"
            val canAcceptInput = textFieldState.canAcceptInput(currentValue, proposedValue)
            assertThat(canAcceptInput).isFalse()
        }

        @Test
        fun `ProposedValue is less than currentValue, Returns false`() {
            val textFieldState = TextFieldStateConstants.Valid.Full
            val currentValue = "424242"
            val proposedValue = "42424"
            val canAcceptInput = textFieldState.canAcceptInput(currentValue, proposedValue)
            assertThat(canAcceptInput).isTrue()
        }

        @Test
        fun `ProposedValue is less than currentValue, Returns true`() {
            val textFieldState = TextFieldStateConstants.Error.Incomplete()
            val currentValue = "424242"
            val proposedValue = "42424"
            val canAcceptInput = textFieldState.canAcceptInput(currentValue, proposedValue)
            assertThat(canAcceptInput).isTrue()
        }

        @Test
        fun `ProposedValue is equal to currentValue, Returns true`() {
            val textFieldState = TextFieldStateConstants.Error.Incomplete()
            val currentValue = "424242"
            val proposedValue = "424242"
            val canAcceptInput = textFieldState.canAcceptInput(currentValue, proposedValue)
            assertThat(canAcceptInput).isTrue()
        }

        @Test
        fun `ProposedValue is greater than currentValue, Returns true`() {
            val textFieldState = TextFieldStateConstants.Valid.Limitless
            val currentValue = "424242"
            val proposedValue = "4242424"
            val canAcceptInput = textFieldState.canAcceptInput(currentValue, proposedValue)
            assertThat(canAcceptInput).isTrue()
        }
    }

    class ToPublicFieldState {
        @Test
        fun `To public field state, Returns valid full`() {
            val textFieldState = TextFieldStateConstants.Valid.Full
            val result = textFieldState.toPublicFieldState()
            assertThat(result).isEqualTo(FieldState(valid = true, empty = false, error = emptyList()))
        }

        @Test
        fun `To public field state, Returns valid limitless`() {
            val textFieldState = TextFieldStateConstants.Valid.Limitless
            val result = textFieldState.toPublicFieldState()
            assertThat(result).isEqualTo(FieldState(valid = true, empty = false, error = emptyList()))
        }

        @Test
        fun `To public field state, Returns error blank`() {
            val textFieldState = TextFieldStateConstants.Error.Blank
            val result = textFieldState.toPublicFieldState()
            assertThat(result).isEqualTo(FieldState(valid = false, empty = true, error = listOf(FieldError(FieldError.ErrorType.BLANK))))
        }

        @Test
        fun `To public field state, Returns error incomplete`() {
            val textFieldState = TextFieldStateConstants.Error.Incomplete()

            val result = textFieldState.toPublicFieldState()
            assertThat(result).isEqualTo(FieldState(valid = false, empty = false, error = listOf(FieldError(FieldError.ErrorType.INCOMPLETE))))
        }

        @Test
        fun `To public field state, Returns error invalid`() {
            val textFieldState = TextFieldStateConstants.Error.Invalid()
            val result = textFieldState.toPublicFieldState()
            assertThat(result).isEqualTo(FieldState(valid = false, empty = false, error = listOf(FieldError(FieldError.ErrorType.INVALID))))
        }
    }

    class ShouldShowError {
        @Test
        fun `State Full, shouldShowError`() {
            val textFieldState = TextFieldStateConstants.Valid.Full
            assertThat(textFieldState.shouldShowError(true)).isFalse
            assertThat(textFieldState.shouldShowError(false)).isFalse
        }

        @Test
        fun `State Incomplete, shouldShowError`() {
            val textFieldState = TextFieldStateConstants.Error.Incomplete()
            assertThat(textFieldState.shouldShowError(true)).isFalse
            assertThat(textFieldState.shouldShowError(false)).isTrue
        }

        @Test
        fun `State Invalid, shouldShowError`() {
            val textFieldState = TextFieldStateConstants.Error.Invalid()
            assertThat(textFieldState.shouldShowError(true)).isTrue
            assertThat(textFieldState.shouldShowError(false)).isTrue
        }

        @Test
        fun `Blank, always returns false`() {
            val textFieldState = TextFieldStateConstants.Error.Blank
            assertThat(textFieldState.shouldShowError(true)).isFalse
            assertThat(textFieldState.shouldShowError(false)).isFalse
        }
    }
}
