#!/usr/bin/env python3
"""말씀 콘텐츠(assets/data) 검사기. ContentRules.kt 와 같은 규칙을 적용한다.

사용법: python3 tools/validate_content.py
"""
import json
import sys
from pathlib import Path

DATA = Path(__file__).resolve().parent.parent / "app/src/main/assets/data"
TYPES = {"BUDDHA", "SCRIPTURE", "MASTER", "WISDOM", "UNKNOWN"}


def main() -> int:
    manifest = json.loads((DATA / "manifest.json").read_text(encoding="utf-8"))
    categories = json.loads((DATA / "categories.json").read_text(encoding="utf-8"))["categories"]
    quotes = json.loads((DATA / "quotes_ko.json").read_text(encoding="utf-8"))["quotes"]
    errors = []

    if not isinstance(manifest.get("contentVersion"), int) or manifest["contentVersion"] < 1:
        errors.append("manifest.contentVersion 은 1 이상의 정수여야 합니다")

    cat_ids = [c["id"] for c in categories]
    if len(cat_ids) != len(set(cat_ids)):
        errors.append("카테고리 id 중복")
    cat_ids = set(cat_ids)

    seen = set()
    for q in quotes:
        qid = q.get("id")
        where = f"말씀 {qid}"
        if not isinstance(qid, int) or qid <= 0:
            errors.append(f"{where}: id 는 1 이상이어야 합니다")
        if qid in seen:
            errors.append(f"{where}: id 중복")
        seen.add(qid)
        if not q.get("text", "").strip():
            errors.append(f"{where}: 본문이 비어 있습니다")
        if q.get("category") not in cat_ids:
            errors.append(f"{where}: 알 수 없는 카테고리 {q.get('category')}")
        qtype = q.get("quoteType", "UNKNOWN")
        if qtype not in TYPES:
            errors.append(f"{where}: 알 수 없는 quoteType {qtype}")
        if qtype == "BUDDHA" and not (q.get("source") or "").strip():
            errors.append(f"{where}: BUDDHA 유형은 source(경전)가 필요합니다")
        if q.get("verified") and not (q.get("source") or q.get("author")):
            errors.append(f"{where}: 검수 완료 항목은 출처나 인물이 있어야 합니다")
        if not q.get("licenseNote"):
            errors.append(f"{where}: licenseNote(저작권 근거)가 필요합니다")

    for e in errors:
        print("ERROR", e)
    verified = sum(1 for q in quotes if q.get("verified"))
    print(f"말씀 {len(quotes)}개 · 카테고리 {len(cat_ids)}개 · 검수 완료 {verified}개 · 오류 {len(errors)}개")
    return 1 if errors else 0


if __name__ == "__main__":
    sys.exit(main())
