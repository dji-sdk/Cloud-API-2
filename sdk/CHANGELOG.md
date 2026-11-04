# 更新日志

## v3.4.0

### 新增功能

- **级联部署**：新增级联部署切换 node 功能与 topic 监听，支持多层节点拓扑
- **自动录制**：新增 `IAutoRecordModule` 自动录制配置模块
- **AI 告警**：新增 AI 告警记录删除接口 `deleteAiAlertRecord` 及告警推送 WS 监听全链路
- **全景拼接**：PaaS 3X 超清全景拼接 SDK 接口实现
- **旁路推流**：新增 `LiveSyncDemo` 旁路推流示例，去除硬编码敏感信息改为参数读取
- **航线安全**：对接航线任务飞行安全风险查询接口
- **蛙跳任务**：支持 A 起 B 降蛙跳任务与指定机场返航
- **高速模式**：`createFlightTask` 支持 `speed_mode` 参数切换高速模式及 `resumable_status` 透传
- **DRC 控制**：DRC 链路新增 `drc_set_flying_max_speed`；杆量改走 V3 压缩协议并兼容 V2
- **舱外起降**：新增地面起飞/降落模块及 demo
- **DRTC 多实例**：集成 DRTC Device SDK v0.28，支持双机并发拉流
- **AI 机载算法**：新增 AI 机载算法 demo 入口
- **平台支持**：新增 Linux aarch64 支持；适配 340 机型直播拉流

### 安全加固

- 密码日志脱敏：`post_relay_password` / `put_live_sync_update` 请求/响应体日志统一经 `log_safe()` 过滤
- `setLogLevel` 增加四值白名单校验，非法参数返回 `ERROR_INVALID_PARAM` 且配置不变更
- `LiveSyncDemo` 去除默认用户名/密码，敏感字段改为参数读取+交互输入
- Emscripten 网络层 `strcpy` 替换为 `snprintf`，消除 CWE-120 告警

### 修复

- 修复 `Session` 实例构造链同线程二次加锁自死锁（`getDroneInstance` / `getDockInstance`）
- 修复 `shutdown()` 在途 HTTP 回调 IO 侧判空守卫
- 修复 `listenWaylineTaskEvent` 回调 `media_upload_status` 恒为 0
- 修复级联 topic 解析兼容 null 字段与原样透传 payload
- 修复 WS 未就绪时的订阅丢弃问题，改为暂存 recovery 表待 OnOpen 补订阅
- S3 直传通道接入 `ssl_ca_path` / `verify_ssl`，修复自签环境 `uploadSpeakerAudioFile` curl 60 错误
- WS 通知通道 TLS 配置透传（`verify_ssl` / `ssl_ca_path`）
- 飞行区更新废弃 `update_by`、元素渲染属性键对齐 `clampToGround`
- 修复 `cover-close` 补 `data.mode`、`speed-tests` bid 解析、`reports/firmwares` 分页 `page_num`
- 修复设备详情/HMS/中继控制协议问题；空中下发航线查询接口对齐后端协议
- 删除不可用的 `listenDrcDeviceHeartBeat` 接口及其专属数据结构
- 修复 `AircraftBatteryInfo` / `AircraftMaintainStatusInfo` 默认构造或 `set(null)` 后 `toString()` 抛 NPE，集合字段统一归一化为空列表

### 不兼容变更

- `AiAlertRecordItem.mediaIndex`：`int` → `long`（C++ 对应 `int64_t`），与后端 `media_index` 的 int64 类型对齐
- `AiAlertTargetAlertInfo.targetValue`：`int` → `double`，与后端 `target_value` 的 float64 类型对齐
- 影响：Java 方法签名变化，旧版本编译产物需重新编译；C++ 结构体布局变化，C++ 调用方需重新编译

### 依赖更新

- macOS arm64 库更新

### 文档

- 补充 `AiDetectionTarget.targetState` 目标跟踪状态取值表
- 补充 `videoResolution` 取值表并修正 HMS `module/displayScene` 注释
- 补充 `AircraftWindSpeed` 风速单位说明（0.1 m/s）
- 补充舱外起降模块固件版本支持说明
- STS expire 文档标注合法下限
- 航线列表与详情 `updatedAt` 单位说明
- 补充 AI SEI 框来源字段（`model_index` / `frame_source` / `model_info_type`）JavaDoc，明确字段位于 `AiDetectionTarget`、订阅入口为 `LiveModule.subscribeAiSeiInfo`
- README CMake 版本要求更正为 3.20+

## v3.3.7

### 安全审计整改

- TLS 证书验证 `verify_ssl` 默认值统一为 `true`（修正 v3.3.6 回归），新增打包门禁防翻转
- 预编译库构建路径残留清理
- 打包脚本加固与 `.sha256` 校验文件生成
- 文档与注释口径对齐

## v3.3.6

### ⚠️ 破坏性安全变更：TLS 证书验证默认开启（verify_ssl 默认值由 false 改为 true）

自本版本起，HTTPS（PaaS API）、WSS（消息通知）及 DRTC demo 通道的 `verify_ssl` **默认值由 `false` 改为 `true`**，即默认校验服务器证书链与主机名/IP。关闭验证时会产生 WARN 日志。

- **谁受影响**：所有未显式配置 `verify_ssl` 的集成方——升级后首次连接自签名证书环境将失败（预期行为）。
- **迁移路径**（二选一）：
  1. **推荐**：保持 `verify_ssl=true`，通过 `ssl_ca_path` 指定私有 CA 证书（PEM 格式）：
     - C++：`FlightHubSdkConfig::ssl_ca_path = "/path/to/ca.pem"`
     - Java：`FlightHubSdkConfig#setSslCaPath("/path/to/ca.pem")`
     - `ssl_ca_path` 为空时使用系统 CA 信任库（适用于官方云端点）
  2. 受控测试环境临时降级：显式 `verify_ssl = false`（**存在中间人攻击风险，严禁用于生产**）
- **DRTC 拉流通道**：`drtc_config_node.json` 各 `verify_ssl` 字段同步改为 `true`；私有化部署须同步配置 `ssl_ca_path`。

### 安全加固

- 直播流 token 等凭据不再写入日志；请求/响应体日志由 INFO 降为 DEBUG 级（生产 info 级下不再输出，排障时调 debug 级可见）。
- `gradle-wrapper.properties` 增加 `distributionSha256Sum`，wrapper 下载 Gradle 发行包时校验完整性。

### 新增

- Java 侧 `FlightHubSdkConfig` 暴露 TLS 配置（`verifySsl` / `sslCaPath`），与 C++ 层能力对齐。
