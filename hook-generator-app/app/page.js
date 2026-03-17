'use client';

import { useState } from 'react';

const sampleScript = `제 동생이 이걸 보더니 언니 생각난다면서 가져온 거에요. 제가 또 얼죽아거든요. 이게 얼음 트레이랑 물통이 하나로 된 2-in-1 컵이더라고요. 얼음을 얼린 다음 살짝 눌러주면 얼음이 쏙 떨어져요. 여기에 커피만 넣으면 바로 아아가 돼요. 집에서도 좋고 외출할 때나 운동 갈 때도 편하더라고요. 얼음 트레이랑 물통이 분리돼서 세척하기에도 정말 편했어요. 시원한 음료 좋아하시면 댓글에 '아이스' 남겨주세요.`;

export default function Home() {
  const [script, setScript] = useState('');
  const [style, setStyle] = useState('auto');
  const [hookCount, setHookCount] = useState(12);
  const [thumbCount, setThumbCount] = useState(10);
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const [copiedId, setCopiedId] = useState(null);

  const generate = async () => {
    if (!script.trim()) {
      setError('대본을 입력해주세요.');
      return;
    }

    setLoading(true);
    setError('');
    setResult(null);

    try {
      const response = await fetch('/api/generate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          script,
          style,
          hookCount: Math.max(5, Math.min(20, hookCount)),
          thumbCount: Math.max(5, Math.min(20, thumbCount))
        })
      });

      const data = await response.json();

      if (data.error) {
        throw new Error(data.error);
      }

      setResult(data);
    } catch (err) {
      setError(err.message || '생성 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleCopy = (text, id) => {
    navigator.clipboard.writeText(text);
    setCopiedId(id);
    setTimeout(() => setCopiedId(null), 1500);
  };

  const copyAll = (items, label) => {
    const text = items.map((item, i) => `${i + 1}. ${item}`).join('\n');
    navigator.clipboard.writeText(text);
    alert(`${label} 복사 완료!`);
  };

  return (
    <div className="container">
      <header className="header">
        <div className="badge">AI POWERED</div>
        <h1>숏폼 후킹 & 썸네일 생성기</h1>
        <p>유튜브 쇼츠 · 인스타 릴스 · 틱톡에서 실제로 먹히는 문구</p>
      </header>

      <div className="grid">
        {/* Input */}
        <div className="card">
          <div className="card-title">대본 입력</div>

          <textarea
            value={script}
            onChange={(e) => setScript(e.target.value)}
            placeholder={`영상에서 사용할 대본을 붙여넣으세요.

• 어떤 문제가 있었는지
• 어떻게 해결/변화했는지  
• 어떤 점이 좋았는지

이런 내용이 들어갈수록 결과가 좋아져요.`}
          />

          <div className="controls">
            <div className="control-group">
              <label>스타일</label>
              <select value={style} onChange={(e) => setStyle(e.target.value)}>
                <option value="auto">🎯 자동 추천</option>
                <option value="fomo">🔥 손실회피/FOMO형</option>
                <option value="solution">💡 즉각 해결책형</option>
                <option value="curiosity">❓ 호기심/반전형</option>
                <option value="authority">👑 권위/사회적증거형</option>
                <option value="empathy">💬 타겟 공감형</option>
              </select>
            </div>
            <div className="control-group">
              <label>후킹 개수</label>
              <input
                type="number"
                value={hookCount}
                onChange={(e) => setHookCount(Number(e.target.value) || 12)}
                min={5}
                max={20}
              />
            </div>
            <div className="control-group">
              <label>썸네일 개수</label>
              <input
                type="number"
                value={thumbCount}
                onChange={(e) => setThumbCount(Number(e.target.value) || 10)}
                min={5}
                max={20}
              />
            </div>
          </div>

          <div className="btn-group">
            <button className="btn btn-primary" onClick={generate} disabled={loading}>
              {loading ? (
                <>
                  <div className="spinner spinner-sm" />
                  생성 중...
                </>
              ) : (
                '✨ 생성하기'
              )}
            </button>
            <button className="btn btn-secondary" onClick={() => setScript(sampleScript)}>
              샘플
            </button>
            <button
              className="btn btn-secondary"
              onClick={() => {
                setScript('');
                setResult(null);
                setError('');
              }}
            >
              초기화
            </button>
          </div>

          {error && <div className="error-box">{error}</div>}

          <div className="tip-box">
            💡 <strong>팁:</strong> "전엔 이랬는데 지금은~", "이거 쓰고 나서~", "몰랐다가 알게 된~" 같은 변화 스토리가 있으면 훨씬 좋은 결과가 나와요.
          </div>
        </div>

        {/* Result */}
        <div className="card">
          <div className="card-title">생성 결과</div>

          {!result ? (
            <div className="result-empty">
              {loading ? (
                <>
                  <div className="spinner" />
                  <p>대본 분석하고 생성 중...</p>
                </>
              ) : (
                <p>대본 입력 후 생성 버튼을 눌러주세요</p>
              )}
            </div>
          ) : (
            <div className="fade-in">
              {/* Analysis */}
              {result.analysis && (
                <div className="analysis-box">
                  <div className="analysis-row">
                    <span className="analysis-label">🎯 타깃</span>
                    <span className="analysis-value">{result.analysis.target || '-'}</span>
                  </div>
                  <div className="analysis-row">
                    <span className="analysis-label">🔥 후킹포인트</span>
                    <span className="analysis-value">{result.analysis.hook_point || '-'}</span>
                  </div>
                  <div className="analysis-row">
                    <span className="analysis-label">✨ 매력포인트</span>
                    <span className="analysis-value">{result.analysis.appeal_point || '-'}</span>
                  </div>
                </div>
              )}

              {/* Hooks */}
              {result.hooks && result.hooks.length > 0 && (
                <div className="section">
                  <div className="section-header">
                    <span className="section-title">🎬 후킹 문구</span>
                    <button
                      className="btn btn-secondary btn-sm"
                      onClick={() => copyAll(result.hooks, '후킹 문구')}
                    >
                      전체 복사
                    </button>
                  </div>
                  <div className="result-list">
                    {result.hooks.map((hook, i) => (
                      <div
                        key={i}
                        className={`result-item ${copiedId === `hook-${i}` ? 'copied' : ''}`}
                        onClick={() => handleCopy(hook, `hook-${i}`)}
                      >
                        <span className="result-num">{String(i + 1).padStart(2, '0')}</span>
                        <span className="result-text">{hook}</span>
                        <span className="result-copy">
                          {copiedId === `hook-${i}` ? '복사됨!' : '클릭해서 복사'}
                        </span>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Thumbnails */}
              {result.thumbnails && result.thumbnails.length > 0 && (
                <div className="section">
                  <div className="section-header">
                    <span className="section-title">🖼️ 썸네일 제목</span>
                    <button
                      className="btn btn-secondary btn-sm"
                      onClick={() => copyAll(result.thumbnails, '썸네일 제목')}
                    >
                      전체 복사
                    </button>
                  </div>
                  <div className="thumb-grid">
                    {result.thumbnails.map((thumb, i) => (
                      <div
                        key={i}
                        className={`thumb-item ${copiedId === `thumb-${i}` ? 'copied' : ''}`}
                        onClick={() => handleCopy(thumb, `thumb-${i}`)}
                      >
                        {thumb}
                      </div>
                    ))}
                  </div>
                </div>
              )}

              <button className="btn regenerate-btn" onClick={generate} disabled={loading}>
                🔄 다른 버전 다시 생성
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
