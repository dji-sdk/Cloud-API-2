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

package com.dji.flighthub.mcp.tools.livesync;

import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.LiveSyncCameraCapacity;
import com.dji.flighthubsdk.bean.LiveSyncCreateParams;
import com.dji.flighthubsdk.bean.LiveSyncCreateResult;
import com.dji.flighthubsdk.bean.LiveSyncDeviceItemV2;
import com.dji.flighthubsdk.bean.LiveSyncDeviceV2;
import com.dji.flighthubsdk.bean.LiveSyncDevicesV2Result;
import com.dji.flighthubsdk.bean.LiveSyncItem;
import com.dji.flighthubsdk.bean.LiveSyncListParams;
import com.dji.flighthubsdk.bean.LiveSyncListResult;
import com.dji.flighthubsdk.bean.LiveSyncSchemaOption;
import com.dji.flighthubsdk.bean.LiveSyncSummaryResult;
import com.dji.flighthubsdk.bean.LiveSyncUpdateParams;
import com.dji.flighthubsdk.bean.LiveSyncVideoCapacity;
import com.dji.flighthubsdk.core.LiveSyncModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * MCP Tool: fh_live_sync — 直播同步管理（LiveSync CRUD）。
 *
 * <p>动作列表：
 * <ul>
 *   <li>{@code devices} — 查询可用设备及相机能力</li>
 *   <li>{@code create} — 创建直播同步任务（RTMP/GB28181 等）</li>
 *   <li>{@code update} — 更新直播同步任务（开关/参数）</li>
 *   <li>{@code list} — 分页查询直播同步列表</li>
 *   <li>{@code delete} — 删除直播同步任务</li>
 *   <li>{@code summary} — 统计摘要（总数/启用数）</li>
 * </ul>
 *
 * <p>SDK 入口：{@code SdkService.getWorkspace().getLiveSyncModule()}（Workspace 级别，非设备级别）。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhLiveSyncTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhLiveSyncTool.class);

    private static final String TOOL_NAME = "fh_live_sync";

    private static final String ACTION_DEVICES = "devices";
    private static final String ACTION_CREATE = "create";
    private static final String ACTION_UPDATE = "update";
    private static final String ACTION_LIST = "list";
    private static final String ACTION_DELETE = "delete";
    private static final String ACTION_SUMMARY = "summary";

    private final SdkService sdkService;

    public FhLiveSyncTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

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
        def.put("description", "直播同步管理（LiveSync）：查询可用设备、创建/更新/列表/删除直播同步任务（RTMP/GB28181 等）。");
        def.put("inputSchema", Map.of(
                "type", "object",
                "required", List.of("action"),
                "properties", Map.ofEntries(
                        Map.entry("action", Map.of(
                                "type", "string",
                                "enum", List.of(ACTION_DEVICES, ACTION_CREATE, ACTION_UPDATE,
                                        ACTION_LIST, ACTION_DELETE, ACTION_SUMMARY),
                                "description", "动作：devices=可用设备, create=创建, update=更新, list=列表, delete=删除, summary=统计")),
                        Map.entry("uuid", Map.of(
                                "type", "string",
                                "description", "直播同步任务 UUID（update/delete 必填）")),
                        Map.entry("sn", Map.of(
                                "type", "string",
                                "description", "设备序列号（create 必填；list 可选过滤）")),
                        Map.entry("camera_index", Map.of(
                                "type", "string",
                                "description", "相机索引，如 '81-0-0'（create 必填）")),
                        Map.entry("video_type", Map.of(
                                "type", "string",
                                "description", "视频类型，如 'normal-0'（create 可选）")),
                        Map.entry("video_quality", Map.of(
                                "type", "integer",
                                "description", "视频质量 0-100（create/update 可选）")),
                        Map.entry("schema", Map.of(
                                "type", "string",
                                "description", "推流协议：rtmp, rtsp, gb28181 等（create 必填；update/list 可选）")),
                        Map.entry("schema_option", Map.of(
                                "type", "object",
                                "description", "协议参数（url, server_id, server_ip, server_port 等），create 必填")),
                        Map.entry("converter_name", Map.of(
                                "type", "string",
                                "description", "转换器名称（create/update 可选）")),
                        Map.entry("enable", Map.of(
                                "type", "boolean",
                                "description", "是否启用推流（update 可选；list 可选过滤）")),
                        Map.entry("page", Map.of(
                                "type", "integer",
                                "description", "页码，默认 1（list 用）")),
                        Map.entry("size", Map.of(
                                "type", "integer",
                                "description", "每页数量，默认 20（list 用）"))
                )));
        return def;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> args) {
        if (requireSession() == null) {
            return noSessionError();
        }

        String action = getStringArg(args, "action", "");
        return switch (action) {
            case ACTION_DEVICES -> doDevices();
            case ACTION_CREATE -> doCreate(args);
            case ACTION_UPDATE -> doUpdate(args);
            case ACTION_LIST -> doList(args);
            case ACTION_DELETE -> doDelete(args);
            case ACTION_SUMMARY -> doSummary();
            default -> errorResult("Unknown action: " + action,
                    "Valid: devices, create, update, list, delete, summary");
        };
    }

    // ==================== devices ====================

    private Map<String, Object> doDevices() {
        if (sdkService.isMockMode()) {
            return mockDevices();
        }
        LiveSyncModule module = resolveModule();
        if (module == null) {
            return moduleUnavailableError();
        }

        CompletableFuture<LiveSyncDevicesV2Result> future = new CompletableFuture<>();
        module.getLiveSyncDevicesV2((error, result) -> bridgeCallback(future, error, result));
        LiveSyncDevicesV2Result result = SdkCallbackBridge.await(future);

        List<Map<String, Object>> devices = new ArrayList<>();
        if (result.getList() != null) {
            for (LiveSyncDeviceItemV2 item : result.getList()) {
                devices.add(mapDeviceItem(item));
            }
        }
        return successResult(Map.of("total", result.getTotal(), "devices", devices),
                "Found " + result.getTotal() + " device(s)");
    }

    private Map<String, Object> mockDevices() {
        Map<String, Object> gateway = new LinkedHashMap<>();
        gateway.put("sn", "DOCK-MOCK-001");
        gateway.put("name", "Mock Dock");
        gateway.put("device_model_key", "0-67-0");
        gateway.put("device_type", "dock");
        gateway.put("camera_capacity", List.of());

        Map<String, Object> host = new LinkedHashMap<>();
        host.put("sn", "DRONE-MOCK-001");
        host.put("name", "Mock Drone");
        host.put("device_model_key", "0-89-0");
        host.put("device_type", "drone");
        host.put("camera_capacity", List.of(
                Map.of("camera_index", "81-0-0",
                        "videos", List.of(
                                Map.of("video_type", "normal-0", "video_index", "0"),
                                Map.of("video_type", "zoom-0", "video_index", "1")))));

        Map<String, Object> item = Map.of("gateway", gateway, "hosts", List.of(host));
        return successResult(Map.of("total", 1L, "devices", List.of(item), "_mock", true),
                "Mock: 1 device");
    }

    // ==================== create ====================

    private Map<String, Object> doCreate(Map<String, Object> args) {
        Map<String, Object> denied = enforceSecurityLevel(SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }

        String sn = getStringArg(args, "sn", null);
        String cameraIndex = getStringArg(args, "camera_index", null);
        String schema = getStringArg(args, "schema", null);
        if (sn == null || cameraIndex == null || schema == null) {
            return errorResult("create requires sn, camera_index, schema",
                    "Example: {action:'create', sn:'...', camera_index:'81-0-0', schema:'rtmp', schema_option:{url:'rtmp://...'}}");
        }

        if (sdkService.isMockMode()) {
            return mockCreate(sn, cameraIndex, schema);
        }

        LiveSyncModule module = resolveModule();
        if (module == null) {
            return moduleUnavailableError();
        }

        LiveSyncCreateParams params = new LiveSyncCreateParams();
        params.setSn(sn);
        params.setCameraIndex(cameraIndex);
        params.setSchema(schema);

        String videoType = getStringArg(args, "video_type", null);
        if (videoType != null) {
            params.setVideoType(videoType);
        }
        int videoQuality = getIntArg(args, "video_quality", -1);
        if (videoQuality >= 0) {
            params.setVideoQuality(videoQuality);
        }
        String converterName = getStringArg(args, "converter_name", null);
        if (converterName != null) {
            params.setConverterName(converterName);
        }

        LiveSyncSchemaOption option = buildSchemaOption(args);
        if (option != null) {
            params.setSchemaOption(option);
        }

        CompletableFuture<LiveSyncCreateResult> future = new CompletableFuture<>();
        module.createLiveSync(params, (error, result) -> bridgeCallback(future, error, result));
        LiveSyncCreateResult result = SdkCallbackBridge.await(future);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("item", mapItem(result.getItem()));
        if (result.isHasLimit()) {
            data.put("limit", result.getLimit());
        }
        log.info("LiveSync created: uuid={}, sn={}, schema={}", result.getItem().getUuid(), sn, schema);
        return successResult(data, "LiveSync task created");
    }

    private Map<String, Object> mockCreate(String sn, String cameraIndex, String schema) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("uuid", "mock-livesync-" + System.currentTimeMillis() % 100000);
        item.put("status", 1);
        item.put("sn", sn);
        item.put("camera_index", cameraIndex);
        item.put("schema", schema);
        item.put("enable", false);
        return successResult(Map.of("item", item, "_mock", true), "Mock: LiveSync created");
    }

    // ==================== update ====================

    private Map<String, Object> doUpdate(Map<String, Object> args) {
        Map<String, Object> denied = enforceSecurityLevel(SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }

        String uuid = getStringArg(args, "uuid", null);
        if (uuid == null) {
            return errorResult("update requires uuid", "Pass the LiveSync task UUID.");
        }

        if (sdkService.isMockMode()) {
            return mockUpdate(uuid, args);
        }

        LiveSyncModule module = resolveModule();
        if (module == null) {
            return moduleUnavailableError();
        }

        LiveSyncUpdateParams params = new LiveSyncUpdateParams();
        params.setId(uuid);

        if (args.containsKey("enable")) {
            params.setSetEnable(true);
            params.setEnable(Boolean.TRUE.equals(args.get("enable")));
        }
        if (args.containsKey("schema")) {
            params.setSetSchema(true);
            params.setSchema(getStringArg(args, "schema", ""));
        }
        if (args.containsKey("sn")) {
            params.setSetSn(true);
            params.setSn(getStringArg(args, "sn", ""));
        }
        if (args.containsKey("camera_index")) {
            params.setSetCameraIndex(true);
            params.setCameraIndex(getStringArg(args, "camera_index", ""));
        }
        if (args.containsKey("video_type")) {
            params.setSetVideoType(true);
            params.setVideoType(getStringArg(args, "video_type", ""));
        }
        if (args.containsKey("video_quality")) {
            params.setSetVideoQuality(true);
            params.setVideoQuality(getIntArg(args, "video_quality", 0));
        }
        if (args.containsKey("converter_name")) {
            params.setSetConverterName(true);
            params.setConverterName(getStringArg(args, "converter_name", ""));
        }

        LiveSyncSchemaOption option = buildSchemaOption(args);
        if (option != null) {
            params.setSetSchemaOption(true);
            params.setSchemaOption(option);
        }

        CompletableFuture<LiveSyncItem> future = new CompletableFuture<>();
        module.updateLiveSync(params, (error, result) -> bridgeCallback(future, error, result));
        LiveSyncItem item = SdkCallbackBridge.await(future);

        log.info("LiveSync updated: uuid={}", uuid);
        return successResult(Map.of("item", mapItem(item)), "LiveSync task updated");
    }

    private Map<String, Object> mockUpdate(String uuid, Map<String, Object> args) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("uuid", uuid);
        item.put("status", Boolean.TRUE.equals(args.get("enable")) ? 2 : 1);
        item.put("enable", Boolean.TRUE.equals(args.get("enable")));
        return successResult(Map.of("item", item, "_mock", true), "Mock: LiveSync updated");
    }

    // ==================== list ====================

    private Map<String, Object> doList(Map<String, Object> args) {
        if (sdkService.isMockMode()) {
            return mockList();
        }

        LiveSyncModule module = resolveModule();
        if (module == null) {
            return moduleUnavailableError();
        }

        LiveSyncListParams params = new LiveSyncListParams();
        params.setPage(getIntArg(args, "page", 1));
        params.setSize(getIntArg(args, "size", 20));

        String sn = getStringArg(args, "sn", null);
        if (sn != null) {
            params.setSetSn(true);
            params.setSn(sn);
        }
        String schema = getStringArg(args, "schema", null);
        if (schema != null) {
            params.setSetSchema(true);
            params.setSchema(schema);
        }
        if (args.containsKey("enable")) {
            params.setSetEnable(true);
            params.setEnable(Boolean.TRUE.equals(args.get("enable")));
        }

        CompletableFuture<LiveSyncListResult> future = new CompletableFuture<>();
        module.getLiveSyncList(params, (error, result) -> bridgeCallback(future, error, result));
        LiveSyncListResult result = SdkCallbackBridge.await(future);

        List<Map<String, Object>> items = new ArrayList<>();
        if (result.getList() != null) {
            for (LiveSyncItem item : result.getList()) {
                items.add(mapItem(item));
            }
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("page", result.getPage());
        data.put("size", result.getSize());
        data.put("total", result.getTotal());
        data.put("list", items);
        return successResult(data, "Page " + result.getPage() + ", " + items.size() + " item(s), total " + result.getTotal());
    }

    private Map<String, Object> mockList() {
        return successResult(Map.of("page", 1, "size", 20, "total", 0L, "list", List.of(), "_mock", true),
                "Mock: empty list");
    }

    // ==================== delete ====================

    private Map<String, Object> doDelete(Map<String, Object> args) {
        Map<String, Object> denied = enforceSecurityLevel(SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }

        String uuid = getStringArg(args, "uuid", null);
        if (uuid == null) {
            return errorResult("delete requires uuid", "Pass the LiveSync task UUID.");
        }

        if (sdkService.isMockMode()) {
            return successResult(Map.of("uuid", uuid, "_mock", true), "Mock: LiveSync deleted");
        }

        LiveSyncModule module = resolveModule();
        if (module == null) {
            return moduleUnavailableError();
        }

        CompletableFuture<Void> future = new CompletableFuture<>();
        module.deleteLiveSync(uuid, error -> {
            if (error.isSuccess()) {
                future.complete(null);
            } else {
                future.completeExceptionally(new SdkCallbackBridge.SdkException(
                        error.getServerCode(), formatSdkError(error)));
            }
        });
        SdkCallbackBridge.await(future);

        log.info("LiveSync deleted: uuid={}", uuid);
        return successResult(Map.of("uuid", uuid), "LiveSync task deleted");
    }

    // ==================== summary ====================

    private Map<String, Object> doSummary() {
        if (sdkService.isMockMode()) {
            return successResult(Map.of("total", 0L, "enable_count", 0L, "_mock", true),
                    "Mock: summary");
        }

        LiveSyncModule module = resolveModule();
        if (module == null) {
            return moduleUnavailableError();
        }

        CompletableFuture<LiveSyncSummaryResult> future = new CompletableFuture<>();
        module.getLiveSyncSummary((error, result) -> bridgeCallback(future, error, result));
        LiveSyncSummaryResult result = SdkCallbackBridge.await(future);

        return successResult(Map.of("total", result.getTotal(), "enable_count", result.getEnableCnt()),
                "Total: " + result.getTotal() + ", enabled: " + result.getEnableCnt());
    }

    // ==================== helpers ====================

    private LiveSyncModule resolveModule() {
        try {
            if (sdkService.getWorkspace() == null) {
                return null;
            }
            return sdkService.getWorkspace().getLiveSyncModule();
        } catch (Exception e) {
            log.error("Failed to get LiveSyncModule: {}", e.getMessage());
            return null;
        }
    }

    private Map<String, Object> moduleUnavailableError() {
        return errorResult("LiveSyncModule unavailable",
                "Workspace not connected or LiveSync not supported. Check workspace configuration.");
    }

    private <T> void bridgeCallback(CompletableFuture<T> future, FlightHubError error, T result) {
        if (error.isSuccess()) {
            future.complete(result);
        } else {
            future.completeExceptionally(new SdkCallbackBridge.SdkException(
                    error.getServerCode(), formatSdkError(error)));
        }
    }

    @SuppressWarnings("unchecked")
    private LiveSyncSchemaOption buildSchemaOption(Map<String, Object> args) {
        Object raw = args.get("schema_option");
        if (!(raw instanceof Map)) {
            return null;
        }
        Map<String, Object> opt = (Map<String, Object>) raw;
        LiveSyncSchemaOption option = new LiveSyncSchemaOption();
        ifPresent(opt, "url", option::setUrl);
        ifPresent(opt, "server_id", option::setServerId);
        ifPresent(opt, "server_ip", option::setServerIp);
        ifPresent(opt, "server_port", option::setServerPort);
        ifPresent(opt, "device_password", option::setDevicePassword);
        ifPresent(opt, "local_port", option::setLocalPort);
        ifPresent(opt, "device_id", option::setDeviceId);
        ifPresent(opt, "local_channel", option::setLocalChannel);
        ifPresent(opt, "provider", option::setProvider);
        ifPresent(opt, "volc_user_id", option::setVolcUserId);
        ifPresent(opt, "rtsp_url", option::setRtspUrl);
        ifPresent(opt, "username", option::setUsername);
        ifPresent(opt, "password", option::setPassword);
        if (opt.containsKey("enable_ts")) {
            option.setEnableTs(Boolean.TRUE.equals(opt.get("enable_ts")));
        }
        return option;
    }

    private static void ifPresent(Map<String, Object> map, String key, java.util.function.Consumer<String> setter) {
        Object val = map.get(key);
        if (val != null) {
            setter.accept(String.valueOf(val));
        }
    }

    private static Map<String, Object> mapItem(LiveSyncItem item) {
        if (item == null) {
            return Map.of();
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("uuid", item.getUuid());
        m.put("status", item.getStatus());
        m.put("sn", item.getSn());
        m.put("camera_index", item.getCameraIndex());
        m.put("name", item.getName());
        m.put("schema", item.getSchema());
        m.put("device_name", item.getDeviceName());
        m.put("device_online", item.isDeviceOnline());
        m.put("device_model_key", item.getDeviceModelKey());
        m.put("device_type", item.getDeviceType());
        m.put("video_type", item.getVideoType());
        m.put("video_quality", item.getVideoQuality());
        m.put("region", item.getRegion());
        m.put("enable", item.isEnable());
        m.put("err_msg", item.getErrMsg());
        if (item.getSchemaOption() != null) {
            m.put("schema_option", mapSchemaOption(item.getSchemaOption()));
        }
        return m;
    }

    private static Map<String, Object> mapSchemaOption(LiveSyncSchemaOption opt) {
        Map<String, Object> m = new LinkedHashMap<>();
        putIfNotNull(m, "url", opt.getUrl());
        putIfNotNull(m, "server_id", opt.getServerId());
        putIfNotNull(m, "server_ip", opt.getServerIp());
        putIfNotNull(m, "server_port", opt.getServerPort());
        putIfNotNull(m, "device_id", opt.getDeviceId());
        putIfNotNull(m, "local_port", opt.getLocalPort());
        putIfNotNull(m, "local_channel", opt.getLocalChannel());
        putIfNotNull(m, "provider", opt.getProvider());
        putIfNotNull(m, "volc_user_id", opt.getVolcUserId());
        putIfNotNull(m, "rtsp_url", opt.getRtspUrl());
        putIfNotNull(m, "username", opt.getUsername());
        return m;
    }

    private static Map<String, Object> mapDeviceItem(LiveSyncDeviceItemV2 item) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("gateway", mapDevice(item.getGateway()));
        List<Map<String, Object>> hosts = new ArrayList<>();
        if (item.getHosts() != null) {
            for (LiveSyncDeviceV2 host : item.getHosts()) {
                hosts.add(mapDevice(host));
            }
        }
        m.put("hosts", hosts);
        return m;
    }

    private static Map<String, Object> mapDevice(LiveSyncDeviceV2 dev) {
        if (dev == null) {
            return Map.of();
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("sn", dev.getSn());
        m.put("name", dev.getName());
        m.put("device_model_key", dev.getDeviceModelKey());
        m.put("device_type", dev.getDeviceType());
        List<Map<String, Object>> cameras = new ArrayList<>();
        if (dev.getCameraCapacity() != null) {
            for (LiveSyncCameraCapacity cam : dev.getCameraCapacity()) {
                Map<String, Object> cm = new LinkedHashMap<>();
                cm.put("camera_index", cam.getCameraIndex());
                List<Map<String, Object>> videos = new ArrayList<>();
                if (cam.getVideosList() != null) {
                    for (LiveSyncVideoCapacity vc : cam.getVideosList()) {
                        videos.add(Map.of("video_type", vc.getVideoType(), "video_index", vc.getVideoIndex()));
                    }
                }
                cm.put("videos", videos);
                cameras.add(cm);
            }
        }
        m.put("camera_capacity", cameras);
        return m;
    }

    private static void putIfNotNull(Map<String, Object> map, String key, String value) {
        if (value != null) {
            map.put(key, value);
        }
    }
}
