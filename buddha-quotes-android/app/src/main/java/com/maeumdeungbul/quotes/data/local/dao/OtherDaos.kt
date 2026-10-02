package com.maeumdeungbul.quotes.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.maeumdeungbul.quotes.data.local.entity.CategoryEntity
import com.maeumdeungbul.quotes.data.local.entity.DailyQuoteEntity
import com.maeumdeungbul.quotes.data.local.entity.FavoriteEntity
import com.maeumdeungbul.quotes.data.local.entity.MeditationSessionEntity
import com.maeumdeungbul.quotes.data.local.entity.QuoteReportEntity
import com.maeumdeungbul.quotes.data.local.entity.RecentViewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY sort_order")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query(
        "SELECT DISTINCT c.* FROM categories c JOIN quotes q ON q.category_id = c.id " +
            "JOIN favorites f ON f.quote_id = q.id ORDER BY c.sort_order",
    )
    fun observeFavoriteCategories(): Flow<List<CategoryEntity>>

    @Upsert
    suspend fun upsertAll(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories WHERE content_version < :version")
    suspend fun deleteOlderThan(version: Int)
}

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE quote_id = :quoteId")
    suspend fun delete(quoteId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE quote_id = :quoteId)")
    suspend fun isFavorite(quoteId: Long): Boolean

    @Query("DELETE FROM favorites")
    suspend fun deleteAll()
}

@Dao
interface RecentViewDao {
    @Upsert
    suspend fun upsert(view: RecentViewEntity)

    @Query(
        "DELETE FROM recent_views WHERE quote_id NOT IN " +
            "(SELECT quote_id FROM recent_views ORDER BY viewed_at DESC LIMIT :keep)",
    )
    suspend fun trim(keep: Int)

    @Query("DELETE FROM recent_views")
    suspend fun deleteAll()
}

@Dao
interface DailyQuoteDao {
    @Query("SELECT quote_id FROM daily_quote_history WHERE date = :date")
    suspend fun getQuoteId(date: String): Long?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entry: DailyQuoteEntity)

    @Query("DELETE FROM daily_quote_history WHERE date = :date")
    suspend fun delete(date: String)

    @Query("SELECT quote_id FROM daily_quote_history WHERE date < :date ORDER BY date DESC LIMIT :limit")
    suspend fun recentQuoteIds(date: String, limit: Int): List<Long>
}

@Dao
interface MeditationSessionDao {
    @Insert
    suspend fun insert(session: MeditationSessionEntity): Long

    @Query("SELECT * FROM meditation_sessions ORDER BY started_at DESC")
    fun observeAll(): Flow<List<MeditationSessionEntity>>

    @Query("DELETE FROM meditation_sessions")
    suspend fun deleteAll()
}

@Dao
interface QuoteReportDao {
    @Insert
    suspend fun insert(report: QuoteReportEntity): Long

    @Query("DELETE FROM quote_reports")
    suspend fun deleteAll()
}
