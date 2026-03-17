# 숏폼 후킹 & 썸네일 생성기

유튜브 쇼츠, 인스타 릴스, 틱톡에서 실제로 먹히는 후킹 문구와 썸네일 제목을 AI로 생성합니다.

---

## 🚀 Vercel 무료 배포 방법 (5분 소요)

### 1단계: GitHub에 업로드

1. [GitHub](https://github.com) 로그인 (계정 없으면 무료 가입)
2. 우측 상단 **+** 버튼 → **New repository** 클릭
3. Repository name: `hook-generator` 입력
4. **Create repository** 클릭
5. 이 폴더의 파일들을 GitHub에 업로드
   - **Add file** → **Upload files** 클릭
   - 모든 파일/폴더 드래그앤드롭
   - **Commit changes** 클릭

### 2단계: Vercel 연결

1. [Vercel](https://vercel.com) 접속
2. **Sign Up** → **Continue with GitHub** 클릭
3. GitHub 계정 연동 허용
4. **Add New...** → **Project** 클릭
5. 방금 만든 `hook-generator` 저장소 선택
6. **Import** 클릭

### 3단계: API 키 설정 ⚠️ 중요!

1. Import 화면에서 **Environment Variables** 섹션 찾기
2. 아래 내용 입력:
   - **Name**: `ANTHROPIC_API_KEY`
   - **Value**: 본인의 Claude API 키 (https://console.anthropic.com 에서 발급)
3. **Add** 클릭

### 4단계: 배포!

1. **Deploy** 클릭
2. 1-2분 기다리면 배포 완료!
3. 생성된 URL (예: `hook-generator-xxx.vercel.app`)로 접속

---

## 🔑 API 키 발급 방법

1. [Anthropic Console](https://console.anthropic.com) 접속
2. 계정 생성 또는 로그인
3. **API Keys** 메뉴 클릭
4. **Create Key** 클릭
5. 생성된 키 복사 (sk-ant-... 형태)

**참고**: Anthropic API는 사용량 기반 과금입니다. 개인 사용 수준에서는 월 몇 달러 수준.

---

## 📁 파일 구조

```
hook-generator-app/
├── app/
│   ├── api/
│   │   └── generate/
│   │       └── route.js    # API 엔드포인트
│   ├── globals.css         # 스타일
│   ├── layout.js           # 레이아웃
│   └── page.js             # 메인 페이지
├── package.json
└── README.md
```

---

## ❓ 문제 해결

**Q: "API 키가 설정되지 않았습니다" 오류**
→ Vercel 대시보드 → Settings → Environment Variables에서 `ANTHROPIC_API_KEY` 확인

**Q: 배포 후 변경사항 반영**
→ GitHub에 파일 업데이트하면 자동으로 재배포됨

**Q: 도메인 변경하고 싶어요**
→ Vercel 대시보드 → Settings → Domains에서 커스텀 도메인 연결 가능

---

## 💡 기능

- ✅ 5가지 후킹 패턴 (FOMO, 해결책, 호기심, 권위, 공감)
- ✅ 대본 분석 후 맞춤 생성
- ✅ 썸네일 제목 20자 이내
- ✅ 클릭 한 번으로 복사
- ✅ 반응형 디자인 (모바일 지원)
