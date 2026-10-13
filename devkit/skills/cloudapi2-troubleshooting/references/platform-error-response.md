# 平台 HTTP 响应错误码解构

调用 FlightHub 平台 HTTP API 时，返回体里的 `code` **不一定是真正的业务错误码**。本文教你把响应拆开，拿到能定位问题的那个码。

适用于 RCA **Stage 2 Locate** —— 当用户提供的是 JSON 错误响应（而不是 SDK 错误码或前端 4001-7000 码）时先走这里。

## 第一步：判断响应形态

| 响应形态 | 示例 | 含义 |
|---|---|---|
| **A. 直出业务码** | `{"code": 213020, "message": "some business error", "data": null}` | `code` 就是业务错误码，直接查域表定位 |
| **B. 通用框架码** | `{"code": 200612, "message": "RPC Failure", "data": null}` | `code` 是平台通用框架码（200XXX 段），`message` 来自固定映射表，**不是动态错误信息** |
| **C. 双错误码（最容易误判）** | `{"code": 200612, "message": "Bad Request. err_code: 213020"}` | 外层是框架包装码，**内层 `err_code` 才是真正的业务码** |
| **D. 嵌套 result** | `{"result": {"code": 213020, "message": "Bad Request"}, "code": 213020, "data": null}` | 旧版接口形态，`code` 在顶层和 `result` 内重复出现 |

补充特征：

- ServiceError 场景下 **HTTP status 仍然是 200** —— 不要因为 HTTP 200 就认为调用成功，必须看 body 里的 `code`
- `message` 出现 `"Bad Request"` 往往是**兜底文案**（业务码不在框架映射表中时的 fallback），本身不携带信息，别拿它当线索

## 第二步：双错误码解构（形态 C）

这是最高频的误判来源。看到 `message` 里含 `". err_code: "` 就说明是嵌套错误。

```
message 解构：
"Bad Request. err_code: 213020"
 ↑ 兜底文案       ↑ 真正的业务错误码
```

- **外层 `code` (200612)** = `CodeRpcFailure`，说明「平台内部一次服务间调用失败了」，只用于理解调用链路径
- **内层 `err_code` (213020)** = 实际业务错误码 → `device_not_open`（设备未推流）

**分析顺序：先看内层 `err_code` 定位业务问题，再看外层 `code` 理解链路。**

只盯着 `200612` 排查，只会得到「RPC 失败」这种无用结论。

## 第三步：按域前缀定位归属

平台业务错误码遵循 `2XXYYYY` 格式，`XX` 标识服务域：

| 码段 | 服务域 |
|---|---|
| 200XXX | 通用管理框架（不是业务域，见下表） |
| 210XXX | 管理服务 ManageService |
| 211XXX | 地图服务 MapService |
| 212XXX | 设备接入服务 ThingAccessService |
| 213XXX | 直播服务 LiveViewService |
| 214XXX | 航线服务 WaylineService |
| 215XXX | 飞行安全服务 FlySafeService |
| 216XXX | 飞行控制模块 FlyControlService |
| 217XXX | 文件管理模块 FileStorageService |
| 218XXX | 实时建图模块 ReconstructionService |
| 219XXX | 计划库 TaskManageService |
| 220XXX | 机场日志 AirportLogService |

知道域之后：

- **域与你调的接口对得上** → 是业务参数/状态问题，按该域的业务语义排查（如 213XXX 先确认设备是否在推流）
- **域与你调的接口对不上** → 说明失败发生在平台内部的下游依赖，你侧改参数大概率无效，**带完整响应体报给 DJI**

## 常见通用框架码（200XXX）

| 错误码 | 常量 | 含义 | ISV 侧动作 |
|---|---|---|---|
| 200400 | `CodeFailure` | 通用失败 | 信息量为零，必须找内层 `err_code` |
| 200401 | `CodeInvalidToken` | Token 无效 | 刷新 token；检查 auth 回调是否返回了最新值 |
| 200403 | `CodePermissionDenied` | 权限不足 | 确认 workspace / 设备归属与账号权限 |
| 200404 | `CodeRecordNotFound` | 记录不存在 | 确认 SN / uuid 是否正确、是否已被删除 |
| 200610 | `CodeInvalidOrBlankParam` | 参数无效或为空 | 比对接口文档必填参数 |
| **200612** | **`CodeRpcFailure`** | **RPC 调用失败** | **典型的包装码，一定要解构出内层 `err_code`** |
| 200613 | `CodeErrorSign` | 签名错误 | 检查签名算法与密钥 |
| 200614 | `CodeBindError` | 绑定错误 | 检查设备绑定关系 |

## 常见业务码

| 错误码 | 含义 | 排查方向 |
|---|---|---|
| 213020 | `device_not_open` 设备未推流 | 先确认直播已启动（`fh_live_start`），再调依赖直播流的接口 |
| 213022 | 录制任务已启动（重复录制） | 幂等问题 —— 先查当前录制状态再决定是否发起 |

> 完整设备侧 6 位码（312xxx–514xxx）见 `error-codes.yaml`，与本文的平台侧 `2XXYYYY` 码是两套体系，不要混查。

## 报障给 DJI 时必须附带

平台内部码 ISV 无法自行深挖，走升级流程时附上以下内容可显著缩短定位时间：

1. **完整响应体原文**（不要只截 `code`，`message` 里的 `err_code` 是关键）
2. 请求的**接口路径 + 方法 + 参数**（脱敏后）
3. 请求时间 + 涉及的设备 SN / workspace
4. 响应 Header 中的 `request_id`（如有）—— 平台侧靠它串联全链路日志
