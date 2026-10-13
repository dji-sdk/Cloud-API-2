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

package com.dji.flighthub.mcp.tools.airspace;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhFlysafeTool 单元测试。
 * 覆盖：getToolDefinition、无 session 报错、参数校验、Mock check/upgrade、confirm 门控。
 *
 * @author DJI FlightHub SDK Team
 */
class FhFlysafeToolTest {

    private SessionManager sessionManager;
    private FhFlysafeTool tool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);

        SdkService sdkService = new SdkService();
        tool = new FhFlysafeTool(sessionManager, sdkService);
    }

    @Test
    void getToolDefinition_shouldReturnCorrectNameAndSchema() {
        Map<String, Object> def = tool.getToolDefinition();

        assertEquals("fh_flysafe", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));

        @SuppressWarnings("unchecked")
        Map<String, Object> schema = (Map<String, Object>) def.get("inputSchema");
        @SuppressWarnings("unchecked")
        List<String> required = (List<String>) schema.get("required");
        assertTrue(required.contains("action"));
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = tool.execute(Map.of(
                "action", "check",
                "device_sn", "DRONE-001"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenActionInvalid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "delete"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid or missing action"));
    }

    // ==================== check ====================

    @Test
    void executeCheck_shouldReturnError_whenDeviceSnMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "check"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("device_sn"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeCheck_shouldReturnMockResult() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "check",
                "device_sn", "DRONE-001"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data.get("current_version"));
        assertNotNull(data.get("latest_version"));
        assertEquals(true, data.get("upgrade_available"));
        assertEquals(true, data.get("_mock"));
    }

    // ==================== upgrade ====================

    @Test
    void executeUpgrade_shouldReturnError_whenDeviceSnsMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "upgrade"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("device_sns"));
    }

    @Test
    void executeUpgrade_shouldRequireConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "upgrade");
        args.put("device_sns", List.of("DRONE-001"));

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeUpgrade_shouldSucceed_whenConfirmed() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "upgrade");
        args.put("device_sns", List.of("DRONE-001", "DRONE-002"));
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("upgrade_triggered", data.get("status"));
        assertEquals(2, data.get("device_count"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeUpgrade_shouldFallbackToDeviceSn_whenDeviceSnsNotProvided() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "upgrade");
        args.put("device_sn", "DRONE-001");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(1, data.get("device_count"));
    }

    @Test
    void requiresHumanConfirmation_shouldReturnFalse() {
        assertFalse(tool.requiresHumanConfirmation());
    }

    // ==================== API 移除错误（非 Mock 模式 + workspace 可用）====================

    @Test
    void executeCheck_shouldReturnApiRemovedError_whenRealSdkWithWorkspace() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = createRealSdkTool().execute(Map.of(
                "action", "check",
                "device_sn", "DRONE-001"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("SDK 2.0"));
    }

    @Test
    void executeUpgrade_shouldReturnApiRemovedError_whenRealSdkWithWorkspace() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "upgrade");
        args.put("device_sns", List.of("DRONE-001"));
        args.put("confirm", true);

        Map<String, Object> result = createRealSdkTool().execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("SDK 2.0"));
    }

    // ==================== 工具方法 ====================

    /**
     * 创建非 Mock 模式且 workspace 非 null 的 FhFlysafeTool，用于验证 SDK API 移除路径。
     * Workspace 含 JNI native 方法，使用 Unsafe.allocateInstance() 绕过构造函数。
     */
    private FhFlysafeTool createRealSdkTool() {
        SdkService realSdkService = new SdkService() {
            @Override
            public boolean isMockMode() {
                return false;
            }
        };
        try {
            java.lang.reflect.Field workspaceField = SdkService.class.getDeclaredField("workspace");
            workspaceField.setAccessible(true);
            java.lang.reflect.Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);
            com.dji.flighthubsdk.core.Workspace mockWorkspace =
                    (com.dji.flighthubsdk.core.Workspace) unsafe.allocateInstance(
                            com.dji.flighthubsdk.core.Workspace.class);
            workspaceField.set(realSdkService, mockWorkspace);
        } catch (Exception e) {
            throw new RuntimeException("createRealSdkTool 失败", e);
        }
        return new FhFlysafeTool(sessionManager, realSdkService);
    }
}
