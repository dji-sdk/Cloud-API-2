/*
 * Copyright 2025-2026 DJI. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dji.flighthub.mcp.tools.terra;

import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.tools.subscription.ListenChannelSupport;
import com.dji.flighthub.mcp.tools.subscription.SubscriptionBuffer;
import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthubsdk.bean.TerraBuildCreatePanoramaResult;
import com.dji.flighthubsdk.bean.TerraBuildCreateReconstructionParams;
import com.dji.flighthubsdk.bean.TerraBuildCreateReconstructionResult;
import com.dji.flighthubsdk.bean.TerraBuildModelFileListResult;
import com.dji.flighthubsdk.bean.TerraBuildModelFileUrlResult;
import com.dji.flighthubsdk.bean.TerraBuildQualityReportResult;
import com.dji.flighthubsdk.bean.TerraBuildReconstructionImagesResult;
import com.dji.flighthubsdk.bean.TerraBuildReconstructionStatusChanged;
import com.dji.flighthubsdk.bean.TerraBuildSettingsResult;
import com.dji.flighthubsdk.bean.TerraBuildSfmResult;
import com.dji.flighthubsdk.bean.TerraModelStatusChangedMsg;
import com.dji.flighthubsdk.bean.TerraUnzipProgressMsg;
import com.dji.flighthubsdk.core.TerraBuildModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * fh_terra — 三维重建与成果管理。
 * 基于飞行影像生成 2D 正射/3D 模型/全景/点云，查询重建进度和模型质量报告。
 *
 * <p>安全等级：混合
 * <ul>
 *   <li>create_reconstruction / create_panorama / delete_model / retry / stop / batch_delete_model
 *       — L1（需人工确认）</li>
 *   <li>status / model_list / model_url / quality_report / get_sfm / get_images — L0（只读查询）</li>
 * </ul>
 *
 * <p>对应 SDK 模块：TerraBuildModule（workspace.getTerraBuildModule()）
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhTerraTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhTerraTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_terra";

    // ── 参数名常量 ──

    private static final String PARAM_ACTION = "action";
    private static final String PARAM_TASK_ID = "task_id";
    private static final String PARAM_FILE_ID = "file_id";
    private static final String PARAM_MEDIA_IDS = "media_ids";
    private static final String PARAM_RECONSTRUCTION_TYPE = "reconstruction_type";
    private static final String PARAM_NAME = "name";
    private static final String PARAM_WKT = "wkt";
    private static final String PARAM_MODEL_TYPE = "model_type";
    private static final String PARAM_PAGE = "page";
    private static final String PARAM_PAGE_SIZE = "page_size";
    private static final String PARAM_LANGUAGE = "language";
    private static final String PARAM_CONFIRM = "confirm";
    private static final String PARAM_FILE_UUID = "file_uuid";
    private static final String PARAM_FILE_IDS = "file_ids";
    private static final String PARAM_CHANNEL = "channel";
    private static final String PARAM_CHANNELS = "channels";
    private static final String PARAM_SINCE_SEQ = "since_seq";
    private static final String PARAM_LIMIT = "limit";

    // ── 操作常量 ──

    private static final String ACTION_CREATE_RECONSTRUCTION = "create_reconstruction";
    private static final String ACTION_CREATE_PANORAMA = "create_panorama";
    private static final String ACTION_STATUS = "status";
    private static final String ACTION_MODEL_LIST = "model_list";
    private static final String ACTION_MODEL_URL = "model_url";
    private static final String ACTION_QUALITY_REPORT = "quality_report";
    private static final String ACTION_DELETE_MODEL = "delete_model";
    private static final String ACTION_RETRY = "retry";
    private static final String ACTION_STOP = "stop";
    private static final String ACTION_GET_SFM = "get_sfm";
    private static final String ACTION_BATCH_DELETE_MODEL = "batch_delete_model";
    private static final String ACTION_GET_IMAGES = "get_images";
    private static final String ACTION_LISTEN_STATUS = "listen_status";
    private static final String ACTION_EVENTS = "events";
    private static final String ACTION_LISTEN_STOP = "listen_stop";

    /** 有效操作列表 */
    private static final List<String> VALID_ACTIONS = List.of(
            ACTION_CREATE_RECONSTRUCTION, ACTION_CREATE_PANORAMA,
            ACTION_STATUS, ACTION_MODEL_LIST, ACTION_MODEL_URL, ACTION_QUALITY_REPORT,
            ACTION_DELETE_MODEL, ACTION_RETRY,
            ACTION_STOP, ACTION_GET_SFM, ACTION_BATCH_DELETE_MODEL, ACTION_GET_IMAGES,
            ACTION_LISTEN_STATUS, ACTION_EVENTS, ACTION_LISTEN_STOP
    );

    // ── 订阅 channel 常量 ──

    private static final String CHANNEL_STATUS_CHANGED = "status_changed";
    private static final String CHANNEL_UNZIP_PROGRESS = "unzip_progress";
    private static final String CHANNEL_MODEL_STATUS_CHANGED = "model_status_changed";

    /** 有效订阅 channel 列表 */
    private static final List<String> VALID_CHANNELS = List.of(
            CHANNEL_STATUS_CHANGED, CHANNEL_UNZIP_PROGRESS, CHANNEL_MODEL_STATUS_CHANGED
    );

    /** 订阅注册表命名空间（与 Tool 名对应） */
    private static final String SUBSCRIBE_NAMESPACE = "terra";

    /** 订阅作用域标识：本工具的订阅是 workspace 级，不区分设备 */
    private static final String SUBSCRIBE_SCOPE_ID = "workspace";

    /** events 动作默认读取条数 */
    private static final int DEFAULT_EVENTS_LIMIT = 50;

    /** 有效重建类型 */
    private static final List<String> VALID_RECONSTRUCTION_TYPES = List.of(
            "ortho_2d", "mesh_3d", "point_cloud"
    );

    /** 分页默认值 */
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;

    /** 模型类型默认值：0=正射影像(ortho) */
    private static final int DEFAULT_MODEL_TYPE_ORTHO = 0;
    private static final int MAX_PAGE_SIZE = 100;

    /** 默认报告语言 */
    private static final String DEFAULT_LANGUAGE = "zh";

    /** Mock ID 取模基数 */
    private static final int MOCK_ID_MODULUS = 100_000;

    /** batch_delete_model 一次最多删除的文件数量 */
    private static final int MAX_BATCH_DELETE_SIZE = 100;

    private final SdkService sdkService;
    private final ListenChannelSupport listenSupport;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     * @param listenSupport  订阅 channel 通用支持组件
     */
    public FhTerraTool(SessionManager sessionManager, SdkService sdkService, ListenChannelSupport listenSupport) {
        super(sessionManager);
        this.sdkService = sdkService;
        this.listenSupport = listenSupport;
    }

    /**
     * 工具级标志返回 false：status/model_list/model_url/quality_report 是只读的，不应被传输层硬拦截。
     * create_* 操作的确认在 execute() 内部通过 requireHumanConfirm() 实现。
     */
    @Override
    public boolean requiresHumanConfirmation() {
        return false;
    }

    /**
     * 返回本工具的 MCP 协议定义。
     *
     * @return MCP 工具定义 Map
     */
    /**
     * 声明工具级静态安全等级：REVERSIBLE。
     *
     * @return 静态安全等级
     */
    @Override
    public SecurityLevel getSecurityLevel() {
        return SecurityLevel.REVERSIBLE;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description",
                "3D reconstruction and model management: create ortho/3D/point-cloud reconstruction "
                + "or panorama from flight imagery, check reconstruction status, list model files, "
                + "get model URLs, retrieve quality reports, delete models, retry failed tasks, "
                + "stop a running task, fetch SfM results, batch-delete models, list source images, "
                + "and subscribe to reconstruction/unzip/model-status change events "
                + "(listen_status -> events -> listen_stop).");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_ACTION, Map.of(
                "type", "string",
                "enum", VALID_ACTIONS,
                "description", "create_reconstruction: start 3D reconstruction | "
                        + "create_panorama: generate panorama | "
                        + "status: check task progress | "
                        + "model_list: list model files | "
                        + "model_url: get download URL | "
                        + "quality_report: get quality metrics | "
                        + "delete_model: delete a model file by file_id | "
                        + "retry: retry a failed reconstruction task | "
                        + "stop: stop a running reconstruction task | "
                        + "get_sfm: fetch Structure-from-Motion (SfM) result URLs | "
                        + "batch_delete_model: delete multiple model files by file_ids | "
                        + "get_images: list source images used in a reconstruction task | "
                        + "listen_status: subscribe to a workspace-level event channel (channel required) | "
                        + "events: poll buffered events for one/all active channels since a cursor | "
                        + "listen_stop: unsubscribe one/all active channels"
        ));
        props.put(PARAM_TASK_ID, Map.of(
                "type", "string",
                "description", "Reconstruction task ID / file_uuid (required for status/quality_report/retry)"
        ));
        props.put(PARAM_FILE_ID, Map.of(
                "type", "integer",
                "description", "Model file ID (required for model_url/delete_model). Use action=model_list to find IDs."
        ));
        props.put(PARAM_MEDIA_IDS, Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", "Source media folder UUIDs (required for create_reconstruction/create_panorama)"
        ));
        props.put(PARAM_RECONSTRUCTION_TYPE, Map.of(
                "type", "string",
                "enum", VALID_RECONSTRUCTION_TYPES,
                "description", "Reconstruction type (required for create_reconstruction): ortho_2d/mesh_3d/point_cloud"
        ));
        props.put(PARAM_NAME, Map.of(
                "type", "string",
                "description", "Task name (required for create_panorama, optional for create_reconstruction)"
        ));
        props.put(PARAM_WKT, Map.of(
                "type", "string",
                "description", "WKT area coordinates (optional for create_*)"
        ));
        props.put(PARAM_MODEL_TYPE, Map.of(
                "type", "integer",
                "description", "Model type for URL: 0=ortho, 1=3D mesh, 2=point cloud (default 0)"
        ));
        props.put(PARAM_PAGE, Map.of(
                "type", "integer",
                "description", "Page number for model_list (default 1)"
        ));
        props.put(PARAM_PAGE_SIZE, Map.of(
                "type", "integer",
                "description", "Items per page for model_list (default 20, max 100)"
        ));
        props.put(PARAM_LANGUAGE, Map.of(
                "type", "string",
                "description", "Report language for quality_report (default zh)"
        ));
        props.put(PARAM_CONFIRM, Map.of(
                "type", "boolean",
                "description", "Required for create_reconstruction/create_panorama/delete_model/retry/"
                        + "stop/batch_delete_model actions"
        ));
        props.put(PARAM_FILE_UUID, Map.of(
                "type", "string",
                "description", "Reconstruction file_uuid (required for stop/get_sfm/get_images)"
        ));
        props.put(PARAM_FILE_IDS, Map.of(
                "type", "array",
                "items", Map.of("type", "integer"),
                "description", "Model file IDs to delete in batch (required for batch_delete_model, 1-"
                        + MAX_BATCH_DELETE_SIZE + " items). Use action=model_list to find IDs."
        ));
        props.put(PARAM_CHANNEL, Map.of(
                "type", "string",
                "enum", VALID_CHANNELS,
                "description", "Event channel to subscribe (required for listen_status): "
                        + "status_changed=reconstruction status changes | "
                        + "unzip_progress=source archive unzip progress | "
                        + "model_status_changed=generated model status changes"
        ));
        props.put(PARAM_CHANNELS, Map.of(
                "type", "array",
                "items", Map.of("type", "string", "enum", VALID_CHANNELS),
                "description", "Channel names for events/listen_stop (optional; defaults to all "
                        + "currently active channels when omitted)"
        ));
        props.put(PARAM_SINCE_SEQ, Map.of(
                "type", "integer",
                "description", "Cursor for events: only return events with _seq greater than this value (default 0)"
        ));
        props.put(PARAM_LIMIT, Map.of(
                "type", "integer",
                "description", "Max events to return per channel for events action (default "
                        + DEFAULT_EVENTS_LIMIT + ")"
        ));
        schema.put("properties", props);
        schema.put("required", List.of(PARAM_ACTION));
        def.put("inputSchema", schema);
        return def;
    }

    /**
     * 执行三维重建操作。
     *
     * @param args 输入参数
     * @return 执行结果
     */
    @Override
    public Map<String, Object> execute(Map<String, Object> args) {
        // 1. 校验 session + workspace
        Map<String, Object> preCheck = validateSessionAndWorkspace();
        if (preCheck != null) {
            return preCheck;
        }

        // 2. 校验 action
        String action = getStringArg(args, PARAM_ACTION, null);
        if (action == null || !VALID_ACTIONS.contains(action)) {
            return errorResult("Invalid or missing action: " + action,
                    "action must be one of: " + VALID_ACTIONS);
        }

        // 3. 按 action 校验参数
        Map<String, Object> validationError = validateActionParams(action, args);
        if (validationError != null) {
            return validationError;
        }

        // 4. 订阅类 action（READ_ONLY，不进入 create 确认流程）单独分发
        if (isSubscriptionAction(action)) {
            return dispatchSubscription(action, args);
        }

        // 5. create_* 操作需要安全等级 L1 + 人工确认
        if (isCreateAction(action)) {
            Map<String, Object> denied = enforceSecurityLevel(SecurityLevel.REVERSIBLE);
            if (denied != null) {
                return denied;
            }
            Map<String, Object> confirmError = requireHumanConfirm(args,
                    "terra_" + action, action,
                    buildConfirmMessage(action, args));
            if (confirmError != null) {
                return confirmError;
            }
        }

        // 6. 分发执行
        return sdkService.isMockMode()
                ? dispatchMockMode(action, args)
                : dispatchRealSdk(action, args);
    }

    // ── 校验方法 ──

    /**
     * 校验 session 和 workspace 可用性。
     *
     * @return 校验失败返回错误 Map，成功返回 null
     */
    private Map<String, Object> validateSessionAndWorkspace() {
        SessionContext session = requireSession();
        if (session == null) {
            return noSessionError();
        }
        if (sdkService.getWorkspace() == null) {
            return errorResult("SDK not initialized or workspace unavailable",
                    "Call fh_connect first to initialize.");
        }
        return null;
    }

    /**
     * 按 action 类型校验必填参数。
     */
    private Map<String, Object> validateActionParams(String action, Map<String, Object> args) {
        return switch (action) {
            case ACTION_CREATE_RECONSTRUCTION -> validateCreateReconstructionParams(args);
            case ACTION_CREATE_PANORAMA -> validateCreatePanoramaParams(args);
            case ACTION_STATUS -> validateStatusParams(args);
            case ACTION_MODEL_LIST -> null; // 全部可选
            case ACTION_MODEL_URL -> validateModelUrlParams(args);
            case ACTION_QUALITY_REPORT -> validateQualityReportParams(args);
            case ACTION_DELETE_MODEL -> validateDeleteModelParams(args);
            case ACTION_RETRY -> validateTaskIdRequired(args);
            case ACTION_STOP -> validateFileUuidRequired(args);
            case ACTION_GET_SFM -> validateFileUuidRequired(args);
            case ACTION_BATCH_DELETE_MODEL -> validateBatchDeleteModelParams(args);
            case ACTION_GET_IMAGES -> validateFileUuidRequired(args);
            case ACTION_LISTEN_STATUS -> validateChannelRequired(args);
            case ACTION_EVENTS -> null; // channels/since_seq/limit 均可选
            case ACTION_LISTEN_STOP -> null; // channels 可选，省略则停止全部
            default -> errorResult("Unknown action: " + action, "");
        };
    }

    /**
     * 校验 create_reconstruction 参数。
     */
    private Map<String, Object> validateCreateReconstructionParams(Map<String, Object> args) {
        List<String> mediaIds = parseStringList(args, PARAM_MEDIA_IDS);
        if (mediaIds == null) {
            return errorResult("Missing required parameter: media_ids",
                    "Provide media folder UUIDs for reconstruction.");
        }
        String type = getStringArg(args, PARAM_RECONSTRUCTION_TYPE, null);
        if (type == null || !VALID_RECONSTRUCTION_TYPES.contains(type)) {
            return errorResult("Missing or invalid reconstruction_type: " + type,
                    "reconstruction_type must be one of: " + VALID_RECONSTRUCTION_TYPES);
        }
        return null;
    }

    /**
     * 校验 create_panorama 参数。
     */
    private Map<String, Object> validateCreatePanoramaParams(Map<String, Object> args) {
        List<String> mediaIds = parseStringList(args, PARAM_MEDIA_IDS);
        if (mediaIds == null) {
            return errorResult("Missing required parameter: media_ids",
                    "Provide media folder UUIDs for panorama generation.");
        }
        String name = getStringArg(args, PARAM_NAME, null);
        if (name == null) {
            return errorResult("Missing required parameter: name",
                    "Provide a name for the panorama task.");
        }
        return null;
    }

    /**
     * 校验 status 参数。
     */
    private Map<String, Object> validateStatusParams(Map<String, Object> args) {
        String taskId = getStringArg(args, PARAM_TASK_ID, null);
        if (taskId == null) {
            return errorResult("Missing required parameter: task_id",
                    "Provide the reconstruction task ID (file_uuid).");
        }
        return null;
    }

    /**
     * 校验 model_url 参数。
     */
    private Map<String, Object> validateModelUrlParams(Map<String, Object> args) {
        int fileId = getIntArg(args, PARAM_FILE_ID, -1);
        if (fileId <= 0) {
            return errorResult("Missing or invalid parameter: file_id",
                    "Provide a valid model file ID (from model_list).");
        }
        return null;
    }

    /**
     * 校验 quality_report 参数。
     */
    private Map<String, Object> validateQualityReportParams(Map<String, Object> args) {
        String taskId = getStringArg(args, PARAM_TASK_ID, null);
        if (taskId == null) {
            return errorResult("Missing required parameter: task_id",
                    "Provide the reconstruction task ID (file_uuid).");
        }
        return null;
    }

    /**
     * 校验 retry 参数（需要 task_id）。
     */
    private Map<String, Object> validateTaskIdRequired(Map<String, Object> args) {
        String taskId = getStringArg(args, PARAM_TASK_ID, null);
        if (taskId == null) {
            return errorResult("Missing required parameter: task_id",
                    "Provide the reconstruction task ID (file_uuid) to retry.");
        }
        return null;
    }

    /**
     * 校验 delete_model 参数（需要 file_id，整数型）。
     */
    private Map<String, Object> validateDeleteModelParams(Map<String, Object> args) {
        Object fileIdObj = args.get(PARAM_FILE_ID);
        if (fileIdObj == null) {
            return errorResult("Missing required parameter: file_id",
                    "Provide the model file ID (integer) to delete. Use action=model_list to find file IDs.");
        }
        try {
            Integer.parseInt(fileIdObj.toString());
        } catch (NumberFormatException e) {
            return errorResult("Invalid file_id: must be an integer",
                    "file_id should be a numeric ID from model_list results.");
        }
        return null;
    }

    /**
     * 校验 stop/get_sfm/get_images 参数（需要 file_uuid）。
     */
    private Map<String, Object> validateFileUuidRequired(Map<String, Object> args) {
        String fileUuid = getStringArg(args, PARAM_FILE_UUID, null);
        if (fileUuid == null) {
            return errorResult("Missing required parameter: file_uuid",
                    "Provide the reconstruction file_uuid.");
        }
        return null;
    }

    /**
     * 校验 batch_delete_model 参数（需要 file_ids 数组，数量在 1 到上限之间）。
     */
    private Map<String, Object> validateBatchDeleteModelParams(Map<String, Object> args) {
        List<Integer> fileIds = parseFileIdsList(args);
        if (fileIds == null) {
            return errorResult("Missing or invalid parameter: file_ids",
                    "Provide an array of integer model file IDs (1-" + MAX_BATCH_DELETE_SIZE
                            + " items) to delete.");
        }
        if (fileIds.size() > MAX_BATCH_DELETE_SIZE) {
            return errorResult("Too many file_ids: " + fileIds.size(),
                    "file_ids must contain at most " + MAX_BATCH_DELETE_SIZE + " items.");
        }
        return null;
    }

    /**
     * 校验 listen_status 参数（需要 channel，取值在 VALID_CHANNELS 范围内）。
     */
    private Map<String, Object> validateChannelRequired(Map<String, Object> args) {
        String channel = getStringArg(args, PARAM_CHANNEL, null);
        if (channel == null || !VALID_CHANNELS.contains(channel)) {
            return errorResult("Missing or invalid parameter: channel",
                    "channel must be one of: " + VALID_CHANNELS);
        }
        return null;
    }

    // ── Mock 分发 ──

    /**
     * Mock 模式分发。
     */
    private Map<String, Object> dispatchMockMode(String action, Map<String, Object> args) {
        return switch (action) {
            case ACTION_CREATE_RECONSTRUCTION -> createReconstructionMock(args);
            case ACTION_CREATE_PANORAMA -> createPanoramaMock(args);
            case ACTION_STATUS -> statusMock(args);
            case ACTION_MODEL_LIST -> modelListMock(args);
            case ACTION_MODEL_URL -> modelUrlMock(args);
            case ACTION_QUALITY_REPORT -> qualityReportMock(args);
            case ACTION_DELETE_MODEL -> deleteModelMock(args);
            case ACTION_RETRY -> retryMock(args);
            case ACTION_STOP -> stopMock(args);
            case ACTION_GET_SFM -> getSfmMock(args);
            case ACTION_BATCH_DELETE_MODEL -> batchDeleteModelMock(args);
            case ACTION_GET_IMAGES -> getImagesMock(args);
            default -> errorResult("Unknown action: " + action, "");
        };
    }

    /**
     * Mock 模式 — 创建三维重建任务。
     */
    private Map<String, Object> createReconstructionMock(Map<String, Object> args) {
        String reconType = getStringArg(args, PARAM_RECONSTRUCTION_TYPE, "mesh_3d");
        String name = getStringArg(args, PARAM_NAME, "Reconstruction_" + System.currentTimeMillis() % MOCK_ID_MODULUS);
        List<String> mediaIds = parseStringList(args, PARAM_MEDIA_IDS);
        int mediaCount = mediaIds != null ? mediaIds.size() : 0;

        log.info("[MOCK] Terra reconstruction created: type={}, name={}, media_count={}",
                reconType, name, mediaCount);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_CREATE_RECONSTRUCTION);
        data.put("file_uuid", "recon_" + System.currentTimeMillis() % MOCK_ID_MODULUS);
        data.put("reconstruction_type", reconType);
        data.put("name", name);
        data.put("status", "processing");
        data.put("media_count", mediaCount);
        data.put("created_at", Instant.now().toString());
        data.put("_mock", true);

        return successResult(data, "Reconstruction task (" + reconType + ") created with "
                + mediaCount + " media folders.");
    }

    /**
     * Mock 模式 — 创建全景任务。
     */
    private Map<String, Object> createPanoramaMock(Map<String, Object> args) {
        String name = getStringArg(args, PARAM_NAME, "Panorama");
        List<String> mediaIds = parseStringList(args, PARAM_MEDIA_IDS);
        int mediaCount = mediaIds != null ? mediaIds.size() : 0;

        log.info("[MOCK] Terra panorama created: name={}, media_count={}", name, mediaCount);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_CREATE_PANORAMA);
        data.put("file_uuid", "pano_" + System.currentTimeMillis() % MOCK_ID_MODULUS);
        data.put("name", name);
        data.put("status", "processing");
        data.put("media_count", mediaCount);
        data.put("created_at", Instant.now().toString());
        data.put("_mock", true);

        return successResult(data, "Panorama task '" + name + "' created with "
                + mediaCount + " media folders.");
    }

    /**
     * Mock 模式 — 查询重建任务状态。
     */
    private Map<String, Object> statusMock(Map<String, Object> args) {
        String taskId = getStringArg(args, PARAM_TASK_ID, "");
        log.info("[MOCK] Terra status query: task_id={}", taskId);

        Instant now = Instant.now();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_STATUS);
        data.put("task_id", taskId);
        data.put("status", "completed");
        data.put("progress", 100);
        data.put("reconstruction_type", "mesh_3d");
        data.put("created_at", now.minusSeconds(5400).toString());
        data.put("completed_at", now.toString());
        data.put("_mock", true);

        return successResult(data, "Reconstruction task " + taskId + " status: completed (100%).");
    }

    /**
     * Mock 模式 — 查询模型文件列表。
     */
    private Map<String, Object> modelListMock(Map<String, Object> args) {
        int page = getIntArg(args, PARAM_PAGE, DEFAULT_PAGE);
        int pageSize = getIntArg(args, PARAM_PAGE_SIZE, DEFAULT_PAGE_SIZE);
        pageSize = Math.max(1, Math.min(pageSize, MAX_PAGE_SIZE));
        page = Math.max(1, page);

        log.info("[MOCK] Terra model list: page={}, pageSize={}", page, pageSize);

        Instant now = Instant.now();
        List<Map<String, Object>> models = new ArrayList<>();

        Map<String, Object> model1 = new LinkedHashMap<>();
        model1.put("file_id", 1001);
        model1.put("name", "Orthomosaic_Site_A");
        model1.put("type", "ortho_2d");
        model1.put("size_mb", 256.5);
        model1.put("created_at", now.minusSeconds(86400).toString());
        models.add(model1);

        Map<String, Object> model2 = new LinkedHashMap<>();
        model2.put("file_id", 1002);
        model2.put("name", "3D_Mesh_Survey_B");
        model2.put("type", "mesh_3d");
        model2.put("size_mb", 1024.0);
        model2.put("created_at", now.minusSeconds(43200).toString());
        models.add(model2);

        Map<String, Object> model3 = new LinkedHashMap<>();
        model3.put("file_id", 1003);
        model3.put("name", "PointCloud_Inspection");
        model3.put("type", "point_cloud");
        model3.put("size_mb", 512.3);
        model3.put("created_at", now.minusSeconds(3600).toString());
        models.add(model3);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_MODEL_LIST);
        data.put("models", models);
        data.put("total_count", models.size());
        data.put("page", page);
        data.put("page_size", pageSize);
        data.put("_mock", true);

        return successResult(data, "Found " + models.size() + " model files.");
    }

    /**
     * Mock 模式 — 获取模型文件下载 URL。
     */
    private Map<String, Object> modelUrlMock(Map<String, Object> args) {
        int fileId = getIntArg(args, PARAM_FILE_ID, 0);
        int modelType = getIntArg(args, PARAM_MODEL_TYPE, DEFAULT_MODEL_TYPE_ORTHO);

        log.info("[MOCK] Terra model URL: file_id={}, model_type={}", fileId, modelType);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_MODEL_URL);
        data.put("file_id", fileId);
        data.put("model_type", modelType);
        data.put("url", "https://terra.dji.com/models/mock_" + fileId + "/download?token=mock_token");
        data.put("expires_at", Instant.now().plusSeconds(3600).toString());
        data.put("_mock", true);

        return successResult(data, "Model file " + fileId + " download URL generated.");
    }

    /**
     * Mock 模式 — 获取质量报告。
     */
    private Map<String, Object> qualityReportMock(Map<String, Object> args) {
        String taskId = getStringArg(args, PARAM_TASK_ID, "");
        String language = getStringArg(args, PARAM_LANGUAGE, DEFAULT_LANGUAGE);

        log.info("[MOCK] Terra quality report: task_id={}, language={}", taskId, language);

        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("ground_resolution", "2.5 cm/pixel");
        metrics.put("coverage_percentage", 98.5);
        metrics.put("overlap_quality", "good");
        metrics.put("reconstruction_accuracy", "high");

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_QUALITY_REPORT);
        data.put("task_id", taskId);
        data.put("overall_score", 85);
        data.put("metrics", metrics);
        data.put("language", language);
        data.put("_mock", true);

        return successResult(data, "Quality report for task " + taskId + " (score: 85/100).");
    }

    /**
     * Mock 模式 — 删除重建模型文件。
     */
    private Map<String, Object> deleteModelMock(Map<String, Object> args) {
        int fileId = Integer.parseInt(args.get(PARAM_FILE_ID).toString());
        log.info("[MOCK] Delete model file: fileId={}", fileId);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_DELETE_MODEL);
        data.put("file_id", fileId);
        data.put("deleted", true);
        data.put("_mock", true);

        return successResult(data, "Model file " + fileId + " deleted.");
    }

    /**
     * Mock 模式 — 重试重建任务。
     */
    private Map<String, Object> retryMock(Map<String, Object> args) {
        String taskId = getStringArg(args, PARAM_TASK_ID, null);
        log.info("[MOCK] Retry reconstruction task: taskId={}", taskId);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_RETRY);
        data.put("task_id", taskId);
        data.put("state", "processing");
        data.put("_mock", true);

        return successResult(data, "Reconstruction task " + taskId + " retried, now processing.");
    }

    /**
     * Mock 模式 — 停止重建任务。
     */
    private Map<String, Object> stopMock(Map<String, Object> args) {
        String fileUuid = getStringArg(args, PARAM_FILE_UUID, "");
        log.info("[MOCK] Stop reconstruction: file_uuid={}", fileUuid);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_STOP);
        data.put("file_uuid", fileUuid);
        data.put("stopped", true);
        data.put("_mock", true);

        return successResult(data, "Reconstruction task " + fileUuid + " stopped.");
    }

    /**
     * Mock 模式 — 获取 SfM（Structure-from-Motion）结果。
     */
    private Map<String, Object> getSfmMock(Map<String, Object> args) {
        String fileUuid = getStringArg(args, PARAM_FILE_UUID, "");
        log.info("[MOCK] Get SFM result: file_uuid={}", fileUuid);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_GET_SFM);
        data.put("file_uuid", fileUuid);
        data.put("sfm_json_url", "https://terra.dji.com/mock/sfm_" + fileUuid + ".json");
        data.put("sfm_report_json_url", "https://terra.dji.com/mock/sfm_report_" + fileUuid + ".json");
        data.put("gcp_out_json_url", "https://terra.dji.com/mock/gcp_out_" + fileUuid + ".json");
        data.put("re_projection_error_pic_url_list",
                List.of("https://terra.dji.com/mock/reproj_" + fileUuid + "_1.jpg"));
        data.put("_mock", true);

        return successResult(data, "SFM result retrieved for task " + fileUuid);
    }

    /**
     * Mock 模式 — 批量删除重建模型文件。
     */
    private Map<String, Object> batchDeleteModelMock(Map<String, Object> args) {
        List<Integer> fileIds = parseFileIdsList(args);
        int count = fileIds != null ? fileIds.size() : 0;
        log.info("[MOCK] Batch delete model files: fileIds={}", fileIds);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_BATCH_DELETE_MODEL);
        data.put("file_ids", fileIds);
        data.put("deleted_count", count);
        data.put("_mock", true);

        return successResult(data, count + " model file(s) deleted.");
    }

    /**
     * Mock 模式 — 获取重建任务的源影像列表。
     */
    private Map<String, Object> getImagesMock(Map<String, Object> args) {
        String fileUuid = getStringArg(args, PARAM_FILE_UUID, "");
        log.info("[MOCK] Get reconstruction images: file_uuid={}", fileUuid);

        List<Map<String, Object>> images = new ArrayList<>();
        Map<String, Object> image1 = new LinkedHashMap<>();
        image1.put("image_id", "img_001");
        image1.put("file_name", "DJI_0001.JPG");
        image1.put("thumbnail_image_url", "https://terra.dji.com/mock/thumb_001.jpg");
        image1.put("origin_image_url", "https://terra.dji.com/mock/origin_001.jpg");
        images.add(image1);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_GET_IMAGES);
        data.put("file_uuid", fileUuid);
        data.put("images", images);
        data.put("total_count", images.size());
        data.put("_mock", true);

        return successResult(data, "Found " + images.size() + " reconstruction images for task " + fileUuid);
    }

    // ── 真实 SDK 分发 ──

    /**
     * 真实 SDK 模式分发。
     * 根据 action 类型调用 TerraBuildModule 对应的 SDK 方法。
     */
    private Map<String, Object> dispatchRealSdk(String action, Map<String, Object> args) {
        try {
            TerraBuildModule tbm = sdkService.getWorkspace().getTerraBuildModule();
            if (tbm == null) {
                return errorResult("TerraBuildModule unavailable",
                        "Workspace does not support 3D reconstruction. Check workspace configuration.");
            }

            return switch (action) {
                case ACTION_CREATE_RECONSTRUCTION -> createReconstructionReal(tbm, args);
                case ACTION_CREATE_PANORAMA -> createPanoramaReal(tbm, args);
                case ACTION_STATUS -> statusReal(tbm, args);
                case ACTION_MODEL_LIST -> modelListReal(tbm, args);
                case ACTION_MODEL_URL -> modelUrlReal(tbm, args);
                case ACTION_QUALITY_REPORT -> qualityReportReal(tbm, args);
                case ACTION_DELETE_MODEL -> deleteModelReal(tbm, args);
                case ACTION_RETRY -> retryReal(tbm, args);
                case ACTION_STOP -> stopReal(tbm, args);
                case ACTION_GET_SFM -> getSfmReal(tbm, args);
                case ACTION_BATCH_DELETE_MODEL -> batchDeleteModelReal(tbm, args);
                case ACTION_GET_IMAGES -> getImagesReal(tbm, args);
                default -> errorResult("Unknown action: " + action, "");
            };
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("fh_terra SDK error for action {}: code={}, msg={}",
                    action, e.getErrorCode(), e.getMessage(), e);
            return errorResult("SDK error: " + e.getMessage(),
                    "Check workspace connectivity and try again.");
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("fh_terra timeout for action {}: {}", action, e.getMessage(), e);
            return errorResult("SDK timeout: " + e.getMessage(),
                    "Operation timed out. Check server connectivity.");
        } catch (Exception e) {
            log.error("fh_terra error for action {}: {}", action, e.getMessage(), e);
            return errorResult("SDK error: " + e.getMessage(),
                    "Check workspace connectivity and try again.");
        }
    }

    /**
     * 真实 SDK — 创建三维重建任务。
     * 通过 TerraBuildModule.createReconstruction 提交重建请求。
     *
     * @param tbm  TerraBuildModule 实例
     * @param args 输入参数
     * @return 包含任务 id/uuid/job_uuid 的结果
     */
    private Map<String, Object> createReconstructionReal(TerraBuildModule tbm, Map<String, Object> args) {
        List<String> mediaIds = parseStringList(args, PARAM_MEDIA_IDS);
        String reconType = getStringArg(args, PARAM_RECONSTRUCTION_TYPE, "mesh_3d");
        String name = getStringArg(args, PARAM_NAME, "Reconstruction_" + System.currentTimeMillis() % MOCK_ID_MODULUS);

        // 构建参数
        TerraBuildCreateReconstructionParams params = new TerraBuildCreateReconstructionParams();
        params.setName(name);
        params.setFolderUuids(mediaIds);

        // 映射 reconstruction_type 到 SDK 枚举列表
        List<Integer> reconTypeList = mapReconstructionType(reconType);
        params.setReconstructionType(reconTypeList);

        // 异步转同步
        CompletableFuture<TerraBuildCreateReconstructionResult> future = new CompletableFuture<>();
        tbm.createReconstruction(params, (error, result) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(new SdkCallbackBridge.SdkException(
                        error.getServerCode(), formatSdkError(error)));
                return;
            }
            future.complete(result);
        });
        TerraBuildCreateReconstructionResult result = SdkCallbackBridge.await(future);

        log.info("[REAL] Terra reconstruction created: id={}, uuid={}, job_uuid={}",
                result.getId(), result.getUuid(), result.getJobUuid());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_CREATE_RECONSTRUCTION);
        data.put("id", result.getId());
        data.put("uuid", result.getUuid());
        data.put("job_uuid", result.getJobUuid());
        data.put("reconstruction_type", reconType);
        data.put("name", name);
        data.put("status", "processing");
        data.put("media_count", mediaIds != null ? mediaIds.size() : 0);
        data.put("created_at", Instant.now().toString());

        return successResult(data, "Reconstruction task (" + reconType + ") created successfully. job_uuid=" + result.getJobUuid());
    }

    /**
     * 真实 SDK — 创建全景任务。
     * 通过 TerraBuildModule.createPanorama 提交全景生成请求。
     *
     * @param tbm  TerraBuildModule 实例
     * @param args 输入参数
     * @return 包含任务 id/uuid/job_uuid 的结果
     */
    private Map<String, Object> createPanoramaReal(TerraBuildModule tbm, Map<String, Object> args) {
        String name = getStringArg(args, PARAM_NAME, "Panorama");
        List<String> mediaIds = parseStringList(args, PARAM_MEDIA_IDS);
        String wkt = getStringArg(args, PARAM_WKT, "");
        boolean shownOnMap = true;

        CompletableFuture<TerraBuildCreatePanoramaResult> future = new CompletableFuture<>();
        tbm.createPanorama(name, mediaIds, wkt, shownOnMap, (error, result) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(new SdkCallbackBridge.SdkException(
                        error.getServerCode(), formatSdkError(error)));
                return;
            }
            future.complete(result);
        });
        TerraBuildCreatePanoramaResult result = SdkCallbackBridge.await(future);

        log.info("[REAL] Terra panorama created: id={}, uuid={}, job_uuid={}",
                result.getId(), result.getUuid(), result.getJobUuid());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_CREATE_PANORAMA);
        data.put("id", result.getId());
        data.put("uuid", result.getUuid());
        data.put("job_uuid", result.getJobUuid());
        data.put("name", name);
        data.put("status", "processing");
        data.put("media_count", mediaIds != null ? mediaIds.size() : 0);
        data.put("created_at", Instant.now().toString());

        return successResult(data, "Panorama task '" + name + "' created successfully. job_uuid=" + result.getJobUuid());
    }

    /**
     * 真实 SDK — 查询重建任务状态（settings）。
     * 通过 TerraBuildModule.getReconstructionSettings 查询任务状态和配置。
     *
     * @param tbm  TerraBuildModule 实例
     * @param args 输入参数
     * @return 包含 state 和 settings 的结果
     */
    private Map<String, Object> statusReal(TerraBuildModule tbm, Map<String, Object> args) {
        String taskId = getStringArg(args, PARAM_TASK_ID, "");

        CompletableFuture<TerraBuildSettingsResult> future = new CompletableFuture<>();
        tbm.getReconstructionSettings(taskId, (error, result) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(new SdkCallbackBridge.SdkException(
                        error.getServerCode(), formatSdkError(error)));
                return;
            }
            future.complete(result);
        });
        TerraBuildSettingsResult result = SdkCallbackBridge.await(future);

        log.info("[REAL] Terra status query: task_id={}, state={}", taskId, result.getState());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_STATUS);
        data.put("task_id", taskId);
        data.put("state", result.getState());
        data.put("media_input_object_key", result.getMediaInputObjectKey());
        data.put("sfm_output_object_prefix", result.getSfmOutputObjectPrefix());

        // 包含 folder 信息
        if (result.getFolderList() != null) {
            data.put("folder_count", result.getFolderList().size());
        }

        return successResult(data, "Reconstruction task " + taskId + " state=" + result.getState());
    }

    /**
     * 真实 SDK — 查询模型文件列表。
     * 通过 TerraBuildModule.getModelFileList 分页获取。
     *
     * @param tbm  TerraBuildModule 实例
     * @param args 输入参数
     * @return 分页模型文件列表
     */
    private Map<String, Object> modelListReal(TerraBuildModule tbm, Map<String, Object> args) {
        int page = getIntArg(args, PARAM_PAGE, DEFAULT_PAGE);
        int pageSize = getIntArg(args, PARAM_PAGE_SIZE, DEFAULT_PAGE_SIZE);
        pageSize = Math.max(1, Math.min(pageSize, MAX_PAGE_SIZE));
        page = Math.max(1, page);

        CompletableFuture<TerraBuildModelFileListResult> future = new CompletableFuture<>();
        tbm.getModelFileList(page, pageSize, (error, result) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(new SdkCallbackBridge.SdkException(
                        error.getServerCode(), formatSdkError(error)));
                return;
            }
            future.complete(result);
        });
        TerraBuildModelFileListResult result = SdkCallbackBridge.await(future);

        log.info("[REAL] Terra model list: total={}, page={}, pageSize={}",
                result.getTotal(), result.getPage(), result.getPageSize());

        // 转换模型列表
        List<Map<String, Object>> models = new ArrayList<>();
        if (result.getList() != null) {
            result.getList().forEach(item -> {
                Map<String, Object> model = new LinkedHashMap<>();
                model.put("file_id", item.getId());
                model.put("uuid", item.getUuid());
                model.put("name", item.getName());
                model.put("file_type", item.getFileType());
                model.put("sub_file_type", item.getSubFileType());
                model.put("state", item.getState());
                model.put("reconstruction_type", item.getReconstructionType());
                model.put("created_at", item.getCreatedAt());
                models.add(model);
            });
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_MODEL_LIST);
        data.put("models", models);
        data.put("total_count", result.getTotal());
        data.put("page", result.getPage());
        data.put("page_size", result.getPageSize());

        return successResult(data, "Found " + result.getTotal() + " model files (page " + result.getPage() + ").");
    }

    /**
     * 真实 SDK — 获取模型文件下载 URL。
     * 通过 TerraBuildModule.getModelFileUrl 获取临时下载链接。
     *
     * @param tbm  TerraBuildModule 实例
     * @param args 输入参数
     * @return 包含 url 的结果
     */
    private Map<String, Object> modelUrlReal(TerraBuildModule tbm, Map<String, Object> args) {
        int fileId = getIntArg(args, PARAM_FILE_ID, 0);
        int modelType = getIntArg(args, PARAM_MODEL_TYPE, DEFAULT_MODEL_TYPE_ORTHO);

        CompletableFuture<TerraBuildModelFileUrlResult> future = new CompletableFuture<>();
        tbm.getModelFileUrl(fileId, modelType, (error, result) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(new SdkCallbackBridge.SdkException(
                        error.getServerCode(), formatSdkError(error)));
                return;
            }
            future.complete(result);
        });
        TerraBuildModelFileUrlResult result = SdkCallbackBridge.await(future);

        log.info("[REAL] Terra model URL for file_id={}: url_length={}", fileId,
                result.getUrl() != null ? result.getUrl().length() : 0);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_MODEL_URL);
        data.put("file_id", fileId);
        data.put("model_type", modelType);
        data.put("url", result.getUrl());

        return successResult(data, "Model file " + fileId + " download URL generated.");
    }

    /**
     * 真实 SDK — 获取质量报告 URL。
     * 通过 TerraBuildModule.getQualityReport 获取质量报告的下载链接。
     *
     * @param tbm  TerraBuildModule 实例
     * @param args 输入参数
     * @return 包含 quality_report_url 的结果
     */
    private Map<String, Object> qualityReportReal(TerraBuildModule tbm, Map<String, Object> args) {
        String taskId = getStringArg(args, PARAM_TASK_ID, "");
        String language = getStringArg(args, PARAM_LANGUAGE, DEFAULT_LANGUAGE);
        String measureUnitSystem = "metric";

        CompletableFuture<TerraBuildQualityReportResult> future = new CompletableFuture<>();
        tbm.getQualityReport(taskId, language, measureUnitSystem, (error, result) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(new SdkCallbackBridge.SdkException(
                        error.getServerCode(), formatSdkError(error)));
                return;
            }
            future.complete(result);
        });
        TerraBuildQualityReportResult result = SdkCallbackBridge.await(future);

        log.info("[REAL] Terra quality report: task_id={}, url_length={}",
                taskId, result.getQualityReportUrl() != null ? result.getQualityReportUrl().length() : 0);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_QUALITY_REPORT);
        data.put("task_id", taskId);
        data.put("quality_report_url", result.getQualityReportUrl());
        data.put("language", language);

        return successResult(data, "Quality report URL retrieved for task " + taskId);
    }

    /**
     * 真实 SDK — 删除重建模型文件。
     */
    @Generated
    private Map<String, Object> deleteModelReal(TerraBuildModule tbm, Map<String, Object> args) {
        int fileId = Integer.parseInt(args.get(PARAM_FILE_ID).toString());

        SdkCallbackBridge.awaitVoidRc(cb -> tbm.deleteModelFile(fileId, cb));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_DELETE_MODEL);
        data.put("file_id", fileId);
        data.put("deleted", true);

        return successResult(data, "Model file " + fileId + " deleted.");
    }

    /**
     * 真实 SDK — 重试重建任务。
     */
    @Generated
    private Map<String, Object> retryReal(TerraBuildModule tbm, Map<String, Object> args) {
        String taskId = getStringArg(args, PARAM_TASK_ID, null);

        SdkCallbackBridge.awaitVoidRc(cb -> tbm.retryReconstruction(taskId, cb));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_RETRY);
        data.put("task_id", taskId);
        data.put("state", "processing");

        return successResult(data, "Reconstruction task " + taskId + " retried, now processing.");
    }

    /**
     * 真实 SDK — 停止重建任务。
     */
    @Generated
    private Map<String, Object> stopReal(TerraBuildModule tbm, Map<String, Object> args) {
        String fileUuid = getStringArg(args, PARAM_FILE_UUID, null);

        SdkCallbackBridge.awaitVoidRc(cb -> tbm.stopReconstruction(fileUuid, cb));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_STOP);
        data.put("file_uuid", fileUuid);
        data.put("stopped", true);

        return successResult(data, "Reconstruction task " + fileUuid + " stopped.");
    }

    /**
     * 真实 SDK — 获取 SfM（Structure-from-Motion）结果。
     * 通过 TerraBuildModule.getSfmResult 获取稀疏重建相关文件的下载链接。
     *
     * @param tbm  TerraBuildModule 实例
     * @param args 输入参数
     * @return 包含 sfm_json_url 等字段的结果
     */
    private Map<String, Object> getSfmReal(TerraBuildModule tbm, Map<String, Object> args) {
        String fileUuid = getStringArg(args, PARAM_FILE_UUID, "");

        CompletableFuture<TerraBuildSfmResult> future = new CompletableFuture<>();
        tbm.getSfmResult(fileUuid, (error, result) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(new SdkCallbackBridge.SdkException(
                        error.getServerCode(), formatSdkError(error)));
                return;
            }
            future.complete(result);
        });
        TerraBuildSfmResult result = SdkCallbackBridge.await(future);

        log.info("[REAL] Terra SFM result: file_uuid={}, sfm_json_url_length={}",
                fileUuid, result.getSfmJsonUrl() != null ? result.getSfmJsonUrl().length() : 0);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_GET_SFM);
        data.put("file_uuid", fileUuid);
        data.put("sfm_json_url", result.getSfmJsonUrl());
        data.put("sfm_report_json_url", result.getSfmReportJsonUrl());
        data.put("gcp_out_json_url", result.getGcpOutJsonUrl());
        data.put("re_projection_error_pic_url_list", result.getReProjectionErrorPicUrlList());

        return successResult(data, "SFM result retrieved for task " + fileUuid);
    }

    /**
     * 真实 SDK — 批量删除重建模型文件。
     */
    @Generated
    private Map<String, Object> batchDeleteModelReal(TerraBuildModule tbm, Map<String, Object> args) {
        List<Integer> fileIds = parseFileIdsList(args);
        int count = fileIds != null ? fileIds.size() : 0;

        SdkCallbackBridge.awaitVoidRc(cb -> tbm.batchDeleteModelFiles(fileIds, cb));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_BATCH_DELETE_MODEL);
        data.put("file_ids", fileIds);
        data.put("deleted_count", count);

        return successResult(data, count + " model file(s) deleted.");
    }

    /**
     * 真实 SDK — 获取重建任务的源影像列表。
     * 通过 TerraBuildModule.getReconstructionImages 获取影像缩略图与原图链接。
     *
     * @param tbm  TerraBuildModule 实例
     * @param args 输入参数
     * @return 包含 images 列表的结果
     */
    private Map<String, Object> getImagesReal(TerraBuildModule tbm, Map<String, Object> args) {
        String fileUuid = getStringArg(args, PARAM_FILE_UUID, "");

        CompletableFuture<TerraBuildReconstructionImagesResult> future = new CompletableFuture<>();
        tbm.getReconstructionImages(fileUuid, (error, result) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(new SdkCallbackBridge.SdkException(
                        error.getServerCode(), formatSdkError(error)));
                return;
            }
            future.complete(result);
        });
        TerraBuildReconstructionImagesResult result = SdkCallbackBridge.await(future);

        log.info("[REAL] Terra reconstruction images: file_uuid={}, total={}", fileUuid, result.getTotal());

        List<Map<String, Object>> images = new ArrayList<>();
        if (result.getList() != null) {
            result.getList().forEach(item -> {
                Map<String, Object> image = new LinkedHashMap<>();
                image.put("image_id", item.getImageId());
                image.put("file_name", item.getFileName());
                image.put("thumbnail_image_url", item.getThumbnailImageUrl());
                image.put("origin_image_url", item.getOriginImageUrl());
                images.add(image);
            });
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_GET_IMAGES);
        data.put("file_uuid", fileUuid);
        data.put("images", images);
        data.put("total_count", result.getTotal());

        return successResult(data, "Found " + result.getTotal() + " reconstruction images for task " + fileUuid);
    }

    /**
     * 将字符串重建类型映射为 SDK 所需的整数列表。
     * ortho_2d=0, mesh_3d=1, point_cloud=2
     *
     * @param reconType 重建类型字符串
     * @return SDK 枚举整数列表
     */
    private List<Integer> mapReconstructionType(String reconType) {
        int type = switch (reconType) {
            case "ortho_2d" -> 0;
            case "mesh_3d" -> 1;
            case "point_cloud" -> 2;
            default -> 1; // 默认 mesh_3d
        };
        return List.of(type);
    }

    // ── 订阅分发 ──

    /**
     * 订阅类 action 分发入口。
     *
     * @param action 操作名
     * @param args   输入参数
     * @return 执行结果
     */
    private Map<String, Object> dispatchSubscription(String action, Map<String, Object> args) {
        SessionContext session = requireSession();
        return switch (action) {
            case ACTION_LISTEN_STATUS -> handleListenStatus(session, args);
            case ACTION_EVENTS -> handleEvents(session, args);
            case ACTION_LISTEN_STOP -> handleListenStop(session, args);
            default -> errorResult("Unknown action: " + action, "");
        };
    }

    /**
     * 处理 listen_status：订阅指定 channel，返回 listen_id。
     */
    private Map<String, Object> handleListenStatus(SessionContext session, Map<String, Object> args) {
        String channel = getStringArg(args, PARAM_CHANNEL, null);
        String registryKey = ListenChannelSupport.registryKey(SUBSCRIBE_NAMESPACE, SUBSCRIBE_SCOPE_ID, channel);
        String scope = SubscriptionBuffer.scope(session, SUBSCRIBE_SCOPE_ID);
        boolean mock = sdkService.isMockMode();

        int listenId = listenSupport.startChannel(session, registryKey, channel, scope, channel, mock,
                mockSupplierFor(channel),
                onEvent -> startRealListen(channel, onEvent),
                listenId2 -> cancelRealListen(channel, listenId2));

        if (listenId < 0) {
            return errorResult("Failed to start listening on channel: " + channel,
                    "Check SDK connectivity and try again.");
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_LISTEN_STATUS);
        data.put("channel", channel);
        data.put("listen_id", listenId);
        return successResult(data, "Subscribed to channel '" + channel + "'. Poll with action=events.");
    }

    /**
     * 处理 events：按游标轮询一个或多个已订阅 channel 的缓冲事件。
     */
    private Map<String, Object> handleEvents(SessionContext session, Map<String, Object> args) {
        List<String> channels = resolveChannels(session, args);
        long sinceSeq = getLongArg(args, PARAM_SINCE_SEQ, 0L);
        int limit = getIntArg(args, PARAM_LIMIT, DEFAULT_EVENTS_LIMIT);

        Map<String, Object> streams = new LinkedHashMap<>();
        for (String channel : channels) {
            String registryKey = ListenChannelSupport.registryKey(SUBSCRIBE_NAMESPACE, SUBSCRIBE_SCOPE_ID, channel);
            String scope = SubscriptionBuffer.scope(session, SUBSCRIBE_SCOPE_ID);
            streams.put(channel, listenSupport.readChannel(session, registryKey, scope, channel, sinceSeq, limit));
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_EVENTS);
        data.put("streams", streams);
        return successResult(data, "Polled " + streams.size() + " channel(s).");
    }

    /**
     * 处理 listen_stop：停止一个/多个/全部已订阅的 channel。
     */
    private Map<String, Object> handleListenStop(SessionContext session, Map<String, Object> args) {
        List<String> requested = getStringListArg(args, PARAM_CHANNELS);
        List<String> channels = (requested != null && !requested.isEmpty())
                ? requested
                : listenSupport.activeChannels(session, subscribePrefix());

        ListenChannelSupport.StopResult result = listenSupport.stopChannels(session, channels,
                channel -> ListenChannelSupport.registryKey(SUBSCRIBE_NAMESPACE, SUBSCRIBE_SCOPE_ID, channel), log);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_LISTEN_STOP);
        data.put("stopped", result.stopped());
        data.put("not_active", result.notActive());
        data.put("failed", result.failed());
        return successResult(data, "Stopped " + result.stopped().size() + " channel(s).");
    }

    /**
     * 解析 events 要轮询的 channel 列表：显式传入 channels 则用它，否则取当前全部已激活 channel。
     */
    private List<String> resolveChannels(SessionContext session, Map<String, Object> args) {
        List<String> requested = getStringListArg(args, PARAM_CHANNELS);
        if (requested != null && !requested.isEmpty()) {
            return requested;
        }
        return listenSupport.activeChannels(session, subscribePrefix());
    }

    /**
     * 本工具订阅注册表 key 的前缀（namespace::scopeId::）。
     */
    private String subscribePrefix() {
        return SUBSCRIBE_NAMESPACE + "::" + SUBSCRIBE_SCOPE_ID + "::";
    }

    /**
     * 按 channel 选择对应的 mock 事件生成器。
     */
    private ListenChannelSupport.MockEventSupplier mockSupplierFor(String channel) {
        return switch (channel) {
            case CHANNEL_STATUS_CHANGED -> this::mockStatusChangedEvent;
            case CHANNEL_UNZIP_PROGRESS -> this::mockUnzipProgressEvent;
            case CHANNEL_MODEL_STATUS_CHANGED -> this::mockModelStatusChangedEvent;
            default -> Map::of;
        };
    }

    /**
     * 按 channel 调用真实 SDK 的 listen 方法，返回 listenId；TerraBuildModule 不可用时返回 -1。
     */
    private int startRealListen(String channel, Consumer<Map<String, Object>> onEvent) {
        TerraBuildModule tbm = sdkService.getWorkspace().getTerraBuildModule();
        if (tbm == null) {
            return -1;
        }
        return switch (channel) {
            case CHANNEL_STATUS_CHANGED -> tbm.listenReconstructionStatusChanged(
                    msg -> onEvent.accept(toMap(msg)));
            case CHANNEL_UNZIP_PROGRESS -> tbm.listenTerraUnzipProgress(
                    msg -> onEvent.accept(toMap(msg)));
            case CHANNEL_MODEL_STATUS_CHANGED -> tbm.listenTerraModelStatusChanged(
                    msg -> onEvent.accept(toMap(msg)));
            default -> -1;
        };
    }

    /**
     * 按 channel 调用真实 SDK 的 cancelListen 方法。
     */
    private void cancelRealListen(String channel, int listenId) {
        TerraBuildModule tbm = sdkService.getWorkspace().getTerraBuildModule();
        if (tbm == null) {
            return;
        }
        switch (channel) {
            case CHANNEL_STATUS_CHANGED -> tbm.cancelListenReconstructionStatusChanged(listenId);
            case CHANNEL_UNZIP_PROGRESS -> tbm.cancelListenTerraUnzipProgress(listenId);
            case CHANNEL_MODEL_STATUS_CHANGED -> tbm.cancelListenTerraModelStatusChanged(listenId);
            default -> { }
        }
    }

    /**
     * Mock — 重建状态变更事件。
     */
    private Map<String, Object> mockStatusChangedEvent() {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("workspace_id", "mock_ws");
        event.put("id", 1001L);
        event.put("name", "Reconstruction_mock");
        event.put("state", 2);
        event.put("uuid", "recon_mock_uuid");
        event.put("file_type", 1);
        event.put("sub_file_type", 0);
        event.put("code", 0);
        event.put("size", 1024000L);
        event.put("_mock", true);
        return event;
    }

    /**
     * Mock — 源文件解压进度事件。
     */
    private Map<String, Object> mockUnzipProgressEvent() {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("file_id", 1001L);
        event.put("uuid", "unzip_mock_uuid");
        event.put("name", "mock_archive.zip");
        event.put("percent", 42.5);
        event.put("_mock", true);
        return event;
    }

    /**
     * Mock — 模型状态变更事件。
     */
    private Map<String, Object> mockModelStatusChangedEvent() {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("file_id", 1002L);
        event.put("uuid", "model_mock_uuid");
        event.put("name", "mock_model");
        event.put("state", 2);
        event.put("file_type", 1);
        event.put("sub_file_type", 0);
        event.put("error_code", 0);
        event.put("_mock", true);
        return event;
    }

    /**
     * 将 TerraBuildReconstructionStatusChanged 转换为事件 Map。
     */
    private Map<String, Object> toMap(TerraBuildReconstructionStatusChanged msg) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("workspace_id", msg.getWorkspaceId());
        event.put("id", msg.getId());
        event.put("name", msg.getName());
        event.put("state", msg.getState());
        event.put("uuid", msg.getUuid());
        event.put("file_type", msg.getFileType());
        event.put("sub_file_type", msg.getSubFileType());
        event.put("code", msg.getCode());
        event.put("size", msg.getSize());
        return event;
    }

    /**
     * 将 TerraUnzipProgressMsg 转换为事件 Map。
     */
    private Map<String, Object> toMap(TerraUnzipProgressMsg msg) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("file_id", msg.getFileId());
        event.put("uuid", msg.getUuid());
        event.put("name", msg.getName());
        event.put("percent", msg.getPercent());
        return event;
    }

    /**
     * 将 TerraModelStatusChangedMsg 转换为事件 Map。
     */
    private Map<String, Object> toMap(TerraModelStatusChangedMsg msg) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("file_id", msg.getFileId());
        event.put("uuid", msg.getUuid());
        event.put("name", msg.getName());
        event.put("state", msg.getState());
        event.put("file_type", msg.getFileType());
        event.put("sub_file_type", msg.getSubFileType());
        event.put("error_code", msg.getErrorCode());
        return event;
    }

    // ── 辅助方法 ──

    /**
     * 解析 file_ids 参数为整数列表，元素非法或列表为空时返回 null。
     *
     * @param args 输入参数
     * @return 解析得到的整数列表；参数缺失、为空或含非法元素时返回 null
     */
    private List<Integer> parseFileIdsList(Map<String, Object> args) {
        Object value = args != null ? args.get(PARAM_FILE_IDS) : null;
        if (!(value instanceof List<?> rawList) || rawList.isEmpty()) {
            return null;
        }
        List<Integer> result = new ArrayList<>(rawList.size());
        for (Object item : rawList) {
            if (item == null) {
                continue;
            }
            try {
                result.add(Integer.parseInt(item.toString().trim()));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return result.isEmpty() ? null : result;
    }

    /**
     * 判断是否为写操作（需要人工确认）。
     */
    private boolean isCreateAction(String action) {
        return ACTION_CREATE_RECONSTRUCTION.equals(action)
                || ACTION_CREATE_PANORAMA.equals(action)
                || ACTION_DELETE_MODEL.equals(action)
                || ACTION_RETRY.equals(action)
                || ACTION_STOP.equals(action)
                || ACTION_BATCH_DELETE_MODEL.equals(action);
    }

    /**
     * 判断是否为订阅类 action（listen_status/events/listen_stop）。
     */
    private boolean isSubscriptionAction(String action) {
        return ACTION_LISTEN_STATUS.equals(action)
                || ACTION_EVENTS.equals(action)
                || ACTION_LISTEN_STOP.equals(action);
    }

    /**
     * 构建人工确认提示消息。
     */
    private String buildConfirmMessage(String action, Map<String, Object> args) {
        if (ACTION_CREATE_RECONSTRUCTION.equals(action)) {
            String type = getStringArg(args, PARAM_RECONSTRUCTION_TYPE, "?");
            List<String> mediaIds = parseStringList(args, PARAM_MEDIA_IDS);
            int count = mediaIds != null ? mediaIds.size() : 0;
            return String.format("即将创建 %s 类型三维重建任务，包含 %d 个媒体文件夹，确认？", type, count);
        }
        if (ACTION_CREATE_PANORAMA.equals(action)) {
            String name = getStringArg(args, PARAM_NAME, "?");
            List<String> mediaIds = parseStringList(args, PARAM_MEDIA_IDS);
            int count = mediaIds != null ? mediaIds.size() : 0;
            return String.format("即将创建全景任务「%s」，包含 %d 个媒体文件夹，确认？", name, count);
        }
        if (ACTION_BATCH_DELETE_MODEL.equals(action)) {
            List<Integer> fileIds = parseFileIdsList(args);
            int count = fileIds != null ? fileIds.size() : 0;
            return String.format("即将批量删除 %d 个模型文件，确认？", count);
        }
        return "确认执行三维重建操作？";
    }
}
