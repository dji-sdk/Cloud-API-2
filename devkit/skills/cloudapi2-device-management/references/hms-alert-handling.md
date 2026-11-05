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

> ⚠️ **官方 `level` 只有 0~2 三档，没有等级 3**。实时推送 `HmsDataBasics.getLevel()` 与历史查询
> `HistoryHmsDataResultItem.getLevel()` 是同名同义字段，javadoc 标注取值如下：

| 等级 | 含义 | 建议处理 |
|------|------|----------|
| 0 | 通知（Notice） | 记录日志，不告警 |
| 1 | 提醒（Caution） | UI 提示，不阻断任务 |
| 2 | 警告（Warning） | 弹窗提醒；若 `inTheSky` 为真建议优先提示返航 |

实时推送的 `HmsDataBasics` **没有 `message`/`status` 字段**——告警文案需自行按 `code` 映射；
是否已恢复（`status`：`1`=live 持续中 / `2`=非 live 已结束）只有历史查询 `HistoryHmsDataResultItem` 才有。

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

需要历史告警查询时，集成商可：
1. 添加数据库 Entity + Repository
2. 在 `@EventListener` 中异步写入
3. 提供分页查询 REST API

> 也可以优先用官方 `HMSModule.queryHistoryHMSData()`（工作空间级 HTTP 分页查询）直接查历史告警，
> 不一定要自建持久化——只有需要跨 SDK 生命周期保留、或做自定义统计报表时才需要自建存储。

### HistoryHmsDataParam 字段表

```java
Workspace workspace = FlightHubSDK.getWorkspaceInstance();
HMSModule hmsModule = workspace.getHMSModule();

HistoryHmsDataParam param = new HistoryHmsDataParam();
param.setSnList(List.of(deviceSn));
param.setCreateTimeBegin(beginTimestampMs);
param.setCreateTimeEnd(endTimestampMs);
param.setPageSize(20);
param.setPageNum(1);

hmsModule.queryHistoryHMSData(param, (error, result) -> {
    if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
        return;
    }
    // result 里逐条是 HistoryHmsDataResultItem，含 status（1=live 持续中 / 2=已结束）
});
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `snList` | `List<String>` | 设备 SN 列表，支持多设备一次查询 |
| `createTimeBegin` / `createTimeEnd` | `long` | 查询时间窗口（毫秒时间戳） |
| `pageSize` | `int` | 分页大小 |
| `pageNum` | `int` | 页码 |

回调为 `(FlightHubError error, HistoryHmsDataResult result)`——error-first，与本域其他 HTTP 接口一致。

## 告警去重

**不要自己发明时间窗口去重**——SDK 已经提供官方去重键：实时推送的 `HmsDataBasics` 自带
`getStatusKey()` / `getJointKey()`，用于同一告警在多次推送间的去重与状态合并，直接拿来用即可：

```java
// 用官方 statusKey 做去重键，而不是"5 分钟内同 alertCode 不重复"这种自造规则
Map<String, Long> activeAlarms = new ConcurrentHashMap<>();

void onAlarm(String sn, HmsDataBasics b) {
    String dedupKey = sn + "#" + b.getStatusKey();
    boolean isNew = activeAlarms.putIfAbsent(dedupKey, System.currentTimeMillis()) == null;
    if (!isNew) {
        return; // 重复推送，不再触发下游动作（建单/通知）
    }
    // ... 分级处置
}
```

另外，一次推送 `HmsData.getHmsDataItems()` 中每条 `HmsDataItem` 都由**主告警** `getBasics()` 与
**子告警列表** `getSubHmsData()` 组成，子告警是主告警的细化（如具体哪颗电芯异常），遍历时两者都要处理，
不能只取 `getBasics()`。

## 告警联动

常见联动操作：
- 等级 ≥ 1 → 发送通知（WebSocket/邮件/短信）
- 等级 = 2 且 `inTheSky` 为真 → 优先提示返航（Return To Home）
- 特定 alertCode → 触发维护工单

