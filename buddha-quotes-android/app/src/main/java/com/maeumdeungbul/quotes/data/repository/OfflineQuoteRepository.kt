package com.maeumdeungbul.quotes.data.repository

import androidx.room.withTransaction
import com.maeumdeungbul.quotes.data.datasource.ContentSeeder
import com.maeumdeungbul.quotes.data.local.MaeumDatabase
import com.maeumdeungbul.quotes.data.local.SqlLike
import com.maeumdeungbul.quotes.data.local.entity.CategoryEntity
import com.maeumdeungbul.quotes.data.local.entity.DailyQuoteEntity
import com.maeumdeungbul.quotes.data.local.entity.FavoriteEntity
import com.maeumdeungbul.quotes.data.local.entity.QuoteReportEntity
import com.maeumdeungbul.quotes.data.local.entity.QuoteWithMeta
import com.maeumdeungbul.quotes.data.local.entity.RecentViewEntity
import com.maeumdeungbul.quotes.domain.model.Category
import com.maeumdeungbul.quotes.domain.model.Quote
import com.maeumdeungbul.quotes.domain.model.QuoteFilter
import com.maeumdeungbul.quotes.domain.model.QuoteReport
import com.maeumdeungbul.quotes.domain.repository.QuoteRepository
import com.maeumdeungbul.quotes.domain.usecase.DailyQuoteSelector
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/** Room 을 단일 진실 공급원으로 쓰는 오프라인 저장소. 모든 조회 전에 콘텐츠 시드를 보장한다. */
class OfflineQuoteRepository(
    private val database: MaeumDatabase,
    private val seeder: ContentSeeder,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis,
) : QuoteRepository {

    private val quoteDao = database.quoteDao()
    private val categoryDao = database.categoryDao()
    private val favoriteDao = database.favoriteDao()
    private val recentViewDao = database.recentViewDao()
    private val dailyQuoteDao = database.dailyQuoteDao()
    private val reportDao = database.quoteReportDao()

    private fun <T> seeded(block: suspend () -> Flow<T>): Flow<T> = flow {
        seeder.ensureSeeded()
        emitAll(block())
    }

    override fun observeDailyQuote(date: LocalDate): Flow<Quote?> = seeded {
        val id = dailyQuoteId(date)
        if (id == null) flowOf(null) else quoteDao.observeById(id).map { it?.toDomain() }
    }

    override suspend fun getDailyQuote(date: LocalDate): Quote? {
        seeder.ensureSeeded()
        val id = dailyQuoteId(date) ?: return null
        return quoteDao.getById(id)?.toDomain()
    }

    override fun observeQuote(id: Long): Flow<Quote?> = seeded {
        quoteDao.observeById(id).map { it?.toDomain() }
    }

    override fun searchQuotes(filter: QuoteFilter): Flow<List<Quote>> = seeded {
        quoteDao.search(
            pattern = SqlLike.containsPattern(filter.query),
            categoryId = filter.categoryId,
            quoteType = filter.quoteType?.name,
            origin = filter.origin,
        ).map { list -> list.map { it.toDomain() } }
    }

    override fun observeCategories(): Flow<List<Category>> = seeded {
        categoryDao.observeAll().map { list -> list.map { it.toDomain() } }
    }

    override fun observeOrigins(): Flow<List<String>> = seeded { quoteDao.observeOrigins() }

    override fun observeRecommended(date: LocalDate, count: Int): Flow<List<Quote>> = seeded {
        val dailyId = dailyQuoteId(date)
        val ids = DailyQuoteSelector.recommend(date, quoteDao.getAllIds(), listOfNotNull(dailyId), count)
        quoteDao.observeByIds(ids).map { list ->
            val order = ids.withIndex().associate { (index, id) -> id to index }
            list.sortedBy { order[it.quote.id] ?: Int.MAX_VALUE }.map { it.toDomain() }
        }
    }

    override fun observeRelated(quote: Quote, count: Int): Flow<List<Quote>> = seeded {
        quoteDao.observeRelated(quote.category.id, quote.id, count).map { list -> list.map { it.toDomain() } }
    }

    override fun observeRecentlyViewed(limit: Int): Flow<List<Quote>> = seeded {
        quoteDao.observeRecentlyViewed(limit).map { list -> list.map { it.toDomain() } }
    }

    override fun observeFavorites(query: String, categoryId: String?): Flow<List<Quote>> = seeded {
        quoteDao.observeFavorites(SqlLike.containsPattern(query), categoryId)
            .map { list -> list.map { it.toDomain() } }
    }

    override fun observeFavoriteCategories(): Flow<List<Category>> = seeded {
        categoryDao.observeFavoriteCategories().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun randomQuoteId(excludeId: Long?): Long? {
        seeder.ensureSeeded()
        return quoteDao.randomId(excludeId)
    }

    override suspend fun toggleFavorite(quoteId: Long) {
        database.withTransaction {
            if (favoriteDao.isFavorite(quoteId)) {
                favoriteDao.delete(quoteId)
            } else {
                favoriteDao.insert(FavoriteEntity(quoteId, currentTimeMillis()))
            }
        }
    }

    override suspend fun markViewed(quoteId: Long) {
        recentViewDao.upsert(RecentViewEntity(quoteId, currentTimeMillis()))
        recentViewDao.trim(RECENT_VIEW_LIMIT)
    }

    override suspend fun saveReport(report: QuoteReport) {
        reportDao.insert(
            QuoteReportEntity(
                quoteId = report.quoteId,
                reason = report.reason,
                memo = report.memo?.takeIf { it.isNotBlank() },
                createdAt = currentTimeMillis(),
            ),
        )
    }

    override suspend fun clearFavorites() = favoriteDao.deleteAll()

    override suspend fun clearRecentlyViewed() = recentViewDao.deleteAll()

    /** 해당 날짜의 말씀을 이력에서 찾고, 없으면 새로 골라 저장한다. */
    private suspend fun dailyQuoteId(date: LocalDate): Long? {
        val key = date.toString()
        dailyQuoteDao.getQuoteId(key)?.let { saved ->
            if (quoteDao.exists(saved)) return saved
            dailyQuoteDao.delete(key) // 콘텐츠 업데이트로 삭제된 말씀
        }
        val candidates = quoteDao.getAllIds()
        val exclusion = minOf(RECENT_DAILY_EXCLUSION, candidates.size * 2 / 3)
        val recent = dailyQuoteDao.recentQuoteIds(key, exclusion)
        val picked = DailyQuoteSelector.select(date, candidates, recent) ?: return null
        dailyQuoteDao.insert(DailyQuoteEntity(key, picked))
        // 알림 Worker 와 동시에 실행되어 먼저 저장된 값이 있으면 그 값을 따른다.
        return dailyQuoteDao.getQuoteId(key) ?: picked
    }

    private companion object {
        const val RECENT_VIEW_LIMIT = 50
        const val RECENT_DAILY_EXCLUSION = 30
    }
}

internal fun QuoteWithMeta.toDomain() = Quote(
    id = quote.id,
    text = quote.text,
    author = quote.author,
    source = quote.source,
    sourceDetail = quote.sourceDetail,
    category = Category(quote.categoryId, categoryName ?: quote.categoryId),
    quoteType = quote.quoteType,
    verified = quote.verified,
    favorite = isFavorite,
)

internal fun CategoryEntity.toDomain() = Category(id = id, name = name)
