package io.hellgate.android.sdk.demo.client

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import io.ktor.http.ContentType
import io.ktor.serialization.Configuration
import io.ktor.serialization.jackson.JacksonConverter

val defaultMapper = jsonMapper {
    configureJackson()
}

fun Configuration.registerJacksonConverter() {
    register(ContentType.Application.Json, JacksonConverter(defaultMapper, true))
}

private fun JsonMapper.Builder.configureJackson() {
    addModule(kotlinModule())
    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    serializationInclusion(JsonInclude.Include.NON_NULL)
    defaultPrettyPrinter(
        DefaultPrettyPrinter().apply {
            indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance)
            indentObjectsWith(DefaultIndenter("  ", "\n"))
        }
    )
}
