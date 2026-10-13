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
 * fh_payload_release — 释放负载（相机/云台）控制权。
 * 无论操作是否成功，都必须在操作完成后调用此工具释放负载控制权。
 *
 * <p>安全等级：L1（释放操作）
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhPayloadReleaseTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhPayloadReleaseTool.class);

    private static final String TOOL_NAME = "fh_payload_release";
    private static final String PARAM_DRONE_SN = "drone_sn";
    private static final String PARAM_PAYLOAD_INDEX = "payload_index";

    private final SdkService sdkService;

    public FhPayloadReleaseTool(SessionManager sessionManager, SdkService sdkService) {
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
                "Release payload (camera/gimbal) control authority. "
                        + "Always call this after camera/gimbal operations complete, even on failure paths.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_DRONE_SN, Map.of(
                "type", "string",
                "description", "Target drone serial number (SN) to release payload control for"
        ));
        props.put(PARAM_PAYLOAD_INDEX, Map.of(
                "type", "string",
                "description", "Payload index to release (e.g. '81-0-0'). "
                        + "If omitted, releases the currently held payload."
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

        String droneSn = getStringArg(args, PARAM_DRONE_SN, null);
        if (droneSn == null) {
            return errorResult("Missing required parameter: drone_sn",
                    "Provide drone_sn of the drone to release payload control from.");
        }

        // 检查是否持有负载控制权
        if (!session.hasPayloadAuthority()) {
            return errorResult("No payload control authority held",
                    "You do not hold payload control authority. Nothing to release.");
        }

        String currentSn = session.getActivePayloadDeviceSn();
        if (!droneSn.equals(currentSn)) {
            return errorResult("Not controlling payload of drone: " + droneSn,
                    "Currently controlling payload of " + currentSn + ". Provide correct drone_sn.");
        }

        // 使用 session 中记录的 payloadIndex，或参数中指定的
        String payloadIndex = getStringArg(args, PARAM_PAYLOAD_INDEX, session.getActivePayloadIndex());

        if (sdkService.isMockMode()) {
            return releaseInMockMode(session, droneSn, payloadIndex);
        }

        return releaseWithRealSdk(session, droneSn, payloadIndex);
    }

    private Map<String, Object> releaseInMockMode(SessionContext session, String droneSn, String payloadIndex) {
        session.clearPayloadDevice();
        log.info("[MOCK] Payload control authority released: drone={}, payload={}", droneSn, payloadIndex);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("drone_sn", droneSn);
        data.put("payload_index", payloadIndex);
        data.put("released", true);
        data.put("_mock", true);

        return successResult(data,
                "[MOCK] Payload control authority released for " + droneSn + " [" + payloadIndex + "].");
    }

    @Generated
    private Map<String, Object> releaseWithRealSdk(SessionContext session, String droneSn, String payloadIndex) {
        try {
            Drone drone = sdkService.getDrone(droneSn);
            boolean droneOffline = (drone == null);

            if (!droneOffline) {
                ControlAuthModule authModule = drone.getControlAuthModule();
                SdkCallbackBridge.awaitVoid(cb -> authModule.releasePayloadControlAuthority(payloadIndex, cb));
            }

            session.clearPayloadDevice();

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("drone_sn", droneSn);
            data.put("payload_index", payloadIndex);
            data.put("released", true);
            if (droneOffline) {
                data.put("_warning", "Drone offline — local state cleared but server-side authority may still be held");
                log.warn("fh_payload_release: drone {} offline, clearing local state only", droneSn);
            }

            return successResult(data,
                    "Payload control authority released for " + droneSn + " [" + payloadIndex + "].");

        } catch (Exception e) {
            log.error("fh_payload_release SDK error: drone={}, payload={}: {}, clearing local state anyway",
                    droneSn, payloadIndex, e.getMessage(), e);
            session.clearPayloadDevice();
            return errorResult("SDK error during release: " + e.getMessage(),
                    "Local payload state cleared. Verify drone status before next operation.");
        }
    }
}
