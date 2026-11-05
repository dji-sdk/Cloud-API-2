// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.scenario;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.POIEnterParams;
import com.dji.flighthubsdk.bean.POITaskInfo;
import com.dji.flighthubsdk.bean.PoiTaskData;
import com.dji.flighthubsdk.bean.PoiTaskEvent;
import com.dji.flighthubsdk.bean.Waypoint;
import com.dji.flighthubsdk.core.ControlAuthModule;
import com.dji.flighthubsdk.core.DrcModule;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.FlightControlModule;
import com.dji.flighthubsdk.core.PoiModule;
import com.dji.flighthubsdk.core.TakeoffModule;
import com.dji.flighthubsdk.demo.base.Precondition;
import com.dji.flighthubsdk.demo.base.Scenario;
import com.dji.flighthubsdk.demo.runtime.Await;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.runtime.Errors;
import com.dji.flighthubsdk.demo.runtime.FlightHubException;
import com.dji.flighthubsdk.demo.runtime.Node;
import com.dji.flighthubsdk.demo.runtime.Subscription;
import com.dji.flighthubsdk.demo.utils.Utils;
import com.dji.flighthubsdk.generated.FlightPoiProgress;
import com.dji.flighthubsdk.value.POITaskStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 场景: 兴趣点环绕 (POI)完整流程——控制权/DRC 前置 → 一键起飞 → 进入 POI → 监听/调速/查询 →
 * 退出 POI → 返航 → 完备收尾。
 *
 * <p>该场景演示了兴趣点环绕 (POI)的完整流程: 获取飞行控制权与负载控制权 → 建立 DRC 链路 →
 * 一键起飞 → 建立基础监听 → 进入 POI 环绕模式 → 更新速度 → 查询任务信息 → 退出 POI → 返航。
 * 步骤:
 * <ol>
 *   <li>基础监听: 飞行/负载控制权状态 (先订阅, 便于观察后续动作引起的变化); </li>
 *   <li>获取飞行控制权; </li>
 *   <li>获取负载控制权 (POI 进入参数携带 payloadIndex, 环绕中云台指向中心点); </li>
 *   <li>建立 DRC 链路 (基础链路前置); </li>
 *   <li>一键起飞 (参数取自 {@code takeoff} 配置节, 只判断指令下发成功); </li>
 *   <li>等待爬升到目标高度 ({@code poi.climb_wait_ms}, 简化为固定等待——严谨做法可订阅 OSD 高度); </li>
 *   <li>订阅 POI 任务事件 ({@code listenPoiTaskEvent})与 POI 飞行进度 ({@code listenFlightPoiProgress})
 *       ——先于进入建立, 不漏首个事件; 事件按本任务 uuid 过滤 (订阅时 SDK 会回放上一任务的缓存事件); </li>
 *   <li>进入 POI 环绕模式 (中心点/速度/半径取自 {@code poi} 配置节), 等待首个本任务事件
 *       (超时 {@code poi_timeout_ms}); </li>
 *   <li>更新环绕速度 ({@code poi.update_speed}; 任务已终态则跳过, 演示性步骤失败只告警不阻断); </li>
 *   <li>观察环绕 {@code poi.observe_ms} 后查询当前 POI 任务信息 ({@code getTask}, 全字段落日志文件); </li>
 *   <li>演示目标达成, 退出 POI 模式; </li>
 *   <li>返航。</li>
 * </ul>
 *
 * <p>POI 任务状态 (见 {@link POITaskStatus}): 0=待开始 1=启动失败 2=执行中 3=已暂停 4=终止
 * 5=成功 6=挂起 7=超时 8=部分执行 9=准备中 10=起飞排队; 其中 1/4/5/7 视为终态——
 * 事件已见终态时跳过退出 (任务已结束, 退出无对象)。
 *
 * <p><b>收尾保证 (finally, 每步各自兜异常不互相阻断)</b>:
 * <ol>
 *   <li>若已进入 POI 且未确认退出、任务未到终态——补一次退出 (含 enter 超时后任务实际已建的竞态:
 *       下发即置 {@code enterAttempted}; 服务端以明确业务码拒绝 (serverCode!=0)时任务确定未创建,
 *       复位标志不做无谓补退); </li>
 *   <li>断开 DRC 链路; </li>
 *   <li>释放负载控制权、释放飞行控制权 (获取类动作按「下发即可能生效」处理, 超时也照常释放); </li>
 *   <li>显式取消全部监听 (订阅同时登记在会话 registry, 双保险; {@code Subscription.close} 幂等); </li>
 *   <li><b>不自动返航/降落</b>——返航是主流程步骤; 异常路径退出时若飞机可能仍在空中仅告警提醒
 *       (与 FlightControlDemo 的策略一致: demo 静默触发返航比把决定留给操作者更危险)。
 *       <b>与 DrcFlightScenario 的策略对比</b>: DrcFlightScenario 收尾时会自动补发返航,
 *       原因是该场景全程处于「持续 DRC 手动操控」语境, 用户对飞机状态持续可见、可控, 断链后自动
 *       补一次返航是让飞机脱离无人操控状态的合理默认动作; 而 POI 环绕等自动化任务场景里,
 *       用户可能并未持续盯盘, 服务端对当前空域/避障状态的判断比 demo 本地静默触发更可靠, 因此
 *       选择保守的「仅告警、把决定权交给操作者」。两种策略均为有意设计, 非缺陷, 各自适配不同的
 *       人机交互假设, 使用者接入生产环境时应按自身场景重新评估该取舍。</li>
 * </ul>
 *
 * <p>所有参数来自 demo_params.json 的 {@code poi}/{@code takeoff} 分组, 无手动交互输入。
 * 其中 {@code poi.latitude/longitude} 是飞行目标, 在<b>取控制权与起飞之前</b>就用
 * {@link Node#requireDouble} 校验并 fail-fast(缺失/类型错误/占位 (0, 0) 一律拒绝)——
 * 拦在起飞前, 而不是等飞机已在空中才报错。
 * <b>日志口径</b>: 步骤提示与<b>动作回调</b>({@code enter}/{@code getTask})双写日志 (控制台 + 日志文件)
 * ——动作回调每次调用只触发一次, 且它的 taskUuid/错误码是后续每一步的判断依据, 实时可见才便于排查;
 * <b>监听推送</b>({@code listenXxx})只落日志文件——推送为高频连续投递, 上控制台会把步骤提示刷没。
 */
public class PoiScenario extends Scenario {

    /** 任务状态未知 (尚未收到本任务事件)。 */
    private static final int STATUS_UNKNOWN = -1;

    @Override
    public String name() {
        return "PoiScenario";
    }

    @Override
    protected void steps() throws Exception {
        // 无飞机拓扑 (只有机场/RC)下 session.drone() 直接抛 FlightHubException (见 {@link DeviceSession#drone()}),
        // 会被 runBody 当成「demo 执行失败」的 ERROR 上报——设备选错属正常前置而非执行失败,
        // 故先判定并用 hint(日志 + stdout)给出可读措辞, 与 12 个模块 demo 的守卫同口径。
        // 只判拓扑有无、不判在线: isDroneOnline() 是选设备时刻的快照 (见 {@link DeviceSession#isDroneOnline()}, 非实时),
        // 拿它当硬门会误伤「选完设备后飞机才上线」。
        if (!session.hasDrone()) {
            hint("该设备没有无人机, 无法运行 POI 环绕场景");
            return;
        }

        final Drone drone = session.drone();

        // 模块 getter 在 SDK 内部失败时会返回 null, 先集中判空给出诊断。
        // 起飞/返航模块也在此判 (getter 结果有缓存): 「没有返航手段」必须在起飞前 fail-fast,
        // 而不是全程飞完后在返航一步抛无诊断的 NPE。
        final PoiModule poi = drone.getPoiModule();
        final ControlAuthModule auth = drone.getControlAuthModule();
        final DrcModule drc = drone.getDrcModule();
        final TakeoffModule takeoff = drone.getTakeoffModule();
        final FlightControlModule fc = drone.getFlightControlModule();
        if (poi == null || auth == null || drc == null || takeoff == null || fc == null) {
            throw new FlightHubException("获取模块失败: poi=" + (poi != null)
                + " auth=" + (auth != null) + " drc=" + (drc != null)
                + " takeoff=" + (takeoff != null) + " fc=" + (fc != null));
        }

        final String payloadIndex = payloadIndex();
        final long defaultTimeout = defaultTimeout();
        final Node cfg = params.node("poi");

        // 安全校验: POI 中心点 latitude/longitude 是飞行目标, 必须在取控制权/建链/起飞【之前】
        // fail-fast——若放在 Step 8, 拦截时飞机已经起飞并空等完一轮爬升, 而本场景不自动返航。
        // 用 requireDouble 而非 has()+dbl(): has() 只判「键存在且非 null」, 对 "22.58N" 这类
        // 非数值照样放行, dbl() 再静默回退 0.0(仅留一条 DEBUG 日志); 单个坐标写错时下面那道
        // (0, 0) 守卫是逻辑与、拦不住, 结果会带着 (0.0, 113.938164) 这种坐标下发。
        final double lat;
        final double lon;
        try {
            lat = cfg.requireDouble("latitude");
            lon = cfg.requireDouble("longitude");
        } catch (FlightHubException e) {
            throw new FlightHubException("poi 配置节坐标非法: " + Utils.describe(e)
                + " — 请在 demo_params.json 的 poi 节填写合法的环绕中心 GPS 坐标", e);
        }
        // 二次校验: (0.0, 0.0) 是 demo_params.json 中的占位默认值, 对应几内亚湾;
        // requireDouble 认为 0.0 是合法数值, 挡不住占位值, 故保留这道守卫。
        if (lat == 0.0 && lon == 0.0) {
            throw new FlightHubException(
                "poi 的 latitude/longitude 均为 0.0 — 这是 demo_params.json 的占位默认值,"
                + " 请填写当前测试场地的真实 GPS 坐标后重新运行");
        }

        // 收尾判断用状态 (「下发即可能生效」语义: 在动作下发前置位, 不依赖回调按时返回)。
        boolean flightAuthAttempted = false;
        boolean payloadAuthAttempted = false;
        boolean drcConnectAttempted = false;
        boolean tookOff = false;
        boolean enterAttempted = false;
        boolean exitConfirmed = false;
        boolean returnedHome = false;

        // 任务事件跟踪: 本次任务 uuid(enter 回调返回) + 最近一次事件的任务状态。
        final AtomicReference<String> taskUuidRef = new AtomicReference<String>("");
        final AtomicInteger lastStatus = new AtomicInteger(STATUS_UNKNOWN);
        final List<Subscription> subs = new ArrayList<Subscription>();

        try {
            // ---- Step 1: 基础监听 (推送只落日志文件) ----
            step("基础监听：飞行控制权 / 负载控制权状态");
            subs.add(subscribe("flightControlAuthority",
                auth.listenFlightControlAuthority(new FlightHubCallback.FlightControlAuthorityCallback() {
                    @Override
                    public void onResult(boolean isGranted) {
                        DemoLog.info(name(), "[推送] listenFlightControlAuthority() #boolean,飞行控制权状态: is_granted=" + isGranted);
                    }
                }),
                auth::cancelListenFlightControlAuthority));
            subs.add(subscribe("payloadControlAuthority",
                auth.listenPayloadControlAuthority(payloadIndex,
                    new FlightHubCallback.PayloadControlAuthorityCallback() {
                        @Override
                        public void onResult(boolean isGranted) {
                            DemoLog.info(name(), "[推送] listenPayloadControlAuthority() #boolean,负载控制权状态: is_granted=" + isGranted);
                        }
                    }),
                id -> auth.cancelListenPayloadControlAuthority(payloadIndex, id)));

            // ---- Step 2: 获取飞行控制权 ----
            step("获取飞行控制权");
            flightAuthAttempted = true;
            Precondition.acquireFlightControl(drone, defaultTimeout);

            // ---- Step 3: 获取负载控制权 ----
            step("获取负载控制权(payloadIndex=" + payloadIndex + ")");
            payloadAuthAttempted = true;
            call("获取负载控制权", cb -> auth.acquirePayloadControlAuthority(payloadIndex, cb), defaultTimeout);

            // ---- Step 4: 建立 DRC 链路 ----
            step("建立 DRC 链路");
            drcConnectAttempted = true;
            syncResult("建立 DRC 链路", drc::connectDrcLink, defaultTimeout);

            // ---- Step 5: 一键起飞 (只判断指令下发成功) ----
            step("一键起飞(参数取自 takeoff 配置节)");
            tookOff = true;   // 下发前置位: 回调超时不代表未生效, 收尾据此告警。
            FlightHubError takeoffErr =
                Precondition.takeoffResult(drone, params, timeout("takeoff_timeout_ms"));
            // 服务端以明确业务码拒绝 (serverCode != 0)时飞机确定未起飞, 复位 tookOff, 避免收尾
            // 对未起飞的飞机误报"可能仍在空中"。传输层歧义失败 (serverCode==0, 如回调超时)保持保守置位。
            // 对齐 DrcFlightScenario:328-330 的范围修正策略; DESIGN.md 4.5 声称的复位逻辑此前丢失。
            if (takeoffErr != null && !takeoffErr.isSuccess() && takeoffErr.getServerCode() != 0) {
                tookOff = false;
            }
            Errors.require("一键起飞", takeoffErr);

            // ---- Step 6: 等待爬升到目标高度 ----
            long climbWaitMs = cfg.longVal("climb_wait_ms", 15000L);
            step("等待爬升到目标高度(" + climbWaitMs + "ms, 简化为固定等待)");
            sleep(climbWaitMs);

            // ---- Step 7: 订阅 POI 任务事件 + 飞行进度 (先于进入, 不漏首个事件) ----
            step("订阅 POI 任务事件(listenPoiTaskEvent) + 飞行进度(listenFlightPoiProgress)");
            final Await<Boolean> firstEvent = new Await<Boolean>("poi.firstEvent");
            subs.add(subscribe("poiTaskEvent",
                poi.listenPoiTaskEvent(new FlightHubCallback.PoiTaskEventCallback() {
                    @Override
                    public void onResult(PoiTaskEvent event) {
                        if (event == null || event.getTaskData() == null) {
                            return;
                        }
                        PoiTaskData d = event.getTaskData();
                        DemoLog.info(name(), "[推送] listenPoiTaskEvent() #PoiTaskEvent,POI 任务事件: action=" + event.getAction()
                            + " uuid=" + d.getUuid()
                            + " status=" + d.getStatus() + "(" + statusText(d.getStatus()) + ")"
                            + " speed=" + d.getSpeed() + "m/s"
                            + " radius=" + d.getCircleRadius() + "m"
                            + " payload=" + d.getPayloadIndex()
                            + " errCode=" + d.getErrCode());
                        // 只跟踪本次进入的任务: 订阅时 SDK 会回放上一任务的缓存事件,
                        // uuid 过滤防止陈旧任务污染状态 (enter 回调返回前 uuid 尚空, 不误采)。
                        String myUuid = taskUuidRef.get();
                        if (!myUuid.isEmpty() && myUuid.equals(d.getUuid())) {
                            // 单调/终态粘滞更新 (对齐 FlyToScenario): 事件可能重复或乱序到达,
                            // 只接受严格大于当前值的状态; 一旦到达终态即固定不再更新。
                            int newStatus = d.getStatus();
                            int cur;
                            do {
                                cur = lastStatus.get();
                                if (isTerminal(cur) || newStatus <= cur) {
                                    break;
                                }
                            } while (!lastStatus.compareAndSet(cur, newStatus));
                            firstEvent.set(Boolean.TRUE);
                        }
                    }
                }),
                poi::cancelListenPoiTaskEvent));

            subs.add(subscribe("flightPoiProgress",
                poi.listenFlightPoiProgress(new FlightHubCallback.FlightPoiProgressCallback() {
                    @Override
                    public void onResult(FlightPoiProgress msg) {
                        if (msg == null) {
                            return;
                        }
                        DemoLog.info(name(), "[推送] listenFlightPoiProgress() #FlightPoiProgress,POI 飞行进度: uuid=" + msg.getUuid()
                            + " status=" + msg.getStatus()
                            + " speed=" + msg.getSpeed() + "m/s"
                            + " radius=" + msg.getCircle_radius() + "m"
                            + " payload=" + msg.getPayload_index()
                            + " errCode=" + msg.getErr_code());
                    }
                }),
                poi::cancelListenFlightPoiProgress));

            // ---- Step 8: 进入 POI 环绕模式 (参数取自 poi 配置节; 中心点坐标已在起飞前校验) ----
            step("进入 POI 环绕模式: lat=" + lat + " lon=" + lon
                + " height=" + cfg.dbl("height", 50) + " speed=" + cfg.dbl("speed", 3.0)
                + "m/s radius=" + cfg.dbl("circle_radius", 20.0) + "m");
            POIEnterParams enter = new POIEnterParams();
            enter.setPayloadIndex(payloadIndex);
            enter.setPoiCenterPoint(new Waypoint(lat, lon, cfg.dbl("height", 50)));
            enter.setSpeed(cfg.dbl("speed", 3.0));
            enter.setCircleRadius(cfg.dbl("circle_radius", 20.0));

            final Await<EnterResult> enterAwait = new Await<EnterResult>("poi.enter");
            enterAttempted = true;   // 下发即置位: 回调超时不代表任务未创建, 收尾据此兜底退出。
            poi.enter(enter, new FlightHubCallback.PoiEnterCallback() {
                @Override
                public void onResult(FlightHubError error, String taskUuid) {
                    // 回调原样双写 (控制台 + 日志文件): enter 是本场景的分水岭——后续「调速 / 查询 /
                    // 退出 / 收尾补退」全都依赖这里的 taskUuid 与错误码, 先把回调的原始值落一份,
                    // 便于 await 超时或后续步骤失败时对照排查 (此处只记录, 判定仍由主线程做)。
                    // 先 set 再打日志并采纳其返回值: Await 只认首个结果 (见 Await#set), 而本回调确实
                    // 可能被投递多次——PoiModule.enter 把整个下发过程包在一个 catch(Exception) 里,
                    // 异常路径会就地回调 (ERROR_OPERATION_FAILED, ""); 若异常发生在 enterWrapper 成功
                    // 注册原生回调【之后】(finally 里的 nativePOIEnterParams.delete() 抛出), 就会先来一条
                    // 空 taskUuid 的失败回调、随后再来一条真正的原生回调。第二次投递的值不会被主线程
                    // 使用, 照原样打印会留下「taskUuid 变了」的误导性记录, 故显式标注为已忽略。
                    boolean adopted = enterAwait.set(new EnterResult(error, taskUuid));
                    String line = (adopted ? "" : "[迟到/重复回调, 已忽略] ")
                        + "poi.enter(), [回调] PoiEnterCallback: taskUuid="
                        + "\"" + taskUuid + "\", "
                        + " error= { " + describeError(error) + " }";
                    DemoLog.info(name(), line);
                    console("   " + line);
                }
            });
            EnterResult entered = enterAwait.await(defaultTimeout);
            // 收尾补退的范围修正: 服务端以明确业务码拒绝 (serverCode!=0)时任务确定未创建, 复位标志;
            // 传输层歧义失败 (curl 失败/应答解析失败, serverCode==0)时任务可能已创建, 保持保守补退。
            if (entered.error != null && !entered.error.isSuccess() && entered.error.getServerCode() != 0) {
                enterAttempted = false;
            }
            requireSuccess("进入 POI", entered.error);
            taskUuidRef.set(entered.taskUuid != null ? entered.taskUuid : "");
            DemoLog.info(name(), "已进入 POI, taskUuid=" + taskUuidRef.get());

            // 等待首个本任务事件 (非致命: 收不到只说明推送链路未就绪, 不阻断流程)。
            try {
                firstEvent.await(timeout("poi_timeout_ms"));
                DemoLog.info(name(), "已收到首个任务事件, status=" + lastStatus.get()
                    + "(" + statusText(lastStatus.get()) + ")");
            } catch (FlightHubException e) {
                DemoLog.warn(name(), "未在超时内收到本任务事件(继续流程): " + Utils.describe(e));
            }

            // ---- Step 9: 更新环绕速度 (任务已终态则跳过——对已结束任务调速无对象) ----
            if (isTerminal(lastStatus.get())) {
                step("任务已到终态(" + statusText(lastStatus.get()) + "), 跳过更新环绕速度");
            } else {
                step("更新环绕速度 -> " + cfg.dbl("update_speed", 6.0) + "m/s");
                // 调速是纯演示性步骤 (它的成败不构成后续任何一步的判据), 与 Step 10 的查询同口径:
                // 失败只告警, 不阻断流程。否则一次可选动作失败会直穿 finally, 把 Step 11 退出 POI
                // 与 Step 12 返航一并跳过, 飞机被留在空中只收一条告警——代价与收益完全不成比例。
                try {
                    syncResult("更新环绕速度",
                        cb -> poi.updateSpeed(cfg.dbl("update_speed", 6.0), cb), defaultTimeout);
                } catch (Exception e) {
                    DemoLog.warn(name(), "更新环绕速度失败(不阻断流程): " + Utils.describe(e));
                }
            }

            // ---- Step 10: 观察环绕后查询任务信息 ----
            long observeMs = cfg.longVal("observe_ms", 10000L);
            step("观察环绕 " + observeMs + "ms 后查询任务信息(事件/进度见日志)");
            sleep(observeMs);
            // 查询是只读演示步骤: 失败 (网络抖动等)只告警, 不中断后续退出/返航主流程。
            try {
                queryTask(poi, defaultTimeout);
            } catch (Exception e) {
                DemoLog.warn(name(), "查询 POI 任务信息失败(不阻断流程): " + Utils.describe(e));
            }

            // ---- Step 11: 演示目标达成, 退出 POI ----
            if (isTerminal(lastStatus.get())) {
                step("任务已到终态(" + statusText(lastStatus.get()) + "), 无需退出 POI");
                exitConfirmed = true;   // 任务已结束, 收尾无需再补退出。
            } else {
                step("退出 POI 模式");
                syncResult("退出 POI", poi::exit, defaultTimeout);
                exitConfirmed = true;
            }

            // ---- Step 12: 返航 ----
            step("返航");
            call("返航", cb -> fc.returnHome(cb), defaultTimeout);
            returnedHome = true;

        } finally {
            // ---- 完备收尾: 每步各自兜异常, 一步失败不阻断后续 ----
            // 收尾标题打印单独兜底: step() 内部含 DemoLog + System.out, 两者故障 (如 stdout 被关闭、
            // OOM)会抛异常——若不兜住, 后续全部收尾步骤 (补退出 → 断 DRC → 释放控制权 → 取消监听)
            // 都会被跳过, 飞机可能处于失控飞行状态。与 FlyToScenario 同口径。
            quiet("打印收尾标题",
                () -> step("收尾：补退出 POI(如需) → 断 DRC → 释放控制权 → 取消监听"));

            // 1) 已进入且未确认退出、也未观察到终态 → 补一次退出。
            if (enterAttempted && !exitConfirmed && !isTerminal(lastStatus.get())) {
                quiet("补退出 POI", () -> syncResult("补退出 POI", poi::exit, defaultTimeout));
            }
            // 2) 断开 DRC 链路。
            if (drcConnectAttempted) {
                quiet("断开 DRC 链路", () -> syncResult("断开 DRC 链路", drc::disconnectDrcLink, defaultTimeout));
            }
            // 3) 释放负载控制权、飞行控制权 (获取动作已下发即尝试释放; 未实际持有时服务端报错, 无害)。
            if (payloadAuthAttempted) {
                quiet("释放负载控制权",
                    () -> call("释放负载控制权", cb -> auth.releasePayloadControlAuthority(payloadIndex, cb),
                        defaultTimeout));
            }
            if (flightAuthAttempted) {
                Precondition.releaseFlightControl(drone, defaultTimeout);   // 内部吞异常仅告警
            }
            // 4) 显式取消全部监听 (subscribe 已同步登记会话 registry, 双保险; close 幂等)。
            for (Subscription sub : subs) {
                sub.close();
            }
            DemoLog.info(name(), "已取消监听 " + subs.size() + " 路");
            // 5) 不自动返航: 异常路径退出且飞机可能仍在空中时仅告警 (与 FlightControlDemo 策略一致)。
            if (tookOff && !returnedHome) {
                DemoLog.warn(name(), "警告: 本场景已起飞但未执行到返航步骤, 飞机可能仍在空中悬停!"
                    + " 请经 FlightControlDemo [9] 返航或其他手段收回, 本场景不自动触发返航。");
            }
        }
    }

    /** 查询当前 POI 任务信息并全字段落日志文件。 */
    private void queryTask(PoiModule poi, long timeoutMs) {
        final Await<TaskResult> await = new Await<TaskResult>("poi.getTask");
        poi.getTask(new FlightHubCallback.PoiGetTaskCallback() {
            @Override
            public void onResult(FlightHubError error, POITaskInfo task) {
                // 回调原样双写 (控制台 + 日志文件): 与 enter 同口径, 先落一条「回调确实回来了、错误码
                // 是什么」的原始记录——否则查询失败时控制台只有主线程那句告警, 分不清是回调根本没到
                // (超时)还是回调带着错误码返回。任务详情不在此展开: 全字段由下方主线程逐行落日志文件
                // (见本方法 javadoc), 这里只回显一行摘要, 避免控制台被长清单刷屏。
                // 采纳 set() 返回值的写法与 enter 保持一致; 但 PoiModule.getTask 的 try 块里
                // getTaskWrapper(swigCallback) 之后没有其它语句 (不像 enter 还要在 finally 里 delete()
                // 参数对象), 未见与 enter 同类的确定性双投递路径——此处的「已忽略」标注属防御性写法,
                // 不代表已知缺陷。
                boolean adopted = await.set(new TaskResult(error, task));
                String line = (adopted ? "" : "[迟到/重复回调, 已忽略] ")
                    + "poi.getTask(), [回调] PoiGetTaskCallback: hasTask=" + (task != null)
                    + ", error= { " + describeError(error) + " }";
                DemoLog.info(name(), line);
                console("   " + line);
            }
        });
        TaskResult r = await.await(timeoutMs);
        requireSuccess("查询 POI 任务", r.error);
        POITaskInfo t = r.task;
        if (t == null) {
            DemoLog.warn(name(), "查询 POI 任务成功但任务信息为空");
            return;
        }
        DemoLog.info(name(), "当前 POI 任务信息:");
        DemoLog.debug(name(), "  uuid=" + t.getUuid() + " sn=" + t.getSn() + " deviceSn=" + t.getDeviceSn());
        DemoLog.info(name(), "  status=" + t.getTaskStatus() + " errCode=" + t.getErrCode());
        if (t.getPoiCenterPoint() != null) {
            DemoLog.info(name(), "  center: lat=" + t.getPoiCenterPoint().getLatitude()
                + " lon=" + t.getPoiCenterPoint().getLongitude()
                + " height=" + t.getPoiCenterPoint().getHeight());
        }
        DemoLog.info(name(), "  speed=" + t.getSpeed() + "m/s radius=" + t.getCircleRadius()
            + "m payload=" + t.getPayloadIndex());
    }

    // syncResult 已提升至 Scenario 基类, console / quiet 已提升至 ModuleDemo 基类, 子类直接调用。

    /** 任务是否已到终态: 1=启动失败 4=终止 5=成功 7=超时 (见 {@link POITaskStatus})。 */
    private static boolean isTerminal(int status) {
        POITaskStatus st = POITaskStatus.fromValue(status);
        return st == POITaskStatus.STARTING_FAILURE || st == POITaskStatus.TERMINATED
            || st == POITaskStatus.SUCCESS || st == POITaskStatus.TIMEOUT;
    }

    /** 任务状态文字 (枚举名; 尚未收到事件显示「尚无事件」)。 */
    private static String statusText(int status) {
        if (status == STATUS_UNKNOWN) {
            return "尚无事件";
        }
        POITaskStatus st = POITaskStatus.fromValue(status);
        return st != null ? st.name() : "未知(" + status + ")";
    }

    private static final class EnterResult {
        final FlightHubError error;
        final String taskUuid;

        EnterResult(FlightHubError error, String taskUuid) {
            this.error = error;
            this.taskUuid = taskUuid;
        }
    }

    private static final class TaskResult {
        final FlightHubError error;
        final POITaskInfo task;

        TaskResult(FlightHubError error, POITaskInfo task) {
            this.error = error;
            this.task = task;
        }
    }
}
