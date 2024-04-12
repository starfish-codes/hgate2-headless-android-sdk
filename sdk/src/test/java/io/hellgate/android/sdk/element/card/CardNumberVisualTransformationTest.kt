package io.hellgate.android.sdk.element.card

import androidx.compose.ui.text.AnnotatedString
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CardNumberVisualTransformationTest {
    @Test
    fun `Do visual transformation on input values`() {
        val visualTransformation = CardNumberVisualTransformation()

        assertThat(visualTransformation.filter(AnnotatedString(CardNumberTestConstants.VISA_NO_SPACES)).text)
            .isEqualTo(AnnotatedString(CardNumberTestConstants.VISA_WITH_SPACES))

        assertThat(visualTransformation.filter(AnnotatedString(CardNumberTestConstants.AMEX_NO_SPACES)).text)
            .isEqualTo(AnnotatedString(CardNumberTestConstants.AMEX_WITH_SPACES))

        assertThat(visualTransformation.filter(AnnotatedString(CardNumberTestConstants.DISCOVER_NO_SPACES)).text)
            .isEqualTo(AnnotatedString(CardNumberTestConstants.DISCOVER_WITH_SPACES))

        assertThat(visualTransformation.filter(AnnotatedString(CardNumberTestConstants.DINERS_CLUB_14_NO_SPACES)).text)
            .isEqualTo(AnnotatedString(CardNumberTestConstants.DINERS_CLUB_14_WITH_SPACES))

        assertThat(visualTransformation.filter(AnnotatedString(CardNumberTestConstants.DINERS_CLUB_16_NO_SPACES)).text)
            .isEqualTo(AnnotatedString(CardNumberTestConstants.DINERS_CLUB_16_WITH_SPACES))

        assertThat(visualTransformation.filter(AnnotatedString(CardNumberTestConstants.JCB_NO_SPACES)).text)
            .isEqualTo(AnnotatedString(CardNumberTestConstants.JCB_WITH_SPACES))

        assertThat(visualTransformation.filter(AnnotatedString(CardNumberTestConstants.UNIONPAY_NO_SPACES)).text)
            .isEqualTo(AnnotatedString(CardNumberTestConstants.UNIONPAY_WITH_SPACES))
    }

    @Test
    fun `6 digit input is given on a visa card, OffsetMapping should return something`() {
        val visualTransformation = CardNumberVisualTransformation()
        val transformedText = visualTransformation.filter(AnnotatedString(CardNumberTestConstants.VISA_NO_SPACES))
        val offsetMapping = transformedText.offsetMapping
        val originalToTransformed = offsetMapping.originalToTransformed(6)
        assertThat(originalToTransformed).isEqualTo(7)
        val transformedToOriginal = offsetMapping.transformedToOriginal(7)
        assertThat(transformedToOriginal).isEqualTo(6)

        val originalToTransformed2 = offsetMapping.originalToTransformed(2)
        assertThat(originalToTransformed2).isEqualTo(2)
        val transformedToOriginal2 = offsetMapping.transformedToOriginal(2)
        assertThat(transformedToOriginal2).isEqualTo(2)

        val originalToTransformed3 = offsetMapping.originalToTransformed(16)
        assertThat(originalToTransformed3).isEqualTo(19)
        val transformedToOriginal3 = offsetMapping.transformedToOriginal(16)
        assertThat(transformedToOriginal3).isEqualTo(13)

        val originalToTransformed4 = offsetMapping.originalToTransformed(9)
        assertThat(originalToTransformed4).isEqualTo(11)
        val transformedToOriginal4 = offsetMapping.transformedToOriginal(9)
        assertThat(transformedToOriginal4).isEqualTo(8)
    }

    @Test
    fun `6 digit input is given on a amex card, OffsetMapping should return something`() {
        val visualTransformation = CardNumberVisualTransformation()
        val transformedText = visualTransformation.filter(AnnotatedString(CardNumberTestConstants.AMEX_NO_SPACES))
        val offsetMapping = transformedText.offsetMapping
        val originalToTransformed = offsetMapping.originalToTransformed(6)
        assertThat(originalToTransformed).isEqualTo(7)
        val transformedToOriginal = offsetMapping.transformedToOriginal(7)
        assertThat(transformedToOriginal).isEqualTo(6)

        val originalToTransformed2 = offsetMapping.originalToTransformed(2)
        assertThat(originalToTransformed2).isEqualTo(2)
        val transformedToOriginal2 = offsetMapping.transformedToOriginal(2)
        assertThat(transformedToOriginal2).isEqualTo(2)

        val originalToTransformed3 = offsetMapping.originalToTransformed(16)
        assertThat(originalToTransformed3).isEqualTo(18)
        val transformedToOriginal3 = offsetMapping.transformedToOriginal(16)
        assertThat(transformedToOriginal3).isEqualTo(14)

        val originalToTransformed4 = offsetMapping.originalToTransformed(9)
        assertThat(originalToTransformed4).isEqualTo(10)
        val transformedToOriginal4 = offsetMapping.transformedToOriginal(9)
        assertThat(transformedToOriginal4).isEqualTo(8)
    }
}
