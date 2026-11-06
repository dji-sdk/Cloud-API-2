# 固件升级（FirmwareManageModule）

`FirmwareManageModule` 是**工作空间级**模块（非设备级），覆盖固件查询、升级触发、进度跟踪与自定义固件包管理。

## 获取模块实例

```java
Workspace workspace = FlightHubSDK.getWorkspaceInstance();
FirmwareManageModule firmwareManageModule = workspace.getFirmwareManageModule();
```

## 核心概念

| 概念 | 说明 |
|------|------|
| `firmwareStatus` | 设备固件状态：`1`=无需升级 `2`=待升级 `3`=一致性升级 `4`=固件升级中 |
| `firmwareUpgradeType` | 升级方式：`1`=普通升级 `2`=一致性升级 |
| 一致性升级 | 网关设备（如机场）与子设备（如飞机）固件版本拉齐时使用 |
| OTA 进度 | 通过 `listenDeviceOtaProgress()` 订阅推送，或 `queryFirmwareUpgradeProgress()` 主动查询快照 |

## 标准流程

```
1. checkFirmwareStatus(deviceSns, status, cb)          — 筛出需要升级的设备
2. getLatestFirmwareReleaseNote(deviceModelKeys, lang, cb)  — 可选，确认目标版本说明
3. invokeFirmwareUpgrade(FirmwareUpgradeParam, cb)      — 触发升级
4. listenDeviceOtaProgress(sn, cb) → 保存 listenId      — 订阅实时进度
5. cancelListenDeviceOtaProgress(listenId)              — 退出时必须释放
```

> ⚠️ `listenDeviceOtaProgress` 依赖 SDK 配置中的 `notificationServerAddress`（WebSocket），未配置时接口仍返回 `listenId`，但回调不会被触发。

## 示例：触发升级 + 跟踪进度

```java
FirmwareUpgradeDeviceItem deviceItem = new FirmwareUpgradeDeviceItem();
deviceItem.setSn(targetSn);
deviceItem.setDeviceModelKey(deviceModelKey);
deviceItem.setFirmwareUpgradeType(1); // 1=普通升级 2=一致性升级
deviceItem.setProductVersion(targetVersion);

FirmwareUpgradeParam upgradeParam = new FirmwareUpgradeParam();
upgradeParam.setDevices(Collections.singletonList(deviceItem));

firmwareManageModule.invokeFirmwareUpgrade(upgradeParam, (error, result) -> {
    if (error != null && !error.isSuccess()) {
        log.error("触发固件升级失败: {}", error.getServerMessage());
        return;
    }
    int listenId = firmwareManageModule.listenDeviceOtaProgress(targetSn, msg -> {
        // msg 仅为进度变化通知，具体百分比/步骤需再调用 queryFirmwareUpgradeProgress 查询
        log.info("设备 {} OTA 进度变化", targetSn);
    });
    // listenId 必须保存，@PreDestroy 中调用 cancelListenDeviceOtaProgress(listenId) 释放
});
```

## 自定义固件包管理

除了触发官方固件升级，`FirmwareManageModule` 还支持集成商自行上传、提交并发布自定义固件包，标准流程：

```
1. uploadFirmwareFile(filePath, cb)                — 上传固件文件到对象存储，拿到 objectKey/etag
2. submitFirmwareInfo(FirmwareInfoParam, cb)        — 提交固件元数据登记（⚠️ 不返回新建固件的 ID，见下方陷阱）
3. queryFirmwareList(page, pageSize, cb)            — 查列表，从中按 version/deviceModelKey 匹配刚提交的记录，拿到 int id
4. releaseFirmware(String fileId, cb)               — 发布（需要 String fileId，即 id 转字符串）
   / unReleaseFirmware(String fileId, cb)           — 下架
   / deleteFirmware(String fileId, cb)              — 删除
5. queryFirmwareCompatibilityConfig(cb)             — 查询设备型号与固件的兼容性配置列表（可选，发布前核对）
```

### 示例：上传并提交固件信息

```java
firmwareManageModule.uploadFirmwareFile(localFilePath, (error, uploadResult) -> {
    if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS || uploadResult == null) {
        return;
    }

    FirmwareInfoParam infoParam = new FirmwareInfoParam();
    infoParam.setDeviceModelKey(deviceModelKey);
    infoParam.setVersion("01.02.0300");
    infoParam.setDesc("自定义固件说明");
    infoParam.setObjectKey(uploadResult.getObjectKey());   // 来自上一步
    infoParam.setMd5(computedMd5);                         // 需应用层自行计算文件 MD5

    firmwareManageModule.submitFirmwareInfo(infoParam, submitError -> {
        if (submitError.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
            return;
        }
        // ⚠️ submitFirmwareInfo 的回调只有 (error)，拿不到新建固件的 id，
        // 必须紧接着查列表才能找到刚提交的记录
        firmwareManageModule.queryFirmwareList(1, 50, (listError, items) -> {
            if (listError.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
                return;
            }
            for (FirmwareItemResult item : items) {
                if (deviceModelKey.equals(item.getDeviceModelKey()) && "01.02.0300".equals(item.getVersion())) {
                    String fileId = String.valueOf(item.getId());   // int → String 转换
                    firmwareManageModule.releaseFirmware(fileId, releaseError -> {
                        // ...
                    });
                    break;
                }
            }
        });
    });
});
```

### FirmwareInfoParam 字段表

| 字段 | 类型 | 说明 |
|------|------|------|
| `deviceModelKey` | String | 目标设备型号 |
| `version` | String | 固件版本号 |
| `desc` | String | 固件说明 |
| `objectKey` | String | `uploadFirmwareFile` 返回的对象键 |
| `md5` | String | 文件 MD5，需应用层自行计算 |

### queryFirmwareUpgradeProgress 返回的是列表，不是单个对象

```java
firmwareManageModule.queryFirmwareUpgradeProgress(targetSn, (error, items) -> {
    if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
        return;
    }
    // items 是 List<FirmwareUpgradeProgressItem>，一次升级可能涉及网关+子设备多条记录
    for (FirmwareUpgradeProgressItem item : items) {
        String sn = item.getSn();
        int workingStatus = item.getWorkingStatus();
        FirmwareUpgradeProgressOTA ota = item.getOta();
        if (ota != null && ota.getProgress() != null) {
            int percent = ota.getProgress().getPercent();
            int currentStep = ota.getProgress().getCurrentStep();
        }
    }
});
```

## 常见错误

- ❌ 把固件相关能力当作设备级模块（`drone.getXxx()`），实际是工作空间级 `workspace.getFirmwareManageModule()`
- ❌ 订阅 OTA 进度后不保存 `listenId`，导致无法 cancel
- ❌ 不检查 `notificationServerAddress` 配置就假设订阅能收到推送
- ❌ `unReleaseFirmware`/`deleteFirmware` 为不可逆操作，生成代码时需提示二次确认（与飞控类危险操作同等对待）
- ❌ 把 `queryFirmwareUpgradeProgress` 的回调结果当成单个 `FirmwareUpgradeProgressItem` 处理——它是 `List<FirmwareUpgradeProgressItem>`，一次升级可能同时推进多台设备（网关 + 子设备）
- ❌ 以为 `submitFirmwareInfo` 成功后能从回调里拿到新建固件的 ID——它的回调只有 `(error)`，必须紧接着 `queryFirmwareList` 按 `deviceModelKey`+`version` 反查
- ❌ `releaseFirmware`/`unReleaseFirmware`/`deleteFirmware` 的 `fileId` 参数类型是 **String**，而 `queryFirmwareList` 返回的 `FirmwareItemResult.getId()` 是 **int**——必须 `String.valueOf(...)` 转换，直接传 int 变量编译失败

## 关联

- 设备详情（型号、固件版本字段）查询属于 `workspace.getDeviceManageModule()`，与固件管理模块分离
- SDK 初始化、`notificationServerAddress` 配置见 `cloudapi2-sdk-core` Skill 的规则 1
