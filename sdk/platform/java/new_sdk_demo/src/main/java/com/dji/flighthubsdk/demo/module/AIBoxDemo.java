// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.module;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.AIBoxExportFileParam;
import com.dji.flighthubsdk.bean.AIBoxExportFileProgressParam;
import com.dji.flighthubsdk.bean.AIBoxFetchAppListParam;
import com.dji.flighthubsdk.bean.AIBoxFetchAppListProgressParam;
import com.dji.flighthubsdk.bean.AIBoxInfoListResult;
import com.dji.flighthubsdk.bean.AIBoxInfoResult;
import com.dji.flighthubsdk.bean.AIBoxInstallAppParam;
import com.dji.flighthubsdk.bean.AIBoxInstallAppProgressParam;
import com.dji.flighthubsdk.bean.AIBoxOperateAppParam;
import com.dji.flighthubsdk.bean.ExportFileProgressResult;
import com.dji.flighthubsdk.bean.ExportFileResult;
import com.dji.flighthubsdk.bean.FetchAppListProgressResult;
import com.dji.flighthubsdk.bean.FetchAppListResult;
import com.dji.flighthubsdk.bean.FetchFileDownloadUrlParam;
import com.dji.flighthubsdk.bean.FileDownloadUrlListResult;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.InstallAppProgressResult;
import com.dji.flighthubsdk.bean.InstallAppResult;
import com.dji.flighthubsdk.bean.OperateAppResult;
import com.dji.flighthubsdk.bean.UploadFileResult;
import com.dji.flighthubsdk.core.AIBoxModule;
import com.dji.flighthubsdk.demo.base.ModuleDemo;
import com.dji.flighthubsdk.demo.runtime.Await;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.runtime.Node;
import com.dji.flighthubsdk.demo.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * AI Box 管理模块 Demo(菜单驱动)：盒子信息、应用管理、文件操作。
 *
 * <p>接口串联：
 * <pre>
 * [1] 查询 AI Box 信息          fetchAIBoxInfo → 缓存 boxSn 供后续使用
 * [2] 获取应用列表(下发+进度)    fetchAppList → 缓存 bid → fetchAppListProgress
 * [3] 安装应用(下发+进度)        uploadAIBoxFile → 缓存 objectKey
 *                                → installApp → 缓存 bid → installAppProgress
 * [4] 操作应用(启动/停止等)      operateApp(需配置 appId)
 * [5] 导出文件(下发+进度)        exportFile → 缓存 bid → exportFileProgress
 * [6] 获取文件下载链接           fetchFileDownloadUrl(需配置 objectKeys)
 * </pre>
 *
 * <p><b>前置条件：</b>AI Box 安装在飞机上, SDK 入口为
 * {@code session.drone().getAIBoxModule()}。
 * 所有接口均需 {@code deviceSn}(飞机 SN)和 {@code boxSn}(AI Box SN)。
 * {@code boxSn} 优先取自 [1] 查询结果自动缓存, 也可直接在配置 {@code ai_box.box_sn} 中指定。
 */
public class AIBoxDemo extends ModuleDemo {

    /** [1] 查询后缓存的 AI Box SN, 供后续接口使用。 */
    private String lastBoxSn = null;
    /** [2] fetchAppList 下发后的 bid, 供查询进度使用。 */
    private String lastFetchAppBid = null;
    /** [3] installApp 下发后的 bid, 供查询进度使用。 */
    private String lastInstallAppBid = null;
    /** [3] uploadAIBoxFile 成功后的 objectKey, 供 installApp 使用。 */
    private String lastUploadObjectKey = null;
    /** [5] exportFile 下发后的 bid, 供查询导出进度使用。 */
    private String lastExportBid = null;

    @Override
    public String name() {
        return "AIBoxDemo";
    }

    @Override
    protected void run() throws Exception {
        if (input == null) {
            DemoLog.warn(name(), "无交互输入, 无法运行菜单式 AI Box demo");
            return;
        }
        if (!session.hasDrone()) {
            hint("当前设备无飞机, AI Box 模块需要飞机设备, 无法运行");
            return;
        }

        final AIBoxModule module = session.drone().getAIBoxModule();
        if (module == null) {
            DemoLog.error(name(), "getAIBoxModule() 返回 null");
            console("   获取 AIBoxModule 失败(返回 null), 无法进入本 demo 菜单");
            return;
        }

        final String deviceSn = session.droneSn();
        DemoLog.info(name(), "AIBoxDemo: droneSn=" + deviceSn);

        while (true) {
            Utils.printTitle("执行 " + name() + "  (飞机 SN: " + deviceSn + ")");
            if (lastBoxSn != null) console("(已缓存 boxSn=" + lastBoxSn + ")");
            System.out.print(menu());
            int c = Utils.readIntInRange(input, "请选择(1-6, q 退出): ", 1, 6);
            if (c == Utils.QUIT) break;
            try {
                switch (c) {
                    case 1: fetchBoxInfo(module, deviceSn);     break;
                    case 2: menuFetchAppList(module, deviceSn); break;
                    case 3: menuInstallApp(module, deviceSn);   break;
                    case 4: operateApp(module, deviceSn);       break;
                    case 5: menuExportFile(module, deviceSn);   break;
                    case 6: fetchFileDownloadUrl(module, deviceSn); break;
                    default: break;
                }
            } catch (Exception e) {
                console("   结果 -> [异常] " + Utils.describe(e));
                DemoLog.error(name(), "操作异常: " + Utils.describe(e), e);
            }
        }
    }

    // ==================== [1] 查询 AI Box 信息 ====================

    /**
     * 查询 AI Box 设备信息, 缓存第一个 boxSn 供后续接口使用。
     * deviceSn 为飞机 SN, 返回该飞机上所有 AI Box 的信息列表。
     */
    private void fetchBoxInfo(AIBoxModule module, String deviceSn) {
        long timeout = defaultTimeout();
        console("\n   查询 AI Box 信息: droneSn=" + deviceSn);

        final Await<FlightHubError> await = new Await<>(name() + ".fetchAIBoxInfo");
        final AtomicReference<AIBoxInfoListResult> ref = new AtomicReference<>();
        // 门控范式(全文件统一): 仅在 Outcome.SUCCESS 时解析/缓存结果——SDK 失败路径可能触发
        // callback(err, 空结构体), 不门控会缓存脏值, 污染后续串联操作。对齐 MaintenanceDemo 等既定写法。
        Outcome o = runOutcome("查询 AI Box 信息", timeout, await, () ->
            module.fetchAIBoxInfo(deviceSn, new FlightHubCallback.FetchAIBoxInfoCallback() {
                @Override
                public void onResult(FlightHubError error, AIBoxInfoListResult r) {
                    ref.set(r); await.set(error);
                }
            })
        );

        AIBoxInfoListResult result = ref.get();
        if (o == Outcome.SUCCESS && result != null && result.getAiBoxes() != null) {
            List<AIBoxInfoResult> boxes = result.getAiBoxes();
            console("   AI Box 列表(共 " + boxes.size() + " 个):");
            for (AIBoxInfoResult box : boxes) {
                console("     index=" + box.getIndex() + " sn=" + box.getSn()
                    + " type=" + box.getType()
                    + " firmware=" + box.getFirmwareVersion()
                    + " storage=" + box.getStorageUsed() + "/" + box.getStorageTotal() + "GB");

                DemoLog.info(name(), "aiBox: sn=" + box.getSn()
                    + " fw=" + box.getFirmwareVersion());
            }

            if (!boxes.isEmpty() && boxes.get(0).getSn() != null) {
                lastBoxSn = boxes.get(0).getSn();
                hint("已缓存 boxSn=" + lastBoxSn + ", 后续操作将自动使用");
            }
        }
    }

    // ==================== [2] 获取应用列表 ====================

    private void menuFetchAppList(AIBoxModule module, String deviceSn) {
        long timeout = defaultTimeout();
        String boxSn = resolveBoxSn();
        if (boxSn == null) return;

        console("\n  --- 获取应用列表 ---");
        console("  1. 下发获取应用列表请求   2. 查询获取进度(用缓存 bid)   q. 返回");
        int c = Utils.readIntInRange(input, "请选择: ", 1, 2);
        if (c == Utils.QUIT) return;

        if (c == 1) {
            // 下发请求
            AIBoxFetchAppListParam param = new AIBoxFetchAppListParam();
            param.setDeviceSn(deviceSn);
            param.setBoxSn(boxSn);
            console("   下发获取应用列表: boxSn=" + boxSn);

            final Await<FlightHubError> await = new Await<>(name() + ".fetchAppList");
            final AtomicReference<FetchAppListResult> ref = new AtomicReference<>();
            Outcome o = runOutcome("获取应用列表(下发)", timeout, await, () ->
                module.fetchAppList(param, new FlightHubCallback.FetchAppListCallback() {
                    @Override
                    public void onResult(FlightHubError error, FetchAppListResult r) {
                        ref.set(r); await.set(error);
                    }
                })
            );

            FetchAppListResult r = ref.get();
            if (o == Outcome.SUCCESS && r != null) {
                console("     bid=" + r.getBid() + " status=" + r.getStatus());
                lastFetchAppBid = r.getBid();
                hint("bid 已缓存, 可选 [2] 查询进度");
            }
        } else {
            // 查询进度
            if (lastFetchAppBid == null || lastFetchAppBid.isEmpty()) {
                hint("无可查询的 bid, 请先执行 [2-1] 下发请求");
                return;
            }
            AIBoxFetchAppListProgressParam param = new AIBoxFetchAppListProgressParam();
            param.setDeviceSn(deviceSn);
            param.setBoxSn(boxSn);
            param.setBid(lastFetchAppBid);
            console("   查询获取应用列表进度: bid=" + lastFetchAppBid);

            final Await<FlightHubError> await = new Await<>(name() + ".fetchAppListProgress");
            final AtomicReference<FetchAppListProgressResult> ref = new AtomicReference<>();
            Outcome o = runOutcome("查询获取应用列表进度", timeout, await, () ->
                module.fetchAppListProgress(param, new FlightHubCallback.FetchAppListProgressCallback() {
                    @Override
                    public void onResult(FlightHubError error, FetchAppListProgressResult r) {
                        ref.set(r); await.set(error);
                    }
                })
            );

            FetchAppListProgressResult r = ref.get();
            if (o == Outcome.SUCCESS && r != null) {
                console("     bid=" + r.getBid() + " status=" + r.getStatus()
                    + " percent=" + r.getPercent() + "%");
                DemoLog.info(name(), "fetchAppListProgress: " + r);
            }
        }
    }

    // ==================== [3] 安装应用 ====================

    private void menuInstallApp(AIBoxModule module, String deviceSn) {
        long timeout = defaultTimeout();
        String boxSn = resolveBoxSn();
        if (boxSn == null) return;

        console("\n  --- 安装应用 (建议: 先上传文件 → 再安装 → 查询进度) ---");
        console("  1. 上传 AI Box 文件      (uploadAIBoxFile, 缓存 objectKey)");
        console("  2. 安装应用              (installApp, 使用缓存 objectKey, 缓存 bid)");
        console("  3. 查询安装进度          (installAppProgress, 使用缓存 bid)");
        console("  q. 返回");
        int c = Utils.readIntInRange(input, "请选择: ", 1, 3);
        if (c == Utils.QUIT) return;

        Node cfg = params.node("ai_box");
        switch (c) {
            case 1: {
                String filePath = cfg.str("upload_file_path", "");
                if (filePath.isEmpty()) {
                    hint("ai_box.upload_file_path 未配置, 无法上传文件");
                    return;
                }

                console("   上传 AI Box 文件: " + filePath);
                final Await<FlightHubError> await = new Await<>(name() + ".uploadAIBoxFile");
                final AtomicReference<UploadFileResult> ref = new AtomicReference<>();
                Outcome o = runOutcome("上传 AI Box 文件", timeout, await, () ->
                    module.uploadAIBoxFile(filePath, deviceSn, boxSn, new FlightHubCallback.UploadAIBoxFileCallback() {
                        @Override
                        public void onResult(FlightHubError error, UploadFileResult r) {
                            ref.set(r); await.set(error);
                        }
                    })
                );

                UploadFileResult r = ref.get();
                if (o == Outcome.SUCCESS && r != null) {
                    console("     objectKey=" + r.getObjectKey()
                        + " etag=" + r.getEtag());
                    lastUploadObjectKey = r.getObjectKey();
                    hint("objectKey 已缓存, 可选 [3-2] 安装应用");
                }
                break;
            }
            case 2: {
                String objectKey = lastUploadObjectKey != null
                    ? lastUploadObjectKey : cfg.str("app_object_key", "");
                if (objectKey.isEmpty()) {
                    hint("objectKey 为空：请先执行 [3-1] 上传文件, 或配置 ai_box.app_object_key");
                    return;
                }
                AIBoxInstallAppParam param = new AIBoxInstallAppParam();
                param.setDeviceSn(deviceSn);
                param.setBoxSn(boxSn);
                param.setObjectKey(objectKey);
                console("   安装应用: boxSn=" + boxSn + " objectKey=" + objectKey);

                final Await<FlightHubError> await = new Await<>(name() + ".installApp");
                final AtomicReference<InstallAppResult> ref = new AtomicReference<>();
                Outcome o = runOutcome("安装应用(下发)", timeout, await, () ->
                    module.installApp(param, new FlightHubCallback.InstallAppCallback() {
                        @Override
                        public void onResult(FlightHubError error, InstallAppResult r) {
                            ref.set(r); await.set(error);
                        }
                    })
                );

                InstallAppResult r = ref.get();
                if (o == Outcome.SUCCESS && r != null) {
                    console("     bid=" + r.getBid() + " status=" + r.getStatus());
                    lastInstallAppBid = r.getBid();
                    hint("bid 已缓存, 可选 [3-3] 查询安装进度");
                }
                break;
            }
            case 3: {
                if (lastInstallAppBid == null || lastInstallAppBid.isEmpty()) {
                    hint("无可查询的 bid, 请先执行 [3-2] 安装应用");
                    return;
                }

                AIBoxInstallAppProgressParam param = new AIBoxInstallAppProgressParam();
                param.setDeviceSn(deviceSn);
                param.setBoxSn(boxSn);
                param.setBid(lastInstallAppBid);
                console("   查询安装进度: bid=" + lastInstallAppBid);

                final Await<FlightHubError> await = new Await<>(name() + ".installAppProgress");
                final AtomicReference<InstallAppProgressResult> ref = new AtomicReference<>();
                Outcome o = runOutcome("查询安装进度", timeout, await, () ->
                    module.installAppProgress(param, new FlightHubCallback.InstallAppProgressCallback() {
                        @Override
                        public void onResult(FlightHubError error, InstallAppProgressResult r) {
                            ref.set(r); await.set(error);
                        }
                    })
                );

                InstallAppProgressResult r = ref.get();
                if (o == Outcome.SUCCESS && r != null) {
                    console("     bid=" + r.getBid() + " status=" + r.getStatus()
                        + " percent=" + r.getPercent() + "%");
                    DemoLog.info(name(), "installAppProgress: " + r);
                }
                break;
            }
            default: break;
        }
    }

    // ==================== [4] 操作应用 ====================

    private void operateApp(AIBoxModule module, String deviceSn) {
        long timeout = defaultTimeout();
        String boxSn = resolveBoxSn();
        if (boxSn == null) return;

        Node cfg = params.node("ai_box");
        String appId = cfg.str("app_id", "");
        if (appId.isEmpty()) {
            hint("ai_box.app_id 未配置, 无法操作应用");
            return;
        }

        console("\n  --- 操作应用 (appId=" + appId + ") ---");
        console("  1. 启动(start)   2. 停止(stop)   3. 重启(restart)   q. 返回");
        int c = Utils.readIntInRange(input, "请选择: ", 1, 3);
        if (c == Utils.QUIT) return;

        String[] actions = {"start", "stop", "restart"};
        String action = actions[c - 1];

        AIBoxOperateAppParam param = new AIBoxOperateAppParam();
        param.setDeviceSn(deviceSn);
        param.setBoxSn(boxSn);
        param.setAppId(appId);
        param.setAction(action);
        console("   操作应用: appId=" + appId + " action=" + action);

        final Await<FlightHubError> await = new Await<>(name() + ".operateApp");
        final AtomicReference<OperateAppResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("操作应用(" + action + ")", timeout, await, () ->
            module.operateApp(param, new FlightHubCallback.OperateAppCallback() {
                @Override
                public void onResult(FlightHubError error, OperateAppResult r) {
                    ref.set(r); await.set(error);
                }
            })
        );

        OperateAppResult r = ref.get();
        if (o == Outcome.SUCCESS && r != null) {
            console("     bid=" + r.getBid() + " status=" + r.getStatus());
            DemoLog.info(name(), "operateApp: " + action + " bid=" + r.getBid());
        }
    }

    // ==================== [5] 导出文件 ====================

    private void menuExportFile(AIBoxModule module, String deviceSn) {
        long timeout = defaultTimeout();
        String boxSn = resolveBoxSn();
        if (boxSn == null) return;

        Node cfg = params.node("ai_box");
        console("\n  --- 导出文件 ---");
        console("  1. 下发导出文件请求   2. 查询导出进度(用缓存 bid)   q. 返回");
        int c = Utils.readIntInRange(input, "请选择: ", 1, 2);
        if (c == Utils.QUIT) return;

        if (c == 1) {
            String appId = cfg.str("app_id", "");
            if (appId.isEmpty()) {
                hint("ai_box.app_id 未配置, 无法导出应用文件");
                return;
            }
            int exportType = cfg.intVal("export_type", 0);
            AIBoxExportFileParam param = new AIBoxExportFileParam();
            param.setDeviceSn(deviceSn);
            param.setBoxSn(boxSn);
            param.setAppId(appId);
            param.setExportType(exportType);
            console("   下发导出文件: boxSn=" + boxSn + " appId=" + appId
                + " exportType=" + exportType);

            final Await<FlightHubError> await = new Await<>(name() + ".exportFile");
            final AtomicReference<ExportFileResult> ref = new AtomicReference<>();
            Outcome o = runOutcome("导出文件(下发)", timeout, await, () ->
                module.exportFile(param, new FlightHubCallback.ExportFileCallback() {
                    @Override
                    public void onResult(FlightHubError error, ExportFileResult r) {
                        ref.set(r); await.set(error);
                    }
                })
            );

            ExportFileResult r = ref.get();
            if (o == Outcome.SUCCESS && r != null) {
                console("     bid=" + r.getBid() + " status=" + r.getStatus());
                lastExportBid = r.getBid();
                hint("bid 已缓存, 可选 [5-2] 查询导出进度");
            }
        } else {
            if (lastExportBid == null || lastExportBid.isEmpty()) {
                hint("无可查询的 bid, 请先执行 [5-1] 下发导出请求");
                return;
            }
            AIBoxExportFileProgressParam param = new AIBoxExportFileProgressParam();
            param.setDeviceSn(deviceSn);
            param.setBoxSn(boxSn);
            param.setBid(lastExportBid);
            console("   查询导出进度: bid=" + lastExportBid);

            final Await<FlightHubError> await = new Await<>(name() + ".exportFileProgress");
            final AtomicReference<ExportFileProgressResult> ref = new AtomicReference<>();
            Outcome o = runOutcome("查询导出进度", timeout, await, () ->
                module.exportFileProgress(param, new FlightHubCallback.ExportFileProgressCallback() {
                    @Override
                    public void onResult(FlightHubError error, ExportFileProgressResult r) {
                        ref.set(r); await.set(error);
                    }
                })
            );

            ExportFileProgressResult r = ref.get();
            if (o == Outcome.SUCCESS && r != null) {
                console("     bid=" + r.getBid() + " status=" + r.getStatus()
                    + " percent=" + r.getPercent() + "%");
                DemoLog.info(name(), "exportFileProgress: " + r);
            }
        }
    }

    // ==================== [6] 获取文件下载链接 ====================

    private void fetchFileDownloadUrl(AIBoxModule module, String deviceSn) {
        long timeout = defaultTimeout();
        String boxSn = resolveBoxSn();
        if (boxSn == null) return;

        Node cfg = params.node("ai_box");
        List<Object> rawKeys = cfg.list("download_object_keys");
        List<String> objectKeys = new ArrayList<>();
        for (Object o : rawKeys) {
            if (o != null && !o.toString().isEmpty()) objectKeys.add(o.toString());
        }
        if (objectKeys.isEmpty()) {
            hint("ai_box.download_object_keys 未配置(文件 objectKey 列表), 无法获取下载链接");
            return;
        }

        FetchFileDownloadUrlParam param = new FetchFileDownloadUrlParam();
        param.setDeviceSn(deviceSn);
        param.setBoxSn(boxSn);
        param.setObjectKeys(objectKeys);
        console("\n   获取文件下载链接: objectKeys=" + objectKeys);

        final Await<FlightHubError> await = new Await<>(name() + ".fetchFileDownloadUrl");
        final AtomicReference<FileDownloadUrlListResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("获取文件下载链接", timeout, await, () ->
            module.fetchFileDownloadUrl(param, new FlightHubCallback.FetchFileDownloadUrlCallback() {
                @Override
                public void onResult(FlightHubError error, FileDownloadUrlListResult r) {
                    ref.set(r); await.set(error);
                }
            })
        );

        FileDownloadUrlListResult r = ref.get();
        if (o == Outcome.SUCCESS && r != null && r.getList() != null) {
            List<String> urls = r.getList();
            console("   下载链接(共 " + urls.size() + " 个):");
            for (int i = 0; i < urls.size(); i++) {
                console("     [" + i + "] " + urls.get(i));
                DemoLog.info(name(), "downloadUrl[" + i + "]: " + urls.get(i));
            }
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 解析 boxSn：优先使用 [1] 缓存, 否则取配置 {@code ai_box.box_sn}。
     * 两者均为空时提示用户先执行 [1] 查询。
     */
    private String resolveBoxSn() {
        if (lastBoxSn != null && !lastBoxSn.isEmpty()) return lastBoxSn;
        String sn = params.node("ai_box").str("box_sn", "");
        if (!sn.isEmpty()) {
            hint("使用配置 ai_box.box_sn=" + sn);
            return sn;
        }

        hint("boxSn 未知：请先执行 [1] 查询 AI Box 信息, 或在配置中填写 ai_box.box_sn");
        return null;
    }

    /** 统一输出到控制台。 */
    private void console(String line) {
        System.out.println(line);
    }

    /** 同时记录日志并输出到控制台(带缩进), 用于关键提示信息。 */
    private void hint(String msg) {
        DemoLog.info(name(), msg);
        console("   " + msg);
    }

    /** 构造主菜单文本。 */
    private String menu() {
        return "\n"
            + "  === AI Box 管理 ===\n"
            + "  1.  查询 AI Box 信息    (fetchAIBoxInfo, 缓存 boxSn)\n"
            + "  2.  获取应用列表        (fetchAppList + fetchAppListProgress)\n"
            + "  3.  安装应用            (uploadAIBoxFile → installApp → installAppProgress)\n"
            + "  4.  操作应用            (operateApp: start/stop/restart)\n"
            + "  5.  导出文件            (exportFile + exportFileProgress)\n"
            + "  6.  获取文件下载链接    (fetchFileDownloadUrl)\n"
            + "\n  q.  返回上级菜单\n";
    }
}
