package io.hellgate.android.sdk.client.hellgate

import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.databind.JsonNode
import io.hellgate.android.sdk.element.additionaldata.AdditionalDataTypes

internal data class SessionResponse(
    val data: Data?,
    val nextAction: NextAction?,
    val status: String?,
) {
    @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION, property = "data")
    @JsonSubTypes(
        JsonSubTypes.Type(value = Data.TokenId::class),
        JsonSubTypes.Type(value = Data.TokenizationParam::class),
    )
    sealed class Data {
        data class TokenId(val tokenId: String) : Data()

        data class TokenizationParam(
            val jwk: JsonNode,
        ) : Data()
    }
}

internal enum class NextAction {
    @JsonProperty("tokenize_card")
    TOKENIZE_CARD,

    @JsonProperty("wait")
    WAIT,
}

internal data class SessionCompleteTokenizeCard(
    val action: String = "tokenize_card",
    val result: Result,
) {
    data class Result(
        val encPayload: String,
        val additionalData: AdditionalData?,
    )

    data class AdditionalData(
        @JsonProperty("cardholder_name")
        val cardholderName: String?,
    ) {
        companion object {
            fun Map<AdditionalDataTypes, String>.toDTO() = AdditionalData(this[AdditionalDataTypes.CARDHOLDER_NAME])
        }
    }
}
