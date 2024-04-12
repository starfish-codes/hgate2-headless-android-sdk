package io.hellgate.android.sdk.model

/**
 * Card data
 * @param cardNumber The card number
 * @param year The year of the card 2 digits
 * @param month The month of the card 2 digits
 * @param cvc The cvc of the card
 */
internal data class CardData(
    val cardNumber: String,
    val year: String,
    val month: String,
    val cvc: String,
)
