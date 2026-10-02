package com.maeumdeungbul.quotes.domain

import com.maeumdeungbul.quotes.domain.model.BreathPhase
import com.maeumdeungbul.quotes.domain.model.BreathStep
import com.maeumdeungbul.quotes.domain.model.BreathingPattern
import org.junit.Assert.assertEquals
import org.junit.Test

class BreathingPatternTest {

    @Test
    fun calmPattern_isFourTwoSix() {
        assertEquals(
            listOf(
                BreathStep(BreathPhase.INHALE, 4),
                BreathStep(BreathPhase.HOLD, 2),
                BreathStep(BreathPhase.EXHALE, 6),
            ),
            BreathingPattern.CALM.steps,
        )
        assertEquals(12, BreathingPattern.CALM.cycleSeconds)
    }

    @Test
    fun boxPattern_hasFourSteps() {
        assertEquals(4, BreathingPattern.BOX.steps.size)
    }

    @Test
    fun unknownId_fallsBackToCalm() {
        assertEquals(BreathingPattern.CALM, BreathingPattern.fromId("nope"))
        assertEquals(BreathingPattern.RELAX, BreathingPattern.fromId("relax"))
    }
}
