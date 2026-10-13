# 添加设备列表功能

## 目标
实现设备列表查询接口，展示当前工作空间下所有机场和无人机的拓扑关系。

## 涉及 SDK API
- `FlightHubSDK.getWorkspaceInstance()` → Workspace
- `workspace.getDeviceManageModule().getDeviceList(DeviceListCallback)` → `List<DeviceTopo>`
- `FlightHubSDK.getDroneInstance(sn)` → Drone (可选，用于获取实例)
- `FlightHubSDK.getDockInstance(sn)` → Dock (可选)

## 必须遵守的规则
- ✅ 规则 2：回调中先检查 `error.getErrorCode()`
- ✅ 规则 5：`getWorkspaceInstance()` 返回值 null 检查

## 实现步骤

1. **创建 Service 层**
   - 注入或获取 Workspace 实例
   - 调用 `getDeviceList`，在回调中检查 errorCode
   - 将 `List<DeviceTopo>` 转为业务 DTO

2. **创建 Controller**
   - GET `/api/devices` → 返回设备列表
   - 处理 Workspace 为 null 的情况（返回 503）

3. **数据结构映射**
   ```
   DeviceTopo
     ├─ device (主设备/机场)
     │    ├─ deviceSn
     │    ├─ deviceName / nickname
     │    └─ isOnline
     └─ subDevices (子设备/无人机)
          ├─ deviceSn
          └─ isOnline
   ```

## 常见错误
- ❌ 不检查 errorCode 直接遍历 topoList（NPE 风险）
- ❌ 硬编码设备 SN 而非从 getDeviceList 获取
- ❌ 在 Controller 中直接调用 SDK（应通过 Service 层）
