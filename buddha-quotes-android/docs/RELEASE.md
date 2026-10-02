# Google Play 출시 가이드 (Phase 9)

이 문서는 출시 직전에 **개발자가 직접 결정·입력해야 하는 항목**과 순서를 정리합니다.
코드에는 임의의 URL·이메일·광고 ID·서명 정보를 넣지 않았습니다.

## 1. 출시 전에 결정할 것

| 항목 | 현재 값 | 위치 |
|---|---|---|
| 앱 이름 | `마음등불` (임시) | `app/src/main/res/values/strings.xml` 의 `app_name` |
| 패키지명(applicationId) | `com.maeumdeungbul.quotes` (임시) | `app/build.gradle.kts`. **첫 업로드 후에는 변경 불가** |
| 버전 | `versionCode = 1`, `versionName = "1.0.0"` | `app/build.gradle.kts`. 업로드할 때마다 versionCode 를 1씩 올림 |
| 개인정보처리방침 URL | (비어 있음 → 앱에 "준비 중" 표시) | Gradle property `maeum.privacyPolicyUrl` |
| 이용약관 URL | (비어 있음) | Gradle property `maeum.termsUrl` |
| 문의 이메일 | (비어 있음 → 문의·출처 신고 메일 비활성) | Gradle property `maeum.contactEmail` |

Gradle property 는 저장소에 커밋하지 않는 `~/.gradle/gradle.properties` 에 적거나, CI 에서 `-P` 로 넘깁니다.

```properties
# ~/.gradle/gradle.properties (예시 형식 — 실제 값으로 바꾸세요)
maeum.privacyPolicyUrl=https://<직접 호스팅한 주소>
maeum.termsUrl=https://<직접 호스팅한 주소>
maeum.contactEmail=<문의 받을 이메일>
```

## 2. 콘텐츠 검수 (필수)

- `docs/CONTENT_REVIEW.md` 의 68개 말씀을 원전과 대조하고, 확인한 항목을 `"verified": true` 로 바꿉니다.
- 수정 후 `assets/data/manifest.json` 의 `contentVersion` 을 올리고 `python3 tools/validate_content.py` 를 실행합니다.
- 검수 전 항목도 앱에는 "출처 확인 중" 배지와 함께 표시되므로 출시는 가능하지만, 신뢰도를 위해 검수를 권장합니다.

## 3. 서명과 빌드

1. 업로드 키 생성(한 번만):
   ```bash
   keytool -genkeypair -v -keystore upload-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
   ```
2. 프로젝트 루트(`buddha-quotes-android/`)에 `keystore.properties` 를 만듭니다(`.gitignore` 에 포함되어 커밋되지 않음).
   ```properties
   storeFile=upload-keystore.jks
   storePassword=...
   keyAlias=upload
   keyPassword=...
   ```
3. 빌드: `./gradlew bundleRelease` → `app/build/outputs/bundle/release/app-release.aab`
4. Play Console 에서 **Play App Signing** 을 사용합니다. 업로드 키와 비밀번호는 안전한 곳에 따로 보관합니다.
5. 릴리스 빌드는 R8(난독화·축소)이 켜져 있습니다. 내부 테스트 트랙에서 실제 기기로 다음을 확인하세요.
   오늘의 말씀 · 검색 · 즐겨찾기 · 이미지 공유 · 명상 타이머(화면 꺼짐 포함) · 알림 · 설정 유지.

## 4. Play Console 입력 항목

### 앱 콘텐츠
- **개인정보처리방침**: 직접 호스팅한 URL(필수). 초안은 `docs/PRIVACY_POLICY_DRAFT.md`.
- **광고**: 현재 광고 SDK 없음 → "광고 없음". AdMob 을 도입하면 "광고 포함"으로 변경.
- **앱 액세스 권한**: 로그인 없음 → 모든 기능 제한 없이 이용 가능.
- **콘텐츠 등급**: 설문 작성(종교 관련 문구, 폭력·성적 콘텐츠 없음).
- **타겟층**: 성인 대상 권장(13세 이상 등). 아동 대상으로 지정하면 가족 정책이 추가로 적용됩니다.
- **포그라운드 서비스 신고**: `mediaPlayback` — "사용자가 시작한 명상 배경음을 화면이 꺼진 상태에서도 재생". 필요 시 시연 영상 제출.
- **건강 앱 여부**: 명상 기능이 있으나 의료 효과를 주장하지 않음. 설명문에 치료·진단 표현을 쓰지 마세요.

### 데이터 보안(Data Safety) — 현재 빌드 기준
| 질문 | 답변 근거 |
|---|---|
| 데이터 수집 | **수집하지 않음**. 즐겨찾기·명상 기록·설정은 기기 안(Room·DataStore)에만 저장되고 개발자 서버로 전송되지 않습니다. |
| 데이터 공유 | **공유하지 않음** |
| 출처 신고 | 사용자가 직접 자신의 메일 앱으로 보내는 경우에만 전달됨(앱이 자동 전송하지 않음) |
| 암호화 전송 | 해당 없음(네트워크 통신 없음) |
| 데이터 삭제 | 설정 → 데이터 초기화, 또는 앱 삭제 |
| Android 백업 | 시스템 백업에 DB·설정이 포함될 수 있음(사용자 Google 계정, 개발자 접근 불가) |

> AdMob·분석 SDK 를 추가하면 기기 식별자·광고 ID 수집을 반드시 신고하고, `AD_ID` 권한과 UMP 동의 화면을 추가해야 합니다.

### 스토어 등록정보
- 기본 언어: 한국어. 초안은 `docs/STORE_LISTING_DRAFT.md`.
- 앱 아이콘 512×512: `store/icon-512.png`
- 그래픽 이미지 1024×500: `store/feature-graphic-1024x500.png`
- 스크린샷: 휴대전화 2~8장(실제 앱 화면). 에뮬레이터·기기에서 홈, 말씀 상세, 이미지 공유, 명상 진행, 호흡 명상, 설정 화면을 캡처하세요.

## 5. 출시 트랙

1. 내부 테스트 → 2. 비공개 테스트 → 3. 프로덕션.
2023년 11월 이후 생성된 **개인 개발자 계정**은 프로덕션 신청 전에 비공개 테스트(일정 인원·일정 기간) 요건이 있습니다. Play Console 에 표시되는 현재 요건을 확인하세요.

## 6. 출시 후 업데이트 시

- 말씀 추가·수정: JSON 수정 → `contentVersion` 증가 → versionCode 증가 → 업로드. 사용자 즐겨찾기는 보존됩니다.
- DB 스키마 변경: `MaeumDatabase` version 증가 + Migration 작성(`app/schemas` 의 스키마 JSON 으로 테스트).
- Target API: 매년 Play 의 targetSdk 요구 사항을 확인합니다.
