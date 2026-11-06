// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.module;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.FlightHubSDK;
import com.dji.flighthubsdk.bean.CreateDeviceLogReportRequestParam;
import com.dji.flighthubsdk.bean.CreateDeviceLogReportResult;
import com.dji.flighthubsdk.bean.Device;
import com.dji.flighthubsdk.bean.DeviceLogListResult;
import com.dji.flighthubsdk.bean.DeviceLogListResultFileItem;
import com.dji.flighthubsdk.bean.DeviceLogListResultLogItem;
import com.dji.flighthubsdk.bean.DeviceLogReportContactInformationParam;
import com.dji.flighthubsdk.bean.DeviceLogReportDeviceTopoItemParam;
import com.dji.flighthubsdk.bean.DeviceLogReportDeviceTopoParam;
import com.dji.flighthubsdk.bean.DeviceLogReportLogItemParam;
import com.dji.flighthubsdk.bean.DeviceLogReportLogListItemParam;
import com.dji.flighthubsdk.bean.DeviceOtaProgress;
import com.dji.flighthubsdk.bean.CommonPaginationData;
import com.dji.flighthubsdk.bean.FirmwareCompatibilityConfigItemResult;
import com.dji.flighthubsdk.bean.FirmwareCompatibilityValidateResult;
import com.dji.flighthubsdk.bean.FirmwareInfoParam;
import com.dji.flighthubsdk.bean.FirmwareItemResult;
import com.dji.flighthubsdk.bean.FirmwareListResult;
import com.dji.flighthubsdk.bean.FirmwareReleaseNoteResult;
import com.dji.flighthubsdk.bean.FirmwareStatusItem;
import com.dji.flighthubsdk.bean.FirmwareUpgradeDeviceItem;
import com.dji.flighthubsdk.bean.FirmwareUpgradeParam;
import com.dji.flighthubsdk.bean.FirmwareUpgradeProgressItem;
import com.dji.flighthubsdk.bean.FirmwareUpgradeResult;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.LogReportListResult;
import com.dji.flighthubsdk.bean.LogReportListResultDeviceLog;
import com.dji.flighthubsdk.bean.LogReportListResultDeviceUploadFilesItem;
import com.dji.flighthubsdk.bean.LogReportListResultItem;
import com.dji.flighthubsdk.bean.UploadFileResult;
import com.dji.flighthubsdk.core.FirmwareManageModule;
import com.dji.flighthubsdk.core.LogManagementModule;
import com.dji.flighthubsdk.demo.base.Menu;
import com.dji.flighthubsdk.demo.base.ModuleDemo;
import com.dji.flighthubsdk.demo.runtime.Await;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.runtime.Node;
import com.dji.flighthubsdk.demo.runtime.Subscription;
import com.dji.flighthubsdk.demo.utils.Utils;
import com.dji.flighthubsdk.generated.DeviceFileUploadProgress;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 系统管理 Demo(菜单驱动): 日志管理 + 固件管理两个功能域, 多级子菜单。
 *
 * <p>主菜单分为两个功能域:
 * <pre>
 * 主菜单
 * ├── [1] 日志管理 (LogManagementModule)
 * │   ├── [1] 查询设备日志文件列表    queryDeviceLogList
 * │   ├── [2] 创建日志报告           createDeviceLogReport(串联 [1] 的日志文件)
 * │   ├── [3] 查询日志报告列表        logReportList
 * │   ├── [4] 日志报告操作           logReportOperation(取消/删除)
 * │   ├── [5] 上传附加文件           uploadLogAppendFile
 * │   └── [6] 订阅文件上传进度       listenDeviceFileUploadProgress(WS, 推送只落日志)
 * └── [2] 固件管理 (FirmwareManageModule)
 *     ├── [1]  查询固件列表           queryFirmwareList
 *     ├── [2]  获取最新固件说明        getLatestFirmwareReleaseNote
 *     ├── [3]  检查设备固件状态        checkFirmwareStatus
 *     ├── [4]  触发固件升级           invokeFirmwareUpgrade
 *     ├── [5]  查询升级进度 (HTTP)    queryFirmwareUpgradeProgress
 *     ├── [6]  订阅升级进度 (WS)      listenDeviceOtaProgress(推送只落日志)
 *     ├── [7]  上传固件文件           uploadFirmwareFile
 *     ├── [8]  提交固件信息           submitFirmwareInfo(串联 [7] 的 objectKey)
 *     ├── [9]  发布/取消发布/删除固件  releaseFirmware / unReleaseFirmware / deleteFirmware
 *     └── [10] 查询固件兼容性配置     queryFirmwareCompatibilityConfig
 * </pre>
 *
 * <p><b>接口串联设计: </b>
 * <ul>
 *   <li>日志: [1] 查文件列表 → [2] 创建报告 (自动填充); </li>
 *   <li>固件升级: [3] 检查状态 → [4] 触发升级 → [5]/[6] 查询/订阅进度; </li>
 *   <li>固件上传发布: [7] 上传缓存 objectKey → [8] 提交信息 → [9] 发布。</li>
 * </ul>
 *
 * <p><b>SDK 入口: </b>{@code session.workspace().getLogManagementModule()} /
 * {@code session.workspace().getFirmwareManageModule()}(均挂在 Workspace 上)。
 *
 * <p><b>设计要点: </b>
 * <ul>
 *   <li>覆盖 {@code LogManagementModule} 全部 7 个公开接口 (5 个动作 + 上传进度 listen/cancel 一对)
 *       与 {@code FirmwareManageModule} 全部 13 个公开接口 (11 个动作 + OTA 进度 listen/cancel 一对); </li>
 *   <li>三态结果回显 (SUCCESS/TIMEOUT/FAILED), 超时与失败明确区分; </li>
 *   <li>结果解析只在 SUCCESS 内进行 (convert 层对 null 输入会造成非 null 空 bean, 失败时判空守卫失效); </li>
 *   <li>订阅经基类 {@code subscribe} 登记到会话 registry(demo 结束兜底退订), 正常路径按 Enter 即退订,
 *       推送只落日志文件不刷控制台; </li>
 *   <li>危险动作 (删除固件)执行前须输入 {@code yes} 二次确认; </li>
 *   <li>会话级缓存串联: 日志 [1] 查询结果 → [2] 创建报告; 固件 [7] objectKey → [8] 提交;
 *       固件 [1] 列表首条 fileId → [9] 发布/取消/删除。</li>
 * </ul>
 */
public class SystemManagementDemo extends ModuleDemo {

    /** 日志 [1]: 本次会话查到的日志文件, 供 [2] 创建报告时自动填充。 */
    private List<DeviceLogListResultFileItem> lastQueryLogFiles = null;
    /** 日志 [1]: 查询时用的设备 SN。 */
    private String lastLogQueryDeviceSn = null;

    /** 固件 [7]: 本次会话上传成功后的 objectKey, 供 [8] 提交信息时自动填充。 */
    private String lastUploadedObjectKey = null;
    /** 固件 [1]: 本次会话自动缓存的固件列表第一条 fileId(int id 转字符串), 供 [9] 发布/取消/删除操作使用。 */
    private String lastSelectedFirmwareId = null;

    @Override
    public String name() {
        return "SystemManagementDemo";
    }

    @Override
    protected void run() throws Exception {
        if (input == null) {
            DemoLog.warn(name(), "无交互输入(input=null), 无法运行菜单式系统管理 demo");
            return;
        }

        final LogManagementModule logModule = session.workspace().getLogManagementModule();
        final FirmwareManageModule fwModule = session.workspace().getFirmwareManageModule();
        if (logModule == null || fwModule == null) {
            DemoLog.error(name(), "getLogManagementModule()/getFirmwareManageModule() 返回 null"
                + "(log=" + (logModule != null) + " firmware=" + (fwModule != null) + ")");
            console("   获取 LogManagementModule/FirmwareManageModule 失败(存在 null), 无法进入本 demo 菜单");
            return;
        }

        final long actionTimeout = defaultTimeout();
        DemoLog.info(name(), "actionTimeout=" + actionTimeout + "ms");

        console("SDK 版本: " + FlightHubSDK.getVersion());

        Menu.of(this, "执行 " + name())
                .banner("=== 系统管理 (System Management) ===")
                .item("日志管理    (LogManagementModule)", () -> menuLogManagement(logModule, actionTimeout))
                .item("固件管理    (FirmwareManageModule)", () -> menuFirmwareManage(fwModule, actionTimeout))
                .exitLog("退出系统管理菜单")
                .run();
    }

    // ==================== 子菜单 [1]: 日志管理 (LogManagementModule) ====================

    private void menuLogManagement(LogManagementModule module, long timeout) {
        Menu.of(this, "日志管理 (Log Management)")
                .banner("--- 日志管理 ---")
                .header(() -> {
                    if (lastQueryLogFiles != null) {
                        console("(已查询到日志文件 " + lastQueryLogFiles.size()
                            + " 条, [2] 创建报告将自动填充)");
                    }
                })
                .item("查询设备日志文件列表  (queryDeviceLogList)", () -> logQueryDeviceLogList(module, timeout))
                .item("创建日志报告          (createDeviceLogReport, 自动填充 [1] 的日志文件)", () -> logCreateReport(module, timeout))
                .item("查询日志报告列表      (logReportList)", () -> logReportList(module, timeout))
                .item("日志报告操作          (logReportOperation: 取消/删除)", () -> logReportOperation(module, timeout))
                .item("上传附加文件          (uploadLogAppendFile)", () -> logUploadAppendFile(module, timeout))
                .item("订阅文件上传进度      (listenDeviceFileUploadProgress: WS, 推送只落日志)", () -> logListenUploadProgress(module))
                .quitWord("返回")
                .run();
    }

    private void logQueryDeviceLogList(LogManagementModule module, long timeout) {
        String deviceSn = resolveLogDeviceSn();
        if (deviceSn.isEmpty()) {
            hint("无法确定设备 SN, 请配置 log_management.device_sn");
            return;
        }
        // module 过滤取值: 0=飞机 3=机场 6=中继 9=机库(出处: 旧 demo FlightHubSDKDemo 注释"对齐司空前端
        // module_list"; SDK bean/message.h 未给出枚举定义)。不传 module_list 参数时 API 返回空列表。
        List<Object> rawModules = params.node("log_management").list("module_list");
        List<Integer> moduleList = new ArrayList<>();
        for (Object o : rawModules) {
            if (o instanceof Number) moduleList.add(((Number) o).intValue());
        }
        console("\n   查询设备日志文件: sn=" + deviceSn
            + " moduleFilter=" + (moduleList.isEmpty() ? "全部" : moduleList));
        DemoLog.info(name(), "queryDeviceLogList: sn=" + deviceSn + " modules=" + moduleList);

        final Await<FlightHubError> await = new Await<>(name() + ".queryDeviceLogList");
        final AtomicReference<List<DeviceLogListResultFileItem>> ref = new AtomicReference<>();
        Outcome o = runOutcome("查询设备日志文件列表", timeout, await, () ->
            module.queryDeviceLogList(deviceSn, moduleList, new FlightHubCallback.QueryDeviceLogListCallback() {
                @Override
                public void onResult(FlightHubError error, DeviceLogListResult result) {
                    ref.set(result != null ? result.getFiles() : null);
                    await.set(error);
                }
            })
        );
        if (o != Outcome.SUCCESS) return;

        List<DeviceLogListResultFileItem> files = ref.get() != null ? ref.get() : Collections.emptyList();
        console("   日志文件列表(共 " + files.size() + " 条):");
        for (DeviceLogListResultFileItem item : files) {
            List<DeviceLogListResultLogItem> logs =
                    item.getList() != null ? item.getList() : Collections.emptyList();
            consoleDebug("     SN=" + item.getDeviceSn() + " module=" + item.getModule()
                + " logType=" + item.getLogType() + " files=" + logs.size());
            DemoLog.debug(name(), "logFileItem: sn=" + item.getDeviceSn()
                + " module=" + item.getModule() + " files=" + logs.size());
        }
        if (!files.isEmpty()) {
            lastQueryLogFiles = files;
            lastLogQueryDeviceSn = deviceSn;
            hint("已缓存日志文件列表, [2] 创建报告将自动填充");
        } else {
            hint("未查询到日志文件, 请确认设备 SN 和模块过滤条件");
        }
    }

    private void logCreateReport(LogManagementModule module, long timeout) {
        Node cfg = params.node("log_management");
        String deviceSn = lastLogQueryDeviceSn != null ? lastLogQueryDeviceSn : resolveLogDeviceSn();
        if (deviceSn.isEmpty()) {
            hint("无法确定设备 SN, 请先执行 [1] 查询日志文件列表, 或配置 log_management.device_sn");
            return;
        }
        String deviceModelKey = cfg.str("device_model_key", "");

        DeviceLogReportDeviceTopoItemParam host = new DeviceLogReportDeviceTopoItemParam();
        host.setSn(deviceSn);
        host.setDeviceModelKey(deviceModelKey);
        DeviceLogReportDeviceTopoParam topo = new DeviceLogReportDeviceTopoParam();
        topo.setHosts(Collections.singletonList(host));

        DeviceLogReportContactInformationParam contact = new DeviceLogReportContactInformationParam();
        contact.setEmail(cfg.str("contact_email", ""));
        contact.setPhone(cfg.str("contact_phone", ""));

        List<DeviceLogReportLogListItemParam> logList = new ArrayList<>();
        if (lastQueryLogFiles != null && !lastQueryLogFiles.isEmpty()) {
            for (DeviceLogListResultFileItem fileItem : lastQueryLogFiles) {
                DeviceLogReportLogListItemParam logItem = new DeviceLogReportLogListItemParam();
                logItem.setDeviceSn(fileItem.getDeviceSn());
                logItem.setModule(fileItem.getModule());
                List<DeviceLogReportLogItemParam> logParams = new ArrayList<>();
                List<DeviceLogListResultLogItem> rawLogs =
                        fileItem.getList() != null ? fileItem.getList() : Collections.emptyList();
                for (DeviceLogListResultLogItem log : rawLogs) {
                    DeviceLogReportLogItemParam lp = new DeviceLogReportLogItemParam();
                    lp.setBootIndex(log.getBootIndex());
                    lp.setStartTime(log.getStartTime());
                    lp.setEndTime(log.getEndTime());
                    lp.setSize(log.getSize());
                    lp.setMd5(log.getMd5());
                    logParams.add(lp);
                }
                logItem.setList(logParams);
                logList.add(logItem);
            }
            hint("已从 [1] 查询结果自动填充 " + logList.size() + " 条日志文件");
        } else {
            hint("未找到已查询的日志文件(请先执行 [1]), 将创建空日志报告(logList 为空)");
        }

        // error_start_time 为 Unix 秒级时间戳 (出处: C++ message.h CreateDeviceLogReportRequestParam
        // "错误开始时间, Unix 时间戳 (秒)"; Java bean javadoc 未标单位)。配置为 0/缺省时按「当前时间前 1h」兜底。
        // 迁移留痕: 旧 demo 缺省为毫秒纪元 (单位错大 1000 倍), 已修正为秒, 且 0/负值现统一走兜底 (有意修正)。
        long errorStartTime = cfg.longVal("error_start_time", 0L);
        if (errorStartTime <= 0) {
            errorStartTime = System.currentTimeMillis() / 1000 - 3600;
        }

        CreateDeviceLogReportRequestParam param = new CreateDeviceLogReportRequestParam();
        param.setReportInformation(cfg.str("report_information", "SDK Demo 日志报告"));
        param.setErrorStartTime(errorStartTime);
        param.setDeviceTopo(topo);
        param.setReportContactInformation(contact);
        param.setLogList(logList);
        param.setCrashed(false);

        console("\n   创建日志报告: sn=" + deviceSn + " info=" + param.getReportInformation()
            + " logs=" + logList.size());
        DemoLog.info(name(), "createDeviceLogReport: sn=" + deviceSn + " logs=" + logList.size());

        final Await<FlightHubError> await = new Await<>(name() + ".createDeviceLogReport");
        final AtomicReference<CreateDeviceLogReportResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("创建日志报告", timeout, await, () ->
            module.createDeviceLogReport(param, new FlightHubCallback.CreateDeviceLogReportCallback() {
                @Override
                public void onResult(FlightHubError error, CreateDeviceLogReportResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );
        if (o != Outcome.SUCCESS) return;

        CreateDeviceLogReportResult result = ref.get();
        if (result != null) {
            console("   报告创建结果:");
            console("     reportUuid=" + result.getReportUuid());
            console("     trackNumber=" + result.getTrackNumber());
            if (result.getErrorLogList() != null && !result.getErrorLogList().isEmpty()) {
                console("     errorLogList=" + result.getErrorLogList());
            }
            DemoLog.info(name(), "createDeviceLogReport: uuid=" + result.getReportUuid());
        }
    }

    private void logReportList(LogManagementModule module, long timeout) {
        Node cfg = params.node("log_management");
        String deviceSn = resolveLogDeviceSn();
        if (deviceSn.isEmpty()) {
            hint("无法确定设备 SN, 请配置 log_management.device_sn");
            return;
        }
        int page = cfg.intVal("list_page", 1);
        int pageSize = cfg.intVal("list_page_size", 10);
        console("\n   查询日志报告列表: sn=" + deviceSn + " page=" + page + " pageSize=" + pageSize);

        final Await<FlightHubError> await = new Await<>(name() + ".logReportList");
        final AtomicReference<LogReportListResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("查询日志报告列表", timeout, await, () ->
            module.logReportList(deviceSn, page, pageSize, new FlightHubCallback.LogReportListCallback() {
                @Override
                public void onResult(FlightHubError error, LogReportListResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );
        if (o != Outcome.SUCCESS) return;

        LogReportListResult result = ref.get();
        if (result != null) {
            List<LogReportListResultItem> list =
                    result.getList() != null ? result.getList() : Collections.emptyList();
            long total = result.getPagination() != null ? result.getPagination().getTotal() : -1;
            console("   日志报告列表(本页 " + list.size()
                + " 条 / 总计 " + (total >= 0 ? total : "?") + " 条):");
            for (LogReportListResultItem item : list) {
                console("     reportUuid=" + item.getReportUuid() + " status=" + item.getStatus()
                    + " trackNumber=" + item.getTrackNumber()
                    + " info=" + item.getReportInformation());
                if (item.getDeviceLogs() != null && item.getDeviceLogs().getFiles() != null) {
                    console("       deviceLogs(" + item.getDeviceLogs().getFiles().size() + "):");
                    for (LogReportListResultDeviceLog log : item.getDeviceLogs().getFiles()) {
                        console("         deviceSn=" + log.getDeviceSn() + " module=" + log.getModule()
                            + " result=" + log.getResult() + " objectKey=" + log.getObjectKey()
                            + " listSize=" + (log.getList() != null ? log.getList().size() : 0));
                    }
                }
                if (item.getDeviceUploadFiles() != null && item.getDeviceUploadFiles().getFiles() != null) {
                    console("       deviceUploadFiles(" + item.getDeviceUploadFiles().getFiles().size() + "):");
                    for (LogReportListResultDeviceUploadFilesItem f : item.getDeviceUploadFiles().getFiles()) {
                        console("         deviceSn=" + f.getDeviceSn() + " module=" + f.getModule()
                            + " key=" + f.getKey() + " size=" + f.getSize()
                            + " downloadUrl=" + f.getDownloadUrl());
                    }
                }
                DemoLog.info(name(), "reportItem: " + item.getReportUuid()
                    + " status=" + item.getStatus());
            }
        }
    }

    private void logReportOperation(LogManagementModule module, long timeout) {
        Node cfg = params.node("log_management");
        String reportId = cfg.str("report_id", "");
        if (reportId.isEmpty()) {
            hint("log_management.report_id 未配置, 无法执行报告操作。"
                + "请在 demo_params.json 中填写目标 reportUuid。");
            return;
        }
        // operation 由 SDK 本地白名单校验, 仅接受 "cancel"/"delete"/"read"(出处: C++
        // http_interface.cpp post_log_report_operation, 非法值同步拒绝不发请求);
        // demo 提供取消/删除两种入口, "read"(标记已读)接口存在但 demo 未做交互入口。
        console("\n  --- 日志报告操作 ---");
        console("  1. 取消(cancel)   2. 删除(delete)   q. 返回");
        int c = Utils.readIntInRange(input, "请选择: ", 1, 2);
        if (c == Utils.QUIT) return;
        String operation = c == 1 ? "cancel" : "delete";

        console("   日志报告操作: operation=" + operation + " reportId=" + reportId);
        final Await<FlightHubError> await = new Await<>(name() + ".logReportOperation");
        runOutcome("日志报告操作(" + operation + ")", timeout, await, () ->
            module.logReportOperation(operation, reportId, new FlightHubCallback.LogReportOperationCallback() {
                @Override
                public void onResult(FlightHubError error) {
                    await.set(error);
                }
            })
        );
    }

    private void logUploadAppendFile(LogManagementModule module, long timeout) {
        String filePath = params.node("log_management").str("append_file_path", "");
        if (filePath.isEmpty()) {
            hint("log_management.append_file_path 未配置, 无法上传附加文件。");
            return;
        }
        console("\n   上传附加文件: " + filePath);
        final Await<FlightHubError> await = new Await<>(name() + ".uploadLogAppendFile");
        runOutcome("上传附加文件", timeout, await, () ->
            module.uploadLogAppendFile(filePath, new FlightHubCallback.UploadLogAppendFileCallback() {
                @Override
                public void onResult(FlightHubError error) {
                    await.set(error);
                }
            })
        );
    }

    private void logListenUploadProgress(LogManagementModule module) {
        String deviceSn = resolveLogDeviceSn();
        if (deviceSn.isEmpty()) {
            hint("无法确定设备 SN, 请配置 log_management.device_sn");
            return;
        }
        console("\n   订阅文件上传进度(WS): sn=" + deviceSn);
        int listenId = module.listenDeviceFileUploadProgress(deviceSn, new FlightHubCallback.DeviceFileUploadProgressCallback() {
            @Override
            public void onResult(DeviceFileUploadProgress msg) {
                if (msg == null) return;
                pushLog("listenDeviceFileUploadProgress() #DeviceFileUploadProgress, 文件上传进度: bid=" + msg.getBid() + " status=" + msg.getStatus()
                    + " result=" + msg.getResult());
            }
        });
        if (listenId == -1) {
            hint("订阅失败(listenId=-1)");
            return;
        }
        // subscribe() 已登记到会话 registry(会话结束自动退订), sub 另存一份供本方法内主动退订
        Subscription sub = subscribe("deviceFileUploadProgress", listenId,
            id -> module.cancelListenDeviceFileUploadProgress(id));
        try {
            Utils.readLine(input, "   订阅已建立(listenId=" + listenId + ", 推送只落日志文件), 按 Enter 退订...");
        } finally {
            sub.close();
            registry().remove(sub);
        }
        hint("文件上传进度订阅已退订");
    }

    // ==================== 子菜单 [2]: 固件管理 (FirmwareManageModule) ====================

    private void menuFirmwareManage(FirmwareManageModule module, long timeout) {
        Menu.of(this, "固件管理 (Firmware Management)")
                .banner("--- 固件管理 (建议顺序: 1 查列表 → 3 检查状态 → 4 触发升级 → 5/6 跟踪进度) ---")
                .header(() -> {
                    if (lastSelectedFirmwareId != null) {
                        console("(已选固件 fileId=" + lastSelectedFirmwareId
                            + ", [9] 发布/取消/删除将使用此 ID)");
                    }
                    if (lastUploadedObjectKey != null) {
                        console("(已上传固件 objectKey=" + lastUploadedObjectKey
                            + ", [8] 提交固件信息将自动填充)");
                    }
                })
                .item("查询固件列表          (queryFirmwareList, 缓存 fileId 供 [9] 使用)", () -> fwQueryList(module, timeout))
                .item("获取最新固件说明      (getLatestFirmwareReleaseNote)", () -> fwGetReleaseNote(module, timeout))
                .item("检查设备固件状态      (checkFirmwareStatus)", () -> fwCheckStatus(module, timeout))
                .item("触发固件升级          (invokeFirmwareUpgrade)", () -> fwInvokeUpgrade(module, timeout))
                .item("查询升级进度(HTTP)    (queryFirmwareUpgradeProgress)", () -> fwQueryUpgradeProgress(module, timeout))
                .item("订阅升级进度(WS)      (listenDeviceOtaProgress, 推送只落日志)", () -> fwListenOtaProgress(module))
                .item("上传固件文件          (uploadFirmwareFile, 缓存 objectKey 供 [8] 使用)", () -> fwUploadFile(module, timeout))
                .item("提交固件信息          (submitFirmwareInfo, 自动填充 [7] 的 objectKey)", () -> fwSubmitInfo(module, timeout))
                .item("发布/取消发布/删除    (releaseFirmware / unReleaseFirmware / deleteFirmware)", () -> fwReleaseOperations(module, timeout))
                .item("查询固件兼容性配置    (queryFirmwareCompatibilityConfig)", () -> fwQueryCompatibilityConfig(module, timeout))
                .quitWord("返回")
                .run();
    }

    private void fwQueryList(FirmwareManageModule module, long timeout) {
        Node cfg = params.node("firmware_manage");
        int page = cfg.intVal("list_page", 1);
        int pageSize = cfg.intVal("list_page_size", 10);
        console("\n   查询固件列表: page=" + page + " pageSize=" + pageSize);

        final Await<FlightHubError> await = new Await<>(name() + ".queryFirmwareList");
        final AtomicReference<FirmwareListResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("查询固件列表", timeout, await, () ->
            module.queryFirmwareListWithPagination(page, pageSize, new FlightHubCallback.QueryFirmwareListWithPaginationCallback() {
                @Override
                public void onResult(FlightHubError error, FirmwareListResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );
        if (o != Outcome.SUCCESS) return;

        FirmwareListResult pageResult = ref.get();
        List<FirmwareItemResult> items = pageResult != null && pageResult.getList() != null
                ? pageResult.getList() : Collections.emptyList();
        CommonPaginationData pagination = pageResult != null ? pageResult.getPagination() : null;
        if (pagination != null) {
            console("   分页: page=" + pagination.getPage() + " pageSize=" + pagination.getPageSize()
                + " total=" + pagination.getTotal());
        }
        console("   固件列表(共 " + items.size() + " 条):");
        for (FirmwareItemResult item : items) {
            console("     id=" + item.getId() + " version=" + item.getVersion()
                + " status=" + item.getStatus() + " modelKey=" + item.getDeviceModelKey()
                + " createdAt=" + item.getCreatedAt());
            DemoLog.info(name(), "firmware: id=" + item.getId() + " v=" + item.getVersion()
                + " status=" + item.getStatus());
        }
        if (!items.isEmpty()) {
            lastSelectedFirmwareId = String.valueOf(items.get(0).getId());
            hint("已缓存第一条固件 fileId=" + lastSelectedFirmwareId + ", [9] 发布/取消/删除将使用此 ID");
        } else {
            // 空列表复位缓存: 避免上一轮查询残留的 fileId 被后续 [9] 误用(对齐 FlySafeUnlockDemo 先复位范式)。
            lastSelectedFirmwareId = null;
        }
    }

    private void fwGetReleaseNote(FirmwareManageModule module, long timeout) {
        Node cfg = params.node("firmware_manage");
        List<Object> rawKeys = cfg.list("device_model_keys");
        List<String> keys = new ArrayList<>();
        for (Object o : rawKeys) {
            if (o != null && !o.toString().isEmpty()) keys.add(o.toString());
        }
        String language = cfg.str("language", "zh");
        console("\n   获取最新固件说明: modelKeys=" + keys + " language=" + language);

        final Await<FlightHubError> await = new Await<>(name() + ".getLatestFirmwareReleaseNote");
        final AtomicReference<List<FirmwareReleaseNoteResult>> ref = new AtomicReference<>();
        Outcome o = runOutcome("获取最新固件说明", timeout, await, () ->
            module.getLatestFirmwareReleaseNote(keys, language, new FlightHubCallback.GetLatestFirmwareReleaseNoteCallback() {
                @Override
                public void onResult(FlightHubError error, List<FirmwareReleaseNoteResult> notes) {
                    ref.set(notes);
                    await.set(error);
                }
            })
        );
        if (o != Outcome.SUCCESS) return;

        List<FirmwareReleaseNoteResult> notes = ref.get() != null ? ref.get() : Collections.emptyList();
        console("   固件说明(共 " + notes.size() + " 条):");
        for (FirmwareReleaseNoteResult note : notes) {
            console("     modelKey=" + note.getDeviceModelKey() + " version=" + note.getProductVersion()
                + " releasedAt=" + note.getReleasedTime());
            console("     releaseNote=" + note.getReleaseNote());
            DemoLog.info(name(), "releaseNote: " + note.getDeviceModelKey() + " v" + note.getProductVersion());
        }
    }

    private void fwCheckStatus(FirmwareManageModule module, long timeout) {
        Node cfg = params.node("firmware_manage");
        List<Object> rawSns = cfg.list("device_sns");
        List<String> sns = new ArrayList<>();
        for (Object o : rawSns) {
            if (o != null && !o.toString().isEmpty()) sns.add(o.toString());
        }
        if (sns.isEmpty()) {
            if (session.hasDock() && session.dockSn() != null) sns.add(session.dockSn());
            if (session.hasDrone() && session.droneSn() != null) sns.add(session.droneSn());
            if (!sns.isEmpty()) hint("firmware_manage.device_sns 未配置, 自动使用: " + sns);
        }
        if (sns.isEmpty()) {
            hint("无法确定设备 SN, 请配置 firmware_manage.device_sns");
            return;
        }
        // status 过滤: 此处传空列表, SDK 仍会在请求体中发送 "status": [] (C++ 层无条件序列化, 见 http_interface.cpp
        // get_check_firmware_status), 服务端对空数组的过滤行为以实际返回为准
        // (旧 demo 示例传 "in_progress" 只查升级进行中的设备)
        console("\n   检查设备固件状态: sns=" + sns);

        final Await<FlightHubError> await = new Await<>(name() + ".checkFirmwareStatus");
        final AtomicReference<List<FirmwareStatusItem>> ref = new AtomicReference<>();
        Outcome o = runOutcome("检查设备固件状态", timeout, await, () ->
            module.checkFirmwareStatus(sns, Collections.emptyList(), new FlightHubCallback.CheckFirmwareStatusCallback() {
                @Override
                public void onResult(FlightHubError error, List<FirmwareStatusItem> items) {
                    ref.set(items);
                    await.set(error);
                }
            })
        );
        if (o != Outcome.SUCCESS) return;

        List<FirmwareStatusItem> items = ref.get() != null ? ref.get() : Collections.emptyList();
        console("   设备固件状态(共 " + items.size() + " 条):");
        for (FirmwareStatusItem item : items) {
            consoleDebug("     sn=" + item.getSn() + " modelKey=" + item.getDeviceModelKey()
                + " currentVersion=" + item.getFirmwareVersion()
                + " status=" + item.getFirmwareStatus()
                + describeFirmwareStatus(item.getFirmwareStatus()));
            DemoLog.debug(name(), "firmwareStatus: sn=" + item.getSn()
                + " status=" + item.getFirmwareStatus());
        }
        if (!items.isEmpty()) hint("可进入 [4] 触发固件升级");
    }

    private void fwInvokeUpgrade(FirmwareManageModule module, long timeout) {
        Node cfg = params.node("firmware_manage");
        String deviceSn = cfg.str("upgrade_device_sn", "");
        String deviceModelKey = cfg.str("upgrade_device_model_key", "");
        if (deviceSn.isEmpty() && session.hasDock()) {
            deviceSn = session.dockSn();
            hintDebug("upgrade_device_sn 未配置, 自动使用机场 SN: " + deviceSn);
        }
        if (deviceSn == null || deviceSn.isEmpty()) {
            hint("firmware_manage.upgrade_device_sn 未配置, 无法触发升级。");
            return;
        }

        // 缺省值 1(普通升级): 旧 demo 缺省 0 不在枚举 (1=普通升级 2=一致性升级)内, 迁移时修正为 1 ——
        // 未配置 upgrade_type 时实际下发的 firmware_upgrade_type 随之由 0 变 1(有意修正, 非笔误)
        int upgradeType = cfg.intVal("upgrade_type", 1);
        if (upgradeType != 1 && upgradeType != 2) {
            hint("upgrade_type=" + upgradeType + " 不在枚举内(1=普通升级 2=一致性升级, "
                + "见 bean FirmwareUpgradeDeviceItem javadoc), 服务端可能拒绝");
        }

        FirmwareUpgradeDeviceItem deviceItem = new FirmwareUpgradeDeviceItem();
        deviceItem.setSn(deviceSn);
        deviceItem.setDeviceModelKey(deviceModelKey);
        deviceItem.setFirmwareUpgradeType(upgradeType);
        deviceItem.setProductVersion(cfg.str("upgrade_product_version", ""));

        FirmwareUpgradeParam param = new FirmwareUpgradeParam();
        param.setDevices(Collections.singletonList(deviceItem));

        console("\n   触发固件升级: sn=" + deviceSn + " modelKey=" + deviceModelKey
            + " targetVersion=" + (deviceItem.getProductVersion().isEmpty() ? "(未指定)" : deviceItem.getProductVersion())
            + " upgradeType=" + upgradeType + describeUpgradeType(upgradeType));
        if (deviceItem.getProductVersion().isEmpty()) {
            hint("upgrade_product_version 未配置: 留空时服务端行为(是否默认升级到最新)仓库内无出处, 以实际返回为准");
        }

        final Await<FlightHubError> await = new Await<>(name() + ".invokeFirmwareUpgrade");
        final AtomicReference<FirmwareUpgradeResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("触发固件升级", timeout, await, () ->
            module.invokeFirmwareUpgrade(param, new FlightHubCallback.InvokeFirmwareUpgradeCallback() {
                @Override
                public void onResult(FlightHubError error, FirmwareUpgradeResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );
        if (o != Outcome.SUCCESS) return;

        FirmwareUpgradeResult result = ref.get();
        if (result != null) {
            console("     bid=" + result.getBid() + " status=" + result.getStatus()
                + " deviceCode=" + result.getDeviceCode());
        }
        hint("升级已触发, 建议用 [5] 查询或 [6] 订阅跟踪进度");
    }

    private void fwQueryUpgradeProgress(FirmwareManageModule module, long timeout) {
        Node cfg = params.node("firmware_manage");
        String deviceSn = cfg.str("upgrade_device_sn", "");
        if (deviceSn.isEmpty() && session.hasDock()) deviceSn = session.dockSn();
        if (deviceSn == null || deviceSn.isEmpty()) {
            hint("无法确定设备 SN, 请配置 firmware_manage.upgrade_device_sn");
            return;
        }
        final String finalSn = deviceSn;
        console("\n   查询固件升级进度(HTTP): sn=" + finalSn);

        final Await<FlightHubError> await = new Await<>(name() + ".queryFirmwareUpgradeProgress");
        final AtomicReference<List<FirmwareUpgradeProgressItem>> ref = new AtomicReference<>();
        Outcome o = runOutcome("查询固件升级进度", timeout, await, () ->
            module.queryFirmwareUpgradeProgress(finalSn, new FlightHubCallback.QueryFirmwareUpgradeProgressCallback() {
                @Override
                public void onResult(FlightHubError error, List<FirmwareUpgradeProgressItem> items) {
                    ref.set(items);
                    await.set(error);
                }
            })
        );
        if (o != Outcome.SUCCESS) return;

        List<FirmwareUpgradeProgressItem> items = ref.get() != null ? ref.get() : Collections.emptyList();
        console("   升级进度(共 " + items.size() + " 条):");
        for (FirmwareUpgradeProgressItem item : items) {
            String percent = "";
            String serviceInfo = "";
            if (item.getServices() != null) {
                if (item.getServices().getProgress() != null) {
                    percent = " percent=" + item.getServices().getProgress().getPercent() + "%"
                            + " currentStep=" + item.getServices().getProgress().getCurrentStep();
                }
                serviceInfo = " serviceStatus=" + item.getServices().getStatus()
                        + " bid=" + item.getServices().getBid();
            } else if (item.getOta() != null && item.getOta().getProgress() != null) {
                percent = " percent=" + item.getOta().getProgress().getPercent() + "%";
            }
            consoleDebug("     sn=" + item.getSn()
                + " firmwareVersion=" + item.getFirmwareVersion()
                + " deviceModelKey=" + item.getDeviceModelKey()
                + " firmwareStatus=" + item.getFirmwareStatus()
                + describeFirmwareStatus(item.getFirmwareStatus())
                + " workingStatus=" + item.getWorkingStatus() + percent + serviceInfo);
            DemoLog.debug(name(), "upgradeProgress: sn=" + item.getSn()
                + " firmwareVersion=" + item.getFirmwareVersion()
                + " status=" + item.getFirmwareStatus() + percent + serviceInfo);
        }
    }

    private void fwListenOtaProgress(FirmwareManageModule module) {
        Node cfg = params.node("firmware_manage");
        String deviceSn = cfg.str("upgrade_device_sn", "");
        if (deviceSn.isEmpty() && session.hasDock()) deviceSn = session.dockSn();
        if (deviceSn == null || deviceSn.isEmpty()) {
            hint("无法确定设备 SN, 请配置 firmware_manage.upgrade_device_sn");
            return;
        }
        final String finalSn = deviceSn;
        console("\n   订阅固件升级进度(WS): sn=" + finalSn);
        int listenId = module.listenDeviceOtaProgress(finalSn, new FlightHubCallback.DeviceOtaProgressCallback() {
            @Override
            public void onResult(DeviceOtaProgress msg) {
                if (msg == null) return;
                String percent = msg.getProgress() != null
                        ? " percent=" + msg.getProgress().getPercent() + "%" : "";
                pushLog("listenDeviceOtaProgress() #DeviceOtaProgress, OTA 进度: bid=" + msg.getBid() + " status=" + msg.getStatus()
                    + " result=" + msg.getResult() + percent);
            }
        });
        if (listenId == -1) {
            hint("订阅失败(listenId=-1)");
            return;
        }
        // subscribe() 已登记到会话 registry(会话结束自动退订), sub 另存一份供本方法内主动退订
        Subscription sub = subscribe("deviceOtaProgress", listenId,
            id -> module.cancelListenDeviceOtaProgress(id));
        try {
            Utils.readLine(input, "   订阅已建立(listenId=" + listenId + ", 推送只落日志文件), 按 Enter 退订...");
        } finally {
            sub.close();
            registry().remove(sub);
        }
        hint("OTA 进度订阅已退订");
    }

    private void fwUploadFile(FirmwareManageModule module, long timeout) {
        String filePath = params.node("firmware_manage").str("firmware_file_path", "");
        if (filePath.isEmpty()) {
            hint("firmware_manage.firmware_file_path 未配置, 无法上传固件文件。");
            return;
        }
        console("\n   上传固件文件: " + filePath);
        final Await<FlightHubError> await = new Await<>(name() + ".uploadFirmwareFile");
        final AtomicReference<UploadFileResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("上传固件文件", timeout, await, () ->
            module.uploadFirmwareFile(filePath, new FlightHubCallback.UploadFirmwareFileCallback() {
                @Override
                public void onResult(FlightHubError error, UploadFileResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );
        if (o != Outcome.SUCCESS) return;

        UploadFileResult result = ref.get();
        if (result != null) {
            console("     objectKey=" + result.getObjectKey());
            console("     objectUrl=" + result.getObjectUrl());
            console("     etag=" + result.getEtag());
            DemoLog.info(name(), "uploadFirmware: objectKey=" + result.getObjectKey());
            lastUploadedObjectKey = result.getObjectKey();
            hint("objectKey 已缓存, [8] 提交固件信息将自动填充");
        }
    }

    private void fwSubmitInfo(FirmwareManageModule module, long timeout) {
        Node cfg = params.node("firmware_manage");
        String objectKey = lastUploadedObjectKey != null
                ? lastUploadedObjectKey : cfg.str("firmware_object_key", "");
        if (objectKey.isEmpty()) {
            hint("objectKey 为空：请先执行 [7] 上传固件文件, 或配置 firmware_manage.firmware_object_key。");
            return;
        }

        FirmwareInfoParam param = new FirmwareInfoParam();
        param.setObjectKey(objectKey);
        param.setDeviceModelKey(cfg.str("firmware_device_model_key", ""));
        param.setVersion(cfg.str("firmware_version", ""));
        param.setDesc(cfg.str("firmware_desc", ""));
        param.setMd5(cfg.str("firmware_md5", ""));

        console("\n   提交固件信息: objectKey=" + objectKey + " version=" + param.getVersion()
            + " modelKey=" + param.getDeviceModelKey());
        DemoLog.info(name(), "submitFirmwareInfo: objectKey=" + objectKey);

        final Await<FlightHubError> await = new Await<>(name() + ".submitFirmwareInfo");
        Outcome o = runOutcome("提交固件信息", timeout, await, () ->
            module.submitFirmwareInfo(param, new FlightHubCallback.SubmitFirmwareInfoCallback() {
                @Override
                public void onResult(FlightHubError error) {
                    await.set(error);
                }
            })
        );
        if (o == Outcome.SUCCESS) {
            hint("提交成功后可用 [1] 查询固件列表确认, 再用 [9] 发布固件");
        }
    }

    private void fwReleaseOperations(FirmwareManageModule module, long timeout) {
        String fileId = lastSelectedFirmwareId != null
                ? lastSelectedFirmwareId
                : params.node("firmware_manage").str("firmware_file_id", "");
        if (fileId.isEmpty()) {
            hint("无可操作的固件 fileId：请先执行 [1] 查询固件列表, 或配置 firmware_manage.firmware_file_id。");
            return;
        }
        console("\n  --- 固件操作 (fileId=" + fileId + ") ---");
        console("  1. 发布固件(releaseFirmware)");
        console("  2. 取消发布(unReleaseFirmware)");
        console("  3. 删除固件(deleteFirmware)");
        console("  q. 返回");
        int c = Utils.readIntInRange(input, "请选择: ", 1, 3);
        if (c == Utils.QUIT) return;

        final String finalFileId = fileId;
        switch (c) {
            case 1: {
                // 首次发布服务端会同步拆包上传小包, SDK HTTP 超时 90s(出处: FirmwareManageModule.releaseFirmware
                // @implNote), 故等待超时取 max(timeout, 90s)
                console("   发布固件: fileId=" + finalFileId + "(注：首次发布服务端会拆包, HTTP 超时 90s)");
                final Await<FlightHubError> await = new Await<>(name() + ".releaseFirmware");
                runOutcome("发布固件", Math.max(timeout, 90_000), await, () ->
                    module.releaseFirmware(finalFileId, new FlightHubCallback.ReleaseFirmwareCallback() {
                        @Override
                        public void onResult(FlightHubError e) {
                            await.set(e);
                        }
                    })
                );
                break;
            }
            case 2: {
                console("   取消发布固件: fileId=" + finalFileId);
                final Await<FlightHubError> await = new Await<>(name() + ".unReleaseFirmware");
                runOutcome("取消发布固件", timeout, await, () ->
                    module.unReleaseFirmware(finalFileId, new FlightHubCallback.UnReleaseFirmwareCallback() {
                        @Override
                        public void onResult(FlightHubError e) {
                            await.set(e);
                        }
                    })
                );
                break;
            }
            case 3: {
                String line = Utils.readLine(input,
                        "\n   警告: 即将执行「删除固件 fileId=" + finalFileId
                            + "」\n   输入 yes 确认, 其他取消: ");
                if (line == null || !line.equalsIgnoreCase("yes")) {
                    hint("已取消");
                    return;
                }
                final Await<FlightHubError> await = new Await<>(name() + ".deleteFirmware");
                runOutcome("删除固件", timeout, await, () ->
                    module.deleteFirmware(finalFileId, new FlightHubCallback.DeleteFirmwareCallback() {
                        @Override
                        public void onResult(FlightHubError e) {
                            await.set(e);
                        }
                    })
                );
                break;
            }
            default: break;
        }
    }

    private void fwQueryCompatibilityConfig(FirmwareManageModule module, long timeout) {
        console("\n   查询固件兼容性配置...");
        final Await<FlightHubError> await = new Await<>(name() + ".queryFirmwareCompatibilityConfig");
        final AtomicReference<List<FirmwareCompatibilityConfigItemResult>> ref = new AtomicReference<>();
        Outcome o = runOutcome("查询固件兼容性配置", timeout, await, () ->
            module.queryFirmwareCompatibilityConfig(new FlightHubCallback.QueryFirmwareCompatibilityConfigCallback() {
                @Override
                public void onResult(FlightHubError error, List<FirmwareCompatibilityConfigItemResult> items) {
                    ref.set(items);
                    await.set(error);
                }
            })
        );
        if (o != Outcome.SUCCESS) return;

        List<FirmwareCompatibilityConfigItemResult> items =
                ref.get() != null ? ref.get() : Collections.emptyList();
        console("   兼容性配置(共 " + items.size() + " 条):");
        for (FirmwareCompatibilityConfigItemResult item : items) {
            console("     key=" + item.getKey()
                + " userType=" + item.getUserType()
                + " description=" + item.getDescription());
            for (FirmwareCompatibilityValidateResult validate : item.getValidates()) {
                console("       validate field=" + validate.getField()
                    + " compare=" + validate.getCompare()
                    + " description=" + validate.getDescription()
                    + " value=" + validate.getValue());
            }
            DemoLog.info(name(), "compatibilityConfig: key=" + item.getKey()
                + " userType=" + item.getUserType()
                + " description=" + item.getDescription()
                + " validates=" + item.getValidates());
        }
    }

    // ==================== 辅助方法 ====================

    /** 解析日志设备 SN：配置 {@code log_management.device_sn} 优先, 未配置时回退拓扑主设备 SN, 再回退 session dock/飞机 SN。 */
    private String resolveLogDeviceSn() {
        String sn = params.node("log_management").str("device_sn", "");
        if (!sn.isEmpty()) return sn;
        // base_station 拓扑下 dock 为子设备, 日志查询需主设备 SN
        Device mainDevice = session.topo().getDevice();
        if (mainDevice != null && mainDevice.getDeviceSn() != null && !mainDevice.getDeviceSn().isEmpty()) {
            hintDebug("log_management.device_sn 未配置, 自动使用拓扑主设备 SN: " + mainDevice.getDeviceSn());
            return mainDevice.getDeviceSn();
        }
        if (session.hasDock() && session.dockSn() != null && !session.dockSn().isEmpty()) {
            hintDebug("log_management.device_sn 未配置, 自动使用机场 SN: " + session.dockSn());
            return session.dockSn();
        }
        if (session.hasDrone() && session.droneSn() != null && !session.droneSn().isEmpty()) {
            hintDebug("log_management.device_sn 未配置, 自动使用飞机 SN: " + session.droneSn());
            return session.droneSn();
        }
        return "";
    }

    /**
     * 固件状态枚举描述。映射(1=无需升级 2=待升级 3=一致性升级 4=固件升级中)对齐 OTA 协议,
     * 与 C++ message.h FirmwareStatusItem.firmware_status 及 bean Javadoc 一致;
     * 两个调用点(FirmwareStatusItem / FirmwareUpgradeProgressItem)共用该套取值。
     */
    private static String describeFirmwareStatus(int status) {
        switch (status) {
            case 1: return "(无需升级)";
            case 2: return "(待升级)";
            case 3: return "(一致性升级)";
            case 4: return "(固件升级中)";
            default: return "";
        }
    }

    /** 固件升级类型枚举描述 (出处: bean {@code FirmwareUpgradeDeviceItem} javadoc "枚举: 1=普通升级, 2=一致性升级")。 */
    private static String describeUpgradeType(int type) {
        switch (type) {
            case 1: return "(普通升级)";
            case 2: return "(一致性升级)";
            default: return "(不在枚举内)";
        }
    }

}
