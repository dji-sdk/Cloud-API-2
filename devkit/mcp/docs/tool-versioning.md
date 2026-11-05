# MCP 工具版本与更新指南

> 本文档说明 DJI Cloud API 2 MCP Server 的工具版本机制，以及如何新增/修改工具。

---

## 目录

- [版本机制](#版本机制)
- [当前版本工具清单](#当前版本工具清单)
- [工具更新方式](#工具更新方式)
  - [方式一：注解驱动（已支持，存量工具未使用）](#方式一注解驱动已支持存量工具未使用)
  - [方式二：Legacy 手写](#方式二legacy-手写)
- [自动注册原理](#自动注册原理)
- [新增工具标准流程](#新增工具标准流程)
- [版本历史](#版本历史)

---

## 版本机制

DJI Cloud API 2 MCP Server 以 **服务版本号** 作为工具集的统一版本标识，遵循 [Semantic Versioning 2.0.0](https://semver.org/lang/zh-CN/)：

```
major.minor.patch
  │      │     └── Bug 修复 / 参数调整 / 安全加固（不新增工具，不删除工具）
  │      └──────── 新增工具 / 已有工具新增 action（向后兼容）
  └─────────────── 破坏性变更（工具重命名 / 参数类型变更 / 删除工具）
```

| 版本 | 包含内容 |
|------|---------|
| `1.0.x` | Bug 修复、参数说明完善、安全等级调整，工具集与 `1.0.0` 完全相同 |
| `1.x.0` | 新增工具或在已有工具上追加 action，不移除已有 API |
| `2.0.0+` | 可能包含工具重命名、参数格式变更或移除废弃工具（需更新 AI Skill / Prompt） |

> **当前版本**：`1.0.0`（首个稳定版正式发布前）  
> Maven artifact：`com.dji.flighthub:flighthub-mcp-server:1.0.0`  
> Docker 镜像：本地构建 → `docker build -t flighthub-mcp .`（见 [快速开始](../README.md#快速开始)）

---

## 当前版本工具清单

**v1.0.0 — 67 个 `fh_*` 工具，覆盖 26 个业务子包**（另有 4 个 `sim_*` 模拟器工具，见本节末尾「模拟器工具」；运行时以 `tools/list` 返回为准）

### 接入与会话（3 个）

| 工具名 | 说明 | 安全等级 |
|--------|------|----------|
| `fh_connect` | 初始化 SDK + 建立工作空间 session | L0 |
| `fh_session_info` | 查询当前 session 状态 | L0 |
| `fh_ops` | 运维：健康检查 / 日志级别 / 优雅关闭 | L0 |

### 设备管理（6 个）

| 工具名 | 说明 | 安全等级 |
|--------|------|----------|
| `fh_device_list` | 查询工作空间设备，支持类型/状态过滤 | L0 |
| `fh_drone_snapshot` | 无人机实时快照：位置/电量/姿态/速度 | L0 |
| `fh_dock_snapshot` | 机场实时快照：舱盖/电源/存储/在仓状态 | L0 |
| `fh_device_manage` | 设备详情 / 重命名 / 解绑 / HMS 告警 | L0~L1 |
| `fh_dock_control` | 控制机场：舱盖 / 充电 / 补光灯 | L1 |
| `fh_deploy_subscribe` | 机场部署事件订阅：视觉侦察任务状态 / 障碍物 / 异常信息 / 星历下载状态 | L0 |

### 航线任务（7 个）

| 工具名 | 说明 | 安全等级 |
|--------|------|----------|
| `fh_wayline_upload` | 上传 KMZ/KML 航线到云端 | L1 |
| `fh_wayline_execute` | 执行航线任务，触发起飞 | L2 |
| `fh_wayline_control` | 暂停 / 恢复 / 取消航线 | L2 |
| `fh_wayline_status` | 查询任务详情或列表 | L0 |
| `fh_wayline_recommend` | 按机型匹配推荐机场可飞航线 | L0 |
| `fh_inflight_wayline` | 空中无人机在飞航线管理（新建 / 暂停 / 恢复 / 取消 / 状态查询） | L1~L2 |
| `fh_testflight` | 试飞航线生成与任务创建（TestFlightModule） | L0~L1 |

### 实时指挥 DRC（18 个）

| 工具名 | 说明 | 安全等级 |
|--------|------|----------|
| `fh_control_acquire` | 获取飞行控制权（全局互斥） | L1 |
| `fh_control_release` | 释放飞行控制权 | L1 |
| `fh_drc_connect` | 建立 DRC 低延迟控制链路 | L1 |
| `fh_drc_disconnect` | 断开 DRC 链路 | L1 |
| `fh_takeoff` | 一键起飞至指定高度 | L2 |
| `fh_return_home` | 返航 / 取消返航 | L2 |
| `fh_flyto` | 指点飞行：start / stop / update | L2 |
| `fh_poi` | 环绕飞行（兴趣点绕飞） | L2 |
| `fh_stick_move` | 受限摇杆控制（方向 + 时长 + 速度） | L2 |
| `fh_emergency_stop` | 紧急停止（不可逆，需审计原因） | L3 |
| `fh_payload_acquire` | 获取负载控制权（相机/云台/喊话器/探照灯等负载操作的前置） | L1 |
| `fh_payload_release` | 释放负载控制权 | L1 |
| `fh_live_start` | 直播推流：开始 / 停止 / 画质切换 | L1 |
| `fh_live_device` | 直播能力查询：可用码流（多路直播）/ 机场直播状态 | L0 |
| `fh_live_sei` | 直播码流查询 / 启停 + SEI 数据订阅（ai/ar/fov/assistant/eis） | L0~L1 |
| `fh_live_sync` | 直播同步任务管理（LiveSync：RTMP/GB28181） | L1 |
| `fh_osd_snapshot` | OSD 实时遥测：位置/姿态/电量/GPS/风速 | L0 |
| `fh_osd_listen` | OSD 遥测读取/订阅（Drone / Dock，字段目录 / 单次读取 / 持续订阅） | L0 |

### 媒体管理（5 个）

| 工具名 | 说明 | 安全等级 |
|--------|------|----------|
| `fh_media_list` | 查询照片/视频列表，支持多维过滤 | L0 |
| `fh_media_download` | 获取媒体文件预签名下载 URL | L0 |
| `fh_media_delete` | 批量删除媒体文件（1~100 个） | L1 |
| `fh_media_record` | 直播录制：开始 / 停止 / 列表 | L1 |
| `fh_media_upload` | 媒体上传：获取对象存储 STS 临时凭证后直传 | L1 |

### 自定义飞行区（3 个）

| 工具名 | 说明 | 安全等级 |
|--------|------|----------|
| `fh_flight_area_list` | 查询自定义飞行区（禁飞区、限高区等） | L0 |
| `fh_flight_area_manage` | 创建 / 更新 / 删除自定义飞行区 | L1 |
| `fh_flight_area_sync` | 同步飞行区到设备端 | L1 |

### 三维重建（1 个）

| 工具名 | 说明 | 安全等级 |
|--------|------|----------|
| `fh_terra` | DJI Terra：任务管理 / 点云 / 模型生成 / 进度 | L0~L1 |

### 空域安全（4 个）

| 工具名 | 说明 | 安全等级 |
|--------|------|----------|
| `fh_device_recommend` | 根据任务需求推荐设备类型 | L0 |
| `fh_tcas` | TCAS：感知列表 / 告警阈值 | L0~L1 |
| `fh_unlock_license` | 空域解锁许可证：列表 / 导入 / 同步 | L0~L1 |
| `fh_flysafe` | FlySafe：禁飞区 / 限飞区 / 授权区域 | L0 |

### 任务管理（1 个）

| 工具名 | 说明 | 安全等级 |
|--------|------|----------|
| `fh_flight_task` | 飞行任务 CRUD（list/detail/create/edit/delete/execute/update_status） | L0~L2 |

### 固件管理（1 个）

| 工具名 | 说明 | 安全等级 |
|--------|------|----------|
| `fh_firmware` | 固件全生命周期（11 个 action：查询/升级/上传/发布等） | L0~L2 |

### 相机与负载（6 个）

| 工具名 | 说明 | 安全等级 |
|--------|------|----------|
| `fh_camera` | 相机高级控制（22 个 action：拍照/录像/变焦/红外等） | L0~L1 |
| `fh_speaker` | PSDK 喊话器（12 个 action：TTS/播放/音量/实时喊话/状态订阅等） | L1 |
| `fh_searchlight` | 探照灯高级控制（7 个 action：模式/校准/云台/亮度等） | L1 |
| `fh_psdk_widget` | PSDK 自定义 Widget：设值 / 文本输入 | L0 |
| `fh_psdk_resource` | PSDK 喊话器音频/TTS 资源 + Widget UI 资源管理 | L0~L1 |
| `fh_psdk_subscribe` | PSDK 负载状态订阅（listen_* → events 生命周期） | L0 |

### 设备健康与诊断（3 个）

| 工具名 | 说明 | 安全等级 |
|--------|------|----------|
| `fh_hms` | HMS 健康管理：当前告警 / 历史告警 / 订阅 | L0 |
| `fh_diagnosis` | 设备诊断：创建报告 / 查询 / 列表 / 操作 | L0~L1 |
| `fh_device_debug` | 机场运维调试（14 个 action：舱盖/上下电/重启/格式化等） | L0~L1 |

### 运维工具（6 个）

| 工具名 | 说明 | 安全等级 |
|--------|------|----------|
| `fh_log` | 日志管理：查询 / 上报 / 列表 / 操作 / 上传 | L0~L1 |
| `fh_maintenance` | 维修工单：获取配置 / 创建 / 详情 / 列表 | L0 |
| `fh_offline_map` | 离线地图：模型 / 更新 / 同步 / FlySafe 升级 | L0~L1 |
| `fh_flight_record` | 飞行记录：列表 / 详情 / 统计 | L0 |
| `fh_workspace` | 工作空间：列表 / 详情 / 复制 / 批量删除 / 下载 | L0~L1 |
| `fh_setting` | 设置管理（持久化飞行参数：返航高度等，不直接触发飞行） | L0~L1 |

### AI 能力（3 个）

| 工具名 | 说明 | 安全等级 |
|--------|------|----------|
| `fh_ai_identify` | AI 目标识别配置：开关 / 检测过滤 / 置信度阈值 / 阈值模式 | L1 |
| `fh_ai_model` | AI 模型选择 / 绑定到相机码流 | L0~L1 |
| `fh_ai_subscribe` | AI 数据推送订阅（ai_data / alarm_state） | L0 |

### 模拟器工具（4 个 sim_* + 2 个 sim_ 子操作）

`tools/simulator/` 包下的模拟器工具，用于无真机环境演示（cookie 认证，不走 session，需 `FH_SIM_PASSWORD`）：

| 工具名 | 说明 | 安全等级 |
|--------|------|----------|
| `sim_device` | 模拟设备上线 / 下线 | 模拟 |
| `sim_flight_state` | 模拟飞行状态（位置 / 姿态 / 电量） | 模拟 |
| `sim_hms` | 模拟 HMS 告警上报 | 模拟 |
| `sim_recording` | 模拟媒体录制上报（含 `sim_slot` / `sim_slot_switch` 子操作） | 模拟 |

---

## 工具更新方式

MCP Server 支持两种工具实现方式，可以混用——不需要将所有工具迁移到同一种方式。

### 方式一：注解驱动（已支持，存量工具未使用）

框架已支持注解驱动（`@McpTool` + `@McpParam` + `SchemaGenerator`），声明式注解替代手写 JSON Schema。
**注意：截至本文档更新时，67 个 `fh_*` 工具全部为 Legacy 手写模式**，尚无工具使用注解驱动；
注解驱动可作为新工具的可选实现方式，但需自行验证与现有 `AbstractMcpTool` 门控逻辑（前置条件链、
人工确认、`dry_run`）的兼容性。

**实现步骤：**

```java
package com.dji.flighthub.mcp.tools.example;

import com.dji.flighthub.mcp.annotation.McpParam;
import com.dji.flighthub.mcp.annotation.McpTool;
import com.dji.flighthub.mcp.annotation.Prerequisite;
import com.dji.flighthub.mcp.annotation.ToolContext;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * fh_example — 示例工具（仅展示注解驱动模式，不对外注册）。
 *
 * @author DJI Cloud API 2 SDK Team
 */
@Component
@McpTool(
    name = "fh_example",
    description = "Example: query current battery of a drone.",  // 保持英文（供 LLM 消费）
    securityLevel = SecurityLevel.READ_ONLY,                      // L0 只读
    prerequisites = { Prerequisite.SESSION }                       // 需要先调用 fh_connect
)
public class FhExampleTool extends AbstractMcpTool {

    // ---------- 参数声明 ----------
    // 字段名即参数名；类型自动映射为 JSON Schema 类型
    @McpParam(description = "Target drone serial number", required = true)
    private String drone_sn;

    @McpParam(description = "Response language (zh/en)", defaultValue = "zh",
              enumValues = { "zh", "en" })
    private String lang;

    // ---------- 构造函数（注入依赖）----------
    public FhExampleTool(SessionManager sessionManager) {
        super(sessionManager);
    }

    // ---------- 业务逻辑 ----------
    @Override
    protected Map<String, Object> doExecute(ToolContext ctx) {
        String sn = ctx.getString("drone_sn");           // 从上下文取参数
        String language = ctx.getString("lang", "zh");   // 带默认值
        return successResult(Map.of("sn", sn, "battery", 85), "OK");
    }
}
```

**注解说明：**

| 注解 / 字段 | 作用 |
|-------------|------|
| `@McpTool(name)` | Tool 名称（MCP `tools/call` 中的 `name` 字段） |
| `@McpTool(description)` | Tool 描述（显示给 AI 的 function 说明，**保持英文**） |
| `@McpTool(securityLevel)` | L0~L3 安全等级，决定是否触发人工确认弹窗 |
| `@McpTool(prerequisites)` | 前置条件链（SESSION / SECURITY_LEVEL / CONTROL_AUTHORITY / DRC_LINK），按顺序自动检查 |
| `@McpParam(description)` | 参数描述（必填，显示给 AI） |
| `@McpParam(required)` | 是否必填，默认 `false` |
| `@McpParam(enumValues)` | 枚举约束，生成 JSON Schema `enum` 字段 |
| `@McpParam(range)` | 数值范围 `{min, max}`，生成 `minimum`/`maximum` 约束 |
| `@McpParam(defaultValue)` | 默认值（字符串，运行时按字段类型转换） |
| `@McpParam(requiredWhen)` | 条件必填，格式：`"action in [start, stop]"` |

**字段类型映射：**

| Java 类型 | JSON Schema 类型 |
|-----------|-----------------|
| `String` | `"string"` |
| `Integer` / `int` | `"integer"` |
| `Long` / `long` | `"integer"` |
| `Double` / `double` | `"number"` |
| `Boolean` / `boolean` | `"boolean"` |

---

### 方式二：Legacy 手写

适用于参数结构复杂（嵌套对象、`oneOf`、条件 Schema）的工具，或保持现有工具原样时使用。

```java
@Component
public class FhLegacyExampleTool extends AbstractMcpTool {

    public FhLegacyExampleTool(SessionManager sessionManager) {
        super(sessionManager);
    }

    // 手动定义 tool name / description / JSON Schema
    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", "fh_legacy_example");
        def.put("description", "Legacy-style tool example.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("drone_sn", Map.of("type", "string", "description", "Target drone SN"));
        schema.put("properties", props);
        schema.put("required", List.of("drone_sn"));
        def.put("inputSchema", schema);
        return def;
    }

    // 覆写 execute()，直接处理 args Map
    @Override
    public Map<String, Object> execute(Map<String, Object> args) {
        if (requireSession() == null) {
            return noSessionError();
        }
        String sn = getStringArg(args, "drone_sn", null);
        if (sn == null) {
            return errorResult("Missing drone_sn", "Provide the target drone SN.");
        }
        return successResult(Map.of("sn", sn), "OK");
    }
}
```

> **存量工具无需迁移**。两种模式可以在同一服务中共存（当前存量工具均为 Legacy 模式），
> 新增工具可按团队习惯任选一种方式。

---

## 自动注册原理

不管使用哪种方式，只要满足以下两个条件，工具就会在服务启动时**自动注册**，无需手动编辑任何注册表或配置文件：

1. 继承 `AbstractMcpTool`
2. 标注 Spring `@Component`（或 `@Service`）

**注册流程：**

```
Spring 容器启动
    └─ 收集所有 AbstractMcpTool 实现 Bean
         └─ McpToolAutoRegistrar.afterSingletonsInstantiated()
              ├─ 验证每个 Tool（@McpTool 注解 OR 覆写 execute()，二选一必须满足）
              ├─ 调用 tool.getToolDefinition() 生成 JSON Schema
              │     注解驱动：SchemaGenerator 从 @McpParam 字段自动生成
              │     Legacy：直接返回手写的 Map
              └─ registry.register(definition, tool::execute)
                    └─ tools/list 和 tools/call 即时生效
```

**关键类：**

| 类 | 职责 |
|----|------|
| `McpToolAutoRegistrar` | 扫描 + 注册所有 Tool Bean，启动时断言配置合法性 |
| `SchemaGenerator` | 反射读取 `@McpParam` 字段，生成 JSON Schema properties |
| `McpToolRegistry` | 运行时工具注册中心，响应 `tools/list` 和 `tools/call` |
| `AbstractMcpTool` | 基类：参数解析 / 错误响应 / 人工确认门控 / SDK 错误格式化 |

---

## 新增工具标准流程

按以下步骤添加一个新工具，确保编译、测试、CI 全部通过：

```
Step 1  确定业务包
         在 tools/ 下找到对应的子包（device/drc/media/...）
         没有合适的则新建（如 tools/org/ 对应组织管理模块）

Step 2  创建 Tool 类
         文件名：Fh{功能}Tool.java
         继承：AbstractMcpTool
         注解：@Component + @McpTool（或 Legacy 覆写 getToolDefinition）

Step 3  实现参数声明
         注解模式：每个参数声明 @McpParam 字段
         Legacy 模式：在 getToolDefinition() 的 inputSchema 中手写

Step 4  实现业务逻辑
         注解模式：覆写 doExecute(ToolContext ctx)
         Legacy 模式：覆写 execute(Map<String, Object> args)
         先检查 session，再校验参数，再调用 SDK

Step 5  编写测试
         创建 Fh{功能}ToolTest.java
         至少覆盖：无 session 返回错误 / 参数缺失返回错误 / Mock 模式正常返回

Step 6  编译验证
         cd mcp && mvn clean package -DskipTests
         确认无 Checkstyle 警告：mvn checkstyle:check

Step 7  运行测试
         mvn test
         确认全部测试通过，无回归
         （当前基线：137 个测试类、约 2000 个 @Test 用例，以本地 mvn test 实际输出为准）

Step 8  更新版本记录
         在 MR 描述中记录新增工具
         更新顶部"最后更新"日期和工具总数

Step 9  提交 MR
         分支命名：feat/fh-{工具名}
         描述中注明：工具名称、所属模块、安全等级、覆盖 actions
```

---

## 版本历史

工具数按 `tools/` 下 `fh_*` 工具的**累计数量**统计，迁移/拆分（如 `fh_payload` 拆为 acquire/release）也计入变更。统计口径：截至 2026-10-23 源码实测 67 个。

| 版本 | 日期 | 变更摘要 | 累计工具数 |
|------|------|---------|-----------|
| `1.0.0` | 2026-06-22 | 基础框架 + 接入与会话 3 工具 + 设备管理 5 工具 | **8** |
| `1.0.0` | 2026-06-25 | DRC 实时指挥核心 11 工具 + `fh_live_start` + `fh_osd_snapshot` | **21** |
| `1.0.0` | 2026-07-02 | 航线 4 + 媒体 4 + 自定义飞行区 3 + 空域安全 4 + `fh_terra` + `fh_flight_task` + `fh_firmware` + `fh_speaker` | **40** |
| `1.0.0` | 2026-07-03 | 相机与负载 5（camera/debug/hms/record/searchlight）+ 运维批 6（log/maintenance/diagnosis/offline_map/psdk_widget/workspace），首个稳定版 | **51** |
| `1.0.0` | 2026-07-23 | `fh_payload` 拆分为 `fh_payload_acquire` / `fh_payload_release` | **52** |
| `1.0.0` | 2026-07-27 | `fh_wayline_recommend`（按机型匹配推荐可飞航线） | **53** |
| `1.0.0` | 2026-09-24 | 模拟器包落地：4 个 `sim_*` 工具（不计入 `fh_*` 计数） | **53** |
| `1.0.0` | 2026-10-20 | AI 能力 3（identify/model/subscribe）+ `fh_live_sei` + `fh_live_device` + `fh_live_sync` + `fh_osd_listen` | **60** |
| `1.0.0` | 2026-10-21 | `fh_testflight` + `fh_media_upload` + `fh_psdk_resource` + `fh_psdk_subscribe` + `fh_deploy_subscribe` + `fh_setting` + `fh_inflight_wayline` | **67** |

**下一版本（v1.1.0）待规划工具：**

| 工具（草案） | SDK 模块 | 状态 | 优先级 |
|-------------|----------|------|--------|
| `fh_ai` | 22-AI 识别 (AiModule) | ✅ 已实现，实际拆为 `fh_ai_identify` / `fh_ai_model` / `fh_ai_subscribe` | — |
| `fh_org` | 09-组织管理 (OrgModule) | ❌ 未实现 | P1 |
| `fh_map_element` | 10-地图元素 (MapElementModule) | ❌ 未实现 | P1 |
| `fh_notification` | 13-通知/告警 | ❌ 未实现 | P2 |
| `fh_storage` | 14-存储管理 (StorageModule) | ❌ 未实现 | P2 |
| `fh_user` | 15-用户管理 (UserModule) | ❌ 未实现 | P2 |

