// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.module;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.AircraftLocation;
import com.dji.flighthubsdk.bean.DroneOnlineStatus;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.GroundTakeoffParams;
import com.dji.flighthubsdk.bean.GroundTakeoffResult;
import com.dji.flighthubsdk.bean.FlyToPointParams;
import com.dji.flighthubsdk.bean.FlyToPointStartResult;
import com.dji.flighthubsdk.bean.FlyToTargetPoint;
import com.dji.flighthubsdk.bean.FlyToUpdateTargetPointsParams;
import com.dji.flighthubsdk.bean.LinkStatus;
import com.dji.flighthubsdk.bean.StickControlData;
import com.dji.flighthubsdk.bean.TakeoffParams;
import com.dji.flighthubsdk.bean.TakeoffResult;
import com.dji.flighthubsdk.core.ControlAuthModule;
import com.dji.flighthubsdk.core.DeviceStateModule;
import com.dji.flighthubsdk.core.DrcModule;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.DroneOsdModule;
import com.dji.flighthubsdk.core.FlightControlModule;
import com.dji.flighthubsdk.core.GroundFlightModule;
import com.dji.flighthubsdk.core.FlyToPointModule;
import com.dji.flighthubsdk.core.TakeoffModule;
import com.dji.flighthubsdk.demo.base.Menu;
import com.dji.flighthubsdk.demo.base.ModuleDemo;
import com.dji.flighthubsdk.demo.base.Precondition;
import com.dji.flighthubsdk.demo.runtime.Await;
import com.dji.flighthubsdk.demo.runtime.FlightHubException;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.runtime.Node;
import com.dji.flighthubsdk.demo.runtime.Subscription;
import com.dji.flighthubsdk.demo.runtime.SubscriptionRegistry;
import com.dji.flighthubsdk.demo.utils.Utils;
import com.dji.flighthubsdk.generated.DrcBatteriesInfo;
import com.dji.flighthubsdk.generated.DrcLandingMissionState;
import com.dji.flighthubsdk.generated.DrcOsdInfo;
import com.dji.flighthubsdk.value.GroundLandingCommand;
import com.dji.flighthubsdk.value.OutOfControlAction;
import com.dji.flighthubsdk.value.SpeedMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 飞行控制模块 Demo(菜单驱动)：飞行控制相关消息监听、飞行控制权、DRC 链路、
 * 一键起飞、WASD 摇杆、急停、返航/取消返航、设置飞行速度、指点飞行、舱外起降。
 *
 * <p>按 {@code PayloadControlDemo} 的子菜单范式实现 (三态结果/监听开关/退订收尾等机制同款),
 * 提供以下功能:
 * <ul>
 *   <li>[1] 消息监听 (飞行控制相关): 在线/链路状态 + 飞行控制权状态;
 *       <b>不含</b>「监听急停可用状态」/「监听摇杆控制可用状态」——SDK 已在
 *       「移除所有 Available 状态门控接口」重构(2026-07, Breaking Change)中删除全部
 *       <b>返回 boolean 的</b> {@code listen*Available} 可用性门控接口(FlightControlModule 的
 *       急停/摇杆可用性监听在列), 能否执行改由服务端判定(直接调用 [7]/[8], 以返回错误码为准);
 *       另包含 DRC OSD / 电池遥测 / 降落任务状态订阅(亦属飞控相关推送, DRC 链路已连时才有数据);</li>
 *   <li>[2]/[3] 获取/释放飞行控制权;</li>
 *   <li>[4]/[5] DRC 链路手动连接/断开;</li>
 *   <li>[6] 一键起飞; [7] WASD 摇杆; [8] 急停;
 *       [9] 返航; [10] 取消返航; [11] DRC 实时设置飞行速度;</li>
 *   <li>[12] 指点飞行启动; [13] 指点飞行更新目标点(须先经 [12] 拿到 fly_to_id);</li>
 *   <li>[14] 舱外起飞; [15] 舱外降落(降落至机场 / 取消降落)——面向「飞机已在机场<b>舱外</b>」
 *       的场景(起飞不经开舱盖流程; 降落由机场自动开舱盖→飞机降落入舱→关舱盖);</li>
 *   <li>[16] 舱外起降状态监听——独立入口, 逻辑完全自包含在 {@link GroundFlightMonitor} 内,
 *       与 [1] 互不影响、可同时开启。集中订阅「舱外起飞→机场降落」全过程的 11 路推送
 *       (飞机模式码、机场舱盖/在舱/模式码/推杆、降落任务状态、DRC OSD、位置状态、
 *       在线/链路/任务异常), 推送同样只落日志文件。</li>
 * </ul>
 *
 * <p><b>菜单序号维护约定</b>：上面所有 {@code [n]} 的 n <b>不是手写的常量</b>, 而是
 * {@link Menu#item} 的调用次序({@code section}/{@code note} 不占号, 见 {@link Menu} 类注释)。
 * 因此<b>在菜单中间插入一项, 其后所有序号会整体位移</b>——本类曾因此把 [11]/[12]/[13]
 * (设置飞行速度/指点飞行启动/更新目标点)插到中段, 把舱外起飞/舱外降落/舱外起降状态监听
 * 顶到 [14]/[15]/[16], 而 README.md 与 DESIGN.md 停在旧号, 使用者照文档点 [11] 会点到
 * 「设置飞行速度」、点 [13] 会点到「指点飞行更新目标点」(会让飞机真实改向)。
 * 故<b>增删菜单项后必须同步这四处</b>: 本类 javadoc 菜单表与危险动作清单、README.md 的
 * 「模块 Demo」清单表 / 「3.1 舱外起降状态监听」/ 「危险动作二次确认」、DESIGN.md 的
 * §4.5 三态判据 / §4.7 挂接点。核对口径(锚定行首, 否则会把本段注释自己也数进去):
 * {@code grep -n "^\s*\.item(" FlightControlDemo.java} 的第 N 行即序号 N, 共应为 16 行。
 *
 * <p><b>设计要点</b>:
 * <ul>
 *   <li><b>[6] 起飞参数配置驱动</b>: 全部取自 {@code demo_params.json} 的 {@code takeoff} 节
 *       (与场景 {@code Precondition.takeoff} 同源); 只判断<b>指令下发</b>是否成功,
 *       不阻塞等待起飞完成; </li>
 *   <li><b>[7] WASD 摇杆参数与安全加固</b>: 各轴取值范围为 SDK 声明的建议操控范围
 *       364–1684(中性 1024)。各轴取值由 {@code flight_control} 配置节提供 (默认全 1024=中性,
 *       需要动作请改配置), 下发前夹取到 [364, 1684]; 固定 20Hz(50ms)连发, 时长可配且夹取到
 *       {@value #STICK_DURATION_MAX_MS}ms 上限; 连发期间<b>按 Enter 可随时中止</b>;
 *       结束/中止后补发全中性「松杆」收尾帧(SDK 约定, 详见 {@link #sticksControl});</li>
 *   <li><b>危险动作二次确认</b>：会让飞机真实起飞/移动/改向/降落入舱的<b>七个</b>动作执行前须输入
 *       {@code yes} 确认——[6] 一键起飞、[7] WASD 摇杆、[11] DRC 实时设置飞行速度、
 *       [12] 指点飞行启动、[13] 指点飞行更新目标点、[14] 舱外起飞、[15] 的「降落至机场」
 *       (取消降落不确认); 急停/返航/取消返航/取消降落为恢复类动作, 不设确认(需要时越快越好);</li>
 *   <li>[1] 为<b>开/关切换</b>且退出模块前统一退订(monitorSubs + 会话 registry 双保险);
 *       各路推送(含高频的 DRC OSD/电池)<b>只落日志文件、不刷控制台</b>, 交互期控制台留给菜单;</li>
 *   <li><b>超时语义</b>：获取类动作(获取控制权/建链)超时按「可能已生效」置位, 退出收尾照常还原;
 *       释放类仅明确成功才清位(安全方向); <b>起飞类与收回类对称</b>——[6]/[14] 起飞与
 *       [9] 返航/[15] 降落至机场均按 {@code != FAILED} 置位(超时≠命令未生效),
 *       [10] 取消返航/[15] 取消降落则按 {@code != FAILED} 清位(同为安全方向);</li>
 *   <li>退出前尽力而为收尾：断 DRC(若已连) → 释放飞行控制权(若已占) → 退订;
 *       <b>不会自动返航/降落</b>——若执行过起飞, 退出时仅告警提醒, 把飞机收回的决定留给操作者
 *       (demo 静默触发返航比留在空中更危险)。</li>
 * </ul>
 *
 * <p><b>操作顺序建议</b>：[2] 获取飞行控制权 → [4] 建 DRC 链路 → [6]-[15] 飞行动作;
 * WASD/急停/设置飞行速度等 DRC 操控指令走 DRC 实时通道(MQTT-DRC 或 RTC), 须先建链。菜单不强制顺序, 缺前置时仅提示。
 * 做舱外起降([14]/[15])时建议先开 [16] 旁路观察整条状态链。
 *
 * <p><b>结果回显</b>: {@code DemoLog} 只写日志文件, 故每个操作项的「开始/[成功]/[超时]/[失败](错误码)」
 * 经 {@link #runOutcome} 实时回显控制台; [1] 的各路状态推送只落日志文件 (见 {@link #pushLog})。
 */
public class FlightControlDemo extends ModuleDemo {

    /**
     * WASD 摇杆连发间隔 (ms), 固定 20Hz。
     *
     * <p>取值依据 SDK {@code StickControlData} 类注释「建议发送频率约 10–20Hz」的<b>上界</b>:
     * 50ms 一帧 = 20Hz。曾用 20ms(50Hz)——超出建议区间 2.5 倍, DRC 实时通道被无谓帧挤占,
     * 弱网下反而更容易丢帧/堆积延迟, 让杆量响应滞后于操作者预期。
     * {@code DrcFlightScenario} 的摇杆连发同为 50ms, 两条 DRC 操控路径口径一致。
     */
    private static final int STICK_INTERVAL_MS = 50;
    /** 连发进度回显间隔 (帧): 由 {@link #STICK_INTERVAL_MS} 推导为「每秒一次」, 不随频率调整而漂移。 */
    private static final int STICK_PROGRESS_EVERY_FRAMES = 1000 / STICK_INTERVAL_MS;
    /** WASD 连发时长上限 (ms), 固定 10s——配置更大值会被夹取, 防止误配把连发窗口放大到分钟级。 */
    private static final long STICK_DURATION_MAX_MS = 10000L;
    /** 松杆收尾帧数: 连发结束/中止后补发的全中性帧 (SDK javadoc: 「松杆建议连续发送若干帧全 1024」)。 */
    private static final int STICK_NEUTRAL_TAIL_FRAMES = 10;
    /** SDK 声明的摇杆各轴建议操控范围 (中性 1024); 配置越界会被夹取。 */
    private static final int STICK_MIN = 364;
    private static final int STICK_NEUTRAL = 1024;
    private static final int STICK_MAX = 1684;

    /** [1] 消息监听合集建立的订阅; 模块退出前统一取消。 */
    private final List<Subscription> monitorSubs = new ArrayList<Subscription>();

    // 仅用于顺序提示与退出收尾判断, 不强制流程顺序。
    private boolean gotAuth;
    private boolean linked;

    /** 是否执行过起飞([6] 一键起飞 / [14] 舱外起飞; 退出时据此提醒「飞机可能仍在空中」, 不自动返航)。 */
    private boolean tookOff;
    /** 是否下发过指点飞行(退出时据此提醒「飞机可能仍在飞向目标点」, 不自动停止)。 */
    private boolean flyToStarted;
    /** [12] 启动指点飞行返回的任务 ID; [13] 更新目标点必须带上它, 拿不到则 [13] 不可用。 */
    private String flyToId;

    /**
     * 是否下发过<b>收回类</b>动作([9] 返航 / [15] 舱外降落至机场), 且此后未被作废
     * ([10] 取消返航 / [15] 取消降落 / <b>再次起飞</b> 都会清位)。
     *
     * <p><b>只用于细化退出提醒的措辞, 绝不用来清 {@link #tookOff}</b>——「指令下发成功」只代表
     * 服务端收下了指令, 不代表飞机已落地; 据此清位会把「可能仍在空中」的告警整条抹掉, 制造
     * 与事实相反的安全暗示。本标志的作用只是把告警从「你什么都没做」改成「你做了但仍需确认」。
     *
     * <p><b>置位/清位的方向恒为安全侧</b>：置 true 只会让提醒变缓和, 故仅在收回指令
     * {@code != FAILED} 时置; 清 false 只会让提醒变响, 故任何「可能作废了收回动作」的操作
     * (取消返航/取消降落/再次起飞)都清。粒度是跨动作的——[15] 取消降落也会清掉 [9] 返航置的位,
     * 方向仍是更响的告警, 可接受。
     */
    private boolean recoveryDispatched;

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

        final long actionTimeout = defaultTimeout();
        DemoLog.info(name(), "actionTimeout=" + actionTimeout + "ms");

        // [16] 舱外起降状态监听: 逻辑完全自包含在 GroundFlightMonitor 内(自己的订阅列表/开关/渲染),
        // 与 [1] 的 monitorSubs/startMonitors 互不影响, 可同时开启。
        final GroundFlightMonitor groundMonitor = new GroundFlightMonitor(session, registry());

        try {
            Menu.of(this, "执行 " + name())
                    .header(() -> {
                        if (!monitorSubs.isEmpty()) {
                            console("(消息监听中：" + monitorSubs.size()
                                    + " 路; 推送只落日志文件; 再次选 [1] 关闭)");
                        }
                        if (groundMonitor.isRunning()) {
                            console("(舱外起降状态监听中：" + groundMonitor.size()
                                    + " 路; 推送只落日志文件; 再次选 [16] 关闭)");
                        }
                    })
                    .section("--- 监听 ---")
                    .item("消息监听(飞行控制相关) 开/关切换: 在线/链路、飞行控制权、DRC OSD/电池遥测、"
                            + "降落任务状态(推送只落日志文件)", () -> {
                        // 开/关切换: 已开启则全部取消 (部分建立失败后可关掉重开补订)。
                        if (monitorSubs.isEmpty()) {
                            startMonitors(fc, auth, drc, state);
                        } else {
                            stopMonitors();
                        }
                    })
                    .section("--- 控制权 / DRC ---")
                    .item("获取飞行控制权         (acquireFlightControlAuthority, 含 controls/bid)", () -> {
                        // 超时≠失败: 命令可能已生效 (应答迟到被忽略), 按「可能已占权」置位, 退出收尾照常释放。
                        if (showControlAuthResult("获取飞行控制权", true, auth, actionTimeout) != Outcome.FAILED) {
                            gotAuth = true;
                        }
                    })
                    .item("释放飞行控制权         (releaseFlightControlAuthority, 含 controls/bid)", () -> {
                        if (showControlAuthResult("释放飞行控制权", false, auth, actionTimeout) == Outcome.SUCCESS) {
                            gotAuth = false;
                        }
                    })
                    .item("DRC 链路: 手动连接     (connectDrcLink)", () -> {
                        if (!gotAuth) {
                            hint("尚未获取飞行控制权, 建议先执行菜单 [2]");
                        }
                        // 超时按「链路可能已建」置位, 退出收尾照常断开。
                        if (syncOutcome("DRC 链路手动连接", drc::connectDrcLink, actionTimeout) != Outcome.FAILED) {
                            linked = true;
                        }
                    })
                    .item("DRC 链路: 手动断开     (disconnectDrcLink)", () -> {
                        if (syncItem("DRC 链路手动断开", drc::disconnectDrcLink, actionTimeout)) {
                            linked = false;
                        }
                    })
                    .section("--- 飞行动作(6/7 需二次确认; 参数见 demo_params.json) ---")
                    .item("飞行: 一键起飞         (takeoff, 参数取自 takeoff 配置节, 飞机将真实起飞!)",
                            () -> takeoff(drone))
                    .item("飞行: WASD             (sendStickControl 20Hz 连发, 参数取自 flight_control 配置节, "
                            + "需先建 DRC 链路; 连发期间按 Enter 可中止)", () -> sticksControl(fc))
                    // 急停是恢复类动作, 不设二次确认 (需要时越快越好)。
                    .item("飞行: 急停             (drcEmergencyStop)",
                            () -> callItem("飞行急停", cb -> fc.drcEmergencyStop(cb), actionTimeout))
                    // 返航是<b>收回类</b>动作(与「恢复类=不设二次确认」是两套分类, 勿混):
                    // 与 [15] 降落至机场对称, 按 != FAILED 置位 recoveryDispatched——超时只说明应答未到,
                    // 不代表命令未在设备侧生效。仅细化退出提醒的措辞, 绝不据此清 tookOff。
                    // 注意: [8] 急停不属收回类(急停后飞机悬停在空中), 切勿照此给急停补置位。
                    .item("飞行: 返航             (returnHome)", () -> {
                        if (callOutcome("返航", cb -> fc.returnHome(cb), actionTimeout) != Outcome.FAILED) {
                            recoveryDispatched = true;
                        }
                    })
                    // 取消返航是撤销类动作: 按 != FAILED 清位, 让退出提醒退回强告警(更响, 恒为安全方向)。
                    // FAILED 时返航确实未被取消, 保持原值——此时缓和档才是与事实相符的一档。
                    .item("飞行: 取消返航         (returnHomeCancel)", () -> {
                        if (callOutcome("取消返航", cb -> fc.returnHomeCancel(cb), actionTimeout)
                                != Outcome.FAILED) {
                            recoveryDispatched = false;
                        }
                    })
                    // 改的是「实时飞行速度」, 与 takeoff/flyTo 参数里的 max_speed、speed_mode 无关(见方法注释)。
                    .item("飞行: 设置飞行速度     (drcSetFlyingMaxSpeed, 参数取自 flight_control.flying_max_speed, "
                            + "需先建 DRC 链路)", () -> setFlyingMaxSpeed(fc, actionTimeout))
                    .section("--- 指点飞行(12/13 需二次确认; 参数见 demo_params.json) ---")
                    .item("指点飞行: 启动         (flyTo.start, 参数取自 fly_to 配置节, 飞机将真实飞向目标点!)",
                            () -> flyToStart(drone, actionTimeout))
                    .item("指点飞行: 更新目标点   (flyTo.updateTargetPoints, 参数取自 fly_to_update 配置节)",
                            () -> flyToUpdate(drone, actionTimeout))
                    .note("须先执行 [12] 拿到 fly_to_id")
                    .section("--- 舱外起降(飞机停在机场舱外; 14 与 15 的「降落至机场」需二次确认) ---")
                    .item("飞行: 舱外起飞         (groundTakeoff, 参数取自 ground_takeoff 配置节, 飞机将真实起飞!)",
                            () -> groundTakeoff(drone))
                    .item("飞行: 舱外降落         (groundLanding, 交互选择 0=降落至机场[自动开舱盖->降落->关舱盖] "
                            + "1=取消降落[悬停])", () -> groundLanding(drone, actionTimeout))
                    // 开/关切换; 全部逻辑自包含在 GroundFlightMonitor 内, 本类不参与其订阅管理。
                    .item("舱外起降状态监听 开/关切换: 飞机模式码、机场舱盖/在舱/模式码/推杆、降落任务状态、"
                            + "DRC OSD、在线/链路/任务异常(共 11 路, 推送只落日志文件)",
                            () -> groundMonitor.toggle())
                    .quit("返回上级菜单(自动收尾: 断链/释权/退订; 不会自动返航/降落/停止指点飞行)")
                    .exitLog("退出飞行控制菜单")
                    .run();
        } finally {
            teardown(auth, drc, groundMonitor, actionTimeout);
        }
    }


    // ==================== [6] 一键起飞 ====================

    /**
     * 一键起飞: 参数取自 {@code takeoff} 配置节 (与 {@code Precondition.takeoff} 同源),
     * 二次确认后下发; 只判断<b>指令下发</b>是否成功 (TakeoffCallback), 不阻塞等待起飞完成。
     * 超时按「可能已起飞」置位 {@link #tookOff}, 退出时提醒。
     */
    private void takeoff(Drone drone) {
        final TakeoffModule takeoffModule = drone.getTakeoffModule();
        if (takeoffModule == null) {
            hint("获取 TakeoffModule 失败, 无法起飞");
            return;
        }

        // 参数组装统一走 Precondition.buildTakeoffParams —— [6] 与场景前置共用同一份读取逻辑与
        // 兜底默认值(此前两处各抄一份, 高度类字段的兜底值已实际漂移)。demo_params.json 的
        // takeoff 节恒优先, 字面默认值只在缺键时兜底; 非法枚举的告警经 hint 回显控制台。
        // speed_mode / real_height_cap_* 等新增可选字段亦在其内读取(-1 = 不下发)。
        final TakeoffParams tp = Precondition.buildTakeoffParams(params, this::hint);

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

        // 同步落盘完整参数明细 (逐项 属性: value; 控制台上面只做精简回显, 全量留档到日志文件供事后排查)。
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
        DemoLog.info(name(), "  speed_mode: " + tp.getSpeedMode() + " (UNKNOWN=未设置)");
        DemoLog.info(name(), "  real_height_cap_enabled/limit: " + tp.getRealHeightCapEnabled()
                + " / " + tp.getRealHeightCapLimit() + " (-1=未设置)");

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
            // 再次起飞使此前的收回指令作废: 不清位会让「先降落再起飞」的会话在退出时
            // 仍按「已下发过收回指令」的缓和措辞提示, 那是与事实相反的安全暗示。
            recoveryDispatched = false;
        }
    }

    // ==================== [14] 舱外起飞 ====================

    /**
     * 舱外起飞：飞机停在机场<b>舱外</b>地面时下发起飞(不经开舱盖流程)。参数取自
     * {@code ground_takeoff} 配置节, 二次确认后下发; 只判断<b>指令下发</b>是否成功
     * (GroundTakeoffCallback), 不阻塞等待起飞完成。超时按「可能已起飞」置位 {@link #tookOff},
     * 退出时提醒。
     */
    private void groundTakeoff(Drone drone) {
        final GroundFlightModule groundFlightModule = drone.getGroundFlightModule();
        if (groundFlightModule == null) {
            hint("获取 GroundFlightModule 失败, 无法舱外起飞");
            return;
        }

        Node cfg = params.node("ground_takeoff");
        final GroundTakeoffParams gp = new GroundTakeoffParams();
        gp.setAutoTakeoffHeight(cfg.intVal("auto_takeoff_height", 100));

        // 枚举配置越界会被 fromValue 映射成 UNKNOWN(65535)——不拦截会把 65535 原样发往服务端。
        OutOfControlAction ooc = OutOfControlAction.fromValue(cfg.intVal("out_of_control_action", 1));
        if (ooc == OutOfControlAction.UNKNOWN) {
            hint("[修正] out_of_control_action 配置值非法(合法: 0=悬停 1=返航 2=继续任务), 回退默认 1=返航");
            ooc = OutOfControlAction.RETURN_HOME;
        }
        gp.setOutOfControlAction(ooc);

        // 服务端对起飞高度有 2-1500m 硬校验, 越界只会换来一个错误码; 本地先提示, 但不擅自改值。
        if (gp.getAutoTakeoffHeight() < 2 || gp.getAutoTakeoffHeight() > 1500) {
            hint("[告警] auto_takeoff_height=" + gp.getAutoTakeoffHeight()
                    + " 超出服务端允许范围 2-1500m, 下发大概率被拒");
        }

        console("\n   舱外起飞参数(GroundTakeoffRequest / ground_takeoff 配置节):");
        console("     自动起飞高度=" + gp.getAutoTakeoffHeight() + "m"
                + " 失控动作=" + gp.getOutOfControlAction());
        console("     (device_sn 由 SDK 注入机场 SN, 无需配置)");

        DemoLog.info(name(), "舱外起飞参数(GroundTakeoffParams):");
        DemoLog.info(name(), "  auto_takeoff_height: " + gp.getAutoTakeoffHeight() + "m");
        DemoLog.info(name(), "  out_of_control_action: " + gp.getOutOfControlAction());

        if (!confirmDangerous("舱外起飞(飞机将真实起飞!)")) {
            return;
        }

        // GroundTakeoffCallback 带 (error, result) 双参, 与 ResultCallbackWithError 不同,
        // 用 resultRef 捕获 flight_id 供成功后回显。
        final Await<FlightHubError> await = new Await<FlightHubError>("舱外起飞");
        final AtomicReference<GroundTakeoffResult> resultRef = new AtomicReference<GroundTakeoffResult>();
        Outcome o = runOutcome("舱外起飞", timeout("takeoff_timeout_ms"), await, () ->
                groundFlightModule.groundTakeoff(gp, new FlightHubCallback.GroundTakeoffCallback() {
                    @Override
                    public void onResult(FlightHubError error, GroundTakeoffResult result) {
                        resultRef.set(result);
                        await.set(error);
                    }
                }));
        if (o == Outcome.SUCCESS) {
            GroundTakeoffResult r = resultRef.get();
            hint("舱外起飞指令下发成功: flight_id=" + (r != null ? r.getFlightId() : "?")
                    + " (起飞过程请观察 OSD/现场, 需要收回请用 [15] 舱外降落或 [9] 返航)");
        }
        if (o != Outcome.FAILED) {
            tookOff = true;
            // 同 [6]: 再次起飞使此前的收回指令作废, 必须清位, 否则退出提醒会给出偏乐观的一档。
            recoveryDispatched = false;
        }
    }

    // ==================== [15] 舱外降落 ====================

    /**
     * 舱外降落：交互选择指令后下发。{@code 0=降落至机场}(机场自动开舱盖→无人机降落→关舱盖)
     * 会让飞机真实降落入舱, 故加二次确认; {@code 1=取消降落}使飞机转为悬停, 属恢复类动作不设确认。
     *
     * <p>降落过程状态可经 [1] 消息监听的「降落任务状态」推送观察
     * (见 {@link #describeLandingState}, 只落日志文件)。
     */
    private void groundLanding(Drone drone, long actionTimeout) {
        final GroundFlightModule groundFlightModule = drone.getGroundFlightModule();
        if (groundFlightModule == null) {
            hint("获取 GroundFlightModule 失败, 无法舱外降落");
            return;
        }

        // readIntInRange 的退出哨兵 QUIT 为 -1, 与合法区间 [0,1] 不冲突。
        int cmdValue = Utils.readIntInRange(input,
                "\n   舱外降落指令(0=降落至机场[自动开舱盖->降落->关舱盖] 1=取消降落[悬停], q 取消): ", 0, 1);
        if (cmdValue == Utils.QUIT) {
            hint("已取消: 舱外降落");
            return;
        }

        final GroundLandingCommand cmd = GroundLandingCommand.fromValue(cmdValue);
        final boolean landing = cmd == GroundLandingCommand.LAND_TO_DOCK;
        if (landing && !confirmDangerous("舱外降落: 降落至机场(飞机将真实降落入舱!)")) {
            return;
        }

        String label = "舱外降落(" + (landing ? "降落至机场" : "取消降落") + ")";
        // 与 [14] 舱外起飞的 `o != FAILED` 对称: 超时只说明「应答未到」, 不代表命令没生效——
        // 起飞侧据此置位 tookOff, 降落侧同样据此给出观察指引并置位 recoveryDispatched。
        // 若沿用 callItem(仅 SUCCESS 为真), 超时会把整条指引连同「已下发」的事实一起吞掉。
        Outcome o = callOutcome(label, cb -> groundFlightModule.groundLanding(cmd, cb), actionTimeout);
        if (o == Outcome.FAILED) {
            return;
        }
        if (landing) {
            recoveryDispatched = true;
            hint((o == Outcome.TIMEOUT ? "降落指令应答未到, 但可能已在设备侧生效; " : "降落指令已下发, ")
                    + "降落过程可经 [16] 舱外起降状态监听观察(舱盖/在舱/推杆/降落任务状态全链路),"
                    + "或 [1] 消息监听的「降落任务状态」推送(DRC 北向推送, 须先经 [4] 建链才有数据; 均只落日志文件)");
        } else {
            // 取消降落 = 撤销收回动作, 飞机转为悬停: 必须清位, 否则退出提醒会继续用「已下发收回指令」
            // 的缓和措辞, 给出与实际相反的安全暗示。
            recoveryDispatched = false;
        }
    }

    // ==================== [11] 设置飞行速度 ====================

    /**
     * DRC 实时设置飞行速度，取自 {@code flight_control.flying_max_speed}(m/s)。
     *
     * <p>常用档位：低速 5 / 默认 15 / 高速 21——但协议接受连续值，SDK 与 demo 都不做范围校验,
     * 超限由设备侧以业务错误码拒绝。
     *
     * <p>与 HTTP 侧的 {@code speed_mode}(是否开启高速模式)、{@code max_speed}(任务速度上限)
     * <b>没有关系</b>：本指令是不依附任何任务的实时指令。
     *
     * <p>需先建立 DRC 链路([4])——未建链时 SDK 在下发环节即返回链路错误。
     */
    private void setFlyingMaxSpeed(FlightControlModule fc, long actionTimeout) {
        if (!linked) {
            hint("DRC 链路尚未建立, 建议先执行菜单 [4]; 本指令经 DRC 实时通道下发");
        }
        final double speed = params.node("flight_control").dbl("flying_max_speed", 15);
        console("\n   飞行速度: " + speed + " m/s (档位参考: 低速 5 / 默认 15 / 高速 21)");
        if (!confirmDangerous("设置飞行速度为 " + speed + " m/s(将改变飞机实际飞行速度!)")) {
            return;
        }
        callItem("设置飞行速度", cb -> fc.drcSetFlyingMaxSpeed(speed, cb), actionTimeout);
    }

    // ==================== [12]/[13] 指点飞行 ====================

    /**
     * 指点飞行·启动：参数取自 {@code fly_to} 配置节(与 {@code FlyToScenario} 同源),
     * 二次确认后下发; 只判断<b>指令下发</b>是否成功, 不阻塞等待飞机抵达目标点。
     *
     * <p>成功时把服务端返回的 {@code fly_to_id} 记入 {@link #flyToId} 供 [13] 更新目标点使用——
     * 服务端按该 ID 定位任务, 拿不到就没法更新。
     *
     * <p>坐标为 (0,0) 时提前拦截: 服务端安全校验会拒绝(飞机不会飞向赤道), 与其发出去再看错误码,
     * 不如在本地给出可读提示。这与 {@code fly_to} 配置节 _comment 的告警是同一件事。
     *
     * <p>超时按「任务可能已创建」处理并置位 {@link #flyToStarted}: 应答迟到被忽略不代表未下发,
     * 退出收尾据此提醒操作者飞机可能仍在飞。
     */
    private void flyToStart(Drone drone, long actionTimeout) {
        final FlyToPointModule flyTo = drone.getFlyToPointModule();
        if (flyTo == null) {
            hint("获取 FlyToPointModule 失败, 无法执行指点飞行");
            return;
        }

        Node cfg = params.node("fly_to");
        List<FlyToTargetPoint> targets = readPoints(cfg, 50);
        if (!checkTargets("fly_to", targets)) {
            return;
        }

        // start_point 与目标点是两个不同坐标, 不能互填; 默认取飞机实时位置(PB 中为 required)。
        final FlyToTargetPoint startPoint = resolveStartPoint(drone, cfg, actionTimeout);
        if (startPoint == null) {
            return;
        }

        final FlyToPointParams p = new FlyToPointParams();
        p.setStartPoint(startPoint);
        p.setPoints(targets);
        p.setMaxSpeed(cfg.intVal("max_speed", 10));
        // 可选字段: UNKNOWN / -1 表示不下发, 设备保持当前设置。
        p.setSpeedMode(SpeedMode.fromValue(cfg.intVal("speed_mode", -1)));
        p.setRealHeightCapEnabled(cfg.intVal("real_height_cap_enabled", -1));
        p.setRealHeightCapLimit(cfg.intVal("real_height_cap_limit", -1));

        // 参数回显, 确认前让操作者看清起点与目标点。
        console("\n   指点飞行参数(fly_to 配置节, 请核对目标点!):");
        console("     起点(飞机实时位置): lat=" + startPoint.getLatitude()
                + " lon=" + startPoint.getLongitude() + " height=" + startPoint.getHeight() + "m");
        console("     目标点 " + targets.size() + " 个, 最大速度=" + p.getMaxSpeed() + "m/s:");
        for (int i = 0; i < targets.size(); i++) {
            FlyToTargetPoint t = targets.get(i);
            console("       [" + i + "] lat=" + t.getLatitude() + " lon=" + t.getLongitude()
                    + " height=" + t.getHeight() + "m");
        }
        console("     speed_mode=" + p.getSpeedMode()
                + " real_height_cap_enabled=" + p.getRealHeightCapEnabled()
                + " real_height_cap_limit=" + p.getRealHeightCapLimit()
                + " (UNKNOWN/-1=不下发)");
        DemoLog.info(name(), "指点飞行参数(FlyToPointParams): " + p);

        if (!confirmDangerous("指点飞行(飞机将真实飞向目标点!)")) {
            return;
        }

        // 使用 startWithResult 同时拿到 fly_to_id 与 flight_id。
        final Await<FlightHubError> await = new Await<FlightHubError>("指点飞行启动");
        final AtomicReference<FlyToPointStartResult> idRef = new AtomicReference<FlyToPointStartResult>();
        Outcome o = runOutcome("指点飞行启动", actionTimeout, await, () ->
                flyTo.startWithResult(p, new FlightHubCallback.FlyToPointStartResultCallback() {
                    @Override
                    public void onResult(FlightHubError error, FlyToPointStartResult result) {
                        idRef.set(result);
                        await.set(error);
                    }
                }));
        if (o != Outcome.FAILED) {
            flyToStarted = true;
            FlyToPointStartResult result = idRef.get();
            String id = result != null ? result.getFlyToId() : null;
            if (id != null && !id.isEmpty()) {
                flyToId = id;
                console("     flight_id=" + (result != null ? result.getFlightId() : ""));
                hint("指点飞行已下发: fly_to_id=" + id + " (可用 [13] 更新目标点; 需中止用 [8] 急停)");
            } else {
                // 超时时应答未到, 自然没有 ID; 明确说清 [13] 不可用, 免得操作者反复试。
                hint("指点飞行已下发, 但未取到 fly_to_id"
                        + (o == Outcome.TIMEOUT ? "(应答超时)" : "") + "; [13] 更新目标点不可用");
            }
        }
    }

    /**
     * 指点飞行·更新目标点：在<b>执行中</b>的指点飞行任务上换目标点, 参数取自 {@code fly_to_update} 配置节。
     * 依赖 [12] 记下的 {@link #flyToId}。
     *
     * <p>{@code startPoint}(当前机位)<b>刻意不设置</b>: 转换层对 null 直接跳过, 由服务端按实时位置处理。
     * 与 {@code FlyToScenario} 保持一致——菜单式 demo 同样无从获知精确实时机位, 交给服务端更稳妥。
     */
    private void flyToUpdate(Drone drone, long actionTimeout) {
        final FlyToPointModule flyTo = drone.getFlyToPointModule();
        if (flyTo == null) {
            hint("获取 FlyToPointModule 失败, 无法更新目标点");
            return;
        }
        if (flyToId == null || flyToId.isEmpty()) {
            hint("尚无 fly_to_id, 请先执行菜单 [12] 启动指点飞行(更新目标点须指定要改的任务)");
            return;
        }

        Node upd = params.node("fly_to_update");
        List<FlyToTargetPoint> targets = readPoints(upd, 60);
        if (!checkTargets("fly_to_update", targets)) {
            return;
        }

        final FlyToUpdateTargetPointsParams up = new FlyToUpdateTargetPointsParams();
        up.setFlyToId(flyToId);
        up.setMaxSpeed(upd.intVal("max_speed", 8));
        up.setSpeedMode(SpeedMode.fromValue(upd.intVal("speed_mode", -1)));
        up.setRealHeightCapEnabled(upd.intVal("real_height_cap_enabled", -1));
        up.setRealHeightCapLimit(upd.intVal("real_height_cap_limit", -1));
        up.setNeedVerifyFlightControl(upd.intVal("need_verify_flight_control", -1));

        up.setPoints(targets);
        // startPoint 必须带: 服务端对 UpdateFlyToPointRequest.StartPoint 同样做 required 校验,
        // 不传会以 200614 "failed on the 'required' tag" 拒绝。默认取飞机实时位置。
        final FlyToTargetPoint updStart = resolveStartPoint(drone, upd, actionTimeout);
        if (updStart == null) {
            return;
        }
        up.setStartPoint(updStart);

        console("\n   更新目标点参数(fly_to_update 配置节, 请核对新目标点!):");
        console("     fly_to_id=" + flyToId);
        console("     新目标点 " + targets.size() + " 个, 最大速度=" + up.getMaxSpeed() + "m/s:");
        for (int i = 0; i < targets.size(); i++) {
            FlyToTargetPoint t = targets.get(i);
            console("       [" + i + "] lat=" + t.getLatitude() + " lon=" + t.getLongitude()
                    + " height=" + t.getHeight() + "m");
        }
        console("     speed_mode=" + up.getSpeedMode()
                + " real_height_cap_enabled=" + up.getRealHeightCapEnabled()
                + " real_height_cap_limit=" + up.getRealHeightCapLimit()
                + " need_verify_flight_control=" + up.getNeedVerifyFlightControl()
                + " (UNKNOWN/-1=不下发)");
        console("     起点: lat=" + updStart.getLatitude() + " lon=" + updStart.getLongitude()
                + " height=" + updStart.getHeight() + "m");
        DemoLog.info(name(), "更新目标点参数(FlyToUpdateTargetPointsParams): " + up);

        if (!confirmDangerous("更新指点飞行目标点(飞机将改向新目标点!)")) {
            return;
        }

        callItem("更新指点飞行目标点", cb -> flyTo.updateTargetPoints(up, cb), actionTimeout);
    }

    /** 从配置节读取目标点数组({@code points})。空数组交由 {@link #checkTargets} 判定为配置错误。 */
    private List<FlyToTargetPoint> readPoints(Node cfg, double defHeight) {
        List<FlyToTargetPoint> out = new ArrayList<FlyToTargetPoint>();
        for (Object o : cfg.list("points")) {
            Node n = new Node(o);
            FlyToTargetPoint pt = new FlyToTargetPoint();
            pt.setLatitude(n.dbl("latitude", 0));
            pt.setLongitude(n.dbl("longitude", 0));
            pt.setHeight(n.dbl("height", defHeight));
            out.add(pt);
        }
        return out;
    }

    /** 目标点合法性: 非空, 且首点不是占位的 (0,0)——(0,0) 会被服务端安全校验拒绝。 */
    private boolean checkTargets(String section, List<FlyToTargetPoint> targets) {
        if (targets.isEmpty()) {
            hint(section + ".points 为空, 请在 demo_params.json 填至少 1 个目标点");
            return false;
        }
        FlyToTargetPoint first = targets.get(0);
        if (first.getLatitude() == 0 && first.getLongitude() == 0) {
            hint(section + ".points[0] 仍为 (0,0) 占位值, 请填真实测试场地坐标"
                    + "((0,0) 会被服务端安全校验拒绝)");
            return false;
        }
        return true;
    }

    /**
     * 解析航段起点：配置里 {@code start_point} 三项均为 0 → 订阅一帧实时机位(默认、推荐);
     * 否则用配置值(仅联调场景)。
     *
     * <p>静态配置的起点在飞机移动后立刻失效, 所以默认走实时机位; 用配置值时会明确告警。
     */
    private FlyToTargetPoint resolveStartPoint(Drone drone, Node cfg, long timeoutMs) {
        Node sp = cfg.child("start_point");
        double lat = sp.dbl("latitude", 0);
        double lon = sp.dbl("longitude", 0);
        double h = sp.dbl("height", 0);
        if (lat != 0 || lon != 0 || h != 0) {
            FlyToTargetPoint p = new FlyToTargetPoint();
            p.setLatitude(lat);
            p.setLongitude(lon);
            p.setHeight(h);
            hint("start_point 取自配置(非实时机位): lat=" + lat + " lon=" + lon + " height=" + h
                    + " — 飞机已移动时该值会失效, 联调场景外建议三项留 0 走实时机位");
            return p;
        }
        return captureStartPoint(drone, timeoutMs);
    }

    /**
     * 抓取一次飞机实时位置, 作为指点飞行的 {@code start_point}(航段起点)。
     *
     * <p>PB 中 {@code start_point} 是 <b>required</b> 且语义为「飞机当前位置」——与目标点
     * 是两个不同坐标, 不能拿配置里的目标点顶替(填成同一个点会让服务端把航段起点误判为目标点)。
     * SDK 没有拉取式位置接口, 只能临时订阅 {@code listenAircraftLocation}, 收到一帧后立刻退订。
     *
     * <p>{@code height} 取 {@code getHeight()}(WGS84 椭球高), 与目标点 height 同一基准;
     * {@code getElevation()} 是相对起飞点高度, 基准不同, 混用会让航段起点的高度整体偏移。
     *
     * @return 抓到的位置; 模块缺失/超时返回 null, 调用方据此中止——起点坐标不能猜。
     */
    private FlyToTargetPoint captureStartPoint(Drone drone, long timeoutMs) {
        final DroneOsdModule osd = drone.getDroneOsdModule();
        if (osd == null) {
            hint("获取 DroneOsdModule 失败, 无法取得飞机实时位置(指点飞行需要它作为航段起点)");
            return null;
        }

        final Await<AircraftLocation> await = new Await<AircraftLocation>("AircraftLocation");
        final int listenId;
        try {
            listenId = osd.listenAircraftLocation(new FlightHubCallback.AircraftLocationCallback() {
                @Override
                public void onResult(AircraftLocation loc) {
                    // 空帧(本帧无数据)直接忽略, 继续等下一帧, 避免把 0,0 当成机位。
                    if (loc != null) {
                        await.set(loc);
                    }
                }
            });
        } catch (Exception e) {
            hint("订阅飞机位置失败: " + Utils.describe(e));
            return null;
        }

        try {
            AircraftLocation loc = await.await(timeoutMs);
            FlyToTargetPoint sp = new FlyToTargetPoint();
            sp.setLatitude(loc.getLatitude());
            sp.setLongitude(loc.getLongitude());
            sp.setHeight(loc.getHeight());
            DemoLog.info(name(), "取得航段起点(飞机实时位置): lat=" + sp.getLatitude()
                    + " lon=" + sp.getLongitude() + " height=" + sp.getHeight());
            return sp;
        } catch (FlightHubException e) {
            hint("等待飞机位置推送超时/失败: " + Utils.describe(e)
                    + " — 请确认飞机在线且 OSD 正常, 或先用 [1] 开启监听确认有推送");
            return null;
        } finally {
            try {
                osd.cancelListenAircraftLocation(listenId);
            } catch (Exception e) {
                DemoLog.warn(name(), "取消飞机位置订阅失败(忽略): " + Utils.describe(e));
            }
        }
    }

    // ==================== [7] WASD 摇杆 ====================

    /**
     * WASD 摇杆控制: 按 {@code flight_control} 配置节的各轴值以固定 20Hz 连发
     * {@code sendStickControl}(fire-and-forget, 无回调; 走 DRC 实时通道)。
     *
     * <p>各轴中性 1024、SDK 建议操控范围 364–1684, 越界配置会被夹取; 各轴取值为配置驱动
     * (默认全中性, 需要动作请改配置)。须先建 DRC 链路并持有飞行控制权。
     *
     * <p><b>安全加固</b>:
     * <ul>
     *   <li>连发时长夹取到 {@value #STICK_DURATION_MAX_MS}ms 上限 (
     *       配置驱动后若不设上限, 误多敲一个 0 会把不可控窗口放大到分钟级); </li>
     *   <li>连发期间<b>按 Enter 可随时中止</b>(每帧非阻塞探测共享 reader; 保证急停在唯一产生真实运动的
     *       动作期间仍然可达); </li>
     *   <li>结束/中止后<b>补发全中性「松杆」收尾帧</b>——SDK javadoc 明确「松杆建议连续发送若干帧全 1024」
     *       且 SDK 纯透传无自动回中; 不补发则飞机会按最后一帧 (可能满杆)杆量继续飞到机端保护介入。</li>
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
            // 与 clampStick 同款「告警+夹取」: 时长误配 (如多敲一个 0)不该把不可控窗口放大到分钟级。
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
        console("     连发: 20Hz × " + durationMs + "ms = " + total + " 次(期间按 Enter 可随时中止)");
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
                // 每秒回显一次进度: 帧数由间隔推导, 改连发频率时进度节奏自动跟随 (写死帧数会随频率漂移)。
                if (sent % STICK_PROGRESS_EVERY_FRAMES == 0) {
                    console("   进度: " + sent + "/" + total);
                }
                // 非阻塞探测共享 reader: 有任何待读输入即消费该行并中止连发——
                // 让急停等菜单动作在连发期间重新可达 (消费防止残留输入串进下一次菜单读取)。
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
            // 松杆收尾 (所有退出路径必经): SDK javadoc「松杆建议连续发送若干帧全 1024」且 SDK 纯透传
            // 无自动回中——不补发中性帧, 飞机会按最后一帧 (可能满杆)杆量继续飞到机端保护介入,
            // 实际移动窗口超出操作者确认的时长。全中性配置无需收尾 (每一帧本就是中性)。
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

    /** 补发全中性「松杆」收尾帧 (连发结束/中止后调用; 任何异常只记录, 不影响调用方流程)。 */
    private void sendNeutralTail(FlightControlModule fc) {
        try {
            StickControlData neutral = new StickControlData();
            neutral.setRoll(STICK_NEUTRAL);
            neutral.setPitch(STICK_NEUTRAL);
            neutral.setThrottle(STICK_NEUTRAL);
            neutral.setYaw(STICK_NEUTRAL);
            neutral.setGimbalPitch(STICK_NEUTRAL);
            // 主循环的中断路径会先恢复中断标志再进本方法, 若不先清掉, 第 1 帧后的 sleep 会立即抛
            // InterruptedException 把收尾截断成 1 帧。先清标志保证安全收尾完整 (至多推迟响应中断
            // STICK_NEUTRAL_TAIL_FRAMES×50ms=500ms), 收尾后如数恢复。
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

    /** 夹取摇杆轴值到 SDK 建议范围 [364, 1684], 越界告警并夹取。 */
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

    // ==================== [1] 消息监听 (飞行控制相关) ====================

    /**
     * 建立飞行控制相关的全部消息监听 (急停/摇杆可用性监听已随 SDK 删除, 只留提示)。
     *
     * <p>各路监听独立建立: 单路失败只告警并继续其余路。全部订阅登记进 {@link #monitorSubs}
     * (模块退出前统一取消), 并同步登记进会话 {@code registry()} 兜底 (close 幂等, 双重取消无害)。
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

        // 急停可用 / 摇杆可用: SDK 已删除全部返回 boolean 的 listen*Available 门控接口
        // (能否执行由服务端判定), 无对应 API 可订阅——见类 javadoc。
        hint("急停/摇杆可用性监听已随 SDK Available 门控接口移除, 无法订阅; 以 [7]/[8] 执行结果为准");

        // DRC OSD / 电池遥测 (DRC 链路已连时才有推送)。
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

        // 降落任务状态(降落/回舱过程推送; DRC 链路已连时才有数据)。
        // landing_state: 0=初始空闲 1=等待参数同步 2=航向对齐 3=精准降落(有水平源)
        //                4=无源降落(无水平源, 继续下降) 5=悬停微调(有水平源) 6=纯悬停(无水平源)
        //                7=备降(有源) 8=备降(无源) 9=备降(用户 RC 接管)
        // 注: getCover_percent() 在协议侧拼作 cover_precent(协议设计如此), SDK 对外已更正。
        addMonitor("drcLandingMissionState", () -> Subscription.of("drcLandingMissionState",
                fc.listenDrcLandingMissionState(new FlightHubCallback.DrcLandingMissionStateCallback() {
                    @Override
                    public void onResult(DrcLandingMissionState info) {
                        if (info == null) {
                            return;
                        }
                        pushLog("listenDrcLandingMissionState() #DrcLandingMissionState, DRC 降落任务状态: state="
                                + info.getLanding_state() + "(" + describeLandingState(info.getLanding_state()) + ")"
                                + " pauseReason=" + info.getPause_reason()
                                + " coverPercent=" + info.getCover_percent() + "%"
                                + " coverErrCode=" + info.getCover_error_code());
                    }
                }),
                fc::cancelListenDrcLandingMissionState));

        console("   消息监听建立完成: 共 " + monitorSubs.size()
                + " 路(推送见日志文件; 再次选 [1] 关闭; 退出本模块时自动取消)");
        DemoLog.info(name(), "消息监听合集建立完成: " + monitorSubs.size() + " 路");
    }

    /**
     * 取消 [1] 建立的全部监听 (开/关切换的「关」半边; {@link Subscription#close()} 幂等且吞异常)。
     *
     * <p>关闭 sub 的同时从会话 registry 移除, 避免 registry 长期持有已关闭 sub 引用 (长会话下反复
     * 开/关监听会累积冗余条目)。registry 作为兜底仍有效: teardown() 的 finally 块会再次遍历
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

    /** 建立单路监听并登记 (monitorSubs + 会话 registry 双登记); 失败只告警不中断其余路。 */
    private void addMonitor(String label, Callable<Subscription> factory) {
        try {
            Subscription sub = factory.call();
            monitorSubs.add(sub);
            // 兜底: 同步登记进会话 registry——即使 teardown 被原生 Error 中断, 基类关闭会话时仍会退订。
            registry().add(sub);
            console("   [监听] " + label + " 已建立");
        } catch (Throwable e) {
            // catch Throwable 而非 Exception: SDK 经 JNI, listenXxx 可能抛原生 Error
            // (如 UnsatisfiedLinkError / SWIG OutOfMemoryError); 只 catch Exception 会让 Error
            // 逃逸、中断 startMonitors、跳过剩余路由——违反「单路失败只告警并继续其余路」的契约。
            console("   [监听] " + label + " 建立失败: " + Utils.describe(e));
            DemoLog.warn(name(), "监听建立失败: " + label + " -> " + Utils.describe(e));
        }
    }

    // ==================== 退出收尾 ====================

    /**
     * 退出前尽力而为收尾: 断 DRC(若已连) → 释放飞行控制权 (若已占) → 取消全部监听订阅。
     *

     * <p><b>不自动返航/降落</b>：若执行过起飞, 仅告警提醒——demo 静默触发返航比把决定留给操作者更危险。
     * 提醒措辞按 {@link #recoveryDispatched} 分两档：未下发过收回指令时给「可能仍在空中」的强告警;
     * 下发过时改为「已下发但需确认落地」——<b>两档都会告警</b>, 不存在「已安全」这一档,
     * 因为本类拿不到「飞机已落地」的确证(指令下发成功只代表服务端收下了指令)。
     * 本方法在 {@link #run} 的 {@code finally} 中调用：动作步各自经 {@link #quietStep} 兜
     * {@code Throwable}(SDK 为 JNI, 原生 Error 不得中断后续收尾), 订阅取消放在内层 {@code finally}。
     */
    private Outcome showControlAuthResult(String label, boolean acquire, ControlAuthModule auth, long timeout) {
        final Await<FlightHubError> await = new Await<>(name() + "." + label);
        final AtomicReference<com.dji.flighthubsdk.bean.ControlAuthResult> ref = new AtomicReference<>();
        Outcome o = runOutcome(label, timeout, await, () -> {
            FlightHubCallback.ControlAuthResultCallback cb = new FlightHubCallback.ControlAuthResultCallback() {
                @Override
                public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.ControlAuthResult result) {
                    ref.set(result);
                    await.set(error);
                }
            };
            if (acquire) {
                auth.acquireFlightControlAuthorityWithResult(cb);
            } else {
                auth.releaseFlightControlAuthorityWithResult(cb);
            }
        });
        if (o == Outcome.SUCCESS) {
            com.dji.flighthubsdk.bean.ControlAuthResult r = ref.get();
            if (r != null) {
                console("     droneSn=" + r.getDroneSn()
                        + " bid=" + r.getBid()
                        + " controls=" + r.getControls().size());
                for (com.dji.flighthubsdk.bean.ControlAuthController c : r.getControls()) {
                    console("       control key=" + c.getKey()
                            + " type=" + c.getType()
                            + " version=" + c.getVersion()
                            + " gatewaySn=" + (c.getGateway() == null ? "" : c.getGateway().getSn())
                            + " userId=" + (c.getUser() == null ? "" : c.getUser().getUserId())
                            + " isLocked=" + (c.getSpec() != null && c.getSpec().isLocked())
                            + " payloadIndex=" + (c.getSpec() == null ? "" : c.getSpec().getPayloadIndex())
                            + " waitingUser=" + (c.getSpec() == null ? "" : c.getSpec().getWaitingForAuthUser()));
                }
            }
        }
        return o;
    }

    private void teardown(ControlAuthModule auth, DrcModule drc, GroundFlightMonitor groundMonitor,
            long actionTimeout) {
        if (tookOff) {
            console("\n   警告: 本次会话执行过起飞([6] 一键起飞 / [14] 舱外起飞)!");
            if (recoveryDispatched) {
                // 下发过收回指令: 措辞放缓, 但绝不宣布「已安全」——指令下发成功 != 飞机已落地。
                console("   已下发过收回指令([9] 返航 / [15] 舱外降落至机场), 但<指令下发成功>不等于<已落地>:");
                console("   请经 [16] 舱外起降状态监听(舱外降落场景)、[1] 消息监听的 DRC OSD、OSD 或现场确认飞机确已降落;");
                console("   未确认落地前请勿离开现场, 必要时重新进入本模块处置。");
                DemoLog.warn(name(), "退出时提醒: 执行过起飞, 已下发收回指令但未确认落地");
            } else {
                console("   飞机可能仍在空中! 本 demo 不会自动返航/降落,");
                console("   需要收回请重新进入本模块用 [9] 返航或 [15] 舱外降落, 或经其他手段处置。");
                DemoLog.warn(name(), "退出时提醒: 执行过起飞, 未下发任何收回指令");
            }
        }

        if (flyToStarted) {
            console("\n   警告: 本次会话下发过指点飞行, 飞机可能仍在飞向目标点!");
            console("   本 demo 不会自动停止, 需要中止请重新进入本模块用 [8] 急停, 或经其他手段处置。");
            DemoLog.warn(name(), "退出时提醒: 下发过指点飞行, 未自动停止");
        }
        if (linked || gotAuth || !monitorSubs.isEmpty() || groundMonitor.isRunning()) {
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
            // 取消 [1] 建立的全部监听 (Subscription.close 幂等、内部吞 Throwable)。
            // 同步移出 registry, 与 stopMonitors() 范式一致, 避免已关闭引用在长会话中累积。
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
            // [16] 的订阅由 GroundFlightMonitor 自行收尾(close 幂等, 未开启时静默返回);
            // quietStep 兜原生 Error, 保证与上面 [1] 的收尾互不牵连。
            quietStep("舱外起降状态监听退订(收尾)", groundMonitor::close);
        }
    }

    // ---- 控制台结果回显 (范式同 PayloadControlDemo: 三态结果; 监听推送只落日志)----
    // DemoLog 只落滚动日志文件, 交互终端看不到调用结果; 以下把每个测试项的
    // 「开始 / 成功 / 超时 / 失败 (附错误码与消息)」实时回显到 stdout。
    // Outcome 枚举 / callOutcome / syncOutcome / runOutcome / console / hint / pushLog /
    // quietStep / confirmDangerous 均由基类 ModuleDemo 统一提供。


    /** 降落任务状态枚举描述(见 DrcLandingMissionState::landing_state 注释)。 */
    private static String describeLandingState(int state) {
        switch (state) {
            case 0:  return "初始空闲";
            case 1:  return "等待参数同步";
            case 2:  return "航向对齐";
            case 3:  return "精准降落(有水平源)";
            case 4:  return "无源降落(无水平源, 继续下降)";
            case 5:  return "悬停微调(有水平源)";
            case 6:  return "纯悬停(无水平源)";
            case 7:  return "备降(有源)";
            case 8:  return "备降(无源)";
            case 9:  return "备降(用户 RC 接管)";
            default: return "未知";
        }
    }
}
