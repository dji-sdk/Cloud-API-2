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
 * 封装 pf-device-simulator-service RecordingService + TemplateService（只读部分）。
 *
 * <p>list/get/list_candidates/list_templates/get_template 为 L0（只读）；
 * start/stop/promote 为 L1（无需 confirm）。promote 的响应必须提示需重启模拟器进程
 * （模板目录经 go:embed 在构建期打包，运行期覆盖不会立即生效）。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class SimRecordingTool extends AbstractSimTool {

    private static final String RECORDINGS_BASE_PATH = "/simulator/api/v1/recordings";
    private static final String TEMPLATES_BASE_PATH = "/simulator/api/v1/templates";

    private final SimulatorClient simulatorClient;

    public SimRecordingTool(SessionManager sessionManager, SimulatorClient simulatorClient,
            FlightHubProperties flightHubProperties, SimulatorProperties simulatorProperties) {
        super(sessionManager, flightHubProperties, simulatorProperties);
        this.simulatorClient = simulatorClient;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        return Map.of(
                "name", "sim_recording",
                "description", "Record real MQTT traffic from simulated devices and promote it into reusable "
                        + "response templates: list/get/list_candidates/list_templates/get_template/start/stop/promote",
                "inputSchema", Map.of(
                        "type", "object",
                        "properties", buildProperties(),
                        "required", List.of("action")));
    }

    private Map<String, Object> buildProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("action", Map.of(
                "type", "string",
                "enum", List.of("list", "get", "list_candidates", "list_templates", "get_template",
                        "start", "stop", "promote"),
                "description", "The recording/template action to perform"));
        properties.put("id", Map.of("type", "string",
                "description", "Recording id (for get/list_candidates/stop/promote)"));
        properties.put("product_type", Map.of("type", "string",
                "description", "Filter/target product type (for list_templates/start)"));
        properties.put("template_id", Map.of("type", "string",
                "description", "Template id, format product_type:method (for get_template)"));
        properties.put("source_sns", Map.of("type", "array",
                "description", "SNs to record traffic from (for start)"));
        properties.put("duration_sec", Map.of("type", "integer",
                "description", "Recording duration in seconds (for start)"));
        properties.put("mqtt_broker", Map.of("type", "string",
                "description", "Override MQTT broker address (optional, for start)"));
        properties.put("mqtt_username", Map.of("type", "string",
                "description", "Override MQTT username (optional, for start)"));
        properties.put("mqtt_password", Map.of("type", "string",
                "description", "Override MQTT password (optional, for start)"));
        properties.put("methods", Map.of("type", "array",
                "description", "Candidate methods to promote into templates (for promote)"));
        return Map.copyOf(properties);
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> args) {
        String action = getStringArg(args, "action", "");
        return switch (action) {
            case "list" -> executeList();
            case "get" -> executeGet(args);
            case "list_candidates" -> executeListCandidates(args);
            case "list_templates" -> executeListTemplates(args);
            case "get_template" -> executeGetTemplate(args);
            case "start" -> executeStart(args);
            case "stop" -> executeStop(args);
            case "promote" -> executePromote(args);
            default -> errorResult("Unknown action: " + action,
                    "Use one of: list, get, list_candidates, list_templates, get_template, start, stop, promote");
        };
    }

    private Map<String, Object> executeList() {
        return callSimulator(() -> simulatorClient.get(RECORDINGS_BASE_PATH), "Listed recordings");
    }

    private Map<String, Object> executeGet(Map<String, Object> args) {
        String id = getStringArg(args, "id", null);
        if (id == null || id.isBlank()) {
            return errorResult("Missing id", "Provide the recording id.");
        }
        return callSimulator(() -> simulatorClient.get(RECORDINGS_BASE_PATH + "/" + id), "Fetched recording " + id);
    }

    private Map<String, Object> executeListCandidates(Map<String, Object> args) {
        String id = getStringArg(args, "id", null);
        if (id == null || id.isBlank()) {
            return errorResult("Missing id", "Provide the recording id.");
        }
        return callSimulator(() -> simulatorClient.get(RECORDINGS_BASE_PATH + "/" + id + "/candidates"),
                "Listed promotion candidates for recording " + id);
    }

    private Map<String, Object> executeListTemplates(Map<String, Object> args) {
        String productType = getStringArg(args, "product_type", null);
        String query = (productType != null && !productType.isBlank()) ? "?product_type=" + productType : "";
        return callSimulator(() -> simulatorClient.get(TEMPLATES_BASE_PATH + query), "Listed templates");
    }

    private Map<String, Object> executeGetTemplate(Map<String, Object> args) {
        String templateId = getStringArg(args, "template_id", null);
        if (templateId == null || templateId.isBlank()) {
            return errorResult("Missing template_id", "Provide the template id (format product_type:method).");
        }
        return callSimulator(() -> simulatorClient.get(TEMPLATES_BASE_PATH + "/" + templateId),
                "Fetched template " + templateId);
    }

    private Map<String, Object> executeStart(Map<String, Object> args) {
        Map<String, Object> denied = enforceSimSecurityLevel(SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }
        Object sourceSns = args.get("source_sns");
        if (!(sourceSns instanceof List<?> sourceSnsList) || sourceSnsList.isEmpty()) {
            return errorResult("Missing source_sns", "Provide a non-empty list of SNs to record traffic from.");
        }
        String productType = getStringArg(args, "product_type", null);
        if (productType == null || productType.isBlank()) {
            return errorResult("Missing product_type", "Provide the target product type.");
        }
        Map<String, Object> body = new HashMap<>();
        body.put("source_sns", sourceSns);
        body.put("product_type", productType);
        body.put("duration_sec", getIntArg(args, "duration_sec", 0));
        body.put("mqtt_broker", getStringArg(args, "mqtt_broker", ""));
        body.put("mqtt_username", getStringArg(args, "mqtt_username", ""));
        body.put("mqtt_password", getStringArg(args, "mqtt_password", ""));
        Map<String, Object> finalBody = Map.copyOf(body);
        return callSimulator(() -> simulatorClient.post(RECORDINGS_BASE_PATH, finalBody), "Started recording");
    }

    private Map<String, Object> executeStop(Map<String, Object> args) {
        Map<String, Object> denied = enforceSimSecurityLevel(SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }
        String id = getStringArg(args, "id", null);
        if (id == null || id.isBlank()) {
            return errorResult("Missing id", "Provide the recording id.");
        }
        return callSimulator(() -> simulatorClient.post(RECORDINGS_BASE_PATH + "/" + id + "/stop", null),
                "Stopped recording " + id);
    }

    private Map<String, Object> executePromote(Map<String, Object> args) {
        Map<String, Object> denied = enforceSimSecurityLevel(SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }
        String id = getStringArg(args, "id", null);
        if (id == null || id.isBlank()) {
            return errorResult("Missing id", "Provide the recording id.");
        }
        Object methods = args.get("methods");
        if (!(methods instanceof List<?> methodsList) || methodsList.isEmpty()) {
            return errorResult("Missing methods", "Provide a non-empty list of candidate methods to promote.");
        }
        Map<String, Object> body = Map.of("methods", methods);
        String message = "Promoted " + methodsList.size() + " template(s) from recording " + id
                + ". Restart the simulator process for the promoted templates to take effect "
                + "(the template directory is embedded via go:embed at build time).";
        return callSimulator(() -> simulatorClient.post(RECORDINGS_BASE_PATH + "/" + id + "/promote", body), message);
    }
}
