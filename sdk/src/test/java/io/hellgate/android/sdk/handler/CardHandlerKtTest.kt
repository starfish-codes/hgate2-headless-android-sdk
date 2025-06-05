package io.hellgate.android.sdk.handler

import io.hellgate.android.sdk.element.additionaldata.AdditionalDataTypes
import io.hellgate.android.sdk.element.additionaldata.DataField
import io.hellgate.android.sdk.element.card.CardNumberField
import io.hellgate.android.sdk.element.cvc.CvcNumberField
import io.hellgate.android.sdk.element.exiprydate.ExpiryDateField
import io.hellgate.android.sdk.model.*
import io.hellgate.android.sdk.model.CardNumber
import io.hellgate.android.sdk.service.ITokenService
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CardHandlerKtTest {
    @Test
    fun `TokenizeCard, Receive valid input, Returns expected result`() =
        runTest {
            val sessionID = "sessionID"
            val cardMockk = mockk<CardNumberField> {
                coEvery { validatedValue() } returns CardNumber.Validated("4242424242424242")
            }
            val cvcMockk = mockk<CvcNumberField> {
                coEvery { value() } returns "767"
            }
            val expiryDateField = mockk<ExpiryDateField> {
                coEvery { value() } returns "1223"
            }
            val cardholderNameField = mockk<DataField> {
                coEvery { value() } returns "John Doe"
                every { additionalDataTypes } returns AdditionalDataTypes.CARDHOLDER_NAME
            }
            val tokenServiceMockk = mockk<ITokenService> {
                coEvery {
                    tokenize(
                        sessionID,
                        CardData("4242424242424242", "23", "12", "767"),
                        mapOf(AdditionalDataTypes.CARDHOLDER_NAME to "John Doe"),
                    )
                } returns TokenizeCardResponse.Success("tokenID")
            }

            val result = cardHandler(tokenServiceMockk, sessionID).tokenizeCard(cardMockk, cvcMockk, expiryDateField, listOf(cardholderNameField))

            assertThat(result).isEqualTo(TokenizeCardResponse.Success("tokenID"))
        }

    @Test
    fun `TokenizeCard, Receive invalid card number, Returns Failure with error and message about card`() =
        runTest {
            val sessionID = "sessionID"
            val cardMockk = mockk<CardNumberField> {
                coEvery { validatedValue() } returns null
            }
            val expiryDateField = mockk<ExpiryDateField> {
                coEvery { value() } returns "1245"
            }
            val cvcMockk = mockk<CvcNumberField> {
                coEvery { value() } returns "767"
            }

            val result = cardHandler(mockk<ITokenService>(), sessionID).tokenizeCard(cardMockk, cvcMockk, expiryDateField)

            assertThat(result).isEqualTo(TokenizeCardResponse.Failure("Card data validation failed", validationErrors = listOf(InvalidCardNumber)))
        }

    @Test
    fun `TokenizeCard, Receive invalid fields, Returns Failure with errors and concatinated message`() =
        runTest {
            val sessionID = "sessionID"
            val cardMockk = mockk<CardNumberField> {
                coEvery { validatedValue() } returns null
            }
            val expiryDateField = mockk<ExpiryDateField> {
                coEvery { value() } returns "14"
            }
            val cvcMockk = mockk<CvcNumberField> {
                coEvery { value() } returns "12"
            }

            val result = cardHandler(mockk<ITokenService>(), sessionID).tokenizeCard(cardMockk, cvcMockk, expiryDateField)

            assertThat(result).isEqualTo(
                TokenizeCardResponse.Failure(
                    "Card data validation failed",
                    validationErrors = listOf(InvalidCardNumber, InvalidExpiryDate, InvalidCvc),
                ),
            )
        }
}
