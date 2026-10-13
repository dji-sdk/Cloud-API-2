# EA340/EA610 私有化环境设备无法直播 — 排查清单

「设备无法直播」在 EA340D / EA340T / EA610 上反复出现，**根因几乎总是三类**：固件版本不达标、上云方式错误、DRTC/底座配置不一致。这类问题**不在你的应用代码里**——改 SDK 调用方式不会有任何效果。

适用于 RCA **Stage 2 Locate**：本清单用于把「直播打不开」快速收敛到具体配置项；命中某项后按对应修复动作处理，不要跳步（前一项不满足会让后面的排查方向失真）。

## 排查顺序

```
1. 固件版本达标？            → 不达标则后续全部无意义
2. 上云方式对？              → MQTT Broker 里有没有该 SN 的会话、username/clientid 前缀对不对
3. DRTC 底座 .env 对？       → EXTERNAL_DOMAIN / EXTERNAL_IP / HOST_IP
4. FlightHub2 底座 .env 对？ → 与 DRTC 的内外网 IP 是否成对一致
5. 端口开了吗？              → 从设备网络侧验证，不只看本地监听
6. 仍定位不到？              → 对号入座两个历史案例
```

**关键分流**：先确认 `212015` 出现在哪一侧 —— **仅飞机 SN 的指令**返回 `212015`，而机库/地面站指令正常 → 是**飞机侧网络接入**问题（第 6 节案例），不是直播服务故障。两侧都异常才回到第 3-5 节。

## 1. 固件版本（前提，不达标则后续全部无意义）

| 机型 | 最低固件版本 |
|------|-------------|
| EA610 | 01.02.2319 及以上 |
| EA340D | 00.06.0220 及以上 |
| EA340T | 00.06.0220 及以上 |

在 FlightHub2 设备管理页或司空 App 查看当前版本。低于要求先升级再继续排查。

## 2. 上云方式（MQTT 接入标识）

飞机须直连私有化 MQTT Broker，直播/DRC 链路才能建立：

- MQTT `username` 以 `{SN}&WZR2JJ...` 开头
- MQTT `clientid` 以 `WZR2JJ...` 开头

```bash
docker exec <mqtt-broker> <broker-cli> clients list | grep <设备SN>
```

| 查询结果 | 结论 |
|---|---|
| 无该 SN 的任何记录 | 设备**根本没发起 MQTT 连接** → 网络不可达，走第 6 节案例一，不要按上云方式处理 |
| 有记录但前缀不符 | 上云方式错误 → 按下述三步处理 |
| 有记录且前缀正确 | 上云方式正常 → 进第 3 节 |

**前缀不符的处理（顺序不可颠倒）**：

1. 升级设备端行服 APP（司空领航员/驾驶舱）到 **0.0.90 及以上**（依赖第 1 节固件已达标，固件与行服 APP 版本互相依赖）
2. 用**上云 API2** 方式重新扫码上云，让设备重新生成符合 `WZR2JJ` 规则的 username/clientid

顺序反了需要重复操作一遍。

## 3. DRTC 底座 `.env`

| 变量 | 含义 | 要求 |
|------|------|------|
| `EXTERNAL_DOMAIN` | 外部访问域名 | 实际对外可访问的域名；**用 IP 访问时此项也填外部 IP** |
| `EXTERNAL_IP` | 外部访问 IP | 实际对外可访问的公网/专网 IP |
| `HOST_IP` | 宿主机内网 IP | **必须是内网 IP**，DRTC 容器靠它与其他容器通信 |

**高频错误**：`EXTERNAL_IP`/`EXTERNAL_DOMAIN` 被配成客户内网 IP → 设备蜂窝网络无法路由，DRTC 媒体链路建立失败（案例一）。

**改过 IP 后必须删除重建容器，不能 `restart`**：

```bash
docker compose stop <drtc相关容器>
docker compose rm -f <drtc相关容器>
docker compose up -d <drtc相关容器>
```

`restart` 不会触发 DRTC **证书重新生成**，新域名/IP 不会写进证书 —— 这是本类问题里最容易被忽略的一步。

## 4. FlightHub2 底座 `.env` 与 DRTC 交叉核对

| 变量 | 要求 |
|------|------|
| `USE_SSL` | `true`（支持 EA340 系列必须开） |
| `OBJECT_STORAGE_USE_HTTPS` | `true`（支持 EA340 系列必须开） |
| `INTERNAL_IP` | 内网 IP，**须等于** DRTC 的 `HOST_IP` |
| `EXTERNAL_IP` | 外网 IP，**须等于** DRTC 的 `EXTERNAL_IP` |

把两份 `.env` 的对应变量并排比对，确认内外网 IP **成对一致**。典型不匹配：FlightHub2 用了外网 IP，DRTC 的 `HOST_IP` 却配了另一个内网 IP。

## 5. 端口开放（以宿主机监听端口为准）

| 宿主机端口 | 协议 | 容器/服务 | 用途 |
|-----------|------|----------|------|
| 8088 | TCP/HTTP | drtc-traefik | web entrypoint，仅 301/302 跳转 HTTPS |
| 8443 | TCP/HTTPS（TLS，可选 mTLS） | drtc-traefik | websecure entrypoint：`/v1(v2)/create\|find/room`、`create/token` → drtc-device-manager（强制设备 mTLS，CA 见 `PrivateRootCA.crt`）；`exchange/token` → openapi（不强制 mTLS）；`/mediaserver/arbitration`、`/probe` → drtc-worker-arbitrator；`/v1/sdk` → drtc-log-stack |
| 30830 | TCP/HTTP | drtc-manager | DRTC 视频/会话管理 API（直接发布端口，未经 traefik） |
| 50001-50003 | TCP | drtc-session-manager | 会话信令 / 媒体协商长连接 |
| 60002 | TCP | drtc-media-server-worker | 媒体 TCP 通道（`MS_TCP_PORT_MIN`/`MS_UDP_PORT_MIN`） |
| 60003 | TCP/WSS | drtc-media-server-worker | WSS / WebTransport 信令（`MS_WSS_PORT_MIN`/`MS_WT_PORT_MIN`） |
| 60004 | TCP | drtc-media-server-worker | MPTCP / 多路径 UDP（`MS_MPTCP_PORT_MIN`/`MS_MPUDP_PORT_MIN`） |
| 60006 | TCP/HTTP | drtc-media-server-worker | 管理接口（`HTTP_LISTEN_PORT`） |
| 10000-10100 | **UDP**/RTP | live-stream-hub | 直播流 RTP 转发（`STREAM_FORWARD_RTP_PORT_RANGE`） |
| 30854 | TCP/RTSP | live-stream-hub | 直播流 RTSP 转发（`STREAM_FORWARD_RTSP_PORT`） |

**验证要点**：

1. **TCP/UDP 别混** —— 50001-50003、60002-60004、60006 是 TCP；10000-10100 是 **UDP**，按 TCP 放行等于没放
2. 从**设备网络侧（或公网侧）**用 `telnet`/`nc` 验证可达。服务器本地 `netstat -tulnp` 只能证明服务在监听，**不能证明外部可达**
3. 云主机需**安全组 + 系统防火墙（iptables/firewalld）同时放行**，缺一个端口就不可达
4. `10000-10100` 范围较大，可按实际并发路数评估缩小，但不建议擅自缩小后不做验证

## 6. 两个历史案例（对号入座）

### 案例 A：飞机蜂窝直连会话为 0 → 212015

**现象**：机库指令正常、飞机 OSD 遥测正常，但**飞机 SN** 的直播/DRC 指令返回 `212015`（设备离线）；`<broker-cli> clients list | grep <飞机SN>` 无任何会话。

**定性**：飞机从未发起过 MQTT 连接 → **设备到云的网络问题**（机场网联机场景，飞机需经蜂窝网络直连 EMQX），**与平台服务本身无关**。

**根因**：环境对外地址（`EXTERNAL_IP`/域名）被误改成客户内网 IP → 飞机蜂窝公网无法路由到内网地址 → MQTT + DRTC 链路整体断开。对应第 3、4 节。

### 案例 B：TAS 判离线 → 212015

**现象**：地面站/拓扑接口显示设备在线，但 TAS 持续判该设备离线，直播/DRC 指令同样返回 `212015`，且日志中大量 `timestamp: 0`。

**⚠️ 先排掉干扰项**：私有化部署的 MQTT Broker 4.x 的 webhook 插件回调**本身不带 timestamp 字段**，日志出现 `timestamp: 0` 是**正常现象**，**不是根因**，不要被它带偏去查时间同步。

**真实根因方向**：

- 设备侧同一 `clientid` 存在**多个并发连接**（互踢 → 频繁 connect/disconnect）
- 设备状态服务重启导致上下线事件丢失 / 全量置离线

**排查动作**：查 MQTT Broker 日志确认是否有同一 SN 的多个并发连接（**不同源端口**），以及设备状态服务的重启/宕机记录。

## 通用经验规则

1. 私有化环境每次执行 `setuptools update` / 升级重启设备状态服务后，**所有机场/地面站会被置离线**，设备需重新上报拓扑才能恢复。交付/升级后必须逐台核对在线状态 —— 不要一看到「离线」就认定是直播链路故障。
2. 「机库/地面站指令正常 + 仅飞机 SN 相关指令失败（212015）」是特征明显的现象，优先怀疑**飞机侧网络接入**（蜂窝直连 MQTT Broker 会话），而不是直播服务的 bug。
3. 修改环境对外地址（域名 / `EXTERNAL_IP`）后，必须同时评估对 **DRTC 证书**和**飞机蜂窝网络可达性**的影响，不要只考虑 Web 端能否访问。

## 快速自查清单（可直接发给 ISV 客户）

- [ ] 固件达标（EA610 ≥ 01.02.2319；EA340D/EA340T ≥ 00.06.0220）
- [ ] `docker exec <mqtt-broker> <broker-cli> clients list | grep <SN>` 能查到连接，且 username 以 `{SN}&WZR2JJ`、clientid 以 `WZR2JJ` 开头
- [ ] 上云方式不对时：行服 APP 已升到 0.0.90+，并已用上云 API2 重新扫码
- [ ] DRTC `.env`：`EXTERNAL_DOMAIN`/`EXTERNAL_IP` 为外部地址，`HOST_IP` 为内网 IP
- [ ] 改过 IP 后 DRTC 容器已执行**删除重建**（非简单 restart）
- [ ] FlightHub2 `.env`：`USE_SSL=true`、`OBJECT_STORAGE_USE_HTTPS=true`
- [ ] FlightHub2 `INTERNAL_IP` 与 DRTC `HOST_IP` 一致；`EXTERNAL_IP` 双方一致
- [ ] 上表全部端口已开放，且**从设备网络侧**验证可达
- [ ] 以上均无误仍无法直播 → 收集 MQTT Broker 日志 + 设备状态服务日志 + DRTC 容器日志，走 `diag.sh report` 出脱敏包后按 Jira Bug 单流程提单

## 相关

- 错误码 `212015` / `2130xx` 见 `../error-codes.yaml`（唯一真值源）
- 部署与环境变量：`knowledge/operations/live-stream-troubleshooting.md`、`knowledge/operations/deployment.md`
- 平台 HTTP 响应解构：`platform-error-response.md`
