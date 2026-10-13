// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.module;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.CameraFocalLengthParams;
import com.dji.flighthubsdk.bean.CameraMeasureTarget;
import com.dji.flighthubsdk.bean.CameraState;
import com.dji.flighthubsdk.bean.CameraWideLensState;
import com.dji.flighthubsdk.bean.CameraZoomLensState;
import com.dji.flighthubsdk.bean.DroneOnlineStatus;
import com.dji.flighthubsdk.bean.GimbalState;
import com.dji.flighthubsdk.bean.LinkStatus;
import com.dji.flighthubsdk.bean.LiveInfo;
import com.dji.flighthubsdk.bean.LiveRecordStateInfo;
import com.dji.flighthubsdk.bean.LiveVideoInfo;
import com.dji.flighthubsdk.bean.LiveviewWorldRegion;
import com.dji.flighthubsdk.bean.LookAtTargetParams;
import com.dji.flighthubsdk.bean.NightModeSettings;
import com.dji.flighthubsdk.bean.PanoramaParam;
import com.dji.flighthubsdk.bean.PanoramaStatus;
import com.dji.flighthubsdk.bean.SetNightSceneModeParams;
import com.dji.flighthubsdk.bean.SetPhotoStorageSettingsParams;
import com.dji.flighthubsdk.bean.SetVideoStorageSettingsParams;
import com.dji.flighthubsdk.bean.SwitchCameraModeParams;
import com.dji.flighthubsdk.bean.SwitchLiveLensParams;
import com.dji.flighthubsdk.bean.ThermalGainModeInfo;
import com.dji.flighthubsdk.bean.ThermalGainModeSetParams;
import com.dji.flighthubsdk.bean.ZoomAtTargetParams;
import com.dji.flighthubsdk.core.CameraControlModule;
import com.dji.flighthubsdk.core.ControlAuthModule;
import com.dji.flighthubsdk.core.DeviceStateModule;
import com.dji.flighthubsdk.core.Dock;
import com.dji.flighthubsdk.core.DrcModule;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.LiveDeviceModule;
import com.dji.flighthubsdk.core.LiveRecordModule;
import com.dji.flighthubsdk.demo.base.ModuleDemo;
import com.dji.flighthubsdk.demo.runtime.Await;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.runtime.FlightHubException;
import com.dji.flighthubsdk.demo.runtime.Node;
import com.dji.flighthubsdk.demo.runtime.Subscription;
import com.dji.flighthubsdk.demo.runtime.SubscriptionRegistry;
import com.dji.flighthubsdk.demo.utils.Utils;
import com.dji.flighthubsdk.value.CameraLensType;
import com.dji.flighthubsdk.value.CameraMode;
import com.dji.flighthubsdk.value.IrGainMode;
import com.dji.flighthubsdk.value.NightSceneMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * 负载/相机控制模块 Demo(菜单驱动)：负载相关消息监听、负载控制权、DRC 链路与云台重置、
 * 相机模式/镜头/变焦/存储/夜景/红外增益设置、拍照/录像/全景拍照。
 *
 * <p>按 {@code DrcDemo} 的子菜单范式实现, 20 项子菜单覆盖 6 个模块的常用接口(非全量;
 * 4 个 Drone 模块 + [1] 录制状态一路经由的 2 个 Dock 侧模块, 见下文「SDK 入口」)：
 * <ul>
 *   <li>[1] 消息监听(负载相关)：在线/链路状态({@code listenDroneOnlineStatus} / {@code listenLinkStatus})
 *       + 相机状态 6 路({@code listenDrcCameraState} / {@code listenDrcLiveviewWorldRegion} /
 *       {@code listenDrcCameraWideLensState} / {@code listenDrcCameraZoomLensState} /
 *       {@code listenDrcCameraMeasureTarget} / {@code listenDrcGimbalState})
 *       + 负载控制权状态({@code listenPayloadControlAuthority}) + 直播镜头信息({@code listenDrcLiveVideoInfo})
 *       + 红外增益模式({@code listenDrcThermalGainMode}) + 直播录制状态({@code listenRecordState})
 *       + 夜景模式状态({@code listenDrcNightModeSettings});
 *       <b>不含</b>「监听红外增益模式设置是否可用」——SDK 已在「移除所有 Available
 *       状态门控接口」重构(2026-07, Breaking Change)中删除全部<b>返回 boolean 的</b>
 *       {@code listen*Available} 可用性门控接口(数据类 {@code listenLiveAvailableRange}/
 *       {@code listenCameraModeAvailableRange} 仍保留), 能否设置改由服务端判定
 *       (直接调用 [15] 设置红外增益模式, 以返回错误码为准);</li>
 *   <li>[2]/[3] 获取/释放负载控制权(acquirePayloadControlAuthority / releasePayloadControlAuthority);</li>
 *   <li>[4]/[5] DRC 链路手动连接/断开(connectDrcLink / disconnectDrcLink); [6] 云台重置(drcResetGimbal);</li>
 *   <li>[7] 切换相机模式(drcSwitchCameraMode); [8] 切换镜头(drcSwitchLiveLens); [9] 变焦(drcSetFocalLength);
 *       [10] 框选变焦(drcZoomAtTarget); [11] LookAtTarget(lookAtTarget);</li>
 *   <li>[12]/[13] 拍照/录像存储设置(drcSetPhotoStorageSettings / drcSetVideoStorageSettings);
 *       [14] 夜景模式(drcSetNightSceneMode); [15] 红外增益(drcSetThermalGainMode);</li>
 *   <li>[16] 拍照(drcTakePhoto); [17]/[18] 开始/停止录像(drcStartRecord / drcStopRecord);
 *       [19]/[20] 开始/停止全景(drcStartPanorama / drcStopPanorama, 含 listenDrcPanoramaStatus 进度监听)。</li>
 * </ul>
 *
 * <p><b>接口串联设计：</b>先 [2] 获取负载控制权(云台/相机类操控的前置), 再执行 [6]-[20] 各操控项;
 * [6] 云台重置需先 [4] 建立 DRC 链路。菜单不强制顺序, 缺前置时仅提示。
 *
 * <p><b>SDK 入口：</b>{@code session.drone().getCameraControlModule()} / {@code getControlAuthModule()}
 * / {@code getDrcModule()} / {@code getDeviceStateModule()}(均挂在 Drone 上); [1] 的直播录制状态一路挂在
 * {@code LiveRecordModule} 上, 经机场 {@code session.dock().getLiveDeviceModule().listenLiveAvailableRange}
 * 发现直播源后由 {@code dock.getLiveRecord(liveInfo)} 获得(挂在 Dock 上)。
 *
 * <p><b>设计要点：</b>
 * <ul>
 *   <li>[9] 变焦为单次交互输入倍数(回车用 {@code payload_control.zoom_factor} 配置默认值),
 *       合法范围由服务端判定(message.h 注明可见光 2-200);</li>
 *   <li>框选变焦 / LookAtTarget 的坐标参数从 {@code demo_params.json} 的 {@code payload_control} 节读取,
 *       修改配置即可适配不同测试点位;</li>
 *   <li>[1] 建立的全部监听登记在模块内并同步登记进会话 registry(双保险), <b>退出本模块前统一
 *       取消订阅</b>; [1] 为<b>开/关切换</b>——再次选择即取消全部监听,
 *       亦可在部分监听建立失败(如机场未开播时被跳过)后关掉重开补订;
 *       各路推送(含相机/云台等 OSD 高频路)<b>只落日志文件、不刷控制台</b>
 *       (对齐 LiveStreamDemo「不淹没交互菜单」的范式);
 *       录制状态订阅所需的 {@code LiveRecordModule} 经机场直播源(range)发现获得, 选源按
 *       <b>本机场 SN 过滤</b>(避免同拓扑他机源被错选);</li>
 *   <li><b>超时语义</b>：获取类动作(获取控制权/建链/开始录像/开始全景)<b>超时按「可能已生效」处理</b>
 *       (状态标志照常置位)——超时说明应答未到, 不代表命令未生效(应答迟到即被静默忽略),
 *       按未生效处理会让退出收尾漏停、把设备留在录像/占权态; 释放类动作则仅在明确成功时清位,
 *       失败/超时保持置位由退出收尾重试(安全方向);</li>
 *   <li>退出前尽力而为收尾：停录像(若在录) → 停全景(若在拍) → 断 DRC(若已连) → 释放负载控制权(若已占),
 *       不把设备留在「录像中 / DRC 受控 / 占权」状态影响后续 demo; 收尾每步兜 {@code Throwable}
 *       (SDK 为 JNI, 原生 Error 不得中断后续收尾), 订阅取消放在 {@code finally} 必然执行;</li>
 *   <li><b>结果回显</b>：{@code DemoLog} 只写日志文件, 故每个操作项的「开始/[成功]/[超时]/[失败](错误码)」
 *       经 {@link #runOutcome} 实时回显控制台; [1] 的各路状态推送只落日志文件(见 {@link #pushLog});
 *       [19] 全景拍照进度是用户主动发起动作的反馈, 仍以 {@code [推送]} 前缀回显控制台;</li>
 *   <li><b>已知 SDK 限制(非本 demo bug)</b>：DRC 应答链路为 at-least-once, 应答重放时 SDK 原生层
 *       一次性回调包装器存在重复投递缺陷(已上报), 极小概率在 drcResetGimbal / drcSwitchCameraMode /
 *       drcStopRecord 等 DRC 命令返回后发生原生崩溃; 与 demo 用法无关。</li>
 * </ul>
 */
public class PayloadControlDemo extends ModuleDemo {

    /** [244] 录制状态订阅前, 等待机场上报可用直播源(range)的默认超时(ms)。[244] 为旧 demo 菜单号,
     *  沿用作日志/提示 tag(非本菜单 1-20)。range 来自 SDK 每 5s 一轮的 live_capacity 轮询,
     *  10s 可覆盖 ~2 轮; 相比 {@code LiveStreamDemo} 的 20s(需额外容忍首轮 topo 拉取延迟),
     *  本 demo 场景下机场通常已在线, 无需更长等待。 */
    private static final long DEFAULT_RECORD_RANGE_TIMEOUT_MS = 10000L;

    /** [1] 消息监听合集建立的订阅(含 [244] 录制状态); 模块退出前统一取消。 */
    private final List<Subscription> monitorSubs = new ArrayList<>();

    /** 全景拍照状态监听([19] 建立, [20] 或退出时取消)。 */
    private Subscription panoramaSub;

    // 仅用于顺序提示与退出收尾判断, 不强制流程顺序。
    private boolean gotAuth;
    private boolean linked;
    private boolean recording;
    private boolean panoramaActive;

    @Override
    public String name() {
        return "PayloadControlDemo";
    }

    @Override
    protected void run() throws Exception {
        if (input == null) {
            // 菜单式 demo 必须有交互输入; 正常经 DemoEntry 运行时 input 恒非空, 这里只是防御。
            DemoLog.warn(name(), "无交互输入(input=null), 无法运行菜单式负载控制 demo");
            return;
        }
        if (!session.hasDrone()) {
            hint("该设备没有无人机, 无法运行负载控制 demo");
            return;
        }

        Drone drone = null;
        try {
            drone = session.drone();
        } catch (Exception e) {
            DemoLog.warn(name(), "获取无人机实例失败, 无法运行负载控制 demo: " + Utils.describe(e));
            return;
        }

        // ControlAuth/Drc getter 在 SDK 内部异常时经 catch 返回 null; DeviceState getter 在原生模块缺失时返回 null;
        // CameraControl getter 不会返回 null(原生句柄为 null 也照常包装, 失败推迟到调用期以错误回调形式暴露)。
        // 显式判空覆盖前三者的失败路径, 避免后续调用抛裸 NullPointerException。
        final CameraControlModule cam = drone.getCameraControlModule();
        final ControlAuthModule auth = drone.getControlAuthModule();
        final DrcModule drc = drone.getDrcModule();
        final DeviceStateModule state = drone.getDeviceStateModule();
        if (cam == null || auth == null || drc == null || state == null) {
            DemoLog.error(name(), "获取模块失败, 无法运行负载控制 demo: cam=" + (cam != null)
                + " auth=" + (auth != null) + " drc=" + (drc != null) + " state=" + (state != null));
            return;
        }

        final String payloadIndex = payloadIndex();
        final Node cfg = params.node("payload_control");
        final long actionTimeout = timeout("default_callback_timeout_ms");
        DemoLog.info(name(), "payloadIndex=" + payloadIndex + " actionTimeout=" + actionTimeout + "ms");
        if (payloadIndex.isEmpty()) {
            // 回退路径(机型未命中列表 / target_payload_index 配置为空): 空值不冠「机型匹配」归因,
            // 把 DemoParams 只落日志文件的补配置引导直接呈现给交互终端用户。
            hint("payloadIndex 为空: 该机型未在 demo_params.json 的 device_common_configs_list 中"
                + "配置 target_payload_index, 负载指令可能被拒绝 — 请按机型实际负载补充配置");
        } else {
            console("使用 payloadIndex=" + payloadIndex + " (来自 device_common_configs 机型匹配)");
        }

        try {
            while (true) {
                Utils.printTitle("执行 " + name());
                if (!monitorSubs.isEmpty()) {
                    console("(消息监听中：" + monitorSubs.size()
                        + " 路; 推送只落日志文件; 再次选 [1] 关闭)");
                }
                System.out.print(menu());
                int choice = Utils.readIntInRange(input, "请选择(1-20, q 退出): ", 1, 20);
                if (choice == Utils.QUIT) {
                    DemoLog.info(name(), "退出负载控制菜单");
                    break;
                }

                // 单个菜单动作失败(含超时、错误码)不退出菜单：记录后回到菜单继续。
                // 原生 Error(如 UnsatisfiedLinkError)不在此拦截, 交由基类 runBody 兜底隔离。
                try {
                    switch (choice) {
                        case 1:
                            // 开/关切换: 已开启则全部取消(也给「部分建立失败后重开补订」留出模块内恢复路径)。
                            if (monitorSubs.isEmpty()) {
                                startMonitors(drone, cam, auth, drc, state, payloadIndex, cfg);
                            } else {
                                stopMonitors();
                            }
                            break;

                        case 2:
                            // 超时≠失败: 命令可能已生效(应答迟到被忽略), 按「可能已占权」置位, 退出收尾照常释放。
                            if (callOutcome("获取负载控制权",
                                    cb -> auth.acquirePayloadControlAuthority(payloadIndex, cb), actionTimeout)
                                    != Outcome.FAILED) {
                                gotAuth = true;
                            }
                            break;

                        case 3:
                            if (callItem("释放负载控制权",
                                    cb -> auth.releasePayloadControlAuthority(payloadIndex, cb), actionTimeout)) {
                                gotAuth = false;
                            }
                            break;

                        case 4:
                            if (!gotAuth) {
                                hint("尚未获取负载控制权, 建议先执行菜单 [2]");
                            }
                            // 超时按「链路可能已建」置位, 退出收尾照常断开。
                            if (syncOutcome("DRC 链路手动连接", drc::connectDrcLink, actionTimeout) != Outcome.FAILED) {
                                linked = true;
                            }
                            break;

                        case 5:
                            if (syncItem("DRC 链路手动断开", drc::disconnectDrcLink, actionTimeout)) {
                                linked = false;
                            }
                            break;

                        case 6: {
                            if (!linked) {
                                hint("DRC 链路尚未建立, 建议先执行菜单 [4]; 云台重置需链路可用");
                            }
                            // reset_mode 取值语义出处: message.h GimbalResetParams.reset_mode(见 describeGimbalResetMode)。
                            final int mode = readIntWithDefault(
                                    "请选择 reset_mode: 0=回中 1=向下 2=偏航回中 3=俯仰向下(回车默认 %d): ",
                                    clampInt(params.node("drc").intVal("gimbal_reset_mode", 0), 0, 3, 0), 0, 3);
                            if (callItem("云台重置(reset_mode=" + mode + " " + describeGimbalResetMode(mode) + ")",
                                    cb -> cam.drcResetGimbal(payloadIndex, mode, cb), actionTimeout)) {
                                hint("请观察云台是否已复位");
                            }
                            break;
                        }

                        case 7: {
                            final CameraMode mode = readCameraMode();
                            if (mode == null) {
                                break;
                            }
                            SwitchCameraModeParams p = new SwitchCameraModeParams();
                            p.setPayloadIndex(payloadIndex);
                            p.setCameraMode(mode);
                            callItem("切换相机模式(" + mode + ")", cb -> cam.drcSwitchCameraMode(p, cb), actionTimeout);
                            break;
                        }

                        case 8: {
                            final CameraLensType lens = readLensType();
                            if (lens == null) {
                                break;
                            }
                            SwitchLiveLensParams p = new SwitchLiveLensParams();
                            p.setPayloadIndex(payloadIndex);
                            p.setLensType(lens);
                            callItem("切换镜头(" + lens + ")", cb -> cam.drcSwitchLiveLens(p, cb), actionTimeout);
                            break;
                        }

                        case 9: {
                            // 单次交互输入变焦倍数(message.h 注明可见光 2-200; 其它镜头/机型合法范围由服务端判定)。
                            final int factor = readIntWithDefault(
                                    "请输入变焦倍数(如 2~200, 回车默认 %d): ",
                                    cfg.intVal("zoom_factor", 5), Integer.MIN_VALUE, Integer.MAX_VALUE);
                            CameraFocalLengthParams p = new CameraFocalLengthParams();
                            p.setPayloadIndex(payloadIndex);
                            p.setCameraType(CameraLensType.ZOOM);
                            p.setZoomFactor(factor);
                            callItem("变焦(ZOOM x" + factor + ")", cb -> cam.drcSetFocalLength(p, cb), actionTimeout);
                            break;
                        }

                        case 10: {
                            ZoomAtTargetParams p = new ZoomAtTargetParams();
                            p.setPayloadIndex(payloadIndex);
                            p.setCameraLensType(CameraLensType.ZOOM);
                            p.setX(cfg.dbl("zoom_area_x", 0.5));
                            p.setY(cfg.dbl("zoom_area_y", 0.5));
                            p.setWidth(cfg.dbl("zoom_area_width", 0.05));
                            p.setHeight(cfg.dbl("zoom_area_height", 0.05));
                            hint("框选区域(归一化): x=" + p.getX() + " y=" + p.getY()
                                + " w=" + p.getWidth() + " h=" + p.getHeight() + "(可在 payload_control 配置)");
                            callItem("框选变焦", cb -> cam.drcZoomAtTarget(p, cb), actionTimeout);
                            break;
                        }

                        case 11: {
                            LookAtTargetParams p = new LookAtTargetParams();
                            p.setPayloadIndex(payloadIndex);
                            // 经纬度默认值改为 天空之城公开位置经纬度;
                            p.setLatitude(cfg.dbl("look_at_latitude", 22.580320));
                            p.setLongitude(cfg.dbl("look_at_longitude", 113.938164));
                            p.setHeight(cfg.dbl("look_at_height", 200.0));
                            hint("目标点: lat=" + p.getLatitude() + " lon=" + p.getLongitude()
                                + " height=" + p.getHeight() + "(可在 payload_control 配置)");
                            callItem("LookAtTarget", cb -> cam.lookAtTarget(p, cb), actionTimeout);
                            break;
                        }

                        case 12: {
                            List<String> settings = readStorageSettings("拍照", cfg, "photo_storage_settings");
                            SetPhotoStorageSettingsParams p = new SetPhotoStorageSettingsParams();
                            p.setPayloadIndex(payloadIndex);
                            p.setPhotoStorageSettings(settings);
                            callItem("设置拍照存储设置" + settings, cb -> cam.drcSetPhotoStorageSettings(p, cb), actionTimeout);
                            break;
                        }

                        case 13: {
                            List<String> settings = readStorageSettings("录像", cfg, "video_storage_settings");
                            SetVideoStorageSettingsParams p = new SetVideoStorageSettingsParams();
                            p.setPayloadIndex(payloadIndex);
                            p.setVideoStorageSettings(settings);
                            callItem("设置录像存储设置" + settings, cb -> cam.drcSetVideoStorageSettings(p, cb), actionTimeout);
                            break;
                        }

                        case 14: {
                            final int mode = readIntWithDefault(
                                    "请输入夜景模式: 0=关闭 1=开启 2=自动(回车默认 %d): ", 2, 0, 2);
                            SetNightSceneModeParams p = new SetNightSceneModeParams();
                            p.setPayloadIndex(payloadIndex);
                            p.setMode(NightSceneMode.fromValue(mode));
                            callItem("设置夜景模式(" + p.getMode() + ")", cb -> cam.drcSetNightSceneMode(p, cb), actionTimeout);
                            break;
                        }

                        case 15: {
                            // 前置提示必须先于读输入(对齐 case 6 的 hint→read→call 顺序), 否则用户看到时已无法据此行动。
                            hint("注意: 只有当前镜头为 IR(红外)时才能设置红外增益模式(可先用 [8] 切换镜头)");
                            final int mode = readIntWithDefault(
                                    "请选择红外增益模式: 0=自动 1=低增益 2=高增益 3=超清模式(回车默认 %d): ", 0, 0, 3);
                            ThermalGainModeSetParams p = new ThermalGainModeSetParams();
                            p.setPayloadIndex(payloadIndex);
                            p.setThermalGainMode(IrGainMode.fromValue(mode));
                            callItem("设置红外增益模式(" + p.getThermalGainMode() + ")",
                                    cb -> cam.drcSetThermalGainMode(p, cb), actionTimeout);
                            break;
                        }

                        case 16:
                            callItem("拍照", cb -> cam.drcTakePhoto(payloadIndex, cb), actionTimeout);
                            break;

                        case 17:
                            // 超时按「可能已在录像」置位, 退出收尾照常停录——避免下发成功但应答超时把设备留在录像态。
                            if (callOutcome("开始录像", cb -> cam.drcStartRecord(payloadIndex, cb), actionTimeout)
                                    != Outcome.FAILED) {
                                recording = true;
                                hint("录像中(超时视为可能已开录), 停止请选 [18](退出模块时会自动停止)");
                            }
                            break;

                        case 18:
                            if (callItem("停止录像", cb -> cam.drcStopRecord(payloadIndex, cb), actionTimeout)) {
                                recording = false;
                            }
                            break;

                        case 19:
                            startPanorama(cam, payloadIndex, actionTimeout);
                            break;

                        case 20:
                            stopPanorama(cam, payloadIndex, actionTimeout);
                            break;

                        default:
                            break;
                    }
                } catch (Exception e) {
                    console("   结果 -> [异常] " + Utils.describe(e));
                    DemoLog.error(name(), "菜单动作执行异常: " + Utils.describe(e), e);
                }
            }
        } finally {
            teardown(cam, auth, drc, payloadIndex, actionTimeout);
        }
    }

    // ==================== [1] 消息监听(负载相关) ====================

    /**
     * 建立负载相关的全部消息监听。
     *
     * <p>各路监听独立建立：单路失败(listenId=-1 / 异常)只告警并继续其余路。全部订阅登记进
     * {@link #monitorSubs}(模块退出前统一取消, 见 {@link #teardown}), 并同步登记进会话
     * {@code registry()} 作为兜底(收尾即使被原生 Error 中断, 基类关闭会话时仍会退订;
     * {@code Subscription.close} 幂等, 双重取消无害)。
     *
     * <p>各路推送经 {@link #pushLog} <b>只落日志文件、不刷控制台</b>(相机/云台等 OSD 定频路
     * 回显会把菜单 prompt 滚出屏幕; 交互期控制台留给菜单与动作结果)。
     */
    private void startMonitors(Drone drone, CameraControlModule cam, ControlAuthModule auth,
            DrcModule drc, DeviceStateModule state, String payloadIndex, Node cfg) {
        if (!monitorSubs.isEmpty()) {
            // 防御: run() 的 switch 已按开/关分流, 正常不可达。
            hint("消息监听已开启(" + monitorSubs.size() + " 路), 再次选择 [1] 即关闭");
            return;
        }
        console("\n>> [消息监听] 建立负载相关监听(推送只落日志文件, 不刷控制台) ...");

        // 旧 demo [17] 飞机在线状态 + 链路状态(方括号编号为旧 demo 菜单号, 非本菜单 1-20, 下同)。
        addMonitor("droneOnlineStatus", () -> Subscription.of("droneOnlineStatus",
                state.listenDroneOnlineStatus(new FlightHubCallback.DroneOnlineStatusCallback() {
                    @Override
                    public void onResult(DroneOnlineStatus s) {
                        if (s == null) {
                            return;
                        }
                        pushLog("listenDroneOnlineStatus() #DroneOnlineStatus, 飞机在线状态: online=" + s.isOnline());
                    }
                }),
                state::cancelListenDroneOnlineStatus));
        addMonitor("drcLinkStatus", () -> Subscription.of("drcLinkStatus",
                drc.listenLinkStatus(new FlightHubCallback.LinkStatusCallback() {
                    @Override
                    public void onResult(LinkStatus s) {
                        if (s == null) {
                            return;
                        }
                        pushLog("listenLinkStatus() #LinkStatus, 链路状态: ctrl=" + s.getCtrlLinkStatus()
                            + " data=" + s.getDataLinkStatus() + " live=" + s.getLiveLinkStatus());
                    }
                }),
                drc::cancelListenLinkStatus));

        // 旧 demo [7] 相机状态(相机/直播范围/广角/变焦/测量目标/云台, 共 6 路)。
        // 这 6 路来自 DRC 相机 OSD 定频推送(飞行中每 tick 一条), 只落日志文件。
        addMonitor("cameraState", () -> Subscription.of("cameraState",
                cam.listenDrcCameraState(payloadIndex, new FlightHubCallback.CameraStateCallback() {
                    @Override
                    public void onResult(CameraState s) {
                        if (s == null) {
                            return;
                        }
                        pushLog("listenDrcCameraState() #CameraState, 相机状态: payload=" + s.getPayloadIndex()
                            + " mode=" + CameraMode.fromValue(s.getCameraMode())
                            + " recording=" + s.isRecordingState() + " recordTime=" + s.getRecordTime() + "s"
                            + " remainRecord=" + s.getRemainRecordDuration()
                            + " photoState=" + s.isPhotoState() + " remainPhoto=" + s.getRemainPhotoNum()
                            + " interval=" + s.getIntervalPhotoInterval());
                    }
                }),
                id -> cam.cancelListenDrcCameraState(payloadIndex, id)));
        addMonitor("liveviewWorldRegion", () -> Subscription.of("liveviewWorldRegion",
                cam.listenDrcLiveviewWorldRegion(payloadIndex, new FlightHubCallback.LiveviewWorldRegionCallback() {
                    @Override
                    public void onResult(LiveviewWorldRegion r) {
                        if (r == null || r.getWorldRegion() == null) {
                            return;
                        }
                        pushLog("listenDrcLiveviewWorldRegion() #LiveviewWorldRegion, 直播范围: payload=" + r.getPayloadIndex()
                            + " region[l=" + r.getWorldRegion().getLeft()
                            + " t=" + r.getWorldRegion().getTop()
                            + " r=" + r.getWorldRegion().getRight()
                            + " b=" + r.getWorldRegion().getBottom() + "]");
                    }
                }),
                id -> cam.cancelListenDrcLiveviewWorldRegion(payloadIndex, id)));
        addMonitor("cameraWideLensState", () -> Subscription.of("cameraWideLensState",
                cam.listenDrcCameraWideLensState(payloadIndex, new FlightHubCallback.CameraWideLensStateCallback() {
                    @Override
                    public void onResult(CameraWideLensState s) {
                        if (s == null) {
                            return;
                        }
                        pushLog("listenDrcCameraWideLensState() #CameraWideLensState, 广角镜头状态: payload=" + s.getPayloadIndex()
                            + " wideFocusState(swig)=" + s.getWideFocusState());
                    }
                }),
                id -> cam.cancelListenDrcCameraWideLensState(payloadIndex, id)));
        addMonitor("cameraZoomLensState", () -> Subscription.of("cameraZoomLensState",
                cam.listenDrcCameraZoomLensState(payloadIndex, new FlightHubCallback.CameraZoomLensStateCallback() {
                    @Override
                    public void onResult(CameraZoomLensState s) {
                        if (s == null) {
                            return;
                        }
                        pushLog("listenDrcCameraZoomLensState() #CameraZoomLensState, 变焦镜头状态: payload=" + s.getPayloadIndex()
                            + " zoomFocusState(swig)=" + s.getZoomFocusState());
                    }
                }),
                id -> cam.cancelListenDrcCameraZoomLensState(payloadIndex, id)));
        addMonitor("cameraMeasureTarget", () -> Subscription.of("cameraMeasureTarget",
                cam.listenDrcCameraMeasureTarget(payloadIndex, new FlightHubCallback.CameraMeasureTargetCallback() {
                    @Override
                    public void onResult(CameraMeasureTarget t) {
                        if (t == null) {
                            return;
                        }
                        pushLog("listenDrcCameraMeasureTarget() #CameraMeasureTarget, 测量目标: payload=" + t.getPayloadIndex()
                            + " lat=" + t.getLatitude() + " lon=" + t.getLongitude()
                            + " alt=" + t.getAltitude() + " dist=" + t.getDistance()
                            + " laserState(swig)=" + t.getLaserMeasureState());
                    }
                }),
                id -> cam.cancelListenDrcCameraMeasureTarget(payloadIndex, id)));
        addMonitor("gimbalState", () -> Subscription.of("gimbalState",
                cam.listenDrcGimbalState(payloadIndex, new FlightHubCallback.GimbalStateCallback() {
                    @Override
                    public void onResult(GimbalState s) {
                        if (s == null) {
                            return;
                        }
                        pushLog("listenDrcGimbalState() #GimbalState, 云台状态: payload=" + s.getPayloadIndex()
                            + " yaw=" + s.getYaw() + " pitch=" + s.getPitch() + " roll=" + s.getRoll());
                    }
                }),
                id -> cam.cancelListenDrcGimbalState(payloadIndex, id)));

        // 旧 demo [13] 负载控制权状态。
        addMonitor("payloadControlAuthority", () -> Subscription.of("payloadControlAuthority",
                auth.listenPayloadControlAuthority(payloadIndex, new FlightHubCallback.PayloadControlAuthorityCallback() {
                    @Override
                    public void onResult(boolean isGranted) {
                        pushLog("listenPayloadControlAuthority() #boolean, 负载控制权状态: is_granted=" + isGranted);
                    }
                }),
                id -> auth.cancelListenPayloadControlAuthority(payloadIndex, id)));

        // 旧 demo [80] 直播镜头信息。
        addMonitor("liveVideoInfo", () -> Subscription.of("liveVideoInfo",
                cam.listenDrcLiveVideoInfo(payloadIndex, new FlightHubCallback.LiveVideoInfoCallback() {
                    @Override
                    public void onResult(LiveVideoInfo info) {
                        if (info == null) {
                            return;
                        }
                        pushLog("listenDrcLiveVideoInfo() #LiveVideoInfo, 直播镜头信息: payload=" + info.getPayloadIndex()
                            + " videoIndex=" + info.getVideoIndex()
                            + " lens=" + CameraLensType.fromValue(info.getVideoTypeSwig())
                            + " switchable=" + lensNames(info.getSwitchableVideoTypeSwigValues()));
                    }
                }),
                id -> cam.cancelListenDrcLiveVideoInfo(payloadIndex, id)));

        // 旧 demo [82] 红外增益模式。
        addMonitor("thermalGainMode", () -> Subscription.of("thermalGainMode",
                cam.listenDrcThermalGainMode(payloadIndex, new FlightHubCallback.ThermalGainModeInfoCallback() {
                    @Override
                    public void onResult(ThermalGainModeInfo info) {
                        if (info == null) {
                            return;
                        }
                        pushLog("listenDrcThermalGainMode() #ThermalGainModeInfo, 红外增益模式: payload=" + info.getPayloadIndex()
                            + " mode=" + IrGainMode.fromValue(info.getThermalGainModeSwig()));
                    }
                }),
                id -> cam.cancelListenDrcThermalGainMode(payloadIndex, id)));

        // 「监听红外增益模式设置是否可用」：SDK 已删除全部返回 boolean 的 listen*Available 门控接口
        // (数据类 range 接口仍保留; 能否设置由服务端判定), 无对应 API 可用——见类 javadoc。
        hint("红外增益设置可用性监听已随 SDK Available 门控接口移除, 无法订阅; 以 [15] 设置结果为准");

        // 夜景模式状态。
        addMonitor("nightModeSettings", () -> Subscription.of("nightModeSettings",
                cam.listenDrcNightModeSettings(payloadIndex, new FlightHubCallback.NightModeSettingsCallback() {
                    @Override
                    public void onResult(NightModeSettings s) {
                        if (s == null) {
                            return;
                        }
                        pushLog("listenDrcNightModeSettings() #NightModeSettings, 夜景模式状态: payload=" + s.getPayloadIndex()
                            + " mode=" + s.getNightMode() + " working=" + s.isWorking());
                    }
                }),
                id -> cam.cancelListenDrcNightModeSettings(payloadIndex, id)));

        // 旧 demo [244] 直播录制状态(经机场直播源发现 LiveRecordModule, 可能等待数秒)。
        subscribeRecordState(cfg);

        console("   消息监听建立完成: 共 " + monitorSubs.size()
            + " 路(推送见日志文件; 再次选 [1] 关闭; 退出本模块时自动取消)");
        DemoLog.info(name(), "消息监听合集建立完成: " + monitorSubs.size() + " 路");
    }

    /**
     * 取消 [1] 建立的全部监听(开/关切换的「关」半边; {@link Subscription#close()} 幂等且吞异常)。
     *
     * <p>关闭 sub 的同时从会话 registry 移除, 避免 registry 长期持有已关闭 sub 引用(长会话下反复
     * 开/关监听会累积冗余条目)。registry 作为兜底仍有效：teardown() 的 finally 块会再次遍历
     * monitorSubs(彼时已 clear, 无重复操作), 会话 close 时 registry 也只剩其余资源。
     */
    private void stopMonitors() {
        console("\n>> [消息监听] 取消全部 " + monitorSubs.size() + " 路监听 ...");
        SubscriptionRegistry reg = registry();
        for (Subscription sub : monitorSubs) {
            sub.close();
            reg.remove(sub);   // 主动移出 registry, 避免已关闭引用长期积累
        }
        DemoLog.info(name(), "已取消消息监听 " + monitorSubs.size() + " 路");
        monitorSubs.clear();
        console("   已全部取消(可再次选 [1] 重新开启)");
    }

    /** 建立单路监听并登记(monitorSubs + 会话 registry 双登记); 失败只告警不中断其余路。 */
    private void addMonitor(String label, Callable<Subscription> factory) {
        try {
            Subscription sub = factory.call();
            monitorSubs.add(sub);
            // 兜底: 同步登记进会话 registry——即使 teardown 被原生 Error 中断, 基类关闭会话时仍会退订。
            registry().add(sub);
            console("   [监听] " + label + " 已建立");
        } catch (Exception e) {
            console("   [监听] " + label + " 建立失败: " + Utils.describe(e));
            DemoLog.warn(name(), "监听建立失败: " + label + " -> " + Utils.describe(e));
        }
    }

    /**
     * [244] 订阅直播录制状态：{@code listenRecordState} 挂在 {@code LiveRecordModule} 上,
     * 须先经机场 {@code listenLiveAvailableRange} 发现直播源(LiveInfo)再 {@code getLiveRecord}。
     * 选源按<b>本机场 SN</b> 过滤(盲取第一路可能拿到同拓扑其它设备的源)。
     * 机场缺失 / 超时无源 / 模块获取失败时跳过并告警, 不影响其余监听。
     */
    private void subscribeRecordState(Node cfg) {
        if (!session.hasDock()) {
            hint("[244] 当前设备无机场, 跳过录制状态订阅");
            return;
        }
        final long rangeTimeout = cfg.longVal("record_range_timeout_ms", DEFAULT_RECORD_RANGE_TIMEOUT_MS);
        try {
            // session.dock() 失败时抛 FlightHubException(不返回 null), 由本方法末尾的 catch 统一兜住。
            final Dock dock = session.dock();
            final LiveDeviceModule ldm = dock.getLiveDeviceModule();
            if (ldm == null) {
                hint("[244] 获取机场 LiveDeviceModule 失败, 跳过录制状态订阅");
                return;
            }
            console("   [监听] recordState: 等待机场直播源(最长 " + rangeTimeout + "ms) ...");

            final String dockSn = session.dockSn();
            final Await<LiveInfo> await = new Await<>(name() + ".listenLiveAvailableRange");
            int rangeId = ldm.listenLiveAvailableRange(new FlightHubCallback.LiveAvailableRangeCallback() {
                @Override
                public void onResult(List<LiveInfo> infos) {
                    LiveInfo picked = pickDockLiveInfo(infos, dockSn);
                    if (picked != null) {
                        await.set(picked);
                    }
                }
            });
            if (rangeId == -1) {
                hint("[244] 机场直播源监听建立失败(listenId=-1), 跳过录制状态订阅");
                return;
            }

            LiveInfo picked;
            try {
                picked = await.await(rangeTimeout);
            } catch (FlightHubException e) {
                hint("[244] 等待机场直播源超时(" + rangeTimeout + "ms, 可能未开播), 跳过录制状态订阅");
                return;
            } finally {
                // range 监听仅用于发现源, 用完即取消(录制状态订阅本身不依赖它)。
                ldm.cancelListenLiveAvailableRange(rangeId);
            }

            final LiveRecordModule module = dock.getLiveRecord(picked);
            if (module == null) {
                hint("[244] getLiveRecord 返回 null, 跳过录制状态订阅");
                return;
            }
            /* @deprecated: 本接口暂未开放使用
            addMonitor("recordState(sn=" + picked.getDeviceSn() + ")", () -> Subscription.of("recordState",
                    module.listenRecordState(new FlightHubCallback.LiveRecordStateCallback() {
                        @Override
                        public void onRecordState(LiveRecordStateInfo info) {
                            if (info == null) {
                                return;
                            }
                            pushLog("listenRecordState() #LiveRecordStateInfo, 直播录制状态: sn=" + info.getSn() + " camera=" + info.getCamera()
                                + " video=" + info.getVideo() + " state=" + info.getState()
                                + " duration=" + info.getDuration() + "s");
                        }
                    }),
                    module::cancelListenRecordState));
            */
        } catch (Exception e) {
            hint("[244] 录制状态订阅失败: " + Utils.describe(e));
            DemoLog.warn(name(), "录制状态订阅失败: " + Utils.describe(e));
        }
    }

    /**
     * 从机场 range 列表挑一路<b>属于本机场</b>的源：本机场 SN 匹配优先, 其次 sn 为空的兜底路;
     * 全部明确属于其它设备则返回 null(继续等待, 宁缺勿错)。
     */
    private LiveInfo pickDockLiveInfo(List<LiveInfo> infos, String dockSn) {
        if (infos == null || infos.isEmpty()) {
            return null;
        }
        LiveInfo unknown = null;
        for (LiveInfo li : infos) {
            if (li == null) {
                continue;
            }
            String sn = li.getDeviceSn();
            if (dockSn != null && dockSn.equals(sn)) {
                return li;
            }
            if ((sn == null || sn.isEmpty()) && unknown == null) {
                unknown = li;
            }
        }
        if (unknown != null) {
            DemoLog.warn(name(), "[244] 选中源未携带 deviceSn(无法验证归属), 兜底采用: payload="
                + unknown.getPayloadIndex() + " video_id=" + unknown.getVideoId());
        }
        return unknown;
    }

    // ==================== [19]/[20] 全景拍照 (drcStartPanorama / drcStopPanorama) ====================

    /**
     * 开始全景拍照：先建状态监听(不漏早期推送), 再下发 start。
     * start <b>明确失败</b>(错误码)且当前无进行中的全景时回收监听;
     * <b>超时不回收</b>——全景可能已实际开拍(应答迟到), 保留监听并按「可能进行中」置位, 退出收尾照常停止。
     */
    private void startPanorama(CameraControlModule cam, String payloadIndex, long actionTimeout) {
        if (panoramaSub == null) {
            try {
                panoramaSub = Subscription.of("panoramaStatus",
                        cam.listenDrcPanoramaStatus(payloadIndex, new FlightHubCallback.PanoramaStatusCallback() {
                            @Override
                            public void onResult(PanoramaStatus s) {
                                if (s == null) {
                                    return;
                                }
                                push("listenDrcPanoramaStatus() #PanoramaStatus, 全景拍照状态: percent=" + s.getPercent()
                                    + " currentStep(swig)=" + s.getCurrentStep()
                                    + " photoState(swig)=" + s.getPhotoState());
                            }
                        }),
                        id -> cam.cancelListenDrcPanoramaStatus(payloadIndex, id));
                // 兜底: 入会话 registry, 收尾被中断时基类关闭会话仍会退订(close 幂等)。
                registry().add(panoramaSub);
            } catch (Exception e) {
                // 监听失败不拦截主动作：没有进度推送也能拍, 结果以 start 回调为准。
                hint("全景拍照状态监听建立失败(仅影响进度显示): " + Utils.describe(e));
            }
        }

        PanoramaParam p = new PanoramaParam();
        p.setPayloadIndex(payloadIndex);
        Outcome o = callOutcome("开始全景拍照", cb -> cam.drcStartPanorama(p, cb), actionTimeout);
        if (o != Outcome.FAILED) {
            panoramaActive = true;
            hint("全景拍照中(超时视为可能已开拍), 进度见 [推送]; 停止请选 [20]");
        } else if (!panoramaActive && panoramaSub != null) {
            // start 被设备明确拒绝且当前无进行中的全景：回收本次建立的监听, 不留死订阅。
            panoramaSub.close();
            registry().remove(panoramaSub);
            panoramaSub = null;
        }
    }

    /** 停止全景拍照并取消状态监听(drcStopPanorama + cancelListenDrcPanoramaStatus)。 */
    private void stopPanorama(CameraControlModule cam, String payloadIndex, long actionTimeout) {
        if (callItem("停止全景拍照", cb -> cam.drcStopPanorama(payloadIndex, cb), actionTimeout)) {
            panoramaActive = false;
        }
        if (panoramaSub != null) {
            panoramaSub.close();
            registry().remove(panoramaSub);
            panoramaSub = null;
        }
    }

    // ==================== 收尾 ====================

    /**
     * 退出前尽力而为收尾：停录像 → 停全景 → 断 DRC → 释放负载控制权 → 取消全部监听订阅。
     *
     * <p>顺序说明：录像/全景停止需要负载控制权, 故先停再释放; DRC 断开先于释放(对齐 DrcDemo)。
     * 本方法在 {@link #run} 的 {@code finally} 中调用：四个动作步各自经 {@link #quietStep}
     * 兜 {@code Throwable}(SDK 为 JNI, 原生 Error 不得中断后续收尾、也不得掩盖主异常),
     * 订阅取消放在内层 {@code finally} 保证必然执行。
     */
    private void teardown(CameraControlModule cam, ControlAuthModule auth, DrcModule drc,
            String payloadIndex, long actionTimeout) {
        if (recording || panoramaActive || linked || gotAuth
                || !monitorSubs.isEmpty() || panoramaSub != null) {
            console("\n>> [退出收尾] 还原设备状态并取消监听 ...");
        }
        try {
            // callItem/syncItem 已兜 Exception 并回显成败; quietStep 再兜原生 Error, 单步失败不中断后续收尾。
            if (recording) {
                quietStep("停止录像(收尾)",
                        () -> callItem("停止录像(收尾)", cb -> cam.drcStopRecord(payloadIndex, cb), actionTimeout));
            }
            if (panoramaActive) {
                quietStep("停止全景拍照(收尾)",
                        () -> callItem("停止全景拍照(收尾)", cb -> cam.drcStopPanorama(payloadIndex, cb), actionTimeout));
            }
            if (linked) {
                quietStep("DRC 链路断开(收尾)",
                        () -> syncItem("DRC 链路断开(收尾)", drc::disconnectDrcLink, actionTimeout));
            }
            if (gotAuth) {
                quietStep("释放负载控制权(收尾)",
                        () -> callItem("释放负载控制权(收尾)",
                                cb -> auth.releasePayloadControlAuthority(payloadIndex, cb), actionTimeout));
            }
        } finally {
            // 取消 [1] 建立的全部监听 + 全景状态监听(Subscription.close 幂等、内部吞 Throwable)。
            if (panoramaSub != null) {
                panoramaSub.close();
                registry().remove(panoramaSub);
                panoramaSub = null;
            }
            if (!monitorSubs.isEmpty()) {
                SubscriptionRegistry reg = registry();
                for (Subscription sub : monitorSubs) {
                    sub.close();
                    reg.remove(sub);
                }
                console("   已取消 " + monitorSubs.size() + " 路消息监听");
                DemoLog.info(name(), "退出前已取消消息监听 " + monitorSubs.size() + " 路");
                monitorSubs.clear();
            }
        }
    }

    /** 收尾单步的 Throwable 兜底：记录后继续, 不中断后续收尾、不掩盖主异常。 */
    private void quietStep(String label, Runnable step) {
        try {
            step.run();
        } catch (Throwable t) {
            console("   [收尾] " + label + " 异常: " + Utils.describe(t));
            DemoLog.error(name(), "[收尾] " + label + " 异常: " + Utils.describe(t), t);
        }
    }

    // ==================== 辅助方法 ====================

    /** 读取相机模式(0-4 选项, 取值语义出处: {@link CameraMode} 枚举 javadoc); q/EOF 返回 null 表示放弃本次操作。 */
    private CameraMode readCameraMode() {
        console("   相机模式: 0=拍照 1=录像 2=智能低光拍照 3=全景拍照 4=定时拍照");
        int v = Utils.readIntInRange(input, "   请选择相机模式(0-4, q 取消): ", 0, 4);
        if (v == Utils.QUIT) {
            hint("已取消切换相机模式");
            return null;
        }
        return CameraMode.fromValue(v);
    }

    /**
     * 读取镜头类型(0-2 选项); q/EOF 返回 null 表示放弃本次操作。
     *
     * <p>0/1/2 为本菜单编号, 非 {@link CameraLensType} 原始值(WIDE=1/ZOOM=2/IR=3, 见枚举 javadoc),
     * 映射在下方 switch 完成, 下发的是枚举本身。
     */
    private CameraLensType readLensType() {
        console("   镜头类型: 0=广角 1=变焦 2=红外");
        int v = Utils.readIntInRange(input, "   请选择镜头类型(0-2, q 取消): ", 0, 2);
        if (v == Utils.QUIT) {
            hint("已取消切换镜头");
            return null;
        }
        switch (v) {
            case 0:
                return CameraLensType.WIDE;
            case 1:
                return CameraLensType.ZOOM;
            default:
                return CameraLensType.IR;
        }
    }

    /**
     * 读一个整数：空行/非法输入回退默认值 {@code def}, 合法但越界时告警并回退默认值。
     *
     * @param promptFmt 提示语, 含一个 %d 占位(填默认值)
     */
    private int readIntWithDefault(String promptFmt, int def, int min, int max) {
        String line = Utils.readLine(input, "\n" + String.format(promptFmt, def));
        if (line == null || line.isEmpty()) {
            return def;
        }
        try {
            int v = Integer.parseInt(line);
            if (v >= min && v <= max) {
                return v;
            }
        } catch (NumberFormatException ignored) {
            // 落到下方告警 + 默认值。
        }
        DemoLog.warn(name(), "无效输入'" + line + "', 使用默认 " + def);
        console("   无效输入'" + line + "', 使用默认 " + def);
        return def;
    }

    /**
     * 读取存储设置列表(逗号分隔输入)：
     * 回车用 {@code payload_control.<cfgKey>} 配置默认值; 手输如 {@code current,vision,ir}。
     *
     * <p>可选值 current/vision/ir/wide/zoom 沿自旧 demo 提示与上云 API 文档;
     * SDK bean/message.h 均未枚举取值, 合法性由服务端校验。
     */
    private List<String> readStorageSettings(String kind, Node cfg, String cfgKey) {
        List<String> def = new ArrayList<>();
        for (Object o : cfg.list(cfgKey)) {
            if (o != null && !String.valueOf(o).trim().isEmpty()) {
                def.add(String.valueOf(o).trim());
            }
        }
        String line = Utils.readLine(input, "\n请输入" + kind + "存储设置(逗号分隔, 可选 current/vision/ir/wide/zoom, 回车默认 "
            + (def.isEmpty() ? "[]" : def) + "): ");
        if (line == null || line.isEmpty()) {
            return def;
        }
        List<String> settings = new ArrayList<>();
        for (String item : line.split(",")) {
            String v = item.trim();
            if (!v.isEmpty()) {
                settings.add(v);
            }
        }
        return settings;
    }

    /** swig 镜头类型列表 → 枚举名列表(用于直播镜头信息推送回显)。 */
    private static String lensNames(List<Integer> swigValues) {
        if (swigValues == null || swigValues.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < swigValues.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            Integer v = swigValues.get(i);
            sb.append(CameraLensType.fromValue(v != null ? v : 0));
        }
        return sb.append(']').toString();
    }

    /** 夹取整数到 [min,max], 越界告警并回退 {@code def}(用于配置读入的不受信值)。 */
    private int clampInt(int v, int min, int max, int def) {
        if (v < min || v > max) {
            DemoLog.warn(name(), "配置值 " + v + " 超出范围[" + min + "," + max + "], 回退默认 " + def);
            return def;
        }
        return v;
    }

    /** 云台重置模式回显(出处: message.h {@code GimbalResetParams.reset_mode} 注释, 措辞逐字一致)。 */
    private static String describeGimbalResetMode(int mode) {
        switch (mode) {
            case 0:
                return "回中";
            case 1:
                return "向下";
            case 2:
                return "偏航回中";
            case 3:
                return "俯仰向下";
            default:
                return "未知(" + mode + ")";
        }
    }

    // ---- 控制台结果回显(范式同 DrcDemo, 另增三态结果; 监听推送只落日志)----
    // DemoLog 只落滚动日志文件, 交互终端看不到调用结果; 以下把每个测试项的
    // 「开始 / 成功 / 超时 / 失败(附错误码与消息)」实时回显到 stdout。
    // Outcome 枚举 / callOutcome / syncOutcome / runOutcome 由基类 ModuleDemo 统一提供。

    /** 纯控制台输出(不落日志文件), 用于菜单文案与结果回显。 */
    private void console(String line) {
        System.out.println(line);
    }

    /** 提示信息双写：既落日志文件(带 demo 名 tag)又回显控制台, 用于前置条件不满足、取消等需留痕的提示。 */
    private void hint(String msg) {
        DemoLog.info(name(), msg);
        console("   " + msg);
    }

    /** 动作进度推送回显(仅 [19] 全景拍照进度——用户主动发起动作的反馈): 控制台 [推送] 前缀 + 落日志文件。 */
    private void push(String line) {
        DemoLog.info(name(), line);
        console("   [推送] " + line);
    }

    /**
     * 监听推送专用：只落日志文件不刷控制台, 避免推送打断菜单交互——[1] 的监听是持续后台观察,
     * 高频路(相机/云台等 OSD 定频类)逐条回显会把菜单 prompt 滚出屏幕。回调在 SDK 线程触发, 只做记录不阻塞。
     */
    private void pushLog(String line) {
        DemoLog.info(name(), line);
    }

    /** 执行一个「ResultCallbackWithError 动作」并回显结果; 仅明确成功返回 true(超时视为失败)。 */
    private boolean callItem(String label, ActionInvoker action, long timeoutMs) {
        return callOutcome(label, action, timeoutMs) == Outcome.SUCCESS;
    }

    /** 执行一个「ResultCallback 动作」(DRC 连接/断开)并回显结果; 仅明确成功返回 true。 */
    private boolean syncItem(String label, Consumer<FlightHubCallback.ResultCallback> action, long timeoutMs) {
        return syncOutcome(label, action, timeoutMs) == Outcome.SUCCESS;
    }

    // ==================== 菜单文案 ====================

    /**
     * 负载控制菜单文案(每轮循环打印, 分组间空行便于阅读)：20 个菜单项与类 Javadoc 菜单树中的
     * 各模块接口一一对应。增删菜单项时须同步 {@link #run()} 中的 switch 分支与
     * {@code readIntInRange} 的取值上界。
     */
    private String menu() {
        return "\n"
            + "  === 负载控制 (Payload Control) ===\n"
            + "  --- 监听 ---\n"
            + "  1.  消息监听(负载相关)   开/关切换: 在线/链路、相机状态、控制权、直播镜头、红外增益、录制状态、夜景模式(推送只落日志文件)\n"
            + "\n"
            + "  --- 控制权 / DRC ---\n"
            + "  2.  获取负载控制权       (acquirePayloadControlAuthority)\n"
            + "  3.  释放负载控制权       (releasePayloadControlAuthority)\n"
            + "  4.  DRC 链路: 手动连接   (connectDrcLink)\n"
            + "  5.  DRC 链路: 手动断开   (disconnectDrcLink)\n"
            + "  6.  DRC 操控: 云台重置   (drcResetGimbal, 需先建 DRC 链路)\n"
            + "\n"
            + "  --- 相机设置 ---\n"
            + "  7.  切换相机模式         (drcSwitchCameraMode)\n"
            + "  8.  切换镜头             (drcSwitchLiveLens)\n"
            + "  9.  变焦                 (drcSetFocalLength, ZOOM 镜头)\n"
            + "  10. 框选变焦             (drcZoomAtTarget, 区域取自 payload_control 配置)\n"
            + "  11. LookAtTarget         (lookAtTarget, 目标点取自 payload_control 配置)\n"
            + "  12. 设置拍照存储设置     (drcSetPhotoStorageSettings)\n"
            + "  13. 设置录像存储设置     (drcSetVideoStorageSettings)\n"
            + "  14. 设置夜景模式         (drcSetNightSceneMode)\n"
            + "  15. 设置红外增益模式     (drcSetThermalGainMode, 需 IR 镜头)\n"
            + "\n"
            + "  --- 拍摄 ---\n"
            + "  16. 拍照                 (drcTakePhoto)\n"
            + "  17. 开始录像             (drcStartRecord)\n"
            + "  18. 停止录像             (drcStopRecord)\n"
            + "  19. 开始全景拍照         (drcStartPanorama + 状态监听)\n"
            + "  20. 停止全景拍照         (drcStopPanorama)\n"
            + "\n"
            + "  q.  返回上级菜单(自动收尾: 停录像/停全景/断链/释权/退订)\n";
    }
}
