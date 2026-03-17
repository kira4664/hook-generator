import './globals.css';

export const metadata = {
  title: '숏폼 후킹 & 썸네일 생성기',
  description: '유튜브 쇼츠, 인스타 릴스, 틱톡에서 실제로 먹히는 후킹 문구와 썸네일 제목을 AI로 생성합니다.',
};

export default function RootLayout({ children }) {
  return (
    <html lang="ko">
      <body>{children}</body>
    </html>
  );
}
