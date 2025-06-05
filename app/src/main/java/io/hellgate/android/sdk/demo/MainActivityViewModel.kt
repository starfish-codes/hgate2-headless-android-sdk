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

    var cardNumberField = CardNumberField()
    val cardNumberState = MutableStateFlow(FieldState())
    var expiryDateField = ExpiryDateField()
    val expiryDateState = MutableStateFlow(FieldState())
    var cvcField = CvcNumberField(cardNumberField.maxBrandCvcLength)
    val cvcState = MutableStateFlow(FieldState())
    var cardholderNameField = DataField(AdditionalDataTypes.CARDHOLDER_NAME)
    val cardholderError = MutableStateFlow(false)
    val cardholderNameState = MutableStateFlow(AdditionalDataFieldState())

    var textValue by mutableStateOf("Submit")
    private lateinit var hellgate: Hellgate

    val sessionState = MutableStateFlow<SessionState?>(null)
    val loading = MutableStateFlow(false)

    fun loading() {
        loading.value = true
    }

    fun doneLoading() {
        loading.value = false
    }

    fun createNewSession() {
        loading()
        viewModelScope.launch {
            val sessionId: String = hgBackendClient().createSession().getOrNull()?.sessionId.orEmpty()
            sessionState.value = SessionState.UNKNOWN
            hellgate = initHellgate(sessionId, BuildConfig.HG_BACKEND_API_URL)
            doneLoading()
        }
    }

    fun fetchSessionStatus() {
        loading()
        viewModelScope.launch {
            sessionState.value = hellgate.fetchSessionStatus()
            doneLoading()
        }
    }

    fun submit() {
        loading()
        viewModelScope.launch {
            hellgate.cardHandler().fold(
                onSuccess = {
                    debugLog("$TAG Success, cardHandler created")
                    val response = it.tokenizeCard(
                        cardNumberField,
                        cvcField,
                        expiryDateField,
                        if (cardholderNameState.value.empty) emptyList() else listOf(cardholderNameField),
                    )

                    handleResponse(response)
                    textValue = response.toString()
                },
                onFailure = {
                    debugLog("$TAG Failure, cardHandler not created: " + it.message.toString())
                },
            )
            doneLoading()
        }
    }

    private fun handleResponse(response: TokenizeCardResponse) {
        when (response) {
            is TokenizeCardResponse.Success -> {
                debugLog("$TAG Tokenization successful, token ID: ${response.id}")
                textValue = "Token ID: ${response.id}"
                sessionState.value = SessionState.COMPLETED
            }

            is TokenizeCardResponse.Failure -> {
                debugLog("$TAG Tokenization failed: ${response.message}, Validation Errors: ${response.validationErrors}")
                textValue = "Error: ${response.message}"
                sessionState.value = SessionState.FAILURE
            }
        }
    }

    fun reset() {
        sessionState.value = null
        textValue = "Submit"
        cardNumberField = CardNumberField()
        expiryDateField = ExpiryDateField()
        cvcField = CvcNumberField(cardNumberField.maxBrandCvcLength)
        cardholderNameField = DataField(AdditionalDataTypes.CARDHOLDER_NAME)
    }

    companion object {
        private const val TAG = "MainActivityViewModel"
    }
}
