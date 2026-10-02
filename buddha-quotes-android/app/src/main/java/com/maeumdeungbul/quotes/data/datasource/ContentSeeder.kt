package com.maeumdeungbul.quotes.data.datasource

import android.util.Log
import androidx.room.withTransaction
import com.maeumdeungbul.quotes.data.local.MaeumDatabase
import com.maeumdeungbul.quotes.data.local.entity.CategoryEntity
import com.maeumdeungbul.quotes.data.local.entity.QuoteEntity
import com.maeumdeungbul.quotes.domain.repository.PreferencesRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 번들된 JSON 콘텐츠를 Room 으로 옮긴다.
 * 최초 실행이나 앱 업데이트로 contentVersion 이 올라갔을 때만 실행되며,
 * 즐겨찾기·최근 기록·명상 기록 같은 사용자 데이터는 건드리지 않는다.
 */
class ContentSeeder(
    private val database: MaeumDatabase,
    private val dataSource: AssetContentDataSource,
    private val preferences: PreferencesRepository,
) {
    private val mutex = Mutex()

    @Volatile
    private var ready = false

    suspend fun ensureSeeded() {
        if (ready) return
        mutex.withLock {
            if (ready) return
            val bundledVersion = dataSource.loadManifest().contentVersion
            val seededVersion = preferences.getSeededContentVersion()
            if (bundledVersion > seededVersion || database.quoteDao().count() == 0) {
                seed(bundledVersion)
            }
            ready = true
        }
    }

    private suspend fun seed(version: Int) {
        val categories = dataSource.loadCategories().categories
        val categoryIds = categories.map { it.id }.toSet()
        val quotes = dataSource.loadQuotes().quotes
            .distinctBy { it.id }
            .filter { quote ->
                val problems = ContentRules.violations(quote, categoryIds)
                    // 유형 문제는 시드 시 UNKNOWN 으로 낮춰서 살린다.
                    .filterNot { it.contains("quoteType") || it.contains("BUDDHA") }
                if (problems.isNotEmpty()) Log.w(TAG, "말씀 ${quote.id} 제외: $problems")
                problems.isEmpty()
            }

        database.withTransaction {
            database.categoryDao().upsertAll(
                categories.map { CategoryEntity(it.id, it.name, it.sortOrder, version) },
            )
            database.quoteDao().upsertAll(
                quotes.map { quote ->
                    QuoteEntity(
                        id = quote.id,
                        text = quote.text.trim(),
                        author = quote.author?.takeIf { it.isNotBlank() },
                        source = quote.source?.takeIf { it.isNotBlank() },
                        sourceDetail = quote.sourceDetail?.takeIf { it.isNotBlank() },
                        categoryId = quote.category,
                        quoteType = ContentRules.effectiveType(quote),
                        verified = quote.verified,
                        translator = quote.translator,
                        licenseNote = quote.licenseNote,
                        tags = quote.tags.joinToString(",").ifBlank { null },
                        contentVersion = version,
                    )
                },
            )
            database.quoteDao().deleteOlderThan(version)
            database.categoryDao().deleteOlderThan(version)
        }
        preferences.setSeededContentVersion(version)
    }

    private companion object {
        const val TAG = "ContentSeeder"
    }
}
