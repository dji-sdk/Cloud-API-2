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

package com.dji.flighthub.mcp.tools.simulator;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.simulator.SimulatorClient;
import com.dji.flighthub.mcp.simulator.SimulatorProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 封装 pf-device-simulator-service HmsService，进行 HMS 故障注入调试。
 *
 * <p>不含 Favorites/Presets 的增删改（模板策展功能，非 v1 范围）。全部为 L0（只读）或
 * L1（写，均可撤销），无需 confirm。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class SimHmsTool extends AbstractSimTool {

    private static final String HMS_BASE_PATH = "/simulator/api/v1/hms";
    private static final String DEVICES_BASE_PATH = "/simulator/api/v1/devices";

    private final SimulatorClient simulatorClient;

    public SimHmsTool(SessionManager sessionManager, SimulatorClient simulatorClient,
            FlightHubProperties flightHubProperties, SimulatorProperties simulatorProperties) {
        super(sessionManager, flightHubProperties, simulatorProperties);
        this.simulatorClient = simulatorClient;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        return Map.of(
                "name", "sim_hms",
                "description", "Inject and manage simulated HMS (Health Management System) faults: "
                        + "list/list_active_blockers/list_codes/list_presets/list_active/inject/batch_inject/"
                        + "inject_preset/cancel/cancel_all",
                "inputSchema", Map.of(
                        "type", "object",
                        "properties", buildProperties(),
                        "required", List.of("action")));
    }

    private Map<String, Object> buildProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("action", Map.of(
                "type", "string",
                "enum", List.of("list", "list_active_blockers", "list_codes", "list_presets", "list_active",
                        "inject", "batch_inject", "inject_preset", "cancel", "cancel_all"),
                "description", "The HMS action to perform"));
        properties.put("sn", Map.of("type", "string", "description", "Target device serial number"));
        properties.put("sns", Map.of("type", "array", "description", "List of SNs (for list_active)"));
        properties.put("device_type", Map.of("type", "integer", "description", "Filter by device type, 0 = all (for list_codes)"));
        properties.put("code", Map.of("type", "string", "description", "HMS error code (for inject)"));
        properties.put("level", Map.of("type", "integer", "description", "0=tip/1=warn/2=error (for inject)"));
        properties.put("module", Map.of("type", "integer", "description", "Module id (for inject)"));
        properties.put("device_type_arg", Map.of("type", "integer", "description", "Device type for the injection payload (for inject)"));
        properties.put("imminent", Map.of("type", "boolean", "description", "Imminent flag (for inject)"));
        properties.put("in_the_sky", Map.of("type", "boolean", "description", "In-the-sky flag (for inject)"));
        properties.put("args_json", Map.of("type", "string", "description", "JSON-serialized extra args (for inject)"));
        properties.put("expires_at_ms", Map.of("type", "integer", "description", "Expiry unix ms, 0 = never (for inject)"));
        properties.put("period_ms", Map.of("type", "integer", "description", "Injector resend period ms (for inject/batch_inject)"));
        properties.put("injections", Map.of("type", "array", "description", "List of injection payloads (for batch_inject)"));
        properties.put("preset_name", Map.of("type", "string", "description", "Preset scenario name (for inject_preset)"));
        properties.put("id", Map.of("type", "string", "description", "HMS injection id (for cancel)"));
        return Map.copyOf(properties);
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> args) {
        String action = getStringArg(args, "action", "");
        return switch (action) {
            case "list" -> executeList(args);
            case "list_active_blockers" -> executeListActiveBlockers(args);
            case "list_codes" -> executeListCodes(args);
            case "list_presets" -> executeListPresets();
            case "list_active" -> executeListActive(args);
            case "inject" -> executeInject(args);
            case "batch_inject" -> executeBatchInject(args);
            case "inject_preset" -> executeInjectPreset(args);
            case "cancel" -> executeCancel(args);
            case "cancel_all" -> executeCancelAll(args);
            default -> errorResult("Unknown action: " + action,
                    "Use one of: list, list_active_blockers, list_codes, list_presets, list_active, "
                            + "inject, batch_inject, inject_preset, cancel, cancel_all");
        };
    }

    private Map<String, Object> executeList(Map<String, Object> args) {
        String sn = requireSn(args);
        if (sn == null) {
            return errorResult("Missing sn", "Provide the target device SN.");
        }
        return callSimulator(() -> simulatorClient.get(HMS_BASE_PATH + "/" + sn), "Listed HMS entries for " + sn);
    }

    private Map<String, Object> executeListActiveBlockers(Map<String, Object> args) {
        String sn = requireSn(args);
        if (sn == null) {
            return errorResult("Missing sn", "Provide the target device SN.");
        }
        return callSimulator(() -> simulatorClient.get(HMS_BASE_PATH + "/" + sn + "/blockers"),
                "Listed active blockers for " + sn);
    }

    private Map<String, Object> executeListCodes(Map<String, Object> args) {
        int deviceType = getIntArg(args, "device_type", 0);
        String query = deviceType != 0 ? "?device_type=" + deviceType : "";
        return callSimulator(() -> simulatorClient.get(HMS_BASE_PATH + ":codes" + query), "Listed HMS code dictionary");
    }

    private Map<String, Object> executeListPresets() {
        return callSimulator(() -> simulatorClient.get(HMS_BASE_PATH + ":presets"), "Listed HMS presets");
    }

    private Map<String, Object> executeListActive(Map<String, Object> args) {
        Object sns = args.get("sns");
        if (!(sns instanceof List<?> snList) || snList.isEmpty()) {
            return errorResult("Missing sns", "Provide a non-empty list of SNs to query.");
        }
        StringBuilder query = new StringBuilder("?");
        for (int i = 0; i < snList.size(); i++) {
            if (i > 0) {
                query.append('&');
            }
            query.append("sns=").append(snList.get(i));
        }
        String path = HMS_BASE_PATH + ":active" + query;
        return callSimulator(() -> simulatorClient.get(path), "Listed active HMS injections");
    }

    private Map<String, Object> executeInject(Map<String, Object> args) {
        Map<String, Object> denied = enforceSimSecurityLevel(SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }
        String sn = requireSn(args);
        if (sn == null) {
            return errorResult("Missing sn", "Provide the target device SN.");
        }
        String code = getStringArg(args, "code", null);
        if (code == null || code.isBlank()) {
            return errorResult("Missing code", "Provide the HMS error code to inject.");
        }
        Map<String, Object> body = new HashMap<>();
        body.put("code", code);
        body.put("device_type", getIntArg(args, "device_type_arg", 0));
        body.put("level", getIntArg(args, "level", 0));
        body.put("imminent", getBooleanArg(args, "imminent"));
        body.put("in_the_sky", getBooleanArg(args, "in_the_sky"));
        body.put("module", getIntArg(args, "module", 0));
        body.put("args_json", getStringArg(args, "args_json", ""));
        body.put("expires_at_ms", getLongArg(args, "expires_at_ms", 0L));
        body.put("period_ms", getIntArg(args, "period_ms", 0));
        Map<String, Object> finalBody = Map.copyOf(body);
        return callSimulator(() -> simulatorClient.post(HMS_BASE_PATH + "/" + sn + "/inject", finalBody),
                "Injected HMS code " + code + " for " + sn);
    }

    private Map<String, Object> executeBatchInject(Map<String, Object> args) {
        Map<String, Object> denied = enforceSimSecurityLevel(SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }
        String sn = requireSn(args);
        if (sn == null) {
            return errorResult("Missing sn", "Provide the target device SN.");
        }
        Object injections = args.get("injections");
        if (!(injections instanceof List<?> injectionsList) || injectionsList.isEmpty()) {
            return errorResult("Missing injections", "Provide a non-empty list of injection payloads.");
        }
        int periodMs = getIntArg(args, "period_ms", 0);
        Map<String, Object> body = Map.of("injections", injections, "period_ms", periodMs);
        return callSimulator(() -> simulatorClient.post(DEVICES_BASE_PATH + "/" + sn + "/hms:batch", body),
                "Batch-injected " + injectionsList.size() + " HMS fault(s) for " + sn);
    }

    private Map<String, Object> executeInjectPreset(Map<String, Object> args) {
        Map<String, Object> denied = enforceSimSecurityLevel(SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }
        String sn = requireSn(args);
        if (sn == null) {
            return errorResult("Missing sn", "Provide the target device SN.");
        }
        String presetName = getStringArg(args, "preset_name", null);
        if (presetName == null || presetName.isBlank()) {
            return errorResult("Missing preset_name", "Provide the preset scenario name.");
        }
        String path = DEVICES_BASE_PATH + "/" + sn + "/hms/presets/" + presetName + ":inject";
        return callSimulator(() -> simulatorClient.post(path, null), "Injected preset " + presetName + " for " + sn);
    }

    private Map<String, Object> executeCancel(Map<String, Object> args) {
        Map<String, Object> denied = enforceSimSecurityLevel(SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }
        String sn = requireSn(args);
        if (sn == null) {
            return errorResult("Missing sn", "Provide the target device SN.");
        }
        String id = getStringArg(args, "id", null);
        if (id == null || id.isBlank()) {
            return errorResult("Missing id", "Provide the HMS injection id to cancel.");
        }
        return callSimulator(() -> simulatorClient.delete(HMS_BASE_PATH + "/" + sn + "/" + id),
                "Cancelled HMS injection " + id + " for " + sn);
    }

    private Map<String, Object> executeCancelAll(Map<String, Object> args) {
        Map<String, Object> denied = enforceSimSecurityLevel(SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }
        String sn = requireSn(args);
        if (sn == null) {
            return errorResult("Missing sn", "Provide the target device SN.");
        }
        return callSimulator(() -> simulatorClient.delete(HMS_BASE_PATH + "/" + sn),
                "Cancelled all active HMS injections for " + sn);
    }
}
