---
title: "设备管理全流程"
module: api-reference
tags: [device, DeviceTopo, drone-instance, listen, osd, hms]
audience: [agent, developer]
complexity: intermediate
summary: "设备发现 → 订阅 → 控制全流程"
last_updated: 2026-08-05
sdk_version: "3.3.7"
related:
  - api-reference/core-rules.md
  - api-reference/flight-control.md
---

设备管理遵循"发现 → 订阅 → 控制"三阶段流程，入口是 getDeviceList() 返回的 DeviceTopo 拓扑树。

# 设备管理流程

## 完整流程：设备发现 → 订阅 → 控制

```
初始化完成
  │
  ▼
getWorkspaceInstance() → null 检查
  │
  ▼
workspace.getDeviceManageModule().getDeviceList(callback)
  │  回调返回 List<DeviceTopo>
  ▼
遍历 DeviceTopo
  ├─ topo.getDevice() → 机场信息 (deviceSn, isOnline, nickname)
  └─ topo.getSubDevices() → 无人机列表
       │
       ▼
     getDroneInstance(sn) → null 检查
       │
       ▼
     订阅状态
     ├─ listenAircraftLocation(cb)     → 位置数据（纬度/经度/高度）  [DroneOsdModule]
     ├─ listenAircraftBatteryInfo(cb)  → 电量信息（5-10Hz）         [DroneOsdModule]
     ├─ listenDroneOnlineStatus(cb)    → 上下线状态                 [DeviceStateModule]
     ├─ listenDeviceHMS(cb)            → 健康管理告警               [DeviceHMSModule]
     └─ listenAircraftSpeed(cb)        → 飞行速度                   [DroneOsdModule]
```

## DeviceTopo 数据结构

```java
// getDeviceList 回调的数据
List<DeviceTopo> topoList;

DeviceTopo {
    Device device;              // 主设备（通常是机场 Dock）
    List<Device> subDevices;   // 子设备（通常是无人机 Drone）
}

Device {
    String deviceSn;      // 序列号（唯一标识）
    String deviceClass;   // 设备类型
    String deviceName;    // 设备名称
    String nickname;      // 昵称
    boolean isOnline;     // 在线状态
}
```

## 完整示例：发现设备并订阅遥测数据

```java
@Service
@Slf4j
public class DeviceDiscoveryService {

    private final DeviceSubscriptionManager subscriptionManager;

    public DeviceDiscoveryService(DeviceSubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
    }

    public void discoverAndSubscribe() {
        Workspace workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log.error("Workspace 为 null，SDK 可能未初始化");
            return;
        }

        workspace.getDeviceManageModule().getDeviceList((error, topoList) -> {
            // ✅ 规则 2：检查 errorCode
            if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
                log.error("获取设备列表失败: {}", error.getErrorCode());
                return;
            }

            for (DeviceTopo topo : topoList) {
                String dockSn = topo.getDevice().getDeviceSn();
                log.info("发现机场: sn={}, online={}", dockSn, topo.getDevice().isOnline());

                for (Device sub : topo.getSubDevices()) {
                    String droneSn = sub.getDeviceSn();
                    if (!sub.isOnline()) {
                        log.info("无人机离线，跳过: {}", droneSn);
                        continue;
                    }
                    subscribeDrone(droneSn);
                }
            }
        });
    }

    private void subscribeDrone(String sn) {
        // ✅ 规则 5：null 检查
        Drone drone = FlightHubSDK.getDroneInstance(sn);
        if (drone == null) {
            log.warn("无法获取 Drone 实例: {}", sn);
            return;
        }

        // ✅ 规则 3：保存 listenId + 注册清理 — 订阅位置数据
        DroneOsdModule osd = drone.getDroneOsdModule();
        if (osd == null) {
            log.warn("DroneOsdModule 不可用: {}", sn);
            return;
        }
        int locId = osd.listenAircraftLocation(loc -> {
            log.debug("位置 [{}]: lat={}, lng={}, height={}", sn,
                loc.getLatitude(), loc.getLongitude(), loc.getHeight());
        });
        if (locId >= 0) {
            subscriptionManager.track(sn, () -> osd.cancelListenAircraftLocation(locId));
        } else {
            log.error("订阅位置失败: sn={}, listenId={}", sn, locId);
        }

        // 订阅电量信息
        int batId = osd.listenAircraftBatteryInfo(bat -> {
            log.debug("电量 [{}]: capacity={}%, remainTime={}s", sn,
                bat.getCapacityPercent(), bat.getRemainFlightTime());
        });
        if (batId >= 0) {
            subscriptionManager.track(sn, () -> osd.cancelListenAircraftBatteryInfo(batId));
        }

        // 订阅在线状态（注意：在 DeviceStateModule 上，非 DroneOsdModule）
        DeviceStateModule state = drone.getDeviceStateModule();
        if (state == null) {
            log.warn("DeviceStateModule 不可用: {}", sn);
            return;
        }
        int onlineId = state.listenDroneOnlineStatus(status -> {
            if (!status.isOnline()) {
                log.info("设备离线: {}", sn);
                subscriptionManager.removeDevice(sn);
            }
        });
        if (onlineId >= 0) {
            subscriptionManager.track(sn, () -> state.cancelListenDroneOnlineStatus(onlineId));
        }
    }
}
```

## 设备详情查询

```java
// 获取单台设备的详细信息
workspace.getDeviceDetailInfo(deviceSn, (error, detail) -> {
    if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) return;

    log.info("设备详情: sn={}, model={}, firmware={}, status={}",
        detail.getDeviceSn(),
        detail.getModelInfo(),
        detail.getDeviceFirmwareVersion(),
        detail.getDeviceWorkingStatus());
});
```

## 常见问题

| 问题 | 原因 | 解决 |
|------|------|------|
| getDeviceList 回调返回空列表 | orgUuid 不匹配或无设备绑定 | 确认 Workspace 配置正确 |
| subDevices 为空 | 机场已绑定但无人机未入网 | 检查无人机是否已在 DJI CloudAPI2 注册 |
| getDroneInstance 返回 null 但列表中有 | 设备刚离线，缓存已清除 | 监听 onlineStatus，离线时移除 |
