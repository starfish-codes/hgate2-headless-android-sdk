package io.hellgate.android.sdk.element.additionaldata

import org.assertj.core.api.Assertions
import org.junit.Test

class AdditionalDataTypesTest {
    @Test
    fun getLabel() {
        AdditionalDataTypes.entries.forEach {
            Assertions.assertThat(it.getLabel()).isNotEmpty().isEqualTo(it.label)
        }
    }
}
