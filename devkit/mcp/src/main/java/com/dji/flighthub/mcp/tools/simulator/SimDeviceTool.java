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

import java.util.List;
import java.util.Map;

/**
 * 封装 pf-device-simulator-service DeviceService，管理模拟设备的生命周期。
 *
 * <p>安全等级：list/get 为 L0；create/update/start/stop/batch_create/sub_online/sub_offline 为 L1
 * （可撤销，不额外确认）；delete/batch_delete 为 L1 且不可逆，需要 {@code confirm=true}。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class SimDeviceTool extends AbstractSimTool {

    private static final String BASE_PATH = "/simulator/api/v1/devices";

    private final SimulatorClient simulatorClient;

    public SimDeviceTool(SessionManager sessionManager, SimulatorClient simulatorClient,
            FlightHubProperties flightHubProperties, SimulatorProperties simulatorProperties) {
        super(sessionManager, flightHubProperties, simulatorProperties);
        this.simulatorClient = simulatorClient;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        return Map.of(
                "name", "sim_device",
                "description", "Manage simulated devices in pf-device-simulator-service: "
                        + "list/get/create/update/delete/start/stop/batch_create/batch_delete/sub_online/sub_offline",
                "inputSchema", Map.of(
                        "type", "object",
                        "properties", Map.ofEntries(
                                Map.entry("action", Map.of(
                                        "type", "string",
                                        "enum", List.of("list", "get", "create", "update", "delete", "start", "stop",
                                                "batch_create", "batch_delete", "sub_online", "sub_offline"),
                                        "description", "The device management action to perform")),
                                Map.entry("sn", Map.of("type", "string", "description", "Target device serial number")),
                                Map.entry("sub_sn", Map.of("type", "string", "description", "Sub-device serial number (for sub_online/sub_offline)")),
                                Map.entry("product_type", Map.of("type", "string", "description", "Filter by product type (for list)")),
                                Map.entry("status", Map.of("type", "string", "description", "Filter by status (for list)")),
                                Map.entry("device", Map.of("type", "object", "description", "Device payload (for create/update)")),
                                Map.entry("devices", Map.of("type", "array", "description", "List of device payloads (for batch_create)")),
                                Map.entry("auto_start", Map.of("type", "boolean", "description", "Auto-start devices after batch_create")),
                                Map.entry("sns", Map.of("type", "array", "description", "List of SNs (for batch_delete)")),
                                Map.entry("confirm", Map.of("type", "boolean", "description", "Required true for delete/batch_delete")),
                                Map.entry("dry_run", Map.of("type", "boolean", "description", "Preview the action without executing it"))),
                        "required", List.of("action")));
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> args) {
        String action = getStringArg(args, "action", "");
        return switch (action) {
            case "list" -> executeList(args);
            case "get" -> executeGet(args);
            case "create" -> executeCreate(args);
            case "update" -> executeUpdate(args);
            case "delete" -> executeDelete(args);
            case "start" -> executeStart(args);
            case "stop" -> executeStop(args);
            case "batch_create" -> executeBatchCreate(args);
            case "batch_delete" -> executeBatchDelete(args);
            case "sub_online" -> executeSubOnline(args);
            case "sub_offline" -> executeSubOffline(args);
            default -> errorResult("Unknown action: " + action,
                    "Use one of: list, get, create, update, delete, start, stop, batch_create, batch_delete, sub_online, sub_offline");
        };
    }

    private Map<String, Object> executeList(Map<String, Object> args) {
        StringBuilder query = new StringBuilder();
        appendQueryParam(query, "product_type", getStringArg(args, "product_type", null));
        appendQueryParam(query, "status", getStringArg(args, "status", null));
        String path = BASE_PATH + query;
        return callSimulator(() -> simulatorClient.get(path), "Listed devices");
    }

    private void appendQueryParam(StringBuilder query, String key, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        query.append(query.isEmpty() ? '?' : '&').append(key).append('=').append(value);
    }

    private Map<String, Object> executeGet(Map<String, Object> args) {
        String sn = requireSn(args);
        if (sn == null) {
            return errorResult("Missing sn", "Provide the target device SN.");
        }
        return callSimulator(() -> simulatorClient.get(BASE_PATH + "/" + sn), "Fetched device " + sn);
    }

    private Map<String, Object> executeCreate(Map<String, Object> args) {
        Map<String, Object> denied = enforceSimSecurityLevel(SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }
        Object device = args.get("device");
        if (!(device instanceof Map)) {
            return errorResult("Missing device", "Provide a device object with at least sn and product_type.");
        }
        Map<String, Object> body = Map.of("device", device);
        return callSimulator(() -> simulatorClient.post(BASE_PATH, body), "Created device");
    }

    private Map<String, Object> executeUpdate(Map<String, Object> args) {
        Map<String, Object> denied = enforceSimSecurityLevel(SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }
        String sn = requireSn(args);
        if (sn == null) {
            return errorResult("Missing sn", "Provide the target device SN.");
        }
        Object device = args.get("device");
        if (!(device instanceof Map)) {
            return errorResult("Missing device", "Provide a device object with the fields to update.");
        }
        Map<String, Object> body = Map.of("device", device);
        return callSimulator(() -> simulatorClient.patch(BASE_PATH + "/" + sn, body), "Updated device " + sn);
    }

    private Map<String, Object> executeDelete(Map<String, Object> args) {
        Map<String, Object> denied = enforceSimSecurityLevel(SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }
        String sn = requireSn(args);
        if (sn == null) {
            return errorResult("Missing sn", "Provide the target device SN.");
        }
        if (isDryRun(args)) {
            return dryRunResult("Would delete device " + sn + " (irreversible)",
                    buildDryRunPreview("sim_device", "delete", sn, Map.of(),
                            List.of(), SecurityLevel.REVERSIBLE, true));
        }
        if (!getBooleanArg(args, "confirm")) {
            return errorResult("Confirmation required",
                    "About to delete device " + sn + ". This action cannot be undone. Pass confirm=true.");
        }
        return callSimulator(() -> simulatorClient.delete(BASE_PATH + "/" + sn), "Deleted device " + sn);
    }

    private Map<String, Object> executeStart(Map<String, Object> args) {
        Map<String, Object> denied = enforceSimSecurityLevel(SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }
        String sn = requireSn(args);
        if (sn == null) {
            return errorResult("Missing sn", "Provide the target device SN.");
        }
        return callSimulator(() -> simulatorClient.post(BASE_PATH + "/" + sn + "/start", null), "Started device " + sn);
    }

    private Map<String, Object> executeStop(Map<String, Object> args) {
        Map<String, Object> denied = enforceSimSecurityLevel(SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }
        String sn = requireSn(args);
        if (sn == null) {
            return errorResult("Missing sn", "Provide the target device SN.");
        }
        return callSimulator(() -> simulatorClient.post(BASE_PATH + "/" + sn + "/stop", null), "Stopped device " + sn);
    }

    private Map<String, Object> executeSubOnline(Map<String, Object> args) {
        Map<String, Object> denied = enforceSimSecurityLevel(SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }
        String sn = requireSn(args);
        if (sn == null) {
            return errorResult("Missing sn", "Provide the target device SN.");
        }
        String subSn = getStringArg(args, "sub_sn", null);
        if (subSn == null || subSn.isBlank()) {
            return errorResult("Missing sub_sn", "Provide the sub-device SN.");
        }
        Map<String, Object> body = Map.of("sub_sn", subSn);
        return callSimulator(() -> simulatorClient.post(BASE_PATH + "/" + sn + "/sub-online", body),
                "Sub-device " + subSn + " online under " + sn);
    }

    private Map<String, Object> executeSubOffline(Map<String, Object> args) {
        Map<String, Object> denied = enforceSimSecurityLevel(SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }
        String sn = requireSn(args);
        if (sn == null) {
            return errorResult("Missing sn", "Provide the target device SN.");
        }
        String subSn = getStringArg(args, "sub_sn", null);
        if (subSn == null || subSn.isBlank()) {
            return errorResult("Missing sub_sn", "Provide the sub-device SN.");
        }
        Map<String, Object> body = Map.of("sub_sn", subSn);
        return callSimulator(() -> simulatorClient.post(BASE_PATH + "/" + sn + "/sub-offline", body),
                "Sub-device " + subSn + " offline under " + sn);
    }

    private Map<String, Object> executeBatchCreate(Map<String, Object> args) {
        Map<String, Object> denied = enforceSimSecurityLevel(SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }
        Object devices = args.get("devices");
        if (!(devices instanceof List) || ((List<?>) devices).isEmpty()) {
            return errorResult("Missing devices", "Provide a non-empty list of device payloads.");
        }
        boolean autoStart = getBooleanArg(args, "auto_start");
        Map<String, Object> body = Map.of("devices", devices, "auto_start", autoStart);
        return callSimulator(() -> simulatorClient.post(BASE_PATH + ":batch", body),
                "Batch-created " + ((List<?>) devices).size() + " device(s)");
    }

    private Map<String, Object> executeBatchDelete(Map<String, Object> args) {
        Map<String, Object> denied = enforceSimSecurityLevel(SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }
        Object sns = args.get("sns");
        if (!(sns instanceof List) || ((List<?>) sns).isEmpty()) {
            return errorResult("Missing sns", "Provide a non-empty list of SNs to delete.");
        }
        if (isDryRun(args)) {
            return dryRunResult("Would batch-delete " + ((List<?>) sns).size() + " device(s) (irreversible)",
                    buildDryRunPreview("sim_device", "batch_delete", sns.toString(), Map.of("sns", sns),
                            List.of(), SecurityLevel.REVERSIBLE, true));
        }
        if (!getBooleanArg(args, "confirm")) {
            return errorResult("Confirmation required",
                    "About to delete " + ((List<?>) sns).size() + " device(s). This action cannot be undone. Pass confirm=true.");
        }
        Map<String, Object> body = Map.of("sns", sns);
        return callSimulator(() -> simulatorClient.post(BASE_PATH + ":batchDelete", body), "Batch-deleted devices");
    }
}
