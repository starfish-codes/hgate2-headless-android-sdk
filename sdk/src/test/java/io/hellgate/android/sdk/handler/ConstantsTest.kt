package io.hellgate.android.sdk.handler

import io.hellgate.android.sdk.Constants
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ConstantsTest {
    @Test
    fun `Constants, Check if all constants are correct`() {
        assertThat(Constants).isInstanceOf(Constants::class.java)
        assertThat(Constants.HG_STAGING_URL).isEqualTo("https://staging.hellgate.dev")
    }
}
