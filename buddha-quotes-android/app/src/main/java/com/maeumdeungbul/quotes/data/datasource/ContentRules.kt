package com.maeumdeungbul.quotes.data.datasource

import com.maeumdeungbul.quotes.data.model.QuoteJson
import com.maeumdeungbul.quotes.domain.model.QuoteType

/**
 * 콘텐츠 품질 규칙. 시드할 때 위반 항목은 걸러내거나 안전한 쪽으로 낮추고,
 * 단위 테스트(ContentAssetsTest)는 번들된 JSON 에 위반이 하나도 없는지 검사한다.
 */
object ContentRules {

    fun violations(quote: QuoteJson, categoryIds: Set<String>): List<String> = buildList {
        if (quote.id <= 0) add("id 는 1 이상이어야 합니다")
        if (quote.text.isBlank()) add("본문이 비어 있습니다")
        if (quote.category !in categoryIds) add("알 수 없는 카테고리: ${quote.category}")
        val type = parseType(quote.quoteType)
        if (type == null) add("알 수 없는 quoteType: ${quote.quoteType}")
        // 부처님 말씀은 경전 근거가 있을 때만 표시한다(오귀속 방지).
        if (type == QuoteType.BUDDHA && quote.source.isNullOrBlank()) add("BUDDHA 유형은 source(경전)가 필요합니다")
        if (quote.verified && quote.source.isNullOrBlank() && quote.author.isNullOrBlank()) {
            add("검수 완료(verified) 항목은 출처나 인물이 있어야 합니다")
        }
    }

    fun parseType(value: String): QuoteType? = QuoteType.entries.firstOrNull { it.name == value }

    /** 시드 시 적용하는 유형: 근거 없는 BUDDHA·알 수 없는 값은 UNKNOWN(출처 확인 중)으로 낮춘다. */
    fun effectiveType(quote: QuoteJson): QuoteType {
        val type = parseType(quote.quoteType) ?: return QuoteType.UNKNOWN
        return if (type == QuoteType.BUDDHA && quote.source.isNullOrBlank()) QuoteType.UNKNOWN else type
    }
}
