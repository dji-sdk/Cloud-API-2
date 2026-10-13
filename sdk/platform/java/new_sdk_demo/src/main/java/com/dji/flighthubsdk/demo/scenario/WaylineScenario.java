// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.scenario;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.CreateTaskReq;
import com.dji.flighthubsdk.bean.CreateTaskResp;
import com.dji.flighthubsdk.bean.TaskDetail;
import com.dji.flighthubsdk.bean.WaylineTaskData;
import com.dji.flighthubsdk.bean.WaylineTaskEvent;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.FlightControlModule;
import com.dji.flighthubsdk.core.FlightTaskControlModule;
import com.dji.flighthubsdk.core.FlightTaskModule;
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
 * 场景：航线飞行控制——创建任务 → 事件监听 → 暂停 → 恢复 → 返航。
 *
 * <p>步骤：
 * <ol>
 *   <li>按 {@code flight_task} 配置创建一个新任务;</li>
 *   <li>订阅任务事件(listenWaylineTaskEvent);</li>
 *   <li>暂停任务(pause);</li>
 *   <li>等待后恢复任务(resume);</li>
 *   <li>观察任务事件;</li>
 *   <li>返航(returnHome) + 停止任务。</li>
 * </ol>
 */
public class WaylineScenario extends Scenario {

    private static final int STATUS_UNKNOWN = -1;

    @Override
    public String name() {
        return "WaylineScenario";
    }

    @Override
    protected void steps() throws Exception {
        final Drone drone = session.drone();
        final FlightTaskModule ftm = session.workspace().getFlightTaskModule();
        final FlightTaskControlModule ctrl = drone.getFlightTaskControlModule();
        final FlightControlModule fc = drone.getFlightControlModule();
        if (ftm == null || ctrl == null || fc == null) {
            throw new FlightHubException("获取模块失败: ftm=" + (ftm != null)
                    + " ctrl=" + (ctrl != null) + " fc=" + (fc != null));
        }

        final long defaultTimeout = defaultTimeout();
        final Node cfg = params.node("wayline");

        boolean stopConfirmed = false;
        final AtomicReference<String> taskUuidRef = new AtomicReference<String>("");
        final AtomicInteger lastStatus = new AtomicInteger(STATUS_UNKNOWN);
        final List<Subscription> subs = new ArrayList<Subscription>();

        try {
            // ---- Step 1: 创建任务并等待进入运行态 ----
            step("创建飞行计划, 等待任务进入运行中或暂停中状态");
            TaskDetail picked = createAndAwaitTask(ftm, defaultTimeout, cfg);
            taskUuidRef.set(picked.getUuid());
            lastStatus.set(picked.getStatus());
            DemoLog.info(name(), "选中任务: uuid=" + picked.getUuid()
                    + " name=" + picked.getName()
                    + " status=" + picked.getStatus() + "(" + statusText(picked.getStatus()) + ")"
                    + " sn=" + picked.getSn());
            step("选中任务: " + picked.getName() + " (status=" + statusText(picked.getStatus()) + ")");

            // ---- Step 2: 订阅任务事件 ----
            step("订阅航线任务事件(listenWaylineTaskEvent)");
            subs.add(subscribe("waylineTaskEvent",
                    ctrl.listenWaylineTaskEvent(new FlightHubCallback.WaylineTaskEventCallback() {
                        @Override
                        public void onResult(WaylineTaskEvent event) {
                            if (event == null || event.getTaskData() == null) {
                                return;
                            }
                            WaylineTaskData d = event.getTaskData();
                            DemoLog.info(name(), "[推送] listenWaylineTaskEvent() #WaylineTaskEvent,航线任务事件: action=" + event.getAction()
                                    + " uuid=" + d.getUuid()
                                    + " status=" + d.getStatus() + "(" + statusText(d.getStatus()) + ")"
                                    + " name=" + d.getName()
                                    + " currentWaypoint=" + d.getCurrentWaypointIndex()
                                    + "/" + d.getTotalWaypoints()
                                    + " sn=" + d.getSn());
                            String myUuid = taskUuidRef.get();
                            if (!myUuid.isEmpty() && myUuid.equals(d.getUuid())) {
                                lastStatus.set(d.getStatus());
                            }
                        }
                    }),
                    ctrl::cancelListenWaylineTaskEvent));

            // ---- Step 3: 用户确认后暂停 ----
            if (isTerminal(lastStatus.get())) {
                step("任务已到终态, 跳过暂停");
            } else {
                Utils.readLine(input, "   任务已起飞, 按回车暂停任务...");
                step("暂停航线任务(pause)");
                call("暂停任务", ctrl::pause, defaultTimeout);
            }

            // ---- Step 4: 用户确认后恢复 ----
            if (isTerminal(lastStatus.get())) {
                step("任务已到终态, 跳过恢复");
            } else {
                Utils.readLine(input, "   按回车恢复任务...");
                step("恢复航线任务(resume)");
                call("恢复任务", ctrl::resume, defaultTimeout);
            }

            // ---- Step 5: 用户确认后返航 ----
            if (isTerminal(lastStatus.get())) {
                step("任务已到终态(" + statusText(lastStatus.get()) + "), 无需返航");
                stopConfirmed = true;
            } else {
                Utils.readLine(input, "   按回车返航...");
                step("返航(returnHome)");
                call("返航", fc::returnHome, defaultTimeout);
                step("停止任务(updateFlightTaskStatus -> 6=stop)");
                call("停止任务", cb -> ftm.updateFlightTaskStatus(taskUuidRef.get(), 6, cb),
                        defaultTimeout);
                stopConfirmed = true;
            }

        } finally {
            step("收尾：补停止(如需) → 取消监听");
            if (!stopConfirmed && !isTerminal(lastStatus.get())) {
                quiet("补停止任务",
                        () -> call("补停止任务",
                                cb -> ftm.updateFlightTaskStatus(taskUuidRef.get(), 6, cb),
                                defaultTimeout));
            }
            for (Subscription sub : subs) {
                sub.close();
            }
            DemoLog.info(name(), "已取消监听 " + subs.size() + " 路");
        }
    }

    private static boolean isTerminal(int status) {
        return status == 1 || status == 4 || status == 5 || status == 7;
    }

    private static String statusText(int status) {
        switch (status) {
            case 0:  return "pending";
            case 1:  return "start_failed";
            case 2:  return "running";
            case 3:  return "paused";
            case 4:  return "terminated";
            case 5:  return "success";
            case 6:  return "suspended";
            case 7:  return "timeout";
            case 8:  return "partial";
            case 9:  return "preparing";
            case 10: return "takeoff_queued";
            default: return "unknown(" + status + ")";
        }
    }

    private TaskDetail createAndAwaitTask(FlightTaskModule ftm, long timeoutMs, Node scenarioCfg) throws Exception {
        Node flightTaskCfg = params.node("flight_task");
        String waylineUuid = flightTaskCfg.str("wayline_uuid", "");
        if (waylineUuid == null || waylineUuid.isEmpty()) {
            throw new FlightHubException("flight_task.wayline_uuid 未配置, 无法创建任务");
        }

        CreateTaskReq req = new CreateTaskReq();
        req.setName("wayline-scenario-" + System.currentTimeMillis());
        req.setSn(session.topo().getDevice().getDeviceSn());
        req.setWaylineUuid(waylineUuid);
        req.setTaskType(flightTaskCfg.intVal("task_type", 2));
        req.setTimeZone(flightTaskCfg.str("time_zone", "Asia/Shanghai"));
        req.setOutOfControlActionInFlight(flightTaskCfg.intVal("out_of_control_action", 1));
        req.setRthAltitude(flightTaskCfg.dbl("rth_altitude", 100));

        if (req.getTaskType() == 2) {
            long beginAt = flightTaskCfg.longVal("begin_at", 0);
            long endAt = flightTaskCfg.longVal("end_at", 0);
            if (beginAt == 0) {
                long beginDelayMinutes = Math.max(2L, flightTaskCfg.longVal("begin_delay_minutes", 2L));
                beginAt = System.currentTimeMillis() / 1000 + beginDelayMinutes * 60;
            }
            if (endAt == 0) {
                endAt = beginAt + 3600;
            }
            req.setBeginAt(beginAt);
            req.setEndAt(endAt);
        }

        final Await<FlightHubError> createAwait = new Await<FlightHubError>("task.create");
        final AtomicReference<CreateTaskResp> createRef = new AtomicReference<CreateTaskResp>();
        ftm.createFlightTask(req, new FlightHubCallback.CreateFlightTaskCallback() {
            @Override
            public void onResult(FlightHubError error, CreateTaskResp result) {
                createRef.set(result);
                createAwait.set(error);
            }
        });
        FlightHubError createErr = createAwait.await(timeoutMs);
        requireSuccess("创建飞行计划", createErr);

        CreateTaskResp created = createRef.get();
        String taskUuid = created != null ? created.getUuid() : "";
        if (taskUuid == null || taskUuid.isEmpty()) {
            throw new FlightHubException("创建任务成功但未返回 taskUuid");
        }

        long waitRunMs = scenarioCfg.longVal("wait_run_ms", 30000L);
        long deadline = System.currentTimeMillis() + waitRunMs;
        while (System.currentTimeMillis() < deadline) {
            final Await<TaskDetailResult> detailAwait = new Await<TaskDetailResult>("task.detail");
            ftm.getFlightTaskDetail(taskUuid, new FlightHubCallback.GetFlightTaskDetailCallback() {
                @Override
                public void onResult(FlightHubError error, TaskDetail result) {
                    detailAwait.set(new TaskDetailResult(error, result));
                }
            });
            TaskDetailResult detail = detailAwait.await(timeoutMs);
            requireSuccess("获取任务详情", detail.error);
            if (detail.task != null) {
                int status = detail.task.getStatus();
                if (status == 2 || status == 3) {
                    return detail.task;
                }
                if (isTerminal(status)) {
                    throw new FlightHubException("任务未进入运行态即终止: status=" + statusText(status));
                }
            }
            sleep(2000L);
        }
        throw new FlightHubException("等待任务进入 running/paused 超时(" + waitRunMs + "ms)");
    }

    private static final class TaskDetailResult {
        final FlightHubError error;
        final TaskDetail task;

        TaskDetailResult(FlightHubError error, TaskDetail task) {
            this.error = error;
            this.task = task;
        }
    }
}