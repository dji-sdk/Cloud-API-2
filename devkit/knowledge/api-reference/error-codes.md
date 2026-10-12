---
title: "错误码四层分诊手册"
module: api-reference
tags: [error-code, troubleshooting, callback, FlightHubError, wayline]
audience: [agent, developer, operator]
complexity: intermediate
summary: "四层分诊错误码字典（errorCode → httpCode → serverCode → deviceCode）"
last_updated: 2026-09-18
sdk_version: "3.3.7"
related:
  - api-reference/core-rules.md
  - operations/deployment.md
  - faq/sdk-common-questions.md
---

SDK 所有回调遵循统一错误结构，采用四层分诊流程：errorCode → httpCode → serverCode → deviceCode。

# FlightHub SDK 错误码诊断参考手册

> **加载时机**：当用户遇到 SDK 返回错误、回调异常或 API 调用失败时，优先加载本文件。

---

## 使用方法

本手册采用**四层分诊**流程，按优先级逐层排查：

1. **获取错误对象**：从回调的 `FlightHubError error` 参数中读取各字段
2. **按优先级排查**：`errorCode` → `httpCode` → `serverCode` → `deviceCode`
3. **查对应表格**：在下方对应层级的表格中找到匹配的值
4. **执行修复方案**：按表格右列的指导步骤修复，再重试

---

## 错误响应结构

所有 SDK 回调均遵循统一的错误结构：

```java
FlightHubError {
    FlightHubSdkErrorCode errorCode;   // SDK 本地层（优先级最高）
    int httpCode;                       // HTTP 传输层
    int serverCode;                     // 服务端业务层
    int deviceCode;                     // 设备端
    String serverMessage;               // 服务端错误描述（辅助诊断）
}
```

**诊断优先级**：`errorCode` ＞ `httpCode` ＞ `serverCode` ＞ `deviceCode`

> **判断规则**：先判断 `errorCode` 是否为 `SUCCESS`；若是，再看 `httpCode` 是否为 `200`；若是，再看 `serverCode` 是否为 `0`；最后看 `deviceCode`。

---

## 第一层：SDK 本地错误码（FlightHubSdkErrorCode）

**当 `errorCode != SUCCESS` 时，优先处理此层，无需再看下层。**

| 枚举值 | 含义 | 常见触发场景 | 修复方案 |
|--------|------|-------------|---------|
| `SUCCESS` | 成功 | 正常返回 | 继续检查 `httpCode` |
| `ERROR_NOT_INITIALIZED` | SDK 未初始化 | 在 `initialize()` 完成之前调用了其他 API | 确保 `FlightHubSDK.initialize(config)` 已成功返回 `SUCCESS`，再调用其他方法 |
| `ERROR_INIT_FAILED` | 初始化失败 | ① `address` 不可达 ② `token` 无效 ③ 原生库加载失败 | ① `curl -I http://your-address` 验证网络 ② 重新获取有效 token ③ 检查原生库路径及版本兼容性 |
| `ERROR_ALREADY_INITIALIZED` | 重复初始化 | 多次调用 `initialize()` | 在调用前用 `isInitialized()` 前置判断，已初始化则跳过 |
| `ERROR_INVALID_PARAM` | 参数无效 | SN 为空/null、坐标超范围、枚举值非法 | 检查入参：① 字符串非 null/空 ② 经纬度在合法范围 ③ 枚举值在文档列表中 |
| `ERROR_OPERATION_FAILED` | 操作失败 | DRC 链路断开、设备离线、操作时序错误 | 查看日志定位具体原因；检查设备在线状态；确认操作前置条件（如已 acquire 飞控权限） |

---

## 第二层：HTTP 状态码（httpCode）

**当 `errorCode == SUCCESS` 时，检查此层。**

| httpCode | 含义 | 常见触发场景 | 修复方案 |
|----------|------|-------------|---------|
| `0` | 网络层失败（无响应） | 服务器不可达、DNS 解析失败、防火墙拦截 | 检查网络连通性；`ping` / `curl` 目标地址；检查防火墙规则 |
| `200` | 请求成功 | 正常 | 继续检查 `serverCode` |
| `401` | 认证失败 / Token 过期 | Token 已过期或格式错误 | 调用认证接口刷新 token，更新 SDK config 中的 token 字段后重新初始化或更新会话 |
| `403` | 权限不足 | 当前账号无目标资源的操作权限 | 检查组织/角色权限配置；确认账号已绑定目标设备或工作空间 |
| `404` | 接口不存在 | SDK 版本与服务端版本不匹配 | 确认 SDK 版本与服务端版本一致；升级 SDK 或联系服务端确认接口路径 |
| `500` | 服务端内部错误 | 服务端代码异常、数据库故障 | 记录 `serverMessage` 和请求时间戳，联系服务端技术支持 |
| `502` | 网关错误（Bad Gateway） | 上游服务短暂不可用 | 使用指数退避重试（建议 2s → 4s → 8s，最多 3 次） |
| `503` | 服务暂时不可用 | 服务端过载或滚动发布中 | 同 `502`，等待 30s 后重试；检查服务端维护公告 |

---

## 第三层：服务端业务码（serverCode）

**当 `errorCode == SUCCESS` 且 `httpCode == 200` 时，检查此层。**

| serverCode | 含义 | 常见触发场景 | 修复方案 |
|------------|------|-------------|---------|
| `0` | 业务成功 | 正常 | 继续检查 `deviceCode` |
| `100001` | 参数校验失败 | 请求体中某字段格式、范围或类型不符合服务端要求 | 阅读 `serverMessage` 定位具体字段；对照 SDK 接口文档修正入参 |
| `100002` | 资源不存在 | 设备 SN 错误/已注销、任务 ID 无效、工作空间不存在 | 确认设备 SN 拼写正确且已在平台注册；确认任务 ID 来自当前会话 |
| `100003` | 操作冲突 | 设备正在执行其他任务、资源锁定中 | 等待当前任务完成或显式取消（调用取消任务接口），再重试操作 |
| `100004` | 请求频率限制 | 短时间内调用同一接口次数过多 | 降低调用频率；在重试逻辑中加入随机抖动的退避间隔（建议 ≥ 1s） |

> **注意**：完整的 `serverCode` 列表取决于服务端版本。当遇到上表未列出的 `serverCode` 时，优先阅读 `serverMessage` 字段，其中包含服务端的详细错误描述。

---

## 第四层：设备端错误码（deviceCode）

**当 `errorCode == SUCCESS`、`httpCode == 200`、`serverCode == 0` 时，检查此层。**

| deviceCode | 含义 | 修复方案 |
|------------|------|---------|
| `0` | 无设备错误 | 操作正常完成 |
| 非 `0` | 设备端执行异常 | 阅读 `serverMessage` 获取设备侧的具体错误描述；常见原因包括：电池电量不足、GPS 信号弱、禁飞区限制、电机异常等 |

> 设备端错误码由具体的无人机/基站固件定义，含义随固件版本变化。`serverMessage` 是最可靠的诊断依据。

---

## 诊断流程图

```
                    收到 FlightHubError
                           │
                           ▼
              ┌─── errorCode == SUCCESS? ───┐
              │ 否                          │ 是
              ▼                             ▼
    查「第一层 SDK 错误码表」    ┌─── httpCode == 200? ───┐
    按枚举值定位并修复           │ 否                     │ 是
                                ▼                        ▼
                      查「第二层 HTTP 状态码表」  ┌─── serverCode == 0? ───┐
                      按状态码定位并修复          │ 否                     │ 是
                                                 ▼                        ▼
                                       查「第三层 服务端业务码表」  ┌─── deviceCode == 0? ───┐
                                       读 serverMessage 定位并修复  │ 否                     │ 是
                                                                    ▼                        ▼
                                                          读 serverMessage 获取        ✅ 操作成功
                                                          设备侧错误描述并修复
```

---

## 常见问题速查

| 现象 | 最可能原因 | 快速修复 |
|------|-----------|---------|
| 所有 API 返回 `null` | `initialize()` 未调用或失败 | 确认 `initialize()` 已 `await` 且返回 `SUCCESS`；检查 `errorCode == ERROR_NOT_INITIALIZED` 或 `ERROR_INIT_FAILED` |
| `getDroneInstance(sn)` 返回 `null` | SN 拼写错误、设备离线、SDK 未初始化 | ① 调用 `getDeviceList()` 打印真实 SN 对比（大小写敏感） ② 检查 `isOnline` 状态 ③ 确认 `initialize()` 已成功。→ 详见 `references/设备管理流程.md#常见问题` |
| `listen` 回调不触发 | `listenId < 0`（订阅失败）/ 设备离线 / 数据未变化 | ① 检查 `listenXxx()` 返回值是否 `≥ 0` ② 确认设备已上线推送遥测 ③ 确认未重复订阅导致旧回调被覆盖 |
| 飞控指令报错 `ERROR_OPERATION_FAILED` | 未调用 `acquireFlightControlAuthority()` | 在任何飞控指令前调用 `acquireFlightControlAuthority()`，失败路径中调用 `releaseFlightControlAuthority()` |
| 回调重复触发 | 多次调用 `listenXxx()` 未 cancel 旧的订阅 | 在重新订阅前，调用 `cancelListenXxx(oldListenId)` 取消上一个订阅 |
| 暂停/恢复/取消航线任务报"状态无法更新"，但任务状态字段看起来正常 | 设备已自主进入返航（RTH），但任务状态字段粒度不区分"执行中"与"返航中" | 调用前用 `fh_osd_snapshot` 查 `mode_code` 确认设备是否已进入返航，不要只看任务状态字段；详见 `faq/sdk-common-questions.md#q10` |
| 内存持续增长 | `listenId` 未在组件卸载时 cancel | 在销毁/卸载生命周期中遍历所有 `listenId` 并调用对应的 `cancelListenXxx(id)` |
| `initialize()` 返回 `ERROR_INIT_FAILED` | `address` 格式错误（缺 `http://`）/ 端口错误 / 网络不通 | ① 确认格式为 `http://host:port`（必须含协议头） ② `curl -I http://host:port/health` 验证连通性 ③ 检查 token 是否有效 |
