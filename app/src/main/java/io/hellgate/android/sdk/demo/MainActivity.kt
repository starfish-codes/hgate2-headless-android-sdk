package io.hellgate.android.sdk.demo

import android.os.Bundle
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.hellgate.android.sdk.SessionState
import io.hellgate.android.sdk.demo.ui.theme.HellgateAndroidSDKTheme
import io.hellgate.android.sdk.element.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewmodel by viewModels<MainActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        setContent {
            HellgateAndroidSDKTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(WindowInsets.safeDrawing.asPaddingValues()),
                    color = Color(0xffe9f2f4),
                ) {

                    val sessionState by viewmodel.sessionState.collectAsStateWithLifecycle(null)
                    val loading by viewmodel.loading.collectAsStateWithLifecycle(true)

                    Column(
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                    ) {
                        if (loading) {
                            Box(modifier = Modifier.padding(top = 20.dp)){
                                CircularProgressIndicator()
                            }
                        } else {
                            InputFields(sessionState)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun InputFields(sessionState: SessionState?) {
        when (sessionState) {
            null -> {
                Text("Session status: null")
                Button(onClick = viewmodel::createNewSession) {
                    Text("Create new session")
                }
            }

            SessionState.UNKNOWN -> {
                Text("Session status: $sessionState")
                FetchButton()
            }

            SessionState.REQUIRE_TOKENIZATION -> {
                Text("Session status: $sessionState")
                CardForm()
                StatePrintout()
                SubmitButton()
            }

            SessionState.WAITING -> {
                Text("Session status: $sessionState")
                CircularProgressIndicator()
                LaunchedEffect(Unit) {
                    while (true) {
                        debugLog("Session status: $sessionState")
                        viewmodel.fetchSessionStatus()
                        delay(500)
                    }
                }
            }

            SessionState.COMPLETED, SessionState.FAILURE -> {
                Text("Session status: $sessionState")
                Text(viewmodel.textValue)
                ResetButton()
            }
        }
    }

    @Composable
    private fun FetchButton() {
        Button(
            onClick = {
                debugLog("$TAG Fetch button clicked")
                viewmodel.fetchSessionStatus()
            },
        ) {
            Text("Fetch session status")
        }
    }


    @Composable
    private fun CardForm() {
        Column(modifier = Modifier.padding(4.dp)) {
            viewmodel.cardNumberField.ComposeUI(
                onValueChange = {
                    debugLog("$TAG cardnumberState changed to: $it")
                    viewmodel.cardNumberState.value = it
                },
                modifier = Modifier.fillMaxWidth(),
                onFocused = { debugLog("$TAG cardnumber focused") },
                onBlur = { debugLog("$TAG cardnumber blurred") },
                shape = RoundedCornerShape(2.dp),
                colors = TextFieldDefaults.colors(
                    errorTextColor = MaterialTheme.colorScheme.error,
                    focusedTextColor = Color.Black,
                    focusedContainerColor = Color.White,
                    unfocusedTextColor = Color.Black,
                    unfocusedContainerColor = Color.White,
                ),
            )
            Row {
                viewmodel.expiryDateField.ComposeUI(
                    modifier = Modifier,
                    onValueChange = {
                        debugLog("$TAG expiryDateState changed to: $it")
                        viewmodel.expiryDateState.value = it
                    },
                    onFocused = { debugLog("$TAG expiryDate focused") },
                    onBlur = { debugLog("$TAG expiryDate blurred") },
                    shape = RoundedCornerShape(0.dp),
                    colors = TextFieldDefaults.colors(
                        errorTextColor = MaterialTheme.colorScheme.error,
                        focusedTextColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedTextColor = Color.Black,
                        unfocusedContainerColor = Color.White,
                    ),
                )
                viewmodel.cvcField.ComposeUI(
                    modifier = Modifier.padding(start = 10.dp),
                    onValueChange = {
                        debugLog("$TAG cvcState changed to: $it")
                        viewmodel.cvcState.value = it
                    },
                    onFocused = { debugLog("$TAG cvc focused") },
                    onBlur = { debugLog("$TAG cvc blurred") },
                    shape = RoundedCornerShape(2.dp),
                    colors = TextFieldDefaults.colors(
                        errorTextColor = MaterialTheme.colorScheme.error,
                        focusedTextColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedTextColor = Color.Black,
                        unfocusedContainerColor = Color.White,
                    ),
                )
            }

            val cardHolderError by viewmodel.cardholderError.collectAsStateWithLifecycle(LocalLifecycleOwner.current)

            viewmodel.cardholderNameField.ComposeUI(
                modifier = Modifier.fillMaxWidth(),
                onValueChange = {
                    debugLog("$TAG cardholderName changed to: $it")
                    viewmodel.cardholderError.value = it.value.contains("ö")
                    viewmodel.cardholderNameState.value = it
                },
                onFocused = { debugLog("$TAG cardholderName focused") },
                onBlur = { debugLog("$TAG cardholderName blurred") },
                isErrorVisible = cardHolderError,
                shape = RoundedCornerShape(2.dp),
                colors = TextFieldDefaults.colors(
                    errorTextColor = MaterialTheme.colorScheme.error,
                    focusedTextColor = Color.Black,
                    focusedContainerColor = Color.White,
                    unfocusedTextColor = Color.Black,
                    unfocusedContainerColor = Color.White,
                ),
            )
        }
    }

    @Composable
    private fun StatePrintout() {
        val cardNumberState by viewmodel.cardNumberState.collectAsStateWithLifecycle(LocalLifecycleOwner.current)
        val expiryDateState by viewmodel.expiryDateState.collectAsStateWithLifecycle(LocalLifecycleOwner.current)
        val cvcState by viewmodel.cvcState.collectAsStateWithLifecycle(LocalLifecycleOwner.current)

        Text(text = "CardNumberState:\n${getErrorText(cardNumberState)}")
        Text(text = "ExpiryDateState:\n${getErrorText(expiryDateState)}")
        Text(text = "CVCState: \n${getErrorText(cvcState)}")
    }

    @Composable
    private fun SubmitButton() {
        Button(
            onClick = {
                debugLog("$TAG Submit button clicked")
                viewmodel.submit()
            },
        ) {
            Text(viewmodel.textValue)
        }
    }

    @Composable
    private fun ResetButton() {
        Button(
            onClick = {
                debugLog("$TAG Reset button clicked")
                viewmodel.reset()
            },
        ) {
            Text("Reset")
        }
    }


    @Composable
    private fun getErrorText(
        fieldState: FieldState,
    ) = if (fieldState.error.isEmpty()) "" else stringResource(id = fieldState.error.first().errorType.toStringRes())

    companion object {
        private const val TAG = "MainActivity"
    }
}

fun FieldError.ErrorType.toStringRes() = when (this) {
    FieldError.ErrorType.INVALID -> R.string.invalid_field
    FieldError.ErrorType.BLANK -> R.string.blank_and_required
    FieldError.ErrorType.INCOMPLETE -> R.string.incomplete_expiry_date
}
