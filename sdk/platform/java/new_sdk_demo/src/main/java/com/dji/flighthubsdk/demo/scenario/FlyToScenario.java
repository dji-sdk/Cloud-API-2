// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.scenario;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.CommandTaskData;
import com.dji.flighthubsdk.bean.CommandTaskEvent;
import com.dji.flighthubsdk.bean.DroneOnlineStatus;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.AircraftLocation;
import com.dji.flighthubsdk.bean.FlyToPointParams;
import com.dji.flighthubsdk.value.SpeedMode;
import com.dji.flighthubsdk.bean.FlyToTargetPoint;
import com.dji.flighthubsdk.bean.FlyToTaskData;
import com.dji.flighthubsdk.bean.FlyToTaskEvent;
import com.dji.flighthubsdk.bean.FlyToUpdateTargetPointsParams;
import com.dji.flighthubsdk.bean.LinkStatus;
import com.dji.flighthubsdk.core.ControlAuthModule;
import com.dji.flighthubsdk.core.DeviceStateModule;
import com.dji.flighthubsdk.core.DrcModule;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.DroneOsdModule;
import com.dji.flighthubsdk.core.FlyToPointModule;
import com.dji.flighthubsdk.core.TakeoffModule;
import com.dji.flighthubsdk.demo.base.Precondition;
import com.dji.flighthubsdk.demo.base.Scenario;
import com.dji.flighthubsdk.demo.runtime.Await;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.runtime.FlightHubException;
import com.dji.flighthubsdk.demo.runtime.Node;
import com.dji.flighthubsdk.demo.runtime.Subscription;
import com.dji.flighthubsdk.demo.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 场景: 指点飞行 (FlyTo)完整流程——DRC/控制权 前置 → 监听 → 启动指点飞行 →
 * 更新目标点 → 停止 → 完备收尾。
 *
 * <p><b>前置要求: 飞机须已在空中</b>。本场景<b>不含一键起飞、也不返航</b>——指点飞行的对象是
 * 空中飞行器, 请先经 {@code FlightControlDemo [6] 一键起飞}(或其他手段)把飞机送上天再运行本场景;
 * 场景结束后飞机仍在空中, 需自行经 {@code FlightControlDemo [9] 返航} 或其他手段收回。
 *
 * <p><b>SDK-P1-12 · 坐标合法性校验属于业务应用层职责</b>:
 * FHSDK / native 层<b>不做</b>飞行目标点的业务合法性校验 (是否在合法飞行区、是否为占位坐标、
 * 是否越境、是否偏离起飞点过远等), 仅做「参数格式正确」的基础校验。ISV 应用集成 SDK 时,
 * <b>必须在应用层自行实现</b>坐标校验 (如禁飞区查询、坐标可信性判断)。本 Demo 只做最低限度的
 * 「坐标不能为 (0, 0) 占位默认值」示例校验 (见 {@link #steps()} 中「启动指点飞行」与「更新目标点」两步的
 * 坐标合法性检查), 供参考而非兜底。
 *
 * <p>该场景演示了指点飞行 (FlyTo)的完整流程: 建立 DRC 链路 → 获取飞行控制权与负载控制权
 * (负载控制权失败自动重试, 重试耗尽后跳过) → 建立基础监听 → 订阅任务事件 → 启动指点飞行
 * → 更新目标点 → 观察 → 停止指点飞行。步骤:
 * <ol>
 *   <li>建立 DRC 链路——<b>「停止指点飞行」只有 {@code FlyToPointModule.drcStop} 一个入口, 它走 DRC 通道
 *       (DRC method: {@code drone_emergency_stop})</b>, 建链是停止的硬前置, 必须先于 start;
 *       等待结果, 双写日志 (控制台+文件), 失败即中止场景; </li>
 *   <li>获取飞行控制权 (指点飞行的前置); 等待结果, 双写日志, 失败即中止场景; </li>
 *   <li>获取负载控制权 (相机/云台类操控的前置, 演示基础功能编排); 等待结果, 双写日志;
 *       失败后最多共尝试 3 次 (首次 + 重试 2 次)、重试间隔 10s, 每轮打印轮次与结果 (双写日志);
 *       尝试耗尽后<b>跳过、继续后续流程</b>(本场景后续步骤不依赖负载控制权, 仅作获取/释放的编排示范); </li>
 *   <li>基础监听: 飞机在线状态、DRC 链路状态、飞行/负载控制权状态、指令任务事件
 *       (指令任务事件即 {@code listenCommandTaskEvent}——本场景<b>不下发</b>指令任务, 订阅它仅作
 *       观察: 若飞机正在执行其它指令任务, 其状态变化会落到日志文件, 便于排查; 属纯观察能力,
 *       {@code TakeoffModule} 不可用时仅告警跳过、不中止场景;
 *       步骤提示双写日志, 推送数据仅落日志文件); </li>
 *   <li>订阅指点飞行任务事件 {@code listenFlyToTaskEvent}(见下方「监听接口选型」; 步骤提示
 *       双写日志, 推送数据仅落日志文件); </li>
 *   <li>启动指点飞行 (目标点取自 {@code fly_to} 配置节), 等待首个任务事件
 *       (超时 {@code fly_to_timeout_ms}); </li>
 *   <li>更新目标点 (取自 {@code fly_to_update} 配置节; startPoint 不设, 由服务端按当前机位处理); </li>
 *   <li>观察一段任务事件推送 ({@code fly_to.observe_ms}; <b>等的是终态信号而非死睡</b>——任务在
 *       观察窗口内结束就立即醒来进入下一步, 不空等剩余时间; 推送数据仅落日志文件); </li>
 *   <li>停止指点飞行 (任务已到终态则跳过)。</li>
 * </ol>
 *
 * <p><b>为何调整顺序 (实测发现)</b>: {@code acquirePayloadControlAuthority} /
 * {@code listenPayloadControlAuthority} 可能因负载 (相机)OSD 信息尚未推送到云端而报
 * "payload_index: xx-x-x not found"——这是设备刚上线/建链时的时序竞态, 并非硬件或接口错误。
 * 调整为先建立 DRC 链路、获取飞行控制权 (两者更可靠), 再尝试获取负载控制权并配合重试, 为 OSD
 * 信息的到达争取时间; 若重试耗尽仍失败, 场景选择跳过负载控制权、继续后续飞行流程, 而不是
 * 中止整个场景。
 *
 * <p><b>监听接口选型</b>: {@code subscribeFlyToDroneSelfPlanPath}(规划路径)与
 * {@code subscribeFlyToPointProgress}(进度)已被 SDK 标记 {@code @Deprecated}——其消息 topic
 * 已被 {@code topic_atc_flyto_task_event} 取代, 老 topic 不再有数据。本场景按 SDK 指引改用
 * {@code listenFlyToTaskEvent} 一个订阅同时覆盖两者: 路径见
 * {@link FlyToTaskData#getPlannedPathPoints()}, 进度见 {@code status/wayPointIndex/
 * remainingDistance/remainingTime}。任务状态取值 (源自 message.h FlyToTaskData 注释):
 * 0=待执行 1=执行中 2=成功 3=失败 4=取消 5=超时 (≥2 为终态, 统一经 {@link #isTerminal} 判定;
 * 尚未收到本任务事件时内部记为哨兵 {@link #STATUS_UNKNOWN}, 显示为「尚无事件」且不算终态)。
 *
 * <p><b>收尾保证 (finally, 每步各自兜异常不互相阻断)</b>:
 * <ol>
 *   <li>若指点飞行已下发且未确认停止、任务未到终态——补一次停止 (含 start 回调超时但任务实际已在
 *       服务端创建的竞态: 下发即置 {@code startAttempted}, 不依赖回调是否按时返回; 服务端以明确业务码
 *       拒绝 (serverCode!=0)时任务确定未创建, 复位该标志, 不做无谓补停); </li>
 *   <li>断开 DRC 链路 (在停止<b>之后</b>——{@code drcStop} 走 DRC, 顺序不可颠倒); </li>
 *   <li>释放负载控制权、释放飞行控制权 (获取类动作按「下发即可能生效」处理, 超时也照常释放); </li>
 *   <li>显式取消全部监听 (订阅同时登记在会话 registry, 双保险; {@code Subscription.close} 幂等)。</li>
 * </ol>
 *
 * <p>收尾<b>不含返航</b>——本场景不负责起降 (见开头「前置要求」), 飞机在场景结束后仍保持进入本场景
 * 时的飞行状态, 由操作者自行收回。
 *
 * <p>所有参数来自 demo_params.json 的 {@code fly_to}/{@code fly_to_update} 分组, 无手动交互输入。
 *
 * <p><b>已知 SDK 限制 (非本场景 bug)</b>: {@code cancelListenXxx} 会立即销毁原生回调包装器,
 * 与在途事件派发存在极小概率的原生竞态 (与 resetGimbal 回调重复投递属同族系统性问题, 已上报);
 * 本场景按正确顺序退订, demo 侧无法根除。
 */
public class FlyToScenario extends Scenario {

    /** demo 自定义哨兵值: 尚未收到本任务的事件 (<b>非</b> native 取值表中的状态)。 */
    private static final int STATUS_UNKNOWN = -1;
    /** 终态下界 (源自 native message.h FlyToTaskData.status 注释): ≥2 即终态 (2=成功 3=失败 4=取消 5=超时)。 */
    private static final int STATUS_TERMINAL_MIN = 2;

    @Override
    public String name() {
        return "FlyToScenario";
    }

    @Override
    protected void steps() throws Exception {
        // 无飞机拓扑下 session.drone() 直接抛 (见 {@link DeviceSession#drone()}), 会被 runBody 报成
        // 「demo 执行失败」; 设备选错属正常前置, 先判定并用 hint 给出可读措辞。
        if (!session.hasDrone()) {
            hint("该设备没有无人机, 无法运行指点飞行场景");
            return;
        }

        final Drone drone = session.drone();

        // 模块 getter 在 SDK 内部失败时会返回 null, 先集中判空给出诊断。
        // takeoff 刻意不列入 fail-fast: 本场景对它的唯一用途是观察性的 listenCommandTaskEvent
        // (本场景不下发任何指令任务), 缺它不影响指点飞行主流程——纯观察能力不应握有中止整个飞行
        // 场景的权力, 故降级为「有则订阅、无则告警跳过」(见下方 Step 4)。
        final FlyToPointModule flyTo = drone.getFlyToPointModule();
        final ControlAuthModule auth = drone.getControlAuthModule();
        final DrcModule drc = drone.getDrcModule();
        final DeviceStateModule state = drone.getDeviceStateModule();
        final TakeoffModule takeoff = drone.getTakeoffModule();   // optional: null-guarded at use site
        final DroneOsdModule osd = drone.getDroneOsdModule();     // 指点飞行的航段起点(实时机位)来源
        if (flyTo == null || auth == null || drc == null || state == null) {
            throw new FlightHubException("获取模块失败: flyTo=" + (flyTo != null) + " auth=" + (auth != null)
                + " drc=" + (drc != null) + " state=" + (state != null));
        }

        final String payloadIndex = payloadIndex();
        final long defaultTimeout = defaultTimeout();

        // 收尾判断用状态 (「下发即可能生效」语义: 在动作下发前置位, 不依赖回调按时返回)。
        boolean flightAuthAttempted = false;
        boolean payloadAuthAttempted = false;
        boolean drcConnectAttempted = false;
        boolean startAttempted = false;
        boolean stopConfirmed = false;

        // 任务事件跟踪: 本次任务 id(start 回调返回) + 已观察到的最新任务状态。
        // lastStatus 按「单调递增 + 终态粘滞」更新, 不是「最近一条事件」的原样快照 (见订阅回调内的 CAS)。
        final AtomicReference<String> flyToIdRef = new AtomicReference<String>("");
        final AtomicInteger lastStatus = new AtomicInteger(STATUS_UNKNOWN);
        final List<Subscription> subs = new ArrayList<Subscription>();

        try {
            // ---- Step 1: 建立 DRC 链路 (drcStop 走 DRC 通道, 建链是停止指点飞行的硬前置) ----
            step("建立 DRC 链路");
            drcConnectAttempted = true;
            Outcome drcOutcome = syncOutcome("建立 DRC 链路", drc::connectDrcLink, defaultTimeout);
            if (drcOutcome != Outcome.SUCCESS) {
                throw new FlightHubException("建立 DRC 链路失败: " + drcOutcome);
            }

            // ---- Step 2: 获取飞行控制权 ----
            step("获取飞行控制权");
            flightAuthAttempted = true;
            Outcome flightAuthOutcome = callOutcome("获取飞行控制权", cb -> auth.acquireFlightControlAuthority(cb), defaultTimeout);
            if (flightAuthOutcome != Outcome.SUCCESS) {
                throw new FlightHubException("获取飞行控制权失败: " + flightAuthOutcome);
            }

            // ---- Step 3: 获取负载控制权 (最多共尝试 3 次 = 首次 + 重试 2 次, 重试间隔 10s) ----
            step("获取负载控制权(payloadIndex=" + payloadIndex + ")");
            final int payloadAuthMaxAttempts = 3;
            final long payloadAuthRetryDelayMs = 10_000L;
            payloadAuthAttempted = true;    // 下发即置位 (整组重试只置一次): 收尾据此兜底释放。
            Outcome payloadAuthOutcome = Outcome.FAILED;
            for (int attempt = 1; attempt <= payloadAuthMaxAttempts; attempt++) {
                payloadAuthOutcome = callOutcome(
                    "获取负载控制权(第 " + attempt + "/" + payloadAuthMaxAttempts + " 次尝试)",
                    cb -> auth.acquirePayloadControlAuthority(payloadIndex, cb), defaultTimeout
                );

                if (payloadAuthOutcome == Outcome.SUCCESS) {
                    break;
                }
                if (attempt < payloadAuthMaxAttempts) {
                    String waitMsg = "获取负载控制权失败, " + (payloadAuthRetryDelayMs / 1000) + "s 后重试...";
                    DemoLog.info(name(), waitMsg);
                    console("   " + waitMsg);
                    sleep(payloadAuthRetryDelayMs);
                }
            }

            if (payloadAuthOutcome != Outcome.SUCCESS) {
                // hint 即「日志 + stdout(缩进 3 空格)」双写 (DESIGN 4.7), 与此前手写两遍等价;
                // 收敛后措辞只有一份, 不会两侧漂移 (级别随之由 WARN 归为 INFO, 与 12 处既有前置提示一致)。
                hint("获取负载控制权 " + payloadAuthMaxAttempts + " 次尝试均失败, 跳过负载控制权, 继续后续飞行流程");
            }

            // ---- Step 4: 基础监听 (推送只落日志文件) ----
            step("基础监听：在线状态 / DRC 链路状态 / 飞行控制权 / 负载控制权 / 指令任务事件");
            trySubscribe(subs, "droneOnlineStatus",
                state.listenDroneOnlineStatus(new FlightHubCallback.DroneOnlineStatusCallback() {
                    @Override
                    public void onResult(DroneOnlineStatus s) {
                        DemoLog.info(name(), "[推送] listenDroneOnlineStatus() #DroneOnlineStatus,飞机在线状态: online=" + (s != null && s.isOnline()));
                    }
                }),
                state::cancelListenDroneOnlineStatus);

            trySubscribe(subs, "drcLinkStatus",
                drc.listenLinkStatus(new FlightHubCallback.LinkStatusCallback() {
                    @Override
                    public void onResult(LinkStatus s) {
                        if (s == null) {
                            return;
                        }

                        DemoLog.info(
                            name(), "[推送] listenLinkStatus() #LinkStatus,链路状态: ctrl=" + s.getCtrlLinkStatus()
                            + " data=" + s.getDataLinkStatus() + " live=" + s.getLiveLinkStatus()
                        );
                    }
                }),
                drc::cancelListenLinkStatus);

            trySubscribe(subs, "flightControlAuthority",
                auth.listenFlightControlAuthority(new FlightHubCallback.FlightControlAuthorityCallback() {
                    @Override
                    public void onResult(boolean isGranted) {
                        DemoLog.info(name(), "[推送] listenFlightControlAuthority() #boolean,飞行控制权状态: is_granted=" + isGranted);
                    }
                }),
                auth::cancelListenFlightControlAuthority);

            trySubscribe(subs, "payloadControlAuthority",
                auth.listenPayloadControlAuthority(payloadIndex,
                    new FlightHubCallback.PayloadControlAuthorityCallback() {
                        @Override
                        public void onResult(boolean isGranted) {
                            DemoLog.info(name(), "[推送] listenPayloadControlAuthority() #boolean,负载控制权状态: is_granted=" + isGranted);
                        }
                    }),
                id -> auth.cancelListenPayloadControlAuthority(payloadIndex, id));

            // 指令任务事件: 本场景不下发指令任务 (起飞/返航等), 订阅它仅作观察——飞机若正在执行
            // 其它来源的指令任务, 状态变化会落到日志文件, 便于排查指点飞行被拒的外部原因。
            // CommandTaskData.status: 0=待执行 1=执行中 2=已完成 3=失败 4=已取消 (本场景仅观察, 不做终态判定)
            // 纯观察能力: TakeoffModule 不可用时只告警跳过, 不中止场景 (见上方模块判空处的说明)。
            if (takeoff == null) {
                DemoLog.warn(name(), "TakeoffModule 不可用, 跳过指令任务事件观察(纯观察能力, 不影响指点飞行主流程)");
            } else {
                trySubscribe(subs, "commandTaskEvent",
                    takeoff.listenCommandTaskEvent(new FlightHubCallback.CommandTaskEventCallback() {
                        @Override
                        public void onResult(CommandTaskEvent event) {
                            if (event == null) {
                                return;
                            }
                            CommandTaskData d = event.getTaskData();
                            DemoLog.info(
                                name(), "[推送] listenCommandTaskEvent() #CommandTaskEvent,指令任务事件: eventId=" + event.getEventId()
                                + " action=" + event.getAction()
                                + " uuid=" + event.getUuid()
                                + " timestamp=" + event.getTimestamp()
                            );

                            if (d != null) {
                                DemoLog.debug(name(), "  task_data: id=" + d.getId()
                                    + " uuid=" + d.getUuid()
                                    + " prj_uuid=" + d.getPrjUuid()
                                    + " sn=" + d.getSn()
                                    + " drone_sn=" + d.getDroneSn()
                                    + " track_id=" + d.getTrackId()
                                    + " user_id=" + Utils.maskUserId(d.getUserId())
                                    + " status=" + d.getStatus() + "(" + commandStatusText(d.getStatus()) + ")"
                                    + " err_code=" + d.getErrCode()
                                    + " takeoff_mode=" + d.getTakeoffMode()
                                    + " security_takeoff_height=" + d.getSecurityTakeoffHeight()
                                    + " max_speed=" + d.getMaxSpeed()
                                    + " out_of_control_action=" + d.getOutOfControlAction()
                                    + " rth_altitude=" + d.getRthAltitude()
                                    + " rth_mode=" + d.getRthMode()
                                    + " commander_flight_mode=" + d.getCommanderFlightMode()
                                    + " commander_flight_height=" + d.getCommanderFlightHeight()
                                    + " cloud_to_cloud_id=" + d.getCloudToCloudId()
                                    + " created_at=" + d.getCreatedAt());
                            }
                        }
                    }),
                    takeoff::cancelListenCommandTaskEvent);
            }

            // ---- Step 5: 订阅指点飞行任务事件 (先于 start, 不漏首个事件) ----
            step("订阅指点飞行任务事件(listenFlyToTaskEvent, 替代已废弃的路径/进度订阅)");
            final Await<Boolean> firstEvent = new Await<Boolean>("flyTo.firstEvent");
            // 终态信号: 供 Step 8 的观察期提前结束 (任务已结束就没必要再空睡满 observe_ms)。
            final Await<Boolean> terminalEvent = new Await<Boolean>("flyTo.terminalEvent");
            trySubscribe(subs, "flyToTaskEvent",
                flyTo.listenFlyToTaskEvent(new FlightHubCallback.FlyToTaskEventCallback() {
                    @Override
                    public void onResult(FlyToTaskEvent event) {
                        if (event == null || event.getTaskData() == null) {
                            return;
                        }
                        FlyToTaskData d = event.getTaskData();
                        // plannedPathPoints 判空: 服务端事件省略该字段时 bean 保持 null(无参构造不初始化)。
                        DemoLog.info(name(), "[推送] listenFlyToTaskEvent() #FlyToTaskEvent, FlyTo 任务事件: action=" + event.getAction()
                            + " uuid=" + d.getUuid()
                            + " status=" + d.getStatus() + "(" + statusText(d.getStatus()) + ")"
                            + " wayPointIndex=" + d.getWayPointIndex()
                            + " 剩余距离=" + d.getRemainingDistance() + "m"
                            + " 剩余时间=" + d.getRemainingTime() + "s"
                            + " 规划路径点=" + (d.getPlannedPathPoints() != null
                            ? d.getPlannedPathPoints().size() : 0));
                        // 只跟踪本次启动的任务 (uuid 匹配 flyToId; 事件先于 start 回调返回时 id 尚空, 不误采)。
                        String myId = flyToIdRef.get();
                        if (!myId.isEmpty() && myId.equals(d.getUuid())) {
                            // 单调/终态粘滞更新: 原生 topic_atc_flyto_task_event 存在至少一次投递语义,
                            // 事件可能重复或乱序到达——只接受严格大于当前值的状态 (挡掉重复推送与乱序回退);
                            // 一旦到达终态 (见 isTerminal)即固定不再更新, 终态之后的任何取值都不采纳
                            // (2/3/4/5 互斥且无先后可言, 保留首个到达的终态最能代表任务结局)。
                            int newStatus = d.getStatus();
                            int cur;
                            do {
                                cur = lastStatus.get();
                                if (isTerminal(cur) || newStatus <= cur) {
                                    break;
                                }
                            } while (!lastStatus.compareAndSet(cur, newStatus));
                            firstEvent.set(Boolean.TRUE);
                            // 终态唤醒: 读回 lastStatus 而非直接判 newStatus——本次可能因单调性未被采纳,
                            // 但终态可能是更早一条事件写入的, 以已固定的最终值为准。Await.set 幂等,
                            // 每条终态后事件都会走到这里, 重复调用无副作用。
                            if (isTerminal(lastStatus.get())) {
                                terminalEvent.set(Boolean.TRUE);
                            }
                        }
                    }
                }),
                flyTo::cancelListenFlyToTaskEvent);

            // ---- Step 6: 启动指点飞行 (目标点取自 fly_to 配置节) ----
            Node cfg = params.node("fly_to");
            // 安全校验：points 是目标点数组, 空数组或占位 (0,0) 时 fail-fast——
            // (0,0) 是 demo_params.json 的占位默认值(赤道与本初子午线交点/几内亚湾),
            // 几乎必然是忘改配置而非真实坐标, 放行会让飞机飞向赤道。
            List<FlyToTargetPoint> targets = readPoints(cfg, 50);
            if (targets.isEmpty()) {
                throw new FlightHubException("fly_to.points 为空 — 请在 demo_params.json 的 fly_to 节"
                    + "填写至少 1 个目标点(latitude/longitude/height)");
            }
            if (targets.get(0).getLatitude() == 0.0 && targets.get(0).getLongitude() == 0.0) {
                throw new FlightHubException(
                    "fly_to.points[0] 的 latitude/longitude 均为 0.0 — 这是 demo_params.json 的占位默认值,"
                    + " 请填写当前测试场地的真实 GPS 坐标后重新运行");
            }

            // start_point(航段起点)与目标点是两个不同坐标——PB 中 start_point 为 required,
            // 拿目标点顶替会让服务端把起点误判为目标点。默认取飞机实时位置。
            FlyToTargetPoint startPoint = resolveStartPoint(osd, cfg, defaultTimeout);

            // 限速先落局部变量再用: 步骤提示与真正下发的参数取同一份值, 避免两处各写一遍默认值
            // 导致「日志显示 A、实际下发 B」的漂移 (默认值与 demo_params.json 的 fly_to 节对齐)。
            int maxSpeed = cfg.intVal("max_speed", 10);
            step("启动指点飞行: 起点 lat=" + startPoint.getLatitude()
                + " lon=" + startPoint.getLongitude() + " height=" + startPoint.getHeight()
                + " -> 目标点 " + targets.size() + " 个, 首点 lat=" + targets.get(0).getLatitude()
                + " lon=" + targets.get(0).getLongitude() + " height=" + targets.get(0).getHeight()
                + ", maxSpeed=" + maxSpeed);

            FlyToPointParams p = new FlyToPointParams();
            p.setStartPoint(startPoint);
            p.setPoints(targets);
            p.setMaxSpeed(maxSpeed);
            // 新增可选字段: -1 表示不下发, 设备保持当前设置
            p.setSpeedMode(SpeedMode.fromValue(cfg.intVal("speed_mode", -1)));
            p.setRealHeightCapEnabled(cfg.intVal("real_height_cap_enabled", -1));
            p.setRealHeightCapLimit(cfg.intVal("real_height_cap_limit", -1));

            final Await<StartResult> startAwait = new Await<StartResult>("flyTo.start");
            startAttempted = true;   // 下发即置位: 回调超时不代表任务未启动, 收尾据此兜底停止。
            flyTo.start(p, new FlightHubCallback.FlyToPointStartCallback() {
                @Override
                public void onResult(FlightHubError error, String flyToId) {
                    // 回调原样双写 (控制台 + 日志文件): start 是本场景的分水岭——后续「更新目标点 /
                    // 停止 / 收尾补停」全都依赖这里的 flyToId 与错误码, 先把回调的原始值落一份,
                    // 便于 await 超时或后续步骤失败时对照排查 (此处只记录, 判定仍由主线程做)。
                    // 先 set 再打日志并采纳其返回值: Await 只认首个结果 (见 Await#set), 而本回调确实
                    // 可能被投递多次——{@code FlyToPointModule.start} 把整个下发过程包在一个
                    // catch(Exception) 里, 异常路径会就地回调 (ERROR_OPERATION_FAILED, ""); 若异常发生在
                    // startWrapper 成功注册原生回调【之后】(如参数对象 delete() 抛出), 就会先来一条空
                    // flyToId 的失败回调、随后再来一条真正的原生回调。第二次投递的值不会被主线程使用,
                    // 照原样打印会留下「flyToId 变了」的误导性记录, 故显式标注为已忽略。
                    boolean adopted = startAwait.set(new StartResult(error, flyToId));
                    String line = (adopted ? "" : "[迟到/重复回调, 已忽略] ")
                        + "flyTo.start(), [回调] FlyToPointStartCallback: flyToId="
                        + "\"" + flyToId +  "\", "
                        + " error= { " + describeError(error) + " }";
                    DemoLog.info(name(), line);
                    console("   " + line);
                }
            });

            StartResult start = startAwait.await(defaultTimeout);
            // 收尾补停的范围修正: 服务端以明确业务码拒绝 (serverCode!=0)时任务确定未创建,
            // 复位 startAttempted, 避免对未创建的任务补发 DRC 急停 (drcStop 即 DRC 的
            // drone_emergency_stop, 会刹停飞机当前正在执行的其它飞行)。仅此类失败复位——传输层歧义失败
            // (curl 失败/应答解析失败, serverCode==0)时任务可能已在服务端创建, 保持保守补停。
            if (start.error != null && !start.error.isSuccess() && start.error.getServerCode() != 0) {
                startAttempted = false;
            }

            requireSuccess("启动指点飞行", start.error);
            flyToIdRef.set(start.flyToId != null ? start.flyToId : "");
            DemoLog.info(name(), "指点飞行已启动, flyToId=" + flyToIdRef.get());
            // start 成功但回调未返回 flyToId(SDK 回调契约异常): 事件回调按 uuid 过滤(:329),
            // 空 id 永不匹配 → lastStatus 永远 STATUS_UNKNOWN → 终态判断失效; 且 Step 7 会以空 id
            // 下发 updateTargetPoints(服务端行为未定义)。此处告警, Step 7 据此跳过更新;
            // Step 9 的 drcStop 仍按保守策略执行(任务状态未知时停止是安全的)。
            if (flyToIdRef.get().isEmpty()) {
                DemoLog.warn(name(), "start 成功但服务端未返回 flyToId(SDK 回调契约异常), 任务事件将无法关联, 跳过更新目标点");
                hint("启动成功但未返回 flyToId, 跳过更新目标点(SDK 回调契约异常)");
            }

            // 等待首个本任务事件 (非致命: 收不到只说明推送链路未就绪, 不阻断流程)。
            // 用场景级超时 fly_to_timeout_ms(demo_params.json 现配 120s; 该键缺失时 timeout() 会回退到
            // default_callback_timeout_ms): 任务创建→ATC 分发→首个事件推送在真机链路上常超过默认回调
            // 超时 (15s), 事件是后续「终态跳过」判断的依据, 值得等更久。
            try {
                firstEvent.await(timeout("fly_to_timeout_ms"));
                DemoLog.info(name(), "已收到首个任务事件, status=" + lastStatus.get() + "(" + statusText(lastStatus.get()) + ")");
            } catch (FlightHubException e) {
                DemoLog.warn(name(), "未在超时内收到本任务事件(继续流程): " + Utils.describe(e));
            }

            // ---- Step 7: 更新目标点 (参数取自 fly_to_update 配置节) ----
            if (isTerminal(lastStatus.get()) || flyToIdRef.get().isEmpty()) {
                step("任务已到终态或未返回 flyToId, 跳过更新目标点");
            } else {
                Node upd = params.node("fly_to_update");
                // 安全校验：更新目标点同样是飞行指令, points 必须非空且首点不是占位默认值 (0,0)——
                // 两种情况统一落到「跳过」分支, 不阻断场景后续步骤。
                List<FlyToTargetPoint> updTargets = readPoints(upd, 60);
                boolean updUsable = !updTargets.isEmpty()
                    && !(updTargets.get(0).getLatitude() == 0.0 && updTargets.get(0).getLongitude() == 0.0);

                if (!updUsable) {
                    DemoLog.warn(name(), "fly_to_update.points 为空或首点仍是占位默认值 (0,0), 跳过更新目标点");
                    step("fly_to_update 缺少合法目标点, 跳过更新目标点");
                } else {
                    // 同「启动指点飞行」: 限速先落局部变量, 步骤提示与下发参数取同一份值。
                    // 高度不在此单独取: 它是每个目标点自己的字段, 已由 readPoints(upd, 60) 逐点读入。
                    int updMaxSpeed = upd.intVal("max_speed", 8);
                    step(
                        "更新目标点: " + updTargets.size() + " 个, 首点 lat=" + updTargets.get(0).getLatitude()
                        + " lon=" + updTargets.get(0).getLongitude()
                        + " height=" + updTargets.get(0).getHeight()
                        + ", maxSpeed=" + updMaxSpeed
                    );

                    FlyToUpdateTargetPointsParams up = new FlyToUpdateTargetPointsParams();
                    up.setFlyToId(flyToIdRef.get());
                    up.setMaxSpeed(updMaxSpeed);
                    up.setSpeedMode(SpeedMode.fromValue(upd.intVal("speed_mode", -1)));
                    up.setRealHeightCapEnabled(upd.intVal("real_height_cap_enabled", -1));
                    up.setRealHeightCapLimit(upd.intVal("real_height_cap_limit", -1));
                    up.setPoints(updTargets);
                    // startPoint 必须带: 服务端对 UpdateFlyToPointRequest.StartPoint 同样做
                    // required 校验, 不传会以 200614 "failed on the 'required' tag" 拒绝。
                    // 默认取飞机实时位置(与 start 一致), 配置里填了非 0 值才用配置值。
                    up.setStartPoint(resolveStartPoint(osd, upd, defaultTimeout));

                    call("更新目标点", cb -> flyTo.updateTargetPoints(up, cb), defaultTimeout);
                }
            }

            // ---- Step 8: 观察一段事件推送 (任务提前到终态则提前结束, 不空睡) ----
            // 复用上方已取到的 cfg(同一 fly_to 配置节), 不再重复 params.node("fly_to")。
            // 默认值与 demo_params.json 的 fly_to.observe_ms 对齐: 配置漏填时是短暂观察而非长时间无响应。
            long observeMs = cfg.longVal("observe_ms", 5000L);
            step("观察任务事件 " + observeMs + "ms(进度/路径见日志; 任务提前到终态则提前结束观察)");
            try {
                // 等终态信号而不是死睡: 任务在观察窗口内结束时立刻醒来, 直接进入「无需停止」分支。
                terminalEvent.await(observeMs);
                DemoLog.info(name(), "任务已到终态(" + statusText(lastStatus.get()) + "), 提前结束观察");
            } catch (FlightHubException e) {
                // 正常路径: 观察窗口内没等到终态信号 (await 超时即「观察满 observeMs」), 不是错误。
                // observe_ms 配成 ≤0 时 Await 也走这里 (它对非法超时立即抛), 等效于「不观察」, 同样不阻断流程。
                // 不在文案里断言「未到终态」——上述 ≤0 分支下任务可能其实已终态, 由 statusText 如实呈现。
                DemoLog.info(name(), "观察结束, 当前 status=" + statusText(lastStatus.get()));
            }

            // ---- Step 9: 停止指点飞行 (经 DRC 通道下发) ----
            if (isTerminal(lastStatus.get())) {
                step("任务已到终态(" + statusText(lastStatus.get()) + "), 无需停止");
                stopConfirmed = true;   // 任务已结束, 收尾无需再补停止。
            } else {
                step("停止指点飞行");
                call("停止指点飞行", cb -> flyTo.drcStop(cb), defaultTimeout);
                stopConfirmed = true;
            }
        } catch (Throwable t) {
            // 打印到控制台: steps() 抛出的异常最终会被 ModuleDemo.runBody() 捕获记录到日志文件,
            // 但那里不会回显控制台, 交互式运行的用户看不到场景为何中止——这里补一次控制台回显。
            // 捕 Throwable 而非 Exception: SDK 为 JNI 实现, 原生层可能抛 Error(如 UnsatisfiedLinkError),
            // 只 catch Exception 会让它绕过这次回显 (与基类 Scenario.quiet 的兜底口径一致)。原样重抛,
            // 不改变传播语义 (Java 7 精确重抛: t 为 effectively final, 方法签名仍是 throws Exception)。
            console("场景执行异常, 中止流程(详见日志文件): " + Utils.describe(t));
            throw t;
        } finally {
            // ---- 完备收尾: 每步各自兜异常, 一步失败不阻断后续 ----
            // 收尾标题打印单独兜底: step() 内部含 DemoLog + System.out, 两者故障 (如 stdout 被关闭、
            // OOM)会抛异常——若不兜住, 后续全部收尾步骤 (补停止 → 断 DRC → 释放控制权 → 取消监听)
            // 都会被跳过, 飞机可能处于失控飞行状态。与下方各步用 quiet() 包裹同理。
            quiet("打印收尾标题",
                () -> step("收尾：补停止(如需) → 断 DRC → 释放控制权 → 取消监听"));

            // 1) 任务已下发且未确认停止、也未观察到终态 → 补一次停止 (须在断开 DRC 之前——drcStop 走 DRC)。
            if (startAttempted && !stopConfirmed && !isTerminal(lastStatus.get())) {
                quiet("补停止指点飞行", () -> call("补停止指点飞行", cb -> flyTo.drcStop(cb), defaultTimeout));
            }

            // 2) 断开 DRC 链路。
            if (drcConnectAttempted) {
                quiet("断开 DRC 链路", () -> syncResult("断开 DRC 链路", drc::disconnectDrcLink, defaultTimeout));
            }

            // 3) 释放负载控制权、飞行控制权 (获取动作已下发即尝试释放; 未实际持有时服务端报错, 无害)。
            if (payloadAuthAttempted) {
                quiet(
                    "释放负载控制权",
                    () -> call("释放负载控制权", cb -> auth.releasePayloadControlAuthority(payloadIndex, cb), defaultTimeout)
                );
            }
            if (flightAuthAttempted) {
                Precondition.releaseFlightControl(drone, defaultTimeout);   // 内部吞异常仅告警
            }

            // 4) 显式取消全部监听 (subscribe 已同步登记会话 registry, 双保险; close 幂等)。
            for (Subscription sub : subs) {
                sub.close();
            }
            DemoLog.info(name(), "已取消监听 " + subs.size() + " 路");
        }
    }

    // syncResult / quiet 已提升至 Scenario 基类 (见 Scenario#syncResult / Scenario#quiet),
    // describeError 已提升至 ModuleDemo 基类 (见 ModuleDemo#describeError, 与 PoiScenario 共用同一份
    // 错误码格式), 子类直接调用。

    /**
     * 任务是否已到终态 (2=成功 3=失败 4=取消 5=超时)。
     *
     * <p>收敛为单一判据: 终态语义在「事件 CAS 更新 / 终态唤醒观察期 / 跳过更新目标点 / 跳过停止 /
     * 收尾是否补停」五处都要判, 各处裸写 {@code >= STATUS_TERMINAL_MIN} 容易在后续改动中漏改一处而分叉。
     * 哨兵值 {@link #STATUS_UNKNOWN}(-1, 尚未收到事件)在此一律视为<b>非</b>终态, 使「收不到事件」
     * 保守地走完整流程 (该补停就补停), 而不是被误判成已结束。
     */
    private static boolean isTerminal(int status) {
        return status >= STATUS_TERMINAL_MIN;
    }

    /**
     * 任务状态文字 (取值表源自 native message.h FlyToTaskData.status 注释)。
     *
     * <p>哨兵值 {@link #STATUS_UNKNOWN} 单独给「尚无事件」而不并入 {@code default} 的「未知」——
     * 「还没收到本任务的事件」与「收到了一个取值表外的状态码」是两种完全不同的排查方向。
     */
    private static String statusText(int status) {
        if (status == STATUS_UNKNOWN) {
            return "尚无事件";
        }

        switch (status) {
            case 0:
                return "待执行";
            case 1:
                return "执行中";
            case 2:
                return "成功";
            case 3:
                return "失败";
            case 4:
                return "取消";
            case 5:
                return "超时";
            default:
                return "未知";
        }
    }

    /**
     * 指令任务状态文字 (CommandTaskData.status, 源自 native message.h):
     * 0=待执行 1=执行中 2=已完成 3=失败 4=已取消; 与 FlyToTaskData 状态表的差异仅在<b>没有 5=超时</b>。
     */
    private static String commandStatusText(int status) {
        switch (status) {
            case 0:
                return "待执行";
            case 1:
                return "执行中";
            case 2:
                return "已完成";
            case 3:
                return "失败";
            case 4:
                return "已取消";
            default:
                return "未知";
        }
    }

    private static final class StartResult {
        final FlightHubError error;
        final String flyToId;

        StartResult(FlightHubError error, String flyToId) {
            this.error = error;
            this.flyToId = flyToId;
        }
    }
    /**
     * 抓取一次飞机实时位置作为指点飞行的 {@code start_point}(航段起点)。
     *
     * <p>SDK 没有拉取式位置接口, 只能临时订阅 {@code listenAircraftLocation}, 收到一帧后立刻退订。
     * {@code height} 取 {@code getHeight()}(WGS84 椭球高), 与目标点 height 同一基准;
     * {@code getElevation()} 是相对起飞点高度, 基准不同不可混用。
     *
     * <p>取不到就 <b>fail-fast</b>: 起点坐标不能猜——猜错会让服务端按错误的航段起点规划路径。
     */
    private FlyToTargetPoint captureStartPoint(DroneOsdModule osd, long timeoutMs) {
        if (osd == null) {
            throw new FlightHubException("获取 DroneOsdModule 失败, 无法取得飞机实时位置作为指点飞行起点");
        }
        final Await<AircraftLocation> await = new Await<AircraftLocation>("AircraftLocation");
        final int listenId = osd.listenAircraftLocation(new FlightHubCallback.AircraftLocationCallback() {
            @Override
            public void onResult(AircraftLocation loc) {
                // 空帧(本帧无数据)忽略, 继续等下一帧, 避免把 0,0 当成机位。
                if (loc != null) {
                    await.set(loc);
                }
            }
        });
        try {
            AircraftLocation loc = await.await(timeoutMs);
            FlyToTargetPoint sp = new FlyToTargetPoint();
            sp.setLatitude(loc.getLatitude());
            sp.setLongitude(loc.getLongitude());
            sp.setHeight(loc.getHeight());
            return sp;
        } finally {
            try {
                osd.cancelListenAircraftLocation(listenId);
            } catch (Exception e) {
                DemoLog.warn(name(), "取消飞机位置订阅失败(忽略): " + Utils.describe(e));
            }
        }
    }

    /** 从配置节读取目标点数组({@code points})。 */
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

    /**
     * 解析航段起点：配置里 {@code start_point} 三项均为 0 → 订阅一帧实时机位(默认、推荐);
     * 否则用配置值(仅联调场景)。静态配置的起点在飞机移动后立刻失效, 所以默认走实时机位。
     */
    private FlyToTargetPoint resolveStartPoint(DroneOsdModule osd, Node cfg, long timeoutMs) {
        Node sp = cfg.child("start_point");
        double lat = sp.dbl("latitude", 0);
        double lon = sp.dbl("longitude", 0);
        double h = sp.dbl("height", 0);
        if (lat != 0 || lon != 0 || h != 0) {
            FlyToTargetPoint p = new FlyToTargetPoint();
            p.setLatitude(lat);
            p.setLongitude(lon);
            p.setHeight(h);
            DemoLog.warn(name(), "start_point 取自配置(非实时机位): lat=" + lat + " lon=" + lon
                + " height=" + h + " — 飞机已移动时该值会失效");
            return p;
        }
        return captureStartPoint(osd, timeoutMs);
    }

}
