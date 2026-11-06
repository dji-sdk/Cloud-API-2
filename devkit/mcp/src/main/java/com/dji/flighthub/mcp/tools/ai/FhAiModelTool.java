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

package com.dji.flighthub.mcp.tools.ai;

import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthubsdk.bean.AiModelsConfigParams;
import com.dji.flighthubsdk.bean.AiModelsConfigSet;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.OnboardAiModule;
import com.dji.flighthubsdk.generated.AiModelSelectParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * fh_ai_model — AI 模型选择与配置（select 选模型，config 绑定流+模型）。
 *
 * <p>安全等级：L1（REVERSIBLE），前置条件：SESSION + DRC_LINK。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhAiModelTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhAiModelTool.class);

    private static final String TOOL_NAME = "fh_ai_model";
    private static final String PARAM_DEVICE_SN = "device_sn";
    private static final String PARAM_ACTION = "action";

    private static final String ACTION_SELECT = "select";
    private static final String ACTION_CONFIG = "config";

    private static final Set<String> VALID_ACTIONS = Set.of(ACTION_SELECT, ACTION_CONFIG);
    private static final List<String> VALID_ACTIONS_SORTED = List.of("config", "select");

    private final SdkService sdkService;

    public FhAiModelTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

    @Override
    public boolean requiresHumanConfirmation() {
        return true;
    }

    @Override
    public SecurityLevel getSecurityLevel() {
        return SecurityLevel.REVERSIBLE;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description", "Select AI model or bind models to camera streams. "
                + "action=select: choose a single model by index. "
                + "action=config: bind model list to a specific stream_type.");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_DEVICE_SN, Map.of(
                "type", "string", "description", "Target drone serial number"));
        props.put(PARAM_ACTION, Map.of(
                "type", "string", "enum", VALID_ACTIONS_SORTED,
                "description", "Model action: select or config"));
        props.put("index", Map.of(
                "type", "integer", "description", "Model index (for action=select)"));
        props.put("stream_type", Map.of(
                "type", "integer",
                "description", "Stream type: 1=wide, 3=infrared (for action=config)"));
        props.put("models", Map.of(
                "type", "array", "items", Map.of("type", "integer"),
                "description", "Model index list to bind (for action=config)"));
        props.put("confirm", Map.of(
                "type", "boolean", "description", "Must be true to execute"));

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", props);
        schema.put("required", List.of(PARAM_DEVICE_SN, PARAM_ACTION, "confirm"));
        def.put("inputSchema", schema);
        return def;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> args) {
        SessionContext session = requireSession();
        if (session == null) {
            return noSessionError();
        }
        Map<String, Object> secError = enforceToolSecurityLevel();
        if (secError != null) {
            return secError;
        }
        String sn = getStringArg(args, PARAM_DEVICE_SN, null);
        if (sn == null) {
            return errorResult("Missing required parameter: device_sn",
                    "Provide the target drone serial number.");
        }
        Map<String, Object> drcError = requireDrcLink(session, sn);
        if (drcError != null) {
            return drcError;
        }
        String action = getStringArg(args, PARAM_ACTION, "");
        if (!VALID_ACTIONS.contains(action)) {
            return errorResult("Invalid action: " + action,
                    "Valid actions: " + VALID_ACTIONS_SORTED);
        }
        Map<String, Object> confirmError = requireHumanConfirm(args, TOOL_NAME + ":" + action, sn,
                String.format("About to execute '%s' on %s. Confirm?", TOOL_NAME + ":" + action, sn));
        if (confirmError != null) {
            return confirmError;
        }
        Map<String, Object> paramError = validateParams(action, args);
        if (paramError != null) {
            return paramError;
        }
        return dispatchAction(sn, action, args);
    }

    private Map<String, Object> validateParams(String action, Map<String, Object> args) {
        if (ACTION_SELECT.equals(action)) {
            if (getIntArg(args, "index", -1) < 0) {
                return errorResult("Missing required parameter: index", "Model index to select.");
            }
        } else if (ACTION_CONFIG.equals(action)) {
            if (getIntArg(args, "stream_type", -1) < 0) {
                return errorResult("Missing required parameter: stream_type",
                        "Stream type: 1=wide, 3=infrared.");
            }
            List<Integer> models = parseIntList(args, "models");
            if (models == null || models.isEmpty()) {
                return errorResult("Missing required parameter: models", "Model index list to bind.");
            }
        }
        return null;
    }

    private Map<String, Object> dispatchAction(String sn, String action, Map<String, Object> args) {
        if (sdkService.isMockMode()) {
            return mockResult(action, args);
        }
        Drone drone = sdkService.getDrone(sn);
        if (drone == null) {
            return errorResult("Device instance not found: " + sn,
                    "Use fh_device_list to check device status.");
        }
        OnboardAiModule aiModule = drone.getOnboardAiModule();
        try {
            return switch (action) {
                case ACTION_SELECT -> executeSelect(aiModule, args);
                case ACTION_CONFIG -> executeConfig(aiModule, args);
                default -> errorResult("Unhandled action: " + action, "This should not happen.");
            };
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            return errorResult("SDK timeout: " + e.getMessage(), "Check device connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(),
                    "Check device state and retry.");
        }
    }

    @Generated
    private Map<String, Object> executeSelect(OnboardAiModule aiModule, Map<String, Object> args) {
        int index = getIntArg(args, "index", -1);
        AiModelSelectParam param = new AiModelSelectParam();
        param.setIndex(index);
        SdkCallbackBridge.awaitVoidRc(cb -> aiModule.AIModelSelect(param, cb));
        return successResult(Map.of("action", ACTION_SELECT, "index", index), "AI model selected: " + index);
    }

    @Generated
    private Map<String, Object> executeConfig(OnboardAiModule aiModule, Map<String, Object> args) {
        int streamType = getIntArg(args, "stream_type", -1);
        List<Integer> models = parseIntList(args, "models");
        AiModelsConfigSet configSet = new AiModelsConfigSet();
        configSet.setStreamType(streamType);
        configSet.setModels(new ArrayList<>(models));
        AiModelsConfigParams params = new AiModelsConfigParams();
        params.setAiModelsConfig(List.of(configSet));
        SdkCallbackBridge.awaitVoid(cb -> aiModule.DrcAIModelsConfig(params, cb));
        return successResult(Map.of("action", ACTION_CONFIG, "stream_type", streamType, "models", models),
                "AI models config applied: stream_type=" + streamType);
    }

    private Map<String, Object> mockResult(String action, Map<String, Object> args) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("_mock", true);
        data.put("action", action);
        switch (action) {
            case ACTION_SELECT -> data.put("index", getIntArg(args, "index", 0));
            case ACTION_CONFIG -> {
                data.put("stream_type", getIntArg(args, "stream_type", 1));
                data.put("models", parseIntList(args, "models"));
            }
            default -> { /* exhaustive switch */ }
        }
        return successResult(data, "Mock: AI model " + action + " applied");
    }

    private static List<Integer> parseIntList(Map<String, Object> args, String key) {
        Object val = args != null ? args.get(key) : null;
        if (val instanceof List<?> list) {
            return list.stream()
                    .filter(item -> item instanceof Number)
                    .map(item -> ((Number) item).intValue())
                    .toList();
        }
        return null;
    }
}
