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

package com.dji.flighthub.mcp.tools.workspace;

import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.tools.subscription.ListenChannelSupport;
import com.dji.flighthub.mcp.tools.subscription.SubscriptionBuffer;
import com.dji.flighthubsdk.bean.BatchDownloadWaylinesItemResult;
import com.dji.flighthubsdk.bean.BatchDownloadWaylinesParam;
import com.dji.flighthubsdk.bean.ConcatWaylineParam;
import com.dji.flighthubsdk.bean.ConcatWaylineResult;
import com.dji.flighthubsdk.bean.DuplicateWaylineParam;
import com.dji.flighthubsdk.bean.DuplicateWaylineResult;
import com.dji.flighthubsdk.bean.GetWaylineDetailResult;
import com.dji.flighthubsdk.bean.GetWaylineListParam;
import com.dji.flighthubsdk.bean.GetWaylineListResult;
import com.dji.flighthubsdk.bean.ImportKmzItem;
import com.dji.flighthubsdk.bean.ImportKmzResult;
import com.dji.flighthubsdk.bean.ImportWaylineEventKmzData;
import com.dji.flighthubsdk.bean.ImportWaylineEventResult;
import com.dji.flighthubsdk.bean.SubmitWaylineInfoParam;
import com.dji.flighthubsdk.bean.WaylineDetailExtData;
import com.dji.flighthubsdk.bean.WaylineDetailKmzData;
import com.dji.flighthubsdk.bean.WaylineDetailSurveyAreaScope;
import com.dji.flighthubsdk.bean.WaylineDetailSurveyAreaScopePoint;
import com.dji.flighthubsdk.bean.WaylineListItemResult;
import com.dji.flighthubsdk.core.Workspace;
import com.dji.flighthubsdk.core.WaylineManageModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * fh_workspace — 航线文件（Wayline）管理工具。
 * 支持航线列表查询、详情查看、上传、更新、复制、批量删除、批量下载、拼接，
 * 以及订阅本地 KMZ 导入（submit）产生的异步事件（listen_import/events/listen_stop）。
 *
 * <p>安全等级：混合等级工具，工具级基线 L0（READ_ONLY）；写操作（submit/update/duplicate/
 * batch_delete/concat）由 {@link #ACTION_SECURITY_LEVELS} 抬高至 L1，门控只抬高不降低。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhWorkspaceTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhWorkspaceTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_workspace";

    // ==================== 参数常量 ====================
    private static final String PARAM_ACTION = "action";
    private static final String PARAM_WAYLINE_UUID = "wayline_uuid";
    private static final String PARAM_WAYLINE_UUIDS = "wayline_uuids";
    private static final String PARAM_WAYLINE_NAME = "wayline_name";
    private static final String PARAM_FILE_PATH = "file_path";
    private static final String PARAM_FILE_PATHS = "file_paths";
    private static final String PARAM_TARGET_WAYLINE_NAME = "target_wayline_name";
    private static final String PARAM_TARGET_OBJECT_KEY_PREFIX = "target_object_key_prefix";
    private static final String PARAM_DRONE_TYPE = "drone_type";
    private static final String PARAM_PAYLOAD_TYPE = "payload_type";
    private static final String PARAM_DRONE_TYPES = "drone_types";
    private static final String PARAM_PAYLOAD_TYPES = "payload_types";
    private static final String PARAM_DATE_START = "date_start";
    private static final String PARAM_DATE_END = "date_end";
    private static final String PARAM_PAGE = "page";
    private static final String PARAM_PAGE_SIZE = "page_size";
    private static final String PARAM_CONFIRM = "confirm";
    private static final String PARAM_KMZ_OBJECT_KEY = "kmz_object_key";
    private static final String PARAM_SUB_WAYLINE_UUIDS = "sub_wayline_uuids";
    private static final String PARAM_EDITOR_WARNINGS = "editor_warnings";
    private static final String PARAM_CHANNELS = "channels";
    private static final String PARAM_SINCE_SEQ = "since_seq";
    private static final String PARAM_LIMIT = "limit";

    // ==================== Action 常量 ====================
    private static final String ACTION_LIST = "list";
    private static final String ACTION_DETAIL = "detail";
    private static final String ACTION_SUBMIT = "submit";
    private static final String ACTION_UPDATE = "update";
    private static final String ACTION_DUPLICATE = "duplicate";
    private static final String ACTION_BATCH_DELETE = "batch_delete";
    private static final String ACTION_BATCH_DOWNLOAD = "batch_download";
    private static final String ACTION_CONCAT = "concat";
    private static final String ACTION_LISTEN_IMPORT = "listen_import";
    private static final String ACTION_EVENTS = "events";
    private static final String ACTION_LISTEN_STOP = "listen_stop";

    /** 有效 action 列表 */
    private static final List<String> VALID_ACTIONS = List.of(
            ACTION_LIST, ACTION_DETAIL, ACTION_SUBMIT, ACTION_UPDATE,
            ACTION_DUPLICATE, ACTION_BATCH_DELETE, ACTION_BATCH_DOWNLOAD, ACTION_CONCAT,
            ACTION_LISTEN_IMPORT, ACTION_EVENTS, ACTION_LISTEN_STOP
    );

    /** 各 action 要求的安全等级（未列出的 action 为 L0，不需要拦截） */
    private static final Map<String, SecurityLevel> ACTION_SECURITY_LEVELS = Map.of(
            ACTION_SUBMIT, SecurityLevel.REVERSIBLE,
            ACTION_UPDATE, SecurityLevel.REVERSIBLE,
            ACTION_DUPLICATE, SecurityLevel.REVERSIBLE,
            ACTION_BATCH_DELETE, SecurityLevel.REVERSIBLE,
            ACTION_CONCAT, SecurityLevel.REVERSIBLE
    );

    // ==================== 订阅 channel 常量 ====================

    /** 唯一订阅 channel：航线导入（submit）异步事件 */
    private static final String CHANNEL_IMPORT_WAYLINE_EVENT = "import_wayline_event";

    /**
     * 订阅注册表命名空间。
     * 不直接使用工具名 "workspace"（风险：其他工具也以 workspace 命名作用域，可能撞 key），
     * 改用 SDK 模块名 "wayline_manage" 作为命名空间前缀，与 WaylineManageModule 对应。
     */
    private static final String SUBSCRIBE_NAMESPACE = "wayline_manage";

    /** 订阅作用域标识：本工具的导入事件订阅是 workspace 级，不区分设备 */
    private static final String SUBSCRIBE_SCOPE_ID = "workspace";

    /** events 动作默认读取条数 */
    private static final int DEFAULT_EVENTS_LIMIT = 50;

    /** 默认分页 */
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;

    /** 分页上限 */
    private static final int MAX_PAGE_SIZE = 100;

    /**
     * 导入结果事件等待超时（秒）。
     * 服务端需要异步解析 KMZ，耗时明显长于普通查询回调，故单独放宽。
     */
    private static final int IMPORT_EVENT_TIMEOUT_SECONDS = 30;

    /** 单次批量提交的文件数上限：每个文件最长等待一次导入事件（30s），需控制总阻塞时长 */
    private static final int MAX_SUBMIT_FILES = 5;

    /** 导入成功的事件码 */
    private static final int IMPORT_CODE_SUCCESS = 0;

    /** 本地 KMZ 上传超时（秒）：importKmz 需先把文件推送到对象存储，比普通查询慢 */
    private static final int IMPORT_UPLOAD_TIMEOUT_SECONDS = 60;

    /** 同步导入模式下按名称反查 UUID 的列表页大小 */
    private static final int UUID_LOOKUP_PAGE_SIZE = 20;

    /** 同步导入模式下按名称反查 UUID 的最大尝试次数（平台落库可能滞后于 importKmz 返回） */
    private static final int UUID_LOOKUP_MAX_ATTEMPTS = 3;

    /** 反查重试间隔（毫秒） */
    private static final long UUID_LOOKUP_INTERVAL_MS = 800L;

    /** 监听注册失败的 listenId 约定值 */
    private static final int INVALID_LISTEN_ID = -1;

    /** 毫秒与秒的换算 */
    private static final long MILLIS_PER_SECOND = 1000L;

    /**
     * 允许上传的 KMZ 目录环境变量名（覆盖默认目录，便于本地/容器化部署与测试）。
     * 本地路径上传只会接受该目录（含子目录）内的文件，防止任意文件读取。
     */
    private static final String ENV_WAYLINE_UPLOAD_DIR = "FH_WAYLINE_UPLOAD_DIR";

    /**
     * 允许上传的 KMZ 目录默认值。
     *
     * <p>取 {@code /tmp}：KMZ 通常由客户端侧 skill 现场生成到临时目录，
     * 而 MCP 默认以 stdio 传输运行在客户端同一台机器上，可直接读取该文件。
     * 容器化部署（文件在容器外不可见）应通过 {@code FH_WAYLINE_UPLOAD_DIR}
     * 指向挂载目录，否则本地路径上传会因路径不在允许目录内被拒绝。
     */
    private static final String DEFAULT_WAYLINE_UPLOAD_DIR = "/tmp";

    /** 允许上传的 KMZ 目录（构造时从环境变量读取，运行期间不变） */
    private final String waylineUploadDir;

    // ==================== submit 状态枚举（真实/mock 路径必须一致） ====================
    /** 服务端已受理，等待导入事件 */
    private static final String STATUS_SUBMITTED = "submitted";
    /** 导入成功，wayline_uuid 可用 */
    private static final String STATUS_IMPORTED = "imported";
    /** 导入事件未在超时内到达，需用 list 复查 */
    private static final String STATUS_TIMEOUT = "timeout";
    /** 服务端明确拒绝导入（KMZ 非法等） */
    private static final String STATUS_IMPORT_FAILED = "import_failed";
    /** 提交请求本身失败 */
    private static final String STATUS_FAILED = "failed";
    /** 批量结果：部分成功 */
    private static final String STATUS_PARTIALLY_SUBMITTED = "partially_submitted";

    private final SdkService sdkService;
    private final ListenChannelSupport listenSupport;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     * @param listenSupport  订阅 channel 通用支持组件
     */
    @Autowired
    public FhWorkspaceTool(SessionManager sessionManager, SdkService sdkService,
                            ListenChannelSupport listenSupport) {
        super(sessionManager);
        this.sdkService = sdkService;
        this.listenSupport = listenSupport;
        this.waylineUploadDir = System.getenv()
                .getOrDefault(ENV_WAYLINE_UPLOAD_DIR, DEFAULT_WAYLINE_UPLOAD_DIR);
    }

    /**
     * 测试专用构造函数：直接指定允许上传目录，避免依赖环境变量与真实文件系统布局。
     *
     * @param sessionManager   session 管理器
     * @param sdkService       SDK 服务
     * @param listenSupport    订阅 channel 通用支持组件
     * @param waylineUploadDir 允许上传的 KMZ 目录
     */
    FhWorkspaceTool(SessionManager sessionManager, SdkService sdkService,
                    ListenChannelSupport listenSupport, String waylineUploadDir) {
        super(sessionManager);
        this.sdkService = sdkService;
        this.listenSupport = listenSupport;
        this.waylineUploadDir = waylineUploadDir;
    }

    /**
     * 声明工具级静态安全等级：READ_ONLY（混合等级工具的门控基线，写操作由
     * {@link #ACTION_SECURITY_LEVELS} 抬升至 L1，只读 action 不被误拦）。
     *
     * @return 静态安全等级
     */
    @Override
    public SecurityLevel getSecurityLevel() {
        return SecurityLevel.READ_ONLY;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description",
                "Manage wayline files in the workspace library. "
                + "Actions: list, detail, submit, update, duplicate, batch_delete, batch_download, concat, "
                + "listen_import, events, listen_stop. "
                + "WHEN TO USE (action=submit): You have a LOCAL KMZ file to upload to workspace. "
                + "AFTER submit: use returned wayline_uuid with fh_flight_task(action=create). "
                + "WHEN TO USE (action=list): Browse all workspace waylines (returns all, no device/distance filter). "
                + "DO NOT USE (action=list) WHEN: user has a specific dock and wants flyable/reachable waylines "
                + "→ use fh_wayline_recommend instead (it filters by drone model + distance; list returns all "
                + "waylines regardless of dock, which may recommend a faraway wayline the dock cannot reach). "
                + "WHEN TO USE (action=concat): merge an ORDERED list of existing sub-wayline UUIDs into a single "
                + "new wayline (e.g. combining several short survey legs into one continuous mission). "
                + "AFTER concat: use returned wayline_uuid with fh_flight_task(action=create). "
                + "WHEN TO USE (action=listen_import -> events -> listen_stop): submit() triggers an "
                + "asynchronous server-side KMZ import; subscribe with listen_import right after (or before) "
                + "calling submit to track the resulting import_wayline_event stream, poll progress with "
                + "events (cursor-based, since_seq/limit), and release the subscription with listen_stop "
                + "once the import result (code/message/wayline_uuid) has been observed.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_ACTION, Map.of(
                "type", "string",
                "enum", VALID_ACTIONS,
                "description", "Action to perform: list, detail, submit, update, duplicate, batch_delete, "
                        + "batch_download, concat | "
                        + "listen_import: subscribe to the workspace-level import_wayline_event channel "
                        + "(fires after a submit) | "
                        + "events: poll buffered events for the import_wayline_event channel since a cursor | "
                        + "listen_stop: unsubscribe the import_wayline_event channel"
        ));
        props.put(PARAM_WAYLINE_UUID, Map.of(
                "type", "string",
                "description", "Wayline UUID (required for detail, update, duplicate)"
        ));
        props.put(PARAM_WAYLINE_UUIDS, Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", "List of wayline UUIDs (required for batch_delete, batch_download)"
        ));
        props.put(PARAM_WAYLINE_NAME, Map.of(
                "type", "string",
                "description", "Wayline name (required for submit, update)"
        ));
        props.put(PARAM_FILE_PATH, Map.of(
                "type", "string",
                "description", "One wayline KMZ file (required for submit unless file_paths is given). "
                        + "Two forms are accepted: (1) an absolute local path starting with '/' "
                        + "(e.g. /tmp/wayline-ab12cd34.kmz), which is uploaded via importKmz — the path "
                        + "must be inside " + DEFAULT_WAYLINE_UPLOAD_DIR + " or FH_WAYLINE_UPLOAD_DIR; "
                        + "(2) an object-storage key for an already-uploaded KMZ "
                        + "(e.g. waylines/abc.kmz), which is submitted as-is."
        ));
        props.put(PARAM_FILE_PATHS, Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", "Multiple wayline KMZ files (optional for submit, alternative to file_path). "
                        + "Each element accepts the same two forms as file_path (absolute local path or "
                        + "object-storage key). Files are submitted sequentially; each result is "
                        + "reported separately."
        ));
        props.put(PARAM_TARGET_WAYLINE_NAME, Map.of(
                "type", "string",
                "description", "Name for the duplicated wayline (optional for duplicate, "
                        + "defaults to the server-side naming rule)"
        ));
        props.put(PARAM_TARGET_OBJECT_KEY_PREFIX, Map.of(
                "type", "string",
                "description", "Object key prefix for the duplicated KMZ (optional for duplicate)"
        ));
        props.put(PARAM_DRONE_TYPE, Map.of(
                "type", "string",
                "description", "Drone type filter for list"
        ));
        props.put(PARAM_PAYLOAD_TYPE, Map.of(
                "type", "string",
                "description", "Payload type filter for list"
        ));
        props.put(PARAM_DRONE_TYPES, Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", "Drone types filter for list (multi-select)"
        ));
        props.put(PARAM_PAYLOAD_TYPES, Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", "Payload types filter for list (multi-select)"
        ));
        props.put(PARAM_DATE_START, Map.of(
                "type", "string",
                "description", "Start date filter for list (ISO format)"
        ));
        props.put(PARAM_DATE_END, Map.of(
                "type", "string",
                "description", "End date filter for list (ISO format)"
        ));
        props.put(PARAM_PAGE, Map.of(
                "type", "integer",
                "description", "Page number (default: 1)"
        ));
        props.put(PARAM_PAGE_SIZE, Map.of(
                "type", "integer",
                "description", "Page size (default: 20)"
        ));
        props.put(PARAM_CONFIRM, Map.of(
                "type", "boolean",
                "description", "Confirmation flag for destructive actions (batch_delete)"
        ));
        props.put(PARAM_TARGET_WAYLINE_NAME, Map.of(
                "type", "string",
                "description", "Name for the resulting concatenated wayline (required for concat)"
        ));
        props.put(PARAM_KMZ_OBJECT_KEY, Map.of(
                "type", "string",
                "description", "Object key of the base/template KMZ file used to build the concatenated "
                        + "wayline (required for concat)"
        ));
        props.put(PARAM_SUB_WAYLINE_UUIDS, Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", "Ordered list of sub-wayline UUIDs to concatenate into the target wayline, "
                        + "in flight order (required for concat, at least one)"
        ));
        props.put(PARAM_EDITOR_WARNINGS, Map.of(
                "type", "object",
                "additionalProperties", Map.of("type", "string"),
                "description", "Optional map of sub-wayline UUID to editor warning message, "
                        + "surfaced back to the caller after concatenation (optional for concat)"
        ));
        props.put(PARAM_CHANNELS, Map.of(
                "type", "array",
                "items", Map.of("type", "string", "enum", List.of(CHANNEL_IMPORT_WAYLINE_EVENT)),
                "description", "Channel names for events/listen_stop (optional; defaults to all "
                        + "currently active channels when omitted; the only supported channel is '"
                        + CHANNEL_IMPORT_WAYLINE_EVENT + "')"
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

    @Override
    public Map<String, Object> execute(Map<String, Object> args) {
        // 1. 校验 session
        SessionContext session = requireSession();
        if (session == null) {
            return noSessionError();
        }

        // 2. 校验 action
        String action = getStringArg(args, PARAM_ACTION, null);
        if (action == null || !VALID_ACTIONS.contains(action)) {
            return errorResult("Invalid action: " + action,
                    "action must be one of: " + VALID_ACTIONS);
        }

        // 3. 统一安全等级拦截：submit/update/duplicate/batch_delete/concat=L1
        SecurityLevel requiredLevel = ACTION_SECURITY_LEVELS.get(action);
        if (requiredLevel != null) {
            Map<String, Object> denied = enforceSecurityLevel(requiredLevel);
            if (denied != null) {
                return denied;
            }
        }

        // 4. 订阅类 action（只读，无需确认）单独分发，不进入下方写操作分支
        if (isSubscriptionAction(action)) {
            return dispatchSubscription(session, action, args);
        }

        // 5. 分发到对应操作
        return switch (action) {
            case ACTION_LIST -> executeList(args);
            case ACTION_DETAIL -> executeDetail(args);
            case ACTION_SUBMIT -> executeSubmit(args);
            case ACTION_UPDATE -> executeUpdate(args);
            case ACTION_DUPLICATE -> executeDuplicate(args);
            case ACTION_BATCH_DELETE -> executeBatchDelete(args);
            case ACTION_BATCH_DOWNLOAD -> executeBatchDownload(args);
            case ACTION_CONCAT -> executeConcat(args);
            default -> errorResult("Unhandled action: " + action, "Report this bug.");
        };
    }

    /**
     * 列出航线文件（支持可选过滤和分页）。
     */
    private Map<String, Object> executeList(Map<String, Object> args) {
        int page = getIntArg(args, PARAM_PAGE, DEFAULT_PAGE);
        int pageSize = getIntArg(args, PARAM_PAGE_SIZE, DEFAULT_PAGE_SIZE);
        if (page < 1) {
            page = DEFAULT_PAGE;
        }
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            pageSize = DEFAULT_PAGE_SIZE;
        }

        // Mock 模式
        if (sdkService.isMockMode() || sdkService.getWorkspace() == null) {
            long now = System.currentTimeMillis() / 1000;
            List<Map<String, Object>> waylines = List.of(
                    Map.of(
                            "uuid", "wl_mock_001",
                            "name", "Survey Route A",
                            "drone_type", "M350 RTK",
                            "payload_type", "Zenmuse L2",
                            "created_at", now - 86400,
                            "updated_at", now - 3600
                    ),
                    Map.of(
                            "uuid", "wl_mock_002",
                            "name", "Inspection Path B",
                            "drone_type", "Matrice 30T",
                            "payload_type", "Zenmuse H20T",
                            "created_at", now - 172800,
                            "updated_at", now - 7200
                    )
            );

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("page", page);
            data.put("page_size", pageSize);
            data.put("total", 2);
            data.put("waylines", waylines);
            return successResult(data, "Found 2 wayline(s)");
        }

        // 真实 SDK 调用
        return executeRealList(args, page, pageSize);
    }

    /**
     * 查询航线详情。
     */
    private Map<String, Object> executeDetail(Map<String, Object> args) {
        String waylineUuid = getStringArg(args, PARAM_WAYLINE_UUID, null);
        if (waylineUuid == null) {
            return errorResult("Missing required parameter: wayline_uuid",
                    "Provide wayline_uuid for detail action.");
        }

        // Mock 模式
        if (sdkService.isMockMode() || sdkService.getWorkspace() == null) {
            long now = System.currentTimeMillis() / 1000;
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("uuid", waylineUuid);
            data.put("name", "Survey Route A");
            data.put("md5", "a1b2c3d4e5f6789012345678abcdef01");
            data.put("kmz_key", "waylines/" + waylineUuid + "/route.kmz");
            data.put("download_url", "https://flighthub.example.com/waylines/" + waylineUuid + "/download");
            data.put("created_at", now - 86400);
            data.put("updated_at", now - 3600);

            // 与真实路径保持同一结构：kmz_data / ext_data 为结构化对象而非字符串
            Map<String, Object> kmzData = new LinkedHashMap<>();
            kmzData.put("wayline_type", 0);
            kmzData.put("wayline_point_count", 42);
            kmzData.put("drone_type", "M350 RTK");
            kmzData.put("payload_types", List.of("Zenmuse L2"));
            kmzData.put("distance", 1532.5);
            kmzData.put("duration", 420.0);
            kmzData.put("wayline_validity_check", true);
            kmzData.put("wayline_validity_check_codes", List.of());
            data.put("kmz_data", kmzData);

            Map<String, Object> extData = new LinkedHashMap<>();
            extData.put("content_length", 1048576L);
            extData.put("model_file_object_keys", List.of());
            extData.put("elevation_file_object_keys", List.of());
            data.put("ext_data", extData);

            return successResult(data, "Wayline detail for " + waylineUuid);
        }

        // 真实 SDK 调用
        return executeRealDetail(waylineUuid);
    }

    /**
     * 上传航线文件（支持单文件 file_path 与多文件 file_paths）。
     *
     * <p>每个 file 支持两种形式：绝对路径（本地 KMZ，经 importKmz 上传）与对象存储 key
     * （已上传的 KMZ，直接登记）。两者可混用。
     */
    private Map<String, Object> executeSubmit(Map<String, Object> args) {
        String waylineName = getStringArg(args, PARAM_WAYLINE_NAME, null);
        if (waylineName == null) {
            return errorResult("Missing required parameter: wayline_name",
                    "Provide wayline_name for the uploaded wayline.");
        }

        List<String> fileRefs = collectSubmitFileRefs(args);
        if (fileRefs == null) {
            return errorResult("Missing required parameter: file_path or file_paths",
                    "Provide file_path (single) or file_paths (batch) with the KMZ path or object key.");
        }
        if (fileRefs.isEmpty()) {
            return errorResult("Empty file_paths",
                    "file_paths must contain at least one KMZ file.");
        }
        if (fileRefs.size() > MAX_SUBMIT_FILES) {
            return errorResult("Too many files: " + fileRefs.size(),
                    "Submit at most " + MAX_SUBMIT_FILES + " files per call; split into multiple calls.");
        }

        // 本地路径必须先通过允许目录校验再进入任何真实/mock 分支，避免任意文件读取
        String pathError = validateLocalPaths(fileRefs);
        if (pathError != null) {
            return errorResult(pathError,
                    "Local KMZ paths must be inside " + waylineUploadDir
                            + " (override with " + ENV_WAYLINE_UPLOAD_DIR + ").");
        }

        // Mock 模式
        if (sdkService.isMockMode() || sdkService.getWorkspace() == null) {
            return submitInMockMode(waylineName, fileRefs);
        }

        // 真实 SDK 调用
        return executeRealSubmit(waylineName, fileRefs);
    }

    /**
     * 收集待提交的 KMZ 文件引用（本地绝对路径或对象存储 key）。
     * 优先读取 file_paths（批量），其次回退到 file_path（单个）；两者都存在时合并去重。
     *
     * @param args 工具参数
     * @return 去重后的文件引用列表；两者都缺失时返回 null
     */
    private List<String> collectSubmitFileRefs(Map<String, Object> args) {
        List<String> filePaths = getStringListArg(args, PARAM_FILE_PATHS);
        String filePath = getStringArg(args, PARAM_FILE_PATH, null);
        if (filePaths == null && filePath == null) {
            return null;
        }

        // LinkedHashSet 去重并保持顺序，避免同一文件被重复提交
        Set<String> keys = new LinkedHashSet<>();
        if (filePaths != null) {
            keys.addAll(filePaths);
        }
        if (filePath != null) {
            keys.add(filePath);
        }
        return new ArrayList<>(keys);
    }

    /**
     * 判断文件引用是否为本地绝对路径。
     *
     * <p>对象存储 key 不会以 {@code /} 开头，以此区分两类通道。
     *
     * @param fileRef 文件引用
     * @return 绝对路径返回 true
     */
    private boolean isLocalFilePath(String fileRef) {
        return fileRef != null && fileRef.startsWith("/");
    }

    /**
     * 校验所有本地路径均位于允许上传目录内。
     *
     * @param fileRefs 文件引用列表
     * @return 越界时返回错误描述，全部合法返回 null
     */
    private String validateLocalPaths(List<String> fileRefs) {
        Path allowedDir = Paths.get(waylineUploadDir).toAbsolutePath().normalize();
        for (String fileRef : fileRefs) {
            if (!isLocalFilePath(fileRef)) {
                continue;
            }
            if (!isWithinAllowedDir(fileRef, allowedDir)) {
                return "file_path must be within " + waylineUploadDir + " directory: " + fileRef;
            }
        }
        return null;
    }

    /**
     * 判断本地路径是否落在允许目录内。
     *
     * <p>路径规范化校验：防止路径穿越（{@code ../../etc/passwd}）与符号链接逃逸。
     * {@link Path#toRealPath()} 会解析符号链接，比 {@code normalize()} 更严格；文件尚不存在时
     * 回退到词法归一。macOS 下 {@code /tmp} 是指向 {@code /private/tmp} 的符号链接，
     * 故允许目录本身也按真实路径再比一次。
     *
     * @param filePath   原始文件路径
     * @param allowedDir 允许上传的目录（已归一化的绝对路径）
     * @return 在允许目录内返回 true
     */
    private boolean isWithinAllowedDir(String filePath, Path allowedDir) {
        Path resolved = resolveRealOrNormalized(filePath);
        if (resolved.startsWith(allowedDir)) {
            return true;
        }
        Path realAllowedDir = resolveRealOrNormalized(allowedDir.toString());
        return !realAllowedDir.equals(allowedDir) && resolved.startsWith(realAllowedDir);
    }

    /**
     * 解析为真实路径，无法解析（文件不存在）时回退到词法归一后的绝对路径。
     *
     * @param path 待解析路径
     * @return 真实路径或归一化绝对路径
     */
    private Path resolveRealOrNormalized(String path) {
        Path raw = Paths.get(path);
        try {
            return raw.toRealPath();
        } catch (IOException e) {
            return raw.toAbsolutePath().normalize();
        }
    }

    /**
     * Mock 模式下的提交响应：为每个文件生成一条结果，结构与真实路径保持一致。
     *
     * @param waylineName 航线名称
     * @param fileRefs    待提交的文件引用列表（本地路径或对象 key）
     * @return 成功响应
     */
    private Map<String, Object> submitInMockMode(String waylineName, List<String> fileRefs) {
        long now = System.currentTimeMillis();
        List<Map<String, Object>> results = new ArrayList<>();
        String firstWaylineUuid = null;
        for (int i = 0; i < fileRefs.size(); i++) {
            String waylineUuid = "wl_" + (now + i);
            if (firstWaylineUuid == null) {
                firstWaylineUuid = waylineUuid;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("file_path", fileRefs.get(i));
            // 本地路径在真实链路会先换取对象 key，mock 下给一个结构等价的占位值
            if (isLocalFilePath(fileRefs.get(i))) {
                item.put("kmz_object_key", "waylines/mock_" + (now + i) + ".kmz");
            }
            item.put("async_request_id", UUID.randomUUID().toString());
            item.put("wayline_uuid", waylineUuid);
            // mock 下导入瞬时完成、wayline_uuid 已可用，等价于真实路径收到导入成功事件
            item.put("status", STATUS_IMPORTED);
            results.add(item);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("request_id", "req_" + now);
        data.put("wayline_uuid", firstWaylineUuid);
        // 顶层状态与真实路径对齐：只反映提交结果，不反映单个文件的导入结果
        data.put("status", STATUS_SUBMITTED);
        data.put("total_count", results.size());
        data.put("submitted_count", results.size());
        data.put("failed_count", 0);
        data.put("results", results);

        return successResultWithNextSteps(data,
                "Wayline '" + waylineName + "' submitted successfully.",
                buildCreateTaskNextSteps(firstWaylineUuid));
    }

    /**
     * 构建"用该航线创建飞行任务"的下一步引导。
     *
     * @param waylineUuid 航线 UUID，可能为 null（导入事件超时时）
     * @return next_steps 列表
     */
    private List<Map<String, Object>> buildCreateTaskNextSteps(String waylineUuid) {
        String uuidHint = waylineUuid != null ? waylineUuid : "<QUERY_VIA_list>";
        List<Map<String, Object>> nextSteps = new ArrayList<>(2);
        nextSteps.add(Map.of(
                "action", "Create a flight task using this wayline",
                "tool", "fh_flight_task",
                "params_hint", Map.of(
                        "action", "create",
                        "wayline_uuid", uuidHint,
                        "sn", "<DOCK_SN>",
                        "task_type", 1,
                        "rth_altitude", 120.0)));
        if (waylineUuid == null) {
            nextSteps.add(Map.of(
                    "action", "List waylines to look up the wayline_uuid",
                    "tool", "fh_workspace",
                    "params_hint", Map.of("action", "list")));
        }
        return nextSteps;
    }

    /**
     * 更新航线信息。
     */
    private Map<String, Object> executeUpdate(Map<String, Object> args) {
        String waylineUuid = getStringArg(args, PARAM_WAYLINE_UUID, null);
        if (waylineUuid == null) {
            return errorResult("Missing required parameter: wayline_uuid",
                    "Provide wayline_uuid for the wayline to update.");
        }

        String waylineName = getStringArg(args, PARAM_WAYLINE_NAME, null);
        if (waylineName == null) {
            return errorResult("Missing required parameter: wayline_name",
                    "Provide the new wayline_name.");
        }

        // Mock 模式
        if (sdkService.isMockMode() || sdkService.getWorkspace() == null) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("uuid", waylineUuid);
            data.put("name", waylineName);
            data.put("status", "updated");
            return successResult(data, "Wayline " + waylineUuid + " updated");
        }

        // 真实 SDK 调用
        return executeRealUpdate(waylineUuid, waylineName);
    }

    /**
     * 复制航线。
     */
    private Map<String, Object> executeDuplicate(Map<String, Object> args) {
        String waylineUuid = getStringArg(args, PARAM_WAYLINE_UUID, null);
        if (waylineUuid == null) {
            return errorResult("Missing required parameter: wayline_uuid",
                    "Provide wayline_uuid of the wayline to duplicate.");
        }

        String targetWaylineName = getStringArg(args, PARAM_TARGET_WAYLINE_NAME, null);
        String targetObjectKeyPrefix = getStringArg(args, PARAM_TARGET_OBJECT_KEY_PREFIX, null);

        // Mock 模式
        if (sdkService.isMockMode() || sdkService.getWorkspace() == null) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("source_uuid", waylineUuid);
            data.put("new_uuid", "wl_dup_" + System.currentTimeMillis());
            data.put("new_name", targetWaylineName);
            data.put("target_object_key_prefix", targetObjectKeyPrefix);
            data.put("status", "duplicated");
            return successResult(data, "Wayline " + waylineUuid + " duplicated");
        }

        // 真实 SDK 调用
        return executeRealDuplicate(waylineUuid, targetWaylineName, targetObjectKeyPrefix);
    }

    /**
     * 批量删除航线（不可逆，需确认）。
     */
    private Map<String, Object> executeBatchDelete(Map<String, Object> args) {
        List<String> uuids = getStringListArg(args, PARAM_WAYLINE_UUIDS);
        if (uuids == null || uuids.isEmpty()) {
            return errorResult("Missing required parameter: wayline_uuids",
                    "Provide a list of wayline UUIDs to delete.");
        }

        // 安全等级 L1 已在 execute() 内统一拦截，这里只做确认校验
        Map<String, Object> confirmResult = requireHumanConfirm(args, "batch_delete",
                uuids.size() + " wayline(s)",
                "Permanently delete " + uuids.size() + " wayline(s). This action cannot be undone.");
        if (confirmResult != null) {
            return confirmResult;
        }

        // Mock 模式
        if (sdkService.isMockMode() || sdkService.getWorkspace() == null) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("deleted_count", uuids.size());
            data.put("deleted_uuids", uuids);
            return successResult(data, "Deleted " + uuids.size() + " wayline(s)");
        }

        // 真实 SDK 调用
        return executeRealBatchDelete(uuids);
    }

    /**
     * 批量下载航线。
     */
    private Map<String, Object> executeBatchDownload(Map<String, Object> args) {
        List<String> uuids = getStringListArg(args, PARAM_WAYLINE_UUIDS);
        if (uuids == null || uuids.isEmpty()) {
            return errorResult("Missing required parameter: wayline_uuids",
                    "Provide a list of wayline UUIDs to download.");
        }

        // Mock 模式
        if (sdkService.isMockMode() || sdkService.getWorkspace() == null) {
            List<Map<String, Object>> downloads = new ArrayList<>();
            for (String uuid : uuids) {
                downloads.add(Map.of(
                        "uuid", uuid,
                        "download_url", "https://flighthub.example.com/waylines/" + uuid + "/download"
                ));
            }

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("count", uuids.size());
            data.put("downloads", downloads);
            return successResult(data, "Download URLs generated for " + uuids.size() + " wayline(s)");
        }

        // 真实 SDK 调用
        return executeRealBatchDownload(uuids);
    }

    /**
     * 拼接多个子航线为一个新航线。
     */
    private Map<String, Object> executeConcat(Map<String, Object> args) {
        String targetWaylineName = getStringArg(args, PARAM_TARGET_WAYLINE_NAME, null);
        if (targetWaylineName == null) {
            return errorResult("Missing required parameter: target_wayline_name",
                    "Provide target_wayline_name for the concatenated wayline.");
        }

        String kmzObjectKey = getStringArg(args, PARAM_KMZ_OBJECT_KEY, null);
        if (kmzObjectKey == null) {
            return errorResult("Missing required parameter: kmz_object_key",
                    "Provide kmz_object_key of the base KMZ file.");
        }

        List<String> subWaylineUuids = getStringListArg(args, PARAM_SUB_WAYLINE_UUIDS);
        if (subWaylineUuids == null || subWaylineUuids.isEmpty()) {
            return errorResult("Missing required parameter: sub_wayline_uuids",
                    "Provide an ordered list of at least one sub-wayline UUID to concatenate.");
        }

        Map<String, String> editorWarnings = getStringMapArg(args, PARAM_EDITOR_WARNINGS);

        // Mock 模式
        if (sdkService.isMockMode() || sdkService.getWorkspace() == null) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("_mock", true);
            data.put("wayline_uuid", "wl_concat_" + System.currentTimeMillis());
            data.put("target_wayline_name", targetWaylineName);
            data.put("kmz_object_key", kmzObjectKey);
            data.put("sub_wayline_uuids", subWaylineUuids);
            if (editorWarnings != null) {
                data.put("editor_warnings", editorWarnings);
            }
            return successResult(data, "Wayline '" + targetWaylineName + "' concatenated from "
                    + subWaylineUuids.size() + " sub-wayline(s) (mock).");
        }

        // 真实 SDK 调用
        return executeRealConcat(targetWaylineName, kmzObjectKey, subWaylineUuids, editorWarnings);
    }

    // ==================== 真实 SDK 调用 ====================

    /**
     * 真实 SDK 列表查询。
     */
    private Map<String, Object> executeRealList(Map<String, Object> args, int page, int pageSize) {
        try {
            log.info("[REAL] listWaylines: page={}/{}", page, pageSize);
            Workspace workspace = sdkService.getWorkspace();
            WaylineManageModule wmm = workspace.getWaylineManageModule();

            GetWaylineListParam param = new GetWaylineListParam();
            param.setPage(page);
            param.setPageSize(pageSize);

            CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
            wmm.getWaylineList(param, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getHttpCode(), "getWaylineList failed: " + formatSdkError(error)));
                    return;
                }
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("items", result.getItems());
                data.put("pagination", result.getPagination());
                future.complete(data);
            });

            Map<String, Object> data = SdkCallbackBridge.await(future);
            return successResult(data, "Wayline list retrieved.");
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            return errorResult("SDK timeout", "Wayline list request timed out.");
        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("SDK error (code=" + e.getErrorCode() + ")", e.getMessage());
        } catch (Exception e) {
            log.error("Wayline list error: {}", e.getMessage(), e);
            return errorResult("List failed: " + e.getMessage(), "Retry or check connectivity.");
        }
    }

    /**
     * 真实 SDK 详情查询。
     */
    private Map<String, Object> executeRealDetail(String waylineUuid) {
        try {
            log.info("[REAL] getWaylineDetail: uuid={}", waylineUuid);
            Workspace workspace = sdkService.getWorkspace();
            WaylineManageModule wmm = workspace.getWaylineManageModule();

            CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
            wmm.getWaylineDetail(waylineUuid, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getHttpCode(), "getWaylineDetail failed: " + formatSdkError(error)));
                    return;
                }
                future.complete(mapWaylineDetail(result));
            });

            Map<String, Object> data = SdkCallbackBridge.await(future);
            return successResult(data, "Wayline detail retrieved for " + waylineUuid);
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            return errorResult("SDK timeout", "Wayline detail request timed out.");
        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("SDK error (code=" + e.getErrorCode() + ")", e.getMessage());
        } catch (Exception e) {
            log.error("Wayline detail error: {}", e.getMessage(), e);
            return errorResult("Detail failed: " + e.getMessage(), "Retry or check connectivity.");
        }
    }

    /**
     * 把 SDK 的 {@link GetWaylineDetailResult} 映射为结构化 Map。
     *
     * <p>SDK 3.3.7 没有 {@code templateTypes} / {@code payloadModelKey} 字段，
     * 机型与载荷信息从 {@code kmz_data.payload_types} 透出。包可见以便单元测试直接验证字段映射。
     *
     * @param result SDK 详情结果
     * @return 结构化详情
     */
    Map<String, Object> mapWaylineDetail(GetWaylineDetailResult result) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("wayline_uuid", result.getWaylineUuid());
        data.put("wayline_name", result.getWaylineName());
        data.put("md5", result.getMd5());
        data.put("kmz_object_key", result.getKmzObjectKey());
        data.put("url", result.getUrl());
        data.put("suffix", result.getSuffix());
        data.put("deleted_at", result.getDeletedAt());
        data.put("updated_at", result.getUpdatedAt());
        data.put("kmz_without_res_url", result.getKmzWithoutResUrl());
        data.put("target_area_file_url", result.getTargetAreaFileUrl());
        data.put("business_file_url", result.getBusinessFileUrl());
        data.put("kmz_data", mapKmzData(result.getKmzData()));
        data.put("ext_data", mapExtData(result.getExtData()));
        data.put("editor_warnings", result.getEditorWarnings());
        data.put("execute_warnings", result.getExecuteWarnings());
        return data;
    }

    /**
     * 映射 KMZ 解析数据。
     *
     * <p>包可见以便单元测试直接验证字段映射。
     *
     * @param kmzData SDK KMZ 数据，可能为 null
     * @return 结构化 Map，null 时返回 null
     */
    Map<String, Object> mapKmzData(WaylineDetailKmzData kmzData) {
        if (kmzData == null) {
            return null;
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("wayline_type", kmzData.getWaylineType());
        data.put("wayline_point_count", kmzData.getWaylinePointCount());
        data.put("drone_type", kmzData.getDroneType());
        data.put("payload_types", kmzData.getPayloadTypes());
        data.put("payload_types_with_index", kmzData.getPayloadTypesWithIndex());
        data.put("distance", kmzData.getDistance());
        data.put("duration", kmzData.getDuration());
        data.put("survey_area", kmzData.getSurveyArea());
        data.put("start_latitude", kmzData.getStartLatitude());
        data.put("start_longitude", kmzData.getStartLongitude());
        data.put("start_altitude", kmzData.getStartAltitude());
        data.put("estimated_photo_count", kmzData.getEstimatedPhotoCount());
        data.put("wayline_validity_check", kmzData.isWaylineValidityCheck());
        data.put("wayline_validity_check_codes", kmzData.getWaylineValidityCheckCodes());
        data.put("elevation_file_names", kmzData.getElevationFileNames());
        data.put("survey_area_scope", mapSurveyAreaScope(kmzData.getSurveyAreaScope()));
        return data;
    }

    /**
     * 映射测绘范围多边形。
     *
     * <p>包可见以便单元测试直接验证字段映射。
     *
     * @param scope SDK 测绘范围，可能为 null
     * @return {@code {points: [{lng, lat, alt}]}}，null 时返回 null
     */
    Map<String, Object> mapSurveyAreaScope(WaylineDetailSurveyAreaScope scope) {
        if (scope == null) {
            return null;
        }
        List<Map<String, Object>> points = new ArrayList<>();
        List<WaylineDetailSurveyAreaScopePoint> scopePoints = scope.getPoints();
        if (scopePoints != null) {
            for (WaylineDetailSurveyAreaScopePoint point : scopePoints) {
                points.add(Map.of(
                        "lng", point.getLng(),
                        "lat", point.getLat(),
                        "alt", point.getAlt()));
            }
        }
        return Map.of("points", points);
    }

    /**
     * 映射扩展数据（原始文件对象键、文件大小等）。
     *
     * <p>包可见以便单元测试直接验证字段映射。
     *
     * @param extData SDK 扩展数据，可能为 null
     * @return 结构化 Map，null 时返回 null
     */
    Map<String, Object> mapExtData(WaylineDetailExtData extData) {
        if (extData == null) {
            return null;
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("kmz_without_res_object_key", extData.getKmzWithoutResObjectKey());
        data.put("kmz_without_res_md5", extData.getKmzWithoutResMd5());
        data.put("target_area_file_object_key", extData.getTargetAreaFileObjectKey());
        data.put("business_file_object_key", extData.getBusinessFileObjectKey());
        data.put("model_file_object_keys", extData.getModelFileObjectKeys());
        data.put("elevation_file_object_keys", extData.getElevationFileObjectKeys());
        data.put("content_length", extData.getContentLength());
        return data;
    }

    /**
     * 真实 SDK 上传航线（支持一次提交多个 KMZ，本地路径与对象 key 可混用）。
     *
     * <p>每个文件独立提交并各自等待导入结果事件，单个文件失败不影响其余文件。
     *
     * @param waylineName 航线名称
     * @param fileRefs    待提交的文件引用列表（本地路径或对象 key）
     * @return 工具响应
     */
    private Map<String, Object> executeRealSubmit(String waylineName, List<String> fileRefs) {
        List<Map<String, Object>> results = new ArrayList<>();
        String firstWaylineUuid = null;
        int failedCount = 0;
        for (String fileRef : fileRefs) {
            Map<String, Object> item = submitSingleFile(waylineName, fileRef);
            results.add(item);
            if (item.get("error") != null) {
                failedCount++;
            } else if (firstWaylineUuid == null && item.get("wayline_uuid") != null) {
                firstWaylineUuid = item.get("wayline_uuid").toString();
            }
        }
        if (failedCount == results.size()) {
            return errorResult("All " + results.size() + " file(s) failed to submit",
                    "First error: " + results.get(0).get("error")
                            + ". Check the KMZ path/object key and the server-side import logs.");
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("wayline_name", waylineName);
        data.put("total_count", results.size());
        // submitted_count 只统计成功提交的文件，与 failed_count 互补
        data.put("submitted_count", results.size() - failedCount);
        data.put("failed_count", failedCount);
        data.put("status", failedCount == 0 ? STATUS_SUBMITTED : STATUS_PARTIALLY_SUBMITTED);
        data.put("results", results);
        if (firstWaylineUuid != null) {
            data.put("wayline_uuid", firstWaylineUuid);
        }
        return successResultWithNextSteps(data,
                "Wayline '" + waylineName + "' submitted (" + results.size() + " file(s)).",
                buildCreateTaskNextSteps(firstWaylineUuid));
    }

    /**
     * 提交单个 KMZ，按引用形式选择通道：绝对路径走本地文件上传，否则按对象 key 登记。
     *
     * <p>返回的 Map 中 {@code error} 非空表示该文件提交/导入失败。
     *
     * @param waylineName 航线名称
     * @param fileRef     KMZ 本地绝对路径或对象 key
     * @return 单文件结果
     */
    private Map<String, Object> submitSingleFile(String waylineName, String fileRef) {
        if (isLocalFilePath(fileRef)) {
            return submitLocalFile(waylineName, fileRef);
        }
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("file_path", fileRef);
        // 异步请求标识：服务端 ImportRequest.AsyncRequestId 必填，用于配对导入结果事件
        String asyncRequestId = UUID.randomUUID().toString();
        item.put("async_request_id", asyncRequestId);
        return submitObjectKey(item, waylineName, fileRef, asyncRequestId);
    }

    /**
     * 提交已上传的 KMZ 对象 key：先注册导入事件监听，再登记，最后等待导入结果。
     *
     * @param item           单文件结果
     * @param waylineName    航线名称
     * @param objectKey      KMZ 对象键
     * @param asyncRequestId 异步请求标识
     * @return 单文件结果
     */
    private Map<String, Object> submitObjectKey(Map<String, Object> item, String waylineName,
                                                String objectKey, String asyncRequestId) {
        WaylineManageModule wmm = sdkService.getWorkspace().getWaylineManageModule();
        // 先注册导入事件监听，再提交，避免事件先于监听到达而丢失
        CompletableFuture<ImportWaylineEventResult> importFuture = new CompletableFuture<>();
        int listenerId = wmm.listenImportWaylineEvent(event -> {
            if (event != null && asyncRequestId.equals(event.getAsyncRequestId())) {
                importFuture.complete(event);
            }
        });
        // 监听注册失败时回调永远不会触发：不提交（否则会白白等待 30 秒并误报 timeout），
        // 直接标记该文件失败，由批量汇总逻辑计入 failed_count
        if (listenerId == INVALID_LISTEN_ID) {
            log.error("listenImportWaylineEvent failed: objectKey={}, listenerId={}", objectKey, listenerId);
            item.put("status", STATUS_FAILED);
            item.put("error", "Failed to register wayline import event listener (listenerId="
                    + listenerId + ")");
            return item;
        }
        try {
            submitWaylineInfo(wmm, waylineName, objectKey, asyncRequestId);
            item.put("status", STATUS_SUBMITTED);
            // 对象 key 通道由调用方自行保管 UUID，事件超时不兜底反查
            fillImportResult(item, importFuture, null);
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            item.put("status", STATUS_TIMEOUT);
            item.put("error", e.getMessage());
        } catch (SdkCallbackBridge.SdkException e) {
            item.put("status", STATUS_FAILED);
            item.put("error", "SDK error (code=" + e.getErrorCode() + "): " + e.getMessage());
        } catch (Exception e) {
            log.error("Wayline submit error: {}", e.getMessage(), e);
            item.put("status", STATUS_FAILED);
            item.put("error", e.getMessage());
        } finally {
            wmm.cancelListenImportWaylineEvent(listenerId);
        }
        return item;
    }

    /**
     * 提交本地 KMZ 文件：先经 {@code importKmz} 上传换取对象 key，再按平台导入模式完成入库。
     *
     * <p>平台有同步、异步两种导入模式，以 importKmz 返回的 asyncRequestId 是否为空区分：
     * 同步模式（为空）一步完成入库，按航线名称反查 UUID；异步模式（非空）用对象 key 登记导入，
     * 结果由平台经 {@code listenImportWaylineEvent} 推送，必须先注册监听再登记。
     *
     * @param waylineName 航线名称
     * @param localPath   本地 KMZ 绝对路径（已通过允许目录校验）
     * @return 单文件结果
     */
    private Map<String, Object> submitLocalFile(String waylineName, String localPath) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("file_path", localPath);

        WaylineManageModule wmm = sdkService.getWorkspace().getWaylineManageModule();
        try {
            log.info("[REAL] importKmz: name={}, file={}", waylineName, localPath);
            ImportKmzItem uploaded = uploadKmz(wmm, localPath);
            item.put("kmz_object_key", uploaded.getKmzObjectKey());

            String asyncRequestId = uploaded.getAsyncRequestId();
            if (asyncRequestId == null || asyncRequestId.isBlank()) {
                log.info("Platform imported synchronously (empty asyncRequestId), skip submitWaylineInfo:"
                        + " name={}, objectKey={}", waylineName, uploaded.getKmzObjectKey());
                item.put("status", STATUS_IMPORTED);
                item.put("wayline_uuid", lookupUuidByNameWithRetry(wmm, resolveImportedName(uploaded, waylineName)));
                return item;
            }

            item.put("async_request_id", asyncRequestId);
            submitImportedItem(item, wmm, waylineName, uploaded);
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            item.put("status", STATUS_TIMEOUT);
            item.put("error", e.getMessage());
        } catch (SdkCallbackBridge.SdkException e) {
            item.put("status", STATUS_FAILED);
            item.put("error", "SDK error (code=" + e.getErrorCode() + "): " + e.getMessage());
        } catch (Exception e) {
            log.error("Wayline local upload error: {}", e.getMessage(), e);
            item.put("status", STATUS_FAILED);
            item.put("error", e.getMessage());
        }
        return item;
    }

    /**
     * 异步模式下完成导入的第二、三步：先注册监听，再登记，然后等待导入结果事件。
     * 监听按 {@code kmzObjectKey} 匹配：asyncRequestId 要等 importKmz 回调才拿到，
     * 而同一批次的多次上传可能并发，用对象 key 配对更稳妥。
     *
     * @param item        单文件结果
     * @param wmm         航线管理模块
     * @param waylineName 航线名称
     * @param uploaded    importKmz 上传结果
     */
    private void submitImportedItem(Map<String, Object> item, WaylineManageModule wmm,
                                    String waylineName, ImportKmzItem uploaded) {
        String objectKey = uploaded.getKmzObjectKey();
        // 必须先注册监听再登记，否则可能漏掉平台推送的导入完成事件
        CompletableFuture<ImportWaylineEventResult> importFuture = new CompletableFuture<>();
        int listenerId = wmm.listenImportWaylineEvent(event -> {
            if (event != null && Objects.equals(objectKey, event.getKmzObjectKey())) {
                importFuture.complete(event);
            }
        });
        if (listenerId == INVALID_LISTEN_ID) {
            log.error("listenImportWaylineEvent failed: objectKey={}, listenerId={}", objectKey, listenerId);
            item.put("status", STATUS_FAILED);
            item.put("error", "Failed to register wayline import event listener (listenerId="
                    + listenerId + ")");
            return;
        }
        try {
            submitWaylineInfo(wmm, waylineName, objectKey, uploaded.getAsyncRequestId());
            item.put("status", STATUS_SUBMITTED);
            // 本地文件导入：事件超时后按名称反查 UUID，避免调用方拿不到 UUID 卡住后续流程
            fillImportResult(item, importFuture, resolveImportedName(uploaded, waylineName));
        } finally {
            cancelImportListener(wmm, listenerId);
        }
    }

    /**
     * 取消导入事件监听，忽略取消失败（监听器已随批量导入结束释放）。
     *
     * @param wmm        航线管理模块
     * @param listenerId 监听 ID
     */
    private void cancelImportListener(WaylineManageModule wmm, int listenerId) {
        try {
            wmm.cancelListenImportWaylineEvent(listenerId);
        } catch (RuntimeException e) {
            log.warn("cancelListenImportWaylineEvent failed: listenerId={}, error={}",
                    listenerId, e.getMessage());
        }
    }

    /**
     * 解析导入后实际使用的航线名称：优先平台回传的名称，为空时回退到调用方指定名称。
     *
     * @param uploaded    importKmz 上传结果
     * @param waylineName 调用方指定名称
     * @return 用于反查 UUID 的航线名称
     */
    private String resolveImportedName(ImportKmzItem uploaded, String waylineName) {
        String importedName = uploaded.getWaylineName();
        return importedName == null || importedName.isBlank() ? waylineName : importedName;
    }

    /**
     * 调用 SDK {@code importKmz} 把本地 KMZ 上传到对象存储，返回首个上传项。
     *
     * @param wmm       航线管理模块
     * @param localPath 本地 KMZ 绝对路径
     * @return 上传结果（含 kmzObjectKey / waylineName / asyncRequestId）
     */
    private ImportKmzItem uploadKmz(WaylineManageModule wmm, String localPath) {
        CompletableFuture<ImportKmzResult> future = new CompletableFuture<>();
        wmm.importKmz(List.of(localPath), (error, result) -> {
            if (error != null && !error.isSuccess()) {
                future.completeExceptionally(new SdkCallbackBridge.SdkException(
                        error.getHttpCode(), "importKmz failed: " + formatSdkError(error)));
                return;
            }
            future.complete(result);
        });

        ImportKmzResult result = SdkCallbackBridge.await(future, IMPORT_UPLOAD_TIMEOUT_SECONDS);
        if (result == null || result.getList() == null || result.getList().isEmpty()) {
            throw new SdkCallbackBridge.SdkException(-1, "importKmz returned no result for file: "
                    + localPath);
        }
        ImportKmzItem first = result.getList().get(0);
        log.info("[REAL] importKmz done: objectKey={}, waylineName={}, asyncRequestId={}",
                first.getKmzObjectKey(), first.getWaylineName(), first.getAsyncRequestId());
        return first;
    }

    /**
     * 按航线名称反查 UUID，未查到时按固定间隔重试。
     *
     * <p>同步导入模式下 importKmz 返回时平台落库可能尚未完成，单次查询会落空；
     * 重试耗尽仍未查到则抛异常，避免把「未确认落库」当作导入成功。
     *
     * @param wmm         航线管理模块
     * @param waylineName 航线名称
     * @return 匹配到的 UUID
     */
    private String lookupUuidByNameWithRetry(WaylineManageModule wmm, String waylineName) {
        for (int attempt = 1; attempt <= UUID_LOOKUP_MAX_ATTEMPTS; attempt++) {
            String uuid = lookupUuidByName(wmm, waylineName);
            if (uuid != null) {
                return uuid;
            }
            if (attempt < UUID_LOOKUP_MAX_ATTEMPTS) {
                sleepBeforeRetry(waylineName);
            }
        }
        throw new SdkCallbackBridge.SdkException(-1,
                "Wayline not found by name after " + UUID_LOOKUP_MAX_ATTEMPTS
                        + " attempts: waylineName=" + waylineName);
    }

    /**
     * 按航线名称在列表首页反查 UUID。
     *
     * @param wmm         航线管理模块
     * @param waylineName 航线名称
     * @return 匹配到的 UUID，未匹配到返回 null
     */
    private String lookupUuidByName(WaylineManageModule wmm, String waylineName) {
        GetWaylineListParam param = new GetWaylineListParam();
        param.setPage(DEFAULT_PAGE);
        param.setPageSize(UUID_LOOKUP_PAGE_SIZE);

        CompletableFuture<GetWaylineListResult> future = new CompletableFuture<>();
        wmm.getWaylineList(param, (error, result) -> {
            if (error != null && !error.isSuccess()) {
                future.completeExceptionally(new SdkCallbackBridge.SdkException(
                        error.getHttpCode(), "getWaylineList failed: " + formatSdkError(error)));
                return;
            }
            future.complete(result);
        });

        GetWaylineListResult result = SdkCallbackBridge.await(future);
        if (result == null || result.getItems() == null) {
            return null;
        }
        for (WaylineListItemResult listItem : result.getItems()) {
            if (waylineName.equals(listItem.getWaylineName())) {
                return listItem.getWaylineUuid();
            }
        }
        log.warn("Wayline not found in first page after import: waylineName={}", waylineName);
        return null;
    }

    /**
     * 反查重试前的等待。
     *
     * @param waylineName 航线名称（异常信息用）
     */
    private void sleepBeforeRetry(String waylineName) {
        try {
            Thread.sleep(UUID_LOOKUP_INTERVAL_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SdkCallbackBridge.SdkException(-1,
                    "Interrupted while looking up wayline UUID: waylineName=" + waylineName);
        }
    }

    /**
     * 提交单个航线的导入请求。
     *
     * @param wmm            航线管理模块
     * @param waylineName    航线名称
     * @param objectKey      KMZ 对象键
     * @param asyncRequestId 异步请求标识
     */
    private void submitWaylineInfo(WaylineManageModule wmm, String waylineName,
                                   String objectKey, String asyncRequestId) {
        log.info("[REAL] submitWayline: name={}, file={}, asyncRequestId={}",
                waylineName, objectKey, asyncRequestId);
        SubmitWaylineInfoParam param = new SubmitWaylineInfoParam();
        param.setKmzObjectKey(objectKey);
        param.setWaylineName(waylineName);
        param.setAsyncRequestId(asyncRequestId);
        param.setTimestamp(System.currentTimeMillis() / MILLIS_PER_SECOND);

        CompletableFuture<Void> future = new CompletableFuture<>();
        wmm.submitWaylineInfo(param, (error) -> {
            if (error.isSuccess()) {
                future.complete(null);
            } else {
                future.completeExceptionally(new SdkCallbackBridge.SdkException(
                        error.getHttpCode(), "submitWaylineInfo failed: " + formatSdkError(error)));
            }
        });
        SdkCallbackBridge.await(future);
    }

    /**
     * 等待导入结果事件并回填数据。
     *
     * <p>提交已被受理但事件未在超时内到达时，不视为失败，仅标记 {@code import_event_timeout}；
     * 传入 {@code fallbackWaylineName} 时（本地文件导入），超时后按名称反查 UUID 兜底，
     * 保证调用方仍能拿到 {@code wayline_uuid} 进入后续流程。
     *
     * @param item                单文件结果
     * @param importFuture        导入结果事件
     * @param fallbackWaylineName 事件超时后用于反查 UUID 的航线名称，为 null 表示不兜底
     */
    private void fillImportResult(Map<String, Object> item,
                                  CompletableFuture<ImportWaylineEventResult> importFuture,
                                  String fallbackWaylineName) {
        try {
            ImportWaylineEventResult event =
                    SdkCallbackBridge.await(importFuture, IMPORT_EVENT_TIMEOUT_SECONDS);
            fillImportEventData(item, event);
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.warn("Import event not received in {}s for asyncRequestId={}",
                    IMPORT_EVENT_TIMEOUT_SECONDS, item.get("async_request_id"));
            item.put("import_event_timeout", true);
            fallbackUuidByName(item, fallbackWaylineName);
        }
    }

    /**
     * 导入事件超时后按名称反查 UUID 兜底。
     *
     * @param item         单文件结果
     * @param waylineName  航线名称，为 null 或空时跳过
     */
    private void fallbackUuidByName(Map<String, Object> item, String waylineName) {
        if (waylineName == null || waylineName.isBlank()) {
            return;
        }
        log.warn("Falling back to name lookup after import event timeout: waylineName={}", waylineName);
        item.put("wayline_uuid",
                lookupUuidByNameWithRetry(sdkService.getWorkspace().getWaylineManageModule(), waylineName));
    }

    /**
     * 把导入结果事件填入单文件结果。
     *
     * @param item  单文件结果
     * @param event 导入结果事件
     */
    private void fillImportEventData(Map<String, Object> item, ImportWaylineEventResult event) {
        item.put("wayline_uuid", event.getWaylineUuid());
        if (event.getCode() != IMPORT_CODE_SUCCESS) {
            // 服务端导入失败（如 KMZ 格式非法），透出错误码与信息
            item.put("status", STATUS_IMPORT_FAILED);
            item.put("import_code", event.getCode());
            item.put("import_message", event.getMessage());
            item.put("error", "Import failed (code=" + event.getCode() + "): " + event.getMessage());
            return;
        }
        item.put("status", STATUS_IMPORTED);
        ImportWaylineEventKmzData kmzData = event.getKmzData();
        if (kmzData != null) {
            item.put("wayline_point_count", kmzData.getWaylinePointCount());
            item.put("wayline_validity_check_codes", kmzData.getWaylineValidityCheckCodes());
            item.put("drone_type", kmzData.getDroneType());
        }
    }

    /**
     * 真实 SDK 更新航线。
     */
    private Map<String, Object> executeRealUpdate(String waylineUuid, String waylineName) {
        try {
            log.info("[REAL] updateWayline: uuid={}, name={}", waylineUuid, waylineName);
            Workspace workspace = sdkService.getWorkspace();
            WaylineManageModule wmm = workspace.getWaylineManageModule();

            CompletableFuture<Void> future = new CompletableFuture<>();
            wmm.updateWayline(waylineUuid, waylineName, (error, result) -> {
                if (error.isSuccess()) {
                    future.complete(null);
                } else {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getHttpCode(), "updateWayline failed: " + formatSdkError(error)));
                }
            });

            SdkCallbackBridge.await(future);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("wayline_uuid", waylineUuid);
            data.put("wayline_name", waylineName);
            data.put("status", "updated");
            return successResult(data, "Wayline '" + waylineUuid + "' updated to name '" + waylineName + "'.");
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            return errorResult("SDK timeout", "Wayline update request timed out.");
        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("SDK error (code=" + e.getErrorCode() + ")", e.getMessage());
        } catch (Exception e) {
            log.error("Wayline update error: {}", e.getMessage(), e);
            return errorResult("Update failed: " + e.getMessage(), "Retry or check connectivity.");
        }
    }

    /**
     * 真实 SDK 复制航线。
     *
     * @param waylineUuid            原航线 UUID
     * @param targetWaylineName      新航线名称，可为 null（沿用服务端命名规则）
     * @param targetObjectKeyPrefix  新 KMZ 对象键前缀，可为 null
     * @return 工具响应
     */
    private Map<String, Object> executeRealDuplicate(String waylineUuid, String targetWaylineName,
                                                     String targetObjectKeyPrefix) {
        try {
            log.info("[REAL] duplicateWayline: uuid={}, targetName={}, prefix={}",
                    waylineUuid, targetWaylineName, targetObjectKeyPrefix);
            Workspace workspace = sdkService.getWorkspace();
            WaylineManageModule wmm = workspace.getWaylineManageModule();

            DuplicateWaylineParam param = new DuplicateWaylineParam();
            param.setOriginalWaylineUuid(waylineUuid);
            if (targetWaylineName != null) {
                param.setTargetWaylineName(targetWaylineName);
            }
            if (targetObjectKeyPrefix != null) {
                param.setTargetObjectKeyPrefix(targetObjectKeyPrefix);
            }

            CompletableFuture<DuplicateWaylineResult> future = new CompletableFuture<>();
            wmm.duplicateWayline(param, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getHttpCode(), "duplicateWayline failed: " + formatSdkError(error)));
                    return;
                }
                future.complete(result);
            });

            DuplicateWaylineResult result = SdkCallbackBridge.await(future);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("source_uuid", waylineUuid);
            if (result != null) {
                data.put("new_uuid", result.getWaylineUuid());
                data.put("new_name", result.getWaylineName());
            }
            if (targetWaylineName != null) {
                data.put("target_wayline_name", targetWaylineName);
            }
            if (targetObjectKeyPrefix != null) {
                data.put("target_object_key_prefix", targetObjectKeyPrefix);
            }
            data.put("status", "duplicated");
            return successResult(data, "Wayline '" + waylineUuid + "' duplicated successfully.");
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            return errorResult("SDK timeout", "Wayline duplicate request timed out.");
        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("SDK error (code=" + e.getErrorCode() + ")", e.getMessage());
        } catch (Exception e) {
            log.error("Wayline duplicate error: {}", e.getMessage(), e);
            return errorResult("Duplicate failed: " + e.getMessage(), "Retry or check connectivity.");
        }
    }

    /**
     * 真实 SDK 批量删除。
     */
    private Map<String, Object> executeRealBatchDelete(List<String> uuids) {
        try {
            log.info("[REAL] batchDeleteWaylines: count={}", uuids.size());
            Workspace workspace = sdkService.getWorkspace();
            WaylineManageModule wmm = workspace.getWaylineManageModule();

            CompletableFuture<Object> future = new CompletableFuture<>();
            wmm.batchDeleteWaylines(uuids, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getHttpCode(), "batchDeleteWaylines failed: " + formatSdkError(error)));
                    return;
                }
                future.complete(result);
            });

            SdkCallbackBridge.await(future);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("deleted_uuids", uuids);
            data.put("count", uuids.size());
            data.put("status", "deleted");
            return successResult(data, uuids.size() + " wayline(s) deleted successfully.");
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            return errorResult("SDK timeout", "Wayline batch delete request timed out.");
        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("SDK error (code=" + e.getErrorCode() + ")", e.getMessage());
        } catch (Exception e) {
            log.error("Wayline batch delete error: {}", e.getMessage(), e);
            return errorResult("Batch delete failed: " + e.getMessage(), "Retry or check connectivity.");
        }
    }

    /**
     * 真实 SDK 批量下载。
     */
    private Map<String, Object> executeRealBatchDownload(List<String> uuids) {
        try {
            log.info("[REAL] batchDownloadWaylines: count={}", uuids.size());
            Workspace workspace = sdkService.getWorkspace();
            WaylineManageModule wmm = workspace.getWaylineManageModule();

            BatchDownloadWaylinesParam param = new BatchDownloadWaylinesParam();
            param.setWaylineUuids(uuids);

            CompletableFuture<List<BatchDownloadWaylinesItemResult>> future = new CompletableFuture<>();
            wmm.batchDownloadWaylines(param, (error, results) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getHttpCode(), "batchDownloadWaylines failed: " + formatSdkError(error)));
                    return;
                }
                future.complete(results);
            });

            List<BatchDownloadWaylinesItemResult> results = SdkCallbackBridge.await(future);
            List<Map<String, Object>> downloads = new ArrayList<>();
            if (results != null) {
                for (BatchDownloadWaylinesItemResult item : results) {
                    downloads.add(mapDownloadItem(item));
                }
            }
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("count", uuids.size());
            data.put("downloads", downloads);
            return successResult(data, "Download URLs generated for " + uuids.size() + " wayline(s).");
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            return errorResult("SDK timeout", "Wayline batch download request timed out.");
        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("SDK error (code=" + e.getErrorCode() + ")", e.getMessage());
        } catch (Exception e) {
            log.error("Wayline batch download error: {}", e.getMessage(), e);
            return errorResult("Batch download failed: " + e.getMessage(), "Retry or check connectivity.");
        }
    }

    /**
     * 真实 SDK 拼接航线。
     */
    private Map<String, Object> executeRealConcat(String targetWaylineName, String kmzObjectKey,
                                                   List<String> subWaylineUuids,
                                                   Map<String, String> editorWarnings) {
        try {
            log.info("[REAL] concatWayline: name={}, subCount={}", targetWaylineName, subWaylineUuids.size());
            Workspace workspace = sdkService.getWorkspace();
            WaylineManageModule wmm = workspace.getWaylineManageModule();

            ConcatWaylineParam param = new ConcatWaylineParam();
            param.setTargetWaylineName(targetWaylineName);
            param.setKmzObjectKey(kmzObjectKey);
            param.setSubWaylineUuids(subWaylineUuids);
            if (editorWarnings != null) {
                param.setEditorWarnings(editorWarnings);
            }

            CompletableFuture<ConcatWaylineResult> future = new CompletableFuture<>();
            wmm.concatWayline(param, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getHttpCode(), "concatWayline failed: " + formatSdkError(error)));
                    return;
                }
                future.complete(result);
            });

            ConcatWaylineResult result = SdkCallbackBridge.await(future);
            Map<String, Object> data = buildConcatResultData(result);
            return successResult(data, "Wayline '" + targetWaylineName + "' concatenated successfully.");
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            return errorResult("SDK timeout", "Wayline concat request timed out.");
        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("SDK error (code=" + e.getErrorCode() + ")", e.getMessage());
        } catch (Exception e) {
            log.error("Wayline concat error: {}", e.getMessage(), e);
            return errorResult("Concat failed: " + e.getMessage(), "Retry or check connectivity.");
        }
    }

    // ── 订阅分发 ──

    /**
     * 订阅类 action 分发入口。
     *
     * @param session 当前会话
     * @param action  操作名
     * @param args    输入参数
     * @return 执行结果
     */
    private Map<String, Object> dispatchSubscription(SessionContext session, String action,
                                                      Map<String, Object> args) {
        return switch (action) {
            case ACTION_LISTEN_IMPORT -> handleListenImport(session);
            case ACTION_EVENTS -> handleEvents(session, args);
            case ACTION_LISTEN_STOP -> handleListenStop(session, args);
            default -> errorResult("Unknown action: " + action, "Report this bug.");
        };
    }

    /**
     * 处理 listen_import：订阅 workspace 级航线导入异步事件，返回 listen_id。
     * 该事件由 action=submit 触发，订阅本身不发起任何导入操作。
     */
    private Map<String, Object> handleListenImport(SessionContext session) {
        String channel = CHANNEL_IMPORT_WAYLINE_EVENT;
        String registryKey = ListenChannelSupport.registryKey(
                SUBSCRIBE_NAMESPACE, SUBSCRIBE_SCOPE_ID, channel);
        String scope = SubscriptionBuffer.scope(session, SUBSCRIBE_SCOPE_ID);
        boolean mock = sdkService.isMockMode() || sdkService.getWorkspace() == null;

        int listenId = listenSupport.startChannel(session, registryKey, channel, scope, channel, mock,
                this::mockImportWaylineEvent,
                this::startRealListen,
                this::cancelRealListen);

        if (listenId < 0) {
            return errorResult("Failed to listen for wayline import events",
                    "Check SDK connectivity and try again.");
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_LISTEN_IMPORT);
        data.put("channel", channel);
        data.put("listen_id", listenId);
        return successResult(data,
                "Subscribed to '" + channel + "'. Trigger an import with action=submit, "
                        + "then poll with action=events.");
    }

    /**
     * 处理 events：按游标轮询一个或多个已订阅 channel 的缓冲事件。
     */
    private Map<String, Object> handleEvents(SessionContext session, Map<String, Object> args) {
        List<String> channels = resolveChannels(session, args);
        long sinceSeq = getLongArg(args, PARAM_SINCE_SEQ, 0L);
        int limit = getIntArg(args, PARAM_LIMIT, DEFAULT_EVENTS_LIMIT);
        String scope = SubscriptionBuffer.scope(session, SUBSCRIBE_SCOPE_ID);

        Map<String, Object> streams = new LinkedHashMap<>();
        for (String channel : channels) {
            String registryKey = ListenChannelSupport.registryKey(
                    SUBSCRIBE_NAMESPACE, SUBSCRIBE_SCOPE_ID, channel);
            streams.put(channel, listenSupport.readChannel(session, registryKey, scope, channel, sinceSeq, limit));
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_EVENTS);
        data.put("streams", streams);
        return successResult(data, "Polled " + streams.size() + " channel(s).");
    }

    /**
     * 处理 listen_stop：停止一个/多个/全部已订阅的 channel。
     * 未显式传 channels 时停止本工具命名空间下全部活跃 channel。
     */
    private Map<String, Object> handleListenStop(SessionContext session, Map<String, Object> args) {
        List<String> requested = getStringListArg(args, PARAM_CHANNELS);
        List<String> channels = (requested != null && !requested.isEmpty())
                ? requested
                : listenSupport.activeChannels(session, subscribePrefix());

        ListenChannelSupport.StopResult result = listenSupport.stopChannels(session, channels,
                channel -> ListenChannelSupport.registryKey(
                        SUBSCRIBE_NAMESPACE, SUBSCRIBE_SCOPE_ID, channel), log);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_LISTEN_STOP);
        data.put("stopped", result.stopped());
        data.put("not_active", result.notActive());
        data.put("failed", result.failed());
        return successResult(data, "Stopped " + result.stopped().size() + " channel(s).");
    }

    /**
     * 解析 events 要轮询的 channel 列表：显式传入 channels 则用它，
     * 否则取当前本工具命名空间下全部已激活 channel。
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
     * 判断是否为订阅类 action（listen_import/events/listen_stop），
     * 订阅类操作只读且无需人工确认。
     */
    private boolean isSubscriptionAction(String action) {
        return ACTION_LISTEN_IMPORT.equals(action)
                || ACTION_EVENTS.equals(action)
                || ACTION_LISTEN_STOP.equals(action);
    }

    /**
     * 调用真实 SDK 订阅航线导入事件，返回 listenId；模块不可用时返回 -1。
     */
    private int startRealListen(Consumer<Map<String, Object>> onEvent) {
        WaylineManageModule module = sdkService.getWorkspace().getWaylineManageModule();
        if (module == null) {
            return -1;
        }
        return module.listenImportWaylineEvent(result -> onEvent.accept(toMap(result)));
    }

    /**
     * 调用真实 SDK 取消航线导入事件订阅。
     */
    private void cancelRealListen(int listenId) {
        WaylineManageModule module = sdkService.getWorkspace().getWaylineManageModule();
        if (module != null) {
            module.cancelListenImportWaylineEvent(listenId);
        }
    }

    /**
     * Mock — 航线导入事件，字段与 {@link ImportWaylineEventResult} 对齐。
     */
    private Map<String, Object> mockImportWaylineEvent() {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("async_request_id", "mock-async-1");
        event.put("timestamp", System.currentTimeMillis());
        event.put("kmz_object_key", "mock/key.zip");
        event.put("wayline_uuid", "mock-uuid");
        event.put("code", 0);
        event.put("message", "ok");
        event.put("_mock", true);
        return event;
    }

    /**
     * 将 {@link ImportWaylineEventResult} 转换为事件 Map，
     * 省略 kmzData/extData 两个嵌套结构体，避免响应膨胀。
     */
    private Map<String, Object> toMap(ImportWaylineEventResult result) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("async_request_id", result.getAsyncRequestId());
        event.put("timestamp", result.getTimestamp());
        event.put("kmz_object_key", result.getKmzObjectKey());
        event.put("wayline_uuid", result.getWaylineUuid());
        event.put("code", result.getCode());
        event.put("message", result.getMessage());
        return event;
    }

    /**
     * 将 {@link ConcatWaylineResult} 中真正有用的字段摘取为响应数据，
     * 避免把不透明的 SDK 对象直接塞进 JSON 响应。
     */
    private Map<String, Object> buildConcatResultData(ConcatWaylineResult result) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("wayline_uuid", result.getWaylineUuid());

        WaylineDetailKmzData kmzData = result.getKmzData();
        if (kmzData != null) {
            Map<String, Object> kmzSummary = new LinkedHashMap<>();
            kmzSummary.put("drone_type", kmzData.getDroneType());
            kmzSummary.put("wayline_point_count", kmzData.getWaylinePointCount());
            kmzSummary.put("distance_meters", kmzData.getDistance());
            kmzSummary.put("duration_seconds", kmzData.getDuration());
            kmzSummary.put("estimated_photo_count", kmzData.getEstimatedPhotoCount());
            kmzSummary.put("validity_check_passed", kmzData.isWaylineValidityCheck());
            List<Integer> validityCodes = kmzData.getWaylineValidityCheckCodes();
            if (validityCodes != null && !validityCodes.isEmpty()) {
                kmzSummary.put("validity_warning_codes", validityCodes);
            }
            data.put("kmz_summary", kmzSummary);
        }

        WaylineDetailExtData extData = result.getExtData();
        if (extData != null) {
            Map<String, Object> fileSummary = new LinkedHashMap<>();
            fileSummary.put("object_key", extData.getKmzWithoutResObjectKey());
            fileSummary.put("md5", extData.getKmzWithoutResMd5());
            fileSummary.put("content_length_bytes", extData.getContentLength());
            data.put("file_summary", fileSummary);
        }
        return data;
    }

    /**
     * 从参数 Map 中获取 String-to-String 映射参数（如 editor_warnings）。
     * 过滤 key 或 value 为空的条目，结果为空时返回 null。
     */
    private static Map<String, String> getStringMapArg(Map<String, Object> args, String key) {
        if (args == null) {
            return null;
        }
        Object val = args.get(key);
        if (!(val instanceof Map<?, ?> rawMap)) {
            return null;
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                result.put(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        return result.isEmpty() ? null : result;
    }

    /**
     * 映射单个批量下载结果。
     *
     * @param item SDK 下载项
     * @return 结构化下载项
     */
    private Map<String, Object> mapDownloadItem(BatchDownloadWaylinesItemResult item) {
        Map<String, Object> download = new LinkedHashMap<>();
        download.put("wayline_uuid", item.getWaylineUuid());
        download.put("wayline_name", item.getWaylineName());
        download.put("kmz_object_key", item.getKmzObjectKey());
        download.put("download_url", item.getUrl());
        download.put("size", item.getSize());
        return download;
    }
}
