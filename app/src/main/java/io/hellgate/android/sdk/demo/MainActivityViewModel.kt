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

    var sessionState by mutableStateOf<SessionState?>(null)

    fun createNewSession() {
        viewModelScope.launch {
            val sessionId: String = hgBackendClient().createSession().getOrNull()?.sessionId.orEmpty()
            sessionState = SessionState.UNKNOWN
            hellgate = initHellgate(HELLGATE_BASE_URL, sessionId)
        }
    }

    fun fetchSessionStatus() {
        viewModelScope.launch {
            val sessionStatus = hellgate.fetchSessionStatus()
            sessionState = sessionStatus
        }
    }

    fun submit() {
        viewModelScope.launch {
            val cardHandler = hellgate.cardHandler().fold(
                onSuccess = {
                    val hgToken = it.tokenizeCard(
                        cardNumberField,
                        cvcField,
                        expiryDateField,
                        if (cardholderNameState.empty) emptyList() else listOf(cardholderNameField),
                    )

                    debugLog("$TAG cardHandler response: $hgToken")
                    textValue = hgToken.toString()
                    fetchSessionStatus()
                },
                onFailure = { debugLog(it.message.toString()) },
            )
            debugLog("$TAG cardHandler response: $cardHandler")
        }
    }

    companion object {
        private const val TAG = "MainActivityViewModel"
    }
}