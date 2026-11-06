// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.module;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.HistoryHmsDataParam;
import com.dji.flighthubsdk.bean.HistoryHmsDataResult;
import com.dji.flighthubsdk.bean.HistoryHmsDataResultItem;
import com.dji.flighthubsdk.core.HMSModule;
import com.dji.flighthubsdk.demo.base.ModuleDemo;
import com.dji.flighthubsdk.demo.runtime.Await;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.utils.Utils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 历史 HMS 查询 Demo：调用 {@code session.workspace().getHMSModule().queryHistoryHMSData(...)}，
 * 打印告警列表及递归的 HMS 根因列表 {@code sub_hms_list}。
 *
 * <p>链路：{@code HTTP | POST /device/api/v1/manager/hms}。
 *
 * <p>菜单含 1 个正常查询 + 3 个参数校验负例（时间未设置 / begin&gt;end / sn 为空），
 * 用于验证 SDK 的前置校验（不合法时本地回 {@code ERROR_INVALID_PARAM}、不发请求）。
 */
public class DeviceHmsDemo extends ModuleDemo {

    @Override
    public String name() {
        return "DeviceHmsDemo";
    }

    @Override
    protected void run() throws Exception {
        if (input == null) {
            DemoLog.warn(name(), "无交互输入(input=null), 无法运行菜单式历史 HMS demo");
            return;
        }
        final HMSModule module = session.workspace().getHMSModule();
        if (module == null) {
            hint("getHMSModule() 返回 null, 无法查询历史 HMS");
            return;
        }
        final long actionTimeout = timeout("default_callback_timeout_ms");
        final String sn = rootSn();

        while (true) {
            Utils.printTitle("执行 " + name());
            System.out.print(menu());
            int choice = Utils.readIntInRange(input, "请选择(1-4, q 退出): ", 1, 4);
            if (choice == Utils.QUIT) {
                break;
            }
            long now = System.currentTimeMillis();
            switch (choice) {
                case 1:
                    query(module, actionTimeout, "正常查询",
                            new HistoryHmsDataParam(Collections.singletonList(sn),
                                    now - 30L * 24 * 3600 * 1000, now, 100, 1));
                    break;
                case 2:
                    query(module, actionTimeout, "校验: 时间未设置(0,0)",
                            new HistoryHmsDataParam(Collections.singletonList(sn), 0, 0, 100, 1));
                    break;
                case 3:
                    query(module, actionTimeout, "校验: begin>end",
                            new HistoryHmsDataParam(Collections.singletonList(sn), now, now - 1000, 100, 1));
                    break;
                case 4:
                    query(module, actionTimeout, "校验: snList 为空",
                            new HistoryHmsDataParam(Collections.<String>emptyList(),
                                    now - 30L * 24 * 3600 * 1000, now, 100, 1));
                    break;
                default:
                    break;
            }
        }
    }

    private String menu() {
        return "\n"
                + "  1. 正常查询          (now-30d ~ now, sn=" + rootSn() + ")\n"
                + "  2. 校验: 时间未设置  (createTimeBegin/End=0) → 期望 ERROR_INVALID_PARAM, 不发请求\n"
                + "  3. 校验: begin>end   → 期望 ERROR_INVALID_PARAM, 不发请求\n"
                + "  4. 校验: snList 为空 → 期望 ERROR_INVALID_PARAM, 不发请求\n"
                + "  q. 退出\n";
    }

    private void query(HMSModule module, long timeout, String label, HistoryHmsDataParam params) {
        console("\n   查询历史 HMS [" + label + "]: " + describe(params));
        final Await<FlightHubError> await = new Await<FlightHubError>(name() + ".queryHistoryHMSData");
        final AtomicReference<HistoryHmsDataResult> refResult = new AtomicReference<HistoryHmsDataResult>();
        final AtomicReference<FlightHubError> refError = new AtomicReference<FlightHubError>();
        Outcome o = runOutcome(label, timeout, await, () ->
                module.queryHistoryHMSData(params, new FlightHubCallback.QueryHistoryHMSDataCallback() {
                    @Override
                    public void onResult(FlightHubError error, HistoryHmsDataResult result) {
                        refError.set(error);
                        refResult.set(result);
                        await.set(error);
                    }
                })
        );

        FlightHubError err = refError.get();
        if (err != null) {
            both("   error.errorCode = " + err.getErrorCode()
                    + ", serverMessage=" + err.getServerMessage());
        }
        if (o == Outcome.SUCCESS) {
            printResult(refResult.get());
        }
    }

    private String describe(HistoryHmsDataParam p) {
        return "snList=" + p.getSnList()
                + ", begin=" + p.getCreateTimeBegin()
                + ", end=" + p.getCreateTimeEnd()
                + ", pageNum=" + p.getPageNum();
    }

    private String rootSn() {
        if (session.topo() == null || session.topo().getDevice() == null) {
            return "";
        }
        String sn = session.topo().getDevice().getDeviceSn();
        return sn != null ? sn : "";
    }

    private void printResult(HistoryHmsDataResult result) {
        if (result == null) {
            hint("结果为空(回调返回 null)");
            return;
        }
        List<HistoryHmsDataResultItem> list = result.getList();
        int size = list == null ? 0 : list.size();
        both("   list.size = " + size);
        if (list == null) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            printItem(i, list.get(i), 1);
        }
    }

    /** 递归打印一条 HMS 条目及其根因列表。 */
    private void printItem(int index, HistoryHmsDataResultItem item, int depth) {
        if (item == null) {
            both(indent(depth) + "[" + index + "] null");
            return;
        }
        both(indent(depth) + "[" + index + "] id=" + item.getMongoId()
                + " level=" + item.getLevel()
                + " module=" + item.getModule()
                + " message=" + item.getMessage());
        List<HistoryHmsDataResultItem> subs = item.getSubHmsList();
        if (subs == null || subs.isEmpty()) {
            both(indent(depth + 1) + "subHmsList: (空)");
            return;
        }
        both(indent(depth + 1) + "subHmsList.size = " + subs.size());
        for (int i = 0; i < subs.size(); i++) {
            printItem(i, subs.get(i), depth + 2);
        }
    }

    private String indent(int depth) {
        StringBuilder sb = new StringBuilder("   ");
        for (int i = 0; i < depth; i++) {
            sb.append("  ");
        }
        return sb.toString();
    }

    /** 结果行双写：既落日志文件(带 demo 名 tag)又回显控制台。 */
    private void both(String line) {
        DemoLog.info(name(), line);
        console(line);
    }
}
