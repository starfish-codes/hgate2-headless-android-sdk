package io.hellgate.android.sdk.demo

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.hellgate.android.sdk.*
import io.hellgate.android.sdk.demo.client.hgBackendClient
import io.hellgate.android.sdk.element.AdditionalDataFieldState
import io.hellgate.android.sdk.element.FieldState
import io.hellgate.android.sdk.element.additionaldata.AdditionalDataTypes
import io.hellgate.android.sdk.element.additionaldata.DataField
import io.hellgate.android.sdk.element.card.CardNumberField
import io.hellgate.android.sdk.element.cvc.CvcNumberField
import io.hellgate.android.sdk.element.exiprydate.ExpiryDateField
import io.hellgate.android.sdk.model.TokenizeCardResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainActivityViewModel : ViewModel() {

    val cardNumberField = CardNumberField()
    var cardNumberState by mutableStateOf(FieldState())
    val expiryDateField = ExpiryDateField()
    var expiryDateState by mutableStateOf(FieldState())
    val cvcField = CvcNumberField(cardNumberField.maxBrandCvcLength)
    var cvcState by mutableStateOf(FieldState())
    val cardholderNameField = DataField(AdditionalDataTypes.CARDHOLDER_NAME)
    var cardholderError by mutableStateOf(false)
    var cardholderNameState by mutableStateOf(AdditionalDataFieldState())

    var textValue by mutableStateOf("Submit")
    private lateinit var hellgate: Hellgate

    val sessionState = MutableStateFlow<SessionState?>(null)

    // TODO Implement loading state management
    // val loading = true to null
    // fun doneLoading(state: SessionState) {
    //     sessionState.value = false to state
    // }

    fun createNewSession() {
        viewModelScope.launch {
            val sessionId: String = hgBackendClient().createSession().getOrNull()?.sessionId.orEmpty()
            sessionState.value = SessionState.UNKNOWN
            hellgate = initHellgate(sessionId, BuildConfig.HG_BACKEND_API_URL)
        }
    }

    fun fetchSessionStatus() {
        viewModelScope.launch {
            sessionState.value = hellgate.fetchSessionStatus()
        }
    }

    fun submit() {
        viewModelScope.launch {
            hellgate.cardHandler().fold(
                onSuccess = {
                    debugLog("$TAG Success, cardHandler created")
                    val response = it.tokenizeCard(
                        cardNumberField,
                        cvcField,
                        expiryDateField,
                        if (cardholderNameState.empty) emptyList() else listOf(cardholderNameField),
                    )

                    printResponse(response)
                    textValue = response.toString()
                    fetchSessionStatus()
                },
                onFailure = { debugLog("$TAG Failure, cardHandler not created: " + it.message.toString()) },
            )
        }
    }

    fun printResponse(response: TokenizeCardResponse) {
        when (response) {
            is TokenizeCardResponse.Success -> {
                debugLog("$TAG Tokenization successful, token ID: ${response.id}")
                textValue = "Token ID: ${response.id}"
            }

            is TokenizeCardResponse.Failure -> {
                debugLog("$TAG Tokenization failed: ${response.message}, Validation Errors: ${response.validationErrors}")
                textValue = "Error: ${response.message}"
            }
        }
    }

    fun reset() {
        sessionState.value = null
        textValue = "Submit"
    }

    companion object {
        private const val TAG = "MainActivityViewModel"
    }
}
