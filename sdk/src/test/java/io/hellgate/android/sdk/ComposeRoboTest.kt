package io.hellgate.android.sdk

import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.assertj.core.api.Assertions.assertThat
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.shadows.ShadowLog

@RunWith(AndroidJUnit4::class)
class SampleComposeRoboTest : ComposeRoboTest() {
    @Test
    fun `Create a Text composable, testing for text equality is positive`() {
        composeTestRule.setContent {
            Text("Hello, World!", modifier = Modifier.testTag("helloWorld"))
        }

        composeTestRule.onNodeWithTag("helloWorld")
            .assertExists()
            .assertIsDisplayed()
            .assertTextEquals("Hello, World!")
        assertThat(true).isTrue()
    }
}

@Suppress("UnnecessaryAbstractClass")
@RunWith(AndroidJUnit4::class)
abstract class ComposeRoboTest {
    @Before
    @Throws(Exception::class)
    fun setUp() {
        ShadowLog.stream = System.out // Redirect Logcat to console
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    fun printTree(tag: String = "") = composeTestRule.onRoot(useUnmergedTree = true).printToLog(tag)
}

fun SemanticsNodeInteraction.assertIsTextFieldError(): SemanticsNodeInteraction {
    assert(SemanticsMatcher.expectValue(SemanticsProperties.Error, "Invalid input"))
    return this
}

fun SemanticsNodeInteraction.assertIsNotTextFieldError(): SemanticsNodeInteraction {
    assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Error))
    return this
}
