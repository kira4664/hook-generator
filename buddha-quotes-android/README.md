# 마음등불 (Android)

부처님 말씀 · 불교 지혜 · 명상을 위한 한국어 중심 Android 앱입니다.
Kotlin · Jetpack Compose · Material 3 · MVVM · Room · DataStore · WorkManager · Media3 로 만들었으며, 모든 핵심 기능이 오프라인에서 동작합니다.

> 앱 이름 `마음등불`과 패키지 `com.maeumdeungbul.quotes`는 임시값입니다. Play 첫 업로드 전에 확정하세요.

## 문서

| 문서 | 내용 |
|---|---|
| [docs/DESIGN.md](docs/DESIGN.md) | 아키텍처, 화면, Navigation, Room, JSON 형식, 패키지, 개발 단계 |
| [docs/CONTENT_REVIEW.md](docs/CONTENT_REVIEW.md) | 초기 말씀 68개 원전 대조 검수표 |
| [docs/RELEASE.md](docs/RELEASE.md) | Google Play 출시 절차, 서명, Data Safety, 입력해야 할 값 |
| [docs/PRIVACY_POLICY_DRAFT.md](docs/PRIVACY_POLICY_DRAFT.md) | 개인정보처리방침 초안 |
| [docs/STORE_LISTING_DRAFT.md](docs/STORE_LISTING_DRAFT.md) | 스토어 설명문 초안 |

## 빌드

요구 사항: Android Studio(최신 안정판), JDK 17, Android SDK 36

```bash
./gradlew testDebugUnitTest    # 단위 테스트
./gradlew assembleDebug        # 디버그 APK
./gradlew bundleRelease        # 릴리스 AAB (서명: docs/RELEASE.md)
python3 tools/validate_content.py   # 말씀 콘텐츠 검사
```

GitHub Actions(`.github/workflows/android.yml`)가 push 마다 테스트·빌드·lint 를 실행하고 디버그 APK 를 아티팩트로 올립니다.

## 기능 (1차 출시 범위)

- **오늘의 말씀**: 하루 동안 고정, 다른 말씀 보기, 추천·카테고리·최근 본 말씀
- **말씀**: 전체 목록, 검색(본문·인물·출처·카테고리·태그), 카테고리·유형·인물/출처 필터, 랜덤 말씀
- **말씀 상세**: 배경 테마, 글자 크기 4단계, 출처·유형·검수 상태, 관련 말씀, "출처가 잘못된 것 같아요" 신고
- **공유**: 텍스트 공유, 이미지 카드 공유(정사각형·스토리, 배경 8종) — Android 기본 공유 시트
- **즐겨찾기**: 로그인 없이 저장, 카테고리·검색
- **명상**: 1~30분·사용자 설정 타이머, 배경음(Media3, 백그라운드 재생), 시작·종료 종소리, 완료 기록
- **호흡 명상**: 4·2·6 / 4·4·4·4 / 4·7·8, 원형 애니메이션, 접근성(애니메이션 제거 설정·TalkBack) 대응
- **명상 기록**: 오늘·이번 주·이번 달·연속 수행, 달력
- **매일 말씀 알림**: WorkManager, 알림을 켤 때만 권한 요청, 알림 → 말씀 상세
- **설정**: 알림, 글자 크기, 다크모드, 명언 배경, 명상 사운드, 데이터 초기화, 정보(방침·약관·라이선스·문의·버전)
- **온보딩** 3페이지, SplashScreen API

## 콘텐츠·저작권

- 말씀: 퍼블릭 도메인 원전(팔리어·한문)을 바탕으로 새로 옮긴 글. 모두 `verified: false` 상태이며 검수가 필요합니다.
- 배경 그림: 코드로 직접 그림(`ui/components/QuoteBackgroundArt.kt`). 이미지 파일로 교체 가능.
- 종소리: 앱에서 직접 합성한 음원(`res/raw/bell_*.ogg`).
- 빗소리·계곡·숲·새소리: **음원 미포함**. 직접 녹음했거나 라이선스가 명확한 파일을 `res/raw/` 에 넣고 `playback/MeditationSound.kt` 에 연결하면 자동으로 표시됩니다.
- 레퍼런스 앱의 데이터·문구·이미지·음원·디자인은 사용하지 않았습니다.

## 2차 업데이트 예정

불교 달력(검증된 음력 데이터 필요), 배움/불교 이야기, 명상 음원 확대, 계정 동기화·클라우드 백업, AdMob(구조만 준비: `ads/Ads.kt`).
