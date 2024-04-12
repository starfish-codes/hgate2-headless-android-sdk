package io.hellgate.android.sdk.client.hellgate

import io.hellgate.android.sdk.client.hellgate.Segments.COMPLETE_ACTION
import io.hellgate.android.sdk.client.hellgate.Segments.SESSIONS
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SegmentsTest {
    @Test
    fun `Segments, Check if all segments are correct`() {
        assertThat(Segments).isInstanceOf(Segments::class.java)
        assertThat(SESSIONS).isEqualTo("sessions")
        assertThat(COMPLETE_ACTION).isEqualTo("complete-action")
    }
}
