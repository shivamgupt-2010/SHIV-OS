package com.example

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LauncherActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<LauncherActivity>()

    @Test
    fun testLauncherActivityStarts() {
        System.out.println("LauncherActivity tested")
    }
}
