#!/usr/bin/env python3
"""Loki API JSON 响应解析器 — 替代 diag.sh 中 3 处 inline python3 -c 块。

用法: curl ... | python3 loki.py
stdin  → Loki /loki/api/v1/query_range JSON 响应
stdout → [HH:MM:SS][container_name] log_line
stderr → 错误信息
"""
import json
import sys
from datetime import datetime, timezone


def main():
    try:
        d = json.load(sys.stdin)
    except json.JSONDecodeError:
        print("Loki returned non-JSON response", file=sys.stderr)
        sys.exit(1)

    if d.get("status") != "success":
        msg = d.get("message", d.get("error", "unknown"))
        print(f"Loki error: {msg}", file=sys.stderr)
        sys.exit(1)

    for s in d.get("data", {}).get("result", []):
        cn = s.get("stream", {}).get("container_name", "?")
        for ts, line in s.get("values", []):
            dt = datetime.fromtimestamp(int(ts) / 1e9, tz=timezone.utc)
            print(f"[{dt.strftime('%H:%M:%S')}][{cn}] {line.rstrip()}")


if __name__ == "__main__":
    main()
