// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.module;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.DroneOnlineStatus;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.LinkStatus;
import com.dji.flighthubsdk.bean.StickControlData;
import com.dji.flighthubsdk.bean.TakeoffParams;
import com.dji.flighthubsdk.bean.TakeoffResult;
import com.dji.flighthubsdk.core.ControlAuthModule;
import com.dji.flighthubsdk.core.DeviceStateModule;
import com.dji.flighthubsdk.core.DrcModule;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.FlightControlModule;
import com.dji.flighthubsdk.core.TakeoffModule;
import com.dji.flighthubsdk.demo.base.ModuleDemo;
import com.dji.flighthubsdk.demo.runtime.Await;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.runtime.Node;
import com.dji.flighthubsdk.demo.runtime.Subscription;
import com.dji.flighthubsdk.demo.runtime.SubscriptionRegistry;
import com.dji.flighthubsdk.demo.utils.Utils;
import com.dji.flighthubsdk.generated.DrcBatteriesInfo;
import com.dji.flighthubsdk.generated.DrcOsdInfo;
import com.dji.flighthubsdk.value.OutOfControlAction;
import com.dji.flighthubsdk.value.TakeoffMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * 飞行控制模块 Demo(菜单驱动)：飞行控制相关消息监听、飞行控制权、DRC 链路、
 * 一键起飞、WASD 摇杆、急停、返航/取消返航。
 *
 * <p>按 {@code PayloadControlDemo} 的子菜单范式实现(三态结果/监听开关/退订收尾等机制同款)，
 * 提供以下功能：
 * <ul>
 *   <li>[1] 消息监听(飞行控制相关)：在线/链路状态 + 飞行控制权状态;
 *       <b>不含</b>「监听急停可用状态」/「监听摇杆控制可用状态」——SDK 已在
 *       「移除所有 Available 状态门控接口」重构(2026-07, Breaking Change)中删除全部
 *       <b>返回 boolean 的</b> {@code listen*Available} 可用性门控接口(FlightControlModule 的
 *       急停/摇杆可用性监听在列), 能否执行改由服务端判定(直接调用 [7]/[8], 以返回错误码为准);
 *       另包含 DRC OSD / 电池遥测订阅(亦属飞控相关推送, DRC 链路已连时才有数据);</li>
 *   <li>[2]/[3] 获取/释放飞行控制权;</li>
 *   <li>[4]/[5] DRC 链路手动连接/断开;</li>
 *   <li>[6] 一键起飞; [7] WASD 摇杆; [8] 急停;
 *       [9] 返航; [10] 取消返航。</li>
 * </ul>
 *
 * <p><b>设计要点</b>：
 * <ul>
 *   <li><b>[6] 起飞参数配置驱动</b>：全部取自 {@code demo_params.json} 的 {@code takeoff} 节
 *       (与场景 {@code Precondition.takeoff} 同源); 只判断<b>指令下发</b>是否成功,
 *       不阻塞等待起飞完成;</li>
 *   <li><b>[7] WASD 摇杆参数与安全加固</b>：各轴取值范围为 SDK 声明的建议操控范围
 *       364–1684(中性 1024)。各轴取值由 {@code flight_control} 配置节提供(默认全 1024=中性,
 *       需要动作请改配置), 下发前夹取到 [364,1684]; 固定 50Hz(20ms)连发, 时长可配且夹取到
 *       {@value #STICK_DURATION_MAX_MS}ms 上限; 连发期间<b>按 Enter 可随时中止</b>;
 *       结束/中止后补发全中性「松杆」收尾帧(SDK 约定, 详见 {@link #sticksControl});</li>
 *   <li><b>危险动作二次确认</b>：[6] 起飞与 [7] WASD 会让飞机真实起飞/移动, 执行前须输入
 *       {@code yes} 确认; 急停/返航/取消返航为恢复类动作, 不设确认(需要时越快越好);</li>
 *   <li>[1] 为<b>开/关切换</b>且退出模块前统一退订(monitorSubs + 会话 registry 双保险);
 *       各路推送(含高频的 DRC OSD/电池)<b>只落日志文件、不刷控制台</b>, 交互期控制台留给菜单;</li>
 *   <li><b>超时语义</b>：获取类动作(获取控制权/建链)超时按「可能已生效」置位, 退出收尾照常还原;
 *       释放类仅明确成功才清位(安全方向);</li>
 *   <li>退出前尽力而为收尾：断 DRC(若已连) → 释放飞行控制权(若已占) → 退订;
 *       <b>不会自动返航/降落</b>——若执行过起飞, 退出时仅告警提醒, 把飞机收回的决定留给操作者
 *       (demo 静默触发返航比留在空中更危险)。</li>
 * </ul>
 *
 * <p><b>操作顺序建议</b>：[2] 获取飞行控制权 → [4] 建 DRC 链路 → [6]-[10] 飞行动作;
 * WASD/急停等 DRC 操控指令走 DRC 实时通道(MQTT-DRC 或 RTC), 须先建链。菜单不强制顺序, 缺前置时仅提示。
 *
 * <p><b>结果回显</b>：{@code DemoLog} 只写日志文件, 故每个操作项的「开始/[成功]/[超时]/[失败](错误码)」
 * 经 {@link #runOutcome} 实时回显控制台; [1] 的各路状态推送只落日志文件(见 {@link #pushLog})。
 */
public class FlightControlDemo extends ModuleDemo {

    /** WASD 摇杆连发间隔(ms), 固定 50Hz。 */
    private static final int STICK_INTERVAL_MS = 20;
    /** WASD 连发时长上限(ms), 固定 10s——配置更大值会被夹取, 防止误配把连发窗口放大到分钟级。 */
    private static final long STICK_DURATION_MAX_MS = 10000L;
    /** 松杆收尾帧数: 连发结束/中止后补发的全中性帧(SDK javadoc:「松杆建议连续发送若干帧全 1024」)。 */
    private static final int STICK_NEUTRAL_TAIL_FRAMES = 10;
    /** SDK 声明的摇杆各轴建议操控范围(中性 1024); 配置越界会被夹取。 */
    private static final int STICK_MIN = 364;
    private static final int STICK_NEUTRAL = 1024;
    private static final int STICK_MAX = 1684;

    /** [1] 消息监听合集建立的订阅; 模块退出前统一取消。 */
    private final List<Subscription> monitorSubs = new ArrayList<Subscription>();

    // 仅用于顺序提示与退出收尾判断, 不强制流程顺序。
    private boolean gotAuth;
    private boolean linked;
    /** 是否执行过一键起飞(退出时据此提醒「飞机可能仍在空中」, 不自动返航)。 */
    private boolean tookOff;

    @Override
    public String name() {
        return "FlightControlDemo";
    }

    @Override
    protected void run() throws Exception {
        if (input == null) {
            // 菜单式 demo 必须有交互输入; 正常经 DemoEntry 运行时 input 恒非空, 这里只是防御。
            DemoLog.warn(name(), "无交互输入(input=null), 无法运行菜单式飞行控制 demo");
            return;
        }
        if (!session.hasDrone()) {
            hint("该设备没有无人机, 无法运行飞行控制 demo");
            return;
        }

        final Drone drone = session.drone();

        // 模块 getter 在 SDK 内部失败时会返回 null(见 Drone 各 getter 的 catch 分支),
        // 显式判空给出诊断, 避免后续调用抛裸 NullPointerException(TakeoffModule 在 [6] 内单独判)。
        final FlightControlModule fc = drone.getFlightControlModule();
        final ControlAuthModule auth = drone.getControlAuthModule();
        final DrcModule drc = drone.getDrcModule();
        final DeviceStateModule state = drone.getDeviceStateModule();
        if (fc == null || auth == null || drc == null || state == null) {
            DemoLog.error(name(), "获取模块失败, 无法运行飞行控制 demo: fc=" + (fc != null)
                    + " auth=" + (auth != null) + " drc=" + (drc != null) + " state=" + (state != null));
            return;
        }

        final long actionTimeout = timeout("default_callback_timeout_ms");
        DemoLog.info(name(), "actionTimeout=" + actionTimeout + "ms");

        try {
            while (true) {
                Utils.printTitle("执行 " + name());
                if (!monitorSubs.isEmpty()) {
                    console("(消息监听中：" + monitorSubs.size()
                            + " 路; 推送只落日志文件; 再次选 [1] 关闭)");
                }
                System.out.print(menu());
                int choice = Utils.readIntInRange(input, "请选择(1-10, q 退出): ", 1, 10);
                if (choice == Utils.QUIT) {
                    DemoLog.info(name(), "退出飞行控制菜单");
                    break;
                }

                // 单个菜单动作失败(含超时、错误码)不退出菜单：记录后回到菜单继续。
                // 原生 Error(如 UnsatisfiedLinkError)不在此拦截, 交由基类 runBody 兜底隔离。
                try {
                    dispatch(choice, drone, fc, auth, drc, state, actionTimeout);
                } catch (Exception e) {
                    console("   结果 -> [异常] " + Utils.describe(e));
                    DemoLog.error(name(), "菜单动作执行异常: " + Utils.describe(e), e);
                }
            }
        } finally {
            teardown(auth, drc, actionTimeout);
        }
    }

    /** 菜单分发(编号与类 javadoc 中的功能列表一一对应)。 */
    private void dispatch(int choice, Drone drone, FlightControlModule fc, ControlAuthModule auth,
            DrcModule drc, DeviceStateModule state, long actionTimeout) {
        switch (choice) {
            case 1:
                // 开/关切换: 已开启则全部取消(部分建立失败后可关掉重开补订)。
                if (monitorSubs.isEmpty()) {
                    startMonitors(fc, auth, drc, state);
                } else {
                    stopMonitors();
                }
                break;

            case 2:
                // 超时≠失败: 命令可能已生效(应答迟到被忽略), 按「可能已占权」置位, 退出收尾照常释放。
                if (callOutcome("获取飞行控制权",
                        cb -> auth.acquireFlightControlAuthority(cb), actionTimeout) != Outcome.FAILED) {
                    gotAuth = true;
                }
                break;

            case 3:
                if (callItem("释放飞行控制权",
                        cb -> auth.releaseFlightControlAuthority(cb), actionTimeout)) {
                    gotAuth = false;
                }
                break;

            case 4:
                if (!gotAuth) {
                    hint("尚未获取飞行控制权, 建议先执行菜单 [2]");
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

            case 6:
                takeoff(drone);
                break;

            case 7:
                sticksControl(fc);
                break;

            case 8:
                // 急停是恢复类动作, 不设二次确认(需要时越快越好)。
                callItem("飞行急停", cb -> fc.drcEmergencyStop(cb), actionTimeout);
                break;

            case 9:
                callItem("返航", cb -> fc.returnHome(cb), actionTimeout);
                break;

            case 10:
                callItem("取消返航", cb -> fc.returnHomeCancel(cb), actionTimeout);
                break;

            default:
                break;
        }
    }

    // ==================== [6] 一键起飞 ====================

    /**
     * 一键起飞：参数取自 {@code takeoff} 配置节(与 {@code Precondition.takeoff} 同源),
     * 二次确认后下发; 只判断<b>指令下发</b>是否成功(TakeoffCallback), 不阻塞等待起飞完成。
     * 超时按「可能已起飞」置位 {@link #tookOff}, 退出时提醒。
     */
    private void takeoff(Drone drone) {
        final TakeoffModule takeoffModule = drone.getTakeoffModule();
        if (takeoffModule == null) {
            hint("获取 TakeoffModule 失败, 无法起飞");
            return;
        }

        Node cfg = params.node("takeoff");
        final TakeoffParams tp = new TakeoffParams();
        tp.setTargetLatitude(cfg.dbl("target_latitude", 0));
        tp.setTargetLongitude(cfg.dbl("target_longitude", 0));
        tp.setTargetHeight(cfg.dbl("target_height", 120));
        tp.setSecurityTakeoffHeight(cfg.dbl("security_takeoff_height", 100));
        tp.setMaxSpeed(cfg.intVal("max_speed", 10));
        tp.setRthAltitude(cfg.intVal("rth_altitude", 100));

        // 枚举配置越界会被 fromValue 映射成 UNKNOWN(65535)——不拦截会把 65535 原样发往服务端。
        // 与 clampStick 同款「告警+修正」: 回退到配置默认值(1=返航 / 0=普通起飞)。
        OutOfControlAction ooc = OutOfControlAction.fromValue(cfg.intVal("out_of_control_action", 1));
        if (ooc == OutOfControlAction.UNKNOWN) {
            hint("[修正] out_of_control_action 配置值非法(合法: 0=悬停 1=返航 2=继续任务), 回退默认 1=返航");
            ooc = OutOfControlAction.RETURN_HOME;
        }
        tp.setOutOfControlAction(ooc);

        TakeoffMode tm = TakeoffMode.fromValue(cfg.intVal("takeoff_mode", 0));
        if (tm == TakeoffMode.UNKNOWN) {
            hint("[修正] takeoff_mode 配置值非法(合法: 0=起飞 1=起飞并飞向目标点), 回退默认 0=起飞");
            tm = TakeoffMode.TAKEOFF_WITH_FLY_TO;
        }
        tp.setTakeoffMode(tm);

        tp.setCommanderFlightMode(cfg.intVal("commander_flight_mode", 0));
        tp.setCommanderFlightHeight(cfg.dbl("commander_flight_height", 100));
        tp.setRthMode(cfg.intVal("rth_mode", 0));
        tp.setCloudToCloudId(cfg.str("cloud_to_cloud_id", ""));
        tp.setNeedVerifyFlightControl(cfg.intVal("need_verify_flight_control", -1));

        // 起飞参数全量回显, 确认前让操作者看清目标点。
        console("\n   一键起飞参数(CommandRequest / takeoff 配置节, 请核对目标点!):");
        console("     目标点: lat=" + tp.getTargetLatitude() + " lon=" + tp.getTargetLongitude()
                + " height=" + tp.getTargetHeight() + "m");
        console("     安全起飞高度=" + tp.getSecurityTakeoffHeight() + "m 最大速度=" + tp.getMaxSpeed()
                + "m/s 返航高度=" + tp.getRthAltitude() + "m(mode=" + tp.getRthMode() + ")");
        console("     失控动作=" + tp.getOutOfControlAction() + " 起飞模式=" + tp.getTakeoffMode()
                + " 指点飞行模式=" + tp.getCommanderFlightMode() + "(height=" + tp.getCommanderFlightHeight()
                + "m) cloud_to_cloud_id='" + tp.getCloudToCloudId()
                + "' need_verify=" + tp.getNeedVerifyFlightControl());

        // 同步落盘完整参数明细(逐项 属性: value; 控制台上面只做精简回显, 全量留档到日志文件供事后排查)。
        DemoLog.info(name(), "起飞参数(TakeoffParams):");
        DemoLog.info(name(), "  security_takeoff_height: " + tp.getSecurityTakeoffHeight() + "m");
        DemoLog.info(name(), "  max_speed: " + tp.getMaxSpeed() + "m/s");
        DemoLog.info(name(), "  out_of_control_action: " + tp.getOutOfControlAction());
        DemoLog.info(name(), "  rth_altitude: " + tp.getRthAltitude() + "m");
        DemoLog.info(name(), "  target_height: " + tp.getTargetHeight() + "m");
        DemoLog.info(name(), "  target_lat/lon: " + tp.getTargetLatitude() + ", " + tp.getTargetLongitude());
        DemoLog.info(name(), "  takeoff_mode: " + tp.getTakeoffMode());
        DemoLog.info(name(), "  rth_mode: " + tp.getRthMode());
        DemoLog.info(name(), "  commander_flight_mode/height: " + tp.getCommanderFlightMode()
                + " / " + tp.getCommanderFlightHeight() + " (-1=未设置)");
        DemoLog.info(name(), "  cloud_to_cloud_id: '" + tp.getCloudToCloudId() + "' (空=未设置)");
        DemoLog.info(name(), "  need_verify_flight_control: " + tp.getNeedVerifyFlightControl() + " (-1=未设置)");

        if (!confirmDangerous("一键起飞(飞机将真实起飞!)")) {
            return;
        }

        // TakeoffCallback 带 (error, result) 双参, 与 ResultCallbackWithError 不同,
        // 用 resultRef 捕获 flight_id/fly_to_id 供成功后回显。
        final Await<FlightHubError> await = new Await<FlightHubError>("一键起飞");
        final AtomicReference<TakeoffResult> resultRef = new AtomicReference<TakeoffResult>();
        Outcome o = runOutcome("一键起飞", timeout("takeoff_timeout_ms"), await, () ->
                takeoffModule.takeoff(tp, new FlightHubCallback.TakeoffCallback() {
                    @Override
                    public void onResult(FlightHubError error, TakeoffResult result) {
                        resultRef.set(result);
                        await.set(error);
                    }
                }));
        if (o == Outcome.SUCCESS) {
            TakeoffResult r = resultRef.get();
            hint("起飞指令下发成功: flight_id=" + (r != null ? r.getFlightId() : "?")
                + " fly_to_id=" + (r != null ? r.getFlyToId() : "?")
                + " (起飞过程请观察 OSD/现场, 需要收回请用 [9] 返航)");
        }
        if (o != Outcome.FAILED) {
            tookOff = true;
        }
    }

    // ==================== [7] WASD 摇杆 ====================

    /**
     * WASD 摇杆控制：按 {@code flight_control} 配置节的各轴值以固定 50Hz 连发
     * {@code sendStickControl}(fire-and-forget, 无回调; 走 DRC 实时通道)。
     *
     * <p>各轴中性 1024、SDK 建议操控范围 364–1684, 越界配置会被夹取; 各轴取值为配置驱动
     * (默认全中性, 需要动作请改配置)。须先建 DRC 链路并持有飞行控制权。
     *
     * <p><b>安全加固</b>：
     * <ul>
     *   <li>连发时长夹取到 {@value #STICK_DURATION_MAX_MS}ms 上限(
     *       配置驱动后若不设上限, 误多敲一个 0 会把不可控窗口放大到分钟级);</li>
     *   <li>连发期间<b>按 Enter 可随时中止</b>(每帧非阻塞探测共享 reader; 保证急停在唯一产生真实运动的
     *       动作期间仍然可达);</li>
     *   <li>结束/中止后<b>补发全中性「松杆」收尾帧</b>——SDK javadoc 明确「松杆建议连续发送若干帧全 1024」
     *       且 SDK 纯透传无自动回中; 不补发则飞机会按最后一帧(可能满杆)杆量继续飞到机端保护介入。</li>
     * </ul>
     */
    private void sticksControl(FlightControlModule fc) {
        if (!gotAuth) {
            hint("尚未获取飞行控制权, 建议先执行菜单 [2]; 摇杆控制需持有飞行控制权");
        }
        if (!linked) {
            hint("DRC 链路尚未建立, 建议先执行菜单 [4]; 摇杆指令经 DRC 实时通道下发");
        }

        Node cfg = params.node("flight_control");
        final StickControlData data = new StickControlData();
        data.setRoll(clampStick("roll", cfg.intVal("stick_roll", STICK_NEUTRAL)));
        data.setPitch(clampStick("pitch", cfg.intVal("stick_pitch", STICK_NEUTRAL)));
        data.setThrottle(clampStick("throttle", cfg.intVal("stick_throttle", STICK_NEUTRAL)));
        data.setYaw(clampStick("yaw", cfg.intVal("stick_yaw", STICK_NEUTRAL)));
        data.setGimbalPitch(clampStick("gimbal_pitch", cfg.intVal("stick_gimbal_pitch", STICK_NEUTRAL)));
        long durationMs = cfg.longVal("stick_duration_ms", 3000L);
        if (durationMs <= 0) {
            durationMs = 3000L;
        }
        if (durationMs > STICK_DURATION_MAX_MS) {
            // 与 clampStick 同款「告警+夹取」: 时长误配(如多敲一个 0)不该把不可控窗口放大到分钟级。
            console("   [夹取] stick_duration_ms=" + durationMs + " → " + STICK_DURATION_MAX_MS + "(上限)");
            DemoLog.warn(name(), "stick_duration_ms=" + durationMs + " 超出上限, 夹取为 " + STICK_DURATION_MAX_MS);
            durationMs = STICK_DURATION_MAX_MS;
        }
        final int total = (int) (durationMs / STICK_INTERVAL_MS);

        console("\n   WASD 摇杆参数(flight_control 配置节; 中性 " + STICK_NEUTRAL
                + ", 建议范围 " + STICK_MIN + "–" + STICK_MAX + "):");
        console("     roll=" + data.getRoll() + " pitch=" + data.getPitch()
                + " throttle=" + data.getThrottle() + " yaw=" + data.getYaw()
                + " gimbalPitch=" + data.getGimbalPitch());
        console("     连发: 50Hz × " + durationMs + "ms = " + total + " 次(期间按 Enter 可随时中止)");
        final boolean allNeutral = data.getRoll() == STICK_NEUTRAL && data.getPitch() == STICK_NEUTRAL
                && data.getThrottle() == STICK_NEUTRAL && data.getYaw() == STICK_NEUTRAL
                && data.getGimbalPitch() == STICK_NEUTRAL;
        if (allNeutral) {
            hint("各轴均为中性值(1024), 飞机不会移动——验证真实操控请修改 flight_control 配置节");
        }
        if (!confirmDangerous("WASD 摇杆连发(非中性值会让飞机真实移动!)")) {
            return;
        }

        console("\n>> [WASD 摇杆连发] 执行中(按 Enter 可中止) ...");
        DemoLog.info(name(), "WASD 连发开始: roll=" + data.getRoll() + " pitch=" + data.getPitch()
                + " throttle=" + data.getThrottle() + " yaw=" + data.getYaw()
                + " gimbalPitch=" + data.getGimbalPitch() + " total=" + total);
        int sent = 0;
        try {
            for (int i = 0; i < total; i++) {
                // sendStickControl 为 void fire-and-forget(SDK 内部已兜异常), 结果观察 OSD/现场。
                fc.drcSendStickControl(data);
                sent++;
                if (sent % 50 == 0) {
                    console("   进度: " + sent + "/" + total);
                }
                // 非阻塞探测共享 reader: 有任何待读输入即消费该行并中止连发——
                // 让急停等菜单动作在连发期间重新可达(消费防止残留输入串进下一次菜单读取)。
                if (pendingInputConsumed()) {
                    console("   [中止] 收到输入, 停止连发(已发送 " + sent + "/" + total + ")");
                    DemoLog.info(name(), "WASD 连发被用户中止: " + sent + "/" + total);
                    break;
                }
                try {
                    Thread.sleep(STICK_INTERVAL_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    DemoLog.warn(name(), "摇杆连发被中断, 已发送 " + sent + "/" + total);
                    break;
                }
            }
        } finally {
            // 松杆收尾(所有退出路径必经): SDK javadoc「松杆建议连续发送若干帧全 1024」且 SDK 纯透传
            // 无自动回中——不补发中性帧, 飞机会按最后一帧(可能满杆)杆量继续飞到机端保护介入,
            // 实际移动窗口超出操作者确认的时长。全中性配置无需收尾(每一帧本就是中性)。
            if (!allNeutral) {
                sendNeutralTail(fc);
            }
        }
        console("   结果 -> 已发送 " + sent + " 次摇杆指令(fire-and-forget, 实际效果请观察飞机)");
        DemoLog.info(name(), "WASD 连发结束: " + sent + "/" + total);
    }

    /** 非阻塞探测共享 reader 是否有待读输入; 有则消费一行并返回 true(探测异常按无输入处理)。 */
    private boolean pendingInputConsumed() {
        try {
            if (input.ready()) {
                input.readLine();
                return true;
            }
        } catch (java.io.IOException ignored) {
            // 探测失败按无输入处理, 连发照常继续。
        }
        return false;
    }

    /** 补发全中性「松杆」收尾帧(连发结束/中止后调用; 任何异常只记录, 不影响调用方流程)。 */
    private void sendNeutralTail(FlightControlModule fc) {
        try {
            StickControlData neutral = new StickControlData();
            neutral.setRoll(STICK_NEUTRAL);
            neutral.setPitch(STICK_NEUTRAL);
            neutral.setThrottle(STICK_NEUTRAL);
            neutral.setYaw(STICK_NEUTRAL);
            neutral.setGimbalPitch(STICK_NEUTRAL);
            // 主循环的中断路径会先恢复中断标志再进本方法, 若不先清掉, 第 1 帧后的 sleep 会立即抛
            // InterruptedException 把收尾截断成 1 帧。先清标志保证安全收尾完整(至多推迟响应中断
            // STICK_NEUTRAL_TAIL_FRAMES×20ms=200ms), 收尾后如数恢复。
            boolean wasInterrupted = Thread.interrupted();
            int sentFrames = 0;
            try {
                for (int i = 0; i < STICK_NEUTRAL_TAIL_FRAMES; i++) {
                    fc.drcSendStickControl(neutral);
                    sentFrames++;
                    try {
                        Thread.sleep(STICK_INTERVAL_MS);
                    } catch (InterruptedException e) {
                        wasInterrupted = true;
                        break;
                    }
                }
            } finally {
                if (wasInterrupted) {
                    Thread.currentThread().interrupt();
                }
            }
            console("   已补发 " + sentFrames + " 帧全中性(1024)松杆收尾帧");
            DemoLog.info(name(), "松杆收尾帧已补发 " + sentFrames + " 帧");
        } catch (Throwable t) {
            console("   [警告] 松杆收尾帧补发异常: " + Utils.describe(t) + "(请留意飞机是否仍在按末帧杆量移动)");
            DemoLog.error(name(), "松杆收尾帧补发异常: " + Utils.describe(t), t);
        }
    }

    /** 夹取摇杆轴值到 SDK 建议范围 [364,1684], 越界告警并夹取。 */
    private int clampStick(String axis, int v) {
        if (v < STICK_MIN || v > STICK_MAX) {
            int clamped = Math.max(STICK_MIN, Math.min(STICK_MAX, v));
            DemoLog.warn(name(), "摇杆轴 " + axis + "=" + v + " 超出建议范围["
                    + STICK_MIN + "," + STICK_MAX + "], 夹取为 " + clamped);
            console("   [夹取] 摇杆轴 " + axis + "=" + v + " → " + clamped);
            return clamped;
        }
        return v;
    }

    // ==================== [1] 消息监听(飞行控制相关) ====================

    /**
     * 建立飞行控制相关的全部消息监听(急停/摇杆可用性监听已随 SDK 删除, 只留提示)。
     *
     * <p>各路监听独立建立：单路失败只告警并继续其余路。全部订阅登记进 {@link #monitorSubs}
     * (模块退出前统一取消), 并同步登记进会话 {@code registry()} 兜底(close 幂等, 双重取消无害)。
     * 各路推送经 {@link #pushLog} <b>只落日志文件、不刷控制台</b>(DRC OSD/电池为高频推送,
     * 回显会把菜单 prompt 滚出屏幕; 交互期控制台留给菜单与动作结果)。
     */
    private void startMonitors(FlightControlModule fc, ControlAuthModule auth,
            DrcModule drc, DeviceStateModule state) {
        if (!monitorSubs.isEmpty()) {
            // 防御: dispatch 已按开/关分流, 正常不可达。
            hint("消息监听已开启(" + monitorSubs.size() + " 路), 再次选择 [1] 即关闭");
            return;
        }
        console("\n>> [消息监听] 建立飞行控制相关监听(推送只落日志文件, 不刷控制台) ...");

        // 飞机在线状态 + 链路状态。
        addMonitor("droneOnlineStatus", () -> Subscription.of("droneOnlineStatus",
                state.listenDroneOnlineStatus(new FlightHubCallback.DroneOnlineStatusCallback() {
                    @Override
                    public void onResult(DroneOnlineStatus s) {
                        pushLog("listenDroneOnlineStatus() #DroneOnlineStatus, 飞机在线状态: online=" + (s != null && s.isOnline()));
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

        // 飞行控制权状态。
        addMonitor("flightControlAuthority", () -> Subscription.of("flightControlAuthority",
                auth.listenFlightControlAuthority(new FlightHubCallback.FlightControlAuthorityCallback() {
                    @Override
                    public void onResult(boolean isGranted) {
                        pushLog("listenFlightControlAuthority() #boolean, 飞行控制权状态: is_granted=" + isGranted);
                    }
                }),
                auth::cancelListenFlightControlAuthority));

        // 急停可用 / 摇杆可用：SDK 已删除全部返回 boolean 的 listen*Available 门控接口
        // (能否执行由服务端判定), 无对应 API 可订阅——见类 javadoc。
        hint("急停/摇杆可用性监听已随 SDK Available 门控接口移除, 无法订阅; 以 [7]/[8] 执行结果为准");

        // DRC OSD / 电池遥测(DRC 链路已连时才有推送)。
        addMonitor("drcOsdInfo", () -> Subscription.of("drcOsdInfo",
                fc.listenDrcOsdInfo(new FlightHubCallback.DrcOsdInfoCallback() {
                    @Override
                    public void onResult(DrcOsdInfo info) {
                        if (info == null) {
                            return;
                        }
                        pushLog("listenDrcOsdInfo() #DrcOsdInfo, DRC OSD: lat=" + info.getLatitude()
                                + " lon=" + info.getLongitude() + " height=" + info.getHeight()
                                + " head=" + info.getAttitude_head()
                                + " hSpeed=" + info.getHorizontal_speed()
                                + " vSpeed=" + info.getVertical_speed()
                                + " homeDist=" + info.getHome_distance());
                    }
                }),
                fc::cancelListenDrcOsdInfo));
        addMonitor("drcBatteriesInfo", () -> Subscription.of("drcBatteriesInfo",
                fc.listenDrcBatteriesInfo(new FlightHubCallback.DrcBatteriesInfoCallback() {
                    @Override
                    public void onResult(DrcBatteriesInfo info) {
                        if (info == null) {
                            return;
                        }
                        pushLog("listenDrcBatteriesInfo() #DrcBatteriesInfo, DRC 电池: capacity=" + info.getCapacity_percent()
                                + "% remainFlight=" + info.getRemain_flight_time() + "s"
                                + " landingPower=" + info.getLanding_power()
                                + "% rthPower=" + info.getReturn_home_power() + "%");
                    }
                }),
                fc::cancelListenDrcBatteriesInfo));

        console("   消息监听建立完成: 共 " + monitorSubs.size()
                + " 路(推送见日志文件; 再次选 [1] 关闭; 退出本模块时自动取消)");
        DemoLog.info(name(), "消息监听合集建立完成: " + monitorSubs.size() + " 路");
    }

    /**
     * 取消 [1] 建立的全部监听(开/关切换的「关」半边; {@link Subscription#close()} 幂等且吞异常)。
     *
     * <p>关闭 sub 的同时从会话 registry 移除，避免 registry 长期持有已关闭 sub 引用(长会话下反复
     * 开/关监听会累积冗余条目)。registry 作为兜底仍有效：teardown() 的 finally 块会再次遍历
     * monitorSubs(彼时已 clear，无重复操作), 会话 close 时 registry 也只剩其余资源。
     */
    private void stopMonitors() {
        console("\n>> [消息监听] 取消全部 " + monitorSubs.size() + " 路监听 ...");
        SubscriptionRegistry reg = registry();
        for (Subscription sub : monitorSubs) {
            sub.close();
            reg.remove(sub);   // 主动移出 registry，避免已关闭引用长期积累
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

    // ==================== 退出收尾 ====================

    /**
     * 退出前尽力而为收尾：断 DRC(若已连) → 释放飞行控制权(若已占) → 取消全部监听订阅。
     *
     * <p><b>不自动返航/降落</b>：若执行过起飞, 仅告警提醒——demo 静默触发返航比把决定留给操作者更危险。
     * 本方法在 {@link #run} 的 {@code finally} 中调用：动作步各自经 {@link #quietStep} 兜
     * {@code Throwable}(SDK 为 JNI, 原生 Error 不得中断后续收尾), 订阅取消放在内层 {@code finally}。
     */
    private void teardown(ControlAuthModule auth, DrcModule drc, long actionTimeout) {
        if (tookOff) {
            console("\n   警告: 本次会话执行过一键起飞, 飞机可能仍在空中!");
            console("   本 demo 不会自动返航/降落, 需要收回请重新进入本模块用 [9] 返航, 或经其他手段处置。");
            DemoLog.warn(name(), "退出时提醒: 执行过起飞, 未自动返航");
        }
        if (linked || gotAuth || !monitorSubs.isEmpty()) {
            console("\n>> [退出收尾] 还原设备状态并取消监听 ...");
        }
        try {
            // callItem/syncItem 已兜 Exception 并回显成败; quietStep 再兜原生 Error, 单步失败不中断后续收尾。
            if (linked) {
                quietStep("DRC 链路断开(收尾)",
                        () -> syncItem("DRC 链路断开(收尾)", drc::disconnectDrcLink, actionTimeout));
            }
            if (gotAuth) {
                quietStep("释放飞行控制权(收尾)",
                        () -> callItem("释放飞行控制权(收尾)",
                                cb -> auth.releaseFlightControlAuthority(cb), actionTimeout));
            }
        } finally {
            // 取消 [1] 建立的全部监听(Subscription.close 幂等、内部吞 Throwable)。
            if (!monitorSubs.isEmpty()) {
                for (Subscription sub : monitorSubs) {
                    sub.close();
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

    // ==================== 交互辅助 ====================

    /**
     * 危险动作二次确认：须输入 {@code yes}(不区分大小写)才执行, 其他输入(含空行/EOF)一律取消。
     * 用于会让飞机真实起飞/移动的动作([6] 起飞 / [7] WASD)。
     */
    private boolean confirmDangerous(String action) {
        String line = Utils.readLine(input,
                "\n   警告: 即将执行「" + action + "」\n   输入 yes 确认执行, 其他任意输入取消: ");
        boolean ok = line != null && line.equalsIgnoreCase("yes");
        if (!ok) {
            hint("已取消: " + action);
        } else {
            DemoLog.info(name(), "用户确认执行: " + action);
        }
        return ok;
    }

    // ---- 控制台结果回显(范式同 PayloadControlDemo: 三态结果; 监听推送只落日志)----
    // DemoLog 只落滚动日志文件, 交互终端看不到调用结果; 以下把每个测试项的
    // 「开始 / 成功 / 超时 / 失败(附错误码与消息)」实时回显到 stdout。
    // Outcome 枚举 / callOutcome / syncOutcome / runOutcome 由基类 ModuleDemo 统一提供。

    /** 回显一行到控制台(stdout)。 */
    private void console(String line) {
        System.out.println(line);
    }

    /** 提示/说明信息：写日志文件的同时回显控制台。 */
    private void hint(String msg) {
        DemoLog.info(name(), msg);
        console("   " + msg);
    }

    /**
     * [1] 消息监听的推送记录：<b>只落日志文件、不刷控制台</b>——监听是持续后台观察,
     * 高频路(DRC OSD/电池)逐条回显会把菜单 prompt 滚出屏幕。回调在 SDK 线程触发, 只做记录不阻塞。
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

    /** 飞行控制菜单文案(每轮循环打印; 编号与类 javadoc 的功能列表一致; 分组间空行便于阅读)。 */
    private String menu() {
        return "\n"
                + "  --- 监听 ---\n"
                + "  1.  消息监听(飞行控制相关) 开/关切换: 在线/链路、飞行控制权、DRC OSD/电池遥测(推送只落日志文件)\n"
                + "\n"
                + "  --- 控制权 / DRC ---\n"
                + "  2.  获取飞行控制权         (acquireFlightControlAuthority)\n"
                + "  3.  释放飞行控制权         (releaseFlightControlAuthority)\n"
                + "  4.  DRC 链路: 手动连接     (connectDrcLink)\n"
                + "  5.  DRC 链路: 手动断开     (disconnectDrcLink)\n"
                + "\n"
                + "  --- 飞行动作(6/7 需二次确认; 参数见 demo_params.json) ---\n"
                + "  6.  飞行: 一键起飞         (takeoff, 参数取自 takeoff 配置节, 飞机将真实起飞!)\n"
                + "  7.  飞行: WASD             (sendStickControl 50Hz 连发, 参数取自 flight_control 配置节, 需先建 DRC 链路; 连发期间按 Enter 可中止)\n"
                + "  8.  飞行: 急停             (drcEmergencyStop)\n"
                + "  9.  飞行: 返航             (returnHome)\n"
                + "  10. 飞行: 取消返航         (returnHomeCancel)\n"
                + "\n"
                + "  q.  返回上级菜单(自动收尾: 断链/释权/退订; 不会自动返航)\n";
    }
}
