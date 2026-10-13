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
 * 封装 pf-device-simulator-service 的飞行状态注入与 FlightService 物理简模控制。
 *
 * <p>覆盖 DeviceService 的 InjectState/InjectEvent/UpdateFlightState/ResetFlight，以及
 * FlightService 全部 8 个 RPC。全部为 L1，无需 confirm（均可通过 reset 类接口撤销）。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class SimFlightStateTool extends AbstractSimTool {

    private static final String DEVICES_BASE_PATH = "/simulator/api/v1/devices";
    private static final String DRONES_BASE_PATH = "/simulator/api/v1/drones";

    private final SimulatorClient simulatorClient;

    public SimFlightStateTool(SessionManager sessionManager, SimulatorClient simulatorClient,
            FlightHubProperties flightHubProperties, SimulatorProperties simulatorProperties) {
        super(sessionManager, flightHubProperties, simulatorProperties);
        this.simulatorClient = simulatorClient;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        return Map.of(
                "name", "sim_flight_state",
                "description", "Inject state/events and drive the physical flight simulation engine "
                        + "for a simulated drone: get_flight_state/inject_state/inject_event/update_flight_state/"
                        + "reset_flight/takeoff/return_home/land/flyto/manual/patch_state/reset",
                "inputSchema", Map.of(
                        "type", "object",
                        "properties", buildProperties(),
                        "required", List.of("action", "sn")));
    }

    private Map<String, Object> buildProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("action", Map.of(
                "type", "string",
                "enum", List.of("get_flight_state", "inject_state", "inject_event", "update_flight_state",
                        "reset_flight", "takeoff", "return_home", "land", "flyto", "manual", "patch_state", "reset"),
                "description", "The flight-state action to perform"));
        properties.put("sn", Map.of("type", "string", "description", "Target drone serial number"));
        properties.put("data_json", Map.of("type", "string", "description", "JSON-serialized state fields (for inject_state)"));
        properties.put("method", Map.of("type", "string", "description", "MQTT method name to simulate (for inject_event)"));
        properties.put("event_data_json", Map.of("type", "string", "description", "JSON-serialized event payload (for inject_event)"));
        properties.put("patch_json", Map.of("type", "string", "description", "JSON-serialized partial state patch (for update_flight_state)"));
        properties.put("latitude", Map.of("type", "number", "description", "Target latitude (for flyto)"));
        properties.put("longitude", Map.of("type", "number", "description", "Target longitude (for flyto)"));
        properties.put("altitude", Map.of("type", "number", "description", "Target ellipsoid altitude (for flyto)"));
        properties.put("max_speed", Map.of("type", "number", "description", "Max flight speed, 0 = use config default (for flyto)"));
        properties.put("yaw", Map.of("type", "integer", "description", "Target yaw in degrees (optional, for flyto)"));
        properties.put("x", Map.of("type", "number", "description", "Forward/backward speed m/s (for manual)"));
        properties.put("y", Map.of("type", "number", "description", "Left/right speed m/s (for manual)"));
        properties.put("h", Map.of("type", "number", "description", "Vertical speed m/s (for manual)"));
        properties.put("w", Map.of("type", "number", "description", "Yaw angular speed deg/s (for manual)"));
        properties.put("duration_ms", Map.of("type", "integer", "description", "Stick-push duration in ms (for manual)"));
        properties.put("battery", Map.of("type", "integer", "description", "Battery percent 0-100 (for patch_state)"));
        properties.put("power_on", Map.of("type", "boolean", "description", "Power-on flag (for patch_state)"));
        properties.put("attitude_head", Map.of("type", "integer", "description", "Attitude heading degrees (for patch_state)"));
        properties.put("wireless_link", Map.of("type", "integer", "description", "Wireless link signal 0-5 (for patch_state)"));
        properties.put("gps_count", Map.of("type", "integer", "description", "GPS satellite count 0-32 (for patch_state)"));
        properties.put("gps_status", Map.of("type", "integer", "description", "GPS status 0=none/1=fixed/2=RTK (for patch_state)"));
        return Map.copyOf(properties);
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> args) {
        String sn = requireSn(args);
        if (sn == null) {
            return errorResult("Missing sn", "Provide the target drone SN.");
        }
        String action = getStringArg(args, "action", "");
        return switch (action) {
            case "get_flight_state" -> executeGetFlightState(sn);
            case "inject_state" -> executeInjectState(args, sn);
            case "inject_event" -> executeInjectEvent(args, sn);
            case "update_flight_state" -> executeUpdateFlightState(args, sn);
            case "reset_flight" -> executeResetFlight(sn);
            case "takeoff" -> executeTakeoff(sn);
            case "return_home" -> executeReturnHome(sn);
            case "land" -> executeLand(sn);
            case "flyto" -> executeFlyto(args, sn);
            case "manual" -> executeManual(args, sn);
            case "patch_state" -> executePatchState(args, sn);
            case "reset" -> executeReset(sn);
            default -> errorResult("Unknown action: " + action,
                    "Use one of: get_flight_state, inject_state, inject_event, update_flight_state, reset_flight, "
                            + "takeoff, return_home, land, flyto, manual, patch_state, reset");
        };
    }

    private Map<String, Object> executeGetFlightState(String sn) {
        return callSimulator(() -> simulatorClient.get(DRONES_BASE_PATH + "/" + sn + "/flight/state"),
                "Fetched flight state for " + sn);
    }

    private Map<String, Object> executeInjectState(Map<String, Object> args, String sn) {
        Map<String, Object> denied = enforceSimSecurityLevel(SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }
        String dataJson = getStringArg(args, "data_json", null);
        if (dataJson == null || dataJson.isBlank()) {
            return errorResult("Missing data_json", "Provide a JSON-serialized state payload.");
        }
        Map<String, Object> body = Map.of("data_json", dataJson);
        return callSimulator(() -> simulatorClient.post(DEVICES_BASE_PATH + "/" + sn + "/state", body),
                "Injected state for " + sn);
    }

    private Map<String, Object> executeInjectEvent(Map<String, Object> args, String sn) {
        Map<String, Object> denied = enforceSimSecurityLevel(SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }
        String method = getStringArg(args, "method", null);
        if (method == null || method.isBlank()) {
            return errorResult("Missing method", "Provide the MQTT method name to simulate.");
        }
        String dataJson = getStringArg(args, "event_data_json", "");
        Map<String, Object> body = Map.of("method", method, "data_json", dataJson);
        return callSimulator(() -> simulatorClient.post(DEVICES_BASE_PATH + "/" + sn + "/event", body),
                "Injected event " + method + " for " + sn);
    }

    private Map<String, Object> executeUpdateFlightState(Map<String, Object> args, String sn) {
        Map<String, Object> denied = enforceSimSecurityLevel(SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }
        String patchJson = getStringArg(args, "patch_json", null);
        if (patchJson == null || patchJson.isBlank()) {
            return errorResult("Missing patch_json", "Provide a JSON-serialized FlightStatePatch payload.");
        }
        Map<String, Object> body = Map.of("patch_json", patchJson);
        return callSimulator(() -> simulatorClient.patch(DEVICES_BASE_PATH + "/" + sn + "/flight-state", body),
                "Updated flight state for " + sn);
    }

    private Map<String, Object> executeResetFlight(String sn) {
        Map<String, Object> denied = enforceSimSecurityLevel(SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }
        return callSimulator(() -> simulatorClient.post(DEVICES_BASE_PATH + "/" + sn + "/reset-flight", null),
                "Reset flight for " + sn);
    }

    private Map<String, Object> executeTakeoff(String sn) {
        Map<String, Object> denied = enforceSimSecurityLevel(SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }
        return callSimulator(() -> simulatorClient.post(DRONES_BASE_PATH + "/" + sn + "/takeoff", null),
                "Takeoff triggered for " + sn);
    }

    private Map<String, Object> executeReturnHome(String sn) {
        Map<String, Object> denied = enforceSimSecurityLevel(SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }
        return callSimulator(() -> simulatorClient.post(DRONES_BASE_PATH + "/" + sn + "/return-home", null),
                "Return-home triggered for " + sn);
    }

    private Map<String, Object> executeLand(String sn) {
        Map<String, Object> denied = enforceSimSecurityLevel(SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }
        return callSimulator(() -> simulatorClient.post(DRONES_BASE_PATH + "/" + sn + "/land", null),
                "Land triggered for " + sn);
    }

    private Map<String, Object> executeFlyto(Map<String, Object> args, String sn) {
        Map<String, Object> denied = enforceSimSecurityLevel(SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }
        if (!args.containsKey("latitude") || !args.containsKey("longitude") || !args.containsKey("altitude")) {
            return errorResult("Missing coordinates", "Provide latitude, longitude, and altitude.");
        }
        double latitude = getDoubleArg(args, "latitude", 0.0);
        double longitude = getDoubleArg(args, "longitude", 0.0);
        double altitude = getDoubleArg(args, "altitude", 0.0);
        double maxSpeed = getDoubleArg(args, "max_speed", 0.0);
        boolean setYaw = args.containsKey("yaw");
        int yaw = getIntArg(args, "yaw", 0);
        Map<String, Object> body = new HashMap<>();
        body.put("latitude", latitude);
        body.put("longitude", longitude);
        body.put("altitude", altitude);
        body.put("max_speed", maxSpeed);
        body.put("set_yaw", setYaw);
        body.put("yaw", yaw);
        return callSimulator(() -> simulatorClient.post(DRONES_BASE_PATH + "/" + sn + "/flyto", Map.copyOf(body)),
                "Flyto triggered for " + sn);
    }

    private Map<String, Object> executeManual(Map<String, Object> args, String sn) {
        Map<String, Object> denied = enforceSimSecurityLevel(SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }
        Map<String, Object> body = Map.of(
                "x", getDoubleArg(args, "x", 0.0),
                "y", getDoubleArg(args, "y", 0.0),
                "h", getDoubleArg(args, "h", 0.0),
                "w", getDoubleArg(args, "w", 0.0),
                "duration_ms", getIntArg(args, "duration_ms", 0));
        return callSimulator(() -> simulatorClient.post(DRONES_BASE_PATH + "/" + sn + "/manual", body),
                "Manual stick input applied for " + sn);
    }

    private Map<String, Object> executePatchState(Map<String, Object> args, String sn) {
        Map<String, Object> denied = enforceSimSecurityLevel(SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }
        Map<String, Object> body = new HashMap<>();
        boolean hasAnyField = false;

        boolean setBattery = args.containsKey("battery");
        body.put("set_battery", setBattery);
        body.put("battery", getIntArg(args, "battery", 0));
        hasAnyField = hasAnyField || setBattery;

        boolean setPowerOn = args.containsKey("power_on");
        body.put("set_power_on", setPowerOn);
        body.put("power_on", getBooleanArg(args, "power_on"));
        hasAnyField = hasAnyField || setPowerOn;

        boolean setPosition = args.containsKey("latitude") && args.containsKey("longitude") && args.containsKey("altitude");
        body.put("set_position", setPosition);
        body.put("latitude", getDoubleArg(args, "latitude", 0.0));
        body.put("longitude", getDoubleArg(args, "longitude", 0.0));
        body.put("altitude", getDoubleArg(args, "altitude", 0.0));
        hasAnyField = hasAnyField || setPosition;

        boolean setAttitudeHead = args.containsKey("attitude_head");
        body.put("set_attitude_head", setAttitudeHead);
        body.put("attitude_head", getIntArg(args, "attitude_head", 0));
        hasAnyField = hasAnyField || setAttitudeHead;

        boolean setWirelessLink = args.containsKey("wireless_link");
        body.put("set_wireless_link", setWirelessLink);
        body.put("wireless_link", getIntArg(args, "wireless_link", 0));
        hasAnyField = hasAnyField || setWirelessLink;

        boolean setGps = args.containsKey("gps_count") || args.containsKey("gps_status");
        body.put("set_gps", setGps);
        body.put("gps_count", getIntArg(args, "gps_count", 0));
        body.put("gps_status", getIntArg(args, "gps_status", 0));
        hasAnyField = hasAnyField || setGps;

        if (!hasAnyField) {
            return errorResult("No fields to patch",
                    "Provide at least one of: battery, power_on, latitude+longitude+altitude, "
                            + "attitude_head, wireless_link, gps_count/gps_status.");
        }
        Map<String, Object> finalBody = Map.copyOf(body);
        return callSimulator(() -> simulatorClient.patch(DRONES_BASE_PATH + "/" + sn + "/flight/state", finalBody),
                "Patched flight state for " + sn);
    }

    private Map<String, Object> executeReset(String sn) {
        Map<String, Object> denied = enforceSimSecurityLevel(SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }
        return callSimulator(() -> simulatorClient.post(DRONES_BASE_PATH + "/" + sn + "/flight/reset", null),
                "Flight reset for " + sn);
    }
}
