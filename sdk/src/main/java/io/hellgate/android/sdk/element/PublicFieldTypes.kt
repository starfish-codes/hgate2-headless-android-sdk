package io.hellgate.android.sdk.element

data class FieldState(
    val valid: Boolean = false,
    val empty: Boolean = true,
    val error: List<FieldError> = listOf(FieldError(FieldError.ErrorType.BLANK)),
)

data class FieldError(
    val errorType: ErrorType,
) {
    enum class ErrorType {
        INVALID,
        INCOMPLETE,
        BLANK,
    }
}

data class AdditionalDataFieldState(
    val empty: Boolean = true,
    val value: String = "",
)
