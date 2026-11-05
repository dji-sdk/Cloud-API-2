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
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.OnboardAiModule;
import com.dji.flighthubsdk.generated.AiIdentifyFilterSetParam;
import com.dji.flighthubsdk.generated.AiIdentifyScoreModeSetParam;
import com.dji.flighthubsdk.generated.AiIdentifyScoreResetParam;
import com.dji.flighthubsdk.generated.AiIdentifyScoreSetParam;
import com.dji.flighthubsdk.generated.AiIdentifySetParam;
import com.dji.flighthubsdk.generated.IntVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * fh_ai_identify — AI 识别配置（开关、滤波器、评分、评分模式、评分重置）。
 *
 * <p>安全等级：L1（REVERSIBLE），前置条件：SESSION + DRC_LINK。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhAiIdentifyTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhAiIdentifyTool.class);

    private static final String TOOL_NAME = "fh_ai_identify";
    private static final String PARAM_DEVICE_SN = "device_sn";
    private static final String PARAM_ACTION = "action";

    private static final String ACTION_SET = "set";
    private static final String ACTION_FILTER = "filter";
    private static final String ACTION_SCORE = "score";
    private static final String ACTION_SCORE_MODE = "score_mode";
    private static final String ACTION_SCORE_RESET = "score_reset";

    private static final Set<String> VALID_ACTIONS = Set.of(
            ACTION_SET, ACTION_FILTER, ACTION_SCORE, ACTION_SCORE_MODE, ACTION_SCORE_RESET);
    private static final List<String> VALID_ACTIONS_SORTED = List.of(
            "filter", "score", "score_mode", "score_reset", "set");

    private final SdkService sdkService;

    public FhAiIdentifyTool(SessionManager sessionManager, SdkService sdkService) {
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
        def.put("description", "Configure AI object identification on drone: "
                + "turn on/off (set), set detection filters (filter), "
                + "set score threshold (score), set score mode (score_mode), "
                + "reset score (score_reset).");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_DEVICE_SN, Map.of(
                "type", "string", "description", "Target drone serial number"));
        props.put(PARAM_ACTION, Map.of(
                "type", "string", "enum", VALID_ACTIONS_SORTED,
                "description", "Identify config action"));
        props.put("on", Map.of(
                "type", "integer",
                "description", "Identify mode: 0=off, 1=visible light, 2=infrared (for action=set)"));
        props.put("light_type", Map.of(
                "type", "integer",
                "description", "Light type: 1=wide, 2=zoom, 3=infrared (for filter/score/score_mode/score_reset)"));
        props.put("index", Map.of(
                "type", "integer",
                "description", "Fixed index, default 0 per protocol (rarely needs override)"));
        props.put("filters", Map.of(
                "type", "array", "items", Map.of("type", "integer"),
                "description", "Detection filter list (for action=filter)"));
        props.put("score", Map.of(
                "type", "integer",
                "description", "Score threshold (for action=score)"));
        props.put("score_mode", Map.of(
                "type", "integer",
                "description", "Score mode (for action=score_mode)"));
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
                String.format("About to execute '%s' action '%s' on %s. Confirm?", TOOL_NAME, action, sn));
        if (confirmError != null) {
            return confirmError;
        }

        Map<String, Object> paramError = validateParams(action, args);
        if (paramError != null) {
            return paramError;
        }

        return dispatchAction(session, sn, action, args);
    }

    private Map<String, Object> validateParams(String action, Map<String, Object> args) {
        if (ACTION_SET.equals(action)) {
            int on = getIntArg(args, "on", -1);
            if (on < 0 || on > 2) {
                return errorResult("Invalid 'on' value: " + on, "Must be 0 (off), 1 (visible), or 2 (infrared).");
            }
        } else if (ACTION_FILTER.equals(action)) {
            int lightType = getIntArg(args, "light_type", -1);
            if (lightType < 1 || lightType > 3) {
                return errorResult("Invalid 'light_type': " + lightType,
                        "Must be 1 (wide), 2 (zoom), or 3 (infrared).");
            }
        }
        return null;
    }

    private Map<String, Object> dispatchAction(
            SessionContext session, String sn, String action, Map<String, Object> args) {
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
                case ACTION_SET -> executeSet(aiModule, args);
                case ACTION_FILTER -> executeFilter(aiModule, args);
                case ACTION_SCORE -> executeScore(aiModule, args);
                case ACTION_SCORE_MODE -> executeScoreMode(aiModule, args);
                case ACTION_SCORE_RESET -> executeScoreReset(aiModule, args);
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
    private Map<String, Object> executeSet(OnboardAiModule aiModule, Map<String, Object> args) {
        int on = getIntArg(args, "on", -1);
        AiIdentifySetParam param = new AiIdentifySetParam();
        param.setOn(on);
        SdkCallbackBridge.awaitVoidRc(cb -> aiModule.AIIdentifySet(param, cb));
        return successResult(Map.of("action", ACTION_SET, "on", on), "AI identify set to " + on);
    }

    @Generated
    private Map<String, Object> executeFilter(OnboardAiModule aiModule, Map<String, Object> args) {
        int lightType = getIntArg(args, "light_type", -1);
        int index = getIntArg(args, "index", 0);
        AiIdentifyFilterSetParam param = new AiIdentifyFilterSetParam();
        param.setIndex(index);
        param.setLight_type(lightType);
        List<Integer> filters = parseIntList(args, "filters");
        if (filters != null) {
            IntVector vec = new IntVector();
            filters.forEach(vec::add);
            param.setFilters(vec);
        }
        SdkCallbackBridge.awaitVoidRc(cb -> aiModule.AIIdentifyFilterSet(param, cb));
        return successResult(Map.of("action", ACTION_FILTER, "light_type", lightType, "index", index),
                "AI identify filter set");
    }

    @Generated
    private Map<String, Object> executeScore(OnboardAiModule aiModule, Map<String, Object> args) {
        int lightType = getIntArg(args, "light_type", -1);
        int scoreVal = getIntArg(args, "score", -1);
        int index = getIntArg(args, "index", 0);
        AiIdentifyScoreSetParam param = new AiIdentifyScoreSetParam();
        param.setIndex(index);
        param.setLight_type(lightType);
        param.setScore(scoreVal);
        SdkCallbackBridge.awaitVoidRc(cb -> aiModule.AIIdentifyScoreSet(param, cb));
        return successResult(Map.of("action", ACTION_SCORE, "score", scoreVal), "AI identify score set");
    }

    @Generated
    private Map<String, Object> executeScoreMode(OnboardAiModule aiModule, Map<String, Object> args) {
        int lightType = getIntArg(args, "light_type", -1);
        int mode = getIntArg(args, "score_mode", -1);
        int index = getIntArg(args, "index", 0);
        AiIdentifyScoreModeSetParam param = new AiIdentifyScoreModeSetParam();
        param.setIndex(index);
        param.setLight_type(lightType);
        param.setScore_mode(mode);
        SdkCallbackBridge.awaitVoidRc(cb -> aiModule.AIIdentifyScoreModeSet(param, cb));
        return successResult(Map.of("action", ACTION_SCORE_MODE, "score_mode", mode),
                "AI identify score mode set");
    }

    @Generated
    private Map<String, Object> executeScoreReset(OnboardAiModule aiModule, Map<String, Object> args) {
        int lightType = getIntArg(args, "light_type", -1);
        int index = getIntArg(args, "index", 0);
        AiIdentifyScoreResetParam param = new AiIdentifyScoreResetParam();
        param.setIndex(index);
        param.setLight_type(lightType);
        SdkCallbackBridge.awaitVoidRc(cb -> aiModule.AIIdentifyScoreReset(param, cb));
        return successResult(Map.of("action", ACTION_SCORE_RESET, "light_type", lightType),
                "AI identify score reset");
    }

    private Map<String, Object> mockResult(String action, Map<String, Object> args) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("_mock", true);
        data.put("action", action);
        switch (action) {
            case ACTION_SET -> data.put("on", getIntArg(args, "on", 0));
            case ACTION_FILTER -> {
                data.put("light_type", getIntArg(args, "light_type", 1));
                data.put("filters", parseIntList(args, "filters"));
            }
            case ACTION_SCORE -> data.put("score", getIntArg(args, "score", 0));
            case ACTION_SCORE_MODE -> data.put("score_mode", getIntArg(args, "score_mode", 0));
            case ACTION_SCORE_RESET -> data.put("light_type", getIntArg(args, "light_type", 1));
            default -> { /* exhaustive switch */ }
        }
        return successResult(data, "Mock: AI identify " + action + " applied");
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
