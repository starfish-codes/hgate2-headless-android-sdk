package io.hellgate.android.sdk.element

internal class TextFieldStateConstants {
    sealed class Valid : TextFieldState {
        override fun shouldShowError(hasFocus: Boolean): Boolean = false

        override fun isValid(): Boolean = true

        override fun getError(): FieldError? = null

        override fun isBlank(): Boolean = false

        data object Full : Valid() {
            override fun isFull(): Boolean = true
        }

        data object Limitless : Valid() { // no auto-advance
            override fun isFull(): Boolean = false
        }
    }

    sealed class Error(
        protected open val errorType: FieldError.ErrorType,
    ) : TextFieldState {
        override fun isValid(): Boolean = false

        override fun isFull(): Boolean = false

        override fun getError() = FieldError(errorType)

        data class Incomplete(
            override val errorType: FieldError.ErrorType = FieldError.ErrorType.INCOMPLETE,
        ) : Error(errorType) {
            override fun shouldShowError(hasFocus: Boolean): Boolean = !hasFocus

            override fun isBlank(): Boolean = false
        }

        data class Invalid(
            override val errorType: FieldError.ErrorType = FieldError.ErrorType.INVALID,
            private val preventMoreInput: Boolean = false,
        ) : Error(errorType) {
            override fun shouldShowError(hasFocus: Boolean): Boolean = true

            override fun isBlank(): Boolean = false

            override fun isFull(): Boolean = preventMoreInput
        }

        data object Blank : Error(FieldError.ErrorType.BLANK) {
            override fun shouldShowError(hasFocus: Boolean): Boolean = false

            override fun isBlank(): Boolean = true
        }
    }
}
