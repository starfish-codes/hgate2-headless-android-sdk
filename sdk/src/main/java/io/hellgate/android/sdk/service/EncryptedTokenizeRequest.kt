package io.hellgate.android.sdk.service

import com.fasterxml.jackson.databind.JsonNode
import com.nimbusds.jose.EncryptionMethod
import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jose.JWEHeader
import com.nimbusds.jose.crypto.RSAEncrypter
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.EncryptedJWT
import com.nimbusds.jwt.JWTClaimsSet
import io.hellgate.android.sdk.element.additionaldata.AdditionalDataTypes
import io.hellgate.android.sdk.model.CardData
import io.hellgate.android.sdk.util.jsonSerialize

private const val BASE_YEAR = 2000

internal typealias JWE = String

internal fun createJWE(
    cardData: CardData,
    additionalData: Map<AdditionalDataTypes, String>,
    jwk: JsonNode,
): JWE {
    val header = JWEHeader(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM)
    val claims = JWTClaimsSet.Builder()
        .apply {
            claim("expiry_month", cardData.month.toInt())
            claim("expiry_year", cardData.year.toInt() + BASE_YEAR)
            claim("account_number", cardData.cardNumber)
            claim("security_code", cardData.cvc)
        }
        .apply {
            additionalData.forEach { (type, value) -> claim(type.getSerializeName(), value) }
        }
        .build()

    val jwt = EncryptedJWT(header, claims)
    val rsaKey = RSAKey.parse(jwk.jsonSerialize())

    return jwt.apply {
        encrypt(RSAEncrypter(rsaKey))
    }.serialize()
}
