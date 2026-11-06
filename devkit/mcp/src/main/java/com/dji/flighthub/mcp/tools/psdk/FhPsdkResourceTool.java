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

package com.dji.flighthub.mcp.tools.psdk;

import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthubsdk.bean.CreatePsdkSpeakerResourceParams;
import com.dji.flighthubsdk.bean.EditPsdkSpeakerResourceParams;
import com.dji.flighthubsdk.bean.GetPsdkWidgetResourceParams;
import com.dji.flighthubsdk.bean.PsdkSpeakerResource;
import com.dji.flighthubsdk.bean.PsdkSpeakerResourceList;
import com.dji.flighthubsdk.bean.PsdkWidgetResource;
import com.dji.flighthubsdk.bean.PsdkWidgetResourceList;
import com.dji.flighthubsdk.bean.STSCredential;
import com.dji.flighthubsdk.bean.STSInfo;
import com.dji.flighthubsdk.core.PsdkResourceModule;
import com.dji.flighthubsdk.core.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * fh_psdk_resource — PSDK 喊话器/小组件资源管理（core.PsdkResourceModule）。
 *
 * <p>支持 action：
 * <ul>
 *   <li>list_speaker — 获取喊话器资源列表（只读，无需 confirm）</li>
 *   <li>create_speaker — 创建喊话器资源（L1，需 confirm）</li>
 *   <li>edit_speaker — 编辑喊话器资源（L1，需 confirm，id 为 String 类型）</li>
 *   <li>delete_speaker — 删除喊话器资源（L1，需 confirm，resource_id 为 int 类型）</li>
 *   <li>upload_speaker_audio — 获取音频上传 STS 临时凭证（L1，需 confirm）</li>
 *   <li>list_widget — 批量获取 PSDK 小组件资源（只读，无需 confirm）</li>
 * </ul>
 *
 * <p><b>id 类型不一致说明</b>：SDK 真实接口中 {@code edit_speaker} 使用 String 类型 id
 * （{@link EditPsdkSpeakerResourceParams#getId()}），而 {@code delete_speaker} 使用
 * int 类型 resourceId（{@link PsdkResourceModule#deletePsdkSpeakerResource(int, Object)}）。
 * 这是 SDK 本身的既有不一致，本工具按各自的真实签名实现，不做统一转换，
 * 调用方需注意：edit_speaker 传 {@code id}（字符串），delete_speaker 传 {@code resource_id}（整数）。
 *
 * <p><b>upload_speaker_audio 两步式设计</b>：由于 MCP Server 运行于远端，无法读取 Agent
 * 本地文件系统，本 action 不会真正上传音频文件，而是调用 {@code PsdkResourceModule.getSTS()}
 * 获取 STS 临时上传凭证并原样返回；调用方（Agent）需要：
 * <ol>
 *   <li>使用返回的 STS 凭证，自行将音频文件 PUT 到对象存储（bucket/endpoint 等信息已随凭证返回）</li>
 *   <li>基于 {@code object_key_prefix} + 自定义文件名构造最终 object_key</li>
 *   <li>再调用本工具的 {@code create_speaker} action，传入 {@code file_type=audio}
 *       和刚构造的 {@code object_key}，完成资源注册</li>
 * </ol>
 * 本 action 的响应中不包含任何文件内容相关字段。
 *
 * <p>安全等级：list_speaker / list_widget 为只读（{@link SecurityLevel#READ_ONLY}），
 * 其余 4 个写操作为可逆（{@link SecurityLevel#REVERSIBLE}），统一需要人工确认。
 * <p>支持 mock 模式（FH_MOCK=true）和真实 SDK 调用双路径。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhPsdkResourceTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhPsdkResourceTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_psdk_resource";

    // ── action 常量 ──
    private static final String ACTION_LIST_SPEAKER = "list_speaker";
    private static final String ACTION_CREATE_SPEAKER = "create_speaker";
    private static final String ACTION_EDIT_SPEAKER = "edit_speaker";
    private static final String ACTION_DELETE_SPEAKER = "delete_speaker";
    private static final String ACTION_UPLOAD_SPEAKER_AUDIO = "upload_speaker_audio";
    private static final String ACTION_LIST_WIDGET = "list_widget";

    /** 有效 action 列表（固定顺序，用于 schema enum） */
    private static final List<String> VALID_ACTIONS = List.of(
            ACTION_LIST_SPEAKER, ACTION_CREATE_SPEAKER, ACTION_EDIT_SPEAKER,
            ACTION_DELETE_SPEAKER, ACTION_UPLOAD_SPEAKER_AUDIO, ACTION_LIST_WIDGET);

    /** 读操作 action 集合（无需人工确认） */
    private static final Set<String> READ_ONLY_ACTIONS = Set.of(ACTION_LIST_SPEAKER, ACTION_LIST_WIDGET);

    /** 有效 action 集合（用于快速校验） */
    private static final Set<String> VALID_ACTIONS_SET = Set.copyOf(VALID_ACTIONS);

    // ── 参数名常量 ──
    private static final String PARAM_ACTION = "action";
    private static final String PARAM_ID = "id";
    private static final String PARAM_RESOURCE_ID = "resource_id";
    private static final String PARAM_NAME = "name";
    private static final String PARAM_FILE_TYPE = "file_type";
    private static final String PARAM_OBJECT_KEY = "object_key";
    private static final String PARAM_DURATION = "duration";
    private static final String PARAM_CONTENT = "content";
    private static final String PARAM_CREATE_SCENE = "create_scene";
    private static final String PARAM_NEED_OPUS = "need_opus";
    private static final String PARAM_SIZE = "size";
    private static final String PARAM_VISIBLE = "visible";
    private static final String PARAM_OBJECT_KEYS = "object_keys";
    private static final String PARAM_CONFIRM = "confirm";

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhPsdkResourceTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

    @Override
    public SecurityLevel getSecurityLevel() {
        return SecurityLevel.REVERSIBLE;
    }

    @Override
    public boolean requiresHumanConfirmation() {
        return true;
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
                "Manage PSDK speaker (megaphone) audio/TTS resources and PSDK widget UI resources "
                + "via core.PsdkResourceModule. Actions: list_speaker (READ_ONLY, list speaker resources), "
                + "create_speaker (REVERSIBLE+confirm, register a new speaker resource), "
                + "edit_speaker (REVERSIBLE+confirm, update by STRING id), "
                + "delete_speaker (REVERSIBLE+confirm, remove by INT resource_id — "
                + "note the id-type inconsistency vs edit_speaker: this is a known SDK quirk, not a bug), "
                + "upload_speaker_audio (REVERSIBLE+confirm, issues STS upload credentials ONLY; "
                + "it does NOT upload any file itself — the caller must PUT the audio file directly "
                + "to object storage using the returned credentials, then call create_speaker with "
                + "file_type='audio' and the constructed object_key to register the resource), "
                + "list_widget (READ_ONLY, batch-get PSDK widget UI resources by object_keys).");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_ACTION, Map.of(
                "type", "string",
                "enum", VALID_ACTIONS,
                "description", "Operation to perform"
        ));
        props.put(PARAM_ID, Map.of(
                "type", "string",
                "description", "Speaker resource id as STRING (required for edit_speaker only)"
        ));
        props.put(PARAM_RESOURCE_ID, Map.of(
                "type", "integer",
                "description", "Speaker resource id as INTEGER (required for delete_speaker only)"
        ));
        props.put(PARAM_NAME, Map.of(
                "type", "string",
                "description", "Resource name (required for create_speaker/edit_speaker)"
        ));
        props.put(PARAM_FILE_TYPE, Map.of(
                "type", "string",
                "enum", List.of("audio", "tts"),
                "description", "Resource file type (required for create_speaker/edit_speaker)"
        ));
        props.put(PARAM_OBJECT_KEY, Map.of(
                "type", "string",
                "description", "Object storage key, required when file_type=audio (create_speaker)"
        ));
        props.put(PARAM_DURATION, Map.of(
                "type", "integer",
                "description", "Audio duration in ms, required when file_type=audio and create_scene=record"
        ));
        props.put(PARAM_CONTENT, Map.of(
                "type", "string",
                "description", "TTS text content, required when file_type=tts"
        ));
        props.put(PARAM_CREATE_SCENE, Map.of(
                "type", "string",
                "enum", List.of("import", "record"),
                "description", "Creation scene (create_speaker only)"
        ));
        props.put(PARAM_NEED_OPUS, Map.of(
                "type", "boolean",
                "description", "Whether to also convert to opus format (create_speaker only)"
        ));
        props.put(PARAM_SIZE, Map.of(
                "type", "integer",
                "description", "File size in bytes (create_speaker only)"
        ));
        props.put(PARAM_VISIBLE, Map.of(
                "type", "boolean",
                "description", "Whether the resource is visible (create_speaker/edit_speaker)"
        ));
        props.put(PARAM_OBJECT_KEYS, Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", "Widget object keys to batch-get (list_widget only; may be omitted/empty)"
        ));
        props.put(PARAM_CONFIRM, Map.of(
                "type", "boolean",
                "description", "Set true to confirm this write operation"
        ));
        schema.put("properties", props);
        schema.put("required", List.of(PARAM_ACTION));
        def.put("inputSchema", schema);
        return def;
    }

    /**
     * 执行 action 分发。
     *
     * @param args 输入参数
     * @return 执行结果
     */
    @Override
    public Map<String, Object> execute(Map<String, Object> args) {
        SessionContext session = requireSession();
        if (session == null) {
            return noSessionError();
        }

        String action = getStringArg(args, PARAM_ACTION, null);
        if (action == null || !VALID_ACTIONS_SET.contains(action)) {
            return errorResult("Invalid or missing action", "Valid actions: " + VALID_ACTIONS);
        }

        Map<String, Object> denied = enforceSecurityLevel(
                READ_ONLY_ACTIONS.contains(action) ? SecurityLevel.READ_ONLY : SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }

        return switch (action) {
            case ACTION_LIST_SPEAKER -> executeListSpeaker();
            case ACTION_CREATE_SPEAKER -> executeCreateSpeaker(args);
            case ACTION_EDIT_SPEAKER -> executeEditSpeaker(args);
            case ACTION_DELETE_SPEAKER -> executeDeleteSpeaker(args);
            case ACTION_UPLOAD_SPEAKER_AUDIO -> executeUploadSpeakerAudio(args);
            default -> executeListWidget(args);
        };
    }

    // ==================== list_speaker ====================

    /**
     * 处理 list_speaker action：分发到 mock/真实 SDK 路径。
     *
     * @return 执行结果
     */
    private Map<String, Object> executeListSpeaker() {
        log.info("[{}] list_speaker", TOOL_NAME);
        return sdkService.isMockMode() ? listSpeakerInMockMode() : listSpeakerWithRealSdk();
    }

    /**
     * Mock 模式下返回喊话器资源列表。
     *
     * @return 成功响应
     */
    private Map<String, Object> listSpeakerInMockMode() {
        Map<String, Object> resource = new LinkedHashMap<>();
        resource.put("id", 1);
        resource.put("name", "mock-speaker-resource");
        resource.put("file_type", "audio");
        resource.put("object_key", "mock/object-key.mp3");
        resource.put("state", "ready");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("resources", List.of(resource));
        data.put("_mock", true);
        return successResult(data, "Speaker resources listed.");
    }

    /**
     * 通过真实 SDK 获取喊话器资源列表。
     *
     * @return 执行结果
     */
    @Generated
    private Map<String, Object> listSpeakerWithRealSdk() {
        try {
            PsdkResourceModule module = getPsdkResourceModuleOrNull();
            if (module == null) {
                return errorResult("PsdkResourceModule not available",
                        "Workspace may be unavailable. Call fh_connect first.");
            }

            CompletableFuture<PsdkSpeakerResourceList> future = new CompletableFuture<>();
            module.getPsdkSpeakerResourceList((error, resourceList) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getServerCode(), "getPsdkSpeakerResourceList failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(resourceList);
            });

            PsdkSpeakerResourceList resourceList = SdkCallbackBridge.await(future);
            List<Map<String, Object>> resources = resourceList != null
                    ? resourceList.getResources().stream().map(this::toSpeakerResourceMap).collect(Collectors.toList())
                    : List.of();

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("resources", resources);
            return successResult(data, "Speaker resources listed.");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] list_speaker timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] list_speaker SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), "Check error details.");
        } catch (Exception e) {
            log.error("[{}] list_speaker error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to list speaker resources: " + e.getMessage(), "Check logs for details.");
        }
    }

    /**
     * 将 SDK 的 PsdkSpeakerResource 转换为响应 Map。
     *
     * @param resource SDK 返回的喊话器资源
     * @return 响应用 Map
     */
    private Map<String, Object> toSpeakerResourceMap(PsdkSpeakerResource resource) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", resource.getId());
        map.put("name", resource.getName());
        map.put("file_type", resource.getFileType());
        map.put("object_key", resource.getObjectKey());
        map.put("object_url", resource.getObjectUrl());
        map.put("format", resource.getFormat());
        map.put("size", resource.getSize());
        map.put("duration", resource.getDuration());
        map.put("state", resource.getState());
        map.put("state_message", resource.getStateMessage());
        map.put("pcm_object_key", resource.getPcmObjectKey());
        map.put("pcm_object_url", resource.getPcmObjectUrl());
        map.put("pcm_size", resource.getPcmSize());
        map.put("pcm_md5_sum", resource.getPcmMd5Sum());
        map.put("opus_object_key", resource.getOpusObjectKey());
        map.put("opus_size", resource.getOpusSize());
        map.put("opus_md5_sum", resource.getOpusMd5Sum());
        map.put("content", resource.getContent());
        map.put("md5_sum", resource.getMd5Sum());
        map.put("created_at", resource.getCreatedAt());
        return map;
    }

    // ==================== create_speaker ====================

    /**
     * 处理 create_speaker action：参数校验、人工确认后分发到 mock/真实 SDK 路径。
     *
     * @param args 输入参数
     * @return 执行结果
     */
    private Map<String, Object> executeCreateSpeaker(Map<String, Object> args) {
        String name = getStringArg(args, PARAM_NAME, null);
        if (name == null) {
            return errorResult("Missing required parameter: name", "Provide a resource name.");
        }
        String fileType = getStringArg(args, PARAM_FILE_TYPE, null);
        if (fileType == null || !Set.of("audio", "tts").contains(fileType)) {
            return errorResult("Invalid or missing file_type", "Must be 'audio' or 'tts'.");
        }

        Map<String, Object> confirmResult = requireHumanConfirm(args, ACTION_CREATE_SPEAKER, name,
                "Create speaker resource '" + name + "' (file_type=" + fileType + ")");
        if (confirmResult != null) {
            return confirmResult;
        }

        log.info("[{}] create_speaker: name={}, fileType={}", TOOL_NAME, name, fileType);

        return sdkService.isMockMode()
                ? createSpeakerInMockMode(args, name, fileType)
                : createSpeakerWithRealSdk(args, name, fileType);
    }

    /**
     * Mock 模式下创建喊话器资源。
     *
     * @param args     输入参数
     * @param name     资源名称
     * @param fileType 文件类型
     * @return 成功响应
     */
    private Map<String, Object> createSpeakerInMockMode(Map<String, Object> args, String name, String fileType) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", 1001);
        data.put("name", name);
        data.put("file_type", fileType);
        data.put("object_key", getStringArg(args, PARAM_OBJECT_KEY, ""));
        data.put("state", "ready");
        data.put("_mock", true);
        return successResult(data, "Speaker resource created.");
    }

    /**
     * 通过真实 SDK 创建喊话器资源。
     *
     * @param args     输入参数
     * @param name     资源名称
     * @param fileType 文件类型
     * @return 执行结果
     */
    @Generated
    private Map<String, Object> createSpeakerWithRealSdk(Map<String, Object> args, String name, String fileType) {
        try {
            PsdkResourceModule module = getPsdkResourceModuleOrNull();
            if (module == null) {
                return errorResult("PsdkResourceModule not available",
                        "Workspace may be unavailable. Call fh_connect first.");
            }

            CreatePsdkSpeakerResourceParams params = buildCreateParams(args, name, fileType);

            CompletableFuture<PsdkSpeakerResource> future = new CompletableFuture<>();
            module.createPsdkSpeakerResource(params, (error, resource) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getServerCode(), "createPsdkSpeakerResource failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(resource);
            });

            PsdkSpeakerResource resource = SdkCallbackBridge.await(future);
            Map<String, Object> data = resource != null ? toSpeakerResourceMap(resource) : new LinkedHashMap<>();
            return successResult(data, "Speaker resource created.");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] create_speaker timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] create_speaker SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), "Check error details.");
        } catch (Exception e) {
            log.error("[{}] create_speaker error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to create speaker resource: " + e.getMessage(), "Check logs for details.");
        }
    }

    /**
     * 根据输入参数构建 CreatePsdkSpeakerResourceParams。
     *
     * @param args     输入参数
     * @param name     资源名称
     * @param fileType 文件类型
     * @return SDK 创建参数
     */
    private CreatePsdkSpeakerResourceParams buildCreateParams(Map<String, Object> args, String name, String fileType) {
        CreatePsdkSpeakerResourceParams params = new CreatePsdkSpeakerResourceParams();
        params.setName(name);
        params.setFileType(fileType);
        params.setObjectKey(getStringArg(args, PARAM_OBJECT_KEY, ""));
        params.setDuration(getLongArg(args, PARAM_DURATION, 0L));
        params.setContent(getStringArg(args, PARAM_CONTENT, ""));
        params.setCreateScene(getStringArg(args, PARAM_CREATE_SCENE, ""));
        params.setNeedOpus(getBooleanArg(args, PARAM_NEED_OPUS));
        params.setSize(getLongArg(args, PARAM_SIZE, 0L));
        params.setVisible(getBooleanArg(args, PARAM_VISIBLE));
        return params;
    }

    // ==================== edit_speaker ====================

    /**
     * 处理 edit_speaker action：参数校验（String id）、人工确认后分发到 mock/真实 SDK 路径。
     *
     * @param args 输入参数
     * @return 执行结果
     */
    private Map<String, Object> executeEditSpeaker(Map<String, Object> args) {
        String id = getStringArg(args, PARAM_ID, null);
        if (id == null) {
            return errorResult("Missing required parameter: id",
                    "Provide the speaker resource id as a STRING (edit_speaker uses String id).");
        }
        String name = getStringArg(args, PARAM_NAME, null);
        if (name == null) {
            return errorResult("Missing required parameter: name", "Provide a resource name.");
        }

        Map<String, Object> confirmResult = requireHumanConfirm(args, ACTION_EDIT_SPEAKER, id,
                "Edit speaker resource id=" + id + " (name=" + name + ")");
        if (confirmResult != null) {
            return confirmResult;
        }

        log.info("[{}] edit_speaker: id={}, name={}", TOOL_NAME, id, name);

        return sdkService.isMockMode()
                ? editSpeakerInMockMode(args, id, name)
                : editSpeakerWithRealSdk(args, id, name);
    }

    /**
     * Mock 模式下编辑喊话器资源。
     *
     * @param args 输入参数
     * @param id   资源 id（字符串）
     * @param name 资源名称
     * @return 成功响应
     */
    private Map<String, Object> editSpeakerInMockMode(Map<String, Object> args, String id, String name) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", id);
        data.put("name", name);
        data.put("file_type", getStringArg(args, PARAM_FILE_TYPE, ""));
        data.put("_mock", true);
        return successResult(data, "Speaker resource updated.");
    }

    /**
     * 通过真实 SDK 编辑喊话器资源。
     *
     * @param args 输入参数
     * @param id   资源 id（字符串）
     * @param name 资源名称
     * @return 执行结果
     */
    @Generated
    private Map<String, Object> editSpeakerWithRealSdk(Map<String, Object> args, String id, String name) {
        try {
            PsdkResourceModule module = getPsdkResourceModuleOrNull();
            if (module == null) {
                return errorResult("PsdkResourceModule not available",
                        "Workspace may be unavailable. Call fh_connect first.");
            }

            EditPsdkSpeakerResourceParams params = new EditPsdkSpeakerResourceParams();
            params.setId(id);
            params.setName(name);
            params.setFileType(getStringArg(args, PARAM_FILE_TYPE, ""));
            params.setVisible(getBooleanArg(args, PARAM_VISIBLE));
            params.setContent(getStringArg(args, PARAM_CONTENT, ""));

            CompletableFuture<PsdkSpeakerResource> future = new CompletableFuture<>();
            module.editPsdkSpeakerResource(params, (error, resource) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getServerCode(), "editPsdkSpeakerResource failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(resource);
            });

            PsdkSpeakerResource resource = SdkCallbackBridge.await(future);
            Map<String, Object> data = resource != null ? toSpeakerResourceMap(resource) : new LinkedHashMap<>();
            return successResult(data, "Speaker resource updated.");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] edit_speaker timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] edit_speaker SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), "Check error details.");
        } catch (Exception e) {
            log.error("[{}] edit_speaker error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to edit speaker resource: " + e.getMessage(), "Check logs for details.");
        }
    }

    // ==================== delete_speaker ====================

    /**
     * 处理 delete_speaker action：参数校验（int resource_id）、人工确认后分发到 mock/真实 SDK 路径。
     *
     * @param args 输入参数
     * @return 执行结果
     */
    private Map<String, Object> executeDeleteSpeaker(Map<String, Object> args) {
        Object rawId = args != null ? args.get(PARAM_RESOURCE_ID) : null;
        if (!(rawId instanceof Number) && !(rawId instanceof String s && !s.isBlank())) {
            return errorResult("Missing required parameter: resource_id",
                    "Provide the speaker resource id as an INTEGER (delete_speaker uses int resourceId).");
        }
        int resourceId = getIntArg(args, PARAM_RESOURCE_ID, Integer.MIN_VALUE);
        if (resourceId == Integer.MIN_VALUE) {
            return errorResult("Invalid resource_id", "resource_id must be a valid integer.");
        }

        Map<String, Object> confirmResult = requireHumanConfirm(args, ACTION_DELETE_SPEAKER,
                String.valueOf(resourceId), "Delete speaker resource id=" + resourceId);
        if (confirmResult != null) {
            return confirmResult;
        }

        log.info("[{}] delete_speaker: resourceId={}", TOOL_NAME, resourceId);

        return sdkService.isMockMode()
                ? deleteSpeakerInMockMode(resourceId)
                : deleteSpeakerWithRealSdk(resourceId);
    }

    /**
     * Mock 模式下删除喊话器资源。
     *
     * @param resourceId 资源 id（整数）
     * @return 成功响应
     */
    private Map<String, Object> deleteSpeakerInMockMode(int resourceId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("resource_id", resourceId);
        data.put("deleted", true);
        data.put("_mock", true);
        return successResult(data, "Speaker resource deleted.");
    }

    /**
     * 通过真实 SDK 删除喊话器资源。
     *
     * @param resourceId 资源 id（整数）
     * @return 执行结果
     */
    @Generated
    private Map<String, Object> deleteSpeakerWithRealSdk(int resourceId) {
        try {
            PsdkResourceModule module = getPsdkResourceModuleOrNull();
            if (module == null) {
                return errorResult("PsdkResourceModule not available",
                        "Workspace may be unavailable. Call fh_connect first.");
            }

            CompletableFuture<Void> future = new CompletableFuture<>();
            module.deletePsdkSpeakerResource(resourceId, error -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getServerCode(), "deletePsdkSpeakerResource failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(null);
            });

            SdkCallbackBridge.await(future);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("resource_id", resourceId);
            data.put("deleted", true);
            return successResult(data, "Speaker resource deleted.");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] delete_speaker timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] delete_speaker SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), "Check error details.");
        } catch (Exception e) {
            log.error("[{}] delete_speaker error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to delete speaker resource: " + e.getMessage(), "Check logs for details.");
        }
    }

    // ==================== upload_speaker_audio ====================

    /**
     * 处理 upload_speaker_audio action：人工确认后获取 STS 上传凭证（不上传文件本身）。
     *
     * @param args 输入参数
     * @return 执行结果
     */
    private Map<String, Object> executeUploadSpeakerAudio(Map<String, Object> args) {
        Map<String, Object> confirmResult = requireHumanConfirm(args, ACTION_UPLOAD_SPEAKER_AUDIO, "speaker-audio-sts",
                "Issue STS upload credentials for speaker audio (caller must PUT the file directly)");
        if (confirmResult != null) {
            return confirmResult;
        }

        log.info("[{}] upload_speaker_audio: issuing STS credentials", TOOL_NAME);

        return sdkService.isMockMode() ? uploadSpeakerAudioInMockMode() : uploadSpeakerAudioWithRealSdk();
    }

    /**
     * Mock 模式下返回 STS 上传凭证。
     *
     * @return 成功响应
     */
    private Map<String, Object> uploadSpeakerAudioInMockMode() {
        Map<String, Object> data = buildStsInfoMap(
                "mock/speaker/audio/", "mock-bucket", "https://mock-endpoint.example.com", "mock-provider",
                "mock-region", "mock-access-key-id", "mock-access-key-secret", 3600L, "mock-security-token", 0);
        data.put("_mock", true);
        return successResult(data, "STS upload credentials issued. Caller must PUT the file directly, "
                + "then call create_speaker to register it.");
    }

    /**
     * 通过真实 SDK 获取 STS 上传凭证。
     *
     * @return 执行结果
     */
    @Generated
    private Map<String, Object> uploadSpeakerAudioWithRealSdk() {
        try {
            PsdkResourceModule module = getPsdkResourceModuleOrNull();
            if (module == null) {
                return errorResult("PsdkResourceModule not available",
                        "Workspace may be unavailable. Call fh_connect first.");
            }

            CompletableFuture<STSInfo> future = new CompletableFuture<>();
            module.getSTS((error, stsInfo) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getServerCode(), "getSTS failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(stsInfo);
            });

            STSInfo stsInfo = SdkCallbackBridge.await(future);
            if (stsInfo == null) {
                return errorResult("Empty STS info returned", "Retry or check SDK connectivity.");
            }
            STSCredential credential = stsInfo.getCredentials();

            Map<String, Object> data = buildStsInfoMap(
                    stsInfo.getObjectKeyPrefix(), stsInfo.getBucket(), stsInfo.getEndpoint(),
                    stsInfo.getProvider(), stsInfo.getRegion(),
                    credential != null ? credential.getAccessKeyId() : "",
                    credential != null ? credential.getAccessKeySecret() : "",
                    credential != null ? credential.getExpire() : 0L,
                    credential != null ? credential.getSecurityToken() : "",
                    credential != null ? credential.getPlatform() : 0);
            return successResult(data, "STS upload credentials issued. Caller must PUT the file directly, "
                    + "then call create_speaker to register it.");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] upload_speaker_audio timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] upload_speaker_audio SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), "Check error details.");
        } catch (Exception e) {
            log.error("[{}] upload_speaker_audio error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to get STS upload credentials: " + e.getMessage(), "Check logs for details.");
        }
    }

    /**
     * 构建 STS 凭证响应 Map。注意：此响应中不包含任何文件内容字段，
     * 调用方需自行完成对象存储 PUT 操作。
     *
     * @param objectKeyPrefix 上传路径前缀
     * @param bucket          存储桶
     * @param endpoint        访问端点
     * @param provider        存储提供商
     * @param region          区域
     * @param accessKeyId     AccessKeyId
     * @param accessKeySecret AccessKeySecret
     * @param expire          过期时间
     * @param securityToken   安全令牌
     * @param platform        直播专用字段
     * @return STS 凭证响应 Map
     */
    private Map<String, Object> buildStsInfoMap(
            String objectKeyPrefix, String bucket, String endpoint, String provider, String region,
            String accessKeyId, String accessKeySecret, long expire, String securityToken, int platform) {
        Map<String, Object> credentials = new LinkedHashMap<>();
        credentials.put("access_key_id", accessKeyId);
        credentials.put("access_key_secret", accessKeySecret);
        credentials.put("expire", expire);
        credentials.put("security_token", securityToken);
        credentials.put("platform", platform);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("object_key_prefix", objectKeyPrefix);
        data.put("bucket", bucket);
        data.put("endpoint", endpoint);
        data.put("provider", provider);
        data.put("region", region);
        data.put("credentials", credentials);
        return data;
    }

    // ==================== list_widget ====================

    /**
     * 处理 list_widget action：分发到 mock/真实 SDK 路径。
     *
     * @param args 输入参数
     * @return 执行结果
     */
    private Map<String, Object> executeListWidget(Map<String, Object> args) {
        List<String> objectKeys = getStringListArg(args, PARAM_OBJECT_KEYS);
        List<String> effectiveKeys = objectKeys != null ? objectKeys : new ArrayList<>();

        log.info("[{}] list_widget: objectKeys={}", TOOL_NAME, effectiveKeys);

        return sdkService.isMockMode()
                ? listWidgetInMockMode(effectiveKeys)
                : listWidgetWithRealSdk(effectiveKeys);
    }

    /**
     * Mock 模式下返回小组件资源列表。
     *
     * @param objectKeys 待查询的 object key 列表
     * @return 成功响应
     */
    private Map<String, Object> listWidgetInMockMode(List<String> objectKeys) {
        List<Map<String, Object>> resources = new ArrayList<>();
        if (objectKeys.isEmpty()) {
            Map<String, Object> resource = new LinkedHashMap<>();
            resource.put("object_key", "mock/widget/default.png");
            resource.put("url", "https://mock-endpoint.example.com/mock/widget/default.png");
            resources.add(resource);
        } else {
            for (String key : objectKeys) {
                Map<String, Object> resource = new LinkedHashMap<>();
                resource.put("object_key", key);
                resource.put("url", "https://mock-endpoint.example.com/" + key);
                resources.add(resource);
            }
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("ui_resource_urls", resources);
        data.put("_mock", true);
        return successResult(data, "Widget resources listed.");
    }

    /**
     * 通过真实 SDK 批量获取小组件资源。
     *
     * @param objectKeys 待查询的 object key 列表
     * @return 执行结果
     */
    @Generated
    private Map<String, Object> listWidgetWithRealSdk(List<String> objectKeys) {
        try {
            PsdkResourceModule module = getPsdkResourceModuleOrNull();
            if (module == null) {
                return errorResult("PsdkResourceModule not available",
                        "Workspace may be unavailable. Call fh_connect first.");
            }

            GetPsdkWidgetResourceParams params = new GetPsdkWidgetResourceParams();
            params.setObjectKeys(new ArrayList<>(objectKeys));

            CompletableFuture<PsdkWidgetResourceList> future = new CompletableFuture<>();
            module.getPsdkWidgetResources(params, (error, resourceList) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getServerCode(), "getPsdkWidgetResources failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(resourceList);
            });

            PsdkWidgetResourceList resourceList = SdkCallbackBridge.await(future);
            List<Map<String, Object>> resources = resourceList != null
                    ? resourceList.getUiResourceUrls().stream().map(this::toWidgetResourceMap).collect(Collectors.toList())
                    : List.of();

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("ui_resource_urls", resources);
            return successResult(data, "Widget resources listed.");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] list_widget timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] list_widget SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), "Check error details.");
        } catch (Exception e) {
            log.error("[{}] list_widget error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to list widget resources: " + e.getMessage(), "Check logs for details.");
        }
    }

    /**
     * 将 SDK 的 PsdkWidgetResource 转换为响应 Map。
     *
     * @param resource SDK 返回的小组件资源
     * @return 响应用 Map
     */
    private Map<String, Object> toWidgetResourceMap(PsdkWidgetResource resource) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("object_key", resource.getObjectKey());
        map.put("url", resource.getUrl());
        return map;
    }

    // ==================== 工具方法 ====================

    /**
     * 获取 PsdkResourceModule 实例（来自 Workspace，而非 Drone/Dock）。
     *
     * @return PsdkResourceModule，workspace 不可用时返回 null
     */
    private PsdkResourceModule getPsdkResourceModuleOrNull() {
        Workspace workspace = sdkService.getWorkspace();
        if (workspace == null) {
            return null;
        }
        return workspace.getPsdkResourceModule();
    }
}
