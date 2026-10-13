// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.module;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.CloseChargeResult;
import com.dji.flighthubsdk.bean.CloseDebugModeResult;
import com.dji.flighthubsdk.bean.CloseDockCoverProgressResult;
import com.dji.flighthubsdk.bean.CloseDockCoverResult;
import com.dji.flighthubsdk.bean.CloseDroneResult;
import com.dji.flighthubsdk.bean.CloudEnvDeviceInfo;
import com.dji.flighthubsdk.bean.CloudEnvDeviceList;
import com.dji.flighthubsdk.bean.CloudEnvTargetConfig;
import com.dji.flighthubsdk.bean.CloudEnvironmentSwitchCallbackParam;
import com.dji.flighthubsdk.bean.CloudEnvironmentSwitchParam;
import com.dji.flighthubsdk.bean.CloudEnvironmentSwitchResult;
import com.dji.flighthubsdk.bean.DeviceAuthVerifyParam;
import com.dji.flighthubsdk.bean.DeviceFormatProgressResult;
import com.dji.flighthubsdk.bean.DeviceFormatResult;
import com.dji.flighthubsdk.bean.DeviceRebootProgressResult;
import com.dji.flighthubsdk.bean.DeviceRebootResult;
import com.dji.flighthubsdk.bean.DockModeCode;
import com.dji.flighthubsdk.bean.DonglePowerSwitchResult;
import com.dji.flighthubsdk.bean.DroneFormatResult;
import com.dji.flighthubsdk.bean.EsimOperatorSwitchProgressResult;
import com.dji.flighthubsdk.bean.EsimOperatorSwitchResult;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.GeoCagingInfoParam;
import com.dji.flighthubsdk.bean.LowPowerSwitchResult;
import com.dji.flighthubsdk.bean.ManualBatterySwapProgressResult;
import com.dji.flighthubsdk.bean.ManualBatterySwapResult;
import com.dji.flighthubsdk.bean.OpenChargeResult;
import com.dji.flighthubsdk.bean.OpenDebugModeResult;
import com.dji.flighthubsdk.bean.OpenDockCoverProgressResult;
import com.dji.flighthubsdk.bean.OpenDockCoverResult;
import com.dji.flighthubsdk.bean.OpenDroneResult;
import com.dji.flighthubsdk.bean.SetBatteryStoreModeResult;
import com.dji.flighthubsdk.bean.SetDistanceLimitResult;
import com.dji.flighthubsdk.bean.SetHeightLimitResult;
import com.dji.flighthubsdk.bean.SetNightLightsStateResult;
import com.dji.flighthubsdk.bean.SetObstacleAvoidanceResult;
import com.dji.flighthubsdk.bean.SetRemainingPowerForRthResult;
import com.dji.flighthubsdk.bean.SimSlotSwitchResult;
import com.dji.flighthubsdk.bean.SwitchAlarmStateResult;
import com.dji.flighthubsdk.core.DeviceDebugModule;
import com.dji.flighthubsdk.core.DockOsdModule;
import com.dji.flighthubsdk.demo.base.ModuleDemo;
import com.dji.flighthubsdk.demo.runtime.Await;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.runtime.Json;
import com.dji.flighthubsdk.demo.runtime.Node;
import com.dji.flighthubsdk.demo.runtime.Subscription;
import com.dji.flighthubsdk.demo.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 设备调试模块 Demo(菜单驱动)：机场/飞行器调试、舱盖、充电、重启/格式化、
 * 飞行参数设置(限高/限远/避障/返航电量/夜航灯/报警灯)、电池/SIM/低功耗等。
 *
 * <p>对应 {@code IDeviceDebugModule} 全部接口, 按功能域分组为 7 个子菜单：
 *
 * <pre>
 * 主菜单
 * ├── [1] 调试模式              openDebugMode / closeDebugMode
 * ├── [2] 机场舱盖              openDockCover / closeDockCover(含进度订阅)
 *                               getOpenDockCoverProgress / getCloseDockCoverProgress(HTTP查询)
 * ├── [3] 飞行器电源            openDrone / closeDrone / droneFormat / droneSdrWorkMode
 * ├── [4] 充电控制              openCharge / closeCharge
 * ├── [5] 飞行参数设置          setDistanceLimit / setHeightLimit / setObstacleAvoidance
 *                               setRemainingPowerForRth / setNightLightsState
 *                               switchAlarmLightState / setDockAirConditionerMode
 *                               setDockSilentMode
 * ├── [6] 设备维护              deviceReboot / deviceFormat(含进度订阅)
 *                               getDeviceRebootProgress / getDeviceFormatProgress(HTTP查询)
 *                               setBatteryStoreMode / startManualBatterySwap
 *                               setDonglePowerSwitch / setLowPowerSwitch / setGeoCagingEnable
 * └── [7] 4G/SIM 管理           esimOperatorSwitch / getEsimOperatorSwitchProgress
 *                               simSlotSwitch(+ 进度订阅/查询)
 * └── [8] 云环境切换            cloudEnvironmentSwitch / cloudEnvironmentSwitchCallback
 *                               (+ topic_cloud_environment_switch 进度订阅)
 * </pre>
 *
 * <p><b>接口串联设计：</b>
 * <ul>
 *   <li>需要物理设备动作的接口(开舱盖、飞行器开机、重启、格式化)均提供进度订阅(WebSocket), 
 *       demo 采用「先下发指令 → 订阅进度推送 → 等待推送结果」的模式, 比轮询 HTTP getXxxProgress
 *       更高效；进度推送 {@link Subscription} 登记到临时 registry, 动作完成后立即取消；</li>
 *   <li>开舱盖/关舱盖、飞行器开/关机、充电开/关均为「先 HTTP 下发 → 再 WS 收进度」的两阶段动作；</li>
 *   <li>飞行参数设置类接口(setHeightLimit 等)为即时 HTTP 动作, 无进度订阅；</li>
 *   <li>危险动作(格式化、重启)执行前须输入 {@code yes} 二次确认。</li>
 * </ul>
 *
 * <p><b>SDK 入口：</b>{@code session.dock().getDeviceDebugModule()}(挂在 Dock 上)。
 * 设备 SN 优先取 {@code device_debug.target_device_sn} 配置项, 未配置时自动用 session 的
 * dockSn(与 MaintenanceDemo 的回退策略一致)。
 *
 * <p><b>设计要点：</b>
 * <ul>
 *   <li>参数全部配置驱动, 无手动交互输入；</li>
 *   <li>进度类动作(开舱盖/重启/格式化)通过 WebSocket 订阅主动推送, 不轮询；</li>
 *   <li>三态结果回显(SUCCESS/TIMEOUT/FAILED), 超时与失败明确区分；</li>
 *   <li>危险动作(重启/格式化)二次确认；</li>
 *   <li>进度订阅 Subscription 随动作完成即取消, 不泄漏。</li>
 * </ul>
 */
public class DeviceDebugDemo extends ModuleDemo {

    /** 进度推送等待超时(ms), 独立于 action 超时, 给物理动作留足时间。 */
    private static final long PROGRESS_WAIT_MS = 60_000L;

    @Override
    public String name() {
        return "DeviceDebugDemo";
    }

    @Override
    protected void run() throws Exception {
        if (input == null) {
            DemoLog.warn(name(), "无交互输入(input=null), 无法运行菜单式设备调试 demo");
            return;
        }
        if (!session.hasDock()) {
            hint("当前设备无机场(Dock), 设备调试模块需要机场设备, 无法运行");
            return;
        }

        final DeviceDebugModule module = session.dock().getDeviceDebugModule();
        if (module == null) {
            DemoLog.error(name(), "getDeviceDebugModule() 返回 null");
            console("   获取 DeviceDebugModule 失败(返回 null), 无法进入本 demo 菜单");
            return;
        }

        // 设备 SN：配置优先, 回退到 session dockSn
        final String deviceSn = resolveDeviceSn();
        if (deviceSn.isEmpty()) {
            hint("无法确定目标设备 SN：请在 demo_params.json 的 device_debug.target_device_sn 中填写。");
            return;
        }

        final long actionTimeout = timeout("default_callback_timeout_ms");
        DemoLog.info(name(), "deviceSn=" + deviceSn + " actionTimeout=" + actionTimeout + "ms");

        while (true) {
            Utils.printTitle("执行 " + name() + "  (设备 SN: " + deviceSn + ")");
            System.out.print(mainMenu());
            int choice = Utils.readIntInRange(input, "请选择(1-8, q 退出): ", 1, 8);
            if (choice == Utils.QUIT) {
                DemoLog.info(name(), "退出设备调试菜单");
                break;
            }
            try {
                switch (choice) {
                    case 1: menuDebugMode(module, deviceSn, actionTimeout);    break;
                    case 2: menuDockCover(module, deviceSn, actionTimeout);    break;
                    case 3: menuDronePower(module, deviceSn, actionTimeout);   break;
                    case 4: menuCharge(module, deviceSn, actionTimeout);       break;
                    case 5: menuFlightParams(module, deviceSn, actionTimeout); break;
                    case 6: menuMaintenance(module, deviceSn, actionTimeout);  break;
                    case 7: menuSimManagement(module, deviceSn, actionTimeout); break;
                    case 8: menuCloudEnvironmentSwitch(module, deviceSn, actionTimeout); break;
                    default: break;
                }
            } catch (Exception e) {
                console("   结果 -> [异常] " + Utils.describe(e));
                DemoLog.error(name(), "菜单动作执行异常: " + Utils.describe(e), e);
            }
        }
    }

    // ==================== 子菜单 [1]：调试模式 ====================

    private void menuDebugMode(DeviceDebugModule module, String deviceSn, long timeout) {
        console("\n  --- 调试模式 ---");
        console("  1. 打开调试模式   2. 关闭调试模式   q. 返回");
        int c = Utils.readIntInRange(input, "请选择: ", 1, 2);
        if (c == Utils.QUIT) return;

        if (c == 1) {
            // openDebugMode: (device_sn, callback(error, OpenDebugModeResult))
            final Await<FlightHubError> await = new Await<>(name() + ".openDebugMode");
            final AtomicReference<OpenDebugModeResult> ref = new AtomicReference<>();
            runOutcome("打开调试模式", timeout, await, () ->
                module.openDebugMode(deviceSn, new FlightHubCallback.OpenDebugModeCallback() {
                    @Override
                    public void onResult(FlightHubError error, OpenDebugModeResult result) {
                        ref.set(result);
                        await.set(error);
                    }
                })
            );

            printBidMsg("openDebugMode", ref.get() != null ? ref.get().getBid() : null, ref.get() != null ? ref.get().getMessage() : null);
        } else {
            final Await<FlightHubError> await = new Await<>(name() + ".closeDebugMode");
            final AtomicReference<CloseDebugModeResult> ref = new AtomicReference<>();
            runOutcome("关闭调试模式", timeout, await, () ->
                module.closeDebugMode(deviceSn, new FlightHubCallback.CloseDebugModeCallback() {
                    @Override
                    public void onResult(FlightHubError error, CloseDebugModeResult result) {
                        ref.set(result);
                        await.set(error);
                    }
                })
            );

            printBidMsg("closeDebugMode", ref.get() != null ? ref.get().getBid() : null, ref.get() != null ? ref.get().getMessage() : null);
        }
    }

    // ==================== 子菜单 [2]：机场舱盖 ====================

    /**
     * 机场舱盖：需要机场处于远程调试模式(modeCode=2)。
     * 先提示前置条件, 再执行。
     */
    private void menuDockCover(DeviceDebugModule module, String deviceSn, long timeout) {
        if (!checkAndEnsureDebugMode("机场舱盖控制", module, deviceSn, timeout)) return;
        console("\n  --- 机场舱盖 ---");
        console("  1. 打开舱盖(+WS进度订阅)");
        console("  2. 关闭舱盖(+WS进度订阅)");
        console("  3. HTTP查询开舱进度(getOpenDockCoverProgress)");
        console("  4. HTTP查询关舱进度(getCloseDockCoverProgress)");
        console("  q. 返回");
        int c = Utils.readIntInRange(input, "请选择: ", 1, 4);
        if (c == Utils.QUIT) return;

        switch (c) {
            case 1: openDockCover(module, deviceSn, timeout);  break;
            case 2: closeDockCover(module, deviceSn, timeout); break;
            case 3: httpGetOpenDockCoverProgress(module, deviceSn, timeout);  break;
            case 4: httpGetCloseDockCoverProgress(module, deviceSn, timeout); break;
            default: break;
        }
    }

    /** HTTP 主动查询开舱进度(补充；WS 订阅为 subscribeDockCoverOpenProgress)。 */
    private void httpGetOpenDockCoverProgress(DeviceDebugModule module, String deviceSn, long timeout) {
        console("   HTTP 查询开舱进度: sn=" + deviceSn);
        final Await<FlightHubError> await = new Await<>(name() + ".getOpenDockCoverProgress");
        final AtomicReference<OpenDockCoverProgressResult> ref = new AtomicReference<>();
        runOutcome("HTTP查询开舱进度", timeout, await, () ->
            module.getOpenDockCoverProgress(deviceSn, new FlightHubCallback.OpenDockCoverProgressCallback() {
                @Override
                public void onResult(FlightHubError error, OpenDockCoverProgressResult r) {
                    ref.set(r); await.set(error);
                }
            })
        );

        if (ref.get() != null) {
            console("     bid=" + ref.get().getBid() + " msg=" + ref.get().getMessage());
            DemoLog.info(name(), "getOpenDockCoverProgress: " + ref.get());
        }
    }

    /** HTTP 主动查询关舱进度(补充；WS 订阅为 subscribeDockCoverCloseProgress)。 */
    private void httpGetCloseDockCoverProgress(DeviceDebugModule module, String deviceSn, long timeout) {
        console("   HTTP 查询关舱进度: sn=" + deviceSn);
        final Await<FlightHubError> await = new Await<>(name() + ".getCloseDockCoverProgress");
        final AtomicReference<CloseDockCoverProgressResult> ref = new AtomicReference<>();
        runOutcome("HTTP查询关舱进度", timeout, await, () ->
            module.getCloseDockCoverProgress(deviceSn, new FlightHubCallback.CloseDockCoverProgressCallback() {
                @Override
                public void onResult(FlightHubError error, CloseDockCoverProgressResult r) {
                    ref.set(r); await.set(error);
                }
            })
        );

        if (ref.get() != null) {
            console("     bid=" + ref.get().getBid() + " msg=" + ref.get().getMessage());
            DemoLog.info(name(), "getCloseDockCoverProgress: " + ref.get());
        }
    }

    private void openDockCover(DeviceDebugModule module, String deviceSn, long timeout) {
        // 1. 先建进度订阅(不漏首帧推送)
        final Await<FlightHubError> progressAwait = new Await<>(name() + ".dockCoverOpenProgress");
        Subscription progressSub = null;
        try {
            int subId = module.subscribeDockCoverOpenProgress(deviceSn, new FlightHubCallback.SubscribeDockCoverOpenProgressCallback() {
                @Override
                public void onResult(OpenDockCoverResult result) {
                    if (result == null) return;
                    String line = "开舱盖进度: bid=" + result.getBid() + " msg=" + result.getMessage();
                    DemoLog.info(name(), line);
                    console("   [进度推送] " + line);
                    // 收到第一帧推送即视为「物理动作已触发」, 唤醒等待
                    progressAwait.set(null);
                }
            });

            progressSub = Subscription.of("dockCoverOpenProgress", subId, id -> module.unSubscribeDockCoverOpenProgress(id));
            // 登记会话 registry(close 幂等, 双保险): 异常逃逸时由会话收尾兜底退订, 防监听泄漏。
            registry().add(progressSub);
        } catch (Exception e) {
            hint("[警告] 进度订阅建立失败: " + Utils.describe(e) + ", 将继续下发指令但无法接收进度推送");
            DemoLog.warn(name(), "subscribeDockCoverOpenProgress 失败: " + Utils.describe(e));
        }

        try {
            // 2. 下发 HTTP 指令
            final Await<FlightHubError> cmdAwait = new Await<>(name() + ".openDockCover");
            final AtomicReference<OpenDockCoverResult> resultRef = new AtomicReference<>();
            Outcome cmdOutcome = runOutcome("打开舱盖(指令下发)", timeout, cmdAwait, () ->
                module.openDockCover(deviceSn, new FlightHubCallback.OpenDockCoverCallback() {
                    @Override
                    public void onResult(FlightHubError error, OpenDockCoverResult result) {
                        resultRef.set(result);
                        cmdAwait.set(error);
                    }
                })
            );

            printBidMsg("openDockCover", resultRef.get() != null ? resultRef.get().getBid() : null, resultRef.get() != null ? resultRef.get().getMessage() : null);

            // 3. 等待进度推送(指令成功才等)
            if (cmdOutcome != Outcome.FAILED && progressSub != null) {
                console("   >> 等待开舱盖进度推送(最多 " + PROGRESS_WAIT_MS / 1000 + "s)...");
                try {
                    progressAwait.await(PROGRESS_WAIT_MS);
                } catch (Exception e) {
                    hint("[进度] 等待超时或中断: " + Utils.describe(e));
                }
            }
        } finally {
            // 4. 取消进度订阅(finally 保证异常路径也退订), 并移除 registry 死引用。
            if (progressSub != null) {
                progressSub.close();
                registry().remove(progressSub);
            }
        }
    }

    private void closeDockCover(DeviceDebugModule module, String deviceSn, long timeout) {
        final Await<FlightHubError> progressAwait = new Await<>(name() + ".dockCoverCloseProgress");
        Subscription progressSub = null;
        try {
            int subId = module.subscribeDockCoverCloseProgress(deviceSn, new FlightHubCallback.SubscribeDockCoverCloseProgressCallback() {
                @Override
                public void onResult(CloseDockCoverResult result) {
                    if (result == null) return;
                    String line = "关舱盖进度: bid=" + result.getBid()
                        + " msg=" + result.getMessage();
                    DemoLog.info(name(), line);
                    console("   [进度推送] " + line);
                    progressAwait.set(null);
                }
            });

            progressSub = Subscription.of("dockCoverCloseProgress", subId, id -> module.unSubscribeDockCoverCloseProgress(id));
            registry().add(progressSub);   // 登记会话 registry 兜底, 防异常路径监听泄漏。
        } catch (Exception e) {
            hint("[警告] 进度订阅建立失败: " + Utils.describe(e));
            DemoLog.warn(name(), "subscribeDockCoverCloseProgress 失败: " + Utils.describe(e));
        }

        try {
            final Await<FlightHubError> cmdAwait = new Await<>(name() + ".closeDockCover");
            final AtomicReference<CloseDockCoverResult> resultRef = new AtomicReference<>();
            Outcome cmdOutcome = runOutcome("关闭舱盖(指令下发)", timeout, cmdAwait, () ->
                module.closeDockCover(deviceSn, new FlightHubCallback.CloseDockCoverCallback() {
                    @Override
                    public void onResult(FlightHubError error, CloseDockCoverResult result) {
                        resultRef.set(result);
                        cmdAwait.set(error);
                    }
                })
            );

            printBidMsg("closeDockCover", resultRef.get() != null ? resultRef.get().getBid() : null, resultRef.get() != null ? resultRef.get().getMessage() : null);

            if (cmdOutcome != Outcome.FAILED && progressSub != null) {
                console("   >> 等待关舱盖进度推送(最多 " + PROGRESS_WAIT_MS / 1000 + "s)...");
                try {
                    progressAwait.await(PROGRESS_WAIT_MS);
                } catch (Exception e) {
                    hint("[进度] 等待超时或中断: " + Utils.describe(e));
                }
            }
        } finally {
            if (progressSub != null) {
                progressSub.close();
                registry().remove(progressSub);
            }
        }
    }

    // ==================== 子菜单 [3]：飞行器电源 ====================

    private void menuDronePower(DeviceDebugModule module, String deviceSn, long timeout) {
        if (!checkAndEnsureDebugMode("飞行器电源控制", module, deviceSn, timeout)) return;
        console("\n  --- 飞行器电源 ---");
        console("  1. 飞行器开机(+进度订阅)   2. 飞行器关机(+进度订阅)");
        console("  3. 飞行器格式化(危险!)     4. 切换 SDR 工作模式");
        console("  q. 返回");
        int c = Utils.readIntInRange(input, "请选择: ", 1, 4);
        if (c == Utils.QUIT) return;

        switch (c) {
            case 1: openDrone(module, deviceSn, timeout);      break;
            case 2: closeDrone(module, deviceSn, timeout);     break;
            case 3: droneFormat(module, deviceSn, timeout);    break;
            case 4: droneSdrWorkMode(module, deviceSn, timeout); break;
            default: break;
        }
    }

    private void openDrone(DeviceDebugModule module, String deviceSn, long timeout) {
        final Await<FlightHubError> progressAwait = new Await<>(name() + ".droneOpenProgress");
        Subscription progressSub = null;
        try {
            int subId = module.subscribeDroneOpenProgress(deviceSn, new FlightHubCallback.SubscribeDroneOpenProgressCallback() {
                @Override
                public void onResult(OpenDroneResult result) {
                    if (result == null) return;
                    String line = "飞行器开机进度: bid=" + result.getBid() + " msg=" + result.getMessage();
                    DemoLog.info(name(), line);
                    console("   [进度推送] " + line);
                    progressAwait.set(null);
                }
            });

            progressSub = Subscription.of("droneOpenProgress", subId, id -> module.unSubscribeDroneOpenProgress(id));
            registry().add(progressSub);   // 登记会话 registry 兜底, 防异常路径监听泄漏。
        } catch (Exception e) {
            hint("[警告] 进度订阅建立失败: " + Utils.describe(e));
        }

        try {
            final Await<FlightHubError> cmdAwait = new Await<>(name() + ".openDrone");
            final AtomicReference<OpenDroneResult> resultRef = new AtomicReference<>();
            Outcome o = runOutcome("飞行器开机(指令下发)", timeout, cmdAwait, () ->
                module.openDrone(deviceSn, new FlightHubCallback.OpenDroneCallback() {
                    @Override
                    public void onResult(FlightHubError error, OpenDroneResult result) {
                        resultRef.set(result);
                        cmdAwait.set(error);
                    }
                })
            );

            printBidMsg("openDrone", resultRef.get() != null ? resultRef.get().getBid() : null, resultRef.get() != null ? resultRef.get().getMessage() : null);

            if (o != Outcome.FAILED && progressSub != null) {
                console("   >> 等待飞行器开机进度推送(最多 " + PROGRESS_WAIT_MS / 1000 + "s)...");
                try { progressAwait.await(PROGRESS_WAIT_MS); }
                catch (Exception e) { hint("[进度] 等待超时: " + Utils.describe(e)); }
            }
        } finally {
            if (progressSub != null) {
                progressSub.close();
                registry().remove(progressSub);
            }
        }
    }

    private void closeDrone(DeviceDebugModule module, String deviceSn, long timeout) {
        final Await<FlightHubError> progressAwait = new Await<>(name() + ".droneCloseProgress");
        Subscription progressSub = null;
        try {
            int subId = module.subscribeDroneCloseProgress(deviceSn, new FlightHubCallback.SubscribeDroneCloseProgressCallback() {
                @Override
                public void onResult(CloseDroneResult result) {
                    if (result == null) return;
                    String line = "飞行器关机进度: bid=" + result.getBid() + " msg=" + result.getMessage();
                    DemoLog.info(name(), line);
                    console("   [进度推送] " + line);
                    progressAwait.set(null);
                }
            });

            progressSub = Subscription.of("droneCloseProgress", subId, id -> module.unSubscribeDroneCloseProgress(id));
            registry().add(progressSub);   // 登记会话 registry 兜底, 防异常路径监听泄漏。
        } catch (Exception e) {
            hint("[警告] 进度订阅建立失败: " + Utils.describe(e));
        }

        try {
            final Await<FlightHubError> cmdAwait = new Await<>(name() + ".closeDrone");
            final AtomicReference<CloseDroneResult> resultRef = new AtomicReference<>();
            Outcome o = runOutcome("飞行器关机(指令下发)", timeout, cmdAwait, () ->
                module.closeDrone(deviceSn, new FlightHubCallback.CloseDroneCallback() {
                    @Override
                    public void onResult(FlightHubError error, CloseDroneResult result) {
                        resultRef.set(result);
                        cmdAwait.set(error);
                    }
                })
            );

            printBidMsg("closeDrone", resultRef.get() != null ? resultRef.get().getBid() : null, resultRef.get() != null ? resultRef.get().getMessage() : null);

            if (o != Outcome.FAILED && progressSub != null) {
                console("   >> 等待飞行器关机进度推送(最多 " + PROGRESS_WAIT_MS / 1000 + "s)...");
                try { progressAwait.await(PROGRESS_WAIT_MS); }
                catch (Exception e) { hint("[进度] 等待超时: " + Utils.describe(e)); }
            }
        } finally {
            if (progressSub != null) {
                progressSub.close();
                registry().remove(progressSub);
            }
        }
    }

    private void droneFormat(DeviceDebugModule module, String deviceSn, long timeout) {
        // 格式化为危险动作, 二次确认
        if (!confirmDangerous("飞行器格式化(将清除飞行器存储数据！)")) return;

        final Await<FlightHubError> progressAwait = new Await<>(name() + ".droneFormatProgress");
        Subscription progressSub = null;
        try {
            int subId = module.subscribeDroneFormatProgress(deviceSn, new FlightHubCallback.SubscribeDroneFormatProgressCallback() {
                @Override
                public void onResult(DroneFormatResult result) {
                    if (result == null) return;
                    String line = "飞行器格式化进度: bid=" + result.getBid() + " msg=" + result.getMessage();
                    DemoLog.info(name(), line);
                    console("   [进度推送] " + line);
                    progressAwait.set(null);
                }
            });

            progressSub = Subscription.of("droneFormatProgress", subId, id -> module.unSubscribeDroneFormatProgress(id));
            registry().add(progressSub);   // 登记会话 registry 兜底, 防异常路径监听泄漏。
        } catch (Exception e) {
            hint("[警告] 进度订阅建立失败: " + Utils.describe(e));
        }

        try {
            final Await<FlightHubError> cmdAwait = new Await<>(name() + ".droneFormat");
            final AtomicReference<DroneFormatResult> resultRef = new AtomicReference<>();
            Outcome o = runOutcome("飞行器格式化(指令下发)", timeout, cmdAwait, () ->
                module.droneFormat(deviceSn, new FlightHubCallback.DroneFormatCallback() {
                    @Override
                    public void onResult(FlightHubError error, DroneFormatResult result) {
                        resultRef.set(result);
                        cmdAwait.set(error);
                    }
                })
            );

            printBidMsg("droneFormat", resultRef.get() != null ? resultRef.get().getBid() : null, resultRef.get() != null ? resultRef.get().getMessage() : null);

            if (o != Outcome.FAILED && progressSub != null) {
                console("   >> 等待飞行器格式化进度推送(最多 " + PROGRESS_WAIT_MS / 1000 + "s)...");
                try { progressAwait.await(PROGRESS_WAIT_MS); }
                catch (Exception e) { hint("[进度] 等待超时: " + Utils.describe(e)); }
            }
        } finally {
            if (progressSub != null) {
                progressSub.close();
                registry().remove(progressSub);
            }
        }
    }

    private void droneSdrWorkMode(DeviceDebugModule module, String deviceSn, long timeout) {
        Node cfg = params.node("device_debug");
        int mode = cfg.intVal("sdr_work_mode", 0);
        console("   切换 SDR 工作模式: mode=" + mode + "(配置 device_debug.sdr_work_mode)");
        // droneSdrWorkMode 使用 ResultCallbackWithError(仅 error, 无结果对象), 可直接用基类 call
        call("切换 SDR 工作模式", cb -> module.droneSdrWorkMode(deviceSn, mode, cb), timeout);
    }

    // ==================== 子菜单 [4]：充电控制 ====================

    private void menuCharge(DeviceDebugModule module, String deviceSn, long timeout) {
        if (!checkAndEnsureDebugMode("充电控制", module, deviceSn, timeout)) return;
        console("\n  --- 充电控制 ---");
        console("  1. 开启充电(+进度订阅)   2. 关闭充电(+进度订阅)   q. 返回");
        int c = Utils.readIntInRange(input, "请选择: ", 1, 2);
        if (c == Utils.QUIT) return;

        if (c == 1) {
            openCharge(module, deviceSn, timeout);
        } else {
            closeCharge(module, deviceSn, timeout);
        }
    }

    private void openCharge(DeviceDebugModule module, String deviceSn, long timeout) {
        final Await<FlightHubError> progressAwait = new Await<>(name() + ".chargeOpenProgress");
        Subscription progressSub = null;
        try {
            int subId = module.subscribeDroneChargeOpenProgress(deviceSn, new FlightHubCallback.SubscribeDroneChargeOpenProgressCallback() {
                @Override
                public void onResult(OpenChargeResult result) {
                    if (result == null) return;
                    String line = "开启充电进度: bid=" + result.getBid() + " msg=" + result.getMessage();
                    DemoLog.info(name(), line);
                    console("   [进度推送] " + line);
                    progressAwait.set(null);
                }
            });

            progressSub = Subscription.of("chargeOpenProgress", subId, id -> module.unSubscribeDroneChargeOpenProgress(id));
            registry().add(progressSub);   // 登记会话 registry 兜底, 防异常路径监听泄漏。
        } catch (Exception e) {
            hint("[警告] 进度订阅建立失败: " + Utils.describe(e));
        }

        try {
            final Await<FlightHubError> cmdAwait = new Await<>(name() + ".openCharge");
            final AtomicReference<OpenChargeResult> resultRef = new AtomicReference<>();
            Outcome o = runOutcome("开启充电(指令下发)", timeout, cmdAwait, () ->
                module.openCharge(deviceSn, new FlightHubCallback.OpenChargeCallback() {
                    @Override
                    public void onResult(FlightHubError error, OpenChargeResult result) {
                        resultRef.set(result);
                        cmdAwait.set(error);
                    }
                })
            );

            printBidMsg("openCharge", resultRef.get() != null ? resultRef.get().getBid() : null, resultRef.get() != null ? resultRef.get().getMessage() : null);

            if (o != Outcome.FAILED && progressSub != null) {
                console("   >> 等待充电进度推送(最多 " + PROGRESS_WAIT_MS / 1000 + "s)...");
                try { progressAwait.await(PROGRESS_WAIT_MS); }
                catch (Exception e) { hint("[进度] 等待超时: " + Utils.describe(e)); }
            }
        } finally {
            if (progressSub != null) {
                progressSub.close();
                registry().remove(progressSub);
            }
        }
    }

    private void closeCharge(DeviceDebugModule module, String deviceSn, long timeout) {
        final Await<FlightHubError> progressAwait = new Await<>(name() + ".chargeCloseProgress");
        Subscription progressSub = null;
        try {
            int subId = module.subscribeDroneChargeCloseProgress(deviceSn, new FlightHubCallback.SubscribeDroneChargeCloseProgressCallback() {
                @Override
                public void onResult(CloseChargeResult result) {
                    if (result == null) return;
                    String line = "关闭充电进度: bid=" + result.getBid() + " msg=" + result.getMessage();
                    DemoLog.info(name(), line);
                    console("   [进度推送] " + line);
                    progressAwait.set(null);
                }
            });

            progressSub = Subscription.of("chargeCloseProgress", subId, id -> module.unSubscribeDroneChargeCloseProgress(id));
            registry().add(progressSub);   // 登记会话 registry 兜底, 防异常路径监听泄漏。
        } catch (Exception e) {
            hint("[警告] 进度订阅建立失败: " + Utils.describe(e));
        }

        try {
            final Await<FlightHubError> cmdAwait = new Await<>(name() + ".closeCharge");
            final AtomicReference<CloseChargeResult> resultRef = new AtomicReference<>();
            Outcome o = runOutcome("关闭充电(指令下发)", timeout, cmdAwait, () ->
                module.closeCharge(deviceSn, new FlightHubCallback.CloseChargeCallback() {
                    @Override
                    public void onResult(FlightHubError error, CloseChargeResult result) {
                        resultRef.set(result);
                        cmdAwait.set(error);
                    }
                })
            );

            printBidMsg("closeCharge", resultRef.get() != null ? resultRef.get().getBid() : null, resultRef.get() != null ? resultRef.get().getMessage() : null);

            if (o != Outcome.FAILED && progressSub != null) {
                console("   >> 等待充电进度推送(最多 " + PROGRESS_WAIT_MS / 1000 + "s)...");
                try { progressAwait.await(PROGRESS_WAIT_MS); }
                catch (Exception e) { hint("[进度] 等待超时: " + Utils.describe(e)); }
            }
        } finally {
            if (progressSub != null) {
                progressSub.close();
                registry().remove(progressSub);
            }
        }
    }

    // ==================== 子菜单 [5]：飞行参数设置 ====================

    /**
     * 飞行参数设置：均为即时 HTTP 动作, 参数取自 {@code device_debug} 配置节。
     *
     * <p>setDistanceLimit/setHeightLimit 等接口的回调均为 {@code (error, XxxResult)}, 
     * result 只含 {@code getMessage()} 用于回显, 无业务关键字段, 统一用
     * {@link #callWithMessageResult} 处理。
     */
    private void menuFlightParams(DeviceDebugModule module, String deviceSn, long timeout) {
        console("\n  --- 飞行参数设置 ---");
        console("  1. 设置限远距离");
        console("  2. 设置限高");
        console("  3. 设置避障状态");
        console("  4. 设置返航剩余电量");
        console("  5. 设置夜航灯状态");
        console("  6. 切换报警灯状态");
        console("  7. 设置机场空调模式");
        console("  8. 设置机场静音模式");
        console("  q. 返回");
        int c = Utils.readIntInRange(input, "请选择: ", 1, 8);
        if (c == Utils.QUIT) return;

        Node cfg = params.node("device_debug");
        switch (c) {
            case 1: {
                int state = askOnOff("限远距离开关", cfg.intVal("distance_limit_state", 1));
                if (state == -1) break;
                int value = cfg.intVal("distance_limit_value", 500);
                console("   设置限远: state=" + state + "(0=关 1=开) value=" + value + "m"
                    + "(限远距离取自配置 device_debug.distance_limit_value)");
                callWithMessageResult("设置限远距离", timeout,
                    await -> module.setDistanceLimit(deviceSn, state, value, new FlightHubCallback.SetDistanceLimitCallback() {
                        @Override
                        public void onResult(FlightHubError error, SetDistanceLimitResult r) {
                            if (r != null) console("     服务端消息: " + r.getMessage());
                            await.set(error);
                        }
                    })
                );
                break;
            }
            case 2: {
                int heightLimit = cfg.intVal("height_limit", 120);
                console("   设置限高: " + heightLimit + "m(取自配置 device_debug.height_limit)");
                callWithMessageResult("设置限高", timeout,
                    await -> module.setHeightLimit(deviceSn, heightLimit, new FlightHubCallback.SetHeightLimitCallback() {
                        @Override
                        public void onResult(FlightHubError error, SetHeightLimitResult r) {
                            if (r != null) console("     服务端消息: " + r.getMessage());
                            await.set(error);
                        }
                    })
                );
                break;
            }
            case 3: {
                int obstacleState = askOnOff("避障", cfg.intVal("obstacle_avoidance_state", 1));
                if (obstacleState == -1) break;
                console("   设置避障: state=" + obstacleState + "(0=关 1=开)");
                callWithMessageResult("设置避障状态", timeout,
                    await -> module.setObstacleAvoidance(deviceSn, obstacleState, new FlightHubCallback.SetObstacleAvoidanceCallback() {
                        @Override
                        public void onResult(FlightHubError error, SetObstacleAvoidanceResult r) {
                            if (r != null) console("     服务端消息: " + r.getMessage());
                            await.set(error);
                        }
                    })
                );
                break;
            }
            case 4: {
                final int rthPower = Math.max(0, Math.min(100, cfg.intVal("rth_remaining_power", 30)));
                console("   设置返航剩余电量: " + rthPower + "%(取自配置 device_debug.rth_remaining_power)");
                callWithMessageResult("设置返航剩余电量", timeout,
                    await -> module.setRemainingPowerForRth(deviceSn, rthPower, new FlightHubCallback.SetRemainingPowerForRthCallback() {
                        @Override
                        public void onResult(FlightHubError error, SetRemainingPowerForRthResult r) {
                            if (r != null) console("     服务端消息: " + r.getMessage());
                            await.set(error);
                        }
                    })
                );
                break;
            }
            case 5: {
                int nightLights = askOnOff("夜航灯", cfg.intVal("night_lights_state", 1));
                if (nightLights == -1) break;
                console("   设置夜航灯: state=" + nightLights + "(0=关 1=开)");
                callWithMessageResult("设置夜航灯状态", timeout,
                    await -> module.setNightLightsState(deviceSn, nightLights, new FlightHubCallback.SetNightLightsStateCallback() {
                        @Override
                        public void onResult(FlightHubError error, SetNightLightsStateResult r) {
                            if (r != null) console("     服务端消息: " + r.getMessage());
                            await.set(error);
                        }
                    })
                );
                break;
            }
            case 6: {
                int alarmState = askOnOff("报警灯", cfg.intVal("alarm_light_state", 1));
                if (alarmState == -1) break;
                console("   切换报警灯: state=" + alarmState + "(0=关 1=开)");
                callWithMessageResult("切换报警灯状态", timeout,
                    await -> module.switchAlarmLightState(deviceSn, alarmState, new FlightHubCallback.SwitchAlarmLightStateCallback() {
                        @Override
                        public void onResult(FlightHubError error, SwitchAlarmStateResult r) {
                            if (r != null) console("     服务端消息: " + r.getMessage());
                            await.set(error);
                        }
                    })
                );
                break;
            }
            case 7: {
                // 空调有 4 种模式, 用独立子选择而非 askOnOff
                console("\n  --- 机场空调模式 ---");
                console("  1. 空闲   2. 制冷   3. 制热   4. 除湿   q. 返回");
                int acChoice = Utils.readIntInRange(input, "请选择模式: ", 1, 4);
                if (acChoice == Utils.QUIT) break;
                final int acMode = acChoice - 1; // 1→0=空闲, 2→1=制冷, 3→2=制热, 4→3=除湿
                console("   设置空调模式: mode=" + acMode);
                call("设置机场空调模式", cb -> module.setDockAirConditionerMode(deviceSn, acMode, cb), timeout);
                break;
            }
            case 8: {
                int silentMode = askOnOff("机场静音模式", cfg.intVal("silent_mode", 0));
                if (silentMode == -1) break;
                console("   设置静音模式: mode=" + silentMode + "(0=关 1=开)");
                call("设置机场静音模式", cb -> module.setDockSilentMode(deviceSn, silentMode, cb), timeout);
                break;
            }
            default: break;
        }
    }

    // ==================== 子菜单 [6]：设备维护 ====================

    private void menuMaintenance(DeviceDebugModule module, String deviceSn, long timeout) {
        if (!checkAndEnsureDebugMode("设备维护(重启/格式化/换电)", module, deviceSn, timeout)) return;
        console("\n  --- 设备维护 ---");
        console("  1. 设备重启(危险!,+进度订阅)");
        console("  2. 机场格式化(危险!,+进度订阅)");
        console("  3. 设置电池存储模式");
        console("  4. 发起手动换电(+查询进度)");
        console("  5. Dongle 电源开关");
        console("  6. 机场低功耗开关");
        console("  7. HTTP查询重启进度");
        console("  8. HTTP查询机场格式化进度");
        console("  9. 设置 Geo Caging(地理围栏)");
        console("  q. 返回");

        int c = Utils.readIntInRange(input, "请选择: ", 1, 9);
        if (c == Utils.QUIT) return;

        Node cfg = params.node("device_debug");
        switch (c) {
            case 1: deviceReboot(module, deviceSn, timeout);  break;
            case 2: deviceFormat(module, deviceSn, timeout);  break;
            case 3: {
                int mode = askOnOff("电池存储模式", cfg.intVal("battery_store_mode", 0));
                if (mode == -1) break;
                console("   设置电池存储模式: mode=" + mode + "(0=关 1=开)");
                callWithMessageResult("设置电池存储模式", timeout,
                    await -> module.setBatteryStoreMode(deviceSn, mode, new FlightHubCallback.SetBatteryStoreModeCallback() {
                        @Override
                        public void onResult(FlightHubError error, SetBatteryStoreModeResult r) {
                            if (r != null) console("     服务端消息: " + r.getMessage());
                            await.set(error);
                        }
                    })
                );
                break;
            }
            case 4: manualBatterySwap(module, deviceSn, timeout); break;
            case 5: {
                int powerState = askOnOff("Dongle 电源", cfg.intVal("dongle_power_state", 1));
                if (powerState == -1) break;
                // Dongle 电源开关下发给飞行器(UAV), 需要传飞机 SN, 而非机场 SN
                // 前提：飞机已安装 Dongle, 可在 device_state.dongle_infos 中确认
                if (!session.hasDrone()) {
                    hint("Dongle 电源开关需要飞行器设备, 当前 session 无飞机, 无法执行。");
                    break;
                }
                final String droneSn = session.droneSn();
                console("   Dongle 电源: state=" + powerState + "(0=关 1=开)"
                    + "  飞机SN=" + droneSn);
                console("   注意：请确认飞机已安装 Dongle(可查 device_state.dongle_infos)");
                callWithMessageResult("Dongle 电源开关", timeout,
                    await -> module.setDonglePowerSwitch(droneSn, powerState, new FlightHubCallback.DonglePowerSwitchCallback() {
                        @Override
                        public void onResult(FlightHubError error, DonglePowerSwitchResult r) {
                            if (r != null) console("     服务端消息: " + r.getMessage());
                            await.set(error);
                        }
                    })
                );
                break;
            }
            case 6: {
                int lowPower = askOnOff("机场低功耗", cfg.intVal("low_power_mode", 0));
                if (lowPower == -1) break;
                console("   机场低功耗: mode=" + lowPower + "(0=关 1=开)");
                callWithMessageResult("机场低功耗开关", timeout,
                    await -> module.setLowPowerSwitch(deviceSn, lowPower, new FlightHubCallback.LowPowerSwitchCallback() {
                        @Override
                        public void onResult(FlightHubError error, LowPowerSwitchResult r) {
                            if (r != null) console("     服务端消息: " + r.getMessage());
                            await.set(error);
                        }
                    })
                );
                break;
            }
            case 7: httpGetDeviceRebootProgress(module, deviceSn, timeout);  break;
            case 8: httpGetDeviceFormatProgress(module, deviceSn, timeout);  break;
            case 9: setGeoCagingEnable(module, deviceSn, timeout);           break;
            default: break;
        }
    }

    /**
     * HTTP 主动查询设备重启进度(补充；WS 订阅为 subscribeDeviceRebootProgress)。
     *
     * <p>两种查询进度方式对比：
     * <ul>
     *   <li>WS 订阅([1] 重启时自动建立)：实时推送, 无需轮询；</li>
     *   <li>HTTP GET(本项)：按需查询当前进度快照, 适合重新进入菜单后补查状态。</li>
     * </ul>
     */
    private void httpGetDeviceRebootProgress(DeviceDebugModule module, String deviceSn, long timeout) {
        console("   HTTP 查询设备重启进度: sn=" + deviceSn);
        final Await<FlightHubError> await = new Await<>(name() + ".getDeviceRebootProgress");
        final AtomicReference<DeviceRebootProgressResult> ref = new AtomicReference<>();
        runOutcome("HTTP查询设备重启进度", timeout, await, () ->
            module.getDeviceRebootProgress(deviceSn, new FlightHubCallback.DeviceRebootProgressCallback() {
                @Override
                public void onResult(FlightHubError error, DeviceRebootProgressResult r) {
                    ref.set(r); await.set(error);
                }
            })
        );

        if (ref.get() != null) {
            console("     bid=" + ref.get().getBid() + " msg=" + ref.get().getMessage());
            DemoLog.info(name(), "getDeviceRebootProgress: " + ref.get());
        }
    }

    /**
     * HTTP 主动查询机场格式化进度(补充；WS 订阅为 subscribeDeviceFormatProgress)。
     */
    private void httpGetDeviceFormatProgress(DeviceDebugModule module, String deviceSn, long timeout) {
        console("   HTTP 查询机场格式化进度: sn=" + deviceSn);
        final Await<FlightHubError> await = new Await<>(name() + ".getDeviceFormatProgress");
        final AtomicReference<DeviceFormatProgressResult> ref = new AtomicReference<>();
        runOutcome("HTTP查询机场格式化进度", timeout, await, () ->
            module.getDeviceFormatProgress(deviceSn, new FlightHubCallback.DeviceFormatProgressCallback() {
                @Override
                public void onResult(FlightHubError error, DeviceFormatProgressResult r) {
                    ref.set(r); await.set(error);
                }
            })
        );
        if (ref.get() != null) {
            console("     bid=" + ref.get().getBid() + " msg=" + ref.get().getMessage());
            DemoLog.info(name(), "getDeviceFormatProgress: " + ref.get());
        }
    }

    /**
     * 设置 Geo Caging(地理围栏)。
     *
     * <p>围栏参数取自 {@code device_debug.geo_caging_switch}(0=关 1=开)。
     * 围栏坐标(fgLongitude/fgLatitude/fgRadius/fgHeight 等)需在
     * {@code device_debug.geo_caging_*} 中配置；不配置时发送空列表(服务端允许, 表示清除围栏)。
     *
     * <p>GeoCagingInfoParam 字段说明：
     * <ul>
     *   <li>fg*：飞行围栏(Flight Geo-fence)中心坐标和半径/高度；</li>
     *   <li>cv*：自定义围栏(Custom Volume)中心坐标和半径/高度。</li>
     * </ul>
     */
    private void setGeoCagingEnable(DeviceDebugModule module, String deviceSn, long timeout) {
        Node cfg = params.node("device_debug");
        int geoCagingSwitch = askOnOff("Geo Caging(地理围栏)", cfg.intVal("geo_caging_switch", 0));
        if (geoCagingSwitch == -1) return;

        // 构建围栏参数(从配置读取；留空则发空列表, 即清除围栏)
        List<GeoCagingInfoParam> geoCagingInfos = new ArrayList<>();
        double fgLon = cfg.dbl("geo_caging_fg_longitude", 0);
        double fgLat = cfg.dbl("geo_caging_fg_latitude", 0);
        if (fgLon != 0 || fgLat != 0) {
            GeoCagingInfoParam info = new GeoCagingInfoParam();
            info.setFgLongitude(fgLon);
            info.setFgLatitude(fgLat);
            info.setFgRadius(cfg.intVal("geo_caging_fg_radius", 500));
            info.setFgHeight(cfg.intVal("geo_caging_fg_height", 120));
            info.setCvLongitude(cfg.dbl("geo_caging_cv_longitude", fgLon));
            info.setCvLatitude(cfg.dbl("geo_caging_cv_latitude", fgLat));
            info.setCvRadius(cfg.intVal("geo_caging_cv_radius", 500));
            info.setCvHeight(cfg.intVal("geo_caging_cv_height", 120));
            geoCagingInfos.add(info);
        } else {
            hint("未配置围栏坐标(geo_caging_fg_longitude/latitude), 将发送空围栏列表(清除围栏)");
        }

        console("   设置 Geo Caging: switch=" + geoCagingSwitch + "(0=关 1=开) infos=" + geoCagingInfos.size() + "条");

        DemoLog.info(name(), "setGeoCagingEnable: switch=" + geoCagingSwitch + " infos=" + geoCagingInfos.size());

        final Await<FlightHubError> await = new Await<>(name() + ".setGeoCagingEnable");
        runOutcome("设置 Geo Caging", timeout, await, () ->
            module.setGeoCagingEnable(deviceSn, geoCagingSwitch, geoCagingInfos, new FlightHubCallback.SetGeoCagingEnableCallback() {
                @Override
                public void onResult(FlightHubError error) {
                    await.set(error);
                }
            })
        );
    }

    // ==================== 子菜单 [7]：4G/SIM 管理 ====================

    /**
     * 4G 双卡双通 / SIM 卡槽切换 / eSIM 运营商切换管理。
     *
     * <p>所有接口均需 {@code imei}、{@code device_type} 参数, 这些来自设备的 {@code dongle_infos}, 
     * SDK 未封装该字段, 需用户在 {@code demo_params.json} 的 {@code device_debug} 节手动填写：
     * <ul>
     *   <li>{@code dongle_device_sn}：飞机或机场 SN(机场的 Dongle 用机场 SN, 飞机的 Dongle 用飞机 SN)；</li>
     *   <li>{@code dongle_imei}：目标 Dongle 的 IMEI(从 device_state.dongle_infos[*].imei 获取)；</li>
     *   <li>{@code dongle_device_type}：设备类型字符串(如机场 Dongle 填 "dock", 参考上云 API 文档)；</li>
     *   <li>{@code esim_operator}：eSIM 运营商枚举(1=电信 2=联通 3=移动, 以上云 API 文档为准)；</li>
     *   <li>{@code sim_slot}：SIM 卡槽编号(从 device_state.dongle_infos[*].sim_slot 获取)。</li>
     * </ul>
     *
     * <p>查询 dongle_infos 方法：
     * <pre>GET /device/api/v1/manager/devices?device_sn={sn}
     * → device_state.dongle_infos[*].imei / seat_index / sim_slot</pre>
     *
     * <p>接口串联建议：
     * <ol>
     *   <li>[1] eSIM 运营商切换 → 下发后用 [2] 订阅进度 / [3] 查询进度确认结果；</li>
     *   <li>[4] SIM 卡槽切换 → 下发后用 [5] 订阅进度确认结果。</li>
     * </ol>
     */
    private void menuSimManagement(DeviceDebugModule module, String deviceSn, long timeout) {
        Node cfg = params.node("device_debug");

        // 从配置节读取 Dongle 相关参数, 未配置时使用 session 设备 SN 兜底
        String dongleSn = cfg.str("dongle_device_sn", "");
        if (dongleSn.isEmpty()) {
            // 默认用飞机 SN(飞机上的 Dongle 更常见); 与 resolveDeviceSn() 一致对 session 返回值
            // 做 null/空校验, 避免 hasDrone()==true 但 droneSn() 为 null/空时以空 SN 下发。
            String fallback = session.hasDrone() ? session.droneSn() : null;
            dongleSn = (fallback != null && !fallback.isEmpty()) ? fallback : deviceSn;
            hint("dongle_device_sn 未配置, 使用设备 SN: " + dongleSn + "(如 Dongle 在机场上, 请配置机场 SN)");
        }
        final String finalDongleSn = dongleSn;
        String imei = cfg.str("dongle_imei", "");
        String devType = cfg.str("dongle_device_type", "");

        console("\n  --- 4G/SIM 管理 ---");
        console("  当前参数(取自 device_debug 配置节):");
        console("    dongle_device_sn  = " + dongleSn
            + (imei.isEmpty() ? "" : "    dongle_imei = " + imei)
            + (devType.isEmpty() ? "" : "    dongle_device_type = " + devType));
        if (imei.isEmpty() || devType.isEmpty()) {
            console("  [提示] dongle_imei / dongle_device_type 未配置！");
            console("    查询方式: GET /device/api/v1/manager/devices?device_sn=" + dongleSn);
            console("    → device_state.dongle_infos[*].imei / dongle_type");
            console("    device_type 取值: 机场的 Dongle 填 \"dock\", 飞机的 Dongle 参考上云 API 文档");
        }
        console("");
        console("  1. eSIM 运营商切换        (esimOperatorSwitch: 下发切换指令)");
        console("  2. 订阅 eSIM 切换进度     (subscribeDeviceEsimOperatorSwitchProgress: WS)");
        console("  3. 查询 eSIM 切换进度     (getEsimOperatorSwitchProgress: HTTP GET)");
        console("  4. SIM 卡槽切换           (simSlotSwitch: 下发切换指令)");
        console("  5. 订阅 SIM 卡槽切换进度  (subscribeDeviceSimSlotSwitchProgress: WS)");
        console("  q. 返回");

        int c = Utils.readIntInRange(input, "请选择: ", 1, 5);
        if (c == Utils.QUIT) return;

        if ((imei.isEmpty() || devType.isEmpty()) && (c == 1 || c == 4)) {
            hint("dongle_imei 或 dongle_device_type 未配置, 无法下发切换指令。"
                + "请在 demo_params.json 的 device_debug 节填写后重试。");
            return;
        }

        switch (c) {
            case 1: esimOperatorSwitch(module, finalDongleSn, imei, devType, timeout); break;
            case 2: subscribeEsimSwitchProgress(module, finalDongleSn);               break;
            case 3: getEsimSwitchProgress(module, finalDongleSn, timeout);            break;
            case 4: simSlotSwitch(module, finalDongleSn, imei, devType, timeout);     break;
            case 5: subscribeSimSlotSwitchProgress(module, finalDongleSn);            break;
            default: break;
        }
    }

    /** eSIM 运营商切换(下发指令)。 */
    private void esimOperatorSwitch(DeviceDebugModule module, String dongleSn, String imei, String devType, long timeout) {
        Node cfg = params.node("device_debug");
        int operator = cfg.intVal("esim_operator", -1);
        if (operator < 0) {
            hint("esim_operator 未配置(1=电信 2=联通 3=移动), 请在 device_debug 节填写后重试。");
            return;
        }
        console("   eSIM 运营商切换: sn=" + dongleSn + " imei=" + imei
            + " operator=" + operator + " device_type=" + devType);

        final Await<FlightHubError> await = new Await<>(name() + ".esimOperatorSwitch");
        final AtomicReference<EsimOperatorSwitchResult> ref = new AtomicReference<>();
        runOutcome("eSIM 运营商切换", timeout, await, () ->
            module.esimOperatorSwitch(dongleSn, imei, operator, devType, new FlightHubCallback.EsimOperatorSwitchCallback() {
                @Override
                public void onResult(FlightHubError error, EsimOperatorSwitchResult r) {
                    ref.set(r);
                    await.set(error);
                }
            })
        );
        printBidMsg("esimOperatorSwitch", ref.get() != null ? ref.get().getBid() : null, ref.get() != null ? ref.get().getMessage() : null);
        if (ref.get() != null && ref.get().getBid() != null && !ref.get().getBid().isEmpty()) {
            hint("切换已下发, 建议用 [2] 订阅进度或 [3] HTTP 查询进度确认结果");
        }
    }

    /** 订阅 eSIM 运营商切换进度(WS, 持续接收直到用户退出)。 */
    private void subscribeEsimSwitchProgress(DeviceDebugModule module, String dongleSn) {
        console("   订阅 eSIM 切换进度(WS): sn=" + dongleSn);
        final Await<Integer> trigger = new Await<>(name() + ".esimProgressTrigger");
        int subId = -1;
        try {
            subId = module.subscribeDeviceEsimOperatorSwitchProgress(dongleSn, new FlightHubCallback.SubscribeDeviceEsimOperatorSwitchProgressCallback() {
                @Override
                public void onResult(EsimOperatorSwitchResult r) {
                    if (r == null) return;
                    String line = "eSIM 切换进度: bid=" + r.getBid()
                        + " msg=" + r.getMessage();
                    DemoLog.info(name(), line);
                    console("   [进度推送] " + line);
                    trigger.set(1); // 收到首帧, 通知可退出
                }
            });
        } catch (Exception e) {
            hint("[警告] 订阅失败: " + Utils.describe(e));
            return;
        }
        // 注意：本等待阻塞在 trigger.await(...)上, 期间按 Enter 不会中断等待(Enter 会被缓冲,
        // 直到下面 await 返回后才被消费)——提示文案如实说明"最多等待30秒", 不再声称"按 Enter 随时退订"。
        console("   等待进度推送(最多 30s, 收到推送或超时后自动继续)...");
        try { trigger.await(30000); } catch (Exception ignore) { /* 超时走到这里 */ }
        Utils.readLine(input, "   按 Enter 退订...");
        if (subId >= 0) {
            try { module.unSubscribeDeviceEsimOperatorSwitchProgress(subId); }
            catch (Exception e) { DemoLog.warn(name(), "退订失败: " + Utils.describe(e)); }
        }
        hint("eSIM 切换进度订阅已退订");
    }

    /** 查询 eSIM 运营商切换进度(HTTP GET)。 */
    private void getEsimSwitchProgress(DeviceDebugModule module, String dongleSn, long timeout) {
        console("   查询 eSIM 切换进度(HTTP): sn=" + dongleSn);
        final Await<FlightHubError> await = new Await<>(name() + ".getEsimProgress");
        final AtomicReference<EsimOperatorSwitchProgressResult> ref = new AtomicReference<>();
        runOutcome("查询 eSIM 切换进度", timeout, await, () ->
            module.getEsimOperatorSwitchProgress(dongleSn, new FlightHubCallback.EsimOperatorSwitchProgressCallback() {
                @Override
                public void onResult(FlightHubError error, EsimOperatorSwitchProgressResult r) {
                    ref.set(r);
                    await.set(error);
                }
            })
        );
        if (ref.get() != null) {
            console("     msg=" + ref.get().getMessage()
                + " bid=" + ref.get().getBid()
                + (ref.get().getDataJson() != null && !ref.get().getDataJson().isEmpty()
                            ? " data=" + ref.get().getDataJson() : ""));
            DemoLog.info(name(), "esimProgress: " + ref.get());
        }
    }

    /** SIM 卡槽切换(下发指令)。 */
    private void simSlotSwitch(DeviceDebugModule module, String dongleSn, String imei, String devType, long timeout) {
        Node cfg = params.node("device_debug");
        int simSlot = cfg.intVal("sim_slot", -1);
        if (simSlot < 0) {
            hint("sim_slot 未配置, 请在 device_debug 节填写目标卡槽编号后重试。");
            return;
        }
        console("   SIM 卡槽切换: sn=" + dongleSn + " imei=" + imei
            + " sim_slot=" + simSlot + " device_type=" + devType);

        final Await<FlightHubError> await = new Await<>(name() + ".simSlotSwitch");
        final AtomicReference<SimSlotSwitchResult> ref = new AtomicReference<>();
        runOutcome("SIM 卡槽切换", timeout, await, () ->
            module.simSlotSwitch(dongleSn, imei, simSlot, devType, new FlightHubCallback.SimSlotSwitchCallback() {
                @Override
                public void onResult(FlightHubError error, SimSlotSwitchResult r) {
                    ref.set(r);
                    await.set(error);
                }
            })
        );
        printBidMsg("simSlotSwitch", ref.get() != null ? ref.get().getBid() : null, ref.get() != null ? ref.get().getMessage() : null);
        if (ref.get() != null && ref.get().getBid() != null && !ref.get().getBid().isEmpty()) {
            hint("切换已下发, 建议用 [5] 订阅进度确认结果");
        }
    }

    /** 订阅 SIM 卡槽切换进度(WS)。 */
    private void subscribeSimSlotSwitchProgress(DeviceDebugModule module, String dongleSn) {
        console("   订阅 SIM 卡槽切换进度(WS): sn=" + dongleSn);
        final Await<Integer> trigger = new Await<>(name() + ".simSlotProgressTrigger");
        int subId = -1;
        try {
            subId = module.subscribeDeviceSimSlotSwitchProgress(dongleSn, new FlightHubCallback.SubscribeDeviceSimSlotSwitchProgressCallback() {
                @Override
                public void onResult(SimSlotSwitchResult r) {
                    if (r == null) return;
                    String line = "SIM 卡槽切换进度: bid=" + r.getBid()
                        + " msg=" + r.getMessage();
                    DemoLog.info(name(), line);
                    console("   [进度推送] " + line);
                    trigger.set(1);
                }
            });
        } catch (Exception e) {
            hint("[警告] 订阅失败: " + Utils.describe(e));
            return;
        }
        // 同 subscribeEsimSwitchProgress: 等待期间阻塞在 trigger.await(...)上, Enter 无法中断,
        // 提示文案如实说明"最多等待30秒"。
        console("   等待进度推送(最多 30s, 收到推送或超时后自动继续)...");
        try { trigger.await(30000); } catch (Exception ignore) {}
        Utils.readLine(input, "   按 Enter 退订...");
        if (subId >= 0) {
            try { module.unSubscribeDeviceSimSlotSwitchProgress(subId); }
            catch (Exception e) { DemoLog.warn(name(), "退订失败: " + Utils.describe(e)); }
        }
        hint("SIM 卡槽切换进度订阅已退订");
    }

    // ==================== 子菜单 [8]：云环境切换 ====================

    /**
     * 云环境切换子菜单。
     * <ul>
     *   <li>1. cloudEnvironmentSwitch —— <b>推荐/正确调用链路</b>：内部已按
     *       "先订阅 topic_cloud_environment_switch → 再下发 HTTP → 等待 topic 推送确认切换完成"
     *       的顺序封装, 直接选它即可, 不需要额外手动订阅。</li>
     *   <li>2. cloudEnvironmentSwitchCallback（HTTP POST，携带一机一密鉴权信息上报切换结果）</li>
     *   <li>3. 单独订阅云环境切换进度（仅用于调试/观察 topic 推送, 不建议单独作为切换主流程使用）</li>
     * </ul>
     * 参数（目标云环境 org_id/zone_id/bind_code 等）取自 demo_params.json 的
     * device_debug.cloud_env 配置节，未配置时使用占位默认值仅作演示。
     */
    private void menuCloudEnvironmentSwitch(DeviceDebugModule module, String deviceSn, long timeout) {
        console("\n  --- 云环境切换 ---");
        console("  1. 云环境切换(推荐链路: 订阅topic→下发HTTP→等待topic确认)  (cloudEnvironmentSwitch)");
        console("  2. 云环境切换回调通知   (cloudEnvironmentSwitchCallback)");
        console("  3. [调试用]单独订阅云环境切换进度   (topic_cloud_environment_switch)");
        console("  q. 返回");

        int c = Utils.readIntInRange(input, "请选择: ", 1, 3);
        if (c == Utils.QUIT) return;

        switch (c) {
            case 1: cloudEnvironmentSwitch(module, deviceSn, timeout);         break;
            case 2: cloudEnvironmentSwitchCallback(module, deviceSn, timeout); break;
            case 3: listenCloudEnvironmentSwitchProgress(module, deviceSn); break;
            default: break;
        }
    }

    /**
     * 构造 CloudEnvDeviceList。
     *
     * <p>parents 放机场(默认取当前设备 SN), host 放飞行器(配置了 host_sn 才带上)。
     * callsign / device_model_key 均从 device_debug.cloud_env 配置节读取, 需与后端登记的值一致。
     */
    private List<CloudEnvDeviceList> buildDemoDevicesList(String deviceSn) {
        Node cfg = params.node("device_debug").child("cloud_env");

        CloudEnvDeviceList list = new CloudEnvDeviceList();

        // parents: 机场(遥控器/网关侧), SN 默认用当前选中设备
        String parentSn = cfg.str("parent_sn", deviceSn);
        if (!parentSn.isEmpty()) {
            List<CloudEnvDeviceInfo> parents = new ArrayList<>();
            parents.add(new CloudEnvDeviceInfo(
                cfg.str("parent_callsign", ""),
                cfg.str("parent_device_model_key", ""),
                parentSn));
            list.setParents(parents);
        }

        // host: 飞行器, 仅在配置了 host_sn 时下发
        String hostSn = cfg.str("host_sn", "");
        if (!hostSn.isEmpty()) {
            List<CloudEnvDeviceInfo> host = new ArrayList<>();
            host.add(new CloudEnvDeviceInfo(
                cfg.str("host_callsign", ""),
                cfg.str("host_device_model_key", ""),
                hostSn));
            list.setHost(host);
        }

        List<CloudEnvDeviceList> devicesList = new ArrayList<>();
        devicesList.add(list);
        return devicesList;
    }

    /**
     * 构造 CloudEnvTargetConfig。
     *
     * <p>按 {@code target_type} 只填对应的那一组配置(避免把无关配置也塞进请求体)：
     * 0=司空2 公有云(public_cloud_config), 1/3=三方云 API1/API2(third_cloud_config),
     * 2=司空2 私有云(private_cloud_config)。
     */
    private CloudEnvTargetConfig buildDemoTargetConfig() {
        Node cfg = params.node("device_debug").child("cloud_env");
        int type = cfg.intVal("target_type", 3);
        CloudEnvTargetConfig target = new CloudEnvTargetConfig();
        target.setType(type);
        switch (type) {
            case 0: // 司空2 公有云
                target.getPublicCloudConfig().setBindCode(cfg.str("bind_code", ""));
                target.getPublicCloudConfig().setOrgId(cfg.str("org_id", ""));
                target.getPublicCloudConfig().setZoneId(cfg.str("zone_id", ""));
                break;
            case 2: // 司空2 私有云
                target.getPrivateCloudConfig().setBindCode(cfg.str("bind_code", ""));
                target.getPrivateCloudConfig().setOrgId(cfg.str("org_id", ""));
                target.getPrivateCloudConfig().setUrl(cfg.str("private_url", ""));
                break;
            case 1: // 三方云 API1
            case 3: // 三方云 API2
            default:
                target.getThirdCloudConfig().setBindCode(cfg.str("bind_code", ""));
                target.getThirdCloudConfig().setOrgId(cfg.str("org_id", ""));
                target.getThirdCloudConfig().setMqttAddr(cfg.str("mqtt_addr", ""));
                target.getThirdCloudConfig().setMqttUsername(cfg.str("mqtt_username", ""));
                target.getThirdCloudConfig().setMqttPassword(cfg.str("mqtt_password", ""));
                break;
        }
        return target;
    }

    /** 打印本次将要下发的目标云环境配置(密码脱敏), 便于和后端约定的报文比对。 */
    private void printTargetConfig(CloudEnvTargetConfig target) {
        console("     target_config.type = " + target.getType()
            + "  (0=司空2公有云 1=三方云API1 2=司空2私有云 3=三方云API2)");
        switch (target.getType()) {
            case 0:
                console("     public_cloud_config: bind_code=" + target.getPublicCloudConfig().getBindCode()
                    + " org_id=" + target.getPublicCloudConfig().getOrgId()
                    + " zone_id=" + target.getPublicCloudConfig().getZoneId());
                break;
            case 2:
                console("     private_cloud_config: bind_code=" + target.getPrivateCloudConfig().getBindCode()
                    + " org_id=" + target.getPrivateCloudConfig().getOrgId()
                    + " url=" + target.getPrivateCloudConfig().getUrl());
                break;
            default:
                console("     third_cloud_config: bind_code=" + target.getThirdCloudConfig().getBindCode()
                    + " org_id=" + target.getThirdCloudConfig().getOrgId()
                    + " mqtt_addr=" + target.getThirdCloudConfig().getMqttAddr()
                    + " mqtt_username=" + target.getThirdCloudConfig().getMqttUsername()
                    + " mqtt_password=" + (target.getThirdCloudConfig().getMqttPassword().isEmpty() ? "(空)" : "***"));
                break;
        }
    }

    /** 打印本次将要下发的设备列表。 */
    private void printDevicesList(List<CloudEnvDeviceList> devicesList) {
        for (int i = 0; i < devicesList.size(); i++) {
            CloudEnvDeviceList l = devicesList.get(i);
            console("     devices_list[" + i + "]:");
            printDeviceInfos("host", l.getHost());
            printDeviceInfos("nodes", l.getNodes());
            printDeviceInfos("parents", l.getParents());
        }
    }

    private void printDeviceInfos(String label, List<CloudEnvDeviceInfo> infos) {
        if (infos == null || infos.isEmpty()) {
            return;
        }
        for (CloudEnvDeviceInfo info : infos) {
            console("       " + label + ": callsign=" + info.getCallsign()
                + " device_model_key=" + info.getDeviceModelKey()
                + " device_sn=" + info.getDeviceSn());
        }
    }

    /**
     * 云环境切换下发通知(HTTP POST /device/api/v1/service/cloud-environment-switch)。
     *
     * <p><b>正确调用链路(重要, 曾经踩过坑)</b>：{@code cloud-environment-switch} 接口的
     * HTTP 响应 {@code code:0} 只代表"服务端已受理该切换指令", <b>不代表设备已经完成切换</b>。
     * 设备真正完成云环境切换是异步的, 完成后由服务端通过 WebSocket
     * {@code topic_cloud_environment_switch} 推送最终结果(bid/status/progress)。
     *
     * <p>正确顺序必须是：
     * <ol>
     *   <li>先订阅 {@code topic_cloud_environment_switch}(建立监听, 不能等 HTTP 返回后再订阅——
     *       否则如果切换发生得很快, 推送可能在订阅建立前就已经到达并错过);</li>
     *   <li>再下发 HTTP {@code cloudEnvironmentSwitch} 请求, 拿到 {@code code:0} 只是"指令已受理"；</li>
     *   <li>等待 topic 推送的最终切换结果, 才是真正的"切换完成"。</li>
     * </ol>
     *
     * <p>之前 demo 里这三步是拆成三个独立菜单项、需要手动依次触发的, 容易被跳过 topic 等待、
     * 或者在设备已经因指令生效切走之后才发起第二次调用(此时设备已不在原环境, 服务端会返回
     * {@code some devices failed to switch cloud environment} 类业务错误, 极易被误判为"接口调用失败",
     * 实际是"调用链路时序不对, 命令已经生效过一次了")。现已合并为单次调用内的完整流程。
     */
    private void cloudEnvironmentSwitch(DeviceDebugModule module, String deviceSn, long timeout) {
        CloudEnvironmentSwitchParam param = new CloudEnvironmentSwitchParam();
        param.setDevicesList(buildDemoDevicesList(deviceSn));
        param.setTargetConfig(buildDemoTargetConfig());

        console("   云环境切换(完整流程: 先订阅 topic → 下发 HTTP → 等待 topic 确认切换结果)");
        console("   POST /device/api/v1/service/cloud-environment-switch");
        printDevicesList(param.getDevicesList());
        printTargetConfig(param.getTargetConfig());

        // Step 1: 先订阅 topic_cloud_environment_switch, 必须在 HTTP 下发之前建立监听。
        //
        // 关键点(踩过的坑): 订阅用的 device_sn 必须是"真正要切换云环境的目标设备"(即
        // devices_list 里 parents/host 配置的那个 SN), 而不是 demo 菜单里临时选中的那台设备
        // ——两者经常不是同一台, 订阅错设备会导致永远等不到推送(表现为"超时", 但其实请求
        // 已经处理完成, 只是我们监听错了频道)。parents(网关/机场)和 host(飞行器)都有可能是
        // 推送的目标, 两个都配置了就都订阅, 谁先推送就用谁的结果, 避免因为猜错而错过。
        Node cloudEnvCfg = params.node("device_debug").child("cloud_env");
        String subscribeParentSn = cloudEnvCfg.str("parent_sn", "");
        String subscribeHostSn = cloudEnvCfg.str("host_sn", "");
        if (subscribeParentSn.isEmpty() && subscribeHostSn.isEmpty()) {
            // 均未配置时退回菜单选中设备, 保持向下兼容(仅用于纯演示场景)。
            subscribeParentSn = deviceSn;
        }

        final Await<CloudEnvironmentSwitchResult> topicResult = new Await<>(name() + ".cloudEnvironmentSwitchTopic");
        List<Integer> subIds = new ArrayList<>();
        List<String> subscribedSns = new ArrayList<>();
        for (String sn : new String[] { subscribeParentSn, subscribeHostSn }) {
            if (sn == null || sn.isEmpty() || subscribedSns.contains(sn)) {
                continue;
            }
            try {
                int subId = module.listenDeviceCloudEnvironmentSwitchProgress(sn, new FlightHubCallback.DeviceCloudEnvironmentSwitchProgressCallback() {
                    @Override
                    public void onResult(CloudEnvironmentSwitchResult r) {
                        if (r == null) return;
                        String line = "topic_cloud_environment_switch 推送(sn=" + sn + "): bid=" + r.getBid()
                            + " msg=" + r.getMessage() + " progress=" + r.getProgress();
                        DemoLog.info(name(), line);
                        console("   [topic 推送] " + line);
                        topicResult.set(r);
                    }
                });
                subIds.add(subId);
                subscribedSns.add(sn);
                console("   Step1: 已订阅 topic_cloud_environment_switch(sn=" + sn + ", listenId=" + subId + ")");
            } catch (Exception e) {
                hint("[警告] 订阅 topic_cloud_environment_switch(sn=" + sn + ") 失败: " + Utils.describe(e));
            }
        }
        if (subIds.isEmpty()) {
            hint("[警告] 没有任何订阅成功建立; 仍继续下发请求, 但无法感知真实切换结果(只能看 HTTP 层受理结果)");
        }

        // Step 2: 下发 HTTP 请求; code:0 只代表"指令已受理", 不代表切换已完成。
        console("   Step2: 下发 cloudEnvironmentSwitch HTTP 请求 ...");
        final Await<FlightHubError> httpAck = new Await<>(name() + ".cloudEnvironmentSwitchHttp");
        ModuleDemo.Outcome ackOutcome = runOutcome("cloudEnvironmentSwitch(HTTP下发)", timeout, httpAck, () ->
            module.cloudEnvironmentSwitch(param, new FlightHubCallback.ResultCallbackWithError() {
                @Override
                public void onResult(FlightHubError error) {
                    httpAck.set(error);
                }
            })
        );

        // Step 3: 只有 HTTP 受理成功(code:0), 才等待 topic 推送的最终切换结果; 下发失败/超时则没有
        // 意义再等(命令根本没被接受, 或状态未知, 不应该假装能等到确认)。
        if (ackOutcome == ModuleDemo.Outcome.SUCCESS) {
            console("   Step3: HTTP 已受理(code:0), 等待 topic 推送真正的切换完成结果(最多 "
                + PROGRESS_WAIT_MS / 1000 + "s) ...");
            try {
                CloudEnvironmentSwitchResult r = topicResult.await(PROGRESS_WAIT_MS);
                console("   ✅ 切换流程完成: bid=" + r.getBid() + " msg=" + r.getMessage() + " progress=" + r.getProgress());
            } catch (Exception e) {
                hint("[警告] 等待 topic 推送超时/异常: " + Utils.describe(e)
                    + "; HTTP 已受理指令, 但未在超时时间内收到切换完成推送, 请去服务端/设备侧确认实际状态"
                    + "(注意: 此时不要重复调用 cloudEnvironmentSwitch, 设备可能已经在切换中或已切换完成,"
                    + "重复下发在设备已离开原环境后会收到 serverCode=210271 之类的业务错误)");
            }
        } else {
            hint("HTTP 下发未成功(" + ackOutcome + "), 不再等待 topic 推送");
        }

        for (int subId : subIds) {
            try { module.cancelListenDeviceCloudEnvironmentSwitchProgress(subId); }
            catch (Exception e) { DemoLog.warn(name(), "退订失败(listenId=" + subId + "): " + Utils.describe(e)); }
        }
    }

    /** 云环境切换回调通知(HTTP POST /device/api/v1/service/cloud_environment_switch/callback)。 */
    private void cloudEnvironmentSwitchCallback(DeviceDebugModule module, String deviceSn, long timeout) {
        Node cfg = params.node("device_debug").child("cloud_env");
        CloudEnvironmentSwitchCallbackParam param = new CloudEnvironmentSwitchCallbackParam();
        param.setCode(cfg.intVal("callback_code", 0));
        param.setStatus(cfg.str("callback_status", "ok"));
        DeviceAuthVerifyParam authVerify = new DeviceAuthVerifyParam();
        authVerify.setSn(cfg.str("auth_sn", deviceSn));
        authVerify.setSecret(cfg.str("device_secret", ""));
        authVerify.setNonce(java.util.UUID.randomUUID().toString());
        param.setDeviceAuthVerify(authVerify);
        param.setDevicesList(buildDemoDevicesList(deviceSn));
        param.setTargetConfig(buildDemoTargetConfig());

        console("   云环境切换回调通知: POST /device/api/v1/service/cloud_environment_switch/callback");
        console("     code=" + param.getCode() + " status=" + param.getStatus());
        console("     device_auth_verify: sn=" + param.getDeviceAuthVerify().getSn()
            + " secret=" + (param.getDeviceAuthVerify().getSecret().isEmpty() ? "(空, 请在 cloud_env.device_secret 配置)" : "***")
            + " nonce=" + param.getDeviceAuthVerify().getNonce());
        printDevicesList(param.getDevicesList());
        printTargetConfig(param.getTargetConfig());
        callWithMessageResult("cloudEnvironmentSwitchCallback", timeout, await ->
            module.cloudEnvironmentSwitchCallback(param, new FlightHubCallback.ResultCallbackWithError() {
                @Override
                public void onResult(FlightHubError error) {
                    await.set(error);
                }
            })
        );
    }

    /** 订阅云环境切换进度(WS topic_cloud_environment_switch)。 */
    private void listenCloudEnvironmentSwitchProgress(DeviceDebugModule module, String deviceSn) {
        console("   订阅云环境切换进度(WS): sn=" + deviceSn);
        final Await<Integer> trigger = new Await<>(name() + ".cloudEnvironmentSwitchProgressTrigger");
        int subId = -1;
        try {
            subId = module.listenDeviceCloudEnvironmentSwitchProgress(deviceSn, new FlightHubCallback.DeviceCloudEnvironmentSwitchProgressCallback() {
                @Override
                public void onResult(CloudEnvironmentSwitchResult r) {
                    if (r == null) return;
                    String line = "云环境切换进度: bid=" + r.getBid()
                        + " msg=" + r.getMessage()
                        + " progress=" + r.getProgress();
                    DemoLog.info(name(), line);
                    console("   [进度推送] " + line);
                    trigger.set(1);
                }
            });
        } catch (Exception e) {
            hint("[警告] 订阅失败: " + Utils.describe(e));
            return;
        }
        console("   等待进度推送(最多 30s, 收到推送或超时后自动继续)...");
        try { trigger.await(30000); } catch (Exception ignore) { /* 超时走到这里 */ }
        Utils.readLine(input, "   按 Enter 退订...");
        if (subId >= 0) {
            try { module.cancelListenDeviceCloudEnvironmentSwitchProgress(subId); }
            catch (Exception e) { DemoLog.warn(name(), "退订失败: " + Utils.describe(e)); }
        }
        hint("云环境切换进度订阅已退订");
    }

    private void deviceReboot(DeviceDebugModule module, String deviceSn, long timeout) {
        if (!confirmDangerous("设备重启(机场将重启, 飞行中请勿执行！)")) return;

        final Await<FlightHubError> progressAwait = new Await<>(name() + ".deviceRebootProgress");
        Subscription progressSub = null;
        try {
            int subId = module.subscribeDeviceRebootProgress(deviceSn, new FlightHubCallback.SubscribeDeviceRebootProgressCallback() {
                @Override
                public void onResult(DeviceRebootResult result) {
                    if (result == null) return;
                    String line = "设备重启进度: bid=" + result.getBid()
                        + " msg=" + result.getMessage();
                    DemoLog.info(name(), line);
                    console("   [进度推送] " + line);
                    progressAwait.set(null);
                }
            });

            progressSub = Subscription.of("deviceRebootProgress", subId, id -> module.unSubscribeDeviceRebootProgress(id));
            registry().add(progressSub);   // 登记会话 registry 兜底, 防异常路径监听泄漏。
        } catch (Exception e) {
            hint("[警告] 进度订阅建立失败: " + Utils.describe(e));
        }

        try {
            final Await<FlightHubError> cmdAwait = new Await<>(name() + ".deviceReboot");
            final AtomicReference<DeviceRebootResult> resultRef = new AtomicReference<>();
            Outcome o = runOutcome("设备重启(指令下发)", timeout, cmdAwait, () ->
                module.deviceReboot(deviceSn, new FlightHubCallback.DeviceRebootCallback() {
                    @Override
                    public void onResult(FlightHubError error, DeviceRebootResult result) {
                        resultRef.set(result);
                        cmdAwait.set(error);
                    }
                })
            );
            printBidMsg("deviceReboot", resultRef.get() != null ? resultRef.get().getBid() : null, resultRef.get() != null ? resultRef.get().getMessage() : null);

            if (o != Outcome.FAILED && progressSub != null) {
                console("   >> 等待重启进度推送(最多 " + PROGRESS_WAIT_MS / 1000 + "s)...");
                try { progressAwait.await(PROGRESS_WAIT_MS); }
                catch (Exception e) { hint("[进度] 等待超时: " + Utils.describe(e)); }
            }
        } finally {
            if (progressSub != null) {
                progressSub.close();
                registry().remove(progressSub);
            }
        }
    }

    private void deviceFormat(DeviceDebugModule module, String deviceSn, long timeout) {
        if (!confirmDangerous("机场格式化(将清除机场存储数据！)")) return;

        final Await<FlightHubError> progressAwait = new Await<>(name() + ".deviceFormatProgress");
        Subscription progressSub = null;
        try {
            int subId = module.subscribeDeviceFormatProgress(deviceSn, new FlightHubCallback.SubscribeDeviceFormatProgressCallback() {
                @Override
                public void onResult(DeviceFormatResult result) {
                    if (result == null) return;
                    String line = "机场格式化进度: bid=" + result.getBid()
                        + " msg=" + result.getMessage();
                    DemoLog.info(name(), line);
                    console("   [进度推送] " + line);
                    progressAwait.set(null);
                }
            });

            progressSub = Subscription.of("deviceFormatProgress", subId, id -> module.unSubscribeDeviceFormatProgress(id));
            registry().add(progressSub);   // 登记会话 registry 兜底, 防异常路径监听泄漏。
        } catch (Exception e) {
            hint("[警告] 进度订阅建立失败: " + Utils.describe(e));
        }

        try {
            final Await<FlightHubError> cmdAwait = new Await<>(name() + ".deviceFormat");
            final AtomicReference<DeviceFormatResult> resultRef = new AtomicReference<>();
            Outcome o = runOutcome("机场格式化(指令下发)", timeout, cmdAwait, () ->
                module.deviceFormat(deviceSn, new FlightHubCallback.DeviceFormatCallback() {
                    @Override
                    public void onResult(FlightHubError error, DeviceFormatResult result) {
                        resultRef.set(result);
                        cmdAwait.set(error);
                    }
                })
            );
            printBidMsg("deviceFormat", resultRef.get() != null ? resultRef.get().getBid() : null, resultRef.get() != null ? resultRef.get().getMessage() : null);

            if (o != Outcome.FAILED && progressSub != null) {
                console("   >> 等待格式化进度推送(最多 " + PROGRESS_WAIT_MS / 1000 + "s)...");
                try { progressAwait.await(PROGRESS_WAIT_MS); }
                catch (Exception e) { hint("[进度] 等待超时: " + Utils.describe(e)); }
            }
        } finally {
            if (progressSub != null) {
                progressSub.close();
                registry().remove(progressSub);
            }
        }
    }

    /**
     * 手动换电：
     * <ol>
     *   <li>先调 {@code getManualBatterySwapProgress} 查询当前电池仓状态, 解析 {@code dataJson}
     *       展示可用仓位列表；</li>
     *   <li>无可用仓位时提示并中止, 不触发换电指令；</li>
     *   <li>用户选择目标仓位后调 {@code startManualBatterySwap} 下发换电指令；</li>
     *   <li>下发成功后再次查询进度确认。</li>
     * </ol>
     *
     * <p>注意：{@code dataJson} 为服务端透传的原始 JSON, 格式由设备型号和固件版本决定。
     * 典型格式(Dock 3)：
     * <pre>
     * {
     *   "battery_slot_infos": [
     *     {"slot_id": 0, "slot_state": 1, "capacity_percent": 85},
     *     {"slot_id": 1, "slot_state": 0, "capacity_percent": 0},
     *     ...
     *   ]
     * }
     * </pre>
     * slot_state: 0=空/不可用, 1=有电池/可换, 2=换电中。
     * 若服务端无 dataJson(非 Dock3 或固件不支持), 则退回到配置文件 slotId。
     */
    private void manualBatterySwap(DeviceDebugModule module, String deviceSn, long timeout) {
        // Step 1: 查询当前换电进度/仓位状态
        console("\n   >> [手动换电] 查询电池仓状态...");
        final Await<FlightHubError> queryAwait = new Await<>(name() + ".getBatterySwapProgress");
        final AtomicReference<ManualBatterySwapProgressResult> progressRef = new AtomicReference<>();
        Outcome queryOutcome = runOutcome("查询电池仓状态", timeout, queryAwait, () ->
            module.getManualBatterySwapProgress(deviceSn, new FlightHubCallback.ManualBatterySwapProgressCallback() {
                @Override
                public void onResult(FlightHubError error, ManualBatterySwapProgressResult result) {
                    progressRef.set(result);
                    queryAwait.set(error);
                }
            })
        );

        // Step 2: 解析 dataJson, 展示可用仓位让用户选择
        int selectedSlotId = -1;
        ManualBatterySwapProgressResult progress = progressRef.get();
        boolean hasBatterySlotInfo = queryOutcome == Outcome.SUCCESS && progress != null
                && progress.getDataJson() != null && !progress.getDataJson().isEmpty()
                && progress.getDataJson().contains("battery_slot_infos");

        if (hasBatterySlotInfo) {
            // 服务端返回了结构化的仓位数据(部分型号/固件支持)
            selectedSlotId = parseAndSelectBatterySlot(progress.getDataJson());
        } else {
            // SDK 未封装 battery_system, getManualBatterySwapProgress 返回的 dataJson 不含仓位信息。
            // 原始 dataJson 打印出来供参考, 同时说明如何查阅仓位状态。
            if (queryOutcome == Outcome.SUCCESS && progress != null) {
                console("   服务端返回: " + progress.getDataJson());
            }
            console("\n   [提示] 当前设备的换电接口未返回电池仓位信息(battery_slot_infos)。");
            console("   可通过以下方式查看电池仓位状态：");
            console("     · 上云 API: GET /device/api/v1/manager/devices?sn=" + deviceSn);
            console("       → 在 device_state.battery_system.slots 中查看各仓位 pack_presence/capacity_percent");
            console("     · 设备调试界面或机场实物面板直接确认当前仓位状态");
            console("     pack_presence: 0=空仓  1=有电池可换");
            console("");
            // 让用户手动输入 slotId
            selectedSlotId = askSlotId();
        }

        if (selectedSlotId < 0) {
            // parseAndSelectBatterySlot 内部已打印原因(无可用仓位或用户取消)
            return;
        }

        // Step 3: 下发换电指令
        console("   >> 下发手动换电指令: slotId=" + selectedSlotId);
        final int finalSlotId = selectedSlotId;
        final Await<FlightHubError> cmdAwait = new Await<>(name() + ".startManualBatterySwap");
        final AtomicReference<ManualBatterySwapResult> resultRef = new AtomicReference<>();
        Outcome o = runOutcome("发起手动换电", timeout, cmdAwait, () ->
            module.startManualBatterySwap(deviceSn, finalSlotId, new FlightHubCallback.ManualBatterySwapCallback() {
                @Override
                public void onResult(FlightHubError error, ManualBatterySwapResult result) {
                    resultRef.set(result);
                    cmdAwait.set(error);
                }
            })
        );

        printBidMsg(
            "startManualBatterySwap", resultRef.get() != null ? resultRef.get().getBid() : null,
            resultRef.get() != null ? resultRef.get().getMessage() : null
        );

        // Step 4: 换电成功后进入进度查询循环, 直到换电完成或用户主动退出
        if (o == Outcome.SUCCESS) {
            hint("换电指令已下发, 进入进度查询循环(输入 q 退出查询)...");
            pollBatterySwapProgress(module, deviceSn, timeout);
        }
    }

    /**
     * 换电进度轮询循环：每次查询后展示仓位状态, 让用户决定继续查询还是退出。
     *
     * <p>退出条件：
     * <ul>
     *   <li>用户输入 q；</li>
     *   <li>检测到换电已完成(所有仓位均不处于「换电中 slot_state=2」状态)；</li>
     *   <li>连续查询失败超过 3 次。</li>
     * </ul>
     */
    private void pollBatterySwapProgress(DeviceDebugModule module, String deviceSn, long timeout) {
        int failCount = 0;
        final int maxFail = 3;
        int round = 0;

        while (true) {
            round++;
            console("\n   >> [换电进度查询 #" + round + "]");

            final Await<FlightHubError> await = new Await<>(name() + ".swapProgress#" + round);
            final AtomicReference<ManualBatterySwapProgressResult> ref = new AtomicReference<>();
            Outcome qo = runOutcome("查询换电进度", timeout, await, () ->
                module.getManualBatterySwapProgress(deviceSn, new FlightHubCallback.ManualBatterySwapProgressCallback() {
                    @Override
                    public void onResult(FlightHubError error, ManualBatterySwapProgressResult result) {
                        ref.set(result);
                        await.set(error);
                    }
                })
            );

            if (qo != Outcome.SUCCESS) {
                failCount++;
                hint("查询失败(" + failCount + "/" + maxFail + ")");
                if (failCount >= maxFail) {
                    hint("连续查询失败 " + maxFail + " 次, 退出进度查询");
                    return;
                }
            } else {
                failCount = 0;
                ManualBatterySwapProgressResult result = ref.get();
                if (result != null) {
                    DemoLog.info(name(), "swapProgress#" + round + ": " + result);

                    // 优先从 dataJson 解析 status 字段判断换电进度
                    // status: "ok"=换电完成, "in_progress"=换电中, 其他=未知
                    String swapStatus = parseSwapStatus(result.getDataJson());
                    console("     状态: " + describeSwapStatus(swapStatus) + "  bid=" + result.getBid());

                    if ("ok".equals(swapStatus)) {
                        hint("换电已完成(status=ok)");
                        // 若有仓位信息也打出来
                        if (result.getDataJson() != null && !result.getDataJson().isEmpty()
                                && result.getDataJson().contains("battery_slot_infos")) {
                            printBatterySlotInfo(result.getDataJson());
                        }
                        return; // 完成, 直接退出循环
                    }

                    if ("in_progress".equals(swapStatus)) {
                        // 换电仍在进行, 继续轮询
                        if (result.getDataJson() != null && !result.getDataJson().isEmpty()
                                && result.getDataJson().contains("battery_slot_infos")) {
                            printBatterySlotInfo(result.getDataJson());
                        }
                    } else {
                        // status 为空/未知(如设备不支持返回结构化 status)
                        // 降级：用 dataJson 里有无 battery_slot_infos 且 slot_state=2 判断
                        boolean swapping = false;
                        if (result.getDataJson() != null && !result.getDataJson().isEmpty()) {
                            swapping = printBatterySlotInfo(result.getDataJson());
                        }
                        if (!swapping) {
                            hint("换电状态未知(设备未返回 status 字段), "
                                + "且无仓位处于换电中, 换电可能已完成。");
                            console("   继续查询？(按 Enter 再查一次 / 输入 q 退出): ");
                            String pollLine = Utils.readLine(input, "");
                            if (pollLine == null || pollLine.equalsIgnoreCase("q")) {
                                hint("退出进度查询");
                                return;
                            }
                            continue;
                        }
                    }
                }
            }

            // 换电仍在进行：提示用户选择
            console("\n   换电进行中, 选择操作:");
            console("   1. 再次查询进度   q. 退出查询(换电仍在后台进行)");
            String line = Utils.readLine(input, "   请选择: ");
            if (line == null || line.equalsIgnoreCase("q")) {
                hint("退出进度查询(换电仍在后台进行, 可重新进入本菜单查询)");
                return;
            }
        }
    }

    /**
     * 让用户手动输入目标电池仓位 ID(当服务端未返回结构化仓位数据时使用)。
     *
     * @return 用户输入的 slotId(>=0)；-1 表示用户取消
     */
    private int askSlotId() {
        // 先给出配置文件默认值作为参考
        int cfgDefault = params.node("device_debug").intVal("manual_swap_slot_id", -1);
        String prompt = cfgDefault >= 0
                ? "   请输入目标电池仓位 ID(配置默认值: " + cfgDefault + ", 直接回车使用默认, 输入 q 取消): "
                : "   请输入目标电池仓位 ID(如 3 或 4, 输入 q 取消): ";

        while (true) {
            String line = Utils.readLine(input, prompt);
            if (line == null || line.equalsIgnoreCase("q")) {
                hint("已取消换电");
                return -1;
            }
            // 直接回车使用配置默认值
            if (line.trim().isEmpty() && cfgDefault >= 0) {
                console("   使用配置默认值 slotId=" + cfgDefault);
                return cfgDefault;
            }
            try {
                int slotId = Integer.parseInt(line.trim());
                if (slotId < 0) {
                    console("   仓位 ID 不能为负数, 请重新输入");
                    continue;
                }
                return slotId;
            } catch (NumberFormatException e) {
                console("   输入无效, 请输入数字仓位 ID(如 3)或 q 取消");
            }
        }
    }

    /**
     * 解析 dataJson 中的电池仓信息, 展示并让用户选择目标仓位。
     *
     * @param dataJson 服务端透传的原始 JSON
     * @return 用户选择的 slotId；-1 表示无可用仓位或用户取消
     */
    private int parseAndSelectBatterySlot(String dataJson) {
        // 简单 JSON 解析：找 battery_slot_infos 数组
        // 格式: {"battery_slot_infos":[{"slot_id":0,"slot_state":1,"capacity_percent":85},...]}
        // slot_state: 0=空/不可用, 1=有电池/可换, 2=换电中
        List<int[]> availableSlots = new ArrayList<>(); // [slotId, slotState, capacityPercent]
        List<int[]> allSlots = new ArrayList<>();

        try {
            // 用 Node/Json 解析
            Node root = new Node(Json.parse(dataJson));
            List<Object> slotInfos = root.list("battery_slot_infos");
            for (Object o : slotInfos) {
                Node n = new Node(o);
                int slotId = n.intVal("slot_id", -1);
                int slotState = n.intVal("slot_state", 0);
                int capacity = n.intVal("capacity_percent", 0);
                if (slotId >= 0) {
                    allSlots.add(new int[]{slotId, slotState, capacity});
                    if (slotState == 1) { // 1=有电池/可换
                        availableSlots.add(new int[]{slotId, slotState, capacity});
                    }
                }
            }
        } catch (Exception e) {
            DemoLog.warn(
                name(), "解析 battery_slot_infos 失败: " + Utils.describe(e)
                    + " dataJson=" + dataJson
            );
        }

        // 打印所有仓位状态
        console("\n   电池仓状态:");
        if (allSlots.isEmpty()) {
            // dataJson 有效但无 battery_slot_infos(可能是进度尚未开始的空结构)
            console("     暂无电池仓位信息(dataJson=" + dataJson + ")");
        } else {
            for (int[] slot : allSlots) {
                String stateDesc = slot[1] == 0 ? "空/不可用" : slot[1] == 1 ? "有电池/可换" : "换电中";
                console("     仓位 " + slot[0] + ": " + stateDesc + (slot[1] != 0 ? "  电量 " + slot[2] + "%" : ""));
            }
        }

        if (availableSlots.isEmpty()) {
            hint("当前无可用电池仓(slot_state=1), 无法发起换电。请确认机场已装载电池且处于远程调试模式。");
            return -1;
        }

        // 让用户选择目标仓位
        console("\n   可换仓位列表:");
        for (int i = 0; i < availableSlots.size(); i++) {
            int[] slot = availableSlots.get(i);
            console("     " + (i + 1) + ". 仓位 " + slot[0] + "  电量 " + slot[2] + "%");
        }
        int choice = Utils.readIntInRange(input, "   请选择目标仓位(1-" + availableSlots.size() + ", q 取消): ", 1, availableSlots.size());
        if (choice == Utils.QUIT) {
            hint("已取消换电");
            return -1;
        }
        return availableSlots.get(choice - 1)[0]; // 返回 slotId
    }

    /**
     * 从 getManualBatterySwapProgress 返回的 dataJson 中解析 status 字段。
     *
     * <p>服务端返回的 data 结构：
     * <pre>{"bid":"...","status":"ok","create_time":...,"update_time":...}</pre>
     * status 枚举：{@code "ok"}=换电完成, {@code "in_progress"}=换电中。
     *
     * @return status 字符串(如 "ok"/"in_progress"), 解析失败或无该字段时返回空字符串
     */
    private String parseSwapStatus(String dataJson) {
        if (dataJson == null || dataJson.isEmpty()) return "";
        try {
            Node root = new Node(Json.parse(dataJson));
            return root.str("status", "");
        } catch (Exception e) {
            DemoLog.warn(name(), "解析 swap status 失败: " + Utils.describe(e));
            return "";
        }
    }

    /** 将 status 字符串转为用户可读描述。 */
    private static String describeSwapStatus(String status) {
        if ("ok".equals(status))          return "换电完成 ✓ (status=ok)";
        if ("in_progress".equals(status)) return "换电中... (status=in_progress)";
        if (status == null || status.isEmpty()) return "未知(status 字段缺失)";
        return "未知(" + status + ")";
    }

    /**
     * 打印电池仓状态(换电进度查询时使用)。
     *
     * @return true 表示有仓位处于「换电中(slot_state=2)」, 换电尚未结束；false 表示换电已完成或无仓位信息
     */
    private boolean printBatterySlotInfo(String dataJson) {
        boolean swapping = false;
        try {
            Node root = new Node(Json.parse(dataJson));
            List<Object> slotInfos = root.list("battery_slot_infos");
            if (!slotInfos.isEmpty()) {
                console("     当前仓位状态:");
                for (Object o : slotInfos) {
                    Node n = new Node(o);
                    int slotId = n.intVal("slot_id", -1);
                    int slotState = n.intVal("slot_state", 0);
                    int capacity = n.intVal("capacity_percent", 0);
                    String stateDesc = slotState == 0 ? "空/不可用" : slotState == 1 ? "有电池" : ">>> 换电中 <<<";
                    console("       仓位 " + slotId + ": " + stateDesc + (slotState != 0 ? "  电量 " + capacity + "%" : ""));
                    if (slotState == 2) swapping = true;
                }
            }
        } catch (Exception e) {
            DemoLog.warn(name(), "解析仓位信息失败: " + Utils.describe(e));
        }
        return swapping;
    }

    // ==================== 辅助方法 ====================

    /**
     * 远程调试模式前置检查：查询当前 mode_code, 给用户明确的状态反馈和引导。
     *
     * <p>机场 mode_code 语义(0-9)见 {@link #describeModeCode}(出自 SDK bean {@code DockModeCode} 类注释)。
     * 物理操作类接口(开关机/舱盖/充电/重启/格式化等)均需 mode_code=2。
     *
     * <p>流程：
     * <ol>
     *   <li>订阅 {@code listenDockModeCode}, 等待首次推送(已有缓存值会立即推送)；</li>
     *   <li>mode_code=2 → 直接放行, 打印当前状态；</li>
     *   <li>mode_code≠2 或 WS 推送超时(未知) → 打印当前状态 → 自动调用
     *       {@code openDebugMode} 开启(重复开启预期无害, 幂等性未见 SDK 文档背书; 无需用户确认)
     *       → 开启成功才放行, 失败则中止。</li>
     * </ol>
     *
     * @param operation  操作名称, 用于提示文案
     * @param module     DeviceDebugModule 实例(开启调试模式用)
     * @param deviceSn   机场 SN
     * @param timeout    超时毫秒
     * @return true=可以继续执行；false=未开启调试模式且用户未授权开启, 应中止
     */
    private boolean checkAndEnsureDebugMode(String operation, DeviceDebugModule module, String deviceSn, long timeout) {
        // Step 1: 订阅一次 mode_code, 等首帧推送
        // 注意：WS 连接建立需要时间, 首帧推送可能在 5s 内到不了。
        // 为兼容 WS 未就绪的场景, 超时后继续流程, 以 Step 4 的 openDebugMode 返回值为准。
        final AtomicInteger modeCodeRef = new AtomicInteger(-1);
        final Await<Integer> modeAwait = new Await<>(name() + ".listenDockModeCode");

        DockOsdModule dockOsd = session.dock().getDockOsdModule();
        int listenId = -1;
        try {
            listenId = dockOsd.listenDockModeCode(new FlightHubCallback.DockModeCodeCallback() {
                @Override
                public void onResult(DockModeCode modeCode) {
                    if (modeCode != null && !modeAwait.isDone()) {
                        modeCodeRef.set(modeCode.getModeCode());
                        modeAwait.set(modeCode.getModeCode());
                    }
                }
            });
        } catch (Exception e) {
            DemoLog.warn(name(), "listenDockModeCode 失败: " + Utils.describe(e));
        }

        // 等待首次推送, 最多 5s
        try {
            modeAwait.await(5000);
        } catch (Exception e) {
            DemoLog.warn(name(), "等待 mode_code 超时(WS 连接尚未就绪), 将直接尝试开启调试模式");
        } finally {
            if (listenId >= 0) {
                try { dockOsd.cancelListenDockModeCode(listenId); } catch (Exception ignore) {}
            }
        }

        // Step 2: 展示当前状态
        int modeCode = modeCodeRef.get();
        console("\n   [远程调试模式检查] 操作: " + operation);
        if (modeCode == 2) {
            // WS 推送正常到达且已确认是远程调试模式, 直接放行
            console("   当前 mode_code: 2(远程调试中)");
            console("   ✓ 机场已处于远程调试模式, 可以执行 " + operation);
            return true;
        }

        // Step 3: WS 超时(modeCode=-1) 或 mode_code 非 2 — 统一走开启流程
        // WS 超时时不做假设, 直接调 openDebugMode：
        //   · 若机场已是 mode_code=2, openDebugMode 预期仍返回成功(幂等性待实测确认)；
        //   · 若机场不是 mode_code=2, openDebugMode 会真正开启。
        // 两种情况都能正确处理, 不再依赖 WS 推送。
        if (modeCode < 0) {
            console("   当前 mode_code: WS 推送未到(连接尚未就绪), 自动尝试开启远程调试模式...");
        } else {
            console("   当前 mode_code: " + modeCode + "(" + describeModeCode(modeCode) + ")");
            console("   ✗ " + operation + " 需要机场处于远程调试模式(mode_code=2), 尝试开启...");
        }

        // Step 4: 直接调用 openDebugMode(无需用户确认, WS超时场景自动处理)
        console("");
        final Await<FlightHubError> openAwait = new Await<>(name() + ".openDebugMode");
        final AtomicReference<OpenDebugModeResult> openResultRef = new AtomicReference<>();
        Outcome openOutcome = runOutcome("打开远程调试模式", timeout, openAwait, () ->
            module.openDebugMode(deviceSn, new FlightHubCallback.OpenDebugModeCallback() {
                @Override
                public void onResult(FlightHubError error, OpenDebugModeResult result) {
                    openResultRef.set(result);
                    openAwait.set(error);
                }
            })
        );
        printBidMsg("openDebugMode", openResultRef.get() != null ? openResultRef.get().getBid() : null,
                openResultRef.get() != null ? openResultRef.get().getMessage() : null);

        if (openOutcome == Outcome.SUCCESS) {
            hint("远程调试模式开启成功, 继续执行 " + operation);
            return true;
        } else if (openOutcome == Outcome.TIMEOUT) {
            hint("openDebugMode 超时(命令可能已生效), 继续尝试执行 " + operation);
            return true; // 超时按「可能已生效」放行
        } else {
            hint("远程调试模式开启失败, 无法执行 " + operation
                + "。请检查机场状态后重试。");
            return false;
        }
    }

    /** mode_code 枚举描述(0-9, 见 SDK bean {@link DockModeCode} 类注释)。 */
    private static String describeModeCode(int code) {
        switch (code) {
            case 0: return "空闲中";
            case 1: return "现场调试中";
            case 2: return "远程调试中";
            case 3: return "设备升级中";
            case 4: return "设备作业中";
            case 5: return "机场设备未标定";
            case 6: return "节能状态中";
            case 7: return "现场急停中";
            case 8: return "换电中";
            case 9: return "日志拉取中";
            default: return "未知(" + code + ")";
        }
    }

    /**
     * 解析目标设备 SN：配置 {@code device_debug.target_device_sn} 优先, 
     * 未配置时自动回退到 session dockSn(与 MaintenanceDemo 策略一致)。
     */
    private String resolveDeviceSn() {
        String sn = params.node("device_debug").str("target_device_sn", "");
        if (!sn.isEmpty()) return sn;
        if (session.hasDock() && session.dockSn() != null && !session.dockSn().isEmpty()) {
            hint("device_debug.target_device_sn 未配置, 自动使用当前已选机场 SN: " + session.dockSn());
            return session.dockSn();
        }
        return "";
    }

    /**
     * 执行带 {@code (FlightHubError, XxxResult)} 双参回调的动作并回显三态结果。
     * result 的打印由 lambda 自行处理(因各接口 result 字段不同)；
     * await 只传递 error, 供基类 runOutcome 三态骨架使用。
     */
    @FunctionalInterface
    private interface AwaitAction {
        void invoke(Await<FlightHubError> await);
    }

    private void callWithMessageResult(String label, long timeout, AwaitAction action) {
        final Await<FlightHubError> await = new Await<>(name() + "." + label);
        runOutcome(label, timeout, await, () -> action.invoke(await));
    }

    /** 打印含 bid/message 的结果(适用于开关机/开关舱盖等物理动作结果)。 */
    private void printBidMsg(String action, String bid, String msg) {
        if (bid != null || msg != null) {
            console("     " + action + ": bid=" + (bid != null ? bid : "")
                + " msg=" + (msg != null ? msg : ""));
            DemoLog.info(name(), action + " bid=" + bid + " msg=" + msg);
        }
    }

    /** 危险动作二次确认：须输入 {@code yes} 才执行。 */
    private boolean confirmDangerous(String action) {
        String line = Utils.readLine(input, "\n   警告: 即将执行「" + action + "」\n   输入 yes 确认执行, 其他任意输入取消: ");
        boolean ok = line != null && line.equalsIgnoreCase("yes");
        if (!ok) hint("已取消: " + action);
        else DemoLog.info(name(), "用户确认执行: " + action);
        return ok;
    }

    /**
     * 交互询问开关状态：展示功能名和当前配置默认值, 让用户选择 1=开启 / 2=关闭。
     *
     * <p>SDK 无查询接口(Dongle/低功耗/夜航灯等均为纯下发), 配置值只作显示提示, 
     * 实际执行哪个操作由用户在此交互确认, 避免直接用配置值盲发。
     *
     * @param label       功能名, 用于提示(如 "Dongle 电源")
     * @param cfgDefault  配置文件里的默认值(0=关/1=开), 仅作参考提示
     * @return 用户选择的 state：1=开启, 0=关闭, -1=用户取消(q)
     */
    private int askOnOff(String label, int cfgDefault) {
        String defaultHint = cfgDefault == 1 ? "开启" : "关闭";
        console("\n  --- " + label + " ---");
        console("  (注: SDK 无查询接口, 当前状态未知；配置默认值: " + defaultHint + ")");
        console("  1. 开启   2. 关闭   q. 返回");
        int choice = Utils.readIntInRange(input, "请选择操作: ", 1, 2);
        if (choice == Utils.QUIT) return -1;
        return choice == 1 ? 1 : 0;
    }

    private void console(String line) { System.out.println(line); }

    private void hint(String msg) {
        DemoLog.info(name(), msg);
        console("   " + msg);
    }

    // ==================== 菜单文案 ====================

    private String mainMenu() {
        return "\n"
            + "  === 设备调试 (Device Debug) ===\n"
            + "  1.  调试模式       (openDebugMode / closeDebugMode)\n"
            + "  2.  机场舱盖       (openDockCover/closeDockCover+WS进度 / HTTP查询开关舱进度)\n"
            + "  3.  飞行器电源     (openDrone / closeDrone / droneFormat / SDR模式)\n"
            + "  4.  充电控制       (openCharge / closeCharge + 进度订阅)\n"
            + "  5.  飞行参数设置   (限高/限远/避障/返航电量/夜航灯/报警灯/空调/静音)\n"
            + "  6.  设备维护       (重启/格式化+WS进度/HTTP查询进度 / 换电/Dongle/低功耗 / GeoCaging)\n"
            + "  7.  4G/SIM 管理   (eSIM运营商切换/SIM卡槽切换 + 进度订阅/查询)\n"
            + "  8.  云环境切换     (cloudEnvironmentSwitch/cloudEnvironmentSwitchCallback + 进度订阅)\n"
            + "\n"
            + "  q.  返回上级菜单\n";
    }
}
