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
import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthubsdk.bean.TerraBuildCreatePanoramaResult;
import com.dji.flighthubsdk.bean.TerraBuildCreateReconstructionParams;
import com.dji.flighthubsdk.bean.TerraBuildCreateReconstructionResult;
import com.dji.flighthubsdk.bean.TerraBuildModelFileListResult;
import com.dji.flighthubsdk.bean.TerraBuildModelFileUrlResult;
import com.dji.flighthubsdk.bean.TerraBuildQualityReportResult;
import com.dji.flighthubsdk.bean.TerraBuildSettingsResult;
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

/**
 * fh_terra — 三维重建与成果管理。
 * 基于飞行影像生成 2D 正射/3D 模型/全景/点云，查询重建进度和模型质量报告。
 *
 * <p>安全等级：混合
 * <ul>
 *   <li>create_reconstruction / create_panorama — L1（需人工确认）</li>
 *   <li>status / model_list / model_url / quality_report — L0（只读查询）</li>
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

    // ── 操作常量 ──

    private static final String ACTION_CREATE_RECONSTRUCTION = "create_reconstruction";
    private static final String ACTION_CREATE_PANORAMA = "create_panorama";
    private static final String ACTION_STATUS = "status";
    private static final String ACTION_MODEL_LIST = "model_list";
    private static final String ACTION_MODEL_URL = "model_url";
    private static final String ACTION_QUALITY_REPORT = "quality_report";
    private static final String ACTION_DELETE_MODEL = "delete_model";
    private static final String ACTION_RETRY = "retry";

    /** 有效操作列表 */
    private static final List<String> VALID_ACTIONS = List.of(
            ACTION_CREATE_RECONSTRUCTION, ACTION_CREATE_PANORAMA,
            ACTION_STATUS, ACTION_MODEL_LIST, ACTION_MODEL_URL, ACTION_QUALITY_REPORT,
            ACTION_DELETE_MODEL, ACTION_RETRY
    );

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

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhTerraTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
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
    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description",
                "3D reconstruction and model management: create ortho/3D/point-cloud reconstruction "
                + "or panorama from flight imagery, check reconstruction status, list model files, "
                + "get model URLs, retrieve quality reports, delete models, and retry failed tasks.");

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
                        + "retry: retry a failed reconstruction task"
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
                "description", "Required for create_reconstruction/create_panorama actions"
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

        // 4. create_* 操作需要安全等级 L1 + 人工确认
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

        // 5. 分发执行
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

    // ── 辅助方法 ──

    /**
     * 判断是否为写操作（需要人工确认）。
     */
    private boolean isCreateAction(String action) {
        return ACTION_CREATE_RECONSTRUCTION.equals(action)
                || ACTION_CREATE_PANORAMA.equals(action)
                || ACTION_DELETE_MODEL.equals(action)
                || ACTION_RETRY.equals(action);
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
        return "确认执行三维重建操作？";
    }
}
