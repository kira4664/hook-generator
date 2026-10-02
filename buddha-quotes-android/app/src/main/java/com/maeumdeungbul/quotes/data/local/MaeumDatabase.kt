package com.maeumdeungbul.quotes.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.maeumdeungbul.quotes.data.local.dao.CategoryDao
import com.maeumdeungbul.quotes.data.local.dao.DailyQuoteDao
import com.maeumdeungbul.quotes.data.local.dao.FavoriteDao
import com.maeumdeungbul.quotes.data.local.dao.MeditationSessionDao
import com.maeumdeungbul.quotes.data.local.dao.QuoteDao
import com.maeumdeungbul.quotes.data.local.dao.QuoteReportDao
import com.maeumdeungbul.quotes.data.local.dao.RecentViewDao
import com.maeumdeungbul.quotes.data.local.entity.CategoryEntity
import com.maeumdeungbul.quotes.data.local.entity.DailyQuoteEntity
import com.maeumdeungbul.quotes.data.local.entity.FavoriteEntity
import com.maeumdeungbul.quotes.data.local.entity.MeditationSessionEntity
import com.maeumdeungbul.quotes.data.local.entity.QuoteEntity
import com.maeumdeungbul.quotes.data.local.entity.QuoteReportEntity
import com.maeumdeungbul.quotes.data.local.entity.RecentViewEntity

/**
 * 스키마를 바꿀 때는 version 을 올리고 Migration 을 추가한다(사용자 즐겨찾기·명상 기록 보존).
 * 스키마 JSON 은 app/schemas 에 생성되며 저장소에 커밋한다.
 */
@Database(
    entities = [
        QuoteEntity::class,
        CategoryEntity::class,
        FavoriteEntity::class,
        RecentViewEntity::class,
        DailyQuoteEntity::class,
        MeditationSessionEntity::class,
        QuoteReportEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class MaeumDatabase : RoomDatabase() {
    abstract fun quoteDao(): QuoteDao
    abstract fun categoryDao(): CategoryDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun recentViewDao(): RecentViewDao
    abstract fun dailyQuoteDao(): DailyQuoteDao
    abstract fun meditationSessionDao(): MeditationSessionDao
    abstract fun quoteReportDao(): QuoteReportDao

    companion object {
        private const val NAME = "maeum.db"

        fun build(context: Context): MaeumDatabase =
            Room.databaseBuilder(context, MaeumDatabase::class.java, NAME).build()
    }
}
