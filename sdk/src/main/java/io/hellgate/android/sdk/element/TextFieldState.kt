package io.hellgate.android.sdk.element

internal interface TextFieldState {
    fun shouldShowError(hasFocus: Boolean): Boolean

    fun isValid(): Boolean

    fun getError(): FieldError?

    fun isFull(): Boolean

    fun isBlank(): Boolean

    fun canAcceptInput(
        currentValue: String,
        proposedValue: String,
    ) = !(isFull() && proposedValue.length > currentValue.length)

    fun toPublicFieldState() = FieldState(isValid(), isBlank(), getError()?.let { listOf(it) }.orEmpty())
}
