# OSD 数据订阅模式

OSD（On-Screen Display）数据是设备实时遥测信息，由 SDK 以 1-5Hz 频率推送。

## 在 skeleton 中消费 OSD

skeleton 的 kernel 层已将 SDK 底层订阅封装为 Spring Event：

```java
@EventListener
public void onOsdUpdate(OsdUpdateEvent event) {
    String sn = event.deviceSn();
    OsdData osd = event.osd();
    // 处理 OSD 数据
}
```

ISV 不需要自行调用 SDK 的 `listenOsd()` / `cancelListenOsd()`，kernel 层统一管理。

## OsdData 字段

| 字段 | 类型 | 说明 |
|------|------|------|
| latitude | double | 纬度（WGS84） |
| longitude | double | 经度（WGS84） |
| altitude | double | 相对高度（米） |
| height | double | 离地高度（米） |
| speed | double | 地速（m/s） |
| batteryPercent | int | 电量百分比 |
| yaw | double | 航向角 |
| pitch | double | 俯仰角 |
| roll | double | 横滚角 |

## 处理频率与性能

OSD 推送频率高（1-5Hz），处理逻辑必须轻量：

1. **禁止阻塞**：`@EventListener` 方法中不可做数据库写入等耗时操作
2. **异步处理**：如需持久化，使用 `@Async` 或放入消息队列
3. **采样降频**：如业务不需要全量数据，可按时间间隔采样

```java
// 降频示例：每 2 秒更新一次
private final ConcurrentHashMap<String, Instant> lastUpdateMap = new ConcurrentHashMap<>();
private static final Duration MIN_INTERVAL = Duration.ofSeconds(2);

@EventListener
public void onOsdUpdate(OsdUpdateEvent event) {
    Instant now = clock.instant();
    Instant last = lastUpdateMap.get(event.deviceSn());
    if (last != null && Duration.between(last, now).compareTo(MIN_INTERVAL) < 0) {
        return; // 跳过，未到采样间隔
    }
    lastUpdateMap.put(event.deviceSn(), now);
    // 执行实际处理
}
```

## 多设备场景

同时在线多台设备时，每台设备独立推送 OSD。使用 `ConcurrentHashMap<String, T>` 按 SN 隔离状态。

## 与 DeviceStatusService 的关系

skeleton 内置的 `DeviceStatusService` 已实现 OSD 消费并维护内存快照。
ISV 如果只需要"查询设备当前状态"，直接调用 `DeviceStatusService.findBySn(sn)` 即可，无需重复订阅。
