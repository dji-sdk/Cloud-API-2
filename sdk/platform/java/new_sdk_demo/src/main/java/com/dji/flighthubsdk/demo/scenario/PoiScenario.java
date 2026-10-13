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
 * 场景：兴趣点环绕(POI)完整流程——控制权/DRC 前置 → 一键起飞 → 进入 POI → 监听/调速/查询 →
 * 退出 POI → 返航 → 完备收尾。
 *
 * <p>该场景演示了兴趣点环绕(POI)的完整流程：获取飞行控制权与负载控制权 → 建立 DRC 链路 →
 * 一键起飞 → 建立基础监听 → 进入 POI 环绕模式 → 更新速度 → 查询任务信息 → 退出 POI → 返航。
 * 步骤：
 * <ol>
 *   <li>基础监听：飞行/负载控制权状态(先订阅, 便于观察后续动作引起的变化);</li>
 *   <li>获取飞行控制权;</li>
 *   <li>获取负载控制权(POI 进入参数携带 payloadIndex, 环绕中云台指向中心点);</li>
 *   <li>建立 DRC 链路(基础链路前置);</li>
 *   <li>一键起飞(参数取自 {@code takeoff} 配置节, 只判断指令下发成功);</li>
 *   <li>等待爬升到目标高度({@code poi.climb_wait_ms}, 简化为固定等待——严谨做法可订阅 OSD 高度);</li>
 *   <li>订阅 POI 任务事件({@code listenPoiTaskEvent})与 POI 飞行进度({@code listenFlightPoiProgress})
 *       ——先于进入建立, 不漏首个事件; 事件按本任务 uuid 过滤(订阅时 SDK 会回放上一任务的缓存事件);</li>
 *   <li>进入 POI 环绕模式(中心点/速度/半径取自 {@code poi} 配置节), 等待首个本任务事件
 *       (超时 {@code poi_timeout_ms});</li>
 *   <li>更新环绕速度({@code poi.update_speed}; 任务已终态则跳过);</li>
 *   <li>观察环绕 {@code poi.observe_ms} 后查询当前 POI 任务信息({@code getTask}, 全字段落日志文件);</li>
 *   <li>演示目标达成, 退出 POI 模式;</li>
 *   <li>返航。</li>
 * </ol>
 *
 * <p>POI 任务状态(见 {@link POITaskStatus}): 0=待开始 1=启动失败 2=执行中 3=已暂停 4=终止
 * 5=成功 6=挂起 7=超时 8=部分执行 9=准备中 10=起飞排队; 其中 1/4/5/7 视为终态——
 * 事件已见终态时跳过退出(任务已结束, 退出无对象)。
 *
 * <p><b>收尾保证(finally, 每步各自兜异常不互相阻断)</b>：
 * <ol>
 *   <li>若已进入 POI 且未确认退出、任务未到终态——补一次退出(含 enter 超时后任务实际已建的竞态:
 *       下发即置 {@code enterAttempted}; 服务端以明确业务码拒绝(serverCode!=0)时任务确定未创建,
 *       复位标志不做无谓补退);</li>
 *   <li>断开 DRC 链路;</li>
 *   <li>释放负载控制权、释放飞行控制权(获取类动作按「下发即可能生效」处理, 超时也照常释放);</li>
 *   <li>显式取消全部监听(订阅同时登记在会话 registry, 双保险; {@code Subscription.close} 幂等);</li>
 *   <li><b>不自动返航/降落</b>——返航是主流程步骤; 异常路径退出时若飞机可能仍在空中仅告警提醒
 *       (与 FlightControlDemo 的策略一致: demo 静默触发返航比把决定留给操作者更危险)。
 *       <b>与 DrcFlightScenario 的策略对比</b>：DrcFlightScenario 收尾时会自动补发返航,
 *       原因是该场景全程处于「持续 DRC 手动操控」语境, 用户对飞机状态持续可见、可控, 断链后自动
 *       补一次返航是让飞机脱离无人操控状态的合理默认动作；而 POI 环绕等自动化任务场景里,
 *       用户可能并未持续盯盘, 服务端对当前空域/避障状态的判断比 demo 本地静默触发更可靠, 因此
 *       选择保守的「仅告警、把决定权交给操作者」。两种策略均为有意设计, 非缺陷, 各自适配不同的
 *       人机交互假设, 使用者接入生产环境时应按自身场景重新评估该取舍。</li>
 * </ol>
 *
 * <p>所有参数来自 demo_params.json 的 {@code poi}/{@code takeoff} 分组, 无手动交互输入;
 * 监听推送只落日志文件。
 */
public class PoiScenario extends Scenario {

    /** 任务状态未知(尚未收到本任务事件)。 */
    private static final int STATUS_UNKNOWN = -1;

    @Override
    public String name() {
        return "PoiScenario";
    }

    @Override
    protected void steps() throws Exception {
        final Drone drone = session.drone();

        // 模块 getter 在 SDK 内部失败时会返回 null, 先集中判空给出诊断。
        // 起飞/返航模块也在此判(getter 结果有缓存): 「没有返航手段」必须在起飞前 fail-fast,
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

        // 收尾判断用状态(「下发即可能生效」语义: 在动作下发前置位, 不依赖回调按时返回)。
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
            // ---- Step 1: 基础监听(推送只落日志文件) ----
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

            // ---- Step 5: 一键起飞(只判断指令下发成功) ----
            step("一键起飞(参数取自 takeoff 配置节)");
            tookOff = true;
            Precondition.takeoff(drone, params, timeout("takeoff_timeout_ms"));

            // ---- Step 6: 等待爬升到目标高度 ----
            long climbWaitMs = cfg.longVal("climb_wait_ms", 15000L);
            step("等待爬升到目标高度(" + climbWaitMs + "ms, 简化为固定等待)");
            sleep(climbWaitMs);

            // ---- Step 7: 订阅 POI 任务事件 + 飞行进度(先于进入, 不漏首个事件) ----
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
                            // uuid 过滤防止陈旧任务污染状态(enter 回调返回前 uuid 尚空, 不误采)。
                            String myUuid = taskUuidRef.get();
                            if (!myUuid.isEmpty() && myUuid.equals(d.getUuid())) {
                                lastStatus.set(d.getStatus());
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
                                    + " status=" + msg.getStatus() + "(" + statusText(msg.getStatus()) + ")"
                                    + " speed=" + msg.getSpeed() + "m/s"
                                    + " radius=" + msg.getCircle_radius() + "m"
                                    + " payload=" + msg.getPayload_index()
                                    + " errCode=" + msg.getErr_code());
                        }
                    }),
                    poi::cancelListenFlightPoiProgress));

            // ---- Step 8: 进入 POI 环绕模式(参数取自 poi 配置节) ----
            // 安全校验：POI 中心点 latitude/longitude 是飞行目标, 缺失时 Node 默认 0.0 会让飞机
            // 飞向赤道，必须在进入 POI 前 fail-fast。
            if (!cfg.has("latitude") || !cfg.has("longitude")) {
                throw new FlightHubException(
                    "poi 配置节缺少必填字段: latitude=" + cfg.has("latitude")
                    + " longitude=" + cfg.has("longitude")
                    + " — 请在 demo_params.json 的 poi 节填写目标坐标");
            }
            double lat = cfg.dbl("latitude", 0);
            double lon = cfg.dbl("longitude", 0);
            // 二次校验：(0.0, 0.0) 是 demo_params.json 中的占位默认值, 对应几内亚湾,
            // 拦截并强制要求配置真实坐标。
            if (lat == 0.0 && lon == 0.0) {
                throw new FlightHubException(
                    "poi 的 latitude/longitude 均为 0.0 — 这是 demo_params.json 的占位默认值,"
                    + " 请填写当前测试场地的真实 GPS 坐标后重新运行");
            }
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
                    enterAwait.set(new EnterResult(error, taskUuid));
                }
            });
            EnterResult entered = enterAwait.await(defaultTimeout);
            // 收尾补退的范围修正: 服务端以明确业务码拒绝(serverCode!=0)时任务确定未创建, 复位标志;
            // 传输层歧义失败(curl 失败/应答解析失败, serverCode==0)时任务可能已创建, 保持保守补退。
            if (entered.error != null && !entered.error.isSuccess() && entered.error.getServerCode() != 0) {
                enterAttempted = false;
            }
            requireSuccess("进入 POI", entered.error);
            taskUuidRef.set(entered.taskUuid != null ? entered.taskUuid : "");
            DemoLog.info(name(), "已进入 POI, taskUuid=" + taskUuidRef.get());

            // 等待首个本任务事件(非致命: 收不到只说明推送链路未就绪, 不阻断流程)。
            try {
                firstEvent.await(timeout("poi_timeout_ms"));
                DemoLog.info(name(), "已收到首个任务事件, status=" + lastStatus.get()
                        + "(" + statusText(lastStatus.get()) + ")");
            } catch (FlightHubException e) {
                DemoLog.warn(name(), "未在超时内收到本任务事件(继续流程): " + Utils.describe(e));
            }

            // ---- Step 9: 更新环绕速度(任务已终态则跳过——对已结束任务调速无对象) ----
            if (isTerminal(lastStatus.get())) {
                step("任务已到终态(" + statusText(lastStatus.get()) + "), 跳过更新环绕速度");
            } else {
                step("更新环绕速度 -> " + cfg.dbl("update_speed", 6.0) + "m/s");
                syncResult("更新环绕速度",
                        cb -> poi.updateSpeed(cfg.dbl("update_speed", 6.0), cb), defaultTimeout);
            }

            // ---- Step 10: 观察环绕后查询任务信息 ----
            long observeMs = cfg.longVal("observe_ms", 10000L);
            step("观察环绕 " + observeMs + "ms 后查询任务信息(事件/进度见日志)");
            sleep(observeMs);
            // 查询是只读演示步骤: 失败(网络抖动等)只告警, 不中断后续退出/返航主流程。
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
            step("收尾：补退出 POI(如需) → 断 DRC → 释放控制权 → 取消监听");

            // 1) 已进入且未确认退出、也未观察到终态 → 补一次退出。
            if (enterAttempted && !exitConfirmed && !isTerminal(lastStatus.get())) {
                quiet("补退出 POI", () -> syncResult("补退出 POI", poi::exit, defaultTimeout));
            }
            // 2) 断开 DRC 链路。
            if (drcConnectAttempted) {
                quiet("断开 DRC 链路", () -> syncResult("断开 DRC 链路", drc::disconnectDrcLink, defaultTimeout));
            }
            // 3) 释放负载控制权、飞行控制权(获取动作已下发即尝试释放; 未实际持有时服务端报错, 无害)。
            if (payloadAuthAttempted) {
                quiet("释放负载控制权",
                        () -> call("释放负载控制权", cb -> auth.releasePayloadControlAuthority(payloadIndex, cb),
                                defaultTimeout));
            }
            if (flightAuthAttempted) {
                Precondition.releaseFlightControl(drone, defaultTimeout);   // 内部吞异常仅告警
            }
            // 4) 显式取消全部监听(subscribe 已同步登记会话 registry, 双保险; close 幂等)。
            for (Subscription sub : subs) {
                sub.close();
            }
            DemoLog.info(name(), "已取消监听 " + subs.size() + " 路");
            // 5) 不自动返航: 异常路径退出且飞机可能仍在空中时仅告警(与 FlightControlDemo 策略一致)。
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
                await.set(new TaskResult(error, task));
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
        DemoLog.info(name(), "  uuid=" + t.getUuid() + " sn=" + t.getSn() + " deviceSn=" + t.getDeviceSn());
        DemoLog.info(name(), "  status=" + t.getTaskStatus() + " errCode=" + t.getErrCode());
        if (t.getPoiCenterPoint() != null) {
            DemoLog.info(name(), "  center: lat=" + t.getPoiCenterPoint().getLatitude()
                    + " lon=" + t.getPoiCenterPoint().getLongitude()
                    + " height=" + t.getPoiCenterPoint().getHeight());
        }
        DemoLog.info(name(), "  speed=" + t.getSpeed() + "m/s radius=" + t.getCircleRadius()
                + "m payload=" + t.getPayloadIndex());
    }

    // syncResult / quiet 已提升至 Scenario 基类，子类直接调用 syncResult(...) 和 quiet(...)。

    /** 任务是否已到终态: 1=启动失败 4=终止 5=成功 7=超时(见 {@link POITaskStatus})。 */
    private static boolean isTerminal(int status) {
        POITaskStatus st = POITaskStatus.fromValue(status);
        return st == POITaskStatus.STARTING_FAILURE || st == POITaskStatus.TERMINATED
                || st == POITaskStatus.SUCCESS || st == POITaskStatus.TIMEOUT;
    }

    /** 任务状态文字(枚举名; 尚未收到事件显示「尚无事件」)。 */
    private static String statusText(int status) {
        return status == STATUS_UNKNOWN ? "尚无事件" : POITaskStatus.fromValue(status).name();
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
