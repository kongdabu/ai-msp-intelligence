#!/usr/bin/env python3
"""프로덕션 API에서 지난주 기사·인사이트 수집 → _workspace/weekly_{date}/ 저장"""

import sys
import json
import datetime
import requests
from pathlib import Path

PROJECT_ROOT  = Path(__file__).parent.parent
ENV_FILE      = PROJECT_ROOT / ".env"
PROD_API_BASE = "https://aimsp-backend.onrender.com"


def get_last_week_range():
    today        = datetime.date.today()
    last_monday  = today - datetime.timedelta(days=today.weekday() + 7)
    last_sunday  = last_monday + datetime.timedelta(days=6)
    return last_monday, last_sunday


def fetch_articles(last_monday, last_sunday):
    date_from = f"{last_monday}T00:00:00"
    date_to   = f"{last_sunday}T23:59:59"
    articles, page = [], 0

    while True:
        resp = requests.get(f"{PROD_API_BASE}/api/articles",
                            params={"dateFrom": date_from, "dateTo": date_to,
                                    "page": page, "size": 100}, timeout=30)
        if not resp.ok:
            print(f"  기사 API {resp.status_code}", file=sys.stderr)
            break
        data    = resp.json()
        content = data.get("content", [])
        for a in content:
            articles.append({
                "id":          a.get("id"),
                "title":       a.get("title", ""),
                "summary":     (a.get("summary") or "")[:200],
                "competitor":  a.get("competitor", ""),
                "category":    a.get("category", ""),
                "sourceType":  a.get("sourceType", ""),
                "sourceName":  a.get("sourceName", ""),
                "publishedAt": a.get("publishedAt", ""),
                "relevanceScore": a.get("relevanceScore"),
            })
        if page + 1 >= data.get("totalPages", 1) or not content:
            break
        page += 1

    # 지난주 기사 없으면 최근 100건 폴백
    if not articles:
        print("  지난주 기사 없음 — 최근 100건 폴백", file=sys.stderr)
        resp = requests.get(f"{PROD_API_BASE}/api/articles",
                            params={"page": 0, "size": 100}, timeout=30)
        if resp.ok:
            for a in resp.json().get("content", []):
                articles.append({
                    "id":          a.get("id"),
                    "title":       a.get("title", ""),
                    "summary":     (a.get("summary") or "")[:200],
                    "competitor":  a.get("competitor", ""),
                    "category":    a.get("category", ""),
                    "sourceType":  a.get("sourceType", ""),
                    "sourceName":  a.get("sourceName", ""),
                    "publishedAt": a.get("publishedAt", ""),
                    "relevanceScore": a.get("relevanceScore"),
                })
    return articles


def fetch_insights(last_monday, last_sunday):
    from_dt  = datetime.datetime.combine(last_monday, datetime.time.min)
    to_dt    = datetime.datetime.combine(last_sunday,  datetime.time.max)
    insights, page = [], 0

    while True:
        resp = requests.get(f"{PROD_API_BASE}/api/insights",
                            params={"page": page, "size": 100}, timeout=30)
        if not resp.ok:
            break
        data    = resp.json()
        content = data.get("content", [])
        oldest  = None

        for i in content:
            gen_str = i.get("generatedAt", "")
            gen_dt  = None
            if gen_str:
                try:
                    gen_dt = datetime.datetime.fromisoformat(
                        gen_str.replace("Z", "+00:00")).replace(tzinfo=None)
                    if oldest is None or gen_dt < oldest:
                        oldest = gen_dt
                except Exception:
                    pass

            if gen_dt is None or (from_dt <= gen_dt <= to_dt):
                insights.append({
                    "title":       i.get("title", ""),
                    "content":     i.get("content", ""),
                    "insightType": i.get("insightType", ""),
                    "competitor":  i.get("competitor", ""),
                    "impactScore": i.get("impactScore", 0),
                    "actionItems": i.get("actionItems", []),
                    "generatedAt": gen_str,
                })

        if oldest and oldest < from_dt:
            break
        if page + 1 >= data.get("totalPages", 1) or not content:
            break
        page += 1

    # 지난주 인사이트 없으면 최근 50건 폴백
    if not insights:
        print("  지난주 인사이트 없음 — 최근 50건 폴백", file=sys.stderr)
        resp = requests.get(f"{PROD_API_BASE}/api/insights",
                            params={"page": 0, "size": 50}, timeout=30)
        if resp.ok:
            for i in resp.json().get("content", []):
                insights.append({
                    "title":       i.get("title", ""),
                    "content":     i.get("content", ""),
                    "insightType": i.get("insightType", ""),
                    "competitor":  i.get("competitor", ""),
                    "impactScore": i.get("impactScore", 0),
                    "actionItems": i.get("actionItems", []),
                    "generatedAt": i.get("generatedAt", ""),
                })
    return insights


def main():
    last_monday, last_sunday = get_last_week_range()
    print(f"📅 대상 기간: {last_monday} ~ {last_sunday}")

    workspace = PROJECT_ROOT / "_workspace" / f"weekly_{last_monday}"
    workspace.mkdir(parents=True, exist_ok=True)

    print("\n📰 기사 수집 중...")
    articles = fetch_articles(last_monday, last_sunday)
    print(f"   → {len(articles)}건")

    print("💡 인사이트 수집 중...")
    insights = fetch_insights(last_monday, last_sunday)
    print(f"   → {len(insights)}건")

    meta = {
        "weekStart":    str(last_monday),
        "weekEnd":      str(last_sunday),
        "articleCount": len(articles),
        "insightCount": len(insights),
        "fetchedAt":    datetime.datetime.now().isoformat(),
    }

    (workspace / "articles.json").write_text(
        json.dumps(articles, ensure_ascii=False, indent=2), encoding="utf-8")
    (workspace / "insights.json").write_text(
        json.dumps(insights, ensure_ascii=False, indent=2), encoding="utf-8")
    (workspace / "meta.json").write_text(
        json.dumps(meta, ensure_ascii=False, indent=2), encoding="utf-8")

    print(f"\n✅ 데이터 저장 완료: {workspace.absolute()}")
    print(f"   articles.json ({len(articles)}건) | insights.json ({len(insights)}건)")

    # 오케스트레이터가 경로를 읽을 수 있도록 stdout에 출력
    print(f"WORKSPACE_PATH={workspace.absolute()}")


if __name__ == "__main__":
    main()
