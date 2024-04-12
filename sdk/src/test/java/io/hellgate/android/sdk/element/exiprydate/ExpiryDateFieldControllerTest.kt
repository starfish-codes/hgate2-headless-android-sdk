package io.hellgate.android.sdk.element.exiprydate

import io.hellgate.android.sdk.element.TextFieldStateConstants
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith

@RunWith(Enclosed::class)
class ExpiryDateFieldControllerTest {
    class FieldValue {
        @Test
        fun `Input values into textField, Returns correct value`() = runTest {
            val controller = ExpiryDateFieldController()

            controller.onValueChanged("12 / 30")
            val value = controller.fieldValue.first()
            assertThat(value).isEqualTo("1230")
        }

        @Test
        fun `Invalid onValueChanged input, returns the same digits`() = runTest {
            val controller = ExpiryDateFieldController()

            controller.onValueChanged("9999")
            val value = controller.fieldValue.first()
            assertThat(value).isEqualTo("9999")
        }
    }

    class FieldStateTest {
        @Test
        fun `Input values into textField, Returns correct state`() = runTest {
            val controller = ExpiryDateFieldController()

            controller.onValueChanged("12 / 35")
            val state = controller.fieldState.first()
            assertThat(state).isEqualTo(TextFieldStateConstants.Valid.Full)
        }

        @Test
        fun `Invalid onValueChanged input, returns incomplete state`() = runTest {
            val controller = ExpiryDateFieldController()

            controller.onValueChanged("12 / 3")
            val state = controller.fieldState.first()
            assertThat(state).isEqualTo(TextFieldStateConstants.Error.Incomplete())
        }

        @Test
        fun `Invalid onValueChanged input, returns invalid state`() = runTest {
            val controller = ExpiryDateFieldController()

            controller.onValueChanged("01 / 24")
            val state = controller.fieldState.first()
            assertThat(state).isEqualTo(TextFieldStateConstants.Error.Invalid(preventMoreInput = true))
        }
    }

    class HasFocus {
        @Test
        fun `Call onFocusChange with different values, Returns correct value`() = runTest {
            val controller = ExpiryDateFieldController()

            controller.onFocusChange(true)
            val hasFocus = controller.hasFocus.first()
            assertThat(hasFocus).isTrue()

            controller.onFocusChange(false)
            val hasFocus2 = controller.hasFocus.first()
            assertThat(hasFocus2).isFalse()
        }
    }

    class VisibleError {
        @Test
        fun `Input values into textField with field in focus, Returns false`() = runTest {
            val controller = ExpiryDateFieldController()
            controller.onFocusChange(true)
            controller.onValueChanged("12 / 30")

            val value = controller.visibleError.first()
            assertThat(value).isFalse()
        }

        @Test
        fun `Input values partially into textField and lose focus , Returns true`() = runTest {
            val controller = ExpiryDateFieldController()
            controller.onFocusChange(true)
            controller.onValueChanged("12 / 3")
            controller.onFocusChange(false)

            val value = controller.visibleError.first()
            assertThat(value).isTrue()
        }

        @Test
        fun `Input values into textField, Returns true`() = runTest {
            val controller = ExpiryDateFieldController()

            controller.onValueChanged("12 / 3")
            val value = controller.visibleError.first()
            assertThat(value).isTrue()
        }
    }
}
