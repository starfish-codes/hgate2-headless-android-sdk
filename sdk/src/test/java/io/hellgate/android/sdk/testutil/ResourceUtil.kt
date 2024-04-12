package io.hellgate.android.sdk.testutil

import io.hellgate.android.sdk.util.toObject
import org.assertj.core.api.Assertions.fail

internal object ResourceUtil {
    inline fun <reified Type> readJsonResource(name: String): Type =
        this.javaClass.getResource(name)
            ?.readText()
            ?.toObject<Type>()
            ?.getOrNull()
            ?: fail("Could not read json")

    fun readResource(name: String): String =
        this.javaClass.getResource(name)
            ?.readText()
            ?: fail("Could not read json")
}

fun String.jsonMinify() = this.replace("\n", "").replace(" ", "").replace("\t", "")
