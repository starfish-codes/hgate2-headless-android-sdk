package io.hellgate.android.sdk

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.hellgate.android.sdk.element.AdditionalDataFieldState
import io.hellgate.android.sdk.element.additionaldata.AdditionalDataTypes
import io.hellgate.android.sdk.element.additionaldata.DataField
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    fun printTree(tag: String = "") = composeTestRule.onRoot(useUnmergedTree = true).printToLog(tag)

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertThat(appContext.packageName).isEqualTo("io.hellgate.android.sdk.test")
    }

    private fun field() = composeTestRule.onNodeWithTag(DataField.TEST_TAG + AdditionalDataTypes.CARDHOLDER_NAME.name)

    @Test
    fun assertOutlineColorTest() = runTest {
        val redColor = Color(0xFFFF1744)
        val cardholderField = DataField(additionalDataTypes = AdditionalDataTypes.CARDHOLDER_NAME)
        var cardholderFieldState: AdditionalDataFieldState? = null

        composeTestRule.setContent {
            MaterialTheme(colorScheme = lightColorScheme(error = redColor)) {
                cardholderField.ComposeUI(
                    onValueChange = { cardholderFieldState = it },
                    true,
                )
            }
        }

        field().apply { performTextInput("John Doe") }
            .assertTextEquals("Cardholder Name", "John Doe")

        field().assertContainsColor(redColor)
        // Check final state
        assertThat(cardholderField.value()).isEqualTo("John Doe")
        assertThat(cardholderFieldState).isEqualTo(AdditionalDataFieldState(empty = false, value = "John Doe"))
    }

    private fun SemanticsNodeInteraction.assertContainsColor(color: Color) {
        val imageBitmap = this.captureToImage()
        val buffer = IntArray(imageBitmap.width * imageBitmap.height)
        imageBitmap.readPixels(buffer, 0, 0, imageBitmap.width - 1, imageBitmap.height - 1)
        val pixelColors = PixelMap(buffer, 0, 0, imageBitmap.width - 1, imageBitmap.height - 1)

        (0 until imageBitmap.width).forEach { x ->
            (0 until imageBitmap.height).forEach { y ->
                if (pixelColors[x, y] == color) {
                    println("Red color found at $x, $y")
                    return
                }
            }
        }

        fail<Unit>("color not found")
    }
}
