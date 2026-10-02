package com.maeumdeungbul.quotes.domain

import com.maeumdeungbul.quotes.domain.model.Category
import com.maeumdeungbul.quotes.domain.model.Quote
import com.maeumdeungbul.quotes.domain.model.QuoteType
import org.junit.Assert.assertEquals
import org.junit.Test

class QuoteAttributionTest {

    private fun quote(author: String?, source: String?, detail: String?) = Quote(
        id = 1, text = "본문", author = author, source = source, sourceDetail = detail,
        category = Category("mind", "마음"), quoteType = QuoteType.BUDDHA, verified = false, favorite = false,
    )

    @Test
    fun joinsAuthorSourceAndDetail() {
        assertEquals("부처님 · 법구경 제5게", quote("부처님", "법구경", "제5게").attribution)
    }

    @Test
    fun skipsMissingParts() {
        assertEquals("법구경", quote(null, "법구경", null).attribution)
        assertEquals("지눌", quote("지눌", null, null).attribution)
        assertEquals("", quote(null, null, null).attribution)
    }
}
