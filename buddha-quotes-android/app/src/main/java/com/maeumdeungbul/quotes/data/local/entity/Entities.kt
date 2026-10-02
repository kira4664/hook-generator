package com.maeumdeungbul.quotes.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.maeumdeungbul.quotes.domain.model.MeditationType
import com.maeumdeungbul.quotes.domain.model.QuoteType
import com.maeumdeungbul.quotes.domain.model.ReportReason

@Entity(
    tableName = "quotes",
    indices = [Index("category_id"), Index("content_version")],
)
data class QuoteEntity(
    /** JSON 의 id. 안정적인 키라서 콘텐츠를 다시 시드해도 즐겨찾기·기록이 유지된다. */
    @PrimaryKey val id: Long,
    val text: String,
    val author: String?,
    val source: String?,
    @ColumnInfo(name = "source_detail") val sourceDetail: String?,
    @ColumnInfo(name = "category_id") val categoryId: String,
    @ColumnInfo(name = "quote_type") val quoteType: QuoteType,
    val verified: Boolean,
    val translator: String?,
    @ColumnInfo(name = "license_note") val licenseNote: String?,
    /** 검색 보조용 태그(쉼표 구분) */
    val tags: String?,
    @ColumnInfo(name = "content_version") val contentVersion: Int,
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    @ColumnInfo(name = "content_version") val contentVersion: Int,
)

/** 사용자 데이터는 콘텐츠(quotes)와 분리해 둔다. 말씀이 콘텐츠에서 삭제되면 함께 삭제된다. */
@Entity(
    tableName = "favorites",
    foreignKeys = [
        ForeignKey(
            entity = QuoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["quote_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class FavoriteEntity(
    @PrimaryKey @ColumnInfo(name = "quote_id") val quoteId: Long,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)

@Entity(
    tableName = "recent_views",
    foreignKeys = [
        ForeignKey(
            entity = QuoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["quote_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("viewed_at")],
)
data class RecentViewEntity(
    @PrimaryKey @ColumnInfo(name = "quote_id") val quoteId: Long,
    @ColumnInfo(name = "viewed_at") val viewedAt: Long,
)

/** 날짜별 오늘의 말씀 이력. 하루 동안 같은 말씀을 유지하고, 최근에 나온 말씀이 반복되지 않게 한다. */
@Entity(tableName = "daily_quote_history")
data class DailyQuoteEntity(
    /** ISO 로컬 날짜(예: 2026-10-02) */
    @PrimaryKey val date: String,
    @ColumnInfo(name = "quote_id") val quoteId: Long,
)

@Entity(tableName = "meditation_sessions", indices = [Index("started_at")])
data class MeditationSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** epoch millis */
    @ColumnInfo(name = "started_at") val startedAt: Long,
    @ColumnInfo(name = "planned_seconds") val plannedSeconds: Int,
    @ColumnInfo(name = "actual_seconds") val actualSeconds: Int,
    val type: MeditationType,
    @ColumnInfo(name = "sound_id") val soundId: String?,
    val completed: Boolean,
)

/** 출처 신고. 초기 버전은 로컬 기록 + 이메일, 향후 서버 전송 시 status 를 사용한다. */
@Entity(tableName = "quote_reports")
data class QuoteReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "quote_id") val quoteId: Long,
    val reason: ReportReason,
    val memo: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    val status: String = STATUS_PENDING,
) {
    companion object {
        const val STATUS_PENDING = "PENDING"
    }
}

/** 말씀 + 카테고리 이름 + 즐겨찾기 여부 */
data class QuoteWithMeta(
    @Embedded val quote: QuoteEntity,
    @ColumnInfo(name = "category_name") val categoryName: String?,
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean,
)
