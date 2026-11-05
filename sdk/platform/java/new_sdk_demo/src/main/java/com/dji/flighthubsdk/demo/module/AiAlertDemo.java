// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.module;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.AiAlertAlarmInfo;
import com.dji.flighthubsdk.bean.AiAlertAlarmTarget;
import com.dji.flighthubsdk.bean.AiAlertFlightItem;
import com.dji.flighthubsdk.bean.AiAlertLocation;
import com.dji.flighthubsdk.bean.AiAlertNotifyFlightData;
import com.dji.flighthubsdk.bean.AiAlertNotifyRecordItem;
import com.dji.flighthubsdk.bean.AiAlertRecordItem;
import com.dji.flighthubsdk.bean.AiAlertRecordsNotifyResult;
import com.dji.flighthubsdk.bean.AiAlertTargetAlertInfo;
import com.dji.flighthubsdk.bean.DeleteAiAlertRecordParam;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.GetAiAlertAlgorithmSettingParam;
import com.dji.flighthubsdk.bean.GetAiAlertAlgorithmSettingResult;
import com.dji.flighthubsdk.bean.GetAiAlertDeviceSettingParam;
import com.dji.flighthubsdk.bean.GetAiAlertDeviceSettingResult;
import com.dji.flighthubsdk.bean.GetAiAlertFlightsParam;
import com.dji.flighthubsdk.bean.GetAiAlertFlightsResult;
import com.dji.flighthubsdk.bean.GetAiAlertRecordsParam;
import com.dji.flighthubsdk.bean.GetAiAlertRecordsResult;
import com.dji.flighthubsdk.bean.PostAiAlertAlgorithmSettingParam;
import com.dji.flighthubsdk.bean.PostAiAlertAlgorithmSettingResult;
import com.dji.flighthubsdk.bean.PostAiAlertDeviceSettingParam;
import com.dji.flighthubsdk.bean.PostAiAlertDeviceSettingResult;
import com.dji.flighthubsdk.core.AiAlertModule;
import com.dji.flighthubsdk.demo.base.ModuleDemo;
import com.dji.flighthubsdk.demo.runtime.Await;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.runtime.Node;
import com.dji.flighthubsdk.demo.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * AI 告警模块 Demo（菜单驱动）：设备告警配置、算法阈值设置、告警架次查询、告警明细查询、告警推送监听、告警记录删除。
 *
 * <p>对应 {@code IAIAlertModule} 的 7 个 HTTP 接口 + 1 个 WS Listen 接口：
 * <pre>
 * 主菜单
 * ├── [1] 写设备级告警配置   (postDeviceSetting)
 * ├── [2] 读设备级告警配置   (getDeviceSetting)
 * ├── [3] 写算法级告警配置   (postAlgorithmSetting)
 * ├── [4] 读算法级告警配置   (getAlgorithmSetting)
 * ├── [5] 查询架次统计       (getAiAlertFlights)
 * ├── [6] 查询告警明细       (getAiAlertRecords)
 * ├── [7] 监听告警推送       (listenAiAlertRecords)
 * └── [8] 删除告警记录       (deleteAiAlertRecord)
 * </pre>
 *
 * <p><b>SDK 入口：</b>{@code session.workspace().getAIAlertModule()}（挂在 Workspace 上）。
 */
public class AiAlertDemo extends ModuleDemo {

    @Override
    public String name() {
        return "AiAlertDemo";
    }

    @Override
    public boolean requiresDevice() {
        return false;
    }

    @Override
    protected void run() throws Exception {
        if (input == null) {
            DemoLog.warn(name(), "无交互输入(input=null), 无法运行菜单式 AI 告警 demo");
            return;
        }

        final AiAlertModule module = runtime.workspace().getAIAlertModule();
        if (module == null) {
            DemoLog.error(name(), "getAIAlertModule() 返回 null");
            System.out.println("   获取 AiAlertModule 失败(返回 null), 无法进入本 demo 菜单");
            return;
        }

        Node cfg = params.node("ai_alert");
        final String droneSn = cfg.str("drone_sn", "");
        if (droneSn.isEmpty()) {
            System.out.println("   [提示] ai_alert.drone_sn 未配置, 请在 demo_params.json 中填写目标无人机 SN。");
            return;
        }

        final long actionTimeout = timeout("default_callback_timeout_ms");
        DemoLog.info(name(), "droneSn=" + droneSn + " actionTimeout=" + actionTimeout + "ms");

        while (true) {
            Utils.printTitle("执行 " + name() + "  (无人机 SN: " + droneSn + ")");
            System.out.println("  --- AI 告警管理 ---");
            System.out.println("  1. 写设备级告警配置   (postDeviceSetting)");
            System.out.println("  2. 读设备级告警配置   (getDeviceSetting)");
            System.out.println("  3. 写算法级告警配置   (postAlgorithmSetting)");
            System.out.println("  4. 读算法级告警配置   (getAlgorithmSetting)");
            System.out.println("  5. 查询架次统计       (getAiAlertFlights)");
            System.out.println("  6. 查询告警明细       (getAiAlertRecords)");
            System.out.println("  7. 监听告警推送       (listenAiAlertRecords)");
            System.out.println("  8. 删除告警记录       (deleteAiAlertRecord)");
            System.out.println("  q. 返回上级菜单");

            int choice = Utils.readIntInRange(input, "请选择(1-8, q 退出): ", 1, 8);
            if (choice == Utils.QUIT) {
                DemoLog.info(name(), "退出 AI 告警菜单");
                break;
            }

            try {
                switch (choice) {
                    case 1: doPostDeviceSetting(module, droneSn, cfg, actionTimeout);       break;
                    case 2: doGetDeviceSetting(module, droneSn, actionTimeout);              break;
                    case 3: doPostAlgorithmSetting(module, droneSn, cfg, actionTimeout);     break;
                    case 4: doGetAlgorithmSetting(module, droneSn, cfg, actionTimeout);      break;
                    case 5: doGetAiAlertFlights(module, droneSn, cfg, actionTimeout);        break;
                    case 6: doGetAiAlertRecords(module, droneSn, cfg, actionTimeout);        break;
                    case 7: doListenAiAlertRecords(module, cfg.str("gateway_sn", droneSn), actionTimeout); break;
                    case 8: doDeleteAiAlertRecord(module, cfg, actionTimeout);               break;
                    default: break;
                }
            } catch (Exception e) {
                System.out.println("   结果 -> [异常] " + Utils.describe(e));
                DemoLog.error(name(), "菜单动作执行异常: " + Utils.describe(e), e);
            }
        }
    }

    // ==================== 工具方法 ====================

    private static List<Integer> toIntList(List<Object> raw) {
        if (raw == null || raw.isEmpty()) return Collections.emptyList();
        List<Integer> result = new ArrayList<>();
        for (Object o : raw) {
            if (o instanceof Number) result.add(((Number) o).intValue());
        }
        return result;
    }

    private static List<String> toStrList(List<Object> raw) {
        if (raw == null || raw.isEmpty()) return Collections.emptyList();
        List<String> result = new ArrayList<>();
        for (Object o : raw) {
            if (o != null) result.add(String.valueOf(o));
        }
        return result;
    }

    // ==================== [1] 写设备级告警配置 ====================

    private void doPostDeviceSetting(AiAlertModule module, String droneSn, Node cfg, long timeout) {
        boolean enableTargetAlert = cfg.bool("enable_target_alert", true);
        int alarmInterval = cfg.intVal("alarm_interval", 30);
        List<Integer> alarmMethodList = toIntList(cfg.list("alarm_method"));
        if (alarmMethodList.isEmpty()) {
            alarmMethodList = new ArrayList<>();
            alarmMethodList.add(1);
        }

        PostAiAlertDeviceSettingParam param = new PostAiAlertDeviceSettingParam();
        param.setDroneSn(droneSn);
        param.setEnableTargetAlert(enableTargetAlert);
        AiAlertAlarmInfo alarmInfo = new AiAlertAlarmInfo();
        alarmInfo.setAlarmInterval(alarmInterval);
        alarmInfo.setAlarmMethod(alarmMethodList);
        param.setAlarmInfo(alarmInfo);

        System.out.println("\n>> [postDeviceSetting] 请求参数:");
        System.out.println("   drone_sn=" + droneSn);
        System.out.println("   enable_target_alert=" + enableTargetAlert);
        System.out.println("   alarm_info: method=" + alarmMethodList + " interval=" + alarmInterval + "s");

        final Await<FlightHubError> await = new Await<>(name() + ".postDeviceSetting");
        final AtomicReference<PostAiAlertDeviceSettingResult> ref = new AtomicReference<>();
        Outcome outcome = runOutcome("postDeviceSetting", timeout, await, () ->
            module.postDeviceSetting(param, new FlightHubCallback.PostAiAlertDeviceSettingCallback() {
                @Override
                public void onResult(FlightHubError error, PostAiAlertDeviceSettingResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );

        if (outcome == Outcome.SUCCESS) {
            System.out.println("   响应: 写入成功(result 为空体, 成功由 err_code=0 确认)");
        }
    }

    // ==================== [2] 读设备级告警配置 ====================

    private void doGetDeviceSetting(AiAlertModule module, String droneSn, long timeout) {
        GetAiAlertDeviceSettingParam param = new GetAiAlertDeviceSettingParam();
        param.setDroneSn(droneSn);

        System.out.println("\n>> [getDeviceSetting] 请求参数:");
        System.out.println("   drone_sn=" + droneSn);

        final Await<FlightHubError> await = new Await<>(name() + ".getDeviceSetting");
        final AtomicReference<GetAiAlertDeviceSettingResult> ref = new AtomicReference<>();
        Outcome outcome = runOutcome("getDeviceSetting", timeout, await, () ->
            module.getDeviceSetting(param, new FlightHubCallback.GetAiAlertDeviceSettingCallback() {
                @Override
                public void onResult(FlightHubError error, GetAiAlertDeviceSettingResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );

        GetAiAlertDeviceSettingResult r = ref.get();
        if (outcome == Outcome.SUCCESS && r != null) {
            System.out.println("   响应:");
            System.out.println("     drone_sn=" + r.getDroneSn());
            System.out.println("     enable_target_alert=" + r.isEnableTargetAlert());
            AiAlertAlarmInfo info = r.getAlarmInfo();
            if (info != null) {
                System.out.println("     alarm_info: method=" + info.getAlarmMethod() + " interval=" + info.getAlarmInterval() + "s");
            } else {
                System.out.println("     alarm_info: (null, 设备可能尚未配置)");
            }
            DemoLog.info(name(), "getDeviceSetting: sn=" + r.getDroneSn()
                + " enable=" + r.isEnableTargetAlert());
        }
    }

    // ==================== [3] 写算法级告警配置 ====================

    private void doPostAlgorithmSetting(AiAlertModule module, String droneSn, Node cfg, long timeout) {
        int algorithmType = cfg.intVal("algorithm_type", 0);
        String algorithmName = cfg.str("algorithm_name", "");

        PostAiAlertAlgorithmSettingParam param = new PostAiAlertAlgorithmSettingParam();
        param.setDroneSn(droneSn);
        param.setAlgorithmType(algorithmType);
        param.setAlgorithmName(algorithmName);

        List<AiAlertAlarmTarget> targets = new ArrayList<>();
        int targetType = cfg.intVal("target_type", 0);
        int targetValue = cfg.intVal("target_value", 80);
        AiAlertAlarmTarget target = new AiAlertAlarmTarget();
        target.setTargetType(targetType);
        target.setTargetValue(targetValue);
        target.setUseMinThreshold(cfg.bool("use_min_threshold", false));
        target.setUseMaxThreshold(cfg.bool("use_max_threshold", false));
        target.setTargetMinThreshold(cfg.intVal("target_min_threshold", 0));
        target.setTargetMaxThreshold(cfg.intVal("target_max_threshold", 100));
        targets.add(target);
        param.setAlarmTargets(targets);

        System.out.println("\n>> [postAlgorithmSetting] 请求参数:");
        System.out.println("   drone_sn=" + droneSn);
        System.out.println("   algorithm_type=" + algorithmType + " algorithm_name=" + algorithmName);
        System.out.println("   alarm_targets[0]: type=" + targetType + " value=" + targetValue
            + " min_threshold=" + target.getTargetMinThreshold()
            + " max_threshold=" + target.getTargetMaxThreshold());

        final Await<FlightHubError> await = new Await<>(name() + ".postAlgorithmSetting");
        final AtomicReference<PostAiAlertAlgorithmSettingResult> ref = new AtomicReference<>();
        Outcome outcome = runOutcome("postAlgorithmSetting", timeout, await, () ->
            module.postAlgorithmSetting(param, new FlightHubCallback.PostAiAlertAlgorithmSettingCallback() {
                @Override
                public void onResult(FlightHubError error, PostAiAlertAlgorithmSettingResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );

        if (outcome == Outcome.SUCCESS) {
            System.out.println("   响应: 写入成功");
        }
    }

    // ==================== [4] 读算法级告警配置 ====================

    private void doGetAlgorithmSetting(AiAlertModule module, String droneSn, Node cfg, long timeout) {
        int algorithmType = cfg.intVal("algorithm_type", 0);

        GetAiAlertAlgorithmSettingParam param = new GetAiAlertAlgorithmSettingParam();
        param.setDroneSn(droneSn);
        param.setAlgorithmType(algorithmType);

        System.out.println("\n>> [getAlgorithmSetting] 请求参数:");
        System.out.println("   drone_sn=" + droneSn + " algorithm_type=" + algorithmType);

        final Await<FlightHubError> await = new Await<>(name() + ".getAlgorithmSetting");
        final AtomicReference<GetAiAlertAlgorithmSettingResult> ref = new AtomicReference<>();
        Outcome outcome = runOutcome("getAlgorithmSetting", timeout, await, () ->
            module.getAlgorithmSetting(param, new FlightHubCallback.GetAiAlertAlgorithmSettingCallback() {
                @Override
                public void onResult(FlightHubError error, GetAiAlertAlgorithmSettingResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );

        GetAiAlertAlgorithmSettingResult r = ref.get();
        if (outcome == Outcome.SUCCESS && r != null) {
            System.out.println("   响应:");
            System.out.println("     drone_sn=" + r.getDroneSn());
            System.out.println("     algorithm_type=" + r.getAlgorithmType() + " algorithm_name=" + r.getAlgorithmName());
            List<AiAlertAlarmTarget> targets = r.getAlarmTargets();
            if (targets != null) {
                for (int i = 0; i < targets.size(); i++) {
                    AiAlertAlarmTarget t = targets.get(i);
                    System.out.println("     alarm_targets[" + i + "]: type=" + t.getTargetType()
                        + " value=" + t.getTargetValue()
                        + " use_min=" + t.isUseMinThreshold() + " use_max=" + t.isUseMaxThreshold()
                        + " min=" + t.getTargetMinThreshold() + " max=" + t.getTargetMaxThreshold());
                }
            }
            DemoLog.info(name(), "getAlgorithmSetting: sn=" + r.getDroneSn()
                + " type=" + r.getAlgorithmType() + " targets=" + (targets != null ? targets.size() : 0));
        }
    }

    // ==================== [5] 查询架次统计 ====================

    private void doGetAiAlertFlights(AiAlertModule module, String droneSn, Node cfg, long timeout) {
        int page = cfg.intVal("page", 1);
        int pageSize = cfg.intVal("page_size", 50);
        long beginAt = cfg.longVal("begin_at", 0);
        long endAt = cfg.longVal("end_at", 0);

        GetAiAlertFlightsParam param = new GetAiAlertFlightsParam();
        param.setDroneSn(droneSn);
        param.setPage(page);
        param.setPageSize(pageSize);
        param.setBeginAt(beginAt);
        param.setEndAt(endAt);
        param.setAlgorithmSources(toIntList(cfg.list("algorithm_sources")));

        System.out.println("\n>> [getAiAlertFlights] 请求参数:");
        System.out.println("   drone_sn=" + droneSn + " page=" + page + " page_size=" + pageSize);
        if (beginAt > 0) System.out.println("   begin_at=" + beginAt);
        if (endAt > 0)   System.out.println("   end_at=" + endAt);

        final Await<FlightHubError> await = new Await<>(name() + ".getAiAlertFlights");
        final AtomicReference<GetAiAlertFlightsResult> ref = new AtomicReference<>();
        Outcome outcome = runOutcome("getAiAlertFlights", timeout, await, () ->
            module.getAiAlertFlights(param, new FlightHubCallback.GetAiAlertFlightsCallback() {
                @Override
                public void onResult(FlightHubError error, GetAiAlertFlightsResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );

        GetAiAlertFlightsResult r = ref.get();
        if (outcome == Outcome.SUCCESS && r != null) {
            System.out.println("   响应: total=" + r.getTotal() + " page=" + r.getPage()
                + "/" + r.getPageCount() + " pageSize=" + r.getPageSize());
            List<AiAlertFlightItem> flights = r.getData();
            if (flights != null && !flights.isEmpty()) {
                for (int i = 0; i < flights.size(); i++) {
                    AiAlertFlightItem f = flights.get(i);
                    System.out.println("     [" + i + "] flight_id=" + f.getFlightId()
                        + " count=" + f.getCount()
                        + " task_name=" + f.getFlightTaskName()
                        + " status=" + f.getStatus()
                        + " start_time=" + f.getStartTime()
                        + " flight_task_type=" + f.getFlightTaskType()
                        + " is_commented=" + f.isCommented());
                }
            } else {
                System.out.println("     (无架次数据)");
            }
            DemoLog.info(name(), "getAiAlertFlights: total=" + r.getTotal()
                + " flights=" + (flights != null ? flights.size() : 0));
        }
    }

    // ==================== [6] 查询告警明细 ====================

    private void doGetAiAlertRecords(AiAlertModule module, String droneSn, Node cfg, long timeout) {
        int page = cfg.intVal("records_page", 1);
        int pageSize = cfg.intVal("records_page_size", 50);
        long beginAt = cfg.longVal("begin_at", 0);
        long endAt = cfg.longVal("end_at", 0);

        GetAiAlertRecordsParam param = new GetAiAlertRecordsParam();
        param.setFlightId(toStrList(cfg.list("flight_id")));
        param.setDroneSn(toStrList(cfg.list("records_drone_sn")));
        param.setTargetType(toIntList(cfg.list("records_target_type")));
        param.setAlgorithmSources(toIntList(cfg.list("algorithm_sources")));
        param.setBeginAt(beginAt);
        param.setEndAt(endAt);
        param.setPage(page);
        param.setPageSize(pageSize);

        System.out.println("\n>> [getAiAlertRecords] 请求参数:");
        System.out.println("   flight_id=" + param.getFlightId());
        System.out.println("   drone_sn_filter=" + param.getDroneSn());
        System.out.println("   page=" + page + " page_size=" + pageSize);
        if (beginAt > 0) System.out.println("   begin_at=" + beginAt);
        if (endAt > 0)   System.out.println("   end_at=" + endAt);

        if (param.getFlightId() == null || param.getFlightId().isEmpty()) {
            System.out.println("   [提示] flight_id 为空, 该参数必填(先调 [5] 查询架次获取 flight_id)。");
        }

        final Await<FlightHubError> await = new Await<>(name() + ".getAiAlertRecords");
        final AtomicReference<GetAiAlertRecordsResult> ref = new AtomicReference<>();
        Outcome outcome = runOutcome("getAiAlertRecords", timeout, await, () ->
            module.getAiAlertRecords(param, new FlightHubCallback.GetAiAlertRecordsCallback() {
                @Override
                public void onResult(FlightHubError error, GetAiAlertRecordsResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );

        GetAiAlertRecordsResult r = ref.get();
        if (outcome == Outcome.SUCCESS && r != null) {
            System.out.println("   响应: total=" + r.getTotal() + " page=" + r.getPage()
                + "/" + r.getPageCount() + " pageSize=" + r.getPageSize());
            Map<String, List<AiAlertRecordItem>> data = r.getData();
            if (data != null && !data.isEmpty()) {
                for (Map.Entry<String, List<AiAlertRecordItem>> entry : data.entrySet()) {
                    String flightId = entry.getKey();
                    List<AiAlertRecordItem> records = entry.getValue();
                    System.out.println("     flight_id=" + flightId + " records=" + (records != null ? records.size() : 0));
                    if (records != null) {
                        for (int i = 0; i < Math.min(records.size(), 5); i++) {
                            AiAlertRecordItem rec = records.get(i);
                            System.out.println("       [" + i + "] uuid=" + rec.getAlertUuid()
                                + " sn=" + rec.getDroneSn()
                                + " status=" + rec.getStatus()
                                + " algo_source=" + rec.getAlgorithmSource()
                                + " timestamp=" + rec.getTimestamp());
                            System.out.println("           project_id=" + rec.getProjectId()
                                + " reason=" + rec.getReason()
                                + " file_id=" + rec.getFileId()
                                + " media_index=" + rec.getMediaIndex()
                                + " interval_seconds=" + rec.getIntervalSeconds()
                                + " trigger_actions=" + rec.getTriggerActions().size());
                            AiAlertLocation loc = rec.getLocation();
                            if (loc != null) {
                                System.out.println("           location: lat=" + loc.getLatitude()
                                    + " lng=" + loc.getLongitude() + " alt=" + loc.getAltitude());
                            }
                            List<AiAlertTargetAlertInfo> infos = rec.getTargetAlertInfos();
                            if (infos != null && !infos.isEmpty()) {
                                for (AiAlertTargetAlertInfo info : infos) {
                                    System.out.println("           target: type=" + info.getTargetType()
                                        + " value=" + info.getTargetValue()
                                        + " label=" + info.getLabel()
                                        + " use_min_threshold=" + info.isUseMinThreshold()
                                        + " use_max_threshold=" + info.isUseMaxThreshold()
                                        + " target_max_threshold=" + info.getTargetMaxThreshold()
                                        + " target_min_threshold=" + info.getTargetMinThreshold());
                                }
                            }
                            if (!rec.getThumbnailUrl().isEmpty()) {
                                System.out.println("           thumbnail_url=" + rec.getThumbnailUrl());
                            }
                        }
                        if (records.size() > 5) {
                            System.out.println("       ... 省略 " + (records.size() - 5) + " 条");
                        }
                    }
                }
            } else {
                System.out.println("     (无告警记录)");
            }
            DemoLog.info(name(), "getAiAlertRecords: total=" + r.getTotal()
                + " groups=" + (data != null ? data.size() : 0));
        }
    }

    // ==================== [7] 监听告警推送 ====================

    private void doListenAiAlertRecords(AiAlertModule module, String gatewaySn, long timeout) {
        System.out.println("\n>> [listenAiAlertRecords] 订阅告警推送:");
        System.out.println("   device_sn=" + gatewaySn + "  (AI 告警推送按机场/网关 SN 路由, 非无人机 SN)");
        System.out.println("   topic=alert_records.alert_records_notify");

        final Await<Integer> trigger = new Await<>(name() + ".aiAlertRecordsTrigger");

        int listenId = module.listenAiAlertRecords(gatewaySn, new FlightHubCallback.AiAlertRecordsNotifyCallback() {
            @Override
            public void onResult(AiAlertRecordsNotifyResult result) {
                if (result == null) return;
                List<AiAlertNotifyFlightData> flights = result.getData();
                System.out.println("\n   [告警推送] 收到推送: flights=" + (flights != null ? flights.size() : 0));
                if (flights != null) {
                    for (AiAlertNotifyFlightData fd : flights) {
                        System.out.println("     flight_id=" + fd.getFlightId()
                            + " task_name=" + fd.getFlightTaskName()
                            + " records=" + (fd.getRecords() != null ? fd.getRecords().size() : 0));
                        if (fd.getRecords() != null) {
                            for (int i = 0; i < Math.min(fd.getRecords().size(), 3); i++) {
                                AiAlertNotifyRecordItem rec = fd.getRecords().get(i);
                                System.out.println("       [" + i + "] uuid=" + rec.getAlertUuid()
                                    + " sn=" + rec.getDroneSn()
                                    + " status=" + rec.getStatus()
                                    + " ts=" + rec.getTimestamp());
                            }
                        }
                    }
                }
                DemoLog.info("AiAlertDemo", "listenAiAlertRecords: push received, flights="
                    + (flights != null ? flights.size() : 0));
                trigger.set(1);
            }
        });

        if (listenId < 0) {
            System.out.println("   订阅失败(listenId=" + listenId + ")");
            return;
        }

        System.out.println("   订阅成功: listenId=" + listenId);
        System.out.println("   等待推送中(最多 5 分钟, 收到推送或超时后自动继续)...");
        try { trigger.await(300_000); } catch (Exception ignore) {}
        Utils.readLine(input, "   按 Enter 退订...");
        module.cancelListenAiAlertRecords(listenId);
        System.out.println("   已退订(listenId=" + listenId + ")");
        DemoLog.info(name(), "cancelListenAiAlertRecords: listenId=" + listenId);
    }

    // ==================== [8] 删除告警记录 ====================

    private void doDeleteAiAlertRecord(AiAlertModule module, Node cfg, long timeout) {
        String defaultAlertUuid = cfg.str("delete_alert_uuid", "");
        String defaultFlightId = cfg.str("delete_flight_id", "");

        System.out.println("\n>> [deleteAiAlertRecord] 删除告警记录（不可逆）:");
        System.out.println("   alert_uuid 非空时精确删除单条(优先); 为空时按 flight_id 批量删除该架次全部记录。");
        String alertUuid = Utils.readLineEcho(input, "   alert_uuid (留空跳过, 默认=" + defaultAlertUuid + "): ");
        if (alertUuid == null || alertUuid.isEmpty()) alertUuid = defaultAlertUuid;
        String flightId = Utils.readLineEcho(input, "   flight_id (留空跳过, 默认=" + defaultFlightId + "): ");
        if (flightId == null || flightId.isEmpty()) flightId = defaultFlightId;

        if (alertUuid.isEmpty() && flightId.isEmpty()) {
            System.out.println("   [提示] alert_uuid 与 flight_id 均为空, 无法确定删除范围, 已取消。");
            return;
        }

        String scopeDesc = !alertUuid.isEmpty()
            ? "精确删除单条 alert_uuid=" + alertUuid
            : "按架次批量删除 flight_id=" + flightId + " 下全部告警记录";
        System.out.println("   即将 " + scopeDesc);
        String confirm = Utils.readLine(input, "   确认删除? (y/N): ");
        if (confirm == null || !confirm.trim().equalsIgnoreCase("y")) {
            System.out.println("   已取消删除。");
            return;
        }

        DeleteAiAlertRecordParam param = new DeleteAiAlertRecordParam();
        param.setAlertUuid(alertUuid);
        param.setFlightId(flightId);

        System.out.println("\n>> [deleteAiAlertRecord] 请求参数:");
        System.out.println("   alert_uuid=" + alertUuid + " flight_id=" + flightId);

        final Await<FlightHubError> await = new Await<>(name() + ".deleteAiAlertRecord");
        Outcome outcome = runOutcome("deleteAiAlertRecord", timeout, await, () ->
            module.deleteAiAlertRecord(param, new FlightHubCallback.ResultCallbackWithError() {
                @Override
                public void onResult(FlightHubError error) {
                    await.set(error);
                }
            })
        );

        if (outcome == Outcome.SUCCESS) {
            System.out.println("   响应: 删除成功(响应体为空, 成功由 err_code=0 确认)");
        }
        DemoLog.info(name(), "deleteAiAlertRecord: alert_uuid=" + alertUuid
            + " flight_id=" + flightId + " outcome=" + outcome);
    }
}
