// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.module;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.AIInsideAlgorithmDetail;
import com.dji.flighthubsdk.bean.AIInsideDeviceItem;
import com.dji.flighthubsdk.bean.AIInsideModelItem;
import com.dji.flighthubsdk.bean.AIInsideOperateDeviceResult;
import com.dji.flighthubsdk.bean.FileUploadAIInsideAlgorithmParam;
import com.dji.flighthubsdk.bean.FileUploadAIInsideAlgorithmResult;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.GetAIInsideModelDevicesParam;
import com.dji.flighthubsdk.bean.GetAIInsideModelDevicesResult;
import com.dji.flighthubsdk.bean.GetAIInsideModelDevicesSummaryParam;
import com.dji.flighthubsdk.bean.GetAIInsideModelDevicesSummaryResult;
import com.dji.flighthubsdk.bean.GetAIInsideModelsResult;
import com.dji.flighthubsdk.bean.OperateAIInsideModelParam;
import com.dji.flighthubsdk.bean.OperateAIInsideModelResult;
import com.dji.flighthubsdk.bean.UploadAIInsideAlgorithmFileParam;
import com.dji.flighthubsdk.bean.UploadAIInsideAlgorithmFileResult;
import com.dji.flighthubsdk.core.AIInsideManageModule;
import com.dji.flighthubsdk.core.Workspace;
import com.dji.flighthubsdk.demo.base.ModuleDemo;
import com.dji.flighthubsdk.demo.runtime.Await;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * AI Inside 机载算法管理 Demo（PAAS 纯血场景，workspace 级接口，无需选设备）。
 *
 * <p>SDK 入口：{@code Workspace.getAIInsideManageModule()}。覆盖核心 7 个接口：
 * 获取算法列表、获取绑定设备详情、可操作设备统计、批量 load、批量 unload、
 * 一站式上传算法文件、处理已上传算法文件。
 * 参考老 demo {@code FlightHubSDKDemo.handleAIInsideTest()} 实现。
 *
 * <p>load/unload 的 drone SN：交互输入（逗号分隔多个），<b>留空回车 = 对该算法全部可操作设备下发</b>
 *（{@code drone_sn_list} 为空即全量，勿用占位符）。
 */
public class AIInsideDemo extends ModuleDemo {

    @Override
    public String name() {
        return "AIInsideDemo";
    }

    @Override
    public boolean requiresDevice() {
        return false;
    }

    @Override
    protected void run() throws Exception {
        Workspace ws = runtime.workspace();
        if (ws == null) {
            DemoLog.error(name(), "workspace 为 null");
            console("获取 workspace 失败");
            return;
        }
        final AIInsideManageModule module = ws.getAIInsideManageModule();
        if (module == null) {
            DemoLog.error(name(), "getAIInsideManageModule() 返回 null");
            console("获取 AIInsideManageModule 失败");
            return;
        }
        final long timeout = timeout("default_callback_timeout_ms");

        // 非交互环境：依次跑只读接口（1），不做下发，避免误操作。
        if (input == null) {
            getModels(module, timeout);
            return;
        }

        while (true) {
            Utils.printTitle("执行 " + name());
            System.out.print(
                    "  [1] 获取算法列表                  getAIInsideModels\n"
                    + "  [2] 获取算法绑定设备详情          getAIInsideModelDevices\n"
                    + "  [3] 获取可操作设备数量统计        getAIInsideModelDevicesSummary\n"
                    + "  [4] 批量下发装载指令 load         operateAIInsideModel\n"
                    + "  [5] 批量下发卸载指令 unload       operateAIInsideModel\n"
                    + "  [6] 上传算法文件(ZIP)             uploadAlgorithmFile\n"
                    + "  [7] 处理已上传算法文件            fileUploadAIInsideAlgorithm\n");
            int choice = Utils.readIntInRange(input, "请选择(1-7, q 退出): ", 1, 7);
            if (choice == Utils.QUIT) {
                break;
            }
            try {
                switch (choice) {
                    case 1: getModels(module, timeout); break;
                    case 2: getModelDevices(module, timeout); break;
                    case 3: getSummary(module, timeout); break;
                    case 4: operate(module, "load", timeout); break;
                    case 5: operate(module, "unload", timeout); break;
                    case 6: uploadAlgorithmFile(module, timeout); break;
                    case 7: fileUploadAIInsideAlgorithm(module, timeout); break;
                    default: break;
                }
            } catch (Exception e) {
                console("   结果 -> [异常] " + Utils.describe(e));
                DemoLog.error(name(), "菜单动作异常: " + Utils.describe(e), e);
            }
        }
    }

    /** [1] 获取算法列表。 */
    private void getModels(AIInsideManageModule module, long timeoutMs) {
        console("\n>> [getAIInsideModels] 执行中 ...");
        final Await<Object[]> await = new Await<Object[]>("getAIInsideModels");
        module.getAIInsideModels(new FlightHubCallback.GetAIInsideModelsCallback() {
            @Override
            public void onResult(FlightHubError error, GetAIInsideModelsResult result) {
                await.set(new Object[] { error, result });
            }
        });
        Object[] pair = await.await(timeoutMs);
        FlightHubError err = (FlightHubError) pair[0];
        GetAIInsideModelsResult result = (GetAIInsideModelsResult) pair[1];
        console("   error: " + describeError(err));
        if (result == null || result.getModels() == null) {
            return;
        }
        console("   算法总数: " + result.getModels().size());
        for (int i = 0; i < result.getModels().size(); i++) {
            AIInsideModelItem m = result.getModels().get(i);
                    console("   [" + i + "] signed_id=" + m.getSignedId()
                    + ", model_name=" + m.getModelName()
                    + ", status=" + m.getStatus()
                    + ", signed_version=" + m.getSignedVersion()
                    + ", sn_list.size=" + (m.getSnList() != null ? m.getSnList().size() : 0)
                    + ", image_source=" + m.getImageSource()
                    + ", model_types=" + m.getModelTypes()
                    + ", model_combination=" + m.getModelCombination()
                    + ", expired_utc=" + m.getExpiredUtc());
        }
    }

    /** [2] 获取算法绑定设备详情（分页）。 */
    private void getModelDevices(AIInsideManageModule module, long timeoutMs) {
        String signedId = askSignedId();
        GetAIInsideModelDevicesParam param = new GetAIInsideModelDevicesParam();
        param.setSignedId(signedId);
        param.setPage(1);
        param.setPageSize(20);
        console("\n>> [getAIInsideModelDevices] signed_id=" + signedId + " 执行中 ...");
        final Await<Object[]> await = new Await<Object[]>("getAIInsideModelDevices");
        module.getAIInsideModelDevices(param, new FlightHubCallback.GetAIInsideModelDevicesCallback() {
            @Override
            public void onResult(FlightHubError error, GetAIInsideModelDevicesResult result) {
                await.set(new Object[] { error, result });
            }
        });
        Object[] pair = await.await(timeoutMs);
        FlightHubError err = (FlightHubError) pair[0];
        GetAIInsideModelDevicesResult result = (GetAIInsideModelDevicesResult) pair[1];
        console("   error: " + describeError(err)
                + ", total=" + (result != null ? result.getTotal() : 0)
                + ", page=" + (result != null ? result.getPage() : 0));
        if (result == null || result.getList() == null) {
            return;
        }
        for (int i = 0; i < result.getList().size(); i++) {
            AIInsideDeviceItem dev = result.getList().get(i);
            com.dji.flighthubsdk.bean.AIInsideAssociateDrone drone = dev.getAssociateDroneDeviceInfo();
            com.dji.flighthubsdk.bean.AIInsideDeviceModel dm = dev.getDeviceModel();
            console("   [" + i + "] dock_sn=" + dev.getDeviceSn()
                    + ", online=" + dev.isDeviceOnlineStatus()
                    + ", working_status=" + dev.getDeviceWorkingStatus()
                    + ", device_model=" + describeDeviceModel(dm)
                    + ", operate_status="
                    + (dev.getOperateAiModel() != null ? dev.getOperateAiModel().getStatus() : "")
                    + ", operate_progress="
                    + (dev.getOperateAiModel() != null && dev.getOperateAiModel().getProgress() != null
                            ? dev.getOperateAiModel().getProgress().getPercent() : 0)
                    + ", drone_sn=" + (drone != null ? drone.getDeviceSn() : "")
                    + ", drone_working_status=" + (drone != null ? drone.getDeviceWorkingStatus() : 0)
                    + ", drone_device_model=" + (drone != null ? describeDeviceModel(drone.getDeviceModel()) : "")
                    + ", ai_model_status="
                    + (drone != null && drone.getAiModelInfo() != null
                            ? drone.getAiModelInfo().getStatus() : ""));
        }
    }

    private String describeDeviceModel(com.dji.flighthubsdk.bean.AIInsideDeviceModel dm) {
        if (dm == null) {
            return "null";
        }
        return dm.getDeviceModelKey() + "/" + dm.getDomain() + "/" + dm.getType()
                + "/" + dm.getSubType() + "/" + dm.getName()
                + "/" + dm.getDeviceModelClass() + "/" + dm.getDevelopCode();
    }

    /** [3] 获取可操作设备数量统计。 */
    private void getSummary(AIInsideManageModule module, long timeoutMs) {
        String signedId = askSignedId();
        String opType = Utils.readLine(input, " 请输入 operation_type (load/update，留空=load): ");
        if (opType == null) {
            opType = "";
        }
        opType = opType.trim();
        if (opType.isEmpty() || "0".equals(opType)) {
            opType = "load";
        }
        GetAIInsideModelDevicesSummaryParam param = new GetAIInsideModelDevicesSummaryParam();
        param.setSignedId(signedId);
        param.setOperationType(opType);
        console("\n>> [getAIInsideModelDevicesSummary] signed_id=" + signedId
                + ", operation_type=" + opType + " 执行中 ...");
        final Await<Object[]> await = new Await<Object[]>("getAIInsideModelDevicesSummary");
        module.getAIInsideModelDevicesSummary(param,
                new FlightHubCallback.GetAIInsideModelDevicesSummaryCallback() {
            @Override
            public void onResult(FlightHubError error, GetAIInsideModelDevicesSummaryResult result) {
                await.set(new Object[] { error, result });
            }
        });
        Object[] pair = await.await(timeoutMs);
        FlightHubError err = (FlightHubError) pair[0];
        GetAIInsideModelDevicesSummaryResult result = (GetAIInsideModelDevicesSummaryResult) pair[1];
        console("   error: " + describeError(err)
                + ", total_devices=" + (result != null ? result.getTotalDevices() : 0)
                + ", operable_devices=" + (result != null ? result.getOperableDevices() : 0));
    }

    /** [4]/[5] 批量下发 load / unload。drone SN 留空=对全部可操作设备下发。 */
    private void operate(AIInsideManageModule module, String operationType, long timeoutMs) {
        String signedId = askSignedId();
        String snInput = Utils.readLine(input,
                " 请输入 drone SN (逗号分隔多个，留空=全部可操作设备): ");
        List<String> droneSnList = new ArrayList<String>();
        if (snInput != null && !snInput.trim().isEmpty()) {
            for (String s : snInput.split(",")) {
                String sn = s.trim();
                if (!sn.isEmpty()) {
                    droneSnList.add(sn);
                }
            }
        }
        OperateAIInsideModelParam param = new OperateAIInsideModelParam();
        param.setSignedId(signedId);
        param.setOperationType(operationType);
        param.setDroneSNList(droneSnList);
        console("\n>> [operateAIInsideModel(" + operationType + ")] signed_id=" + signedId
                + ", drone_sn_list=" + droneSnList
                + (droneSnList.isEmpty() ? " (全部可操作设备)" : "") + " 执行中 ...");
        DemoLog.info(name(), "operateAIInsideModel " + operationType + " signed_id=" + signedId
                + ", drone_sn_list=" + droneSnList);
        final Await<Object[]> await = new Await<Object[]>("operateAIInsideModel");
        module.operateAIInsideModel(param, new FlightHubCallback.OperateAIInsideModelCallback() {
            @Override
            public void onResult(FlightHubError error, OperateAIInsideModelResult result) {
                await.set(new Object[] { error, result });
            }
        });
        Object[] pair = await.await(timeoutMs);
        FlightHubError err = (FlightHubError) pair[0];
        OperateAIInsideModelResult result = (OperateAIInsideModelResult) pair[1];
        console("   error: " + describeError(err));
        if (result == null || result.getResults() == null) {
            return;
        }
        for (AIInsideOperateDeviceResult r : result.getResults()) {
            console("   drone_sn=" + r.getDroneSn() + ", code=" + r.getCode());
        }
    }

    /** [6] 一站式上传算法文件：获取 STS → 上传 ZIP → 通知后端解压入库。 */
    private void uploadAlgorithmFile(AIInsideManageModule module, long timeoutMs) {
        String path = Utils.readLine(input, " 请输入本地算法 ZIP 绝对路径(q 取消): ");
        if (path == null || path.trim().isEmpty() || "q".equalsIgnoreCase(path.trim())) {
            hint("已取消上传");
            return;
        }
        UploadAIInsideAlgorithmFileParam param = new UploadAIInsideAlgorithmFileParam();
        param.setFilePath(path.trim());

        console("\n>> [uploadAlgorithmFile] file=" + path.trim() + " 执行中 ...");
        final Await<Object[]> await = new Await<Object[]>("uploadAlgorithmFile");
        module.uploadAlgorithmFile(param, new FlightHubCallback.UploadAlgorithmFileCallback() {
            @Override
            public void onResult(FlightHubError error, UploadAIInsideAlgorithmFileResult result) {
                await.set(new Object[] { error, result });
            }
        });
        Object[] pair = await.await(timeoutMs);
        FlightHubError err = (FlightHubError) pair[0];
        UploadAIInsideAlgorithmFileResult result = (UploadAIInsideAlgorithmFileResult) pair[1];
        console("   error: " + describeError(err));
        if (result != null && result.getDetail() != null) {
            AIInsideAlgorithmDetail d = result.getDetail();
            console("   created_at=" + d.getCreatedAt() + ", updated_at=" + d.getUpdatedAt());
        }
    }

    /** [7] 处理已上传算法文件：用 MinIO file_key 通知后端解压入库。 */
    private void fileUploadAIInsideAlgorithm(AIInsideManageModule module, long timeoutMs) {
        String fileKey = Utils.readLine(input, " 请输入已上传文件的 file_key(q 取消): ");
        if (fileKey == null || fileKey.trim().isEmpty() || "q".equalsIgnoreCase(fileKey.trim())) {
            hint("已取消处理");
            return;
        }
        FileUploadAIInsideAlgorithmParam param = new FileUploadAIInsideAlgorithmParam();
        param.setFileKey(fileKey.trim());

        console("\n>> [fileUploadAIInsideAlgorithm] file_key=" + fileKey.trim() + " 执行中 ...");
        final Await<Object[]> await = new Await<Object[]>("fileUploadAIInsideAlgorithm");
        module.fileUploadAIInsideAlgorithm(param,
                new FlightHubCallback.FileUploadAIInsideAlgorithmCallback() {
            @Override
            public void onResult(FlightHubError error, FileUploadAIInsideAlgorithmResult result) {
                await.set(new Object[] { error, result });
            }
        });
        Object[] pair = await.await(timeoutMs);
        FlightHubError err = (FlightHubError) pair[0];
        FileUploadAIInsideAlgorithmResult result = (FileUploadAIInsideAlgorithmResult) pair[1];
        console("   error: " + describeError(err));
        if (result != null && result.getDetail() != null) {
            AIInsideAlgorithmDetail d = result.getDetail();
            console("   created_at=" + d.getCreatedAt() + ", updated_at=" + d.getUpdatedAt());
        }
    }

    /** 读取 signed_id（留空回退演示默认值 algo_signed_001，与老 demo 一致）。 */
    private String askSignedId() {
        String signedId = Utils.readLine(input, " 请输入 signed_id (算法签名ID): ");
        if (signedId == null || signedId.trim().isEmpty()) {
            return "algo_signed_001";
        }
        return signedId.trim();
    }
}
