# 添加 OSD 数据订阅

## 目标
订阅无人机实时遥测数据（OSD），包含位置、电量、速度等。
> ⚠️ SDK 没有单一的 `listenDroneOsdInfo` 接口；"OSD 监听"由多个独立的 listener 组成。

## 涉及 SDK API
- `FlightHubSDK.getDroneInstance(sn)` → Drone
- `drone.getDroneOsdModule().listenAircraftLocation(callback)` → int listenId — 位置（纬度/经度/高度/海拔）
- `drone.getDroneOsdModule().cancelListenAircraftLocation(int listenId)`
- `drone.getDroneOsdModule().listenAircraftBatteryInfo(callback)` → int listenId — 电量信息
- `drone.getDroneOsdModule().cancelListenAircraftBatteryInfo(int listenId)`
- `drone.getDroneOsdModule().listenAircraftSpeed(callback)` → int listenId — 水平/垂直速度
- `drone.getDroneOsdModule().cancelListenAircraftSpeed(int listenId)`
- `drone.getDroneOsdModule().listenAircraftModeCode(callback)` → int listenId — 飞行模式
- `drone.getDeviceHMSModule().listenDeviceHMS(callback)` → int listenId — 健康管理告警
- `drone.getDroneOsdModule().listenAircraftPositionState(callback)` → int listenId — GPS/RTK 信号质量

## 必须遵守的规则
- ✅ 规则 3：每个 listener 均需保存 listenId，`@PreDestroy` 时逐一 cancel
- ✅ 规则 5：`getDroneInstance` null 检查
- ✅ listenId < 0 时记录失败日志

## 实现步骤

1. **创建订阅 Service**
   - 获取 Drone 实例（null 检查）
   - 通过 `drone.getDroneOsdModule()` 获取 OSD 模块
   - 调用各 `module.listenXxx` 方法，分别保存 listenId
   - 回调中处理对应数据（注意频率 5-10Hz，建议采样）

2. **生命周期管理**
   - `@PostConstruct`：按需启动各订阅
   - `@PreDestroy`：遍历所有 listenId，通过 OSD 模块调用对应 cancel 方法

3. **推送到前端**（可选）
   - 通过 WebSocket/SSE 将遥测转发给前端
   - 建议降频到 1Hz 再推送

## 遥测数据字段（常用）

### AircraftLocation
- latitude / longitude — 经纬度
- height — 相对地面高度（m）
- elevation — 海拔高度（m）

### AircraftBatteryInfo
- capacityPercent — 电量百分比（注意：字段名是 `capacityPercent`，不是 `batteryPercent`）
- landingPower — 强制降落剩余电量阈值
- remainFlightTime — 预计剩余飞行时间（秒）
- returnHomePower — 返航剩余电量阈值

### AircraftSpeed
- horizontalSpeed — 水平速度（m/s）
- verticalSpeed — 垂直速度（m/s，正值上升）

### AircraftAttitude
- head — 机头朝向（航向角）
- pitch / roll — 俯仰角 / 横滚角

## 性能注意
- 各 listener 推送频率 5-10Hz，下游处理不及时会阻塞
- 建议在回调中做时间采样（如每秒取最新一条）
- 多台无人机同时订阅时，注意线程安全

## 常见错误
- ❌ 不保存 listenId（无法 cancel，资源泄漏）
- ❌ 不通过 `getDroneOsdModule()` 直接在 drone 上调用 listenXxx（v3.3.x 已迁移到模块模式）
- ❌ 在回调中做重 IO（数据库写入等）导致背压
- ❌ 重复调用 listenXxx 不 cancel 旧的（重复触发）
- ❌ 误用字段名 `batteryPercent`（正确为 `capacityPercent`）
- ❌ 试图通过单个 `listenDroneOsdInfo` 获取所有数据（该 API 不存在）
