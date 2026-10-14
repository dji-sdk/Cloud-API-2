# 设备生命周期

DJI CloudAPI2 SDK 中的设备生命周期由 kernel-spi 事件驱动，ISV 通过 `@EventListener` 消费。

## 生命周期 6 步

```
1. 设备通电/开机
   ↓
2. SDK 连接建立 → DeviceOnlineEvent
   - deviceSn: 设备序列号
   - deviceType: DRONE / DOCK / REMOTE_CONTROLLER
   - deviceModel: 设备型号字符串
   - deviceName: 设备名称
   ↓
3. OSD 数据推送 → OsdUpdateEvent（持续，1-5Hz）
   - deviceSn: 设备序列号
   - osd: OsdData（经纬度、高度、电量、速度、姿态...）
   ↓
4. HMS 告警 → HmsAlertEvent（偶发）
   - deviceSn: 设备序列号
   - alertCode: 告警代码
   - level: 告警等级
   - message: 告警消息
   ↓
5. SDK 连接断开 → DeviceOfflineEvent
   - deviceSn: 设备序列号
   ↓
6. 设备关机/断电
```

## 状态存储策略

skeleton 默认使用内存存储（`ConcurrentHashMap`），适用于单实例部署。

ISV 需要持久化或多实例共享时，可：
1. 通过 `@ConditionalOnMissingBean` 替换 `DeviceStatusService`
2. 在自定义实现中使用 Redis / 数据库

## 设备 SN 唯一性

设备 SN 是全局唯一标识符，格式为大写字母+数字（如 `1ZNBJ790010XXX`）。
同一设备的所有事件通过 `deviceSn` 关联。

## 在线/离线判断

- `DeviceOnlineEvent` → 标记为在线
- `DeviceOfflineEvent` → 标记为离线（不删除 entry，保留 last known state）
- 长时间无 OSD 推送但未收到 offline 事件 → 可能网络中断，需结合业务判断

## 常见 DeviceType 枚举

| 值 | 含义 |
|----|------|
| DRONE | 无人机 |
| DOCK | 机场（机巢） |
| REMOTE_CONTROLLER | 遥控器 |
