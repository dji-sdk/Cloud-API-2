// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.module;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.TerraBuildCreatePanoramaResult;
import com.dji.flighthubsdk.bean.TerraBuildCreateReconstructionParams;
import com.dji.flighthubsdk.bean.TerraBuildCreateReconstructionResult;
import com.dji.flighthubsdk.bean.TerraBuildModelFileItem;
import com.dji.flighthubsdk.bean.TerraBuildModelFileListResult;
import com.dji.flighthubsdk.bean.TerraBuildModelFileUrlResult;
import com.dji.flighthubsdk.bean.TerraBuildQualityReportResult;
import com.dji.flighthubsdk.bean.TerraBuildReconstructionImagesResult;
import com.dji.flighthubsdk.bean.TerraBuildReconstructionStatusChanged;
import com.dji.flighthubsdk.bean.TerraBuildSettingsResult;
import com.dji.flighthubsdk.bean.TerraBuildSfmResult;
import com.dji.flighthubsdk.bean.TerraModelDetailResult;
import com.dji.flighthubsdk.bean.TerraModelStatusChangedMsg;
import com.dji.flighthubsdk.bean.TerraRenameModelParams;
import com.dji.flighthubsdk.bean.TerraSetLasWktParams;
import com.dji.flighthubsdk.bean.TerraDeleteModelFilesResult;
import com.dji.flighthubsdk.bean.TerraSelectFilterParams;
import com.dji.flighthubsdk.bean.TerraShowOnMapParams;
import com.dji.flighthubsdk.bean.TerraShowOnMapResult;
import com.dji.flighthubsdk.bean.TerraUnzipProgressMsg;
import com.dji.flighthubsdk.bean.TerraUploadCallbackParams;
import com.dji.flighthubsdk.bean.TerraUploadCallbackResult;
import com.dji.flighthubsdk.core.TerraBuildModule;
import com.dji.flighthubsdk.demo.base.Menu;
import com.dji.flighthubsdk.demo.base.ModuleDemo;
import com.dji.flighthubsdk.demo.runtime.Await;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.runtime.Node;
import com.dji.flighthubsdk.demo.runtime.Subscription;
import com.dji.flighthubsdk.demo.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Terra Build 建图重建模块 Demo(菜单驱动): 模型文件管理、重建任务、全景拼接、WS 状态推送、LAS 坐标系设置。
 *
 * <p>主菜单为两级结构, [1][2][4] 为子菜单:
 * <pre>
 * 主菜单
 * ├── [1] 模型文件管理
 * │       ├── [1-1] 查询模型列表      getModelFileList(缓存 fileId/fileUuid)
 * │       ├── [1-2] 查询模型详情      getModelDetail
 * │       ├── [1-3] 获取文件 URL      getModelFileUrl
 * │       ├── [1-4] 重命名模型        renameModel
 * │       ├── [1-5] 上图/下图控制     showModelOnMap
 * │       ├── [1-6] 删除模型文件      deleteModelFile
 * │       └── [1-7] 模型上传回调      uploadModelCallback(外部上传后通知服务端)
 * ├── [2] 重建任务
 * │       ├── [2-1] 创建重建任务      createReconstruction(缓存 fileId/fileUuid)
 * │       ├── [2-2] 重试/停止重建     retryReconstruction / stopReconstruction
 * │       ├── [2-3] 查询重建设置      getReconstructionSettings
 * │       ├── [2-4] 获取质量报告      getQualityReport
 * │       ├── [2-5] 获取空三结果      getSfmResult
 * │       └── [2-6] 获取重建图像列表  getReconstructionImages
 * ├── [3] 全景拼接                    createPanorama(缓存 fileId/fileUuid)
 * ├── [4] WS 推送订阅
 * │       ├── [4-1] 重建任务状态变更  listenReconstructionStatusChanged
 * │       ├── [4-2] 模型解压进度      listenTerraUnzipProgress
 * │       └── [4-3] 模型状态变更      listenTerraModelStatusChanged
 * └── [5] 设置 LAS 坐标系             setLasWkt
 * </pre>
 *
 * <p><b>接口串联设计: </b>[1-1] 查询列表、[2-1] 创建重建、[3] 全景拼接、[1-7] 上传回调成功后
 * 均缓存 fileId/fileUuid({@link #lastFileId}/{@link #lastFileUuid}), 其余菜单项自动沿用,
 * 实现「创建 → 查询/操作 → 订阅推送跟踪进度」的业务串联; 未缓存时回退配置项
 * {@code terra_build.file_id}/{@code terra_build.file_uuid}, 支持操作历史任务。
 *
 * <p><b>SDK 入口: </b>{@code session.workspace().getTerraBuildModule()}(挂在 Workspace 上)。
 *
 * <p><b>设计要点: </b>
 * <ul>
 *   <li>覆盖 {@code TerraBuildModule} 29 个公开接口中的 22 个常用接口 (非全量):
 *       未覆盖 {@code batchDeleteModelFiles} 与 listen/cancelListen 三对地图上下图推送
 *       ({@code TerraFileShowOnMap}/{@code TerraMediaFileHideOnMap}/{@code TerraMediaFileShowOnMap}) 共 7 个; </li>
 *   <li>三态结果回显 (SUCCESS/TIMEOUT/FAILED), 结果解析只在 SUCCESS 分支进行
 *       (convert 层对 null 输入会返回非 null 空 bean, 非成功时判空守卫会失效); </li>
 *   <li>危险动作 (删除模型文件)执行前须输入 {@code yes} 二次确认; </li>
 *   <li>WS 订阅采用「订阅 → 阻塞等待按 Enter → 退订」模式, 推送只落日志文件不刷控制台;
 *       {@code subscribe()} 已登记会话 registry, 会话结束自动兜底退订。</li>
 * </ul>
 */
public class TerraBuildDemo extends ModuleDemo {

    /** 从模型列表/重建任务/全景拼接/上传回调缓存的 fileId(整型), 供详情/URL/重命名/上图/删除等使用; -1 表示未缓存。 */
    private int lastFileId = -1;
    /** 同上缓存的 fileUuid, 供查询设置/质量报告/空三/图像/重试/停止/LAS 坐标系等使用; null 表示未缓存。 */
    private String lastFileUuid = null;

    @Override
    public String name() {
        return "TerraBuildDemo";
    }

    @Override
    protected void run() throws Exception {
        if (input == null) {
            DemoLog.warn(name(), "无交互输入(input=null), 无法运行菜单式 Terra Build demo");
            return;
        }

        // workspace 是本模块的唯一依赖, 不需要 drone/dock
        final TerraBuildModule module = session.workspace().getTerraBuildModule();
        if (module == null) {
            DemoLog.error(name(), "getTerraBuildModule() 返回 null");
            console("   获取 TerraBuildModule 失败(返回 null), 无法进入本 demo 菜单");
            return;
        }

        final long actionTimeout = defaultTimeout();
        DemoLog.info(name(), "actionTimeout=" + actionTimeout + "ms");

        Menu.of(this, "执行 " + name())
                .banner("=== 建图重建 (Terra Build) ===")
                .header(() -> {
                    if (lastFileUuid != null) {
                        console("(已缓存 fileUuid=" + lastFileUuid + " fileId=" + lastFileId + ")");
                    }
                })
                .item("模型文件管理    (子菜单: 列表/详情/URL/重命名/上图/删除/上传回调)", () -> menuModelFiles(module, actionTimeout))
                .item("重建任务        (子菜单: 创建/重试/停止/设置/质量报告/空三/图像)", () -> menuReconstruction(module, actionTimeout))
                .item("全景拼接        (createPanorama)", () -> createPanorama(module, actionTimeout))
                .item("WS 推送订阅     (子菜单: 重建状态/解压进度/模型状态)", () -> menuSubscriptions(module))
                .item("设置 LAS 坐标系 (setLasWkt)", () -> setLasWkt(module, actionTimeout))
                .exitLog("退出 Terra Build 菜单")
                .run();
    }

    // ==================== 子菜单 [1]: 模型文件管理 ====================

    private void menuModelFiles(TerraBuildModule module, long timeout) {
        Menu.of(this, "模型文件管理")
                .banner("--- 模型文件管理 ---")
                .item("查询模型列表     (getModelFileList, 缓存 fileId/fileUuid)", () -> modelList(module, timeout))
                .item("查询模型详情     (getModelDetail)", () -> modelDetail(module, timeout))
                .item("获取文件 URL     (getModelFileUrl)", () -> modelGetUrl(module, timeout))
                .item("重命名模型       (renameModel)", () -> modelRename(module, timeout))
                .item("上图/下图控制    (showModelOnMap)", () -> modelShowOnMap(module, timeout))
                .item("删除模型文件     (deleteModelFile)", () -> modelDelete(module, timeout))
                .item("模型上传回调     (uploadModelCallback)", () -> uploadModelCallback(module, timeout))
                .quit("返回")
                .quitWord("返回")
                .run();
    }

    /** [1-1] 查询模型文件列表, 缓存第一条的 fileId 和 fileUuid。 */
    private void modelList(TerraBuildModule module, long timeout) {
        Node cfg = params.node("terra_build");
        int page = cfg.intVal("list_page", 1);
        int pageSize = cfg.intVal("list_page_size", 10);
        console("\n   查询模型文件列表: page=" + page + " pageSize=" + pageSize);

        // 双参回调 (error, result): Await<FlightHubError> 走 runOutcome 三态骨架;
        // 业务结果用 AtomicReference 在回调中保存, 只在 Outcome.SUCCESS 分支解析——
        // convert 层对 null 输入会返回非 null 空 bean, 非成功时判空守卫会失效。
        final Await<FlightHubError> await = new Await<>(name() + ".getModelFileList");
        final AtomicReference<TerraBuildModelFileListResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("查询模型文件列表", timeout, await, () ->
            module.getModelFileList(page, pageSize, new FlightHubCallback.TerraBuildGetModelFileListCallback() {
                @Override
                public void onResult(FlightHubError error, TerraBuildModelFileListResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );

        if (o == Outcome.SUCCESS && ref.get() != null) {
            TerraBuildModelFileListResult result = ref.get();
            List<TerraBuildModelFileItem> list =
                    result.getList() != null ? result.getList() : Collections.emptyList();
            console("   模型文件列表(本页 " + list.size() + " / 总计 " + result.getTotal() + "):");
            for (TerraBuildModelFileItem item : list) {
                // fileType 的数值枚举 SDK 内均未标注 (bean javadoc 标 "tif"/"b3dm"/"pnts" 字符串值却落在 int 字段, 疑为生成器串档;
                // C++ 层 terra_build.h 仅注 "文件大类型"), 原样打印裸值
                console("     id=" + item.getId() + " uuid=" + item.getUuid()
                    + " name=" + item.getName()
                    + " state=" + item.getState() + describeTerraState(item.getState())
                    + " fileType=" + item.getFileType());
                console("       previewUrl=" + item.getPreviewUrl()
                    + " createdAt=" + item.getCreatedAt()
                    + " updatedAt=" + item.getUpdatedAt());
                DemoLog.info(name(), "modelFile: id=" + item.getId() + " uuid=" + item.getUuid()
                    + " name=" + item.getName());
            }
            if (!list.isEmpty()) {
                lastFileId = list.get(0).getId();
                lastFileUuid = list.get(0).getUuid();
                hint("已缓存第一条 fileId=" + lastFileId + " fileUuid=" + lastFileUuid);
            }
        }
    }

    /** [1-2] 获取模型文件详情。 */
    private void modelDetail(TerraBuildModule module, long timeout) {
        int fileId = resolveFileId();
        if (fileId < 0) {
            return;
        }
        console("\n   查询模型详情: fileId=" + fileId);

        final Await<FlightHubError> await = new Await<>(name() + ".getModelDetail");
        final AtomicReference<TerraModelDetailResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("查询模型详情", timeout, await, () ->
            module.getModelDetail(fileId, new FlightHubCallback.TerraGetModelDetailCallback() {
                @Override
                public void onResult(FlightHubError error, TerraModelDetailResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );
        if (o == Outcome.SUCCESS && ref.get() != null) {
            TerraModelDetailResult r = ref.get();
            // size 单位为字节 (TerraModelDetailResult bean javadoc: 文件大小 (字节))
            console("     id=" + r.getId() + " uuid=" + r.getUuid()
                + " name=" + r.getName()
                + " state=" + r.getState() + describeTerraState(r.getState())
                + " showOnMap=" + r.isShowOnMap()
                + " size=" + r.getSize() + "B");
            DemoLog.info(name(), "modelDetail: " + r.getId() + " " + r.getName());
        }
    }

    /** [1-3] 获取模型文件签名 URL。type 枚举: 1=原始文件 (默认), 2=转换后 ZIP(出处: {@code TerraBuildModule.getModelFileUrl} javadoc)。 */
    private void modelGetUrl(TerraBuildModule module, long timeout) {
        int fileId = resolveFileId();
        if (fileId < 0) {
            return;
        }
        console("\n  --- 获取模型文件 URL ---");
        console("  1. 原始文件(type=1)   2. 转换后 ZIP(type=2)   q. 返回");
        int c = Utils.readIntInRange(input, "请选择: ", 1, 2);
        if (c == Utils.QUIT) {
            return;
        }
        int type = c; // 1 or 2

        console("   获取模型文件 URL: fileId=" + fileId + " type=" + type);
        final Await<FlightHubError> await = new Await<>(name() + ".getModelFileUrl");
        final AtomicReference<TerraBuildModelFileUrlResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("获取模型文件 URL", timeout, await, () ->
            module.getModelFileUrl(fileId, type, new FlightHubCallback.TerraBuildGetModelFileUrlCallback() {
                @Override
                public void onResult(FlightHubError error, TerraBuildModelFileUrlResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );
        if (o == Outcome.SUCCESS && ref.get() != null) {
            console("     url=" + ref.get().getUrl());
            DemoLog.info(name(), "modelFileUrl: " + ref.get().getUrl());
        }
    }

    /** [1-4] 模型重命名。 */
    private void modelRename(TerraBuildModule module, long timeout) {
        int fileId = resolveFileId();
        if (fileId < 0) {
            return;
        }
        String newName = params.node("terra_build").str("rename_to", "SDK Demo 重命名");
        TerraRenameModelParams p = new TerraRenameModelParams();
        p.setName(newName);
        console("   重命名模型: fileId=" + fileId + " newName=" + newName);

        // renameModel 的回调仅回传 error(无结果对象), 但回调类型是专属的
        // TerraRenameModelCallback 而非 ResultCallbackWithError, 不能走基类 callOutcome
        final Await<FlightHubError> await = new Await<>(name() + ".renameModel");
        runOutcome("重命名模型", timeout, await, () ->
            module.renameModel(fileId, p, new FlightHubCallback.TerraRenameModelCallback() {
                @Override
                public void onResult(FlightHubError error) {
                    await.set(error);
                }
            })
        );
    }

    /**
     * [1-5] 加载/取消加载模型到地图。
     *
     * <p>接口本身支持批量 ({@code TerraShowOnMapParams} 的 ids 列表与 selectFilter 全选过滤二选一),
     * demo 仅演示单个 fileId(demo 未做批量, 非接口限制)。
     */
    private void modelShowOnMap(TerraBuildModule module, long timeout) {
        int fileId = resolveFileId();
        if (fileId < 0) {
            return;
        }
        int state = Utils.readIntInRange(input, "   模型上图开关(0=下图 1=上图, q 取消): ", 0, 1, "取消");
        if (state == Utils.QUIT) {
            return;
        }

        Node cfg = params.node("terra_build");
        TerraShowOnMapParams p = new TerraShowOnMapParams();
        p.setIds(Collections.singletonList(fileId));
        p.setShowOnMap(state == 1);
        p.setRestore(cfg.bool("show_on_map_restore", false));
        int isDisplayed = cfg.intVal("show_on_map_is_displayed", -1);
        if (isDisplayed >= 0) p.setIsDisplayed(isDisplayed == 1);
        int inUniformHeight = cfg.intVal("show_on_map_in_uniform_height", -1);
        if (inUniformHeight >= 0) p.setInUniformHeight(inUniformHeight == 1);
        int modelDisplayMod = cfg.intVal("show_on_map_model_display_mod", -1);
        if (modelDisplayMod >= 0) p.setModelDisplayMod(modelDisplayMod);
        if (cfg.bool("show_on_map_select_enable", false)) {
            TerraSelectFilterParams sf = new TerraSelectFilterParams();
            sf.setGetSubFile(cfg.bool("select_get_sub_file", false));
            sf.setStartTime(cfg.longVal("select_start_time", 0L));
            sf.setEndTime(cfg.longVal("select_end_time", 0L));
            sf.setFileAllTypes(stringList(cfg.list("select_file_all_types")));
            sf.setPayloadModelKey(stringList(cfg.list("select_payload_model_key")));
            sf.setParentId(cfg.intVal("select_parent_id", 0));
            sf.setFileTags(stringList(cfg.list("select_file_tags")));
            int shareState = cfg.intVal("select_share_state", -1);
            if (shareState >= 0) sf.setShareState(shareState);
            int selectShowOnMap = cfg.intVal("select_show_on_map", -1);
            if (selectShowOnMap >= 0) sf.setShowOnMap(selectShowOnMap == 1);
            p.setSelectFilter(sf);
            p.setIds(new ArrayList<>());
        }
        console("   设置模型上图: fileId=" + fileId + " showOnMap=" + (state == 1)
            + " restore=" + p.isRestore() + " isDisplayed=" + p.getIsDisplayed()
            + " inUniformHeight=" + p.getInUniformHeight()
            + " modelDisplayMod=" + p.getModelDisplayMod());

        final Await<FlightHubError> await = new Await<>(name() + ".showModelOnMap");
        final AtomicReference<TerraShowOnMapResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("设置模型上图", timeout, await, () ->
            module.showModelOnMap(p, new FlightHubCallback.TerraShowOnMapCallback() {
                @Override
                public void onResult(FlightHubError error, TerraShowOnMapResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );
        if (o == Outcome.SUCCESS && ref.get() != null) {
            console("     实际操作的文件 IDs: " + ref.get().getIds());
        }
    }

    /** [1-6] 删除模型文件 (危险动作, 须输入 yes 二次确认)。 */
    private void modelDelete(TerraBuildModule module, long timeout) {
        int fileId = resolveFileId();
        if (fileId < 0) {
            return;
        }
        if (!confirmDangerous("删除模型文件 fileId=" + fileId)) {
            return;
        }

        final Await<FlightHubError> await = new Await<>(name() + ".deleteModelFileWithResult");
        final AtomicReference<TerraDeleteModelFilesResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("删除模型文件", timeout, await, () ->
            module.deleteModelFileWithResult(fileId, new FlightHubCallback.TerraDeleteModelFilesCallback() {
                @Override
                public void onResult(FlightHubError error, TerraDeleteModelFilesResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );
        if (o == Outcome.SUCCESS && ref.get() != null) {
            TerraDeleteModelFilesResult r = ref.get();
            console("   hasLockFiles=" + r.isHasLockFiles() + " lockFiles=" + r.getLockFiles());
        }
        // FAILED = 服务端明确拒绝, 文件确定还在, 保留缓存避免用户被迫重查;
        // SUCCESS/TIMEOUT(可能已生效)才清空缓存, 避免后续菜单沿用已删 fileId。
        if (o != Outcome.FAILED && lastFileId == fileId) {
            lastFileId = -1;
            lastFileUuid = null;
            hint("已清空缓存的 fileId/fileUuid");
        }
    }

    /** [1-7] 模型上传回调 (外部工具上传模型文件后, 调用此接口通知服务端; objectKey/name 必填)。 */
    private void uploadModelCallback(TerraBuildModule module, long timeout) {
        Node cfg = params.node("terra_build");
        String objectKey = cfg.str("upload_object_key", "");
        String fileName = cfg.str("upload_file_name", "");
        if (objectKey.isEmpty() || fileName.isEmpty()) {
            hint("terra_build.upload_object_key / upload_file_name 未配置, 无法执行上传回调");
            return;
        }
        TerraUploadCallbackParams p = new TerraUploadCallbackParams();
        p.setObjectKey(objectKey);
        p.setName(fileName);
        p.setShowOnMap(true); // demo 固定上传后直接上图
        console("\n   模型上传回调: objectKey=" + objectKey + " name=" + fileName);

        final Await<FlightHubError> await = new Await<>(name() + ".uploadModelCallback");
        final AtomicReference<TerraUploadCallbackResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("模型上传回调", timeout, await, () ->
            module.uploadModelCallback(p, new FlightHubCallback.TerraUploadCallbackCallback() {
                @Override
                public void onResult(FlightHubError error, TerraUploadCallbackResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );
        if (o == Outcome.SUCCESS && ref.get() != null) {
            TerraUploadCallbackResult r = ref.get();
            console("     id=" + r.getId() + " uuid=" + r.getUuid() + " name=" + r.getName());
            // getId() 返回 long, 而 deleteModelFile 等下游接口要求 int fileId(与
            // TerraBuildCreateReconstructionResult.getId() 一致为 int); 超出 int 范围时截断会导致
            // fileId 错位, 因此先做范围校验, 越界时保留旧缓存并告警而非静默截断。
            long id = r.getId();
            if (id < Integer.MIN_VALUE || id > Integer.MAX_VALUE) {
                DemoLog.error(name(), "uploadModelCallback 返回的 id=" + id + " 超出 int 范围, 放弃缓存以避免截断错误");
                hint("[警告] 服务端返回 id=" + id + " 超出 int 范围, 未缓存该 fileId, 请手动通过配置 terra_build.file_id 指定");
            } else {
                lastFileId = (int) id;
                lastFileUuid = r.getUuid();
                hint("已缓存上传后的 fileId=" + lastFileId + " fileUuid=" + lastFileUuid);
            }
        }
    }

    // ==================== 子菜单 [2]: 重建任务 ====================

    private void menuReconstruction(TerraBuildModule module, long timeout) {
        Menu.of(this, "重建任务")
                .banner("--- 重建任务 (建议: [1] 创建 → 主菜单 [4] WS 订阅跟踪进度) ---")
                .item("创建重建任务     (createReconstruction, 缓存 fileId/fileUuid)", () -> reconCreate(module, timeout))
                .item("重试/停止重建    (retryReconstruction / stopReconstruction)", () -> reconRetryOrStop(module, timeout))
                .item("查询重建设置     (getReconstructionSettings)", () -> reconGetSettings(module, timeout))
                .item("获取质量报告     (getQualityReport)", () -> reconGetQualityReport(module, timeout))
                .item("获取空三结果     (getSfmResult)", () -> reconGetSfmResult(module, timeout))
                .item("获取重建图像列表 (getReconstructionImages)", () -> reconGetImages(module, timeout))
                .quit("返回")
                .quitWord("返回")
                .run();
    }

    /**
     * [2-1] 创建重建任务。
     *
     * <p>重建类型枚举: 2=二维, 3=三维, 4=3DGS RGB, 6=全景, 7=激光雷达
     * (出处: {@code TerraBuildCreateReconstructionParams} bean javadoc)。
     * folderUuids 取自配置 {@code terra_build.folder_uuids}(媒体文件夹 UUID 列表, 与 folderIds 二选一, demo 走 UUID)。
     */
    private void reconCreate(TerraBuildModule module, long timeout) {
        Node cfg = params.node("terra_build");
        List<Object> rawUuids = cfg.list("folder_uuids");
        List<String> folderUuids = new ArrayList<>();
        for (Object obj : rawUuids) {
            if (obj != null && !obj.toString().isEmpty()) {
                folderUuids.add(obj.toString());
            }
        }
        if (folderUuids.isEmpty()) {
            hint("terra_build.folder_uuids 未配置(媒体文件夹 UUID 列表), 无法创建重建任务");
            return;
        }
        List<Object> rawTypes = cfg.list("reconstruction_type");
        List<Integer> reconTypes = new ArrayList<>();
        for (Object obj : rawTypes) {
            if (obj instanceof Number) {
                reconTypes.add(((Number) obj).intValue());
            }
        }
        if (reconTypes.isEmpty()) {
            reconTypes = Collections.singletonList(2); // 默认二维
        }

        TerraBuildCreateReconstructionParams reqParams = new TerraBuildCreateReconstructionParams();
        reqParams.setName(cfg.str("reconstruction_name", "SDK Demo 重建任务"));
        reqParams.setFolderUuids(folderUuids);
        reqParams.setReconstructionType(reconTypes);
        reqParams.setWkt(cfg.str("reconstruction_wkt", ""));

        console("\n   创建重建任务: name=" + reqParams.getName()
            + " type=" + reconTypes + " folderUuids=" + folderUuids);
        DemoLog.info(name(), "createReconstruction: " + reqParams.getName());

        final Await<FlightHubError> await = new Await<>(name() + ".createReconstruction");
        final AtomicReference<TerraBuildCreateReconstructionResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("创建重建任务", timeout, await, () ->
            module.createReconstruction(reqParams, new FlightHubCallback.TerraBuildCreateReconstructionCallback() {
                @Override
                public void onResult(FlightHubError error, TerraBuildCreateReconstructionResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );
        if (o == Outcome.SUCCESS && ref.get() != null) {
            TerraBuildCreateReconstructionResult r = ref.get();
            console("     id=" + r.getId() + " uuid=" + r.getUuid()
                + " jobUuid=" + r.getJobUuid());
            lastFileId = r.getId();
            lastFileUuid = r.getUuid();
            hint("已缓存 fileId=" + lastFileId + " fileUuid=" + lastFileUuid
                + ", 可订阅主菜单 [4-1] 重建状态推送跟踪进度");
        }
    }

    /** [2-2] 重试/停止重建任务。 */
    private void reconRetryOrStop(TerraBuildModule module, long timeout) {
        String fileUuid = resolveFileUuid();
        if (fileUuid == null) {
            return;
        }
        console("\n  --- 重建任务操作 (fileUuid=" + fileUuid + ") ---");
        console("  1. 重试重建   2. 停止重建   q. 返回");
        int c = Utils.readIntInRange(input, "请选择: ", 1, 2);
        if (c == Utils.QUIT) {
            return;
        }

        // retryReconstruction / stopReconstruction 的回调类型是 FlightHubCallback.ResultCallback, 走基类 syncOutcome 三态化
        if (c == 1) {
            syncOutcome("重试重建", cb -> module.retryReconstruction(fileUuid, cb), timeout);
        } else {
            syncOutcome("停止重建", cb -> module.stopReconstruction(fileUuid, cb), timeout);
        }
    }

    /** [2-3] 查询重建设置。 */
    private void reconGetSettings(TerraBuildModule module, long timeout) {
        String fileUuid = resolveFileUuid();
        if (fileUuid == null) {
            return;
        }
        console("\n   查询重建设置: fileUuid=" + fileUuid);

        final Await<FlightHubError> await = new Await<>(name() + ".getReconstructionSettings");
        final AtomicReference<TerraBuildSettingsResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("查询重建设置", timeout, await, () ->
            module.getReconstructionSettings(fileUuid, new FlightHubCallback.TerraBuildGetReconstructionSettingsCallback() {
                @Override
                public void onResult(FlightHubError error, TerraBuildSettingsResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );
        if (o == Outcome.SUCCESS && ref.get() != null) {
            TerraBuildSettingsResult r = ref.get();
            console("     state=" + r.getState() + describeTerraState(r.getState()));
            if (r.getReconstructionSettings() != null) {
                // reconstructionMode 取值枚举 SDK 内确无标注; qualityLevel 在本 settings bean/结构体两层未给取值,
                // 但 C++ 参数侧同名字段有标注 (terra_build.h ReconParameter3D: 1=高, 2=中, 3=低; Lidar/3DGS 变体语义各异), 此处原样打印裸值
                console("     reconName=" + r.getReconstructionSettings().getName()
                    + " mode=" + r.getReconstructionSettings().getReconstructionMode()
                    + " qualityLevel=" + r.getReconstructionSettings().getQualityLevel());
            }
            DemoLog.info(name(), "reconSettings state=" + r.getState());
        }
    }

    /**
     * [2-4] 获取质量报告 URL。
     *
     * <p>language 取值 SDK 内两处注释不一致 (C++ {@code TerraBuildReportSetting} 标 zh/en,
     * 创建参数 bean 标 zh/en/es/fr/de/ja/ko), 以实际为准; measureUnitSystem 两源一致:
     * {@code metric}=公制 (默认) / {@code imperial}=英制。
     */
    private void reconGetQualityReport(TerraBuildModule module, long timeout) {
        String fileUuid = resolveFileUuid();
        if (fileUuid == null) {
            return;
        }
        Node cfg = params.node("terra_build");
        String language = cfg.str("language", "zh");
        String unitSystem = cfg.str("measure_unit_system", "metric");
        console("\n   获取质量报告: fileUuid=" + fileUuid);

        final Await<FlightHubError> await = new Await<>(name() + ".getQualityReport");
        final AtomicReference<TerraBuildQualityReportResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("获取质量报告", timeout, await, () ->
            module.getQualityReport(fileUuid, language, unitSystem, new FlightHubCallback.TerraBuildGetQualityReportCallback() {
                @Override
                public void onResult(FlightHubError error, TerraBuildQualityReportResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );
        if (o == Outcome.SUCCESS && ref.get() != null) {
            console("     qualityReportUrl=" + ref.get().getQualityReportUrl());
            DemoLog.info(name(), "qualityReport: " + ref.get().getQualityReportUrl());
        }
    }

    /** [2-5] 获取空三结果。 */
    private void reconGetSfmResult(TerraBuildModule module, long timeout) {
        String fileUuid = resolveFileUuid();
        if (fileUuid == null) {
            return;
        }
        console("\n   获取空三结果: fileUuid=" + fileUuid);

        final Await<FlightHubError> await = new Await<>(name() + ".getSfmResult");
        final AtomicReference<TerraBuildSfmResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("获取空三结果", timeout, await, () ->
            module.getSfmResult(fileUuid, new FlightHubCallback.TerraBuildGetSfmResultCallback() {
                @Override
                public void onResult(FlightHubError error, TerraBuildSfmResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );
        if (o == Outcome.SUCCESS && ref.get() != null) {
            TerraBuildSfmResult r = ref.get();
            console("     sfmJsonUrl=" + r.getSfmJsonUrl());
            if (r.getReProjectionErrorPicUrlList() != null) {
                console("     reprojectionPics=" + r.getReProjectionErrorPicUrlList().size());
            }
            DemoLog.info(name(), "sfmResult: " + r.getSfmJsonUrl());
        }
    }

    /** [2-6] 获取重建图像列表。 */
    private void reconGetImages(TerraBuildModule module, long timeout) {
        String fileUuid = resolveFileUuid();
        if (fileUuid == null) {
            return;
        }
        console("\n   获取重建图像列表: fileUuid=" + fileUuid);

        final Await<FlightHubError> await = new Await<>(name() + ".getReconstructionImages");
        final AtomicReference<TerraBuildReconstructionImagesResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("获取重建图像列表", timeout, await, () ->
            module.getReconstructionImages(fileUuid, new FlightHubCallback.TerraBuildGetReconstructionImagesCallback() {
                @Override
                public void onResult(FlightHubError error, TerraBuildReconstructionImagesResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );
        if (o == Outcome.SUCCESS && ref.get() != null) {
            TerraBuildReconstructionImagesResult r = ref.get();
            int count = r.getList() != null ? r.getList().size() : 0;
            // total 字段语义 SDK 内注释不一致 (Java bean 标"总容量 (KB)"且 list 标"HMS 数据项列表", 疑为生成器串档; C++ 层无注释), 原样打印以实际为准
            console("     图像列表: 返回 " + count + " 条, total=" + r.getTotal());
            DemoLog.info(name(), "reconImages: total=" + r.getTotal() + " count=" + count);
        }
    }

    // ==================== [3] 全景拼接 (createPanorama) ====================

    private void createPanorama(TerraBuildModule module, long timeout) {
        Node cfg = params.node("terra_build");
        List<Object> rawUuids = cfg.list("folder_uuids");
        List<String> folderUuids = new ArrayList<>();
        for (Object obj : rawUuids) {
            if (obj != null && !obj.toString().isEmpty()) {
                folderUuids.add(obj.toString());
            }
        }
        if (folderUuids.isEmpty()) {
            hint("terra_build.folder_uuids 未配置, 无法创建全景拼接任务");
            return;
        }
        String panoramaName = cfg.str("panorama_name", "SDK Demo 全景拼接");
        String wkt = cfg.str("reconstruction_wkt", "");
        console("\n   创建全景拼接: name=" + panoramaName + " folderUuids=" + folderUuids);

        final Await<FlightHubError> await = new Await<>(name() + ".createPanorama");
        final AtomicReference<TerraBuildCreatePanoramaResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("创建全景拼接", timeout, await, () ->
            // 第 4 个参数 isShownOnMap=true: demo 固定拼接完成后直接上图
            module.createPanorama(panoramaName, folderUuids, wkt, true, new FlightHubCallback.TerraBuildCreatePanoramaCallback() {
                @Override
                public void onResult(FlightHubError error, TerraBuildCreatePanoramaResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );
        if (o == Outcome.SUCCESS && ref.get() != null) {
            TerraBuildCreatePanoramaResult r = ref.get();
            console("     id=" + r.getId() + " uuid=" + r.getUuid() + " jobUuid=" + r.getJobUuid());
            lastFileId = r.getId();
            lastFileUuid = r.getUuid();
            hint("已缓存 fileId=" + lastFileId + " fileUuid=" + lastFileUuid);
        }
    }

    // ==================== 子菜单 [4]: WS 推送订阅 ====================

    private void menuSubscriptions(TerraBuildModule module) {
        Menu.of(this, "WS 推送订阅")
                .banner("--- WS 推送订阅 (按 Enter 退订, 推送只落日志文件) ---")
                .item("重建任务状态变更 (listenReconstructionStatusChanged)", () -> listenReconstructionStatus(module))
                .item("模型解压进度     (listenTerraUnzipProgress)", () -> listenUnzipProgress(module))
                .item("模型状态变更     (listenTerraModelStatusChanged)", () -> listenModelStatusChanged(module))
                .quit("返回")
                .quitWord("返回")
                .run();
    }

    /** [4-1] 订阅重建任务状态变更 (阻塞等待, 按 Enter 退订; 推送只落日志文件, 不刷控制台)。 */
    private void listenReconstructionStatus(TerraBuildModule module) {
        console("\n>> [重建状态变更监听] 开始监听(推送只落日志文件) ...");
        int listenId = module.listenReconstructionStatusChanged(new FlightHubCallback.TerraBuildReconstructionStatusChangedCallback() {
            @Override
            public void onResult(TerraBuildReconstructionStatusChanged msg) {
                if (msg == null) {
                    return;
                }
                pushLog("listenReconstructionStatusChanged() #TerraBuildReconstructionStatusChanged, 重建状态变更: uuid=" + msg.getUuid()
                    + " name=" + msg.getName()
                    + " state=" + msg.getState() + describeTerraState(msg.getState()));
            }
        });
        if (listenId == -1) {
            hint("订阅失败(listenId=-1)");
            return;
        }
        // subscribe() 已登记到会话 registry(会话结束自动退订, 不得重复登记), sub 另存句柄供本方法内主动退订
        Subscription sub = subscribe("reconStatusChanged", listenId,
            id -> module.cancelListenReconstructionStatusChanged(id));
        console("   订阅已建立(listenId=" + listenId + "), 推送见日志文件");
        try {
            Utils.readLine(input, "   按 Enter 退订...");
        } finally {
            sub.close();
            registry().remove(sub);
        }
        hint("已退订重建状态变更");
    }

    /** [4-2] 订阅模型解压进度 (阻塞等待, 按 Enter 退订; 推送只落日志文件, 不刷控制台)。 */
    private void listenUnzipProgress(TerraBuildModule module) {
        console("\n>> [模型解压进度监听] 开始监听(推送只落日志文件) ...");
        int listenId = module.listenTerraUnzipProgress(new FlightHubCallback.TerraUnzipProgressCallback() {
            @Override
            public void onResult(TerraUnzipProgressMsg msg) {
                if (msg == null) {
                    return;
                }
                // percent 为解压进度百分比 [0, 100](TerraUnzipProgressMsg bean javadoc)
                pushLog("listenTerraUnzipProgress() #TerraUnzipProgressMsg, 解压进度: uuid=" + msg.getUuid()
                    + " name=" + msg.getName()
                    + " percent=" + msg.getPercent() + "%");
            }
        });
        if (listenId == -1) {
            hint("订阅失败(listenId=-1)");
            return;
        }
        // subscribe() 已登记到会话 registry(会话结束自动退订, 不得重复登记), sub 另存句柄供本方法内主动退订
        Subscription sub = subscribe("terraUnzipProgress", listenId,
            id -> module.cancelListenTerraUnzipProgress(id));
        console("   订阅已建立(listenId=" + listenId + "), 推送见日志文件");
        try {
            Utils.readLine(input, "   按 Enter 退订...");
        } finally {
            sub.close();
            registry().remove(sub);
        }
        hint("已退订解压进度");
    }

    /** [4-3] 订阅模型状态变更 (阻塞等待, 按 Enter 退订; 推送只落日志文件, 不刷控制台)。 */
    private void listenModelStatusChanged(TerraBuildModule module) {
        console("\n>> [模型状态变更监听] 开始监听(推送只落日志文件) ...");
        int listenId = module.listenTerraModelStatusChanged(new FlightHubCallback.TerraModelStatusChangedCallback() {
            @Override
            public void onResult(TerraModelStatusChangedMsg msg) {
                if (msg == null) {
                    return;
                }
                pushLog("listenTerraModelStatusChanged() #TerraModelStatusChangedMsg, 模型状态变更: uuid=" + msg.getUuid()
                    + " name=" + msg.getName()
                    + " state=" + msg.getState() + describeTerraState(msg.getState()));
            }
        });
        if (listenId == -1) {
            hint("订阅失败(listenId=-1)");
            return;
        }
        // subscribe() 已登记到会话 registry(会话结束自动退订, 不得重复登记), sub 另存句柄供本方法内主动退订
        Subscription sub = subscribe("terraModelStatusChanged", listenId,
            id -> module.cancelListenTerraModelStatusChanged(id));
        console("   订阅已建立(listenId=" + listenId + "), 推送见日志文件");
        try {
            Utils.readLine(input, "   按 Enter 退订...");
        } finally {
            sub.close();
            registry().remove(sub);
        }
        hint("已退订模型状态变更");
    }

    // ==================== [5] 设置 LAS 坐标系 (setLasWkt) ====================

    /**
     * [5] 设置 LAS 模型坐标系并触发格式转换。
     *
     * <p>仅适用于 subFileType=FormatLas(7) 的模型文件: 上传 LAS 后若探测到坐标系缺失,
     * 模型停在状态 4(选择坐标系), 此时调用本接口写入坐标系并触发 Las2Pnts 转换
     * (见 {@code TerraSetLasWktParams} bean javadoc)。wkt 支持 EPSG 代码 (如 "EPSG: 4326")或完整 WKT 字符串。
     */
    private void setLasWkt(TerraBuildModule module, long timeout) {
        String fileUuid = resolveFileUuid();
        if (fileUuid == null) {
            return;
        }
        String wkt = params.node("terra_build").str("las_wkt", "");
        if (wkt.isEmpty()) {
            hint("terra_build.las_wkt 未配置(如 \"EPSG:4326\" 或完整 WKT 字符串), 无法设置 LAS 坐标系");
            return;
        }
        TerraSetLasWktParams p = new TerraSetLasWktParams();
        p.setFileUuid(fileUuid);
        p.setWkt(wkt);
        console("\n   设置 LAS 坐标系: fileUuid=" + fileUuid + " wkt=" + wkt);

        // setLasWkt 的回调仅回传 error(无结果对象), 但回调类型是专属的
        // TerraSetLasWktCallback 而非 ResultCallbackWithError, 不能走基类 callOutcome
        final Await<FlightHubError> await = new Await<>(name() + ".setLasWkt");
        runOutcome("设置 LAS 坐标系", timeout, await, () ->
            module.setLasWkt(p, new FlightHubCallback.TerraSetLasWktCallback() {
                @Override
                public void onResult(FlightHubError error) {
                    await.set(error);
                }
            })
        );
    }

    // ==================== 辅助方法 ====================

    /**
     * 解析 fileId: 优先用会话缓存 {@link #lastFileId}, 其次配置 {@code terra_build.file_id},
     * 均无时提示并返回 -1(调用方中止)。
     */
    private int resolveFileId() {
        if (lastFileId >= 0) {
            return lastFileId;
        }
        int cfgId = params.node("terra_build").intVal("file_id", -1);
        if (cfgId >= 0) {
            hint("使用配置 terra_build.file_id=" + cfgId);
            return cfgId;
        }
        hint("fileId 未知：请先执行 [1-1] 查询模型列表, 或配置 terra_build.file_id");
        return -1;
    }

    /**
     * 解析 fileUuid: 优先用会话缓存 {@link #lastFileUuid}, 其次配置 {@code terra_build.file_uuid},
     * 均无时提示并返回 null(调用方中止)。
     */
    private String resolveFileUuid() {
        if (lastFileUuid != null && !lastFileUuid.isEmpty()) {
            return lastFileUuid;
        }
        String uuid = params.node("terra_build").str("file_uuid", "");
        if (!uuid.isEmpty()) {
            hint("使用配置 terra_build.file_uuid=" + uuid);
            return uuid;
        }
        hint("fileUuid 未知：请先执行 [1-1] 查询模型列表或 [2-1] 创建重建任务, 或配置 terra_build.file_uuid");
        return null;
    }

    /**
     * 建图文件状态枚举描述 (出处: {@code TerraBuildModelFileItem.getState} bean javadoc,
     * 与 C++ 层 {@code src/api/terra_build.h} 注释逐字一致)。
     */
    private static String describeTerraState(int state) {
        switch (state) {
            case 0:   return "(未知)";
            case 1:   return "(模型文件解压中)";
            case 2:   return "(模型文件解压成功)";
            case 3:   return "(模型文件解压失败)";
            case 4:   return "(选择坐标系)";
            case 5:   return "(格式转换中)";
            case 6:   return "(格式转换成功)";
            case 7:   return "(格式转换失败)";
            case 8:   return "(建图失败)";
            case 9:   return "(模型导入成功)";
            case 10:  return "(格式校验中)";
            case 11:  return "(格式校验失败)";
            case 12:  return "(等待Terra Cloud资源中)";
            case 13:  return "(等待资源失败)";
            case 14:  return "(重建中)";
            case 15:  return "(重建成功)";
            case 16:  return "(重建取消)";
            case 17:  return "(等待图片上传)";
            case 119: return "(空三运行失败)";
            case 120: return "(空三运行中)";
            case 121: return "(空三等待资源中)";
            case 122: return "(空三等待资源失败)";
            case 123: return "(空三被终止或取消)";
            case 124: return "(空三完成DMV解析中)";
            case 125: return "(DMV解析失败)";
            case 126: return "(DMV解析成功等待刺点)";
            default:  return "";
        }
    }

    private List<String> stringList(List<Object> raw) {
        List<String> out = new ArrayList<>();
        if (raw != null) {
            for (Object o : raw) {
                out.add(String.valueOf(o));
            }
        }
        return out;
    }

}
