package io.hellgate.android.sdk.element.additionaldata

import io.hellgate.android.sdk.element.AdditionalDataFieldState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith

@RunWith(Enclosed::class)
class DataFieldControllerTest {
    class FieldValue {
        @Test
        fun `Input values into textField, Returns correct value`() = runTest {
            val controller = DataFieldController()

            controller.onValueChanged("John Doe")
            val value = controller.fieldValue.first()
            assertThat(value).isEqualTo("John Doe")
        }
    }

    class FieldStateTest {
        @Test
        fun `Input values into textField, Returns correct state`() = runTest {
            val controller = DataFieldController()

            controller.onValueChanged("John Doe")
            val state = controller.fieldState.first()
            assertThat(state).isEqualTo(AdditionalDataFieldState(empty = false, value = "John Doe"))
        }

        @Test
        fun `Empty input, Returns empty state`() = runTest {
            val controller = DataFieldController()

            controller.onValueChanged("")
            val state = controller.fieldState.first()
            assertThat(state).isEqualTo(AdditionalDataFieldState(empty = true, value = ""))

            controller.onValueChanged("J")
            val state1 = controller.fieldState.first()
            assertThat(state1).isEqualTo(AdditionalDataFieldState(empty = false, value = "J"))

            controller.onValueChanged("")
            val state2 = controller.fieldState.first()
            assertThat(state2).isEqualTo(AdditionalDataFieldState(empty = true, value = ""))
        }
    }

    @Test
    fun determineState() {
        val controller = DataFieldController()

        val state = controller.determineState("")
        assertThat(state).isEqualTo(AdditionalDataFieldState(empty = true, value = ""))

        val state1 = controller.determineState("J")
        assertThat(state1).isEqualTo(AdditionalDataFieldState(empty = false, value = "J"))

        val state2 = controller.determineState("John Doe")
        assertThat(state2).isEqualTo(AdditionalDataFieldState(empty = false, value = "John Doe"))
    }
}
