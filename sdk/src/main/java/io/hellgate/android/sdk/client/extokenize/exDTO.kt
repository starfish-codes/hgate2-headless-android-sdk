package io.hellgate.android.sdk.client.extokenize

import io.hellgate.android.sdk.model.CardData

@Suppress("unused")
internal data class ExTokenizeRequest(
    val data: Data,
) {
    val type: String = TYPE
    val mask: Mask = Mask()

    companion object {
        private const val TYPE = "card"
    }

    class Mask {
        val expirationMonth: String = "{{ data.expiration_month }}"
        val expirationYear: String = "{{ data.expiration_year }}"
        val number: String = "{{ data.number | card_mask: 'true', 'true' }}"
    }

    data class Data(
        val cvc: String,
        val expirationMonth: Int,
        val expirationYear: Int,
        val number: String,
    ) {
        companion object {
            internal fun fromCardData(cardData: CardData): Data =
                Data(
                    cvc = cardData.cvc,
                    expirationMonth = cardData.month.toInt(),
                    expirationYear = 2000 + cardData.year.toInt(),
                    number = cardData.cardNumber,
                )
        }
    }
}

internal data class ExTokenizeResponse(val id: String)
