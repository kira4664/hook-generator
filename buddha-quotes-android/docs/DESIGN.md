# 마음등불 — 설계 문서

> 부처님 말씀 · 불교 지혜 · 명상 앱 (한국어 중심 Android)
>
> 레퍼런스 앱(`com.holova.buddha.quotes`)은 **기능 범위 참고용**으로만 사용한다.
> 디자인 · 소스 · 문구 · 이미지 · 음원 · 명언 DB는 일절 가져오지 않는다.
>
> 앱 이름(`마음등불`)과 패키지명(`com.maeumdeungbul.quotes`)은 임시값이다.
> Google Play에 처음 업로드하기 전에 확정한다. 업로드 후에는 applicationId를 바꿀 수 없다.

---

## 1. 전체 앱 아키텍처

```
┌──────────────────────────── UI Layer (Jetpack Compose + Material 3) ─────────────────────────┐
│  Screen (stateless Composable)  ◀── UiState(StateFlow) ──  ViewModel                         │
│        │  사용자 이벤트(onFavorite, onShare …) ──────────────▶ │                               │
│  collectAsStateWithLifecycle()                                   │                            │
└──────────────────────────────────────────────────────────────────┼────────────────────────────┘
                                                                   ▼
┌──────────────────────────── Domain Layer (순수 Kotlin) ─────────────────────────────────────┐
│  UseCase: GetTodayQuote, SearchQuotes, ToggleFavorite, SaveMeditationSession,                │
│           GetMeditationStats, ScheduleDailyQuote …                                            │
│  Model:   Quote, QuoteType, Category, MeditationSession, MeditationStats …                   │
└──────────────────────────────────────────────────────────────────┼────────────────────────────┘
                                                                   ▼
┌──────────────────────────── Data Layer ─────────────────────────────────────────────────────┐
│  Repository (interface는 domain, 구현은 data)                                                 │
│   ├─ QuoteRepository ─────── Room(QuoteDao, FavoriteDao, RecentDao) + AssetQuoteDataSource   │
│   ├─ MeditationRepository ── Room(MeditationSessionDao)                                       │
│   ├─ ReportRepository ────── Room(QuoteReportDao) + 이메일 Intent                             │
│   ├─ UserPreferencesRepository ── DataStore Preferences                                       │
│   └─ (2차) CalendarRepository, LearnRepository, SyncRepository                                │
│  DataSource: assets/data/*.json → kotlinx.serialization → 최초 실행 / 버전 업 시 Room seed     │
└──────────────────────────────────────────────────────────────────────────────────────────────┘

플랫폼 서비스
 ├─ WorkManager ─ DailyQuoteWorker (매일 알림), SeedDatabaseWorker(선택)
 ├─ Media3 ────── MeditationPlaybackService (MediaSessionService, 배경음 + 타이머)
 ├─ SplashScreen API, Notification(POST_NOTIFICATIONS 런타임 권한)
 └─ FileProvider ─ 이미지 카드 공유
```

원칙

- **단방향 데이터 흐름(UDF)**: ViewModel은 `StateFlow<UiState>` 하나를 노출하고, 화면은 이벤트만 올려보낸다.
- **오프라인 우선**: 모든 핵심 데이터의 단일 진실 공급원(SSOT)은 Room과 DataStore다. 네트워크는 2차 기능(동기화 · 백업)에서만 쓴다.
- **DI**: 초기에는 Hilt 없이 `Application`의 수동 `AppContainer`로 의존성을 주입한다. 의존성 수와 빌드 시간을 줄이기 위해서다. 모듈이 커지면 Hilt로 바꾼다. 인터페이스 기반이라 교체 비용이 작다.
- **단일 모듈 `:app`**: 패키지는 기능별로 나눈다. 필요해지면 `:core:data`, `:feature:*`로 분리할 수 있도록 패키지 간 의존 방향을 지킨다(ui → domain ← data).
- **스레딩**: DB와 파일 I/O는 Room suspend/Flow API와 `Dispatchers.IO`에서만 실행한다. Main thread에서는 금지한다.

---

## 2. 화면 목록

| # | 화면 | Route | 단계 | 비고 |
|---|------|-------|------|------|
| 1 | Splash | (SplashScreen API) | P1 | 별도 Activity 없음 |
| 2 | 온보딩 (3페이지) | `OnboardingRoute` | P3 | 최초 실행 시에만 |
| 3 | 홈 | `HomeRoute` | P1→P3 | Bottom Nav |
| 4 | 말씀 목록 / 검색 | `QuotesRoute(category?)` | P1→P3 | Bottom Nav |
| 5 | 말씀 상세 | `QuoteDetailRoute(id)` | P3 | Bottom Nav 숨김 |
| 6 | 공유 미리보기 (이미지 카드) | `ShareCardRoute(id)` (BottomSheet) | P4 | |
| 7 | 명상 홈 (시간·사운드 선택) | `MeditationRoute` | P1→P5 | Bottom Nav |
| 8 | 명상 진행 (타이머) | `MeditationSessionRoute(min, sound)` | P5 | 광고 금지, Bottom Nav 숨김 |
| 9 | 호흡 명상 | `BreathingRoute` | P5 | |
| 10 | 명상 완료 | `MeditationCompleteRoute(sessionId)` | P5 | 광고 후보 위치 |
| 11 | 명상 기록 (통계 + 달력) | `MeditationHistoryRoute` | P5 / 2차 | |
| 12 | 즐겨찾기 | `FavoriteRoute` | P1→P3 | Bottom Nav |
| 13 | 설정 | `SettingsRoute` | P1→P8 | Bottom Nav |
| 14 | 알림 설정 | `NotificationSettingsRoute` | P6 | |
| 15 | 오픈소스 라이선스 | `LicensesRoute` | P8 | |
| 16 | 불교 달력 | `CalendarRoute` | P7 (2차) | |
| 17 | 배움 / 불교 이야기 | `LearnRoute`, `LearnArticleRoute(id)` | 2차 | Route와 Data 구조만 미리 준비 |

**Bottom Navigation 5개**: 홈 · 말씀 · 명상 · 즐겨찾기 · 설정.
달력은 2차 기능이므로 1차에서는 탭에 넣지 않는다. 2차에 달력을 추가할 때 `설정`을 홈 상단 아이콘으로 옮기고 그 자리에 `달력`을 넣는다. 탭 구성은 `TopLevelDestination` enum 하나만 고치면 바뀐다.

---

## 3. 화면별 주요 기능

### 홈
- 오늘 날짜(양력, 2차에 음력 병기)
- **오늘의 말씀 카드**(크게): 본문, 출처/인물, 유형 배지(부처님 말씀 / 경전 기반 / 스승의 말씀 / 불교적 지혜 / 출처 확인 중), ♡ 저장, 공유
- `다른 말씀 보기`: 무작위 말씀을 보여준다. 오늘의 말씀 자체는 바꾸지 않는다. 카드 아래에 "오늘의 말씀으로 돌아가기"를 둔다.
- `명상 시작` CTA
- 추천 말씀(가로 스크롤 3~5개), 카테고리 칩, 최근 본 말씀
- **오늘의 말씀 고정 규칙**: DataStore에 `(dailyDate, dailyQuoteId)`를 저장한다. 날짜가 바뀌면 최근 N일(예: 30일) 동안 나온 말씀을 빼고 새로 고른다. 알림도 같은 값을 쓰므로 알림 내용과 홈 화면이 항상 일치한다.

### 말씀
- 검색창 `부처님 말씀을 검색하세요`. 본문 · 카테고리 · 인물 · 출처를 검색하고 300ms 디바운스한다.
- 카테고리 필터 칩, 유형(QuoteType)·인물/출처 필터(BottomSheet)
- 전체 목록(LazyColumn, `key = id`), 카드마다 ♡ / 공유
- 랜덤 말씀 FAB, 최근 본 말씀 섹션
- 빈 결과: "검색 결과가 없습니다. / 다른 단어로 검색해 보세요."

### 말씀 상세
- 배경 테마 위에 본문을 표시한다. 글자 크기는 작게 · 기본 · 크게 · 매우 크게(설정값, 화면에서 즉시 변경 가능)
- 출처 · 인물 · 유형 · 검증 상태 표시
- ♡ 즐겨찾기, 텍스트 공유, 이미지로 공유
- `출처가 잘못된 것 같아요` → 신고 BottomSheet(사유 선택 + 메모) → 로컬 기록 + 이메일 Intent
- 관련 말씀(같은 카테고리, 최대 5개)
- 진입하면 최근 본 말씀에 기록한다.

### 공유
- 텍스트: `ACTION_SEND text/plain`. 본문 + 출처 + 앱 이름. 링크는 Play 등록 후에 추가한다.
- 이미지 카드: Compose `rememberGraphicsLayer()` → `toImageBitmap()` → `cacheDir/shared/quote_{id}.png` → `FileProvider` URI → `ACTION_SEND image/png` + `FLAG_GRANT_READ_URI_PERMISSION`. 저장소 권한이 필요 없다. 정사각형(1080×1080)과 스토리용(1080×1920) 두 가지 비율을 제공하고, 배경 테마를 고를 수 있다.
- 카카오톡 · 문자 · Instagram · Facebook · Threads 모두 Android 기본 공유 시트(`Intent.createChooser`)로 처리한다. SNS SDK는 쓰지 않는다.

### 명상
- 시간 선택 1 · 3 · 5 · 10 · 15 · 20 · 30분 + 사용자 설정(1~120분)
- 배경음 선택: 빗소리 · 계곡 · 숲 · 새소리 · 명상 종 · 무음
- 진행 화면: "호흡에 집중하세요." / 남은 시간 / 일시정지 · 종료. 광고는 절대 노출하지 않는다.
- 타이머는 시작 시각을 `SystemClock.elapsedRealtime()` 기준으로 계산해 tick이 밀리지 않게 한다.
- **백그라운드 정책**: 진행 중에는 `MediaSessionService`(foregroundServiceType=`mediaPlayback`)가 타이머와 배경음을 함께 유지한다. 알림에 남은 시간과 일시정지 버튼을 보여준다. 무음일 때도 같은 서비스로 타이머를 유지한다. 오디오 포커스를 잃으면(전화 등) 자동으로 일시정지한다.
- 완료: "오늘 10분 명상을 완료했습니다." → Room에 기록. 종소리가 있으면 재생한다. 1분 미만 세션은 기록하지 않는다(설정 가능).

### 호흡 명상
- 들이마시기 4초 → 멈추기 2초 → 내쉬기 6초(사용자 조정 가능)
- `rememberInfiniteTransition` 대신 단계별 `Animatable`로 원의 scale을 0.6↔1.0으로 바꾼다. 단계 텍스트와 남은 초를 표시한다.
- 접근성: 단계가 바뀔 때 `liveRegion = Polite`로 TalkBack에 알린다. 시스템 "애니메이션 제거" 설정이 켜져 있으면 크기 변화를 최소화한다.

### 명상 기록
- 오늘 / 이번 주 / 이번 달 합계(분), 연속 수행 일수
- 달력 히트맵(수행한 날 표시). 1차에는 통계 카드만, 2차에 달력 UI를 넣는다.

### 즐겨찾기
- "내가 저장한 말씀". 카테고리 칩(전체 + 저장된 말씀에 있는 카테고리만 동적으로), 검색
- 빈 상태: "아직 저장한 말씀이 없습니다. / 마음에 드는 말씀의 ♡ 버튼을 눌러보세요."

### 설정
- 알림 설정(ON/OFF, 시간), 글자 크기, 다크모드(시스템 · 밝게 · 어둡게), 명언 배경, 명상 기본 사운드, 데이터 초기화(즐겨찾기 / 기록 / 전체, 확인 다이얼로그)
- 개인정보처리방침 · 이용약관: **URL이 설정되지 않으면 항목을 비활성화하고 "준비 중"으로 표시한다.** 임의 URL을 만들지 않는다.
- 오픈소스 라이선스, 앱 버전(`BuildConfig.VERSION_NAME`), 문의하기(설정된 이메일이 있을 때만 `ACTION_SENDTO mailto:`)

### 온보딩
- 3페이지 `HorizontalPager`: 하루 한 말씀 / 잠시 멈추는 시간 / 매일 이어지는 마음공부 → `시작하기`
- 3페이지에서 알림을 켤지 선택하게 하고, 켤 때만 권한을 요청한다. 건너뛸 수 있다.

---

## 4. Navigation 구조

Navigation Compose의 **타입 안전 Route**(`@Serializable`)를 사용한다.

```
MainActivity (single activity)
└─ MaeumAppRoot (Scaffold + NavigationBar)
   └─ NavHost(startDestination = HomeRoute | OnboardingRoute)
      ├─ OnboardingRoute ───────────────▶ HomeRoute (popUpTo Onboarding inclusive)
      │
      ├─ HomeRoute                    [tab]
      │   ├─▶ QuoteDetailRoute(id)
      │   ├─▶ QuotesRoute(category)
      │   └─▶ MeditationRoute (탭 전환)
      ├─ QuotesRoute(category: String? = null) [tab]
      │   └─▶ QuoteDetailRoute(id)
      ├─ QuoteDetailRoute(id: Long)
      │   ├─▶ QuoteDetailRoute(relatedId)
      │   └─▶ ShareCard (ModalBottomSheet, route 아님)
      ├─ MeditationRoute              [tab]
      │   ├─▶ MeditationSessionRoute(minutes, soundId)
      │   │     └─▶ MeditationCompleteRoute(sessionId) (popUpTo Session inclusive)
      │   ├─▶ BreathingRoute
      │   └─▶ MeditationHistoryRoute
      ├─ FavoriteRoute                [tab]
      │   └─▶ QuoteDetailRoute(id)
      ├─ SettingsRoute                [tab]
      │   ├─▶ NotificationSettingsRoute
      │   └─▶ LicensesRoute
      ├─ CalendarRoute                (2차)
      └─ LearnRoute → LearnArticleRoute(id)  (2차)
```

- 탭을 전환할 때는 `popUpTo(startDestination) { saveState = true }`, `launchSingleTop`, `restoreState`를 쓴다. 탭마다 스크롤 상태가 유지된다.
- Bottom Bar는 현재 destination이 탭 route일 때만 보인다(상세 · 명상 진행 화면에서는 숨긴다).
- 알림을 탭하면 `MainActivity`가 deep link로 열려 `QuoteDetailRoute(id)`로 이동한다(`navDeepLink<QuoteDetailRoute>`).

---

## 5. Room Entity 구조

DB 이름 `maeum.db`, `exportSchema = true`(스키마 JSON을 `app/schemas`에 커밋해 마이그레이션 테스트에 쓴다).

```kotlin
@Entity(tableName = "quotes", indices = [Index("category_id"), Index("quote_type")])
data class QuoteEntity(
    @PrimaryKey val id: Long,                  // JSON의 id. 안정 키라서 재시드해도 즐겨찾기가 유지된다
    val text: String,
    val author: String?,                       // 인물 (예: "부처님", "틱낫한")
    val source: String?,                       // 경전·자료명 (예: "법구경")
    @ColumnInfo(name = "source_detail") val sourceDetail: String?, // 장·게송 번호 등
    @ColumnInfo(name = "category_id") val categoryId: String,     // categories.json의 id
    @ColumnInfo(name = "quote_type") val quoteType: QuoteType,    // BUDDHA, SCRIPTURE, MASTER, WISDOM, UNKNOWN
    val verified: Boolean,                     // 출처 검증 완료 여부
    val translator: String?,                   // 번역자 (자체 번역이면 "편집부")
    @ColumnInfo(name = "license_note") val licenseNote: String?,  // 저작권 상태 메모
    val tags: String?,                         // 쉼표 구분 (검색 보조)
    @ColumnInfo(name = "content_version") val contentVersion: Int,
)

@Fts4(contentEntity = QuoteEntity::class)
@Entity(tableName = "quotes_fts")
data class QuoteFtsEntity(val text: String, val author: String?, val source: String?, val tags: String?)
// 한글은 FTS 토크나이저에서 형태소 분리가 되지 않는다. 그래서 1차는 LIKE '%q%' 검색
// (수천 건 규모는 충분히 빠름)을 쓰고, FTS는 데이터가 1만 건을 넘을 때 켠다.

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,                // "mind", "happiness" …
    val name: String,                          // "마음"
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    val description: String?,
)

@Entity(tableName = "favorites")              // quotes와 분리 → 콘텐츠를 재시드해도 사용자 데이터 보존
data class FavoriteEntity(
    @PrimaryKey @ColumnInfo(name = "quote_id") val quoteId: Long,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)

@Entity(tableName = "recent_views")
data class RecentViewEntity(
    @PrimaryKey @ColumnInfo(name = "quote_id") val quoteId: Long,
    @ColumnInfo(name = "viewed_at") val viewedAt: Long,   // 최근 50개만 유지
)

@Entity(tableName = "daily_quote_history")    // 오늘의 말씀 중복 방지
data class DailyQuoteHistoryEntity(
    @PrimaryKey val date: String,              // "2026-10-02" (ISO, 로컬 날짜)
    @ColumnInfo(name = "quote_id") val quoteId: Long,
)

@Entity(tableName = "meditation_sessions", indices = [Index("started_at")])
data class MeditationSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "started_at") val startedAt: Long,     // epoch millis
    @ColumnInfo(name = "planned_seconds") val plannedSeconds: Int,
    @ColumnInfo(name = "actual_seconds") val actualSeconds: Int,
    val type: MeditationType,                  // TIMER, BREATHING
    @ColumnInfo(name = "sound_id") val soundId: String?,
    val completed: Boolean,
)

@Entity(tableName = "quote_reports")
data class QuoteReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "quote_id") val quoteId: Long,
    val reason: ReportReason,                  // WRONG_SOURCE, WRONG_AUTHOR, TYPO, OTHER
    val memo: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    val status: ReportStatus,                  // PENDING, SENT  (향후 서버 전송 시 사용)
)

// ── 2차 (Entity와 DAO만 먼저 설계하고, 마이그레이션으로 추가) ──
@Entity(tableName = "buddhist_events")
data class BuddhistEventEntity(
    @PrimaryKey val id: String,
    val name: String,                          // "부처님오신날"
    @ColumnInfo(name = "lunar_month") val lunarMonth: Int?,
    @ColumnInfo(name = "lunar_day") val lunarDay: Int?,
    @ColumnInfo(name = "solar_date") val solarDate: String?,  // 검증된 연도별 양력 날짜만
    val source: String,                        // 근거 자료
)

@Entity(tableName = "learn_articles")
data class LearnArticleEntity(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "category_id") val categoryId: String, // 불교란?, 불교 용어 …
    val title: String,
    val body: String,                          // Markdown
    val source: String?,
)
```

TypeConverter: `QuoteType`, `MeditationType`, `ReportReason`, `ReportStatus` ↔ `String`. ordinal 대신 이름을 저장해서 enum 순서가 바뀌어도 안전하다.

**도메인 모델**

```kotlin
enum class QuoteType { BUDDHA, SCRIPTURE, MASTER, WISDOM, UNKNOWN }
// UI 라벨: 부처님 말씀 / 불교 경전 기반 / 불교 스승의 말씀 / 불교적 지혜 / 출처 확인 중

data class Quote(
    val id: Long, val text: String, val author: String?, val source: String?,
    val sourceDetail: String?, val category: Category, val quoteType: QuoteType,
    val verified: Boolean, val favorite: Boolean,   // favorites LEFT JOIN으로 계산
)
```

**표시 규칙**: `verified = false`이면 `quoteType`이 무엇이든 "출처 확인 중" 배지를 함께 붙인다. `BUDDHA` 유형은 경전 근거(`source`)가 있을 때만 쓸 수 있다. 시드를 검증하는 단위 테스트에서 이 규칙을 강제한다.

**DataStore (UserPreferences)**: `onboardingDone`, `themeMode`, `quoteFontScale`(SMALL/DEFAULT/LARGE/XLARGE), `quoteBackground`, `defaultSoundId`, `notificationEnabled`, `notificationHour/Minute`, `dailyDate`, `dailyQuoteId`, `seededContentVersion`.

---

## 6. JSON 데이터 구조 (`app/src/main/assets/data/`)

### quotes_ko.json
```json
{
  "contentVersion": 1,
  "quotes": [
    {
      "id": 1,
      "text": "말씀 본문",
      "author": "부처님",
      "source": "법구경",
      "sourceDetail": "제1장 쌍요품 1",
      "category": "mind",
      "quoteType": "SCRIPTURE",
      "verified": true,
      "translator": "편집부 자체 번역",
      "licenseNote": "팔리어 원전(퍼블릭 도메인) 기반 자체 번역",
      "tags": ["마음", "업"]
    }
  ]
}
```
배열을 객체로 감싸서 `contentVersion`을 둔다. 앱을 업데이트할 때 버전이 올라가 있으면 `quotes`와 `categories`만 upsert하고, 즐겨찾기와 기록은 보존한다.

### categories.json
```json
{
  "contentVersion": 1,
  "categories": [
    { "id": "mind", "name": "마음", "sortOrder": 1 },
    { "id": "happiness", "name": "행복", "sortOrder": 2 }
  ]
}
```
초기 17개: 마음 · 행복 · 인생 · 사랑 · 관계 · 욕심 · 분노 · 걱정 · 고통 · 용서 · 감사 · 지혜 · 수행 · 명상 · 자비 · 인연 · 삶과 죽음. 코드에는 하드코딩하지 않고 Room에서 읽는다.

### buddhist_events.json (2차)
```json
{
  "contentVersion": 1,
  "events": [
    {
      "id": "buddhas_birthday",
      "name": "부처님오신날",
      "calendar": "LUNAR",
      "lunarMonth": 4,
      "lunarDay": 8,
      "solarDates": { "2027": "YYYY-MM-DD" },
      "source": "검증 근거(예: 한국천문연구원 월력요항)"
    }
  ]
}
```
`solarDates`에는 공식 자료로 검증한 연도만 넣는다. 확인하지 못한 연도는 비워 두고, 앱은 "음력 4월 8일"만 표시한다.

### sounds.json (명상 음원 메타)
```json
{ "sounds": [ { "id": "rain", "name": "빗소리", "file": "rain.ogg", "license": "자체 녹음", "attribution": null } ] }
```
음원 파일(`res/raw/`)은 **직접 녹음했거나 라이선스가 명확한 것만** 넣는다. 파일이 없는 사운드는 목록에서 자동으로 숨긴다(무음은 항상 제공한다).

---

## 7. 패키지 구조

```
com.maeumdeungbul.quotes
 ├─ MaeumApplication.kt          // AppContainer 생성, WorkManager 초기화
 ├─ MainActivity.kt              // SplashScreen, edge-to-edge, setContent
 ├─ di/AppContainer.kt
 ├─ data/
 │   ├─ local/
 │   │   ├─ MaeumDatabase.kt, Converters.kt
 │   │   ├─ dao/ (QuoteDao, CategoryDao, FavoriteDao, RecentViewDao, DailyQuoteDao,
 │   │   │        MeditationSessionDao, QuoteReportDao)
 │   │   ├─ entity/ (*Entity.kt)
 │   │   └─ preferences/UserPreferencesDataSource.kt     // DataStore
 │   ├─ datasource/
 │   │   ├─ AssetContentDataSource.kt                    // JSON 읽기
 │   │   └─ ContentSeeder.kt                             // contentVersion 비교 후 upsert
 │   ├─ model/ (QuoteJson, CategoryJson, EventJson, SoundJson)  // @Serializable DTO
 │   └─ repository/ (OfflineQuoteRepository, OfflineMeditationRepository, …)
 ├─ domain/
 │   ├─ model/ (Quote, QuoteType, Category, MeditationSession, MeditationStats, FontScale, ThemeMode …)
 │   ├─ repository/ (QuoteRepository, MeditationRepository, PreferencesRepository 인터페이스)
 │   └─ usecase/ (GetTodayQuoteUseCase, ToggleFavoriteUseCase, GetMeditationStatsUseCase …)
 ├─ ui/
 │   ├─ MaeumAppRoot.kt          // Scaffold + Bottom Nav
 │   ├─ theme/ (Color, Theme, Type, Shape)
 │   ├─ components/ (StatusMessage, QuoteCard, QuoteTypeBadge, CategoryChips, …)
 │   ├─ onboarding/
 │   ├─ home/
 │   ├─ quotes/ (list, detail, share)
 │   ├─ meditation/ (timer, breathing, complete, history)
 │   ├─ favorite/
 │   ├─ calendar/                 // 2차
 │   ├─ learn/                    // 2차
 │   └─ settings/
 ├─ navigation/ (Routes.kt, TopLevelDestination.kt, MaeumNavHost.kt)
 ├─ notification/ (DailyQuoteWorker, DailyQuoteScheduler, NotificationChannels)
 ├─ playback/ (MeditationPlaybackService)
 ├─ share/ (QuoteShareHelper, QuoteImageRenderer)
 ├─ ads/ (AdsProvider 인터페이스, NoOpAdsProvider)  // SDK 연동 전까지 NoOp
 └─ util/ (DateProvider, Clock, Result …)
```

---

## 8. Gradle dependency 목록

버전은 `gradle/libs.versions.toml` 한 곳에서 관리한다. 단계마다 **실제로 쓰는 것만** 추가한다.

| 단계 | 라이브러리 | 용도 |
|---|---|---|
| P1 | AGP, Kotlin, Kotlin Compose compiler plugin, Kotlin serialization plugin | 빌드 |
| P1 | `androidx.activity:activity-compose` | `setContent`, `enableEdgeToEdge` |
| P1 | `androidx.compose:compose-bom` → ui, ui-graphics, ui-tooling(-preview), material3 | UI |
| P1 | `androidx.compose.material:material-icons-core` | 기본 아이콘(Home, Favorite, Settings, Search, Share …). extended는 용량이 커서 쓰지 않는다 |
| P1 | `androidx.navigation:navigation-compose` | 타입 안전 내비게이션 |
| P1 | `androidx.core:core-splashscreen` | SplashScreen API(하위 호환) |
| P2 | `androidx.room:room-runtime`, `room-ktx`, `room-compiler`(KSP), KSP plugin | 로컬 DB |
| P1 | `org.jetbrains.kotlinx:kotlinx-serialization-json` | Navigation route 직렬화, P2부터 assets JSON 파싱(Navigation도 같은 라이브러리를 씀 → Gson/Moshi 중복 설치하지 않음) |
| P2 | `androidx.datastore:datastore-preferences` | 설정 |
| P2 | `androidx.lifecycle:lifecycle-viewmodel-compose`, `lifecycle-runtime-compose` | ViewModel, `collectAsStateWithLifecycle` |
| P2 | `kotlinx-coroutines-android` (+ `kotlinx-coroutines-test`) | Coroutines/Flow |
| P5 | `androidx.media3:media3-exoplayer`, `media3-session` | 명상 배경음, 백그라운드 재생 |
| P6 | `androidx.work:work-runtime-ktx` | 매일 알림 |
| P8 | `com.mikepenz:aboutlibraries-compose-m3` (+ plugin) | 오픈소스 라이선스 화면 |
| 테스트 | junit4, `androidx.test.ext:junit`, `room-testing`, `compose ui-test-junit4`, `turbine` | 단위·UI 테스트 |
| 2차 | `com.google.android.gms:play-services-ads` | AdMob(광고 ID는 `local.properties`/CI secret → `BuildConfig`) |

쓰지 않는 것: Hilt(1차), Retrofit/OkHttp(1차는 네트워크 없음), Coil(배경은 로컬 리소스라 필요 없음. 원격 이미지를 도입할 때 Coil 3를 하나만 추가한다), Gson/Moshi, Accompanist(Pager와 권한 처리는 Foundation/Activity API로 대체).

---

## 9. 단계별 개발 계획

| Phase | 내용 | 완료 기준 |
|---|---|---|
| **1** | Gradle(버전 카탈로그), Compose, Material 3 테마(라이트·다크), SplashScreen, 타입 안전 Navigation, Bottom Navigation 5탭, 빈 화면 자리, 앱 아이콘 | `assembleDebug` 성공, 5탭 전환 시 상태 유지 |
| **2** | Quote/Category 도메인 모델, JSON DTO, Room(Entity·DAO·DB), ContentSeeder, Repository, AppContainer, DataStore, 시드 검증 단위 테스트, 소량의 **검증된 초기 말씀** | DB 시드 테스트 통과, 재시드해도 즐겨찾기 유지 |
| **3** | 홈(오늘의 말씀 고정), 말씀 목록 · 검색 · 카테고리 · 필터, 상세(글자 크기, 관련 말씀, 출처 신고), 즐겨찾기, 최근 본 말씀, 온보딩, 에러/빈 상태 | ViewModel 단위 테스트, 주요 화면 UI 테스트 |
| **4** | 텍스트 공유, 이미지 카드 렌더링 + FileProvider 공유, 배경 테마 | 실제 기기에서 카카오톡·인스타그램 공유 확인 |
| **5** | 명상 타이머, 호흡 애니메이션, Media3 배경음 + 포그라운드 서비스, 완료 기록, 통계 | 화면이 꺼지고 앱이 백그라운드로 가도 타이머가 정확함 |
| **6** | 매일 말씀 알림(WorkManager), 알림 채널, 권한 요청 흐름, 알림 → 상세 deep link | API 33+/<33 양쪽에서 권한 흐름 확인 |
| **7** (2차) | 달력(양력 + 검증된 음력 데이터), 불교 기념일 | 공식 자료와 대조 검증 |
| **8** | 설정 완성(다크모드, 글자, 배경, 사운드, 초기화, 라이선스, 문의, 버전) | 모든 설정이 앱 재시작 후에도 유지 |
| **9** | 릴리스 빌드, AAB, R8 규칙, 서명, 개인정보처리방침, Data Safety, 아이콘·스크린샷, 접근성 점검 | `bundleRelease` 성공, 내부 테스트 트랙 업로드 |

각 Phase가 끝나면 컴파일, 테스트, lint를 통과해야 다음 Phase로 넘어간다.

---

## 10. Google Play 출시 시 주의할 사항

1. **콘텐츠 저작권**: 현대 한국어 경전 번역문에는 번역 저작권이 있다. 원전(팔리어·한문 대장경 등, 퍼블릭 도메인) 기반 자체 번역, 저작권이 만료된 번역, 또는 명시적 허락을 받은 자료만 쓴다. `translator`와 `licenseNote`로 근거를 남긴다. 인터넷에 떠도는 "부처님 명언"은 오귀속이 많으므로 `WISDOM`/`UNKNOWN`, `verified=false`로 분류한다.
2. **오귀속 방지**: 부처님 말씀(`BUDDHA`)은 경전 근거가 있을 때만 쓴다. 스토어 설명에서도 "모든 말씀이 부처님의 직접 발언"인 것처럼 표현하지 않는다(메타데이터 정책).
3. **레퍼런스 앱과 혼동 금지**: 앱 이름, 아이콘, 스크린샷, 설명문이 레퍼런스 앱과 비슷하지 않아야 한다(사칭·스팸 정책).
4. **Target API**: Play가 요구하는 최신 targetSdk를 맞춘다(현재 프로젝트는 36). 새 앱 등록과 업데이트 기한을 Play Console에서 확인한다.
5. **신규 개인 개발자 계정**: 프로덕션 출시 전에 비공개 테스트(일정 인원·일정 기간) 요건이 있을 수 있다. Play Console의 현재 요건을 확인하고 일정에 반영한다.
6. **알림 권한(Android 13+)**: `POST_NOTIFICATIONS`는 사용자가 알림을 켤 때만 요청한다. 거부하면 설정 화면으로 안내한다. 정확한 알람(`SCHEDULE_EXACT_ALARM`)은 쓰지 않는다. WorkManager의 수 분 오차를 허용한다.
7. **포그라운드 서비스 타입**: 명상 배경음은 `mediaPlayback` 타입이다. targetSdk 34 이상에서는 Play Console에 포그라운드 서비스 사용 목적을 신고해야 한다. 데모 영상이 필요할 수 있다.
8. **Data Safety**: 1차(광고·분석 없음, 서버 없음)에서는 "수집 데이터 없음"이 가능하다. 출처 신고는 사용자가 직접 이메일을 보내는 방식이라 앱이 수집하지 않는다. AdMob을 도입하면 기기 식별자 · 광고 ID 수집을 반드시 신고하고, `AD_ID` 권한, UMP(동의 관리)를 추가하며, 아동 대상 여부를 설정한다.
9. **개인정보처리방침**: Play는 모든 앱에 방침 URL을 요구한다. 실제로 호스팅한 URL을 개발자가 직접 넣는다. 코드에서는 `local.properties`/Gradle property로 주입하고, 비어 있으면 앱 메뉴를 비활성화한다.
10. **광고 ID 분리**: debug 빌드는 Google이 공식 문서에 공개한 테스트 광고 단위만 쓰고, release 빌드는 CI secret에서 실제 ID를 주입한다. 명상 진행 화면에는 광고를 두지 않는다. 광고를 실수로 클릭하게 만드는 배치는 금지된다.
11. **서명**: Play App Signing을 사용한다. 업로드 키스토어와 비밀번호는 저장소에 커밋하지 않는다.
12. **R8/난독화**: 릴리스 빌드에서 `isMinifyEnabled`/`isShrinkResources`를 켠다. kotlinx.serialization DTO와 Navigation route 클래스의 keep 규칙을 검증한다(릴리스 빌드로 E2E를 확인한다).
13. **16KB 페이지 크기**: 네이티브 라이브러리가 없으면 영향이 없다. 광고 SDK 등을 추가한 뒤에는 APK Analyzer로 확인한다.
14. **접근성 · 품질**: TalkBack 라벨, 터치 영역 48dp, 명암 대비 4.5:1, 글꼴 200% 확대를 점검한다. Pre-launch report의 경고를 해결한다.
15. **건강 관련 표현**: 명상 효과를 의학적 치료처럼 표현하지 않는다(건강 앱 정책).
16. **스토어 등록정보**: 한국어를 기본 언어로 한다. 스크린샷은 실제 앱 화면이어야 한다. 아이콘은 512×512, 그래픽 이미지는 1024×500.
