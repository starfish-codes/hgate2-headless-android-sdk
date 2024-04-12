package io.hellgate.android.sdk.client.guardian

import io.hellgate.android.sdk.model.CardData

internal data class GuardianTokenizeRequest(
    val expiryMonth: Int,
    val expiryYear: Int,
    val accountNumber: String,
    val securityCode: String,
) {
    companion object {
        fun fromCardData(cardData: CardData): GuardianTokenizeRequest =
            GuardianTokenizeRequest(
                expiryMonth = cardData.month.toInt(),
                expiryYear = cardData.year.toInt() + 2000,
                accountNumber = cardData.cardNumber,
                securityCode = cardData.cvc,
            )
    }
}

internal data class GuardianTokenizeResponse(val id: String)
