// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.module;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.CommonPaginationData;
import com.dji.flighthubsdk.bean.ExportFlightRecordsResults;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.FlightRecordExportProgressItemData;
import com.dji.flighthubsdk.bean.GetExportFlightRecordsProgressResults;
import com.dji.flighthubsdk.core.FlightTaskRecordModule;
import com.dji.flighthubsdk.demo.base.ModuleDemo;
import com.dji.flighthubsdk.demo.runtime.Await;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.runtime.Node;
import com.dji.flighthubsdk.demo.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 飞行记录模块 Demo(菜单驱动)：导出飞行记录 + 查询导出进度。
 *
 * <p>对应 {@code FlightTaskRecordModule} 全部 2 个接口：
 *
 * <pre>
 * 主菜单
 * ├── [1] 导出飞行记录    exportFlightRecords
 * └── [2] 查询导出进度    getExportFlightRecordsProgress
 * </pre>
 *
 * <p><b>SDK 入口：</b>{@code session.workspace().getFlightTaskRecordModule()}(挂在 Workspace 上)。
 *
 * <p><b>接口串联设计：</b>[1] 导出成功返回 exportId → 用 [2] 按 exportId 查询导出进度,
 * 完成(status=1)后回显 downloadUrl 供下载。
 *
 * <p><b>设计要点：</b>
 * <ul>
 *   <li>导出参数(contentType/fileType/timeZone 等)配置驱动({@code flight_task_record} 配置节),
 *       仅 flightTaskIds 手动输入——[必填], 缺少必填字段服务端将返回错误；</li>
 *   <li>[2] 的过滤器(contentType/status/sort)从 {@code flight_task_record} 配置节读取,
 *       默认值均为"不过滤"语义(contentType 空串、status=-1、sort 空串=时间降序),
 *       按需修改 {@code query_content_type / query_status / query_sort} 字段即可；</li>
 *   <li>三态结果回显(SUCCESS/TIMEOUT/FAILED), 超时与失败明确区分。</li>
 * </ul>
 */
public class FlightTaskRecordDemo extends ModuleDemo {

    @Override
    public String name() {
        return "FlightTaskRecordDemo";
    }

    @Override
    protected void run() throws Exception {
        if (input == null) {
            DemoLog.warn(name(), "无交互输入(input=null), 无法运行菜单式飞行记录 demo");
            return;
        }

        final FlightTaskRecordModule module = session.workspace().getFlightTaskRecordModule();
        if (module == null) {
            DemoLog.error(name(), "getFlightTaskRecordModule() 返回 null");
            console("   获取 FlightTaskRecordModule 失败(返回 null), 无法进入本 demo 菜单");
            return;
        }

        final long actionTimeout = timeout("default_callback_timeout_ms");
        DemoLog.info(name(), "actionTimeout=" + actionTimeout + "ms");

        while (true) {
            Utils.printTitle("执行 " + name());
            System.out.print(menu());
            int choice = Utils.readIntInRange(input, "请选择(1-2, q 退出): ", 1, 2);
            if (choice == Utils.QUIT) {
                DemoLog.info(name(), "退出飞行记录菜单");
                break;
            }
            try {
                switch (choice) {
                    case 1: exportFlightRecords(module, actionTimeout);            break;
                    case 2: getExportFlightRecordsProgress(module, actionTimeout); break;
                    default: break;
                }
            } catch (Exception e) {
                console("   结果 -> [异常] " + Utils.describe(e));
                DemoLog.error(name(), "菜单动作执行异常: " + Utils.describe(e), e);
            }
        }
    }

    // ==================== [1] 导出飞行记录 (exportFlightRecords) ====================

    private void exportFlightRecords(FlightTaskRecordModule module, long timeout) {
        Node cfg = params.node("flight_task_record");

        String userId = cfg.str("user_id", "demo_user_id");
        String contentType = cfg.str("content_type", "summary");
        String fileNamePrefix = cfg.str("file_name", "flight_records_export");
        String fileName = fileNamePrefix + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String timeZone = cfg.str("time_zone", "Asia/Shanghai");
        String locale = cfg.str("locale", "zh");

        List<String> fileTypeList = new ArrayList<>();
        for (Object ft : cfg.list("file_type")) {
            if (ft != null) fileTypeList.add(ft.toString());
        }
        if (fileTypeList.isEmpty()) {
            fileTypeList.add("Excel");
        }

        // 唯一需要用户输入的字段: [必填], 缺少必填字段将返回错误(见 exportFlightRecords Javadoc)
        List<String> flightTaskIds = parseCsv(Utils.readLineEcho(input, "   请输入 flightTaskIds(必填, 逗号分隔多个): "));
        if (flightTaskIds.isEmpty()) {
            hint("flightTaskIds 不能为空([必填] 字段, 缺少将返回错误)");
            return;
        }

        console("\n   导出飞行记录参数(flight_task_record 配置节):");
        console("     userId      = " + userId);
        console("     contentType = " + contentType);
        console("     fileName    = " + fileName);
        console("     fileType    = " + fileTypeList);
        console("     timeZone    = " + timeZone);
        console("     taskIds     = " + flightTaskIds);

        final Await<FlightHubError> await = new Await<>(name() + ".exportFlightRecords");
        final AtomicReference<ExportFlightRecordsResults> ref = new AtomicReference<>();
        Outcome o = runOutcome("导出飞行记录", timeout, await, () ->
            module.exportFlightRecords(userId, contentType, fileName, timeZone, locale,
                fileTypeList, flightTaskIds, null,
                new FlightHubCallback.ExportFlightRecordsCallback() {
                    @Override
                    public void onResult(FlightHubError error, ExportFlightRecordsResults results) {
                        ref.set(results);
                        await.set(error);
                    }
                })
        );

        if (o == Outcome.SUCCESS) {
            ExportFlightRecordsResults r = ref.get();
            if (r != null) {
                console("   导出成功: exportId=" + r.getExportId());
                hint("可用 exportId 查询导出进度(菜单 [2])");
            }
        }
    }

    // ==================== [2] 查询导出进度 (getExportFlightRecordsProgress) ====================

    private void getExportFlightRecordsProgress(FlightTaskRecordModule module, long timeout) {
        String line = Utils.readLineEcho(input, "   请输入 exportId: ");
        final String exportId = line == null ? "" : line.trim();

        Node cfg = params.node("flight_task_record");
        final int pageSize      = cfg.intVal("list_page_size",    50);
        final String filterContentType = cfg.str("query_content_type", "");
        final int    filterStatus      = cfg.intVal("query_status",    -1);
        final String filterSort        = cfg.str("query_sort",         "");

        // 过滤器语义(status 取值定义见 C++ message.h 的 GetExportFlightRecordsProgressParams):
        //  contentType="" 不按类型过滤(summary/details 都查)
        //  status=-1     查所有状态(0=导出中 1=已完成 2=失败 3=已取消)
        //  sort=""       默认按时间降序("ASC"=升序)
        console("\n   查询导出进度:");
        console("     exportId    = " + (exportId.isEmpty() ? "(全部)" : exportId));
        console("     contentType = " + (filterContentType.isEmpty() ? "(全部)" : filterContentType));
        console("     status      = " + (filterStatus == -1
                ? "(全部)" : filterStatus + "(" + describeExportStatus(filterStatus) + ")"));
        console("     sort        = " + (filterSort.isEmpty() ? "(时间降序)" : filterSort));

        final Await<FlightHubError> await = new Await<>(name() + ".getExportFlightRecordsProgress");
        final AtomicReference<GetExportFlightRecordsProgressResults> ref = new AtomicReference<>();
        Outcome o = runOutcome("查询导出进度", timeout, await, () ->
            module.getExportFlightRecordsProgress(filterContentType, filterStatus, filterSort, 1, pageSize, exportId,
                new FlightHubCallback.GetExportFlightRecordsProgressCallback() {
                    @Override
                    public void onResult(FlightHubError error, GetExportFlightRecordsProgressResults results) {
                        ref.set(results);
                        await.set(error);
                    }
                })
        );

        if (o == Outcome.SUCCESS) {
            GetExportFlightRecordsProgressResults r = ref.get();
            if (r != null) {
                CommonPaginationData p = r.getPagination();
                if (p != null) {
                    console("   分页: page=" + p.getPage() + " pageSize=" + p.getPageSize()
                        + " total=" + p.getTotal());
                }

                List<FlightRecordExportProgressItemData> items = r.getList();
                int count = items != null ? items.size() : 0;
                console("   本页 " + count + " 条记录");
                if (items != null) {
                    for (int i = 0; i < items.size(); i++) {
                        FlightRecordExportProgressItemData item = items.get(i);
                        console("   [" + (i + 1) + "] exportId=" + item.getExportId()
                            + "  status=" + item.getStatus() + "(" + describeExportStatus(item.getStatus()) + ")"
                            + "  progress=" + item.getProgress() + "%");
                        console("       fileName=" + item.getFileName()
                            + "  contentType=" + item.getContentType()
                            + "  createdAt=" + item.getCreatedAt()
                            + "  userId=" + item.getUserId());
                        if (item.getDownloadUrl() != null && !item.getDownloadUrl().isEmpty()) {
                            console("       downloadUrl=" + item.getDownloadUrl());
                        }
                        // 原生侧该字段默认 -1(未设置); SDK 未定义有效码域, demo 仅在为正时回显, 避免把默认值误当失败码
                        if (item.getFailedReasonCode() > 0) {
                            console("       failedReasonCode=" + item.getFailedReasonCode());
                        }
                    }
                }
            }
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

    /** 导出状态枚举描述(0=导出中 1=已完成 2=失败 3=已取消, 取值定义见 C++ message.h 的 GetExportFlightRecordsProgressParams)。 */
    private static String describeExportStatus(int status) {
        switch (status) {
            case 0:  return "导出中";
            case 1:  return "已完成";
            case 2:  return "失败";
            case 3:  return "已取消";
            default: return "未知";
        }
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
     * 主菜单文案：2 个菜单项与 {@code FlightTaskRecordModule} 同名接口一一对应(见类 Javadoc 菜单树)。
     * 增删菜单项时须同步 {@link #run()} 中的 switch 分支与 {@code readIntInRange} 的取值上界。
     */
    private String menu() {
        return "\n"
            + "  === 飞行记录 (Flight Task Record) ===\n"
            + "  1.  导出飞行记录   (exportFlightRecords, 输入 flightTaskIds)\n"
            + "       fileType 如 [\"Excel\"](配置 flight_task_record.file_type)\n"
            + "  2.  查询导出进度   (getExportFlightRecordsProgress, 输入 exportId)\n"
            + "\n"
            + "  q.  返回上级菜单\n";
    }
}
