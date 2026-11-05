// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.module;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.AutoRecordConfig;
import com.dji.flighthubsdk.bean.AutoRecordItem;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.SaveAutoRecordConfigParams;
import com.dji.flighthubsdk.core.AutoRecordModule;
import com.dji.flighthubsdk.demo.base.ModuleDemo;
import com.dji.flighthubsdk.demo.runtime.Await;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.runtime.Node;
import com.dji.flighthubsdk.demo.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 自动录制配置 Demo(菜单驱动)：获取/保存自动录制配置。
 *
 * <p>对应 {@code AutoRecordModule} 全部 2 个接口：
 *
 * <pre>
 * 主菜单
 * ├── [1] 获取自动录制配置    getAutoRecordConfig
 * └── [2] 保存自动录制配置    saveAutoRecordConfig(为当前设备开启自动录制)
 * </pre>
 *
 * <p><b>SDK 入口：</b>{@code workspace.getAutoRecordModule()}——项目级配置模块(PAAS 路径无
 * project_id, 服务端硬编码默认值), 无设备/session 上下文, 与 {@code FlashLedModule} 同构。
 * 本 demo 仍走标准设备会话入口(入口自动先拉设备列表并选设备), 仅借用会话拿到的设备 SN
 * 作为 [2] 保存配置时的录制目标, 模块本身不依赖该设备。
 */
public class AutoRecordDemo extends ModuleDemo {

    @Override
    public String name() {
        return "AutoRecordDemo";
    }

    @Override
    protected void run() throws Exception {
        if (input == null) {
            DemoLog.warn(name(), "无交互输入(input=null), 无法运行菜单式自动录制配置 demo");
            return;
        }
        final AutoRecordModule module = session.workspace().getAutoRecordModule();
        if (module == null) {
            hint("获取 AutoRecordModule 失败(返回 null), 无法进入本 demo 菜单");
            return;
        }
        final long actionTimeout = timeout("default_callback_timeout_ms");
        DemoLog.info(name(), "actionTimeout=" + actionTimeout + "ms");

        while (true) {
            Utils.printTitle("执行 " + name());
            System.out.print(menu());
            int choice = Utils.readIntInRange(input, "请选择(1-2, q 退出): ", 1, 2);
            if (choice == Utils.QUIT) {
                DemoLog.info(name(), "退出自动录制配置菜单");
                break;
            }
            try {
                switch (choice) {
                    case 1: getAutoRecordConfig(module, actionTimeout); break;
                    case 2: saveAutoRecordConfig(module, actionTimeout); break;
                    default: break;
                }
            } catch (Exception e) {
                System.out.println("   结果 -> [异常] " + Utils.describe(e));
                DemoLog.error(name(), "菜单动作执行异常: " + Utils.describe(e), e);
            }
        }
    }

    // ==================== [1] 获取自动录制配置 (getAutoRecordConfig) ====================

    private void getAutoRecordConfig(AutoRecordModule module, long timeout) {
        final Await<FlightHubError> await = new Await<>(name() + ".getAutoRecordConfig");
        final AtomicReference<AutoRecordConfig> ref = new AtomicReference<>();
        Outcome o = runOutcome("获取自动录制配置", timeout, await, () ->
            module.getAutoRecordConfig(new FlightHubCallback.AutoRecordConfigCallback() {
                @Override
                public void onResult(FlightHubError error, AutoRecordConfig config) {
                    ref.set(config);
                    await.set(error);
                }
            })
        );

        if (o == Outcome.SUCCESS) {
            AutoRecordConfig config = ref.get();
            if (config == null) {
                System.out.println("   配置为 null");
                return;
            }
            System.out.println("   id=" + config.getId() + "  projectId=" + config.getProjectId()
                + "  autoCleaningTime=" + config.getAutoCleaningTime()
                + "  disabledAutoClean=" + config.isDisabledAutoClean());
            System.out.println("   disableDockRecording=" + config.isDisableDockRecording()
                + "  disableDroneRecording=" + config.isDisableDroneRecording()
                + "  disableBaseStationRecording=" + config.isDisableBaseStationRecording()
                + "  disableRtmpRecording=" + config.isDisableRtmpRecording());
            List<AutoRecordItem> items = config.getRecordingItems();
            System.out.println("   录制目标数=" + (items != null ? items.size() : 0));
            if (items != null) {
                for (int i = 0; i < items.size(); i++) {
                    AutoRecordItem it = items.get(i);
                    System.out.println("   [" + (i + 1) + "] sn=" + it.getSn()
                        + "  cameraIndex=" + it.getCameraIndex() + "  cameraSn=" + it.getCameraSn()
                        + "  recordingStrategy=" + it.getRecordingStrategy());
                }
            }
        }
    }

    // ==================== [2] 保存自动录制配置 (saveAutoRecordConfig) ====================

    private void saveAutoRecordConfig(AutoRecordModule module, long timeout) {
        final String sn = session.droneSn();
        if (sn == null || sn.isEmpty()) {
            hint("当前 session 未绑定设备 SN, 无法为设备开启自动录制, 跳过");
            return;
        }
        Node cfg = params.node("auto_record");
        int autoCleaningTime = cfg.intVal("auto_cleaning_time", 90);
        // 服务端校验: recording_item 必须满足 sn 非空, 且 camera_index(形如 "99-0-0")与 camera_sn 二选一。
        // camera_index 与机型强相关, 不写入配置文件, 留空时改为交互输入。
        String cameraIndex = cfg.str("camera_index", "");
        if (cameraIndex.isEmpty()) {
            String v = Utils.readLineEcho(input, "   请输入该设备的负载相机索引 camera_index(形如 99-0-0): ");
            cameraIndex = v == null ? "" : v;
        }
        if (cameraIndex.isEmpty()) {
            hint("未提供 camera_index, 服务端会判定录制项无效, 跳过本次保存");
            return;
        }

        AutoRecordItem item = new AutoRecordItem();
        item.setSn(sn);
        item.setCameraIndex(cameraIndex);
        List<AutoRecordItem> items = new ArrayList<>();
        items.add(item);

        SaveAutoRecordConfigParams params = new SaveAutoRecordConfigParams();
        params.setAutoCleaningTime(autoCleaningTime);
        params.setDisableDockRecording(cfg.bool("disable_dock_recording", true));
        params.setDisableDroneRecording(cfg.bool("disable_drone_recording", false));
        params.setDisableBaseStationRecording(cfg.bool("disable_base_station_recording", true));
        params.setDisableRtmpRecording(cfg.bool("disable_rtmp_recording", true));
        params.setRecordingItems(items);

        System.out.println("   保存自动录制配置: sn=" + sn + "  cameraIndex=" + cameraIndex
            + "  autoCleaningTime=" + autoCleaningTime);
        callOutcome("保存自动录制配置", cb -> module.saveAutoRecordConfig(params, cb), timeout);
    }

    // ==================== 辅助方法 ====================

    /** 提示信息双写：既落日志文件(带 demo 名 tag)又回显控制台, 用于前置条件不满足等需留痕的提示。 */
    protected void hint(String msg) {
        DemoLog.info(name(), msg);
        System.out.println("   " + msg);
    }

    // ==================== 菜单文案 ====================

    private String menu() {
        return "\n"
            + "  === 自动录制配置 (Auto Record) ===\n"
            + "  1.  获取自动录制配置   (getAutoRecordConfig)\n"
            + "  2.  保存自动录制配置   (saveAutoRecordConfig, 为当前设备开启自动录制)\n"
            + "\n"
            + "  q.  返回上级菜单\n";
    }
}
