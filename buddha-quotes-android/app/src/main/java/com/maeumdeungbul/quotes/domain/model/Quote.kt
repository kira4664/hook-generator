package com.maeumdeungbul.quotes.domain.model

/**
 * 말씀의 성격. 인터넷에 떠도는 문장을 부처님의 직접 발언으로 단정하지 않기 위해 반드시 구분한다.
 * - [BUDDHA]: 초기 경전 등에서 부처님이 직접 하신 말씀으로 전해지는 것(경전 출처 필수)
 * - [SCRIPTURE]: 대승 경전 등 불교 경전에 근거한 구절
 * - [MASTER]: 불교 스승(고승·선사)의 말씀
 * - [WISDOM]: 불교적 가르침을 풀어 쓴 지혜의 글
 * - [UNKNOWN]: 출처 확인 중
 */
enum class QuoteType { BUDDHA, SCRIPTURE, MASTER, WISDOM, UNKNOWN }

data class Category(
    val id: String,
    val name: String,
)

data class Quote(
    val id: Long,
    val text: String,
    val author: String?,
    val source: String?,
    val sourceDetail: String?,
    val category: Category,
    val quoteType: QuoteType,
    /** 편집자가 원전과 대조해 검수를 마쳤는지 여부. false 이면 "출처 확인 중"을 함께 표시한다. */
    val verified: Boolean,
    val favorite: Boolean,
) {
    /** 화면·공유용 출처 문구. 예: "부처님 · 법구경 제5게" */
    val attribution: String
        get() {
            val sourceText = listOfNotNull(source, sourceDetail).joinToString(" ")
            return listOfNotNull(author, sourceText.ifBlank { null }).joinToString(" · ")
        }
}

data class QuoteFilter(
    val query: String = "",
    val categoryId: String? = null,
    val quoteType: QuoteType? = null,
    /** 인물 또는 출처(경전명) */
    val origin: String? = null,
)

enum class ReportReason { WRONG_SOURCE, WRONG_AUTHOR, TYPO, OTHER }

data class QuoteReport(
    val quoteId: Long,
    val reason: ReportReason,
    val memo: String?,
)
