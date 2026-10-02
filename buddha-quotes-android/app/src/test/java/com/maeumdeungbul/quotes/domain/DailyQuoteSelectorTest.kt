package com.maeumdeungbul.quotes.domain

import com.maeumdeungbul.quotes.domain.usecase.DailyQuoteSelector
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyQuoteSelectorTest {

    private val ids = (1L..50L).toList()
    private val date = LocalDate.of(2026, 10, 2)

    @Test
    fun sameDateAndCandidates_giveSameQuote() {
        val first = DailyQuoteSelector.select(date, ids, emptyList())
        val second = DailyQuoteSelector.select(date, ids.shuffled(), emptyList())
        assertEquals(first, second)
    }

    @Test
    fun recentlyShownQuotes_areExcluded() {
        val recent = (1L..49L).toList()
        assertEquals(50L, DailyQuoteSelector.select(date, ids, recent))
    }

    @Test
    fun whenEverythingWasShown_fallsBackToAllCandidates() {
        val picked = DailyQuoteSelector.select(date, ids, ids)
        assertTrue(picked in ids)
    }

    @Test
    fun noCandidates_returnsNull() {
        assertNull(DailyQuoteSelector.select(date, emptyList(), emptyList()))
    }

    @Test
    fun differentDays_usuallyGiveDifferentQuotes() {
        val picks = (0L until 30L).map { DailyQuoteSelector.select(date.plusDays(it), ids, emptyList()) }.toSet()
        assertTrue("expected variety but got $picks", picks.size > 10)
    }

    @Test
    fun recommend_excludesAndLimits() {
        val result = DailyQuoteSelector.recommend(date, ids, excludeIds = listOf(1L, 2L), count = 5)
        assertEquals(5, result.size)
        assertEquals(5, result.toSet().size)
        assertTrue(result.none { it == 1L || it == 2L })
        assertEquals(result, DailyQuoteSelector.recommend(date, ids, listOf(1L, 2L), 5))
        assertNotEquals(result, DailyQuoteSelector.recommend(date.plusDays(1), ids, listOf(1L, 2L), 5))
    }
}
