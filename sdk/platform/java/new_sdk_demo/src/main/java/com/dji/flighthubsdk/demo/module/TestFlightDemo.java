// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.module;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.core.TestFlightModule;
import com.dji.flighthubsdk.demo.base.Menu;
import com.dji.flighthubsdk.demo.base.ModuleDemo;
import com.dji.flighthubsdk.demo.runtime.Await;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.utils.Utils;
import com.dji.flighthubsdk.generated.CreateTestFlightTaskParams;
import com.dji.flighthubsdk.generated.CreateTestFlightTaskResponse;
import com.dji.flighthubsdk.generated.LongLongVector;

/** 试飞任务 Demo：调用 createTestFlightTask 并展示 219019 校验码。 */
public class TestFlightDemo extends ModuleDemo {

    @Override
    public String name() {
        return "TestFlightDemo";
    }

    @Override
    protected void run() throws Exception {
        if (input == null) {
            DemoLog.warn(name(), "无交互输入(input=null), 无法运行试飞任务 demo");
            return;
        }
        if (!session.hasDrone()) {
            hint("当前设备无飞行器(Drone), 试飞任务需要飞行器设备, 无法运行");
            return;
        }

        final TestFlightModule module = session.drone().getTestFlightModule();
        if (module == null) {
            hint("getTestFlightModule() 返回 null, 无法运行");
            return;
        }

        Menu.of(this, "执行 " + name())
                .banner("=== 试飞任务 (Test Flight) ===")
                .header(() -> console("(后端固定 task_type=试飞任务, rth_altitude=60, 仅支持单航线)"))
                .item("创建试飞任务 (createTestFlightTask)", () -> createTestFlightTask(module, defaultTimeout()))
                .exitLog("退出试飞任务菜单")
                .run();
    }

    private void createTestFlightTask(TestFlightModule module, long timeout) {
        String projectId = Utils.readLineEcho(input, "   请输入 projectId(q 取消): ");
        if (projectId == null || projectId.trim().isEmpty() || "q".equalsIgnoreCase(projectId.trim())) {
            return;
        }
        String waylineUuid = Utils.readLineEcho(input, "   请输入 wayline_uuid(q 取消): ");
        if (waylineUuid == null || waylineUuid.trim().isEmpty() || "q".equalsIgnoreCase(waylineUuid.trim())) {
            return;
        }
        String defaultSn = session.droneSn();
        String sn = Utils.readLineEcho(input, "   请输入 drone_sn(回车使用 " + defaultSn + "): ");
        if (sn == null || sn.trim().isEmpty()) {
            sn = defaultSn;
        }
        if (sn == null || sn.isEmpty()) {
            hint("drone_sn 为空, 已取消");
            return;
        }
        if (!confirmDangerous("创建真实试飞任务，可能触发设备执行，确认继续？")) {
            return;
        }

        CreateTestFlightTaskParams p = new CreateTestFlightTaskParams();
        p.setName("SDK test flight");
        p.setWayline_uuid(waylineUuid.trim());
        p.setSn(sn.trim());
        p.setTime_zone("Asia/Shanghai");
        p.setOut_of_control_action_in_flight(1);
        p.setWayline_precision_type(1);
        p.setWayline_duration(60.0);
        p.setCloud_to_cloud_id("");

        final Await<FlightHubError> await = new Await<>(name() + ".createTestFlightTask");
        // generated response 是 SWIG 原生对象的代理，回调返回后底层对象会被释放；
        // 必须在回调内把字段复制成普通 Java 值，不能在 await 之后继续读 response。
        final boolean[] hasResponse = new boolean[1];
        final boolean[] taskCreated = new boolean[1];
        final String[] uuid = new String[1];
        final long[] beginAt = new long[1];
        final long[] endAt = new long[1];
        final String[] codesText = new String[1];
        console("\n>> [创建试飞任务] 执行中 ...");
        try {
            module.createTestFlightTask(projectId.trim(), p, new FlightHubCallback.CreateTestFlightTaskCallback() {
                @Override
                public void onResult(CreateTestFlightTaskResponse response, FlightHubError error) {
                    if (response != null) {
                        hasResponse[0] = true;
                        taskCreated[0] = response.getIs_task_created();
                        uuid[0] = response.getUuid();
                        beginAt[0] = response.getBegin_at();
                        endAt[0] = response.getEnd_at();
                        codesText[0] = codes(response.getWayline_validity_check_codes());
                    }
                    await.set(error);
                }
            });
        } catch (Exception e) {
            hint("下发失败: " + Utils.describe(e));
            return;
        }

        FlightHubError error;
        try {
            error = await.await(timeout);
        } catch (Exception e) {
            hint("等待超时: " + Utils.describe(e));
            return;
        }

        boolean success = (error == null) || error.isSuccess();
        if (!success) {
            console("   结果 -> [失败] errorCode=" + error.getErrorCode()
                    + " serverCode=" + error.getServerCode()
                    + " message=" + error.getServerMessage());
        } else {
            console("   结果 -> [成功]");
        }
        if (hasResponse[0]) {
            console("   is_task_created=" + taskCreated[0]
                    + " uuid=" + uuid[0]
                    + " begin_at=" + beginAt[0]
                    + " end_at=" + endAt[0]);
            console("   wayline_validity_check_codes=" + codesText[0]);
            if (!taskCreated[0] && error != null && error.getServerCode() == 219019) {
                console("   (219019 场景：任务未创建，仅返回航线校验码)");
            }
        }
    }

    private String codes(LongLongVector vector) {
        if (vector == null || vector.size() == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(vector.get(i));
        }
        return sb.append(']').toString();
    }
}
