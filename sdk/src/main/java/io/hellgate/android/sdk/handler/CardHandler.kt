package io.hellgate.android.sdk.handler

import arrow.core.*
import arrow.core.raise.*
import io.hellgate.android.sdk.element.additionaldata.DataField
import io.hellgate.android.sdk.element.card.CardNumberField
import io.hellgate.android.sdk.element.cvc.CvcNumberField
import io.hellgate.android.sdk.element.exiprydate.ExpiryDateField
import io.hellgate.android.sdk.model.CardData
import io.hellgate.android.sdk.model.TokenizeCardResponse
import io.hellgate.android.sdk.service.ITokenService

interface CardHandler {
    suspend fun tokenizeCard(
        cardNumberField: CardNumberField,
        cvcNumberField: CvcNumberField,
        expiryDateField: ExpiryDateField,
        additionalData: List<DataField> = emptyList(),
    ): TokenizeCardResponse
}

internal fun cardHandler(
    tokenService: ITokenService,
    sessionId: String,
) = object : CardHandler {
    override suspend fun tokenizeCard(
        cardNumberField: CardNumberField,
        cvcNumberField: CvcNumberField,
        expiryDateField: ExpiryDateField,
        additionalData: List<DataField>,
    ): TokenizeCardResponse {
        val cardData = validateInput(cardNumberField, cvcNumberField, expiryDateField).getOrElse { listOfErrors ->
            return TokenizeCardResponse.Failure(listOfErrors.joinToString(", ") { it.message }, validationErrors = listOfErrors)
        }

        return tokenService.tokenize(
            sessionId,
            cardData,
            additionalData.associate { it.additionalDataTypes to it.value() }
                .filter { it.value.isNotBlank() },
        )
    }

    @Suppress("MagicNumber")
    private suspend fun validateInput(
        cardNumberField: CardNumberField,
        cvcNumberField: CvcNumberField,
        expiryDateField: ExpiryDateField,
    ): Either<NonEmptyList<CardDataValidationError>, CardData> =
        either {
            val date = expiryDateField.value()
            val month = date.take(2)
            val year = date.takeLast(2)

            zipOrAccumulate(
                { ensureNotNull(cardNumberField.validatedValue()?.value) { InvalidCardNumber } },
                { ensure(month.toIntOrNull() != null && year.toIntOrNull() != null && date.length == 4) { InvalidExpiryDate } },
                {
                    val value = cvcNumberField.value()
                    ensure(value.length == CvcNumberField.CVC_LENGTH || value.length == CvcNumberField.CVV_LENGTH) { InvalidCvc }
                    ensure(value.isNotEmpty()) { InvalidCvc }
                    value
                },
            ) { cardNumber, _, cvcValue ->
                CardData(cardNumber, year, month, cvcValue)
            }
        }
}
