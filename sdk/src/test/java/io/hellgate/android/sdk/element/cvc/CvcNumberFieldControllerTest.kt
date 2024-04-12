package io.hellgate.android.sdk.element.cvc

import io.hellgate.android.sdk.element.TextFieldStateConstants
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.*
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith

@RunWith(Enclosed::class)
class CvcNumberFieldControllerTest {
    class FieldValue {
        @Test
        fun `Input values into textField, Returns correct value`() = runTest {
            val controller = CvcNumberFieldController(flowOf(3))

            controller.onValueChanged("123")
            val value = controller.fieldValue.first()
            assertThat(value).isEqualTo("123")
        }

        @Test
        fun `Invalid onValueChanged input, returns the same digits without invalid chars`() = runTest {
            val controller = CvcNumberFieldController(flowOf(4))

            controller.onValueChanged("12 3 45")
            val value = controller.fieldValue.first()
            assertThat(value).isEqualTo("12345")
        }
    }

    class FieldStateTest {
        @Test
        fun `Input values into textField, Returns correct state`() = runTest {
            val controller = CvcNumberFieldController(flowOf(3))

            controller.onValueChanged("123")
            val state = controller.fieldState.first()
            assertThat(state).isEqualTo(TextFieldStateConstants.Valid.Full)
        }

        @Test
        fun `Invalid onValueChanged input, returns incomplete state`() = runTest {
            val controller = CvcNumberFieldController(flowOf(3))

            controller.onValueChanged("12")
            val state = controller.fieldState.first()
            assertThat(state).isEqualTo(TextFieldStateConstants.Error.Incomplete())
        }

        @Test
        fun `Invalid onValueChanged input, returns invalid state`() = runTest {
            val controller = CvcNumberFieldController(flowOf(3))

            controller.onValueChanged("1234")
            val state = controller.fieldState.first()
            assertThat(state).isEqualTo(TextFieldStateConstants.Error.Invalid(preventMoreInput = true))
        }
    }

    class HasFocus {
        @Test
        fun `Focus change, Returns correct state`() = runTest {
            val controller = CvcNumberFieldController(flowOf(3))

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
            val controller = CvcNumberFieldController(flowOf(3))

            controller.onFocusChange(true)
            controller.onValueChanged("123")
            val result = controller.visibleError.first()

            assertThat(result).isFalse()
        }

        @Test
        fun `Input values partially into textField and lose focus , Returns true`() = runTest {
            val controller = CvcNumberFieldController(flowOf(3))

            controller.onFocusChange(true)
            controller.onValueChanged("12")
            controller.onFocusChange(false)
            val result = controller.visibleError.first()

            assertThat(result).isTrue()
        }

        @Test
        fun `Input values into textField, Returns true`() = runTest {
            val controller = CvcNumberFieldController(flowOf(3))

            controller.onValueChanged("123")
            val result = controller.visibleError.first()

            assertThat(result).isFalse()
        }
    }
}
