// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.scenario;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.CommandTaskData;
import com.dji.flighthubsdk.bean.CommandTaskEvent;
import com.dji.flighthubsdk.bean.DroneOnlineStatus;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.FlyToPointParams;
import com.dji.flighthubsdk.bean.FlyToTargetPoint;
import com.dji.flighthubsdk.bean.FlyToTaskData;
import com.dji.flighthubsdk.bean.FlyToTaskEvent;
import com.dji.flighthubsdk.bean.FlyToUpdateTargetPointsParams;
import com.dji.flighthubsdk.bean.LinkStatus;
import com.dji.flighthubsdk.core.ControlAuthModule;
import com.dji.flighthubsdk.core.DeviceStateModule;
import com.dji.flighthubsdk.core.DrcModule;
import com.dji.flighthubsdk.core.Drone;
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
 * 场景：指点飞行(FlyTo)完整流程——控制权/DRC 前置 → 监听 → 启动 → 更新目标点 → 停止 → 完备收尾。
 *
 * <p><b>SDK-P1-12 · 坐标合法性校验属于业务应用层职责</b>：
 * FHSDK / native 层<b>不做</b>飞行目标点的业务合法性校验(是否在合法飞行区、是否为占位坐标、
 * 是否越境、是否偏离起飞点过远等), 仅做「参数格式正确」的基础校验。ISV 应用集成 SDK 时,
 * <b>必须在应用层自行实现</b>坐标校验(如禁飞区查询、坐标可信性判断)。本 Demo 只做最低限度的
 * 「坐标不能为 (0,0) 占位默认值」示例校验(见 {@link #steps()} 中「启动指点飞行」步骤的坐标合法性检查),
 * 供参考而非兜底。
 *
 * <p>该场景演示了指点飞行(FlyTo)的完整流程：建立 DRC 链路 → 获取飞行控制权与负载控制权
 * (负载控制权失败自动重试, 重试耗尽后跳过) → 建立基础监听 → 订阅任务事件 → 启动指点飞行 →
 * 更新目标点 → 观察 → 停止指点飞行。步骤：
 * <ol>
 *   <li>建立 DRC 链路——<b>{@code FlyToPointModule.stop} 走 DRC 通道(DrcDroneEmergencyStop)</b>,
 *       建链是「停止指点飞行」的硬前置, 必须先于 start; 等待结果, 双写日志(控制台+文件),
 *       失败即中止场景;</li>
 *   <li>获取飞行控制权(指点飞行的前置); 等待结果, 双写日志, 失败即中止场景;</li>
 *   <li>获取负载控制权(相机/云台类操控的前置, 演示基础功能编排); 等待结果, 双写日志;
 *       失败最多重试 3 次、每次间隔 10s, 每轮打印轮次与结果(双写日志); 重试耗尽后<b>跳过、
 *       继续后续流程</b>(本场景后续步骤不依赖负载控制权, 仅作获取/释放的编排示范);</li>
 *   <li>基础监听：飞机在线状态、DRC 链路状态、飞行/负载控制权状态、指令任务事件(便于观察前序
 *       控制权动作引起的状态变化; 步骤提示双写日志, 推送数据仅落日志文件);</li>
 *   <li>订阅指点飞行任务事件 {@code listenFlyToTaskEvent}(见下方「监听接口选型」; 步骤提示
 *       双写日志, 推送数据仅落日志文件);</li>
 *   <li>启动指点飞行(目标点取自 {@code fly_to} 配置节), 等待首个任务事件
 *       (超时 {@code fly_to_timeout_ms});</li>
 *   <li>更新目标点(取自 {@code fly_to_update} 配置节; startPoint 不设, 由服务端按当前机位处理);</li>
 *   <li>观察一段任务事件推送(推送数据仅落日志文件);</li>
 *   <li>停止指点飞行。</li>
 * </ol>
 *
 * <p><b>为何调整顺序(实测发现)</b>：{@code acquirePayloadControlAuthority} /
 * {@code listenPayloadControlAuthority} 可能因负载(相机)OSD 信息尚未推送到云端而报
 * "payload_index: xx-x-x not found"——这是设备刚上线/建链时的时序竞态, 并非硬件或接口错误。
 * 调整为先建立 DRC 链路、获取飞行控制权(两者更可靠), 再尝试获取负载控制权并配合重试, 为 OSD
 * 信息的到达争取时间; 若重试耗尽仍失败, 场景选择跳过负载控制权、继续后续飞行流程, 而不是
 * 中止整个场景。
 *
 * <p><b>监听接口选型</b>：{@code subscribeFlyToDroneSelfPlanPath}(规划路径)与
 * {@code subscribeFlyToPointProgress}(进度)已被 SDK 标记 {@code @Deprecated}——其消息 topic
 * 已被 {@code topic_atc_flyto_task_event} 取代, 老 topic 不再有数据。本场景按 SDK 指引改用
 * {@code listenFlyToTaskEvent} 一个订阅同时覆盖两者：路径见
 * {@link FlyToTaskData#getPlannedPathPoints()}, 进度见 {@code status/wayPointIndex/
 * remainingDistance/remainingTime}。任务状态取值(源自 message.h FlyToTaskData 注释)：
 * 0=待执行 1=执行中 2=成功 3=失败 4=取消 5=超时(≥2 为终态)。
 *
 * <p><b>收尾保证(finally, 每步各自兜异常不互相阻断)</b>：
 * <ol>
 *   <li>若指点飞行已下发且未确认停止、任务未到终态——补一次停止(含 start 超时后实际已起飞的竞态:
 *       下发即置 {@code startAttempted}, 不依赖回调是否按时返回);</li>
 *   <li>断开 DRC 链路(在停止<b>之后</b>——stop 走 DRC, 顺序不可颠倒);</li>
 *   <li>释放负载控制权、释放飞行控制权(获取类动作按「下发即可能生效」处理, 超时也照常释放);</li>
 *   <li>显式取消全部监听(订阅同时登记在会话 registry, 双保险; {@code Subscription.close} 幂等)。</li>
 * </ol>
 *
 * <p>前置假设：飞机已在空中(可先经 FlightControlDemo [6] 一键起飞或航线任务起飞)。本场景刻意
 * 不包含起飞——与 {@code PoiScenario} 不同, 指点飞行常作为空中任务的编排单元, 由调用方决定起飞方式。
 * 所有参数来自 demo_params.json 的 {@code fly_to}/{@code fly_to_update} 分组, 无手动交互输入。
 *
 * <p><b>已知 SDK 限制(非本场景 bug)</b>：{@code cancelListenXxx} 会立即销毁原生回调包装器,
 * 与在途事件派发存在极小概率的原生竞态(与 resetGimbal 回调重复投递属同族系统性问题, 已上报);
 * 本场景按正确顺序退订, demo 侧无法根除。
 */
public class FlyToScenario extends Scenario {

    // 任务状态取值(源自 native message.h FlyToTaskData.status 注释)。
    private static final int STATUS_UNKNOWN = -1;
    private static final int STATUS_TERMINAL_MIN = 2;   // ≥2 即终态(2=成功 3=失败 4=取消 5=超时)

    @Override
    public String name() {
        return "FlyToScenario";
    }

    @Override
    protected void steps() throws Exception {
        final Drone drone = session.drone();

        // 模块 getter 在 SDK 内部失败时会返回 null, 先集中判空给出诊断。
        final FlyToPointModule flyTo = drone.getFlyToPointModule();
        final ControlAuthModule auth = drone.getControlAuthModule();
        final DrcModule drc = drone.getDrcModule();
        final DeviceStateModule state = drone.getDeviceStateModule();
        final TakeoffModule takeoff = drone.getTakeoffModule();   // optional: null-guarded at use site
        if (flyTo == null || auth == null || drc == null || state == null) {
            throw new FlightHubException("获取模块失败: flyTo=" + (flyTo != null) + " auth=" + (auth != null)
                + " drc=" + (drc != null) + " state=" + (state != null));
        }

        final String payloadIndex = payloadIndex();
        final long defaultTimeout = defaultTimeout();

        // 收尾判断用状态(「下发即可能生效」语义: 在动作下发前置位, 不依赖回调按时返回)。
        boolean flightAuthAttempted = false;
        boolean payloadAuthAttempted = false;
        boolean drcConnectAttempted = false;
        boolean startAttempted = false;
        boolean stopConfirmed = false;

        // 任务事件跟踪: 本次任务 id(start 回调返回) + 最近一次事件的任务状态。
        final AtomicReference<String> flyToIdRef = new AtomicReference<String>("");
        final AtomicInteger lastStatus = new AtomicInteger(STATUS_UNKNOWN);
        final List<Subscription> subs = new ArrayList<Subscription>();

        try {
            // ---- Step 1: 建立 DRC 链路(stop 走 DRC 通道, 建链是停止指点飞行的硬前置) ----
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

            // ---- Step 3: 获取负载控制权(失败重试, 最多 3 次, 每次间隔 10s) ----
            step("获取负载控制权(payloadIndex=" + payloadIndex + ")");
            final int payloadAuthMaxAttempts = 3;
            final long payloadAuthRetryDelayMs = 10_000L;
            payloadAuthAttempted = true;    // 下发即置位(整组重试只置一次): 收尾据此兜底释放。
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
                    System.out.println("   " + waitMsg);
                    sleep(payloadAuthRetryDelayMs);
                }
            }

            if (payloadAuthOutcome != Outcome.SUCCESS) {
                String skipMsg = "获取负载控制权重试 " + payloadAuthMaxAttempts + " 次仍失败, 跳过负载控制权, 继续后续飞行流程";
                DemoLog.warn(name(), skipMsg);
                System.out.println("   " + skipMsg);
            }

            // ---- Step 4: 基础监听(推送只落日志文件) ----
            step("基础监听：在线状态 / DRC 链路状态 / 飞行控制权 / 负载控制权");
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

            // 指令任务事件(TakeoffModule 可选, null 时跳过)。
            // CommandTaskData.status: 0=待执行 1=执行中 2=已完成 3=失败 4=已取消
            if (takeoff == null) {
                DemoLog.warn(name(), "TakeoffModule 不可用, 跳过指令任务事件监听");
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
                                DemoLog.info(name(), "  task_data: id=" + d.getId()
                                    + " uuid=" + d.getUuid()
                                    + " prj_uuid=" + d.getPrjUuid()
                                    + " sn=" + d.getSn()
                                    + " drone_sn=" + d.getDroneSn()
                                    + " track_id=" + d.getTrackId()
                                    + " user_id=" + d.getUserId()
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

            // ---- Step 5: 订阅指点飞行任务事件(先于 start, 不漏首个事件) ----
            step("订阅指点飞行任务事件(listenFlyToTaskEvent, 替代已废弃的路径/进度订阅)");
            final Await<Boolean> firstEvent = new Await<Boolean>("flyTo.firstEvent");
            trySubscribe(subs, "flyToTaskEvent",
                flyTo.listenFlyToTaskEvent(new FlightHubCallback.FlyToTaskEventCallback() {
                    @Override
                    public void onResult(FlyToTaskEvent event) {
                        if (event == null || event.getTaskData() == null) {
                            return;
                        }
                        FlyToTaskData d = event.getTaskData();
                        // plannedPathPoints 判空: 服务端事件省略该字段时 bean 保持 null(无参构造不初始化)。
                        DemoLog.info(name(), "[推送] listenFlyToTaskEvent() #FlyToTaskEvent,FlyTo 任务事件: action=" + event.getAction()
                            + " uuid=" + d.getUuid()
                            + " status=" + d.getStatus() + "(" + statusText(d.getStatus()) + ")"
                            + " wayPointIndex=" + d.getWayPointIndex()
                            + " 剩余距离=" + d.getRemainingDistance() + "m"
                            + " 剩余时间=" + d.getRemainingTime() + "s"
                            + " 规划路径点=" + (d.getPlannedPathPoints() != null
                            ? d.getPlannedPathPoints().size() : 0));
                        // 只跟踪本次启动的任务(uuid 匹配 flyToId; 事件先于 start 回调返回时 id 尚空, 不误采)。
                        String myId = flyToIdRef.get();
                        if (!myId.isEmpty() && myId.equals(d.getUuid())) {
                            // 单调/终态粘滞更新: 原生 topic_atc_flyto_task_event 存在至少一次投递语义,
                            // 事件可能重复或乱序到达——一旦到达终态(≥STATUS_TERMINAL_MIN)就不再被更旧/
                            // 重复的状态覆盖, 且不接受比当前值更旧或相同的状态回退。
                            int newStatus = d.getStatus();
                            int cur;
                            do {
                                cur = lastStatus.get();
                                if (cur >= STATUS_TERMINAL_MIN || newStatus <= cur) {
                                    break;
                                }
                            } while (!lastStatus.compareAndSet(cur, newStatus));
                            firstEvent.set(Boolean.TRUE);
                        }
                    }
                }),
                flyTo::cancelListenFlyToTaskEvent);

            // ---- Step 6: 启动指点飞行(目标点取自 fly_to 配置节) ----
            Node cfg = params.node("fly_to");
            // 安全校验：latitude/longitude 是飞行目标点, 缺失/类型错误时 fail-fast——
            // requireDouble 不像 dbl() 那样静默回退默认值 0.0(会让飞机飞向赤道)。
            double lat;
            double lon;
            try {
                lat = cfg.requireDouble("latitude");
                lon = cfg.requireDouble("longitude");
            } catch (FlightHubException e) {
                throw new FlightHubException("fly_to 配置节坐标非法: " + Utils.describe(e)
                    + " — 请在 demo_params.json 的 fly_to 节填写合法的目标 GPS 坐标");
            }

            // 二次校验：(0.0, 0.0) 可能是 demo_params.json 中的占位默认值, 对应赤道与本初子午线的交点(几内亚湾),
            // 很可能是用户忘记修改配置, 而非真实测试坐标——拦截并强制要求配置真实坐标。
            if (lat == 0.0 && lon == 0.0) {
                throw new FlightHubException(
                    "fly_to 的 latitude/longitude 均为 0.0 — 这是 demo_params.json 的占位默认值,"
                    + " 请填写当前测试场地的真实 GPS 坐标后重新运行");
            }

            step("启动指点飞行: lat=" + lat + " lon=" + lon + " height=" + cfg.dbl("height", 50) + " maxSpeed=" + cfg.intVal("max_speed", 10));
            FlyToPointParams p = new FlyToPointParams();
            p.setLatitude(lat);
            p.setLongitude(lon);
            p.setHeight(cfg.dbl("height", 50));
            p.setMaxSpeed(cfg.intVal("max_speed", 10));

            final Await<StartResult> startAwait = new Await<StartResult>("flyTo.start");
            startAttempted = true;   // 下发即置位: 回调超时不代表任务未启动, 收尾据此兜底停止。
            flyTo.start(p, new FlightHubCallback.FlyToPointStartCallback() {
                @Override
                public void onResult(FlightHubError error, String flyToId) {
                    startAwait.set(new StartResult(error, flyToId));
                }
            });

            StartResult start = startAwait.await(defaultTimeout);
            // 收尾补停的范围修正: 服务端以明确业务码拒绝(serverCode!=0)时任务确定未创建,
            // 复位 startAttempted, 避免对未创建的任务补发 DRC 急停(stop=DrcDroneEmergencyStop,
            // 会刹停飞机当前正在执行的其它飞行)。仅此类失败复位——传输层歧义失败
            // (curl 失败/应答解析失败, serverCode==0)时任务可能已在服务端创建, 保持保守补停。
            if (start.error != null && !start.error.isSuccess() && start.error.getServerCode() != 0) {
                startAttempted = false;
            }

            requireSuccess("启动指点飞行", start.error);
            flyToIdRef.set(start.flyToId != null ? start.flyToId : "");
            DemoLog.info(name(), "指点飞行已启动, flyToId=" + flyToIdRef.get());

            // 等待首个本任务事件(非致命: 收不到只说明推送链路未就绪, 不阻断流程)。
            // 用场景级超时 fly_to_timeout_ms(默认 120s): 任务创建→ATC 分发→首个事件推送在真机链路
            // 上常超过默认回调超时(15s), 事件是后续「终态跳过」判断的依据, 值得等更久。
            try {
                firstEvent.await(timeout("fly_to_timeout_ms"));
                DemoLog.info(name(), "已收到首个任务事件, status=" + lastStatus.get() + "(" + statusText(lastStatus.get()) + ")");
            } catch (FlightHubException e) {
                DemoLog.warn(name(), "未在超时内收到本任务事件(继续流程): " + Utils.describe(e));
            }

            // ---- Step 7: 更新目标点(参数取自 fly_to_update 配置节) ----
            if (lastStatus.get() >= STATUS_TERMINAL_MIN) {
                step("任务已到终态(" + statusText(lastStatus.get()) + "), 跳过更新目标点");
            } else {
                Node upd = params.node("fly_to_update");
                // 安全校验：更新目标点同样是飞行指令, 坐标必填且不能是占位默认值 (0,0)——
                // 缺失/类型错误(requireDouble 抛异常)与仍是占位默认值, 统一落到「跳过」分支。
                Double updLat = null;
                Double updLon = null;
                try {
                    double latVal = upd.requireDouble("latitude");
                    double lonVal = upd.requireDouble("longitude");
                    if (!(latVal == 0.0 && lonVal == 0.0)) {
                        updLat = latVal;
                        updLon = lonVal;
                    }
                } catch (FlightHubException ignore) {
                    // 缺失/类型不对: 落到下方「跳过更新目标点」分支。
                }

                if (updLat == null || updLon == null) {
                    DemoLog.warn(name(), "fly_to_update 配置节缺少合法坐标(缺失/类型错误/仍是占位默认值 0.0), 跳过更新目标点");
                    step("fly_to_update 缺少坐标配置, 跳过更新目标点");
                } else {
                    step(
                        "更新目标点: lat=" + updLat + " lon=" + updLon +
                        " height=" + upd.dbl("height", 60) + " maxSpeed=" + upd.intVal("max_speed", 8)
                    );

                    FlyToUpdateTargetPointsParams up = new FlyToUpdateTargetPointsParams();
                    up.setFlyToId(flyToIdRef.get());
                    up.setMaxSpeed(upd.intVal("max_speed", 8));
                    FlyToTargetPoint point = new FlyToTargetPoint();
                    point.setLatitude(updLat);
                    point.setLongitude(updLon);
                    point.setHeight(upd.dbl("height", 60));
                    List<FlyToTargetPoint> points = new ArrayList<FlyToTargetPoint>();
                    points.add(point);
                    up.setPoints(points);
                    // startPoint(当前机位)不设置: 转换层对 null 直接跳过, 由服务端按实时位置处理
                    // (场景 demo 无交互, 交给服务端更稳妥)。

                    call("更新目标点", cb -> flyTo.updateTargetPoints(up, cb), defaultTimeout);
                }
            }

            // ---- Step 8: 观察一段事件推送 ----
            long observeMs = params.node("fly_to").longVal("observe_ms", 5000L);
            step("观察任务事件 " + observeMs + "ms(进度/路径见日志)");
            sleep(observeMs);

            // ---- Step 9: 停止指点飞行(经 DRC 通道下发) ----
            if (lastStatus.get() >= STATUS_TERMINAL_MIN) {
                step("任务已到终态(" + statusText(lastStatus.get()) + "), 无需停止");
                stopConfirmed = true;   // 任务已结束, 收尾无需再补停止。
            } else {
                step("停止指点飞行");
                call("停止指点飞行", cb -> flyTo.drcStop(cb), defaultTimeout);
                stopConfirmed = true;
            }
        } catch (Exception e) {
            // 打印到控制台: steps() 抛出的异常最终会被 ModuleDemo.runBody() 捕获记录到日志文件,
            // 但那里不会回显控制台, 交互式运行的用户看不到场景为何中止——这里补一次控制台回显。
            System.out.println("场景执行异常, 中止流程(详见日志文件): " + Utils.describe(e));
            throw e;
        } finally {
            // ---- 完备收尾: 每步各自兜异常, 一步失败不阻断后续 ----
            step("收尾：补停止(如需) → 断 DRC → 释放控制权 → 取消监听");

            // 1) 任务已下发且未确认停止、也未观察到终态 → 补一次停止(须在断开 DRC 之前——stop 走 DRC)。
            if (startAttempted && !stopConfirmed && lastStatus.get() < STATUS_TERMINAL_MIN) {
                quiet("补停止指点飞行", () -> call("补停止指点飞行", cb -> flyTo.drcStop(cb), defaultTimeout));
            }

            // 2) 断开 DRC 链路。
            if (drcConnectAttempted) {
                quiet("断开 DRC 链路", () -> syncResult("断开 DRC 链路", drc::disconnectDrcLink, defaultTimeout));
            }

            // 3) 释放负载控制权、飞行控制权(获取动作已下发即尝试释放; 未实际持有时服务端报错, 无害)。
            if (payloadAuthAttempted) {
                quiet(
                    "释放负载控制权",
                    () -> call("释放负载控制权", cb -> auth.releasePayloadControlAuthority(payloadIndex, cb), defaultTimeout)
                );
            }
            if (flightAuthAttempted) {
                Precondition.releaseFlightControl(drone, defaultTimeout);   // 内部吞异常仅告警
            }

            // 4) 显式取消全部监听(subscribe 已同步登记会话 registry, 双保险; close 幂等)。
            for (Subscription sub : subs) {
                sub.close();
            }
            DemoLog.info(name(), "已取消监听 " + subs.size() + " 路");
        }
    }

    /** 把「以 ResultCallback 结尾的动作」同步化——由基类 {@link Scenario#syncResult} 提供, 此处保留说明供阅读者参考。 */
    // syncResult / quiet 已提升至 Scenario 基类，子类直接调用 syncResult(...) 和 quiet(...)。

    /** 任务状态文字(取值表源自 native message.h FlyToTaskData.status 注释)。 */
    private static String statusText(int status) {
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

    /** 指令任务状态文字(CommandTaskData.status; 与 FlyToTaskData 状态表不同: 无超时, 完成=2)。 */
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
}
