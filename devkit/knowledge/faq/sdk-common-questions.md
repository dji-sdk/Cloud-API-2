---
title: "SDK 高频问题"
module: faq
tags: [faq, listen, osd, initialize, device-management, livestream, wayline, flight-task, return-to-home]
audience: [agent, developer]
complexity: intermediate
summary: "ISV 最常见的 10 个 SDK 接入问题"
last_updated: 2026-09-18
sdk_version: "3.3.7"
source: "skills/cloudapi2-sdk-core/references/文档检索与问答.md"
related:
  - api-reference/core-rules.md
  - api-reference/device-management.md
  - api-reference/error-codes.md
---

ISV 接入 FlightHub SDK 时最常遇到的 10 个问题，涵盖 listen 回调、设备管理、初始化、直播、Token 和航线任务控制等核心场景。

## Q1: listen 回调不触发，怎么排查？

排查步骤：
1. 检查 `listenId` 是否 ≥ 0（< 0 = 注册失败）
2. 确认设备在线（`listenDroneOnlineStatus` 验证）
3. 确认数据是否有变化（OSD 只在数据变化时推送）
4. 检查 SDK 是否已初始化成功
5. 检查 WebSocket 通知连接是否正常（`notificationServerAddress` 配置）

## Q2: 多台设备如何统一管理订阅？

使用 `DeviceSubscriptionManager` 模式（按设备 SN 分组管理）：

```java
@Component
public class DeviceSubscriptionManager {
    private final Map<String, List<Runnable>> cleanups = new ConcurrentHashMap<>();

    public void track(String sn, Runnable cancelAction) {
        cleanups.computeIfAbsent(sn, k -> new CopyOnWriteArrayList<>()).add(cancelAction);
    }

    public void removeDevice(String sn) {
        List<Runnable> fns = cleanups.remove(sn);
        if (fns != null) fns.forEach(Runnable::run);
    }

    @PreDestroy
    public void removeAll() {
        cleanups.forEach((sn, fns) -> fns.forEach(Runnable::run));
        cleanups.clear();
    }
}
```

## Q3: SDK 支持哪些平台？

| 平台 | 原生库 | 说明 |
|------|--------|------|
| Linux x86_64 | `.so` | 生产推荐 |
| macOS arm64 | `.dylib` | 开发环境 |
| macOS x86_64 | `.dylib` | Intel Mac |
| Windows x64 | `.dll` | 开发环境 |

## Q4: initialize 应该在哪里调用？

| 框架 | 推荐位置 | 原因 |
|------|----------|------|
| Spring Boot | `@PostConstruct` | 保证容器启动时完成，失败阻止启动 |
| 非 Spring | `main()` 最前面 | 同步阻塞，后续代码可安全使用 |
| 测试 | `@BeforeAll` | 整个测试类共享一次初始化 |

## Q5: 怎么处理设备离线后重新上线？

```java
drone.getDeviceStateModule().listenDroneOnlineStatus(status -> {
    if (status.isOnline()) {
        subscribeDrone(sn);
    } else {
        subscriptionManager.removeDevice(sn);
    }
});
```

## Q6: OSD 数据推送频率太高怎么办？

OSD 各 listener 推送频率可达 5-10Hz。建议在回调中做采样：

```java
private volatile long lastLocationTime = 0;
private static final long LOCATION_INTERVAL_MS = 1000;

int locId = drone.getDroneOsdModule().listenAircraftLocation(loc -> {
    long now = System.currentTimeMillis();
    if (now - lastLocationTime < LOCATION_INTERVAL_MS) return;
    lastLocationTime = now;
    processLocation(loc);
});
```

## Q7: 同时开多少路直播合适？

建议不超过 **4 路**同时直播。超出后可能出现连接建立失败、画面卡顿、SDK 内部 RTC 资源不足。

## Q8: Token 过期怎么处理？

SDK 没有内置 Token 刷新机制。推荐：
1. 应用层定时检测（如每小时）
2. 收到 httpCode=401 时触发刷新
3. 刷新后需要 `shutdown()` → `initialize(newConfig)` 重新初始化

## Q9: 如何判断设备类型（机场 vs 无人机）？

```java
workspace.getDeviceManageModule().getDeviceList((error, topoList) -> {
    for (DeviceTopo topo : topoList) {
        String dockSn = topo.getDevice().getDeviceSn();
        Dock dock = FlightHubSDK.getDockInstance(dockSn);

        for (Device sub : topo.getSubDevices()) {
            String droneSn = sub.getDeviceSn();
            Drone drone = FlightHubSDK.getDroneInstance(droneSn);
        }
    }
});
```

## Q10: 暂停/恢复航线任务时报"状态无法更新"，但任务状态看起来正常，为什么？

**现象**：调用 `FlightTaskModule.updateFlightTaskStatus()`（对应 `fh_wayline_control` 的 pause/resume/cancel）时，服务端返回类似"任务状态无法更新"的错误；但同一时刻通过任务查询接口看到的状态字段（如"执行中""暂停中"）看起来完全允许该操作。

**原因**：航线任务对外暴露的状态字段通常只有"等待/执行中/暂停/取消/完成"等粗粒度枚举，**不单独区分"返航中（RTH）"这个子阶段**。当无人机因低电量保护、失联保护、或此前已下发过 `cancel`/`returnHome` 而自主进入返航后，任务状态字段可能仍显示为"执行中"或"暂停"，但服务端的任务状态机此时已不再接受暂停/恢复类指令，因此调用会被拒绝——报错信息本身通常也不会点明"设备已进入返航"这一真实原因。

**排查建议**：
1. 调用暂停/恢复/取消前，先用 `fh_osd_snapshot` 查看无人机的 `mode_code`，确认设备当前飞行模式是否已经是返航/自动降落类状态，不要只依赖任务状态字段做判断
2. 收到"状态无法更新"类报错时，优先怀疑"设备已进入返航"，再排查是否为参数错误或网络问题
3. 若需要在 UI 上据此禁用暂停/恢复入口，判断依据应该是设备实时飞行模式（OSD `mode_code`），而不是任务状态字段本身——这是当前航线任务状态模型的粒度限制，跨版本、跨项目都可能遇到

## SDK 模块索引

| 模块 | 获取方式 | 主要能力 |
|------|----------|---------|
| Workspace | `FlightHubSDK.getWorkspaceInstance()` | 设备列表、绑定/解绑、设备详情 |
| Drone | `FlightHubSDK.getDroneInstance(sn)` | 飞控、OSD、在线状态、HMS |
| Dock | `FlightHubSDK.getDockInstance(sn)` | 机场控制、舱盖、相机 |
| LiveStream | `drone.getLiveStream(LiveInfo)` / `dock.getLiveStream(LiveInfo)` | 直播开始/停止 |
| FlightTaskModule | `workspace.getFlightTaskModule()` | 航线任务 CRUD |
| FlyToPointModule | `drone.getFlyToPointModule()` | 指点飞行 |
| MediaServiceModule | `workspace.getMediaServiceModule()` | 媒体文件 |
