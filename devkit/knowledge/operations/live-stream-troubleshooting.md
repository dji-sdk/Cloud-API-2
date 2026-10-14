---
title: "EA340/EA610 设备无法直播排查"
module: operations
tags: [troubleshooting, live-stream, drtc, mqtt-broker, private-deployment]
audience: [developer, operator]
complexity: intermediate
summary: "EA340D/EA340T/EA610 私有化环境无法直播的五步自查：固件版本、上云方式、DRTC 与底座 .env、端口开放"
last_updated: 2026-09-16
sdk_version: "3.3.7"
source: "内部直播排查 Skill（EA340/EA610 现场支持经验沉淀）"
related:
  - operations/deployment.md
  - api-reference/error-codes.md
---

私有化环境中 EA340 系列「无法直播」绝大多数出在固件版本、上云方式、DRTC/底座配置三类，按下列顺序逐项检查，不要跳步——前一项不满足会让后面的排查方向失真（例如上云方式错误时，DRTC 配置再对也无法直播）。

## 排查顺序

```
固件版本 → 上云方式 → DRTC 底座 .env → DJI CloudAPI2 底座 .env → DRTC 端口开放 → 日志提单
```

## 1. 固件版本（后续所有排查的前提）

| 机型 | 最低固件版本 |
|------|-------------|
| EA610 | 01.02.2319 及以上 |
| EA340D | 00.06.0220 及以上 |
| EA340T | 00.06.0220 及以上 |

在 DJI CloudAPI2 设备管理页或司空 App 查看当前版本；低于要求先升级，否则配置全对也无法直播。

## 2. 上云方式（MQTT 接入标识）

设备须以正确方式直连私有化 MQTT Broker，直播/DRC 链路才能建立：

- MQTT `username` 必须以 `{SN}&WZR2JJ...` 开头
- MQTT `clientid` 必须以 `WZR2JJ...` 开头

```bash
docker exec <mqtt-broker> <broker-cli> clients list | grep <设备SN>
```

查不到该 SN 的连接记录 → 设备根本没发起 MQTT 连接，属**网络不可达**（见第 6 节案例一），不是上云方式问题，先排除网络。

**前缀不符时**（顺序不可颠倒，否则要重复操作）：

1. 升级设备端行服 APP（司空领航员/驾驶舱）到 **0.0.90 及以上**（依赖第 1 步固件已达标）
2. 用**上云 API2** 方式重新扫码上云，让设备重新生成符合 `WZR2JJ` 规则的 username/clientid

## 3. DRTC 底座 `.env`

| 变量 | 含义 | 配置要求 |
|------|------|---------|
| `EXTERNAL_DOMAIN` | 外部访问域名 | 实际对外可访问的域名；用 IP 访问时此项也填**外部 IP** |
| `EXTERNAL_IP` | 外部访问 IP | 实际对外可访问的公网/专网 IP |
| `HOST_IP` | 宿主机内网 IP | **必须是内网 IP**（DRTC 容器靠它与其他容器通信） |

**常见错误**：`EXTERNAL_IP`/`EXTERNAL_DOMAIN` 误配成内网 IP → 设备蜂窝网络无法路由，DRTC 媒体链路建立失败（案例一）。

**改过 IP 后必须删除重建，不能 `restart`**（`restart` 不触发证书重新生成，是最易漏的一步）：

```bash
docker compose stop <drtc相关容器>
docker compose rm -f <drtc相关容器>
docker compose up -d <drtc相关容器>
```

## 4. DJI CloudAPI2 底座 `.env`

| 变量 | 配置要求 |
|------|---------|
| `USE_SSL` | `true`（支持 EA340 系列时必须开） |
| `OBJECT_STORAGE_USE_HTTPS` | `true`（支持 EA340 系列时必须开） |
| `INTERNAL_IP` | 内网 IP，须与 DRTC `HOST_IP` 一致 |
| `EXTERNAL_IP` | 外网 IP，须与 DRTC `EXTERNAL_IP` 一致 |

**交叉核对**：把两份 `.env` 对应变量并排比对，确认内外网 IP 成对一致，不要出现「DJI CloudAPI2 用外网 IP、DRTC `HOST_IP` 却配了另一个内网 IP」。

## 5. DRTC 端口开放

以**宿主机监听端口**为准，需在防火墙/安全组放行；云主机还需安全组与系统防火墙（iptables/firewalld）**同时**放行。

| 宿主机端口 | 协议 | 容器/服务 | 用途 |
|-----------|------|----------|------|
| 8088 | TCP/HTTP | drtc-traefik | web entrypoint，仅 301/302 跳转到 HTTPS |
| 8443 | TCP/HTTPS（TLS，可选 mTLS） | drtc-traefik | websecure entrypoint：`/v1(v2)/create\|find/room`、`create/token` → drtc-device-manager（强制设备 mTLS）；`exchange/token` → openapi（不强制）；`/mediaserver/arbitration`、`/probe` → drtc-worker-arbitrator；`/v1/sdk` → drtc-log-stack |
| 30830 | TCP/HTTP | drtc-manager | DRTC 视频/会话管理 API |
| 50001-50003 | TCP | drtc-session-manager | 会话信令/媒体协商长连接 |
| 60002 | TCP | drtc-media-server-worker | 媒体 TCP 通道（`MS_TCP_PORT_MIN`/`MS_UDP_PORT_MIN`） |
| 60003 | TCP/WSS | drtc-media-server-worker | WSS / WebTransport 信令（`MS_WSS_PORT_MIN`/`MS_WT_PORT_MIN`） |
| 60004 | TCP | drtc-media-server-worker | MPTCP / 多路径 UDP（`MS_MPTCP_PORT_MIN`/`MS_MPUDP_PORT_MIN`） |
| 60006 | TCP/HTTP | drtc-media-server-worker | 管理接口（`HTTP_LISTEN_PORT`） |
| 10000-10100 | **UDP**/RTP | live-stream-hub | 直播流 RTP 转发范围（`STREAM_FORWARD_RTP_PORT_RANGE`） |
| 30854 | TCP/RTSP | live-stream-hub | 直播流 RTSP 转发（`STREAM_FORWARD_RTSP_PORT`） |

**检查方法**：

1. 注意 TCP/UDP 不要混淆——50001-50003、60002-60004、60006 是 TCP，10000-10100 是 **UDP**
2. 从设备网络侧（或公网侧）用 `telnet`/`nc` 验证可达性；`netstat -tulnp` 只能证明服务在监听，不能证明外部可达
3. `10000-10100` 范围较大，可按实际并发路数评估缩小，但不建议擅自缩小后不做验证

## 6. 历史案例与经验规则

**案例一：飞机蜂窝直连会话为 0。** 机库指令正常、飞机 OSD 遥测正常，但飞机 SN 的直播/DRC 指令返回 `212015`（设备离线）；MQTT Broker 中查不到飞机 SN 的会话，说明飞机从未发起 MQTT 连接——这是**设备到云的网络问题**（机场网联机场景，飞机需经蜂窝网络直连 MQTT Broker），与平台服务无关。根因：环境对外地址（`EXTERNAL_IP`/域名）被误改成客户内网 IP，飞机蜂窝公网无法路由到内网地址，MQTT + DRTC 链路整体断开。

**案例二：设备状态服务判离线导致 212015。** 地面站/拓扑接口显示在线，但设备状态服务持续判离线，日志中大量 `timestamp: 0`。注意：私有化部署的 MQTT Broker 4.x 的 webhook 插件回调**本身不带 timestamp 字段**，`timestamp: 0` 是**正常现象**，不是根因，不要被它误导。真实根因方向：同一 clientid 存在多个并发连接（互踢导致频繁 connect/disconnect），或设备状态服务重启导致上下线事件丢失。排查动作：查 MQTT Broker 日志确认同一 SN 是否有多个并发连接（不同源端口），以及设备状态服务的重启/宕机记录。

**通用经验规则**：

1. 私有化环境每次 `setuptools update`/升级重启设备状态服务后，**所有机场/地面站会被置离线**，需重新上报拓扑才能恢复；交付/升级后必须逐台核对在线状态，不要一看到「离线」就认定是直播链路故障
2. 「机库/地面站指令正常 + 仅飞机 SN 指令失败（212015）」是特征明显的现象，优先怀疑飞机侧网络接入（蜂窝直连 MQTT Broker 会话），而非直播服务本身的 bug
3. 修改环境对外地址（域名/`EXTERNAL_IP`）后，必须同时评估对 DRTC 证书、飞机蜂窝可达性的影响，不要只考虑 Web 端能否访问

## 快速自查清单

- [ ] 固件达标（EA610 ≥ 01.02.2319，EA340D/EA340T ≥ 00.06.0220）
- [ ] `<broker-cli> clients list | grep <SN>` 能查到连接，且 username 以 `{SN}&WZR2JJ`、clientid 以 `WZR2JJ` 开头
- [ ] 上云方式不对时：行服 APP 已升到 0.0.90+，并已用上云 API2 重新扫码
- [ ] DRTC `.env`：`EXTERNAL_DOMAIN`/`EXTERNAL_IP` 为外部地址，`HOST_IP` 为内网 IP
- [ ] 改过 IP 后 DRTC 容器已**删除重建**（非 restart）
- [ ] DJI CloudAPI2 `.env`：`USE_SSL=true`、`OBJECT_STORAGE_USE_HTTPS=true`
- [ ] 两侧 `INTERNAL_IP`/`EXTERNAL_IP` 成对一致
- [ ] 全部端口已放行，且从设备网络侧验证可达
- [ ] 仍无法定位 → 收集 MQTT Broker / 设备状态服务 / DRTC 容器日志，用 `diag.sh report` 出脱敏包后提单
