// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.module;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.GetMediaFilesDownloadUrlResults;
import com.dji.flighthubsdk.bean.GetMediaFilesListParams;
import com.dji.flighthubsdk.bean.GetMediaFilesListResults;
import com.dji.flighthubsdk.bean.GetMediaUploadStsParams;
import com.dji.flighthubsdk.bean.GetMediaUploadStsResult;
import com.dji.flighthubsdk.bean.MediaFileDeviceUpload;
import com.dji.flighthubsdk.bean.MediaFilePostProcess;
import com.dji.flighthubsdk.bean.MediaFilesListItemInfoData;
import com.dji.flighthubsdk.bean.MediaUploadCallbackFile;
import com.dji.flighthubsdk.bean.MediaUploadCallbackParams;
import com.dji.flighthubsdk.bean.MediaUploadCallbackResult;
import com.dji.flighthubsdk.bean.MediaUploadStsCredentials;
import com.dji.flighthubsdk.core.MediaServiceModule;
import com.dji.flighthubsdk.demo.base.ModuleDemo;
import com.dji.flighthubsdk.demo.runtime.Await;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.runtime.Node;
import com.dji.flighthubsdk.demo.runtime.Subscription;
import com.dji.flighthubsdk.demo.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 媒体服务 Demo(菜单驱动)：媒体回传/后处理通知订阅、文件列表/下载地址/删除、上传 STS 凭证获取与上传完成回调。
 *
 * <p>对应 {@code MediaServiceModule} 的 9 个常用接口(非全量: 未覆盖 uploadGeoMediaFile / getMediaFileDetailById,
 * 也未覆盖 getMediaFilesList 的展开参数重载与 getMediaFilesDownloadURL 的 objectKeys 重载)：
 *
 * <pre>
 * 主菜单
 * ├── [1] 订阅/取消 设备媒体回传通知    listenMediaFileDeviceUploadMessage / cancelListenMediaFileDeviceUploadMessage(toggle)
 * ├── [2] 订阅/取消 媒体后处理完成通知  listenMediaFilePostProcessMessage / cancelListenMediaFilePostProcessMessage(toggle)
 * ├── [3] 获取媒体文件列表              getMediaFilesList
 * ├── [4] 获取媒体文件下载地址          getMediaFilesDownloadURL(fileId 重载)
 * ├── [5] 批量删除媒体文件              deleteMediaFiles
 * ├── [6] 获取媒体上传 STS 凭证         getMediaUploadSts
 * └── [7] 媒体上传完成回调              mediaUploadCallback
 * </pre>
 *
 * <p><b>接口串联设计：</b>
 * <ul>
 *   <li>[3] 查列表拿到 fileId → [4] 按 fileId 换下载地址 → [5] 按 fileId 删除；</li>
 *   <li>[6] 获取 STS 凭证(由外部工具持凭证直传对象存储) → [7] 上传完成后回调服务端登记。
 *       注意 [7] 的 file.objectKey 须是文件在对象存储上的<b>完整 Key</b>, 而 [6] 返回的
 *       objectKeyPrefix 只是路径前缀, 两者不可混用；</li>
 *   <li>[1]/[2] 订阅后可留在菜单继续操作, 设备回传/后处理完成时推送落日志文件。</li>
 * </ul>
 *
 * <p><b>SDK 入口：</b>{@code session.workspace().getMediaServiceModule()}(挂在 Workspace 上)。
 *
 * <p><b>设计要点：</b>
 * <ul>
 *   <li>三态结果回显(SUCCESS/TIMEOUT/FAILED), 超时与失败明确区分；</li>
 *   <li>结果解析只在 SUCCESS 门控内(C++ 失败路径回 callback(err, {}) 空结构体, 经 SWIG 落地为非 null 对象、
 *       convert 后为非 null 空 bean, FAILED 时判空守卫不可靠; 仅模块层 Java 异常兜底会直接回 result=null)；</li>
 *   <li>[1]/[2] 的订阅经基类 subscribe 登记到会话 registry(demo 结束兜底退订), 正常路径由再次选择
 *       同一菜单项(toggle)或 run() 收尾显式关闭, 推送只落日志文件不刷控制台；</li>
 *   <li>STS securityToken 打印前脱敏({@link #maskToken}), accessKeySecret 不打印, 避免完整凭证落屏。</li>
 * </ul>
 */
public class MediaServiceDemo extends ModuleDemo {

    /** [1] 设备媒体回传订阅句柄(基类 subscribe 已登记到 registry, demo 结束自动收尾)。 */
    private Subscription deviceUploadSub;
    /** [2] 媒体后处理完成订阅句柄(同上)。 */
    private Subscription postProcessSub;

    @Override
    public String name() {
        return "MediaServiceDemo";
    }

    @Override
    protected void run() throws Exception {
        if (input == null) {
            DemoLog.warn(name(), "无交互输入(input=null), 无法运行菜单式媒体服务 demo");
            return;
        }

        final MediaServiceModule module = session.workspace().getMediaServiceModule();
        if (module == null) {
            DemoLog.error(name(), "getMediaServiceModule() 返回 null");
            console("   获取 MediaServiceModule 失败(返回 null), 无法进入媒体服务菜单");
            return;
        }
        final long actionTimeout = timeout("default_callback_timeout_ms");
        DemoLog.info(name(), "actionTimeout=" + actionTimeout + "ms");

        try {
            while (true) {
                Utils.printTitle("执行 " + name());
                if (deviceUploadSub != null) {
                    console("(设备媒体回传订阅中: 推送只落日志; 选 [1] 关闭)");
                }
                if (postProcessSub != null) {
                    console("(媒体后处理订阅中: 推送只落日志; 选 [2] 关闭)");
                }
                System.out.print(menu());
                int choice = Utils.readIntInRange(input, "请选择(1-7, q 退出): ", 1, 7);
                if (choice == Utils.QUIT) {
                    DemoLog.info(name(), "退出媒体服务菜单");
                    break;
                }
                try {
                    switch (choice) {
                        case 1: toggleDeviceUpload(module);                      break;
                        case 2: togglePostProcess(module);                       break;
                        case 3: getMediaFilesList(module, actionTimeout);        break;
                        case 4: getMediaFilesDownloadURL(module, actionTimeout); break;
                        case 5: deleteMediaFiles(module, actionTimeout);         break;
                        case 6: getMediaUploadSts(module, actionTimeout);        break;
                        case 7: mediaUploadCallback(module, actionTimeout);      break;
                        default: break;
                    }
                } catch (Exception e) {
                    console("   结果 -> [异常] " + Utils.describe(e));
                    DemoLog.error(name(), "菜单动作执行异常: " + Utils.describe(e), e);
                }
            }
        } finally {
            // registry() 会兜底关订阅, 这里显式关一次让本地字段清空, 并从 registry 移除死引用
            if (deviceUploadSub != null) {
                deviceUploadSub.close();
                registry().remove(deviceUploadSub);
                deviceUploadSub = null;
            }
            if (postProcessSub != null) {
                postProcessSub.close();
                registry().remove(postProcessSub);
                postProcessSub = null;
            }
        }
    }

    // ==================== [1] 订阅/取消 设备媒体回传通知 (listenMediaFileDeviceUploadMessage) ====================

    /** 再次选择同一菜单项即取消订阅(toggle)。 */
    private void toggleDeviceUpload(MediaServiceModule module) {
        if (deviceUploadSub != null) {
            deviceUploadSub.close();
            registry().remove(deviceUploadSub);
            deviceUploadSub = null;
            console("   已取消订阅设备媒体回传");
            return;
        }
        final String sn = readSn("设备回传");
        if (sn == null) {
            hint("已取消订阅操作");
            return;
        }
        int listenId = module.listenMediaFileDeviceUploadMessage(sn, new FlightHubCallback.MediaFileDeviceUploadCallback() {
            @Override
            public void onResult(MediaFileDeviceUpload info) {
                if (info == null) {
                    return;
                }
                pushLog("listenMediaFileDeviceUploadMessage() #MediaFileDeviceUpload, [设备媒体回传] fileId=" + info.getFileId() + " flightId=" + info.getFlightId()
                    + " sn=" + info.getDeviceSn()
                    + " fileType=" + info.getFileType() + "(" + describeFileType(info.getFileType()) + ")"
                    + " objectSize=" + info.getObjectSize() + " objectKey=" + info.getObjectKey());
            }
        });
        if (listenId == -1) {
            hint("订阅失败(listenId=-1)");
            return;
        }
        // subscribe() 已登记到会话 registry(会话结束自动退订), deviceUploadSub 另记一份供 toggle/收尾主动退订
        deviceUploadSub = subscribe("listenMediaFileDeviceUpload", listenId,
            id -> module.cancelListenMediaFileDeviceUploadMessage(id));
        console("   订阅已建立(listenId=" + listenId + ", sn=" + (sn.isEmpty() ? "(空)" : sn) + "), 推送只落日志文件");
    }

    // ==================== [2] 订阅/取消 媒体后处理完成通知 (listenMediaFilePostProcessMessage) ====================

    /** 再次选择同一菜单项即取消订阅(toggle)。 */
    private void togglePostProcess(MediaServiceModule module) {
        if (postProcessSub != null) {
            postProcessSub.close();
            registry().remove(postProcessSub);
            postProcessSub = null;
            console("   已取消订阅媒体后处理");
            return;
        }
        final String sn = readSn("后处理");
        if (sn == null) {
            hint("已取消订阅操作");
            return;
        }
        int listenId = module.listenMediaFilePostProcessMessage(sn, new FlightHubCallback.MediaFilePostProcessCallback() {
            @Override
            public void onResult(MediaFilePostProcess info) {
                if (info == null) {
                    return;
                }
                pushLog("listenMediaFilePostProcessMessage() #MediaFilePostProcess, [媒体后处理] fileId=" + info.getFileId() + " flightId=" + info.getFlightId()
                    + " sn=" + info.getDeviceSn()
                    + " fileType=" + info.getFileType() + "(" + describeFileType(info.getFileType()) + ")"
                    + " objectSize=" + info.getObjectSize() + " objectKey=" + info.getObjectKey()
                    + " previewObjectKey=" + info.getPreviewObjectKey());
            }
        });
        if (listenId == -1) {
            hint("订阅失败(listenId=-1)");
            return;
        }
        // subscribe() 已登记到会话 registry(会话结束自动退订), postProcessSub 另记一份供 toggle/收尾主动退订
        postProcessSub = subscribe("listenMediaFilePostProcess", listenId,
            id -> module.cancelListenMediaFilePostProcessMessage(id));
        console("   订阅已建立(listenId=" + listenId + ", sn=" + (sn.isEmpty() ? "(空)" : sn) + "), 推送只落日志文件");
    }

    // ==================== [3] 获取媒体文件列表 (getMediaFilesList) ====================

    private void getMediaFilesList(MediaServiceModule module, long timeoutMs) {
        Node cfg = params.node("media_service");
        final GetMediaFilesListParams p = new GetMediaFilesListParams();
        p.setFlightId(cfg.str("list_flight_id", ""));
        // deviceSn 须传机场 Dock SN: 媒体文件由机场上传, 服务端 media_files.sn 存机场 SN 并做等值匹配,
        // 传飞机 SN 查不到数据(见 bean GetMediaFilesListParams#setDeviceSn)。空串=不按 SN 过滤。
        p.setDeviceSn(cfg.str("list_device_sn", ""));
        p.setStartIndex(cfg.intVal("list_start_index", 0));
        // pageSize 范围 1-500(见 bean 注释)
        p.setPageSize(cfg.intVal("list_page_size", 20));
        // fileTypes/startCreateTime/endCreateTime 保持 bean 默认(空列表 / -1): SDK 组装 query 时直接省略
        // 这些可选参数(见 C++ http_interface.cpp get_media_files_list), 语义是"不过滤类型/不限时间段"。
        // fileTypes 枚举: "2"=图片, "3"=视频; 时间为秒级时间戳。

        console("   > flightId=" + p.getFlightId() + " deviceSn=" + p.getDeviceSn()
            + " startIndex=" + p.getStartIndex() + " pageSize=" + p.getPageSize());

        final Await<FlightHubError> await = new Await<>(name() + ".getMediaFilesList");
        final AtomicReference<GetMediaFilesListResults> ref = new AtomicReference<>();
        Outcome o = runOutcome("获取媒体文件列表", timeoutMs, await, () ->
            module.getMediaFilesList(p, new FlightHubCallback.GetMediaFilesListCallback() {
                @Override
                public void onResult(FlightHubError error, GetMediaFilesListResults r) {
                    ref.set(r);
                    await.set(error);
                }
            })
        );
        if (o == Outcome.SUCCESS) {
            GetMediaFilesListResults r = ref.get();
            if (r != null) {
                List<MediaFilesListItemInfoData> list = r.getList();
                int size = list != null ? list.size() : 0;
                console("   分页: total=" + r.getTotal() + " pageSize=" + r.getPageSize()
                    + " nextIndex=" + r.getNextIndex());
                console("   本页 " + size + " 条:");
                if (list != null) {
                    for (int i = 0; i < list.size(); i++) {
                        MediaFilesListItemInfoData it = list.get(i);
                        // size 单位为字节(C++ api/message.h MediaFilesListItemInfoData: 文件大小(字节))
                        console("   [" + (i + 1) + "] fileId=" + it.getFileId()
                            + "  size=" + it.getSize() + "B  fileName=" + it.getFileName()
                            + "  sn=" + it.getDeviceSn());
                        console("       objectKey=" + it.getObjectKey()
                            + "  flightId=" + it.getFlightId() + "  createdAt=" + it.getCreatedAt()
                            + "  type=" + it.getType());
                    }
                }
            }
        }
    }

    // ==================== [4] 获取媒体文件下载地址 (getMediaFilesDownloadURL) ====================

    private void getMediaFilesDownloadURL(MediaServiceModule module, long timeoutMs) {
        String idStr = Utils.readLineEcho(input, "   请输入 fileId(可先用 [3] 查列表获得, q 取消): ");
        if (idStr == null || idStr.trim().equalsIgnoreCase("q")) {
            hint("已取消获取下载地址");
            return;
        }
        if (idStr.trim().isEmpty()) {
            hint("fileId 不能为空");
            return;
        }
        final long fileId;
        try {
            fileId = Long.parseLong(idStr.trim());
        } catch (NumberFormatException e) {
            hint("fileId 必须是数字: " + idStr.trim());
            return;
        }

        final Await<FlightHubError> await = new Await<>(name() + ".getMediaFilesDownloadURL");
        final AtomicReference<GetMediaFilesDownloadUrlResults> ref = new AtomicReference<>();
        Outcome o = runOutcome("获取下载地址", timeoutMs, await, () ->
            module.getMediaFilesDownloadURL(fileId, new FlightHubCallback.GetMediaFilesDownloadUrlCallback() {
                @Override
                public void onResult(FlightHubError error, GetMediaFilesDownloadUrlResults r) {
                    ref.set(r);
                    await.set(error);
                }
            })
        );
        if (o == Outcome.SUCCESS && ref.get() != null) {
            console("   downloadUrl=" + ref.get().getUrl());
        }
    }

    // ==================== [5] 批量删除媒体文件 (deleteMediaFiles) ====================

    private void deleteMediaFiles(MediaServiceModule module, long timeoutMs) {
        String idsStr = Utils.readLineEcho(input, "   请输入 fileIds(逗号分隔多个, q 取消): ");
        if (idsStr == null || idsStr.trim().equalsIgnoreCase("q")) {
            hint("已取消删除操作");
            return;
        }
        // 空列表会被 SDK 本地校验拒绝(见 C++ media_service_module_impl.cpp), 此处在输入侧提前拦下
        if (idsStr.trim().isEmpty()) {
            hint("fileIds 不能为空");
            return;
        }
        final List<Long> fileIds = new ArrayList<>();
        for (String s : idsStr.split(",")) {
            String t = s.trim();
            if (t.isEmpty()) {
                continue;
            }
            try {
                fileIds.add(Long.parseLong(t));
            } catch (NumberFormatException e) {
                hint("fileId 必须是数字: " + t);
                return;
            }
        }
        console("   > fileIds=" + fileIds);
        callOutcome("批量删除媒体文件", cb -> module.deleteMediaFiles(fileIds, cb), timeoutMs);
    }

    // ==================== [6] 获取媒体上传 STS 凭证 (getMediaUploadSts) ====================

    private void getMediaUploadSts(MediaServiceModule module, long timeoutMs) {
        Node cfg = params.node("media_service");
        final GetMediaUploadStsParams p = new GetMediaUploadStsParams();
        // filePath 语义是"上传目标路径前缀(OSS Object Key 前缀)"而非单个文件的完整 Key(见 C++
        // request_message.h GetMediaUploadStsRequest.file_path), 服务端据此返回 objectKeyPrefix;
        // 为空会被 SDK 本地校验直接拒绝(ERROR_INVALID_PARAM, 见 media_service_module_impl.cpp)。
        // 示例默认为前缀形态(通常以 / 结尾), 旧参考 demo 用 "project/<uuid>/media/" 同款形态。
        p.setFilePath(cfg.str("upload_sts_file_path", "demo/media/"));
        // expire 单位秒(bean 注释); C++ request_message.h 标注最大 3600
        p.setExpire(cfg.intVal("upload_sts_expire", 3600));
        p.setSn(cfg.str("upload_sts_sn", session.hasDock() ? session.dockSn() : ""));
        p.setModule(cfg.intVal("upload_sts_module", 0));

        console("   > filePath=" + p.getFilePath() + " expire=" + p.getExpire() + "s"
            + " sn=" + p.getSn() + " module=" + p.getModule() + "(" + describeStsModule(p.getModule()) + ")");

        final Await<FlightHubError> await = new Await<>(name() + ".getMediaUploadSts");
        final AtomicReference<GetMediaUploadStsResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("获取上传 STS 凭证", timeoutMs, await, () ->
            module.getMediaUploadSts(p, new FlightHubCallback.GetMediaUploadStsCallback() {
                @Override
                public void onResult(FlightHubError error, GetMediaUploadStsResult r) {
                    ref.set(r);
                    await.set(error);
                }
            })
        );
        if (o == Outcome.SUCCESS && ref.get() != null) {
            GetMediaUploadStsResult r = ref.get();
            console("   bucket=" + r.getBucket() + " endpoint=" + r.getEndpoint()
                + " provider=" + r.getProvider() + " region=" + r.getRegion());
            console("   objectKeyPrefix=" + r.getObjectKeyPrefix());
            MediaUploadStsCredentials c = r.getCredentials();
            if (c != null) {
                // accessKeySecret 有意不打印; securityToken 脱敏后回显
                console("   accessKeyId=" + c.getAccessKeyId() + " expire=" + c.getExpire() + "s");
                console("   securityToken=" + maskToken(c.getSecurityToken()));
            }
        }
    }

    // ==================== [7] 媒体上传完成回调 (mediaUploadCallback) ====================

    private void mediaUploadCallback(MediaServiceModule module, long timeoutMs) {
        Node cfg = params.node("media_service");
        final MediaUploadCallbackParams p = new MediaUploadCallbackParams();
        p.setSn(cfg.str("upload_cb_sn", session.hasDock() ? session.dockSn() : ""));
        // result: 上传结果码, 0=成功(见 C++ request_message.h UploadCallbackData 注释)
        p.setResult(cfg.intVal("upload_cb_result", 0));
        // progress: 上传进度, 取值 0-100(见 bean MediaUploadCallbackParams)
        p.setProgress(cfg.intVal("upload_cb_progress", 100));
        p.setExpectedFileCount(cfg.intVal("upload_cb_expected_count", 1));
        p.setUploadedFileCount(cfg.intVal("upload_cb_uploaded_count", 1));
        // flightType: 飞行类型, bean 与 C++ 层均未标注枚举取值, 以实际为准
        p.setFlightType(cfg.intVal("upload_cb_flight_type", 0));
        // bizCode: 取 C++ request_message.h 注释示例值与旧参考 demo(FlightHubSDKDemo#handleMediaUploadCallback)
        // 实传值 "file_upload_callback"(仓库内无枚举出处, 服务端接受的完整取值集合以实际为准)
        p.setBizCode(cfg.str("upload_cb_biz_code", "file_upload_callback"));
        p.setVersion(cfg.str("upload_cb_version", "1.0"));

        MediaUploadCallbackFile file = new MediaUploadCallbackFile();
        // objectKey 须是文件在对象存储上的完整 Key(非 [6] 返回的 objectKeyPrefix 路径前缀);
        // objectKey/name 任一为空会被 SDK 本地校验直接拒绝(ERROR_INVALID_PARAM, 见
        // media_service_module_impl.cpp), 属本地拦截而非服务端返回。
        file.setObjectKey(cfg.str("upload_cb_object_key", ""));
        file.setName(cfg.str("upload_cb_file_name", ""));
        // type: bean 本字段未标注枚举, 仓库内证据矛盾——旧参考 demo(FlightHubSDKDemo#handleMediaUploadCallback)
        // 对 .jpeg 实传 0, C++ request_message.h UploadCallbackFile 默认亦为 0; 推送消息族 fileType 枚举
        // 2=图片 3=视频(见 bean MediaFileDeviceUpload#getFileType), 两者可能非同一枚举族, 以实际为准。
        // 默认取 0(仓库内唯一经实际调用验证过的取值)。
        file.setType(cfg.intVal("upload_cb_file_type", 0));
        file.setFingerprint(cfg.str("upload_cb_fingerprint", ""));
        p.setFile(file);

        console("   > sn=" + p.getSn() + " objectKey=" + file.getObjectKey()
            + " name=" + file.getName() + " progress=" + p.getProgress() + "%");

        final Await<FlightHubError> await = new Await<>(name() + ".mediaUploadCallback");
        final AtomicReference<MediaUploadCallbackResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("媒体上传完成回调", timeoutMs, await, () ->
            module.mediaUploadCallback(p, new FlightHubCallback.MediaUploadCallbackCallback() {
                @Override
                public void onResult(FlightHubError error, MediaUploadCallbackResult r) {
                    ref.set(r);
                    await.set(error);
                }
            })
        );
        if (o == Outcome.SUCCESS && ref.get() != null) {
            MediaUploadCallbackResult r = ref.get();
            console("   mediaId=" + r.getMediaId() + " id=" + r.getId() + " uuid=" + r.getUuid());
            console("   filename=" + r.getFilename() + " flightId=" + r.getFlightId()
                + " objectKey=" + r.getObjectKey());
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 读取订阅用 deviceSn(q / EOF 取消时返回 {@code null})。
     *
     * <p>订阅推送按 device_sn 与订阅时传入值<b>等值匹配</b>分发(NotificationCenter
     * TopicManager::OnPublish 对 sn 做等值校验); 留空即按空 SN 订阅——仓库内未见"空 SN=全部设备"
     * 的通配实现, 留空时是否收到推送取决于服务端行为, 以实际为准。
     */
    private String readSn(String tag) {
        String line = Utils.readLineEcho(input, "   请输入 deviceSn(" + tag + ", 可留空, q 取消): ");
        if (line == null || line.trim().equalsIgnoreCase("q")) {
            return null;
        }
        return line.trim();
    }

    /** 文件类型枚举描述(出处: bean MediaFileDeviceUpload#getFileType: 2=图片, 3=视频, -1=未知/默认)。 */
    private static String describeFileType(int fileType) {
        switch (fileType) {
            case 2:  return "图片";
            case 3:  return "视频";
            case -1: return "未知/默认";
            default: return "未知";
        }
    }

    /** STS 业务模块类型描述(出处: bean GetMediaUploadStsParams#getModule: 2=飞行记录STS, 2001=SN加密, 其他(含0)=默认设备STS)。 */
    private static String describeStsModule(int module) {
        switch (module) {
            case 2:    return "飞行记录 STS";
            case 2001: return "SN 加密";
            default:   return "默认设备 STS";
        }
    }

    /** securityToken 脱敏: 只保留前 6 位与后 4 位, 中间以 "..." 代替, 避免完整 STS 凭证落屏。 */
    private static String maskToken(String s) {
        if (s == null || s.length() <= 12) {
            return s;
        }
        return s.substring(0, 6) + "..." + s.substring(s.length() - 4);
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

    /** 监听推送专用：只落日志文件不刷控制台, 避免推送打断菜单交互。 */
    private void pushLog(String line) {
        DemoLog.info(name(), line);
    }

    // ==================== 菜单文案 ====================

    /**
     * 主菜单文案：7 个菜单项覆盖 {@code MediaServiceModule} 的 9 个常用接口([1]/[2] 为订阅/取消 toggle,
     * 见类 Javadoc 菜单树)。增删菜单项时须同步 {@link #run()} 中的 switch 分支与 {@code readIntInRange} 的取值上界。
     */
    private String menu() {
        return "\n"
            + "  === 媒体服务 (Media Service) ===\n"
            + "  1.  订阅/取消 设备媒体回传通知    (listenMediaFileDeviceUploadMessage, toggle, 推送只落日志)\n"
            + "  2.  订阅/取消 媒体后处理完成通知  (listenMediaFilePostProcessMessage, toggle, 推送只落日志)\n"
            + "  3.  获取媒体文件列表              (getMediaFilesList)\n"
            + "  4.  获取媒体文件下载地址          (getMediaFilesDownloadURL, 需输入 fileId)\n"
            + "  5.  批量删除媒体文件              (deleteMediaFiles, 需输入 fileIds)\n"
            + "  6.  获取媒体上传 STS 凭证         (getMediaUploadSts)\n"
            + "  7.  媒体上传完成回调              (mediaUploadCallback)\n"
            + "\n"
            + "  q.  返回上级菜单\n";
    }
}
