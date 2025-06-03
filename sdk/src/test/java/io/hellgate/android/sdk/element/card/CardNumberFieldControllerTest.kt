package io.hellgate.android.sdk.element.card

import io.hellgate.android.sdk.R
import io.hellgate.android.sdk.element.*
import io.hellgate.android.sdk.element.TextFieldState
import io.hellgate.android.sdk.element.TextFieldStateConstants
import io.hellgate.android.sdk.model.CardBrand
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith

@RunWith(Enclosed::class)
class CardNumberFieldControllerTest {
    class FieldValue {
        @Test
        fun `Input values into textField, Returns correct value`() =
            runTest {
                val controller = CardNumberFieldController()

                controller.onValueChanged("4242 42")

                val value = controller.fieldValue.first()

                assertThat(value).isEqualTo("424242")
            }
    }

    class ImpliedCardBrand {
        @Test
        fun `Input values into textField, impliedBrand Returns null`() =
            runTest {
                val controller = CardNumberFieldController()

                controller.onValueChanged("0")

                val value = controller.impliedCardBrand.first()
                assertThat(value).isEqualTo(CardBrand.Unknown)
            }

        @Test
        fun `Input values into textField, impliedBrand Returns visa`() =
            runTest {
                val controller = CardNumberFieldController()

                controller.onValueChanged("4242 42")

                val value = controller.impliedCardBrand.first()
                assertThat(value).isEqualTo(CardBrand.Visa)
            }
    }

    class OnFocusChange {
        @Test
        fun `Call onFocusChange with different values, Returns correct value`() =
            runTest {
                val controller = CardNumberFieldController()

                controller.onFocusChange(true)
                controller.hasFocus.first().let {
                    assertThat(it).isTrue()
                }

                controller.onFocusChange(false)
                controller.hasFocus.first().let {
                    assertThat(it).isFalse()
                }
            }
    }

    class VisibleError {
        @Test
        fun `Input values into textField with field in focus, Returns false`() =
            runTest {
                val controller = CardNumberFieldController()
                controller.onFocusChange(true)
                controller.onValueChanged("4242 42")

                val value = controller.visibleError.first()
                assertThat(value).isFalse()
            }

        @Test
        fun `Input values into textField, Returns true`() =
            runTest {
                val controller = CardNumberFieldController()

                controller.onValueChanged("12")

                val value = controller.visibleError.first()
                assertThat(value).isTrue()
            }
    }

    class TrailingIcon {
        @Test
        fun `Get trailing icon for Visa card, Returns visa icon res id`() =
            runTest {
                val controller = CardNumberFieldController()

                controller.onValueChanged("4242 4242 4242 4242")

                val value = controller.fieldValue.first()
                assertThat(value).isEqualTo("4242424242424242")
                controller.trailingIcon.first().let {
                    assertThat(it).isEqualTo(R.drawable.ic_cards_visa)
                }
            }

        @Test
        fun `Get trailing icon for error input, Returns error icon res id`() =
            runTest {
                val controller = CardNumberFieldController()

                controller.onValueChanged("12")

                val value = controller.fieldValue.first()
                assertThat(value).isEqualTo("12")
                controller.trailingIcon.first().let {
                    assertThat(it).isEqualTo(R.drawable.ic_cards_error)
                }
            }
    }

    class GetCardNumber {
        @Test
        fun `Get card number for Visa card, Returns card number`() =
            runTest {
                val controller = CardNumberFieldController()

                controller.onValueChanged("4242 4242 4242 4242")

                val validated = controller.getCardNumber()
                assertThat(validated).isNotNull()
                assertThat(validated?.value).isEqualTo("4242424242424242")
            }

        @Test
        fun `Get card number for invalid card number, Returns null`() =
            runTest {
                val controller = CardNumberFieldController()

                controller.onValueChanged("4242 4242 4242 4243")

                val value = controller.getCardNumber()
                assertThat(value).isNull()
            }
    }

    class FieldStateTest {
        @Test
        fun `Get field state for Visa card, Returns valid state`() =
            runTest {
                val controller = CardNumberFieldController()

                controller.onValueChanged("4242 4242 4242 4242")

                val value: TextFieldState = controller.fieldState.first()
                assertThat(value).isInstanceOf(TextFieldStateConstants.Valid.Full::class.java)
                assertThat(value.isFull()).isTrue()
                assertThat(value.getError()).isNull()
            }

        @Test
        fun `Get field state for invalid card number, Returns error state`() =
            runTest {
                val controller = CardNumberFieldController()

                controller.onValueChanged("4242 4242 4242 4243")

                val value: TextFieldState = controller.fieldState.first()
                assertThat(value).isInstanceOf(TextFieldStateConstants.Error.Invalid::class.java)
                assertThat(value.isFull()).isTrue()
                assertThat(value.getError()).isNotNull().isEqualTo(
                    TextFieldStateConstants.Error.Invalid(
                        errorType = FieldError.ErrorType.INVALID,
                        preventMoreInput = true,
                    ).getError(),
                )
            }

        @Test
        fun `Get field state for blank card number, Returns blank state`() =
            runTest {
                val controller = CardNumberFieldController()

                controller.onValueChanged("")

                val value: TextFieldState = controller.fieldState.first()
                assertThat(value).isInstanceOf(TextFieldStateConstants.Error.Blank::class.java)
                assertThat(value.isBlank()).isTrue()
                assertThat(value.getError()).isNotNull().isEqualTo(
                    TextFieldStateConstants.Error.Blank.getError(),
                )
            }
    }
}
