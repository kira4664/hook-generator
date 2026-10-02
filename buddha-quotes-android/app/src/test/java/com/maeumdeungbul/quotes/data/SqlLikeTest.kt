package com.maeumdeungbul.quotes.data

import com.maeumdeungbul.quotes.data.local.SqlLike
import org.junit.Assert.assertEquals
import org.junit.Test

class SqlLikeTest {

    @Test
    fun blankQuery_matchesEverything() {
        assertEquals("%", SqlLike.containsPattern("   "))
    }

    @Test
    fun wrapsAndTrims() {
        assertEquals("%마음%", SqlLike.containsPattern("  마음 "))
    }

    @Test
    fun escapesWildcards() {
        assertEquals("%100\\%%", SqlLike.containsPattern("100%"))
        assertEquals("%a\\_b%", SqlLike.containsPattern("a_b"))
        assertEquals("%\\\\%", SqlLike.containsPattern("\\"))
    }
}
