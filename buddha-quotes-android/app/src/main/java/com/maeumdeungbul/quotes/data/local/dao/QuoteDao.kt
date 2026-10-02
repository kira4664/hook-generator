package com.maeumdeungbul.quotes.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.maeumdeungbul.quotes.data.local.entity.QuoteEntity
import com.maeumdeungbul.quotes.data.local.entity.QuoteWithMeta
import kotlinx.coroutines.flow.Flow

private const val QUOTE_COLUMNS =
    "q.*, c.name AS category_name, (f.quote_id IS NOT NULL) AS is_favorite"
private const val QUOTE_JOINS =
    "LEFT JOIN categories c ON c.id = q.category_id LEFT JOIN favorites f ON f.quote_id = q.id"
private const val SELECT_QUOTES = "SELECT $QUOTE_COLUMNS FROM quotes q $QUOTE_JOINS"

/** [pattern] 은 SqlLike.containsPattern 으로 만든 값(빈 검색어이면 "%") */
private const val MATCHES_PATTERN =
    "(q.text LIKE :pattern ESCAPE '\\' OR q.author LIKE :pattern ESCAPE '\\' " +
        "OR q.source LIKE :pattern ESCAPE '\\' OR c.name LIKE :pattern ESCAPE '\\' " +
        "OR q.tags LIKE :pattern ESCAPE '\\')"

@Dao
interface QuoteDao {

    @Query("$SELECT_QUOTES WHERE q.id = :id")
    fun observeById(id: Long): Flow<QuoteWithMeta?>

    @Query("$SELECT_QUOTES WHERE q.id = :id")
    suspend fun getById(id: Long): QuoteWithMeta?

    @Query("$SELECT_QUOTES WHERE q.id IN (:ids)")
    fun observeByIds(ids: List<Long>): Flow<List<QuoteWithMeta>>

    @Query(
        "$SELECT_QUOTES WHERE (:categoryId IS NULL OR q.category_id = :categoryId) " +
            "AND (:quoteType IS NULL OR q.quote_type = :quoteType) " +
            "AND (:origin IS NULL OR q.author = :origin OR q.source = :origin) " +
            "AND $MATCHES_PATTERN ORDER BY q.id",
    )
    fun search(pattern: String, categoryId: String?, quoteType: String?, origin: String?): Flow<List<QuoteWithMeta>>

    @Query(
        "$SELECT_QUOTES WHERE f.quote_id IS NOT NULL " +
            "AND (:categoryId IS NULL OR q.category_id = :categoryId) " +
            "AND $MATCHES_PATTERN ORDER BY f.created_at DESC",
    )
    fun observeFavorites(pattern: String, categoryId: String?): Flow<List<QuoteWithMeta>>

    @Query(
        "SELECT $QUOTE_COLUMNS FROM recent_views r JOIN quotes q ON q.id = r.quote_id $QUOTE_JOINS " +
            "ORDER BY r.viewed_at DESC LIMIT :limit",
    )
    fun observeRecentlyViewed(limit: Int): Flow<List<QuoteWithMeta>>

    @Query("$SELECT_QUOTES WHERE q.category_id = :categoryId AND q.id != :excludeId ORDER BY q.id LIMIT :limit")
    fun observeRelated(categoryId: String, excludeId: Long, limit: Int): Flow<List<QuoteWithMeta>>

    @Query(
        "SELECT author AS origin FROM quotes WHERE author IS NOT NULL " +
            "UNION SELECT source AS origin FROM quotes WHERE source IS NOT NULL ORDER BY origin",
    )
    fun observeOrigins(): Flow<List<String>>

    @Query("SELECT id FROM quotes")
    suspend fun getAllIds(): List<Long>

    @Query("SELECT EXISTS(SELECT 1 FROM quotes WHERE id = :id)")
    suspend fun exists(id: Long): Boolean

    @Query("SELECT id FROM quotes WHERE (:excludeId IS NULL OR id != :excludeId) ORDER BY RANDOM() LIMIT 1")
    suspend fun randomId(excludeId: Long?): Long?

    @Query("SELECT COUNT(*) FROM quotes")
    suspend fun count(): Int

    @Upsert
    suspend fun upsertAll(quotes: List<QuoteEntity>)

    /** 새 콘텐츠 버전에서 빠진 말씀 삭제(즐겨찾기·최근 기록도 FK CASCADE 로 정리됨) */
    @Query("DELETE FROM quotes WHERE content_version < :version")
    suspend fun deleteOlderThan(version: Int)
}
