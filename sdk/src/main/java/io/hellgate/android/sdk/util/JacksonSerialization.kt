package io.hellgate.android.sdk.util

import arrow.core.Either
import arrow.core.raise.*
import com.ethlo.time.ITU
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.*
import io.hellgate.android.sdk.model.Problem
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.serialization.jackson.*
import java.io.IOException
import java.time.DateTimeException
import java.time.OffsetDateTime

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
    addModule(javaTimeModule())
    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
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

private fun javaTimeModule(): JavaTimeModule {
    val module = JavaTimeModule()
    module.addDeserializer(OffsetDateTime::class.java, Rfc3339OffsetDateTimeDeserializer)
    return module
}

internal object Rfc3339OffsetDateTimeDeserializer : StdDeserializer<OffsetDateTime>(OffsetDateTime::class.java) {
    @Suppress("SwallowedException")
    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(
        jsonparser: JsonParser,
        context: DeserializationContext,
    ): OffsetDateTime {
        val value = jsonparser.text
        return try {
            ITU.parseDateTime(value)
        } catch (e: DateTimeException) {
            throw context.weirdStringException(value, OffsetDateTime::class.java, e.message)
        }
    }
}

internal data class DeserializationError(
    override val message: String,
    override val throwable: Throwable? = null,
) : Problem
