# 마음등불 (Android)

부처님 말씀 · 불교 지혜 · 명상을 위한 한국어 중심 Android 앱입니다.
Kotlin · Jetpack Compose · Material 3 · MVVM으로 만들고, 오프라인 우선으로 동작합니다.

- 설계 문서: [`docs/DESIGN.md`](docs/DESIGN.md) (아키텍처, 화면, Navigation, Room, JSON, 개발 단계, 출시 체크리스트)
- 앱 이름 `마음등불`과 패키지 `com.maeumdeungbul.quotes`는 임시값입니다. Play에 처음 업로드하기 전에 확정하세요.

## 빌드

요구 사항: Android Studio (최신 안정판), JDK 17 이상, Android SDK 36

```bash
./gradlew assembleDebug        # 디버그 APK
./gradlew bundleRelease        # 릴리스 AAB (서명 설정은 Phase 9에서 추가)
```

## 진행 상황

- [x] Phase 1: Gradle(버전 카탈로그), Compose/Material 3 테마(라이트·다크), SplashScreen, 타입 안전 Navigation, Bottom Navigation 5탭
- [ ] Phase 2: 명언 데이터(JSON → Room), Repository
- [ ] Phase 3: 홈 · 말씀 · 검색 · 카테고리 · 상세 · 즐겨찾기 · 온보딩
- [ ] Phase 4: 텍스트/이미지 공유
- [ ] Phase 5: 명상 타이머 · 호흡 · 사운드 · 기록
- [ ] Phase 6: 매일 말씀 알림
- [ ] Phase 7: 불교 달력 (2차)
- [ ] Phase 8: 설정
- [ ] Phase 9: Google Play 출시 준비
