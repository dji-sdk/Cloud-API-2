// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.module;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.CreateSelfDiagnosisParams;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.GetSelfDiagnosisReportListParams;
import com.dji.flighthubsdk.bean.GetSelfDiagnosisReportListResults;
import com.dji.flighthubsdk.bean.GetSelfDiagnosisReportResults;
import com.dji.flighthubsdk.bean.OperateSelfDiagnosisParams;
import com.dji.flighthubsdk.bean.SelfDiagnosisDeviceTopoParams;
import com.dji.flighthubsdk.core.DiagnosisManagementModule;
import com.dji.flighthubsdk.demo.base.Menu;
import com.dji.flighthubsdk.demo.base.ModuleDemo;
import com.dji.flighthubsdk.demo.runtime.Await;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.utils.Utils;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 设备定损 Demo：列表 / 详情 / 创建 / 取消 / 删除。
 *
 * <p>用于验证 SDK 是否已切到后端 -private 路由，以及操作字段是否为 operation。</p>
 */
public class DiagnosisDemo extends ModuleDemo {

    private String lastReportUuid;

    @Override
    public String name() {
        return "DiagnosisDemo";
    }

    @Override
    protected void run() throws Exception {
        if (input == null) {
            DemoLog.warn(name(), "无交互输入(input=null), 无法运行设备定损 demo");
            return;
        }
        final DiagnosisManagementModule module = session.workspace().getDiagnosisManagementModule();
        if (module == null) {
            hint("getDiagnosisManagementModule() 返回 null");
            return;
        }
        final long actionTimeout = defaultTimeout();

        Menu.of(this, "执行 " + name())
                .banner("=== 设备定损 (Diagnosis) ===")
                .header(() -> {
                    if (lastReportUuid != null) {
                        console("(本次会话最近报告: " + lastReportUuid + ")");
                    }
                })
                .item("查询定损报告列表 (getSelfDiagnosisReportList, 验证 -private 路径/无 origin)", () -> listReports(module, actionTimeout))
                .item("查询定损报告详情 (getSelfDiagnosisReport, 输入 report_uuid)", () -> detailReport(module, actionTimeout))
                .item("创建定损报告   (createSelfDiagnosis, 会触发设备自检, 需二次确认)", () -> createReport(module, actionTimeout))
                .item("取消定损报告   (operateSelfDiagnosis: cancel)", () -> operateReport(module, actionTimeout, "cancel"))
                .item("删除定损报告   (operateSelfDiagnosis: delete)", () -> operateReport(module, actionTimeout, "delete"))
                .run();
    }

    private void listReports(DiagnosisManagementModule module, long timeout) {
        GetSelfDiagnosisReportListParams p = new GetSelfDiagnosisReportListParams();
        p.setPage(1);
        p.setPageSize(20);
        // 故意设置 origin，验证 SDK 不再上送
        p.setOrigin(1);

        final Await<FlightHubError> await = new Await<>(name() + ".getSelfDiagnosisReportList");
        final AtomicReference<GetSelfDiagnosisReportListResults> ref = new AtomicReference<>();
        Outcome o = runOutcome("查询定损报告列表", timeout, await, () ->
                module.getSelfDiagnosisReportList(p, new FlightHubCallback.GetSelfDiagnosisReportListCallback() {
                    @Override
                    public void onResult(FlightHubError error, GetSelfDiagnosisReportListResults result) {
                        ref.set(result);
                        await.set(error);
                    }
                })
        );
        if (o == Outcome.SUCCESS) {
            GetSelfDiagnosisReportListResults r = ref.get();
            List<GetSelfDiagnosisReportResults> list = r != null ? r.getList() : null;
            int count = list == null ? 0 : list.size();
            console("   pagination=" + (r != null ? r.getPagination() : null)
                    + " count=" + count);
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    GetSelfDiagnosisReportResults item = list.get(i);
                    console("   [" + i + "] report_uuid=" + item.getReportUuid()
                            + " device_sn=" + item.getDeviceSn()
                            + " device_type=" + item.getDeviceType()
                            + " status=" + item.getStatus()
                            + " result_code=" + item.getResultCode()
                            + " cloud_submit_status=" + item.getCloudSubmitStatus()
                            + " qr_code_url=" + item.getQrCodeUrl()
                            + " organization_uuid=" + item.getOrganizationUuid()
                            + " created_at=" + item.getCreatedAt()
                            + " updated_at=" + item.getUpdatedAt());
                }
            }
        }
    }

    private void detailReport(DiagnosisManagementModule module, long timeout) {
        String uuid = Utils.readLineEcho(input, "   请输入 report_uuid(q 取消): ");
        if (uuid == null || uuid.trim().isEmpty() || "q".equalsIgnoreCase(uuid.trim())) {
            return;
        }
        uuid = uuid.trim();
        final String reportUuid = uuid;
        final Await<FlightHubError> await = new Await<>(name() + ".getSelfDiagnosisReport");
        final AtomicReference<GetSelfDiagnosisReportResults> ref = new AtomicReference<>();
        Outcome o = runOutcome("查询定损报告详情", timeout, await, () ->
                module.getSelfDiagnosisReport(reportUuid, new FlightHubCallback.GetSelfDiagnosisReportCallback() {
                    @Override
                    public void onResult(FlightHubError error, GetSelfDiagnosisReportResults result) {
                        ref.set(result);
                        await.set(error);
                    }
                })
        );
        if (o == Outcome.SUCCESS) {
            GetSelfDiagnosisReportResults r = ref.get();
            if (r != null) {
                lastReportUuid = r.getReportUuid();
                console("   report_uuid=" + r.getReportUuid()
                        + " device_sn=" + r.getDeviceSn()
                        + " device_type=" + r.getDeviceType()
                        + " status=" + r.getStatus()
                        + " result_code=" + r.getResultCode()
                        + " create_time=" + r.getCreateTime()
                        + " qr_code_content=" + r.getQrCodeContent());
                console("   cloud_submit_status=" + r.getCloudSubmitStatus()
                        + " qr_code_url=" + r.getQrCodeUrl()
                        + " qr_code_sign=" + r.getQrCodeSign()
                        + " qr_code_nonce=" + r.getQrCodeNonce());
                console("   device_callback_updated_at=" + r.getDeviceCallbackUpdatedAt()
                        + " created_at=" + r.getCreatedAt()
                        + " updated_at=" + r.getUpdatedAt()
                        + " organization_uuid=" + r.getOrganizationUuid()
                        + " project_uuid=" + r.getProjectUuid());
            }
        }
    }

    private void createReport(DiagnosisManagementModule module, long timeout) {
        if (!confirmDangerous("创建定损报告会触发设备自检/电机动作，确认执行？")) {
            return;
        }
        String defaultSn = session.hasDock() ? session.dockSn() : session.droneSn();
        String sn = Utils.readLineEcho(input, "   请输入 device_sn(回车使用 " + defaultSn + "): ");
        if (sn == null || sn.trim().isEmpty()) {
            sn = defaultSn;
        }
        if (sn == null || sn.isEmpty()) {
            hint("device_sn 为空，已取消");
            return;
        }
        CreateSelfDiagnosisParams p = new CreateSelfDiagnosisParams();
        p.setDeviceSn(sn);
        p.setType(0);
        p.setPart(0);
        p.setIndex(0);
        p.setMotorAction(0);
        p.setDeviceTopo(new SelfDiagnosisDeviceTopoParams());

        final Await<FlightHubError> await = new Await<>(name() + ".createSelfDiagnosis");
        Outcome o = runOutcome("创建定损报告", timeout, await, () ->
                module.createSelfDiagnosis(p, new FlightHubCallback.CreateSelfDiagnosisCallback() {
                    @Override
                    public void onResult(FlightHubError error) {
                        await.set(error);
                    }
                })
        );
        if (o == Outcome.SUCCESS) {
            hint("创建请求成功，请用 [1] 查询列表获取 report_uuid");
        }
    }

    private void operateReport(DiagnosisManagementModule module, long timeout, String method) {
        String uuid = Utils.readLineEcho(input, "   请输入 report_uuid(q 取消): ");
        if (uuid == null || uuid.trim().isEmpty() || "q".equalsIgnoreCase(uuid.trim())) {
            return;
        }
        uuid = uuid.trim();
        if (!confirmDangerous(method + " 定损报告：" + uuid + "，确认执行？")) {
            return;
        }
        OperateSelfDiagnosisParams p = new OperateSelfDiagnosisParams();
        p.setReportUuid(uuid);
        p.setReportCmdMethod(method);

        final Await<FlightHubError> await = new Await<>(name() + ".operateSelfDiagnosis." + method);
        Outcome o = runOutcome(method + " 定损报告", timeout, await, () ->
                module.operateSelfDiagnosis(p, new FlightHubCallback.OperateSelfDiagnosisCallback() {
                    @Override
                    public void onResult(FlightHubError error) {
                        await.set(error);
                    }
                })
        );
        if (o == Outcome.SUCCESS) {
            hint(method + " 成功");
        }
    }
}
