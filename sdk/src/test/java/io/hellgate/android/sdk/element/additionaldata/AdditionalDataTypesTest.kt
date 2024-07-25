package io.hellgate.android.sdk.element.additionaldata

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AdditionalDataTypesTest {
    @Test
    fun getLabel() {
        assertThat(AdditionalDataTypes.EMAIL.getLabel()).isNotEmpty().isEqualTo("E-Mail")
    }

    @Test
    fun getSerializeName() {
        assertThat(AdditionalDataTypes.CARDHOLDER_NAME.getSerializeName()).isEqualTo("cardholder_name")
        assertThat(AdditionalDataTypes.EMAIL.getSerializeName()).isEqualTo("email")
    }
}
