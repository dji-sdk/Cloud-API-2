// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.module;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.BatchDeleteWaylinesResult;
import com.dji.flighthubsdk.bean.BatchDownloadWaylinesItemResult;
import com.dji.flighthubsdk.bean.BatchDownloadWaylinesParam;
import com.dji.flighthubsdk.bean.BatchUpdateStatusReq;
import com.dji.flighthubsdk.bean.CreateTaskReq;
import com.dji.flighthubsdk.bean.CreateTaskResp;
import com.dji.flighthubsdk.bean.DeviceRecommendationReq;
import com.dji.flighthubsdk.bean.DeviceRecommendationResp;
import com.dji.flighthubsdk.bean.DuplicateWaylineParam;
import com.dji.flighthubsdk.bean.DuplicateWaylineResult;
import com.dji.flighthubsdk.bean.EditTaskReq;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.FlightTaskListParams;
import com.dji.flighthubsdk.bean.FlightTaskListResult;
import com.dji.flighthubsdk.bean.GetTCAInfosResp;
import com.dji.flighthubsdk.bean.GetWaylineDetailResult;
import com.dji.flighthubsdk.bean.GetWaylineListParam;
import com.dji.flighthubsdk.bean.GetWaylineListResult;
import com.dji.flighthubsdk.bean.ImportWaylineEventResult;
import com.dji.flighthubsdk.bean.RecommendedDevice;
import com.dji.flighthubsdk.bean.TCAInfo;
import com.dji.flighthubsdk.bean.TaskDetail;
import com.dji.flighthubsdk.bean.UpdateWaylineResult;
import com.dji.flighthubsdk.bean.WaylineDetailKmzData;
import com.dji.flighthubsdk.bean.WaylineListItemResult;
import com.dji.flighthubsdk.core.FlightTaskModule;
import com.dji.flighthubsdk.core.WaylineManageModule;
import com.dji.flighthubsdk.demo.base.ModuleDemo;
import com.dji.flighthubsdk.demo.runtime.Await;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.runtime.Node;
import com.dji.flighthubsdk.demo.runtime.Subscription;
import com.dji.flighthubsdk.demo.runtime.SubscriptionRegistry;
import com.dji.flighthubsdk.demo.utils.Utils;

import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 飞行任务模块 Demo(菜单驱动)：任务 CRUD / 状态更新 / 设备推荐 / TCA + 航线文件管理。
 *
 * <p>覆盖两个模块的 17 个常用接口(非全量; 两模块公开 API 合计约 30 个), 按功能域分为两组：
 *
 * <pre>
 * 主菜单
 * ├── [1–10]  任务管理(FlightTaskModule)
 * │           getFlightTaskList / createFlightTask / getFlightTaskDetail
 * │           editFlightTask(全量替换) / deleteFlightTask
 * │           updateFlightTaskStatus / batchUpdateFlightTaskStatus
 * │           getDeviceRecommendation / getDeviceTcas / updateDeviceTcas
 * └── [11–17] 航线文件管理(WaylineManageModule)
 *             getWaylineList / getWaylineDetail / batchDeleteWaylines
 *             updateWayline / batchDownloadWaylines / duplicateWayline
 *             listenImportWaylineEvent(WS 监听)
 * </pre>
 *
 * <p><b>SDK 入口：</b>两个模块都挂在 Workspace 上：
 * {@code session.workspace().getFlightTaskModule()} /
 * {@code session.workspace().getWaylineManageModule()}。
 *
 * <p><b>接口串联设计：</b>
 * <ul>
 *   <li>[2] 创建默认用条件任务(task_type=2, begin_at 默认一年后)——立即任务(task_type=1)创建后
 *       即下发执行, 不便演示 [4][5]；</li>
 *   <li>[4] 编辑为全量替换语义：先按 uuid 拉取完整详情, 改名后其余字段原样回填；</li>
 *   <li>[16] 复制航线的 targetObjectKeyPrefix 须指向不存在的路径, 从源航线 kmzObjectKey
 *       推导并追加时间戳+随机串保证唯一；</li>
 *   <li>[17] 监听为阻塞式：按 Enter 即退订返回;推送只落日志文件, 不刷控制台。</li>
 * </ul>
 *
 * <p><b>设计要点：</b>
 * <ul>
 *   <li>uuid 类参数手动输入({@code readLineEcho} 回显), 分页/策略等参数取自配置文件；</li>
 *   <li>三态结果回显(SUCCESS/TIMEOUT/FAILED), 超时与失败明确区分；</li>
 *   <li>[17] 的订阅登记到 {@link #monitorSubs}, 正常路径退出即退订,
 *       run() 的 finally 中 {@link #teardown()} 兜底清理异常残留。</li>
 * </ul>
 */
public class FlightTaskDemo extends ModuleDemo {

    /** [17] 监听航线导入事件时建立的订阅;正常路径方法内退订, 异常残留由 {@link #teardown()} 兜底。 */
    private final List<Subscription> monitorSubs = new ArrayList<>();

    @Override
    public String name() {
        return "FlightTaskDemo";
    }

    @Override
    protected void run() throws Exception {
        if (input == null) {
            DemoLog.warn(name(), "无交互输入(input=null), 无法运行菜单式飞行任务 demo");
            return;
        }
        if (!session.hasDock()) {
            hint("该设备没有机场, 无法执行飞行任务 demo");
            return;
        }

        final FlightTaskModule ftm = session.workspace().getFlightTaskModule();
        final WaylineManageModule wmm = session.workspace().getWaylineManageModule();
        if (ftm == null || wmm == null) {
            DemoLog.error(name(), "获取模块失败: ftm=" + (ftm != null) + " wmm=" + (wmm != null));
            console("   获取 FlightTaskModule/WaylineManageModule 失败(存在 null), 无法进入本 demo 菜单");
            return;
        }

        final long actionTimeout = timeout("default_callback_timeout_ms");
        DemoLog.info(name(), "actionTimeout=" + actionTimeout + "ms");

        try {
            while (true) {
                Utils.printTitle("执行 " + name());
                if (!monitorSubs.isEmpty()) {
                    console("(检测到异常残留的航线导入事件监听 " + monitorSubs.size() + " 路; 选 [17] 清理并重新监听)");
                }
                System.out.print(menu());
                int choice = Utils.readIntInRange(input, "请选择(1-17, q 退出): ", 1, 17);
                if (choice == Utils.QUIT) {
                    DemoLog.info(name(), "退出飞行任务菜单");
                    break;
                }
                try {
                    switch (choice) {
                        case 1:  getFlightTaskList(ftm, actionTimeout);           break;
                        case 2:  createFlightTask(ftm, wmm, actionTimeout);       break;
                        case 3:  getFlightTaskDetail(ftm, actionTimeout);         break;
                        case 4:  editFlightTask(ftm, actionTimeout);              break;
                        case 5:  deleteFlightTask(ftm, actionTimeout);            break;
                        case 6:  updateFlightTaskStatus(ftm, actionTimeout);      break;
                        case 7:  batchUpdateFlightTaskStatus(ftm, actionTimeout); break;
                        case 8:  getDeviceRecommendation(ftm, actionTimeout);     break;
                        case 9:  getDeviceTcas(ftm, actionTimeout);               break;
                        case 10: updateDeviceTcas(ftm, actionTimeout);            break;
                        case 11: getWaylineList(wmm, actionTimeout);              break;
                        case 12: getWaylineDetail(wmm, actionTimeout);            break;
                        case 13: batchDeleteWaylines(wmm, actionTimeout);         break;
                        case 14: updateWayline(wmm, actionTimeout);               break;
                        case 15: batchDownloadWaylines(wmm, actionTimeout);       break;
                        case 16: duplicateWayline(wmm, actionTimeout);            break;
                        case 17: listenImportWaylineEvent(wmm);                   break;
                        default: break;
                    }
                } catch (Exception e) {
                    console("   结果 -> [异常] " + Utils.describe(e));
                    DemoLog.error(name(), "菜单动作执行异常: " + Utils.describe(e), e);
                }
            }
        } finally {
            teardown();
        }
    }

    // ==================== [1] 获取任务列表 (getFlightTaskList) ====================

    private void getFlightTaskList(FlightTaskModule ftm, long timeout) {
        Node cfg = params.node("flight_task");
        FlightTaskListParams reqParams = new FlightTaskListParams();
        reqParams.setPage(1);
        reqParams.setPageSize(cfg.intVal("list_page_size", 20));
        reqParams.setSn(Collections.singletonList(session.topo().getDevice().getDeviceSn()));

        final Await<FlightHubError> await = new Await<>(name() + ".getFlightTaskList");
        final AtomicReference<FlightTaskListResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("获取任务列表", timeout, await, () ->
            ftm.getFlightTaskList(reqParams, new FlightHubCallback.GetFlightTaskListCallback() {
                @Override
                public void onResult(FlightHubError error, FlightTaskListResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );

        if (o == Outcome.SUCCESS) {
            FlightTaskListResult r = ref.get();
            List<TaskDetail> list = (r != null && r.getList() != null) ? r.getList() : new ArrayList<>();
            console("   任务列表(共 " + list.size() + " 条):");
            for (int i = 0; i < list.size(); i++) {
                TaskDetail t = list.get(i);
                console("     [" + i + "] uuid=" + t.getUuid() + " name=" + t.getName()
                    + " status=" + t.getStatus() + " sn=" + t.getSn());
            }
        }
    }

    // ==================== [2] 创建飞行计划 (createFlightTask) ====================

    private void createFlightTask(FlightTaskModule ftm, WaylineManageModule wmm, long timeout) {
        Node cfg = params.node("flight_task");
        String waylineUuid = cfg.str("wayline_uuid", "");
        // 优先使用当前选中设备的 SN, 配置文件中的 sn 作为兜底
        String deviceSn = session.topo().getDevice().getDeviceSn();
        String sn = deviceSn != null && !deviceSn.isEmpty() ? deviceSn : cfg.str("sn", "");
        if (waylineUuid.isEmpty()) {
            waylineUuid = chooseWaylineUuid(wmm, timeout);
            if (waylineUuid == null || waylineUuid.isEmpty()) {
                hint("未选择航线 UUID, 中止创建任务");
                return;
            }
        }
        if (sn.isEmpty()) {
            hint("[跳过] 无法确定设备 SN(当前会话无设备且配置为空)");
            return;
        }

        CreateTaskReq req = new CreateTaskReq();
        req.setName(generateTaskName());
        req.setSn(sn);
        req.setWaylineUuid(waylineUuid);
        req.setTimeZone(cfg.str("time_zone", "Asia/Shanghai"));
        req.setOutOfControlActionInFlight(cfg.intVal("out_of_control_action", 1));
        req.setRthAltitude(cfg.dbl("rth_altitude", 100));
        // taskType: 1=立即任务 2=条件任务(demo 默认; 须设置 beginAt/endAt 执行窗口)
        // 立即任务创建后即下发执行, 不便演示 [4]编辑/[5]删除, 因此 demo 默认使用条件任务
        int taskType = cfg.intVal("task_type", 1);
        req.setTaskType(taskType);

        // 条件任务(taskType=2)必须设置 beginAt/endAt(秒级 Unix 时间戳)
        // 当 begin_at 未显式配置时, 按“当前时间 + begin_delay_minutes”自动生成起飞时间,
        // 方便测试定时任务的近实时执行。
        if (taskType == 2) {
            long beginAt = cfg.longVal("begin_at", 0);
            long endAt = cfg.longVal("end_at", 0);
            if (beginAt == 0) {
                long beginDelayMinutes = Math.max(2L, cfg.longVal("begin_delay_minutes", 2L));
                beginAt = System.currentTimeMillis() / 1000 + beginDelayMinutes * 60;
            }
            if (endAt == 0) {
                endAt = beginAt + 3600; // 默认执行窗口 1 小时
            }
            req.setBeginAt(beginAt);
            req.setEndAt(endAt);
            console("     taskType=2(条件任务) beginAt=" + beginAt + "(" + new Date(beginAt * 1000) + ")"
                + " endAt=" + endAt + "(" + new Date(endAt * 1000) + ")");
        }

        console("\n   创建飞行计划参数(flight_task 配置节):");
        console("     name=" + req.getName() + " waylineUuid=" + req.getWaylineUuid() + " sn=" + req.getSn());
        console("     timeZone=" + req.getTimeZone() + " outOfControlAction=" + req.getOutOfControlActionInFlight()
            + " rthAltitude=" + req.getRthAltitude() + "m");

        final Await<FlightHubError> await = new Await<>(name() + ".createFlightTask");
        final AtomicReference<CreateTaskResp> ref = new AtomicReference<>();
        Outcome o = runOutcome("创建飞行计划", timeout, await, () ->
            ftm.createFlightTask(req, new FlightHubCallback.CreateFlightTaskCallback() {
                @Override
                public void onResult(FlightHubError error, CreateTaskResp result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );

        if (o == Outcome.SUCCESS) {
            CreateTaskResp r = ref.get();
            hint("创建成功: taskUuid=" + (r != null ? r.getUuid() : "null"));
        }
    }

    private String chooseWaylineUuid(WaylineManageModule wmm, long timeout) {
        Node cfg = this.params.node("wayline_manage");
        GetWaylineListParam param = new GetWaylineListParam();
        param.setPage(1);
        param.setPageSize(cfg.intVal("list_page_size", 20));

        final Await<FlightHubError> await = new Await<>(name() + ".chooseWayline");
        final AtomicReference<GetWaylineListResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("获取航线列表(选择创建任务航线)", timeout, await, () ->
            wmm.getWaylineList(param, new FlightHubCallback.GetWaylineListCallback() {
                @Override
                public void onResult(FlightHubError error, GetWaylineListResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );
        if (o != Outcome.SUCCESS || ref.get() == null || ref.get().getItems() == null || ref.get().getItems().isEmpty()) {
            return "";
        }

        List<WaylineListItemResult> items = ref.get().getItems();
        console("   flight_task.wayline_uuid 未配置, 请选择航线:");
        for (int i = 0; i < items.size(); i++) {
            WaylineListItemResult item = items.get(i);
            console("     [" + i + "] uuid=" + item.getWaylineUuid()
                + " name=" + item.getWaylineName()
                + " droneType=" + item.getDroneType()
                + " size=" + item.getSize() + "B");
        }

        String inputValue = Utils.readLineEcho(input, "   请输入航线编号或 wayline_uuid: ");
        if (inputValue == null || inputValue.trim().isEmpty()) {
            return "";
        }
        inputValue = inputValue.trim();
        try {
            int idx = Integer.parseInt(inputValue);
            if (idx < 0 || idx >= items.size()) {
                hint("航线编号超出范围: " + idx);
                return "";
            }
            return items.get(idx).getWaylineUuid();
        } catch (NumberFormatException ignore) {
            return inputValue;
        }
    }

    // ==================== [3] 获取任务详情 (getFlightTaskDetail) ====================

    private void getFlightTaskDetail(FlightTaskModule ftm, long timeout) {
        String taskUuid = Utils.readLineEcho(input, "   请输入 task_uuid: ");
        if (taskUuid == null || taskUuid.isEmpty()) {
            hint("task_uuid 不能为空");
            return;
        }

        fetchAndPrintTaskDetail(ftm, taskUuid, timeout);
    }

    /**
     * 拉取并回显任务详情([3] 直接调用, [4] 编辑前取全量字段复用)。
     *
     * @return 服务端返回的详情;失败/超时未拿到时为 null
     */
    private TaskDetail fetchAndPrintTaskDetail(FlightTaskModule ftm, String taskUuid, long timeout) {
        final Await<FlightHubError> await = new Await<>(name() + ".getFlightTaskDetail");
        final AtomicReference<TaskDetail> ref = new AtomicReference<>();
        Outcome o = runOutcome("获取任务详情", timeout, await, () ->
            ftm.getFlightTaskDetail(taskUuid, new FlightHubCallback.GetFlightTaskDetailCallback() {
                @Override
                public void onResult(FlightHubError error, TaskDetail result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );

        TaskDetail detail = ref.get();
        if (o != Outcome.SUCCESS || detail == null) {
            // FAILED(服务端拒绝)与 TIMEOUT 统一按「未拿到」返回 null, 与 @return 契约一致;
            // SDK 转换层对 null 输入会造出全空字段的非 null TaskDetail, 只判 detail null 不够
            return null;
        }
        console("   任务详情: uuid=" + detail.getUuid() + " name=" + detail.getName()
            + " status=" + detail.getStatus() + " sn=" + detail.getSn()
            + " waylineUuid=" + detail.getWaylineUuid()
            + " rthAltitude=" + detail.getRthAltitude());

        return detail;
    }

    // ==================== [4] 编辑飞行计划 (editFlightTask, 全量替换) ====================

    private void editFlightTask(FlightTaskModule ftm, long timeout) {
        String taskUuid = Utils.readLineEcho(input, "   请输入 task_uuid: ");
        if (taskUuid == null || taskUuid.isEmpty()) {
            hint("task_uuid 不能为空");
            return;
        }

        console("   先获取完整任务详情(全量替换语义)…");
        TaskDetail detail = fetchAndPrintTaskDetail(ftm, taskUuid, timeout);
        if (detail == null) {
            hint("获取任务详情失败, 无法编辑");
            return;
        }

        String newName = Utils.readLineEcho(input, "   请输入新任务名称(回车保持原名 \"" + detail.getName() + "\"): ");
        if (newName == null || newName.isEmpty()) {
            newName = detail.getName();
        }

        EditTaskReq req = new EditTaskReq();
        req.setUuid(detail.getUuid());
        req.setName(newName);
        req.setTaskType(detail.getTaskType() != 0 ? detail.getTaskType() : 1);
        // time_zone 为空时兜底用 Asia/Shanghai, 避免服务端 required 校验失败
        String tz = detail.getTimeZone();
        req.setTimeZone(tz != null && !tz.isEmpty() ? tz : "Asia/Shanghai");
        req.setSn(detail.getSn());
        req.setLandingDockSn(detail.getLandingDockSn() != null ? detail.getLandingDockSn() : "");
        req.setWaylineUuid(detail.getWaylineUuid());
        req.setOutOfControlActionInFlight(detail.getOutOfControlActionInFlight());
        req.setRthAltitude(detail.getRthAltitude());
        req.setRthMode(detail.getRthMode());
        req.setMinBatteryCapacity(detail.getMinBatteryCapacity());
        req.setWaylinePrecisionType(detail.getWaylinePrecisionType());
        req.setWaylineSegmentIndexes(detail.getWaylineSegmentIndexes());
        Long beginAt = toEpochSecond(detail.getBeginAt());
        Long endAt = toEpochSecond(detail.getEndAt());
        if (detail.getBeginAt() != null && !detail.getBeginAt().isEmpty() && beginAt == null) {
            hint("beginAt=\"" + detail.getBeginAt() + "\" 解析失败, 将不回填; 条件任务(taskType=2)该字段必填, 服务端可能拒绝");
        }
        if (detail.getEndAt() != null && !detail.getEndAt().isEmpty() && endAt == null) {
            hint("endAt=\"" + detail.getEndAt() + "\" 解析失败, 将不回填; 条件任务(taskType=2)该字段必填, 服务端可能拒绝");
        }
        req.setBeginAt(beginAt);
        req.setEndAt(endAt);
        req.setTags(detail.getTags());
        req.setBreakPoint(detail.getBreakPoint());

        console("   编辑任务: taskUuid=" + taskUuid + " newName=" + newName);

        final Await<FlightHubError> await = new Await<>(name() + ".editFlightTask");
        final AtomicReference<CreateTaskResp> ref = new AtomicReference<>();
        Outcome o = runOutcome("编辑飞行计划", timeout, await, () ->
            ftm.editFlightTask(taskUuid, req, new FlightHubCallback.CreateFlightTaskCallback() {
                @Override
                public void onResult(FlightHubError error, CreateTaskResp result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );

        if (o == Outcome.SUCCESS && ref.get() != null && ref.get().getUuid() != null) {
            console("   编辑成功: uuid=" + ref.get().getUuid());
        }
    }

    // ==================== [5] 删除飞行计划 (deleteFlightTask) ====================

    private void deleteFlightTask(FlightTaskModule ftm, long timeout) {
        String taskUuid = Utils.readLineEcho(input, "   请输入 task_uuid: ");
        if (taskUuid == null || taskUuid.isEmpty()) {
            hint("task_uuid 不能为空");
            return;
        }
        console("   删除任务: " + taskUuid);
        callOutcome("删除飞行计划", cb -> ftm.deleteFlightTask(taskUuid, cb), timeout);
    }

    // ==================== [6] 更新任务状态 (updateFlightTaskStatus) ====================

    private void updateFlightTaskStatus(FlightTaskModule ftm, long timeout) {
        console("   目标状态仅允许: 0=待开始  6=挂起 (服务端仅接受这两个值)");
        String taskUuid = Utils.readLineEcho(input, "   请输入 task_uuid: ");
        if (taskUuid == null || taskUuid.isEmpty()) {
            hint("task_uuid 不能为空");
            return;
        }
        int status = Utils.readIntInRange(input, "   请输入目标状态(0 或 6, q 取消): ", 0, 6);
        if (status == Utils.QUIT) {
            return;
        }
        if (status != 0 && status != 6) {
            hint("目标状态仅允许 0(待开始) 或 6(挂起)");
            return;
        }
        console("   更新任务状态: uuid=" + taskUuid + " status=" + status);
        callOutcome("更新任务状态", cb -> ftm.updateFlightTaskStatus(taskUuid, status, cb), timeout);
    }

    // ==================== [7] 批量更新任务状态 (batchUpdateFlightTaskStatus) ====================

    private void batchUpdateFlightTaskStatus(FlightTaskModule ftm, long timeout) {
        console("   目标状态仅允许: 0=待开始  6=挂起 (服务端仅接受这两个值)");
        String uuidsStr = Utils.readLineEcho(input, "   请输入 task_uuid 列表(逗号分隔): ");
        if (uuidsStr == null || uuidsStr.isEmpty()) {
            hint("task_uuid 列表不能为空");
            return;
        }
        int status = Utils.readIntInRange(input, "   请输入目标状态(0 或 6, q 取消): ", 0, 6);
        if (status == Utils.QUIT) {
            return;
        }
        if (status != 0 && status != 6) {
            hint("目标状态仅允许 0(待开始) 或 6(挂起)");
            return;
        }
        List<String> uuids = parseCsv(uuidsStr);
        if (uuids.isEmpty()) {
            hint("未解析到有效 task_uuid");
            return;
        }

        BatchUpdateStatusReq req = new BatchUpdateStatusReq();
        req.setTaskUuids(uuids);
        req.setStatus(status);

        console("   批量更新状态: count=" + uuids.size() + " status=" + status);
        callOutcome("批量更新任务状态", cb -> ftm.batchUpdateFlightTaskStatus(req, cb), timeout);
    }

    // ==================== [8] 获取设备推荐 (getDeviceRecommendation) ====================

    private void getDeviceRecommendation(FlightTaskModule ftm, long timeout) {
        Node cfg = params.node("device_recommendation");
        DeviceRecommendationReq req = new DeviceRecommendationReq();
        req.setStrategyId(cfg.intVal("strategy_id", 1));
        List<String> snList = askSnList("   请输入设备 SN 列表(逗号分隔, 回车使用当前会话 SN): ");
        req.setSn(snList);

        console("   获取设备推荐: strategyId=" + req.getStrategyId() + " sns=" + snList);

        final Await<FlightHubError> await = new Await<>(name() + ".getDeviceRecommendation");
        final AtomicReference<DeviceRecommendationResp> ref = new AtomicReference<>();
        Outcome o = runOutcome("获取设备推荐", timeout, await, () ->
            ftm.getDeviceRecommendation(req, new FlightHubCallback.GetDeviceRecommendationCallback() {
                @Override
                public void onResult(FlightHubError error, DeviceRecommendationResp result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );

        if (o == Outcome.SUCCESS) {
            DeviceRecommendationResp r = ref.get();
            List<RecommendedDevice> devices = (r != null && r.getDevices() != null) ? r.getDevices() : new ArrayList<>();
            console("   推荐设备(共 " + devices.size() + " 条):");
            for (RecommendedDevice d : devices) {
                console("     droneSn=" + d.getDroneSn() + " gatewaySn=" + d.getGatewaySn()
                    + " distance=" + d.getDistance() + " matched=" + d.isMatched());
            }
        }
    }

    // ==================== [9] 获取设备 TCA (getDeviceTcas) ====================

    private void getDeviceTcas(FlightTaskModule ftm, long timeout) {
        List<String> snList = askSnList("   请输入 centerSn 列表(逗号分隔, 回车使用当前会话 SN): ");
        console("   获取设备 TCA: sns=" + snList);

        final Await<FlightHubError> await = new Await<>(name() + ".getDeviceTcas");
        final AtomicReference<GetTCAInfosResp> ref = new AtomicReference<>();
        Outcome o = runOutcome("获取设备 TCA", timeout, await, () ->
            ftm.getDeviceTcas(snList, new FlightHubCallback.GetDeviceTcasCallback() {
                @Override
                public void onResult(FlightHubError error, GetTCAInfosResp result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );

        if (o == Outcome.SUCCESS) {
            GetTCAInfosResp r = ref.get();
            List<TCAInfo> tcas = (r != null && r.getTcas() != null) ? r.getTcas() : new ArrayList<>();
            console("   TCA 信息(共 " + tcas.size() + " 条):");
            for (TCAInfo t : tcas) {
                console("     centerSn=" + t.getCenterSn() + " dataJson=" + t.getDataJson());
            }
        }
    }

    // ==================== [10] 更新设备 TCA (updateDeviceTcas) ====================

    private void updateDeviceTcas(FlightTaskModule ftm, long timeout) {
        List<String> snList = askSnList("   请输入 centerSn 列表(逗号分隔, 回车使用当前会话 SN): ");
        console("   更新设备 TCA: sns=" + snList);
        callOutcome("更新设备 TCA", cb -> ftm.updateDeviceTcas(snList, cb), timeout);
    }

    // ==================== [11] 获取航线列表 (getWaylineList) ====================

    private void getWaylineList(WaylineManageModule wmm, long timeout) {
        Node cfg = params.node("wayline_manage");
        GetWaylineListParam param = new GetWaylineListParam();
        param.setPage(1);
        param.setPageSize(cfg.intVal("list_page_size", 20));

        final Await<FlightHubError> await = new Await<>(name() + ".getWaylineList");
        final AtomicReference<GetWaylineListResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("获取航线列表", timeout, await, () ->
            wmm.getWaylineList(param, new FlightHubCallback.GetWaylineListCallback() {
                @Override
                public void onResult(FlightHubError error, GetWaylineListResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );

        if (o == Outcome.SUCCESS) {
            GetWaylineListResult r = ref.get();
            List<WaylineListItemResult> items = (r != null && r.getItems() != null) ? r.getItems() : new ArrayList<>();
            console("   航线列表(共 " + items.size() + " 条):");
            for (int i = 0; i < items.size(); i++) {
                WaylineListItemResult item = items.get(i);
                console("     [" + i + "] uuid=" + item.getWaylineUuid()
                    + " name=" + item.getWaylineName()
                    + " droneType=" + item.getDroneType()
                    + " size=" + item.getSize() + "B");
            }
        }
    }

    // ==================== [12] 获取航线详情 (getWaylineDetail) ====================

    private void getWaylineDetail(WaylineManageModule wmm, long timeout) {
        String waylineUuid = Utils.readLineEcho(input, "   请输入 wayline_uuid: ");
        if (waylineUuid == null || waylineUuid.isEmpty()) {
            hint("wayline_uuid 不能为空");
            return;
        }

        final Await<FlightHubError> await = new Await<>(name() + ".getWaylineDetail");
        final AtomicReference<GetWaylineDetailResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("获取航线详情", timeout, await, () ->
            wmm.getWaylineDetail(waylineUuid, new FlightHubCallback.GetWaylineDetailCallback() {
                @Override
                public void onResult(FlightHubError error, GetWaylineDetailResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );

        if (o == Outcome.SUCCESS) {
            GetWaylineDetailResult r = ref.get();
            if (r != null) {
                console("   航线详情: waylineUuid=" + r.getWaylineUuid()
                    + " name=" + r.getWaylineName()
                    + " url=" + r.getUrl()
                    + " md5=" + r.getMd5()
                    + " updatedAt=" + r.getUpdatedAt());

                WaylineDetailKmzData kmz = r.getKmzData();
                if (kmz != null) {
                    console("     kmzData: duration=" + kmz.getDuration()
                        + " distance=" + kmz.getDistance() + "m");
                }
            }
        }
    }

    // ==================== [13] 批量删除航线 (batchDeleteWaylines) ====================

    private void batchDeleteWaylines(WaylineManageModule wmm, long timeout) {
        String uuidsStr = Utils.readLineEcho(input, "   请输入 wayline_uuid 列表(逗号分隔): ");
        if (uuidsStr == null || uuidsStr.isEmpty()) {
            hint("wayline_uuid 列表不能为空");
            return;
        }
        List<String> uuids = parseCsv(uuidsStr);
        if (uuids.isEmpty()) {
            hint("未解析到有效 wayline_uuid");
            return;
        }

        console("   批量删除航线: count=" + uuids.size());

        final Await<FlightHubError> await = new Await<>(name() + ".batchDeleteWaylines");
        Outcome o = runOutcome("批量删除航线", timeout, await, () ->
            wmm.batchDeleteWaylines(uuids, new FlightHubCallback.BatchDeleteWaylinesCallback() {
                @Override
                public void onResult(FlightHubError error, BatchDeleteWaylinesResult result) {
                    await.set(error);
                }
            })
        );

        if (o == Outcome.SUCCESS) {
            console("   批量删除成功");
        }
    }

    // ==================== [14] 更新航线名称 (updateWayline) ====================

    private void updateWayline(WaylineManageModule wmm, long timeout) {
        String waylineUuid = Utils.readLineEcho(input, "   请输入 wayline_uuid: ");
        if (waylineUuid == null || waylineUuid.isEmpty()) {
            hint("wayline_uuid 不能为空");
            return;
        }
        String newName = Utils.readLineEcho(input, "   请输入新航线名称: ");
        if (newName == null || newName.isEmpty()) {
            hint("航线名称不能为空");
            return;
        }

        console("   更新航线名称: uuid=" + waylineUuid + " newName=" + newName);

        final Await<FlightHubError> await = new Await<>(name() + ".updateWayline");
        Outcome o = runOutcome("更新航线名称", timeout, await, () ->
            wmm.updateWayline(waylineUuid, newName, new FlightHubCallback.UpdateWaylineCallback() {
                @Override
                public void onResult(FlightHubError error, UpdateWaylineResult result) {
                    await.set(error);
                }
            })
        );

        if (o == Outcome.SUCCESS) {
            console("   更新成功");
        }
    }

    // ==================== [15] 批量下载航线 (batchDownloadWaylines) ====================

    private void batchDownloadWaylines(WaylineManageModule wmm, long timeout) {
        String uuidsStr = Utils.readLineEcho(input, "   请输入 wayline_uuid 列表(逗号分隔): ");
        if (uuidsStr == null || uuidsStr.isEmpty()) {
            hint("wayline_uuid 列表不能为空");
            return;
        }
        List<String> uuids = parseCsv(uuidsStr);
        if (uuids.isEmpty()) {
            hint("未解析到有效 wayline_uuid");
            return;
        }

        BatchDownloadWaylinesParam param = new BatchDownloadWaylinesParam();
        param.setWaylineUuids(uuids);

        console("   批量下载航线: count=" + uuids.size());

        final Await<FlightHubError> await = new Await<>(name() + ".batchDownloadWaylines");
        final AtomicReference<List<BatchDownloadWaylinesItemResult>> ref = new AtomicReference<>();
        Outcome o = runOutcome("批量下载航线", timeout, await, () ->
            wmm.batchDownloadWaylines(param, new FlightHubCallback.BatchDownloadWaylinesCallback() {
                @Override
                public void onResult(FlightHubError error, List<BatchDownloadWaylinesItemResult> items) {
                    ref.set(items);
                    await.set(error);
                }
            })
        );

        if (o == Outcome.SUCCESS) {
            List<BatchDownloadWaylinesItemResult> items = ref.get();
            if (items != null) {
                console("   下载链接(共 " + items.size() + " 条):");
                for (BatchDownloadWaylinesItemResult item : items) {
                    console("     " + item);
                }
            }
        }
    }

    // ==================== [16] 复制航线 (duplicateWayline) ====================

    private void duplicateWayline(WaylineManageModule wmm, long timeout) {
        // 先拉取航线列表让用户选择
        Node cfg = params.node("wayline_manage");
        GetWaylineListParam listParam = new GetWaylineListParam();
        listParam.setPage(1);
        listParam.setPageSize(cfg.intVal("list_page_size", 20));

        final Await<FlightHubError> listAwait = new Await<>(name() + ".duplicateWayline#list");
        final AtomicReference<GetWaylineListResult> listRef = new AtomicReference<>();
        Outcome listOutcome = runOutcome("获取航线列表", timeout, listAwait, () ->
            wmm.getWaylineList(listParam, new FlightHubCallback.GetWaylineListCallback() {
                @Override
                public void onResult(FlightHubError error, GetWaylineListResult result) {
                    listRef.set(result);
                    listAwait.set(error);
                }
            })
        );
        if (listOutcome != Outcome.SUCCESS || listRef.get() == null) {
            hint("获取航线列表失败, 中止复制");
            return;
        }
        List<WaylineListItemResult> items = listRef.get().getItems();
        if (items == null || items.isEmpty()) {
            hint("没有可用的航线, 中止复制");
            return;
        }
        console("   可选航线列表:");
        for (int i = 0; i < items.size(); i++) {
            WaylineListItemResult item = items.get(i);
            console("     [" + i + "] " + item.getWaylineName() + " (drone=" + item.getDroneType() + ")");
        }

        String srcInput = Utils.readLineEcho(input, "   请输入源航线编号或 wayline_uuid: ");
        if (srcInput == null || srcInput.isEmpty()) {
            hint("输入不能为空");
            return;
        }
        String srcUuid;
        try {
            int idx = Integer.parseInt(srcInput);
            if (idx < 0 || idx >= items.size()) {
                hint("编号超出范围 (0-" + (items.size() - 1) + ")");
                return;
            }
            srcUuid = items.get(idx).getWaylineUuid();
        } catch (NumberFormatException e) {
            srcUuid = srcInput;
        }
        final String finalSrcUuid = srcUuid;
        String targetName = Utils.readLineEcho(input, "   请输入复制后的新航线名称: ");
        if (targetName == null || targetName.isEmpty()) {
            hint("新航线名称不能为空");
            return;
        }

        // targetObjectKeyPrefix 必填(联调实测; SDK/文档未声明该约束), 从源航线 detail.kmzObjectKey
        // 去掉最后一段 basename 推导(实测形如 <workspaceId>/<orgId>/<projectUuid>)。
        final Await<FlightHubError> detailAwait = new Await<>(name() + ".duplicateWayline#detail");
        final AtomicReference<GetWaylineDetailResult> detailRef = new AtomicReference<>();
        Outcome detailOutcome = runOutcome("查询源航线 detail(取 objectKeyPrefix)", timeout, detailAwait, () ->
            wmm.getWaylineDetail(finalSrcUuid, new FlightHubCallback.GetWaylineDetailCallback() {
                @Override
                public void onResult(FlightHubError error, GetWaylineDetailResult result) {
                    detailRef.set(result);
                    detailAwait.set(error);
                }
            })
        );
        if (detailOutcome != Outcome.SUCCESS || detailRef.get() == null) {
            hint("拿不到源航线详情, 无法推导 targetObjectKeyPrefix, 中止复制");
            return;
        }
        String kmzObjectKey = detailRef.get().getKmzObjectKey();
        if (kmzObjectKey == null || kmzObjectKey.isEmpty()) {
            hint("源航线 kmzObjectKey 为空, 无法推导 targetObjectKeyPrefix, 中止复制");
            return;
        }
        int slash = kmzObjectKey.lastIndexOf('/');
        final String basePrefix = (slash > 0) ? kmzObjectKey.substring(0, slash) : kmzObjectKey;
        // 实测(非文档约束): prefix 指向已存在路径时后端返回业务码 217009 invalid path,
        // 故追加时间戳+随机串保证唯一; 该码 SDK 内无定义, 后端行为变更时以实际返回为准。
        final String prefix = basePrefix + "/copy_"
            + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date())
            + "_" + UUID.randomUUID().toString().substring(0, 8);

        DuplicateWaylineParam param = new DuplicateWaylineParam();
        param.setOriginalWaylineUuid(finalSrcUuid);
        param.setTargetWaylineName(targetName);
        param.setTargetObjectKeyPrefix(prefix);

        console("   复制航线: srcUuid=" + finalSrcUuid + " targetName=" + targetName);
        console("             targetObjectKeyPrefix=" + prefix + " (从源 kmzObjectKey 推导)");

        final Await<FlightHubError> await = new Await<>(name() + ".duplicateWayline");
        final AtomicReference<DuplicateWaylineResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("复制航线", timeout, await, () ->
            wmm.duplicateWayline(param, new FlightHubCallback.DuplicateWaylineCallback() {
                @Override
                public void onResult(FlightHubError error, DuplicateWaylineResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );

        if (o == Outcome.SUCCESS) {
            DuplicateWaylineResult r = ref.get();
            if (r != null) {
                console("   复制成功: newWaylineUuid=" + r.getWaylineUuid()
                    + " name=" + r.getWaylineName());
            }
        }
    }

    // ==================== [17] 监听航线导入事件 (listenImportWaylineEvent) ====================

    /**
     * 监听航线导入事件：建立订阅后阻塞等待, 按 Enter 即退订返回(与 DeviceDebugDemo 的
     * 进度订阅交互一致);推送不刷控制台、只落日志文件, 避免打断菜单交互。
     *
     * <p>正常路径退出前必退订;若上次因异常中断残留订阅(登记于 {@link #monitorSubs}),
     * 本次进入先统一清理再重新监听, run() 的 finally 中 {@link #teardown()} 亦会兜底。
     */
    private void listenImportWaylineEvent(WaylineManageModule wmm) {
        if (!monitorSubs.isEmpty()) {
            console("\n>> [航线导入事件监听] 清理上次异常残留的 " + monitorSubs.size() + " 路监听 ...");
            closeMonitorSubs();
        }

        console("\n>> [航线导入事件监听] 开始监听(推送只落日志文件, 不刷控制台) ...");
        int listenId = wmm.listenImportWaylineEvent(new FlightHubCallback.ImportWaylineEventCallback() {
            @Override
            public void onResult(ImportWaylineEventResult result) {
                if (result == null) {
                    return;
                }
                pushLog("listenImportWaylineEvent() #ImportWaylineEventResult, 航线导入事件: " + result);
            }
        });

        // subscribe() 已登记到会话 registry(会话结束自动退订), monitorSubs 另记一份供本方法内主动退订
        Subscription sub = subscribe(
            "listenImportWaylineEvent", listenId,
            id -> wmm.cancelListenImportWaylineEvent(id)
        );
        monitorSubs.add(sub);

        console("   监听已建立(listenId=" + listenId + "), 推送见日志文件");
        Utils.readLine(input, "   按 Enter 退出监听...");
        closeMonitorSubs();
        console("   已退出监听");
    }

    // ==================== 收尾 ====================

    /** 关闭并清空 [17] 建立的监听订阅(正常退出、异常残留清理、收尾兜底共用)。 */
    private void closeMonitorSubs() {
        SubscriptionRegistry reg = registry();
        for (Subscription sub : monitorSubs) {
            sub.close();
            reg.remove(sub);   // 主动移出 registry, 避免已关闭引用长期积累
        }
        monitorSubs.clear();
    }

    /** 收尾：兜底取消 [17] 的监听订阅(正常路径已在方法内退订, 这里只处理异常中断残留)。 */
    private void teardown() {
        if (!monitorSubs.isEmpty()) {
            DemoLog.info(name(), "收尾: 取消 " + monitorSubs.size() + " 路航线导入事件订阅");
            closeMonitorSubs();
        }
    }

    // ==================== 辅助方法 ====================

    /** 解析逗号分隔输入为去空白、去空项的列表;null 输入返回空列表。 */
    private static List<String> parseCsv(String s) {
        List<String> out = new ArrayList<>();
        if (s == null) {
            return out;
        }
        for (String part : s.split(",")) {
            String t = part.trim();
            if (!t.isEmpty()) {
                out.add(t);
            }
        }
        return out;
    }

    /**
     * 交互读取设备 SN / centerSn 列表(逗号分隔), 直接回车时回退当前会话的飞机 SN
     * (无飞机则返回空列表, 交由服务端校验)。[8][9][10] 三个接口共用此输入模式。
     */
    private List<String> askSnList(String prompt) {
        List<String> snList = parseCsv(Utils.readLineEcho(input, prompt));
        if (snList.isEmpty() && session.hasDrone()) {
            snList.add(session.droneSn());
        }

        return snList;
    }

    /** 每次创建生成一个带时间戳的唯一任务名, 便于在列表里回找、避免重名冲突。 */
    private static String generateTaskName() {
        String ts = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(new Date());
        return "demo-wayline-task-" + ts;
    }

    /**
     * TaskDetail 的 beginAt/endAt(ISO 8601 字符串, 兼容纯数字秒级串)转 EditTaskReq 的秒级 Unix 时间戳;
     * null/空串/解析失败返回 null(EditTaskReq 置 null 表示不设置)。
     * 注意: 条件任务(taskType=2)编辑时该字段必填, 返回 null 会使请求缺必填字段, 调用方须提示用户。
     */
    private static Long toEpochSecond(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException ignore) {
            // 非纯数字, 继续按 ISO 8601 解析
        }
        try {
            return OffsetDateTime.parse(s).toEpochSecond();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** 监听推送专用：只落日志文件不刷控制台, 避免推送打断菜单交互。 */
    private void pushLog(String line) {
        DemoLog.info(name(), line);
    }

    /** 纯控制台输出(不落日志文件), 用于菜单文案与结果回显。 */
    private void console(String line) {
        System.out.println(line);
    }

    /** 提示信息双写：既落日志文件(带 demo 名 tag)又回显控制台, 用于前置条件不满足、取消等需留痕的提示。 */
    private void hint(String msg) {
        DemoLog.info(name(), msg);
        console("   " + msg);
    }

    // ==================== 菜单文案 ====================

    /**
     * 主菜单文案：[1–10] 对应 FlightTaskModule、[11–17] 对应 WaylineManageModule 的同名接口(见类 Javadoc 菜单树)。
     * 增删菜单项时须同步 {@link #run()} 中的 switch 分支与 {@code readIntInRange} 的取值上界。
     */
    private String menu() {
        return "\n"
            + "  === 飞行任务 (Flight Task / Wayline Manage) ===\n"
            + "\n"
            + "  --- 任务管理 (FlightTaskModule) ---\n"
            + "  1.  获取任务列表             (getFlightTaskList)\n"
            + "  2.  创建飞行计划             (createFlightTask, 参数见 demo_params.json flight_task 节)\n"
            + "  3.  获取任务详情             (getFlightTaskDetail, 输入 task_uuid)\n"
            + "  4.  编辑飞行计划(全量替换)   (editFlightTask, 先查详情改名后其余字段原样回填)\n"
            + "  5.  删除飞行计划             (deleteFlightTask, 输入 task_uuid)\n"
            + "  6.  更新任务状态             (updateFlightTaskStatus, 0=待开始 6=挂起)\n"
            + "  7.  批量更新任务状态         (batchUpdateFlightTaskStatus, 逗号分隔多个 uuid)\n"
            + "  8.  获取设备推荐             (getDeviceRecommendation)\n"
            + "  9.  获取设备 TCA             (getDeviceTcas)\n"
            + "  10. 更新设备 TCA             (updateDeviceTcas)\n"
            + "\n"
            + "  --- 航线文件管理 (WaylineManageModule) ---\n"
            + "  11. 获取航线列表             (getWaylineList)\n"
            + "  12. 获取航线详情             (getWaylineDetail, 输入 wayline_uuid)\n"
            + "  13. 批量删除航线             (batchDeleteWaylines, 逗号分隔 uuid)\n"
            + "  14. 更新航线名称             (updateWayline)\n"
            + "  15. 批量下载航线             (batchDownloadWaylines, 逗号分隔 uuid)\n"
            + "  16. 复制航线                 (duplicateWayline, 源 uuid + 新名称)\n"
            + "  17. 监听航线导入事件         (listenImportWaylineEvent, 按 Enter 退订, 推送只落日志)\n"
            + "\n"
            + "  q.  返回上级菜单\n";
    }
}
