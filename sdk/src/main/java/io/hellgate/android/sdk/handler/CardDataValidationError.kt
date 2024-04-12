package io.hellgate.android.sdk.handler

sealed interface CardDataValidationError {
    val message: String
}

data object InvalidCardNumber : CardDataValidationError {
    override val message: String = "Invalid card number"
}

data object InvalidExpiryDate : CardDataValidationError {
    override val message: String = "Invalid expiry date"
}

data object InvalidCvc : CardDataValidationError {
    override val message: String = "Invalid cvc"
}
