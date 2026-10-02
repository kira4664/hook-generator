package com.maeumdeungbul.quotes.domain.usecase

import java.time.LocalDate
import kotlin.random.Random

/**
 * 오늘의 말씀·추천 말씀 선택 규칙.
 * 날짜를 시드로 쓰기 때문에 같은 날, 같은 후보라면 언제·어디서 호출해도 같은 결과가 나온다.
 * (앱 화면과 알림 Worker 가 동시에 골라도 일치한다.)
 */
object DailyQuoteSelector {

    /** 최근 이력에 없는 말씀 중에서 하나를 고른다. 모두 이력에 있으면 전체에서 고른다. */
    fun select(
        date: LocalDate,
        candidateIds: Collection<Long>,
        recentlyShownIds: Collection<Long>,
    ): Long? {
        val sorted = candidateIds.distinct().sorted()
        if (sorted.isEmpty()) return null
        val recent = recentlyShownIds.toSet()
        val pool = sorted.filterNot { it in recent }.ifEmpty { sorted }
        return pool[Random(date.toEpochDay()).nextInt(pool.size)]
    }

    fun recommend(
        date: LocalDate,
        candidateIds: Collection<Long>,
        excludeIds: Collection<Long>,
        count: Int,
    ): List<Long> {
        if (count <= 0) return emptyList()
        val exclude = excludeIds.toSet()
        return candidateIds.distinct().sorted()
            .filterNot { it in exclude }
            .shuffled(Random(date.toEpochDay() * 31 + 7))
            .take(count)
    }
}
