#!/usr/bin/env python3
"""脱敏引擎 — 替代 diag.sh 中 sanitize_stdin() 的 sed 管道和 sanitize_with_stats() 的 awk 计数。

用法:
  cat file.txt | python3 sanitize.py           # 仅脱敏
  cat file.txt | python3 sanitize.py --stats   # 脱敏 + stderr 输出命中统计

stdin  → 原始文本
stdout → 脱敏后文本
stderr → (仅 --stats) 命中统计行
"""
import re
import sys

# --- 脱敏规则：顺序对应 sed -E 管道，逐条应用 ---

# 规则 1: 凭证 key=value（case-insensitive）
_RE_CRED = re.compile(
    r"""(password|passwd|token|secret|api[_-]?key|access[_-]?key)"""
    r"""["']?\s*[:=]\s*["']?[^"' ,}]+""",
    re.IGNORECASE,
)

# 规则 2-4: 内网 IP
_RE_IP10 = re.compile(r"(^|[^0-9.])(10\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3})")
_RE_IP192 = re.compile(r"(^|[^0-9.])(192\.168\.[0-9]{1,3}\.[0-9]{1,3})")
_RE_IP172 = re.compile(r"(^|[^0-9.])(172\.(1[6-9]|2[0-9]|3[01])\.[0-9]{1,3}\.[0-9]{1,3})")

# 规则 5: 邮箱
_RE_EMAIL = re.compile(r"[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}")

# 规则 6: 中国手机号
_RE_PHONE = re.compile(r"(1[3-9][0-9])\s*[- ]?\s*([0-9]{4})\s*[- ]?\s*([0-9]{4})")

# 规则 7: URL query 参数值
_RE_URL_QUERY = re.compile(r"([?&][A-Za-z0-9_]+)=[^&\s]*")

# 规则 8: 用户目录路径
_RE_USER_PATH = re.compile(r"(/Users/[A-Za-z0-9._-]+|/home/[A-Za-z0-9._-]+)")

# 规则 9: IPv6
_RE_IPV6 = re.compile(r"([0-9a-fA-F]{1,4}:){3,}[0-9a-fA-F]{1,4}")

# 规则 10: MAC 地址
_RE_MAC = re.compile(r"([0-9a-fA-F]{2}[:-]){5}[0-9a-fA-F]{2}")

# 规则 11: IMEI（15 位纯数字）
_RE_IMEI = re.compile(r"(^|[^0-9])[0-9]{15}($|[^0-9])")

# 规则 12: JWT
_RE_JWT = re.compile(r"eyJ[A-Za-z0-9_-]+\.eyJ[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+")

# 规则 13-14: PEM 起始行（替换）
_RE_PEM_KEY_BEGIN = re.compile(r"-----BEGIN.*PRIVATE KEY-----")
_RE_PEM_CERT_BEGIN = re.compile(r"-----BEGIN CERTIFICATE-----")

# 规则 15-16: PEM 结束行（删除整行）
_RE_PEM_KEY_END = re.compile(r"-----END.*PRIVATE KEY-----")
_RE_PEM_CERT_END = re.compile(r"-----END CERTIFICATE-----")

# 规则 17: GPS 经纬度（保留小数点后 2 位）
_RE_GPS = re.compile(r"([+-]?[0-9]{1,3}\.[0-9]{2})[0-9]+")

# 规则 18: SN key=value（case-insensitive）
_RE_SN_KV = re.compile(
    r"""([A-Za-z_]*sn|serial[A-Za-z_]*)\s*[:=]\s*["']?([A-Za-z0-9]{10,20})""",
    re.IGNORECASE,
)

# 规则 19: SN JSON 格式（case-insensitive）
_RE_SN_JSON = re.compile(
    r""""([A-Za-z_]*sn|serial[A-Za-z_]*)"\s*:\s*"[A-Za-z0-9]{10,20}" """.strip(),
    re.IGNORECASE,
)


def sanitize_line(line: str) -> str:
    """对单行文本按顺序应用全部脱敏规则，返回脱敏后的行。"""
    # 规则 1: 凭证
    line = _RE_CRED.sub(lambda m: m.group(1) + '="***"', line)
    # 规则 2-4: 内网 IP
    line = _RE_IP10.sub(r"\1***.***.***.***", line)
    line = _RE_IP192.sub(r"\g<1>192.168.***.***", line)
    line = _RE_IP172.sub(r"\g<1>172.***.***" + "." + "***", line)
    # 规则 5: 邮箱
    line = _RE_EMAIL.sub("***@***", line)
    # 规则 6: 中国手机号
    line = _RE_PHONE.sub(r"\1-****-****", line)
    # 规则 7: URL query
    line = _RE_URL_QUERY.sub(r"\1=***", line)
    # 规则 8: 用户目录
    line = _RE_USER_PATH.sub("/***", line)
    # 规则 9: IPv6
    line = _RE_IPV6.sub("***:***:***:***", line)
    # 规则 10: MAC
    line = _RE_MAC.sub("**:**:**:**:**:**", line)
    # 规则 11: IMEI
    line = _RE_IMEI.sub(r"\g<1>***IMEI***\g<2>", line)
    # 规则 12: JWT
    line = _RE_JWT.sub("***JWT***", line)
    # 规则 13-14: PEM 起始
    line = _RE_PEM_KEY_BEGIN.sub("***PRIVATE_KEY_REMOVED***", line)
    line = _RE_PEM_CERT_BEGIN.sub("***CERTIFICATE_REMOVED***", line)
    # 规则 17: GPS
    line = _RE_GPS.sub(r"\g<1>***", line)
    # 规则 18-19: SN
    line = _RE_SN_KV.sub(r"\1=***SN***", line)
    line = _RE_SN_JSON.sub(r'"\1":"***SN***"', line)
    return line


def should_delete_line(line: str) -> bool:
    """判断是否应删除整行（PEM 结束行 + 空白行）。"""
    if _RE_PEM_KEY_END.search(line):
        return True
    if _RE_PEM_CERT_END.search(line):
        return True
    if not line.strip():
        return True
    return False


# --- 统计计数：对应 sanitize_with_stats() 中的 awk 块 ---

_STAT_CRED = re.compile(r"(password|passwd|token|secret|api[_-]?key|access[_-]?key)", re.IGNORECASE)
_STAT_IPV4 = re.compile(r"(^|[^0-9.])(10\.[0-9]|192\.168\.|172\.(1[6-9]|2[0-9]|3[01])\.)")
_STAT_EMAIL = re.compile(r"[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z][A-Za-z]")
_STAT_PHONE = re.compile(r"1[3-9][0-9][ -]?[0-9]{4}[ -]?[0-9]{4}")
_STAT_URL_QUERY = re.compile(r"[?&][A-Za-z0-9_]+=[^& ]")
_STAT_USER_PATH = re.compile(r"/Users/|/home/")
_STAT_IPV6 = re.compile(r"[0-9a-fA-F]+:[0-9a-fA-F]+:[0-9a-fA-F]+:[0-9a-fA-F]")
_STAT_MAC = re.compile(r"[0-9a-fA-F]{2}[:-][0-9a-fA-F]{2}[:-]")
_STAT_JWT = re.compile(r"eyJ[A-Za-z0-9_-]+\.eyJ")
_STAT_GPS = re.compile(r"[0-9]?[0-9]?[0-9]\.[0-9]{3}")
_STAT_SN = re.compile(r"[a-z_]*sn\s*[:=]|serial[a-z_]*\s*[:=]", re.IGNORECASE)


def main():
    stats_mode = "--stats" in sys.argv

    counters = {
        "credential": 0, "ipv4": 0, "ipv6": 0, "mac": 0,
        "email": 0, "phone_cn": 0, "url_query": 0, "user_path": 0,
        "jwt": 0, "gps": 0, "sn": 0,
    }

    stat_patterns = [
        ("credential", _STAT_CRED),
        ("ipv4", _STAT_IPV4),
        ("email", _STAT_EMAIL),
        ("phone_cn", _STAT_PHONE),
        ("url_query", _STAT_URL_QUERY),
        ("user_path", _STAT_USER_PATH),
        ("ipv6", _STAT_IPV6),
        ("mac", _STAT_MAC),
        ("jwt", _STAT_JWT),
        ("gps", _STAT_GPS),
        ("sn", _STAT_SN),
    ]

    for raw_line in sys.stdin:
        line = raw_line.rstrip("\n")

        if stats_mode:
            for key, pat in stat_patterns:
                if pat.search(line):
                    counters[key] += 1

        if should_delete_line(line):
            continue

        print(sanitize_line(line))

    if stats_mode:
        stats = ", ".join(f"{k}={v}" for k, v in counters.items())
        print(f"命中统计: {stats}", file=sys.stderr)


if __name__ == "__main__":
    main()
