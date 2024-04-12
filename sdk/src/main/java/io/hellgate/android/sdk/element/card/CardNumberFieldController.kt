package io.hellgate.android.sdk.element.card

import io.hellgate.android.sdk.R
import io.hellgate.android.sdk.element.*
import io.hellgate.android.sdk.model.*
import kotlinx.coroutines.flow.*
import org.jetbrains.annotations.VisibleForTesting

internal class CardNumberFieldController : IFieldController {
    private val stateConfig = CardNumberStateConfig

    private val _fieldValue: MutableStateFlow<String> = MutableStateFlow("")
    private val _hasFocus: MutableStateFlow<Boolean> = MutableStateFlow(false)

    @VisibleForTesting
    val impliedCardBrand: Flow<CardBrand> = _fieldValue.map {
        CardBrand.getCardBrands(it).firstOrNull() ?: CardBrand.Unknown
    }

    override val fieldValue: Flow<String> = _fieldValue.asStateFlow()
    override val fieldState: Flow<TextFieldState> =
        combine(impliedCardBrand, _fieldValue) { brand, fieldValue ->
            stateConfig.determineState(CardNumberStateInput(brand, fieldValue))
        }
    override val hasFocus: Flow<Boolean> = _hasFocus.asStateFlow()

    override val visibleError: Flow<Boolean> =
        combine(fieldState, _hasFocus) { fieldState, hasFocus ->
            fieldState.shouldShowError(hasFocus)
        }

    val trailingIcon: Flow<Int> = combine(_fieldValue, visibleError) { fieldValue, visibleError ->
        when {
            fieldValue.isEmpty() -> R.drawable.ic_cards_unknown
            visibleError -> R.drawable.ic_cards_error
            else -> {
                CardBrand.getCardBrands(fieldValue)
                    .map { cardBrand -> cardBrand.icon }
                    .firstOrNull() ?: R.drawable.ic_cards_unknown
            }
        }
    }.distinctUntilChanged()

    suspend fun getCardNumber(): CardNumber.Validated? =
        impliedCardBrand.first().let { brand ->
            brand.isValidCardNumberLength(_fieldValue.value).let {
                if (it) tryValidate(_fieldValue.value) else null
            }
        }

    private fun tryValidate(value: String): CardNumber.Validated? = CardNumber.Unvalidated(value).validate(value.length)

    override fun onValueChanged(value: String) {
        _fieldValue.value = value.filter { it.isDigit() }
    }

    override fun onFocusChange(newHasFocus: Boolean) {
        _hasFocus.value = newHasFocus
    }
}
