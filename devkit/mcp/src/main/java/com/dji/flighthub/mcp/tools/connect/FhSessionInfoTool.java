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

package com.dji.flighthub.mcp.tools.connect;

import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * fh_session_info — 查询当前 session 状态。
 * 返回 workspace 信息、已连接设备、控制权状态和过期时间。
 */
@Component
public class FhSessionInfoTool extends AbstractMcpTool {

    public FhSessionInfoTool(SessionManager sessionManager) {
        super(sessionManager);
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", "fh_session_info");
        def.put("description", "Query current session status: workspace, devices, control authority, expiry.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", Map.of());
        def.put("inputSchema", schema);
        return def;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> args) {
        SessionContext session = requireSession();
        if (session == null) {
            return noSessionError();
        }

        Duration uptime = Duration.between(session.getCreatedAt(), Instant.now());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("session_id", session.getSessionId());
        data.put("workspace_uuid", session.getWorkspaceUuid());
        data.put("workspace_name", session.getWorkspaceName());
        data.put("max_security_level", "L" + session.getMaxSecurityLevel());
        data.put("uptime_minutes", uptime.toMinutes());
        data.put("control_authority", session.hasControlAuthority()
                ? Map.of("active", true, "device_sn", session.getActiveControlDeviceSn())
                : Map.of("active", false));
        data.put("drc_link", session.hasDrcLink()
                ? Map.of("active", true, "device_sn", session.getActiveDrcDeviceSn())
                : Map.of("active", false));
        data.put("active_subscriptions", session.getActiveListenIds().size());
        data.put("last_activity", session.getLastActivityAt().toString());

        return successResult(data,
                String.format("Session active for %d minutes. Workspace: %s.",
                        uptime.toMinutes(), session.getWorkspaceName()));
    }
}
