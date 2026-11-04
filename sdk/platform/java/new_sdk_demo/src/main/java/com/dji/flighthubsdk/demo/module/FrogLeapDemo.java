// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.module;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.CreateTaskReq;
import com.dji.flighthubsdk.bean.CreateTaskResp;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.ReturnHomeInfoEvent;
import com.dji.flighthubsdk.bean.TaskExceptionEvent;
import com.dji.flighthubsdk.bean.WaylineTaskData;
import com.dji.flighthubsdk.bean.WaylineTaskEvent;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.FlightControlModule;
import com.dji.flighthubsdk.core.FlightTaskControlModule;
import com.dji.flighthubsdk.core.FlightTaskModule;
import com.dji.flighthubsdk.demo.base.ModuleDemo;
import com.dji.flighthubsdk.demo.runtime.Await;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.runtime.FlightHubException;
import com.dji.flighthubsdk.demo.runtime.Node;
import com.dji.flighthubsdk.demo.runtime.Subscription;
import com.dji.flighthubsdk.demo.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 蛙跳(A 起 B 降)任务模块 Demo(菜单驱动)：创建蛙跳任务 + 三类事件监听 + 指定机场返航。
 *
 * <p>覆盖蛙跳任务的 5 个核心能力, 按"创建 / 监听 / 控制"分组：
 *
 * <pre>
 * 主菜单
 * ├── [1] 创建蛙跳任务        FlightTaskModule.createFlightTask
 * ├── [2] 订阅飞行任务状态    FlightTaskControlModule.listenWaylineTaskEvent
 * ├── [3] 订阅多机场返航信息  FlightTaskControlModule.listenReturnHomeInfoEventByDevice(deviceSn, cb)
 * ├── [4] 订阅任务异常        FlightTaskControlModule.listenTaskExceptionEvent
 * ├── [5] 指定机场返航        FlightControlModule.returnHome(targetDockSn, cb)
 * └── [6] 取消全部监听        对应 cancelListenXxx
 * </pre>
 *
 * <p><b>SDK 入口：</b>
 * <ul>
 *   <li>任务创建：{@code session.workspace().getFlightTaskModule()}(挂在 Workspace 上)；</li>
 *   <li>任务事件：{@code session.drone().getFlightTaskControlModule()}；</li>
 *   <li>飞行控制：{@code session.drone().getFlightControlModule()}。</li>
 * </ul>
 *
 * <p><b>蛙跳参数语义(易错点)：</b>
 * <ul>
 *   <li>起飞机场 A = 当前会话所选设备 SN, 由 {@code session.topo().getDevice().getDeviceSn()} 取得；</li>
 *   <li>降落机场 B = {@code frog_leap.landing_dock_sn}, 用于创建任务的 {@code landing_dock_sn}；</li>
 *   <li>指定返航目标 = {@code frog_leap.return_home_dock_sn}, 下发时落在 {@code data.home_dock_sn}；</li>
 *   <li>两者可相同也可不同, 但配置上必须拆开, 避免"创建任务的降落点"与"临时指定返航点"被混用。</li>
 * </ul>
 *
 * <p><b>接口串联设计：</b>
 * <ul>
 *   <li>建议先执行 [2][3][4] 打开监听, 再执行 [1] 创建任务, 这样能完整看到任务状态与返航信息推送；</li>
 *   <li>[3] 的订阅 SN 取 {@code frog_leap.landing_dock_sn}, 与后端多机场返航信息的路由 SN 对齐,
 *       因此无需为了收推送而把会话设备切到降落机场；</li>
 *   <li>[3] 用的是 {@code listenReturnHomeInfoEventByDevice}(按 SN 订阅, <b>不去重</b>,
 *       每条推送都回调), 与 {@code listenReturnHomeInfoEvent}(网关级订阅, <b>内容去重</b>)
 *       是两套独立接口, 监听 ID 空间互不相通, 取消时必须配对调用 {@code cancelListenReturnHomeInfoEventByDevice}；</li>
 *   <li>[5] 走 HTTP 非实时命令({@code method=return_specific_home}), 不依赖 DRC 链路,
 *       未建立 DRC 时同样可下发。</li>
 * </ul>
 *
 * <p><b>设计要点：</b>
 * <ul>
 *   <li>菜单项之间相互独立, 监听建立后常驻, 可继续执行其它菜单项；</li>
 *   <li>所有推送回调同时输出到控制台与 {@link DemoLog}, 便于边测边留证据；</li>
 *   <li>[2][3][4] 的订阅登记到 {@link #monitorSubs}, [6] 可主动清理,
 *       run() 的 finally 兜底退订, 避免退出后残留订阅。</li>
 * </ul>
 */
public class FrogLeapDemo extends ModuleDemo {

    /** [2][3][4] 建立的事件订阅;[6] 主动清理, run() 退出时 finally 兜底退订。 */
    private final List<Subscription> monitorSubs = new ArrayList<Subscription>();
    /** [1] 最近一次创建成功的蛙跳任务 UUID, 便于对照后端推送中的 flight_id。 */
    private String lastTaskUuid = "";

    @Override
    public String name() {
        return "FrogLeapDemo";
    }

    @Override
    protected void run() throws Exception {
        if (input == null) {
            DemoLog.warn(name(), "无交互输入(input=null), 无法运行菜单式蛙跳 demo");
            return;
        }
        if (!session.hasDock()) {
            hint("该设备不是机场, 无法执行蛙跳任务 demo");
            return;
        }

        final Drone drone = session.drone();
        final FlightTaskModule ftm = session.workspace().getFlightTaskModule();
        final FlightTaskControlModule ctrl = drone.getFlightTaskControlModule();
        final FlightControlModule fc = drone.getFlightControlModule();
        if (ftm == null || ctrl == null || fc == null) {
            hint("获取模块失败: ftm=" + (ftm != null) + " ctrl=" + (ctrl != null) + " fc=" + (fc != null));
            return;
        }

        final long timeoutMs = defaultTimeout();
        try {
            while (true) {
                Utils.printTitle("执行 " + name());
                System.out.print(menu());
                int choice = Utils.readIntInRange(input, "请选择(1-6, q 退出): ", 1, 6);
                if (choice == Utils.QUIT) {
                    DemoLog.info(name(), "退出蛙跳任务菜单");
                    break;
                }
                try {
                    switch (choice) {
                        case 1: createFrogLeapTask(ftm, timeoutMs); break;
                        case 2: listenWaylineTaskEvent(ctrl); break;
                        case 3: listenReturnHomeInfoEvent(ctrl); break;
                        case 4: listenTaskExceptionEvent(ctrl); break;
                        case 5: returnSpecificHome(fc, timeoutMs); break;
                        case 6: closeSubscriptions(); break;
                        default: break;
                    }
                } catch (Exception e) {
                    console("   结果 -> [异常] " + Utils.describe(e));
                    DemoLog.error(name(), "菜单动作执行异常: " + Utils.describe(e), e);
                }
            }
        } finally {
            closeSubscriptions();
        }
    }

    private void createFrogLeapTask(FlightTaskModule ftm, long timeoutMs) {
        Node cfg = params.node("frog_leap");
        String waylineUuid = cfg.str("wayline_uuid", "");
        if (waylineUuid.isEmpty()) {
            hint("frog_leap.wayline_uuid 未配置");
            return;
        }
        String landingDockSn = cfg.str("landing_dock_sn", "");
        if (landingDockSn.isEmpty()) {
            landingDockSn = Utils.readLine(input, "请输入目标降落机场 SN: ");
        }
        if (landingDockSn == null || landingDockSn.isEmpty()) {
            hint("未提供目标降落机场 SN");
            return;
        }

        CreateTaskReq req = new CreateTaskReq();
        req.setName(cfg.str("name", "frog-leap-task-" + System.currentTimeMillis()));
        req.setSn(session.topo().getDevice().getDeviceSn());
        req.setLandingDockSn(landingDockSn);
        req.setWaylineUuid(waylineUuid);
        req.setTaskType(cfg.intVal("task_type", 1));
        req.setTimeZone(cfg.str("time_zone", "Asia/Shanghai"));
        req.setOutOfControlActionInFlight(cfg.intVal("out_of_control_action", 1));
        req.setRthAltitude(cfg.dbl("rth_altitude", 100));

        if (req.getTaskType() == 2) {
            long beginAt = cfg.longVal("begin_at", 0);
            long endAt = cfg.longVal("end_at", 0);
            if (beginAt == 0) {
                long beginDelayMinutes = Math.max(2L, cfg.longVal("begin_delay_minutes", 2L));
                beginAt = System.currentTimeMillis() / 1000 + beginDelayMinutes * 60;
            }
            if (endAt == 0) {
                endAt = beginAt + 3600;
            }
            req.setBeginAt(beginAt);
            req.setEndAt(endAt);
        }

        console("\n   创建蛙跳任务参数:");
        console("     sn=" + req.getSn() + " landingDockSn=" + req.getLandingDockSn());
        console("     waylineUuid=" + req.getWaylineUuid() + " taskType=" + req.getTaskType());

        final Await<FlightHubError> await = new Await<FlightHubError>(name() + ".createFrogLeapTask");
        final AtomicReference<CreateTaskResp> ref = new AtomicReference<CreateTaskResp>();
        ftm.createFlightTask(req, new FlightHubCallback.CreateFlightTaskCallback() {
            @Override
            public void onResult(FlightHubError error, CreateTaskResp result) {
                ref.set(result);
                await.set(error);
            }
        });

        FlightHubError err = await.await(timeoutMs);
        requireSuccess("创建蛙跳任务", err);
        CreateTaskResp resp = ref.get();
        lastTaskUuid = resp != null ? resp.getUuid() : "";
        console("   结果 -> [成功] uuid=" + lastTaskUuid
                + " waylineValidityCheckCodes=" + (resp != null ? resp.getWaylineValidityCheckCodes() : null));
        DemoLog.info(name(), "创建蛙跳任务成功, uuid=" + lastTaskUuid);
    }

    private void listenWaylineTaskEvent(FlightTaskControlModule ctrl) {
        safeSubscribe("waylineTaskEvent",
                ctrl.listenWaylineTaskEvent(new FlightHubCallback.WaylineTaskEventCallback() {
                    @Override
                    public void onResult(WaylineTaskEvent event) {
                        if (event == null || event.getTaskData() == null) {
                            return;
                        }
                        WaylineTaskData d = event.getTaskData();
                        String msg = "[推送] listenWaylineTaskEvent: action=" + event.getAction()
                                + " uuid=" + d.getUuid()
                                + " status=" + d.getStatus()
                                + " waypoint=" + d.getCurrentWaypointIndex() + "/" + d.getTotalWaypoints()
                                + " sn=" + d.getSn();
                        DemoLog.info(name(), msg);
                        console("   " + msg);
                    }
                }),
                ctrl::cancelListenWaylineTaskEvent);
    }

    private void listenReturnHomeInfoEvent(FlightTaskControlModule ctrl) {
        String subscribeDockSn = params.node("frog_leap").str("landing_dock_sn", "");
        if (subscribeDockSn.isEmpty()) {
            subscribeDockSn = Utils.readLine(input, "请输入返航信息订阅机场 SN(默认使用 landing_dock_sn): ");
        }
        if (subscribeDockSn == null || subscribeDockSn.isEmpty()) {
            hint("未提供返航信息订阅机场 SN");
            return;
        }
        final String target = subscribeDockSn;
        safeSubscribe("returnHomeInfoEventByDevice(" + target + ")",
                ctrl.listenReturnHomeInfoEventByDevice(target, new FlightHubCallback.ReturnHomeInfoEventCallback() {
                    @Override
                    public void onResult(ReturnHomeInfoEvent event) {
                        if (event == null) {
                            return;
                        }
                        String msg = "[推送] listenReturnHomeInfoEventByDevice: flightId=" + event.getFlightId()
                                + " sn=" + event.getSn()
                                + " homeDockSn=" + (event.getInfo() != null ? event.getInfo().getHomeDockSn() : "")
                                + " lastPointType=" + (event.getInfo() != null ? event.getInfo().getLastPointType() : -1)
                                + " plannedPathPoints=" + (event.getInfo() != null ? event.getInfo().getPlannedPathPoints().size() : 0)
                                + " multiDockHomeInfo=" + (event.getInfo() != null ? event.getInfo().getMultiDockHomeInfo() : null);
                        DemoLog.info(name(), msg);
                        console("   " + msg);
                    }
                }),
                ctrl::cancelListenReturnHomeInfoEventByDevice);
    }

    private void listenTaskExceptionEvent(FlightTaskControlModule ctrl) {
        safeSubscribe("taskExceptionEvent",
                ctrl.listenTaskExceptionEvent(new FlightHubCallback.TaskExceptionEventCallback() {
                    @Override
                    public void onResult(TaskExceptionEvent event) {
                        if (event == null) {
                            return;
                        }
                        String msg = "[推送] listenTaskExceptionEvent: uuid=" + event.getUuid()
                                + " name=" + event.getName()
                                + " sn=" + event.getSn()
                                + " status=" + event.getStatus()
                                + " exceptions=" + event.getExceptions();
                        DemoLog.info(name(), msg);
                        console("   " + msg);
                    }
                }),
                ctrl::cancelListenTaskExceptionEvent);
    }

    private void returnSpecificHome(FlightControlModule fc, long timeoutMs) {
        String targetDockSn = params.node("frog_leap").str("return_home_dock_sn", "");
        if (targetDockSn.isEmpty()) {
            targetDockSn = Utils.readLine(input, "请输入目标返航机场 SN: ");
        }
        if (targetDockSn == null || targetDockSn.isEmpty()) {
            hint("未提供目标返航机场 SN");
            return;
        }
        final String target = targetDockSn;
        call("指定机场返航", cb -> fc.returnHome(target, cb), timeoutMs);
    }

    private void closeSubscriptions() {
        if (monitorSubs.isEmpty()) {
            hint("当前没有模块级监听");
            return;
        }
        for (Subscription sub : monitorSubs) {
            sub.close();
        }
        console("   已取消监听 " + monitorSubs.size() + " 路");
        monitorSubs.clear();
    }

    private void safeSubscribe(String label, int listenId, Subscription.Canceller canceller) {
        try {
            monitorSubs.add(subscribe(label, listenId, canceller));
            console("   已订阅 " + label + "，监听保持中；可继续选择其它菜单项");
        } catch (FlightHubException e) {
            hint("订阅失败(跳过): " + label + ", " + Utils.describe(e));
        }
    }

    private String menu() {
        return "\n"
                + "  [1] 创建蛙跳任务\n"
                + "  [2] 订阅飞行任务状态\n"
                + "  [3] 订阅多机场返航信息\n"
                + "  [4] 订阅任务异常\n"
                + "  [5] 指定机场返航\n"
                + "  [6] 取消全部监听\n";
    }
}
