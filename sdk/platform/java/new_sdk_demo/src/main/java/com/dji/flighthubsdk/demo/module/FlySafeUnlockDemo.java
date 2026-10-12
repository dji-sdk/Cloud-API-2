// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.module;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.CheckUnlockLicenseParam;
import com.dji.flighthubsdk.bean.CheckUnlockLicenseResult;
import com.dji.flighthubsdk.bean.CheckUnlockLicenseResultItem;
import com.dji.flighthubsdk.bean.DeviceUnlockLicenseItem;
import com.dji.flighthubsdk.bean.DockModeCode;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.ImportUnlockLicenseParam;
import com.dji.flighthubsdk.bean.OpenDebugModeResult;
import com.dji.flighthubsdk.bean.QueryDeviceUnlockLicenseListParam;
import com.dji.flighthubsdk.bean.QueryDeviceUnlockLicenseListResult;
import com.dji.flighthubsdk.bean.SwitchUnlockLicenseParam;
import com.dji.flighthubsdk.bean.UploadUnlockLicenseFileResult;
import com.dji.flighthubsdk.core.DeviceDebugModule;
import com.dji.flighthubsdk.core.DockOsdModule;
import com.dji.flighthubsdk.core.SafetyServiceModule;
import com.dji.flighthubsdk.demo.base.ModuleDemo;
import com.dji.flighthubsdk.demo.runtime.Await;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.runtime.Node;
import com.dji.flighthubsdk.demo.utils.Utils;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 限飞解禁 Demo(菜单驱动)。
 *
 * <p>完整覆盖 {@code SafetyServiceModule} 的全部 6 个解禁证书接口：
 *
 * <pre>
 * 主菜单
 * ├── [1] 获取解禁证书列表    getUnlockLicenseList(缓存 licenseId 供 [2])
 * ├── [2] 切换解禁证书状态    switchUnlockLicense
 * ├── [3] 检查解禁证书        checkUnlockLicense(使用 [6] 缓存的 fileKey, 缓存 certUrl 供 [4])
 * ├── [4] 提交解禁证书        submitUnlockLicense(使用 [3] 缓存的 certUrl)
 * ├── [5] 清除解禁证书        clearDeviceUnlockLicense
 * └── [6] 上传解禁证书文件    uploadUnlockLicenseFile(fileStoreDir+文件名拼成 fileKey 供 [3])
 * </pre>
 *
 * <p><b>接口串联设计(数据流见 message.h 的 UploadUnlockLicenseFileResult 流程注释)：</b>
 * <ul>
 *   <li>[6] 上传文件 → 用返回的 fileStoreDir 拼上本地文件名得到 fileKey, 缓存 {@link #lastFileKey}
 *       → [3] 检查自动使用; [3] 返回证书下载 url → 缓存 {@link #lastCertUrl} → [4] 提交自动使用；</li>
 *   <li>[1] 查列表 → 缓存 {@link #lastLicenseId} → [2] 切换直接使用；</li>
 *   <li>[1][2][4][5] 执行前经 {@link #ensureDebugMode} 检查并自动开启机场远程调试模式(mode_code=2)——
 *       此前置为联调实测经验, SDK 注解与仓内文档均未记载(六个解禁接口的链路注解均为云端 HTTP);
 *       [3] 检查与 [6] 上传不做此前置。</li>
 * </ul>
 *
 * <p><b>注意：</b>本模块只负责限飞解禁,
 * 不要与 SafetyServiceDemo(安全服务, 负责自定义飞行区域/离线地图)混淆：
 * <ul>
 *   <li>FlySafeUnlockDemo → 限飞解禁 → {@code workspace.getSafetyServiceModule()} 解禁证书接口</li>
 *   <li>SafetyServiceDemo → 安全服务模块 → 飞行区域 / 离线地图 / 信号地图</li>
 * </ul>
 *
 * <p><b>SDK 入口：</b>{@code session.workspace().getSafetyServiceModule()}(挂在 Workspace 上)。
 *
 * <p><b>设计要点：</b>
 * <ul>
 *   <li>设备 SN 与证书文件路径配置驱动({@code fly_safe_unlock} 配置节), 未配置 SN 时回退 session 设备；</li>
 *   <li>三态结果回显(SUCCESS/TIMEOUT/FAILED), 结果对象仅在 SUCCESS 时解析回显。</li>
 * </ul>
 */
public class FlySafeUnlockDemo extends ModuleDemo {

    /** [6] 上传后缓存的 fileKey(fileStoreDir + "/" + 文件名), 供 [3] 检查使用。 */
    private String lastFileKey = null;
    /** [3] 检查后缓存的证书下载 url(每次检查刷新), 供 [4] 提交使用。 */
    private String lastCertUrl = null;
    /** [1] 查列表后缓存的 licenseId(每次查询刷新为本次列表第一条; 空列表时复位 -1), 供 [2] 切换使用。 */
    private int lastLicenseId = -1;
    /** 本 demo 曾自动开启机场远程调试模式的标志: run() 收尾据此调用 closeDebugMode 还原设备状态。 */
    private boolean debugModeOpenedByDemo = false;

    @Override
    public String name() {
        return "FlySafeUnlockDemo";
    }

    @Override
    protected void run() throws Exception {
        if (input == null) {
            DemoLog.warn(name(), "无交互输入(input=null), 无法运行菜单式限飞解禁 demo");
            return;
        }

        final SafetyServiceModule module = session.workspace().getSafetyServiceModule();
        if (module == null) {
            DemoLog.error(name(), "getSafetyServiceModule() 返回 null");
            console("   获取 SafetyServiceModule 失败(返回 null), 无法进入本 demo 菜单");
            return;
        }

        final long actionTimeout = timeout("default_callback_timeout_ms");
        DemoLog.info(name(), "actionTimeout=" + actionTimeout + "ms");

        try {
            while (true) {
                Utils.printTitle("执行 " + name());
                if (lastFileKey != null) {
                    console("(已缓存 fileKey, [3] 检查将自动使用)");
                }
                if (lastCertUrl != null) {
                    console("(已缓存 certUrl, [4] 提交将自动使用)");
                }
                if (lastLicenseId >= 0) {
                    console("(已缓存 licenseId=" + lastLicenseId + ", [2] 切换将自动使用)");
                }
                System.out.print(menu());
                int choice = Utils.readIntInRange(input, "请选择(1-6, q 退出): ", 1, 6);
                if (choice == Utils.QUIT) {
                    DemoLog.info(name(), "退出限飞解禁菜单");
                    break;
                }
                try {
                    switch (choice) {
                        case 1: getUnlockLicenseList(module, actionTimeout);     break;
                        case 2: switchUnlockLicense(module, actionTimeout);      break;
                        case 3: checkUnlockLicense(module, actionTimeout);       break;
                        case 4: submitUnlockLicense(module, actionTimeout);      break;
                        case 5: clearDeviceUnlockLicense(module, actionTimeout); break;
                        case 6: uploadUnlockLicenseFile(module, actionTimeout);  break;
                        default: break;
                    }
                } catch (Exception e) {
                    console("   结果 -> [异常] " + Utils.describe(e));
                    DemoLog.error(name(), "菜单动作执行异常: " + Utils.describe(e), e);
                }
            }
        } finally {
            // 状态还原: 本 demo 曾自动开启机场远程调试模式时, 退出前关闭还原,
            // 避免 demo 退出后机场遗留在调试模式影响正常作业。兜 Throwable 不掩盖主异常。
            restoreDebugModeIfOpened(actionTimeout);
        }
    }

    // ==================== [1] 获取解禁证书列表 (getUnlockLicenseList) ====================

    /**
     * 获取设备解禁证书列表。
     *
     * <p>成功后缓存本次列表第一条证书的 licenseId(每次查询刷新), 供 [2] 切换状态使用。
     * 设备 SN 取自配置 {@code fly_safe_unlock.device_sn}, 未配置时自动用 session 的机场/飞机 SN。
     */
    private void getUnlockLicenseList(SafetyServiceModule module, long timeout) {
        Node cfg = params.node("fly_safe_unlock");
        String deviceSn = resolveDeviceSn(cfg);
        if (!ensureDebugMode("获取解禁证书列表", timeout)) return;

        QueryDeviceUnlockLicenseListParam param = new QueryDeviceUnlockLicenseListParam();
        param.setDeviceSn(deviceSn);
        int deviceModelDomain = resolveDeviceModelDomain(deviceSn);
        if (deviceModelDomain >= 0) {
            param.setDeviceModelDomain(deviceModelDomain);
        }
        param.setDetail(true);

        console("\n   获取解禁证书列表: sn=" + deviceSn);
        DemoLog.info(name(), "getUnlockLicenseList: sn=" + deviceSn);

        final Await<FlightHubError> await = new Await<>(name() + ".getUnlockLicenseList");
        final AtomicReference<QueryDeviceUnlockLicenseListResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("获取解禁证书列表", timeout, await, () ->
            module.getUnlockLicenseList(param, new FlightHubCallback.GetUnlockLicenseListCallback() {
                @Override
                public void onResult(FlightHubError error, QueryDeviceUnlockLicenseListResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );

        if (o == Outcome.SUCCESS) {
            QueryDeviceUnlockLicenseListResult result = ref.get();
            if (result != null) {
                // 每次成功查询先复位缓存: 查询返回空列表(如换了 device_sn)时不残留上一设备的
                // 旧 licenseId, 否则 [2] 会对新设备下发旧证书 id。
                lastLicenseId = -1;
                console("     result=" + result.getResult()
                    + " deviceModelDomain=" + result.getDeviceModelDomain());
                List<DeviceUnlockLicenseItem> licenses =
                        result.getLicenses() != null ? result.getLicenses() : Collections.emptyList();
                console("     解禁证书列表(共 " + licenses.size() + " 条):");
                boolean cachedThisQuery = false;
                for (DeviceUnlockLicenseItem lic : licenses) {
                    if (lic.getCommonFields() != null) {
                        console("       licenseId=" + lic.getCommonFields().getId()
                            + " type=" + lic.getCommonFields().getType()
                            + " deviceSn=" + lic.getCommonFields().getDeviceSn()
                            + " beginTime=" + lic.getCommonFields().getBeginTime()
                            + " endTime=" + lic.getCommonFields().getEndTime());
                        if (!cachedThisQuery) {
                            lastLicenseId = lic.getCommonFields().getId();
                            cachedThisQuery = true;
                        }
                    }
                    DemoLog.info(name(), "license: " + lic);
                }
                // 「已缓存」提示只在本次确有缓存时打印(对齐 [3] 的 cachedUrlThisQuery 写法)。
                if (cachedThisQuery) {
                    hint("已缓存 licenseId=" + lastLicenseId + ", [2] 切换将自动使用");
                }
            }
        }
    }

    // ==================== [2] 切换解禁证书状态 (switchUnlockLicense) ====================

    /**
     * 切换解禁证书启用/禁用状态。
     *
     * <p>licenseId 优先用 [1] 查询后缓存的 {@link #lastLicenseId},
     * 也可在配置 {@code fly_safe_unlock.license_id} 中指定。
     */
    private void switchUnlockLicense(SafetyServiceModule module, long timeout) {
        Node cfg = params.node("fly_safe_unlock");
        String deviceSn = resolveDeviceSn(cfg);
        if (!ensureDebugMode("切换解禁证书状态", timeout)) return;
        int licenseId = lastLicenseId >= 0 ? lastLicenseId : cfg.intVal("license_id", -1);
        if (licenseId < 0) {
            hint("licenseId 未知：请先执行 [1] 获取证书列表, 或在配置中填写 fly_safe_unlock.license_id");
            return;
        }

        console("\n  --- 切换解禁证书状态(licenseId=" + licenseId + ") ---");
        console("  1. 启用   2. 禁用   q. 返回");
        int c = Utils.readIntInRange(input, "请选择: ", 1, 2);
        if (c == Utils.QUIT) return;
        final boolean enable = (c == 1);

        SwitchUnlockLicenseParam param = new SwitchUnlockLicenseParam();
        param.setDeviceSn(deviceSn);
        param.setLicenseId(licenseId);
        param.setEnable(enable);
        console("   切换解禁证书: sn=" + deviceSn + " licenseId=" + licenseId
            + " enable=" + enable);

        final Await<FlightHubError> await = new Await<>(name() + ".switchUnlockLicense");
        runOutcome("切换解禁证书状态", timeout, await, () ->
            module.switchUnlockLicense(param, new FlightHubCallback.SwitchUnlockLicenseCallback() {
                @Override
                public void onResult(FlightHubError error) {
                    await.set(error);
                }
            })
        );
    }

    // ==================== [3] 检查解禁证书 (checkUnlockLicense) ====================

    /**
     * 检查解禁证书有效性。
     *
     * <p>fileKey 优先用 [6] 上传后缓存的 {@link #lastFileKey},
     * 也可在配置 {@code fly_safe_unlock.file_key} 中指定。
     * 成功后缓存返回条目的证书下载 url 到 {@link #lastCertUrl}, 供 [4] 提交使用。
     */
    private void checkUnlockLicense(SafetyServiceModule module, long timeout) {
        Node cfg = params.node("fly_safe_unlock");
        String deviceSn = resolveDeviceSn(cfg);
        String fileKey = lastFileKey != null ? lastFileKey : cfg.str("file_key", "");
        if (fileKey.isEmpty()) {
            hint("fileKey 为空：请先执行 [6] 上传解禁证书文件, 或在配置中填写 fly_safe_unlock.file_key");
            return;
        }

        CheckUnlockLicenseParam param = new CheckUnlockLicenseParam();
        param.setFileKey(fileKey);
        param.setDeviceSn(deviceSn);
        console("\n   检查解禁证书: fileKey=" + abbreviate(fileKey, 40));

        final Await<FlightHubError> await = new Await<>(name() + ".checkUnlockLicense");
        final AtomicReference<CheckUnlockLicenseResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("检查解禁证书", timeout, await, () ->
            module.checkUnlockLicense(param, new FlightHubCallback.CheckUnlockLicenseCallback() {
                @Override
                public void onResult(FlightHubError error, CheckUnlockLicenseResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );

        if (o == Outcome.SUCCESS) {
            CheckUnlockLicenseResult result = ref.get();
            if (result != null) {
                console("     errFileNames=" + result.getErrFileNames());
                console("     errCertMismatch=" + result.getErrCertMismatch());
                List<CheckUnlockLicenseResultItem> items =
                        result.getItems() != null ? result.getItems() : Collections.emptyList();
                console("     解禁证书详情(共 " + items.size() + " 条):");
                boolean cachedUrlThisQuery = false;
                for (CheckUnlockLicenseResultItem item : items) {
                    console("       name=" + item.getName() + " type=" + item.getType()
                        + " fileName=" + item.getFileName()
                        + " beginTime=" + item.getBeginTime() + " endTime=" + item.getEndTime());
                    console("       url=" + abbreviate(item.getUrl() != null ? item.getUrl() : "", 80));
                    // [4] 提交需要的 certUrl 来自这里返回的 url(见 message.h 流程注释), 缓存第一条非空值
                    if (!cachedUrlThisQuery && item.getUrl() != null && !item.getUrl().isEmpty()) {
                        lastCertUrl = item.getUrl();
                        cachedUrlThisQuery = true;
                    }
                }
                if (cachedUrlThisQuery) {
                    hint("已缓存 certUrl, [4] 提交将自动使用");
                }
                DemoLog.info(name(), "checkLicense: " + result);
            }
        }
    }

    // ==================== [4] 提交解禁证书 (submitUnlockLicense) ====================

    /**
     * 提交解禁证书到设备。
     *
     * <p>certUrl 优先用 [3] 检查后缓存的 {@link #lastCertUrl}(check 返回的证书下载 url,
     * 见 message.h 流程注释), 也可在配置 {@code fly_safe_unlock.cert_url} 中指定。
     */
    private void submitUnlockLicense(SafetyServiceModule module, long timeout) {
        Node cfg = params.node("fly_safe_unlock");
        String deviceSn = resolveDeviceSn(cfg);
        if (!ensureDebugMode("提交解禁证书", timeout)) return;
        String certUrl = lastCertUrl != null ? lastCertUrl : cfg.str("cert_url", "");
        if (certUrl.isEmpty()) {
            hint("certUrl 为空：请先执行 [6] 上传 → [3] 检查获取证书 url, 或在配置中填写 fly_safe_unlock.cert_url");
            return;
        }

        ImportUnlockLicenseParam param = new ImportUnlockLicenseParam();
        param.setDeviceSn(deviceSn);
        param.setCertUrl(certUrl);
        console("\n   提交解禁证书: sn=" + deviceSn + " certUrl=" + abbreviate(certUrl, 60));

        final Await<FlightHubError> await = new Await<>(name() + ".submitUnlockLicense");
        runOutcome("提交解禁证书", timeout, await, () ->
            module.submitUnlockLicense(param, new FlightHubCallback.SubmitUnlockLicenseCallback() {
                @Override
                public void onResult(FlightHubError error) {
                    await.set(error);
                }
            })
        );
    }

    // ==================== [5] 清除解禁证书 (clearDeviceUnlockLicense) ====================

    /** 清除设备上的解禁证书。 */
    private void clearDeviceUnlockLicense(SafetyServiceModule module, long timeout) {
        String deviceSn = resolveDeviceSn(params.node("fly_safe_unlock"));
        if (!ensureDebugMode("清除解禁证书", timeout)) return;
        console("\n   清除解禁证书: sn=" + deviceSn);

        final Await<FlightHubError> await = new Await<>(name() + ".clearDeviceUnlockLicense");
        runOutcome("清除解禁证书", timeout, await, () ->
            module.clearDeviceUnlockLicense(deviceSn, new FlightHubCallback.ClearDeviceUnlockLicenseCallback() {
                @Override
                public void onResult(FlightHubError error) {
                    await.set(error);
                }
            })
        );
    }

    // ==================== [6] 上传解禁证书文件 (uploadUnlockLicenseFile) ====================

    /**
     * 上传解禁证书文件到服务器, SDK 内部自动获取 STS 凭证并上传。
     *
     * <p>上传成功后用返回的 {@code fileStoreDir} 拼上本地文件名得到 fileKey
     * (见 message.h 的 UploadUnlockLicenseFileResult 流程注释), 缓存到
     * {@link #lastFileKey} 供 [3] 检查自动使用。
     */
    private void uploadUnlockLicenseFile(SafetyServiceModule module, long timeout) {
        String filePath = params.node("fly_safe_unlock").str("cert_file_path", "");
        if (filePath.isEmpty()) {
            hint("fly_safe_unlock.cert_file_path 未配置, 无法上传解禁证书文件。"
                + "请在 demo_params.json 中填写本地证书文件路径。");
            return;
        }
        console("\n   上传解禁证书文件: " + filePath);

        final Await<FlightHubError> await = new Await<>(name() + ".uploadUnlockLicenseFile");
        final AtomicReference<UploadUnlockLicenseFileResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("上传解禁证书文件", timeout, await, () ->
            module.uploadUnlockLicenseFile(filePath, new FlightHubCallback.UploadUnlockLicenseFileCallback() {
                @Override
                public void onResult(FlightHubError error, UploadUnlockLicenseFileResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );

        if (o == Outcome.SUCCESS) {
            UploadUnlockLicenseFileResult result = ref.get();
            if (result != null) {
                console("     fileStoreDir=" + result.getFileStoreDir());
                DemoLog.info(name(), "uploadLicense: fileStoreDir=" + result.getFileStoreDir());
                // 服务端只返回存储目录, fileKey = fileStoreDir + "/" + 文件名(见 message.h 流程注释);
                // 直接把目录当 fileKey 用会让 [3] 检查指向目录而非上传的文件
                String dir = result.getFileStoreDir();
                String fileName = new File(filePath).getName();
                lastFileKey = (dir == null || dir.isEmpty()) ? fileName : dir + "/" + fileName;
                console("     fileKey=" + lastFileKey);
                hint("fileKey 已缓存, [3] 检查将自动使用");
            }
        }
    }

    // ==================== 调试模式检测 ====================

    /**
     * 确保会话机场处于远程调试模式(mode_code=2)。
     *
     * <p>[1]列表/[2]切换/[4]提交/[5]清除执行前调用。此前置为联调实测经验——SDK 注解与仓内文档
     * 均未记载, 六个解禁接口的链路注解均为云端 HTTP, 以实际服务端行为为准。
     * 流程：WS 订阅会话机场的 mode_code → 为 2 直接放行 → 否则自动调用 openDebugMode 开启
     * (对已处于调试模式的机场重复开启预期无害, 幂等性未见 SDK 文档背书; 与 DeviceDebugDemo 的
     * checkAndEnsureDebugMode 同思路, 本版开启成功后额外等 2s 让 mode_code 切换生效)。
     *
     * <p>注意：解禁目标设备 SN({@code fly_safe_unlock.device_sn}, 可为飞机)与调试模式所在设备
     * (恒为会话机场)是两个概念, 本方法只检查/开启会话机场。
     */
    private boolean ensureDebugMode(String operation, long timeout) {
        if (!session.hasDock()) {
            hint("当前会话不含机场, 无法检查/开启远程调试模式");
            return false;
        }

        DockOsdModule dockOsd = session.dock().getDockOsdModule();
        DeviceDebugModule debugModule = session.dock().getDeviceDebugModule();
        if (dockOsd == null || debugModule == null) {
            hint("DockOsdModule 或 DeviceDebugModule 不可用, 无法检测调试模式");
            return false;
        }

        final AtomicInteger modeCodeRef = new AtomicInteger(-1);
        final Await<Integer> modeAwait = new Await<>(name() + ".listenDockModeCode");

        int listenId = -1;
        try {
            listenId = dockOsd.listenDockModeCode(new FlightHubCallback.DockModeCodeCallback() {
                @Override
                public void onResult(DockModeCode modeCode) {
                    if (modeCode != null && !modeAwait.isDone()) {
                        modeCodeRef.set(modeCode.getModeCode());
                        modeAwait.set(modeCode.getModeCode());
                    }
                }
            });
        } catch (Exception e) {
            DemoLog.warn(name(), "listenDockModeCode 失败: " + Utils.describe(e));
        }

        try {
            modeAwait.await(5000);
        } catch (Exception e) {
            DemoLog.warn(name(), "等待 mode_code 超时, 将直接尝试开启调试模式");
        } finally {
            if (listenId >= 0) {
                try { dockOsd.cancelListenDockModeCode(listenId); } catch (Exception ignore) {}
            }
        }

        int modeCode = modeCodeRef.get();
        console("\n   [远程调试模式检查] 操作: " + operation);
        if (modeCode == 2) {
            console("   当前 mode_code: 2(远程调试中), ✓ 可以执行 " + operation);
            return true;
        }

        if (modeCode == 4) {
            // 设备作业中: 切远程调试会干扰进行中的作业, 拦截提示而非自动切换。
            console("   当前 mode_code: 4(设备作业中), ✗ 不自动切换调试模式, 请等待作业结束后重试 " + operation);
            hint("设备作业中(mode_code=4), 已拦截自动开启远程调试模式");
            return false;
        }

        if (modeCode >= 0) {
            console("   当前 mode_code: " + modeCode + "(" + describeModeCode(modeCode) + ")");
            console("   ✗ " + operation + " 需要远程调试模式(mode_code=2), 尝试开启...");
        } else {
            console("   当前 mode_code: WS 未推送, 自动尝试开启远程调试模式...");
        }

        final Await<FlightHubError> openAwait = new Await<>(name() + ".openDebugMode");
        final AtomicReference<OpenDebugModeResult> openResultRef = new AtomicReference<>();
        Outcome openOutcome = runOutcome("打开远程调试模式", timeout, openAwait, () ->
            debugModule.openDebugMode(session.dockSn(), new FlightHubCallback.OpenDebugModeCallback() {
                @Override
                public void onResult(FlightHubError error, OpenDebugModeResult result) {
                    openResultRef.set(result);
                    openAwait.set(error);
                }
            })
        );

        if (openResultRef.get() != null) {
            console("     bid=" + openResultRef.get().getBid()
                + " msg=" + openResultRef.get().getMessage());
        }
        if (openOutcome == Outcome.SUCCESS) {
            debugModeOpenedByDemo = true;   // 记录: 收尾时 closeDebugMode 还原。
            hint("远程调试模式开启成功, 继续执行 " + operation);
            hint("等待 2s 让 mode_code 切换生效...");
            sleep(2000);
            return true;
        } else if (openOutcome == Outcome.TIMEOUT) {
            debugModeOpenedByDemo = true;   // 超时 = 命令可能已生效, 保守置位以便收尾还原。
            hint("openDebugMode 超时(命令可能已生效), 继续尝试执行 " + operation);
            return true;
        } else {
            hint("远程调试模式开启失败, 无法执行 " + operation + "。请检查机场状态后重试。");
            return false;
        }
    }

    /**
     * run() 收尾: 若本 demo 曾自动开启机场远程调试模式, 调用 closeDebugMode 还原, 避免设备状态残留。
     * 兜 Throwable(对齐全工程 JNI Error 隔离策略), 失败仅告警不外抛、不掩盖主异常。
     */
    private void restoreDebugModeIfOpened(long timeout) {
        if (!debugModeOpenedByDemo) return;
        try {
            if (!session.hasDock()) return;
            DeviceDebugModule debugModule = session.dock().getDeviceDebugModule();
            if (debugModule == null) {
                DemoLog.warn(name(), "[收尾] DeviceDebugModule 不可用, 无法关闭远程调试模式");
                return;
            }
            hint("[收尾] 本 demo 曾自动开启远程调试模式, 关闭还原...");
            final Await<FlightHubError> closeAwait = new Await<>(name() + ".closeDebugMode");
            Outcome o = runOutcome("关闭远程调试模式(收尾)", timeout, closeAwait, () ->
                debugModule.closeDebugMode(session.dockSn(), new FlightHubCallback.CloseDebugModeCallback() {
                    @Override
                    public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.CloseDebugModeResult result) {
                        closeAwait.set(error);
                    }
                })
            );
            if (o == Outcome.SUCCESS) {
                debugModeOpenedByDemo = false;
                hint("[收尾] 远程调试模式已关闭");
            } else {
                DemoLog.warn(name(), "[收尾] 关闭远程调试模式未确认成功(" + o + "), 机场可能仍处于调试模式");
            }
        } catch (Throwable t) {
            DemoLog.warn(name(), "[收尾] 关闭远程调试模式异常: " + Utils.describe(t));
        }
    }

    /** mode_code 枚举描述(0-9, 见 SDK bean {@link DockModeCode} 类注释)。 */
    private static String describeModeCode(int code) {
        switch (code) {
            case 0: return "空闲中";
            case 1: return "现场调试中";
            case 2: return "远程调试中";
            case 3: return "设备升级中";
            case 4: return "设备作业中";
            case 5: return "机场设备未标定";
            case 6: return "节能状态中";
            case 7: return "现场急停中";
            case 8: return "换电中";
            case 9: return "日志拉取中";
            default: return "未知(" + code + ")";
        }
    }

    // ==================== 辅助方法 ====================

    /** 解析目标设备 SN：配置 {@code fly_safe_unlock.device_sn} 优先, 回退 session 机场 SN, 再回退飞机 SN。 */
    private String resolveDeviceSn(Node cfg) {
        String sn = cfg.str("device_sn", "");
        if (!sn.isEmpty()) return sn;
        if (session.hasDock() && session.dockSn() != null && !session.dockSn().isEmpty()) {
            hint("fly_safe_unlock.device_sn 未配置, 自动使用机场 SN: " + session.dockSn());
            return session.dockSn();
        }
        if (session.hasDrone() && session.droneSn() != null && !session.droneSn().isEmpty()) {
            hint("fly_safe_unlock.device_sn 未配置, 自动使用飞机 SN: " + session.droneSn());
            return session.droneSn();
        }
        return "";
    }

    /** 解析 deviceModelDomain(0=飞机, 3=机场, 见 QueryDeviceUnlockLicenseListParam);无法判断时返回 -1(不设置)。 */
    private int resolveDeviceModelDomain(String deviceSn) {
        if (deviceSn.isEmpty()) return -1;
        if (session.hasDock() && deviceSn.equals(session.dockSn())) return 3;
        if (session.hasDrone() && deviceSn.equals(session.droneSn())) return 0;
        return -1;
    }

    /** 截断长字符串用于回显(如 fileKey), 超过 maxLen 时截断并加省略号。 */
    private static String abbreviate(String s, int maxLen) {
        return s.length() <= maxLen ? s : s.substring(0, maxLen) + "...";
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
     * 主菜单文案：6 个菜单项与 {@code SafetyServiceModule} 的解禁证书接口一一对应(见类 Javadoc 菜单树)。
     * 增删菜单项时须同步 {@link #run()} 中的 switch 分支与 {@code readIntInRange} 的取值上界。
     */
    private String menu() {
        return "\n"
            + "  === 限飞解禁 (Fly Safe Unlock) ===\n"
            + "  1.  获取解禁证书列表   (getUnlockLicenseList, 缓存 licenseId)\n"
            + "  2.  切换解禁证书状态   (switchUnlockLicense: 启用/禁用, 使用缓存 licenseId)\n"
            + "  3.  检查解禁证书       (checkUnlockLicense, 使用缓存 fileKey, 缓存 certUrl)\n"
            + "  4.  提交解禁证书       (submitUnlockLicense, 使用 [3] 缓存的 certUrl)\n"
            + "  5.  清除解禁证书       (clearDeviceUnlockLicense)\n"
            + "  6.  上传解禁证书文件   (uploadUnlockLicenseFile, 缓存 fileKey)\n"
            + "\n"
            + "  操作建议: [6] 上传得 fileKey → [3] 检查得 certUrl → [4] 提交 → [1] 查列表确认 → [2] 切换状态\n"
            + "\n"
            + "  q.  返回上级菜单\n";
    }
}
