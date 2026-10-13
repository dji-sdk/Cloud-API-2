# HMS 告警分级处理

HMS（Health Management System）告警是设备健康管理事件，包括硬件异常、传感器故障、环境警告等。

## 在 skeleton 中消费 HMS

```java
@EventListener
public void onHmsAlert(HmsAlertEvent event) {
    String sn = event.deviceSn();
    String code = event.alertCode();
    int level = event.level();
    String message = event.message();
    // 分级处理
}
```

## 告警等级

| 等级 | 含义 | 建议处理 |
|------|------|----------|
| 0 | 通知（Notice） | 记录日志，不告警 |
| 1 | 注意（Caution） | UI 提示，不阻断任务 |
| 2 | 警告（Warning） | 弹窗提醒，建议操作 |
| 3 | 严重（Serious） | 强制告警，可能需要中断任务 |

## 存储策略

### 内存 Bounded Deque（skeleton 默认）

```java
// 每台设备最多保留最近 N 条告警
private final ArrayDeque<HmsAlert> recentHms = new ArrayDeque<>();
private static final int MAX_HMS_PER_DEVICE = 50;

synchronized void appendHms(HmsAlert alert) {
    if (recentHms.size() >= MAX_HMS_PER_DEVICE) {
        recentHms.pollFirst(); // 淘汰最早的
    }
    recentHms.addLast(alert);
}
```

配置项：`fh.skeleton.feature.device.hms-max-per-device`（默认 50）。

### 持久化扩展

需要历史告警查询时，ISV 可：
1. 添加数据库 Entity + Repository
2. 在 `@EventListener` 中异步写入
3. 提供分页查询 REST API

## 告警去重

同一 `alertCode` 可能在短时间内重复推送（设备持续报警）。
建议对同设备+同 alertCode 做时间窗口去重：

```java
// 5 分钟内同一 alertCode 不重复记录
private static final Duration DEDUP_WINDOW = Duration.ofMinutes(5);

boolean shouldRecord(String sn, String alertCode, Instant now) {
    String key = sn + ":" + alertCode;
    Instant lastSeen = dedup.get(key);
    if (lastSeen != null && Duration.between(lastSeen, now).compareTo(DEDUP_WINDOW) < 0) {
        return false;
    }
    dedup.put(key, now);
    return true;
}
```

## 告警联动

常见联动操作：
- 等级 ≥ 2 → 发送通知（WebSocket/邮件/短信）
- 等级 = 3 → 考虑 RTH（Return To Home）或强制着陆
- 特定 alertCode → 触发维护工单
