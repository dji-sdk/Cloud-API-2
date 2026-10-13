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

import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.PsdkControlModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * fh_psdk_widget — PSDK 自定义控件操作（设置值/输入文本）。
 *
 * <p>支持 action：
 * <ul>
 *   <li>set_value — 设置控件数值（L0）</li>
 *   <li>set_input_text — 设置控件输入文本（L0）</li>
 * </ul>
 *
 * <p>所有 action 均为 L0，不需要 confirm。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhPsdkWidgetTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhPsdkWidgetTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_psdk_widget";

    /** 有效 action 列表（固定顺序，用于 schema enum） */
    private static final List<String> VALID_ACTIONS = List.of("set_value", "set_input_text");

    /** 有效 action 集合（用于快速校验） */
    private static final Set<String> VALID_ACTIONS_SET = Set.copyOf(VALID_ACTIONS);

    /** 参数名常量 */
    private static final String PARAM_ACTION = "action";
    private static final String PARAM_DEVICE_SN = "device_sn";
    private static final String PARAM_PAYLOAD_INDEX = "payload_index";
    private static final String PARAM_WIDGET_INDEX = "widget_index";
    private static final String PARAM_VALUE = "value";
    private static final String PARAM_TEXT = "text";

    /** 默认 payload index */
    private static final int DEFAULT_PAYLOAD_INDEX = 0;

    /** widget_index 缺失标记值 */
    private static final int WIDGET_INDEX_MISSING = -1;

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhPsdkWidgetTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description",
                "PSDK custom widget control: set widget numeric value or input text. "
                + "Actions: set_value, set_input_text.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_ACTION, Map.of("type", "string", "enum", VALID_ACTIONS, "description", "Operation to perform"));
        props.put(PARAM_DEVICE_SN, Map.of("type", "string", "description", "Device serial number"));
        props.put(PARAM_PAYLOAD_INDEX, Map.of("type", "integer", "description", "Payload index (default: 0)"));
        props.put(PARAM_WIDGET_INDEX, Map.of("type", "integer", "description", "Widget index (required for set_value)"));
        props.put(PARAM_VALUE, Map.of("type", "integer", "description", "Widget value to set (required for set_value)"));
        props.put(PARAM_TEXT, Map.of("type", "string", "description", "Input text content (required for set_input_text)"));

        schema.put("properties", props);
        schema.put("required", List.of(PARAM_ACTION, PARAM_DEVICE_SN));
        def.put("inputSchema", schema);
        return def;
    }

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

        String deviceSn = getStringArg(args, PARAM_DEVICE_SN, null);
        if (deviceSn == null) {
            return errorResult("Missing required parameter: device_sn", "Provide device serial number.");
        }

        switch (action) {
            case "set_value":
                return executeSetValue(args, deviceSn);
            case "set_input_text":
                return executeSetInputText(args, deviceSn);
            default:
                return errorResult("Unhandled action: " + action, null);
        }
    }

    @Override
    public boolean requiresHumanConfirmation() {
        return false;
    }

    // ==================== set_value（L0） ====================

    private Map<String, Object> executeSetValue(Map<String, Object> args, String deviceSn) {
        int payloadIndex = getIntArg(args, PARAM_PAYLOAD_INDEX, DEFAULT_PAYLOAD_INDEX);
        int widgetIndex = getIntArg(args, PARAM_WIDGET_INDEX, WIDGET_INDEX_MISSING);
        if (widgetIndex == WIDGET_INDEX_MISSING) {
            return errorResult("Missing required parameter: widget_index",
                    "Provide widget index for set_value action.");
        }

        if (!args.containsKey(PARAM_VALUE)) {
            return errorResult("Missing required parameter: value",
                    "Provide integer value for set_value action.");
        }
        int value = getIntArg(args, PARAM_VALUE, 0);

        log.info("[{}] set_value: deviceSn={}, payloadIndex={}, widgetIndex={}, value={}",
                TOOL_NAME, deviceSn, payloadIndex, widgetIndex, value);

        return sdkService.isMockMode()
                ? setValueInMockMode(deviceSn, payloadIndex, widgetIndex, value)
                : setValueWithRealSdk(deviceSn, payloadIndex, widgetIndex, value);
    }

    private Map<String, Object> setValueInMockMode(String deviceSn, int payloadIndex, int widgetIndex, int value) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("device_sn", deviceSn);
        data.put("payload_index", payloadIndex);
        data.put("widget_index", widgetIndex);
        data.put("value", value);
        data.put("status", "success");
        data.put("_mock", true);
        return successResult(data, "Widget value set.");
    }

    @Generated
    private Map<String, Object> setValueWithRealSdk(String deviceSn, int payloadIndex, int widgetIndex, int value) {
        try {
            PsdkControlModule pcm = getPsdkModule(deviceSn);
            if (pcm == null) {
                return errorResult("PsdkControlModule not available for device: " + deviceSn,
                        "Ensure device is connected and SDK is initialized.");
            }

            // widgetValueSet(int payloadIndex, int widgetIndex, int value, ResultCallbackWithError)
            CompletableFuture<Void> future = new CompletableFuture<>();
            pcm.widgetValueSet(payloadIndex, widgetIndex, value, error -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getServerCode(), "widgetValueSet failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(null);
            });

            SdkCallbackBridge.await(future);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("device_sn", deviceSn);
            data.put("payload_index", payloadIndex);
            data.put("widget_index", widgetIndex);
            data.put("value", value);
            data.put("status", "success");
            return successResult(data, "Widget value set.");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] set_value timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check device connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] set_value SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), null);
        } catch (Exception e) {
            log.error("[{}] set_value error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to set widget value: " + e.getMessage(), null);
        }
    }

    // ==================== set_input_text（L0） ====================

    private Map<String, Object> executeSetInputText(Map<String, Object> args, String deviceSn) {
        int payloadIndex = getIntArg(args, PARAM_PAYLOAD_INDEX, DEFAULT_PAYLOAD_INDEX);
        String text = getStringArg(args, PARAM_TEXT, null);
        if (text == null) {
            return errorResult("Missing required parameter: text",
                    "Provide text content for set_input_text action.");
        }

        log.info("[{}] set_input_text: deviceSn={}, payloadIndex={}, text={}",
                TOOL_NAME, deviceSn, payloadIndex, text);

        return sdkService.isMockMode()
                ? setInputTextInMockMode(deviceSn, payloadIndex, text)
                : setInputTextWithRealSdk(deviceSn, payloadIndex, text);
    }

    private Map<String, Object> setInputTextInMockMode(String deviceSn, int payloadIndex, String text) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("device_sn", deviceSn);
        data.put("payload_index", payloadIndex);
        data.put("text", text);
        data.put("status", "success");
        data.put("_mock", true);
        return successResult(data, "Widget input text set.");
    }

    @Generated
    private Map<String, Object> setInputTextWithRealSdk(String deviceSn, int payloadIndex, String text) {
        try {
            PsdkControlModule pcm = getPsdkModule(deviceSn);
            if (pcm == null) {
                return errorResult("PsdkControlModule not available for device: " + deviceSn,
                        "Ensure device is connected and SDK is initialized.");
            }

            // widgetInputBoxTextSet(int payloadIndex, String text, ResultCallbackWithError)
            CompletableFuture<Void> future = new CompletableFuture<>();
            pcm.widgetInputBoxTextSet(payloadIndex, text, error -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getServerCode(), "widgetInputBoxTextSet failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(null);
            });

            SdkCallbackBridge.await(future);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("device_sn", deviceSn);
            data.put("payload_index", payloadIndex);
            data.put("text", text);
            data.put("status", "success");
            return successResult(data, "Widget input text set.");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] set_input_text timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check device connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] set_input_text SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), null);
        } catch (Exception e) {
            log.error("[{}] set_input_text error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to set input text: " + e.getMessage(), null);
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 获取指定设备的 PsdkControlModule 实例。
     *
     * @param deviceSn 设备 SN
     * @return PsdkControlModule，不可用时返回 null
     */
    private PsdkControlModule getPsdkModule(String deviceSn) {
        Drone drone = sdkService.getDrone(deviceSn);
        if (drone == null) {
            return null;
        }
        return drone.getPsdkControlModule();
    }
}
