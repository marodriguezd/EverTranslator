package tw.firemaples.onscreenocr.floatings.manager

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FloatingStateManagerStateTest {

    @Test
    fun testAllowedStateTransitions_fromResultDisplaying() {
        val allowedNextStates = arrayOf(
            State.Idle::class,
            State.TextTranslating::class,
            State.ScreenCircling::class
        )

        assertTrue(allowedNextStates.contains(State.Idle::class))
        assertTrue(allowedNextStates.contains(State.TextTranslating::class))
        assertTrue(allowedNextStates.contains(State.ScreenCircling::class))
        assertFalse(allowedNextStates.contains(State.ScreenCapturing::class))
    }

    @Test
    fun testMainBarButtonsVisibility_inResultDisplayingState() {
        val state: State = State.ResultDisplaying

        val displaySelectButton = state == State.Idle || state == State.ResultDisplaying
        val displayTranslateButton = state == State.ScreenCircled
        val displayCloseButton =
            state == State.ScreenCircling || state == State.ScreenCircled || state == State.ResultDisplaying

        assertTrue(displaySelectButton)
        assertFalse(displayTranslateButton)
        assertTrue(displayCloseButton)
    }
}
