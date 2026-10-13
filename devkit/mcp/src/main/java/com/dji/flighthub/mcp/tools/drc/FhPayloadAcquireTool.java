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

package com.dji.flighthub.mcp.tools.drc;

import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthubsdk.core.ControlAuthModule;
import com.dji.flighthubsdk.core.Drone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * fh_payload_acquire — 获取负载（相机/云台）控制权。
 * 负载控制权独立于飞行控制权，用于相机操作、云台控制等场景。
 * 操作完成后必须调用 fh_payload_release 释放。
 *
 * <p>安全等级：L1（可逆操作）
 * <p>前置条件：session 有效 + 飞行控制权已获取
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhPayloadAcquireTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhPayloadAcquireTool.class);

    private static final String TOOL_NAME = "fh_payload_acquire";
    private static final String PARAM_DRONE_SN = "drone_sn";
    private static final String PARAM_PAYLOAD_INDEX = "payload_index";
    private static final String DEFAULT_PAYLOAD_INDEX = "81-0-0";

    private final SdkService sdkService;

    public FhPayloadAcquireTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

    @Override
    public SecurityLevel getSecurityLevel() {
        return SecurityLevel.REVERSIBLE;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description",
                "Acquire payload (camera/gimbal) control authority for a drone. "
                        + "Payload control is independent of flight control — required for camera operations. "
                        + "Prerequisites: flight control authority must be acquired first (fh_control_acquire). "
                        + "Must release with fh_payload_release after operations complete.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_DRONE_SN, Map.of(
                "type", "string",
                "description", "Target drone serial number (SN)"
        ));
        props.put(PARAM_PAYLOAD_INDEX, Map.of(
                "type", "string",
                "description", "Payload index (e.g. '81-0-0' for primary camera). "
                        + "Use fh_camera(get_camera_state) to discover available payloads."
        ));
        schema.put("properties", props);
        schema.put("required", List.of(PARAM_DRONE_SN));

        def.put("inputSchema", schema);
        return def;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> args) {
        SessionContext session = requireSession();
        if (session == null) {
            return noSessionError();
        }

        Map<String, Object> denied = enforceToolSecurityLevel();
        if (denied != null) {
            return denied;
        }

        String droneSn = getStringArg(args, PARAM_DRONE_SN, null);
        if (droneSn == null) {
            return errorResult("Missing required parameter: drone_sn",
                    "Provide drone_sn. Use fh_device_list to get available drone SNs.");
        }

        // 前置条件：必须持有该设备的飞行控制权
        if (!session.hasControlOf(droneSn)) {
            return errorResult("Flight control authority not acquired for: " + droneSn,
                    "Call fh_control_acquire(drone_sn='" + droneSn + "') first.");
        }

        String payloadIndex = getStringArg(args, PARAM_PAYLOAD_INDEX, DEFAULT_PAYLOAD_INDEX);

        // 幂等检查
        if (session.hasPayloadAuthorityOf(droneSn, payloadIndex)) {
            return errorResult("Already holding payload control of " + droneSn + " [" + payloadIndex + "]",
                    "You already have payload authority. Proceed with camera/gimbal operations.");
        }

        // 如果持有其他 payload 的控制权，需先释放
        if (session.hasPayloadAuthority()) {
            String currentSn = session.getActivePayloadDeviceSn();
            String currentIdx = session.getActivePayloadIndex();
            return errorResult("Already controlling payload: " + currentSn + " [" + currentIdx + "]",
                    "Call fh_payload_release first, then acquire for " + droneSn + " [" + payloadIndex + "].");
        }

        if (sdkService.isMockMode()) {
            return acquireInMockMode(session, droneSn, payloadIndex);
        }

        return acquireWithRealSdk(session, droneSn, payloadIndex);
    }

    private Map<String, Object> acquireInMockMode(SessionContext session, String droneSn, String payloadIndex) {
        session.setActivePayloadDevice(droneSn, payloadIndex);
        log.info("[MOCK] Payload control authority acquired: drone={}, payload={}", droneSn, payloadIndex);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("drone_sn", droneSn);
        data.put("payload_index", payloadIndex);
        data.put("granted", true);
        data.put("_mock", true);

        Map<String, Object> result = successResult(data,
                "[MOCK] Payload control authority granted for " + droneSn + " [" + payloadIndex + "].");
        result.put("warning", "Must call fh_payload_release after operations complete.");
        result.put("next_steps", List.of(
                "Use fh_camera or fh_payload for camera/gimbal operations",
                "Call fh_payload_release(drone_sn='" + droneSn + "') after operations complete"
        ));
        return result;
    }

    @Generated
    private Map<String, Object> acquireWithRealSdk(SessionContext session, String droneSn, String payloadIndex) {
        try {
            Drone drone = sdkService.getDrone(droneSn);
            if (drone == null) {
                return errorResult("Drone instance not found: " + droneSn,
                        "Ensure drone is online. Use fh_device_list to check.");
            }

            ControlAuthModule authModule = drone.getControlAuthModule();
            SdkCallbackBridge.awaitVoid(cb -> authModule.acquirePayloadControlAuthority(payloadIndex, cb));

            session.setActivePayloadDevice(droneSn, payloadIndex);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("drone_sn", droneSn);
            data.put("payload_index", payloadIndex);
            data.put("granted", true);

            Map<String, Object> result = successResult(data,
                    "Payload control authority granted for " + droneSn + " [" + payloadIndex + "].");
            result.put("warning", "Must call fh_payload_release after operations complete.");
            result.put("next_steps", List.of(
                    "Use fh_camera or fh_payload for camera/gimbal operations",
                    "Call fh_payload_release(drone_sn='" + droneSn + "') after operations complete"
            ));
            return result;

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("fh_payload_acquire timeout: drone={}, payload={}: {}", droneSn, payloadIndex, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(),
                    "Device may be unreachable. Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("fh_payload_acquire SDK error: drone={}, payload={}: [{}] {}",
                    droneSn, payloadIndex, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(),
                    "Check drone state and retry.");
        } catch (Exception e) {
            log.error("fh_payload_acquire error: drone={}, payload={}: {}", droneSn, payloadIndex, e.getMessage(), e);
            return errorResult("Failed: " + e.getMessage(),
                    "Check drone connectivity and try again.");
        }
    }
}
