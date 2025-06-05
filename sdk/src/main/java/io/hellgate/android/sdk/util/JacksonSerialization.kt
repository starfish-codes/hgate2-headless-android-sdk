package io.hellgate.android.sdk.util

import arrow.core.Either
import arrow.core.raise.catch
import arrow.core.raise.either
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.hellgate.android.sdk.model.Problem
import io.ktor.http.ContentType
import io.ktor.serialization.Configuration
import io.ktor.serialization.jackson.JacksonConverter

internal val defaultMapper = jsonMapper {
    configureJackson()
}

internal fun Configuration.registerJacksonConverter() {
    register(ContentType.Application.Json, JacksonConverter(defaultMapper, true))
}

internal inline fun <reified T> String.jsonDeserialize(): T =
    when (T::class) {
        is JsonNode -> defaultMapper.readTree(this) as T
        else -> defaultMapper.readValue(this)
    }

internal inline fun <reified T> T.jsonSerialize(): String = defaultMapper.writeValueAsString(this)

internal inline fun <reified T> T.toJson(): Either<DeserializationError, String> =
    either {
        catch({ this@toJson.jsonSerialize() }) {
            raise(DeserializationError("Error while converting object to Json String", throwable = it))
        }
    }

internal inline fun <reified T> String.toObject(): Either<DeserializationError, T> =
    either {
        catch({ this@toObject.jsonDeserialize() }) {
            raise(DeserializationError("Error while Parsing Json String", throwable = it))
        }
    }

private fun JsonMapper.Builder.configureJackson() {
    addModule(kotlinModule())
    disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    serializationInclusion(JsonInclude.Include.NON_NULL)
    defaultPrettyPrinter(
        DefaultPrettyPrinter().apply {
            indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance)
            indentObjectsWith(DefaultIndenter("  ", "\n"))
        },
    )
}

internal data class DeserializationError(
    override val message: String,
    override val throwable: Throwable? = null,
) : Problem
