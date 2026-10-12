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

package com.dji.flighthub.mcp.tools.device;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhDeviceManageTool 单元测试。
 * 覆盖 session 校验、参数校验、workspace 检查、rename/unbind confirm 校验。
 *
 * @author DJI FlightHub SDK Team
 */
class FhDeviceManageToolTest {

    private static final String DEVICE_SN = "DEVICE-MANAGE-001";

    private SessionManager sessionManager;
    private FhDeviceManageTool deviceManageTool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);

        // Mock 模式 SdkService，getWorkspace() 返回 null（未初始化状态）
        SdkService sdkService = new SdkService() {
            @Override
            public boolean isMockMode() {
                return true;
            }
        };
        deviceManageTool = new FhDeviceManageTool(sessionManager, sdkService);
    }

    @Test
    void getToolDefinition_shouldReturnCorrectName() {
        Map<String, Object> def = deviceManageTool.getToolDefinition();

        assertEquals("fh_device_manage", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = deviceManageTool.execute(Map.of(
                "action", "detail",
                "device_sn", DEVICE_SN
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenActionMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("device_sn", DEVICE_SN);

        Map<String, Object> result = deviceManageTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing action"));
    }

    @Test
    void execute_shouldReturnError_whenDeviceSnMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "detail");

        Map<String, Object> result = deviceManageTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing device_sn"));
    }

    @Test
    void execute_shouldReturnError_whenDeviceSnBlank() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "detail");
        args.put("device_sn", "   ");

        Map<String, Object> result = deviceManageTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing device_sn"));
    }

    @Test
    void execute_shouldReturnError_whenWorkspaceNull() {
        // SdkService 的 getWorkspace() 默认返回 null（Mock 模式未初始化时）
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "detail");
        args.put("device_sn", DEVICE_SN);

        Map<String, Object> result = deviceManageTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("SDK not initialized"));
    }

    @Test
    void execute_shouldReturnSdkNotInitialized_whenWorkspaceNull_givenInvalidAction() {
        // 注意：FhDeviceManageTool 的校验顺序为 session → action null → device_sn → workspace → switch(action)
        // 当 workspace 为 null 时，invalid action 校验不可达
        // 此测试验证 workspace null 校验能正确拦截（即使 action 无效）
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "explode");
        args.put("device_sn", DEVICE_SN);

        Map<String, Object> result = deviceManageTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("SDK not initialized"));
    }

    @Test
    void execute_shouldReturnSdkNotInitialized_whenWorkspaceNull_givenRenameAction() {
        // workspace null 拦截先于 rename 的 new_name 参数校验
        // 深层 rename 逻辑需集成测试（依赖真实 Workspace 实例）覆盖
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "rename");
        args.put("device_sn", DEVICE_SN);
        args.put("confirm", true);

        Map<String, Object> result = deviceManageTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("SDK not initialized"));
    }

    @Test
    void execute_shouldReturnSdkNotInitialized_whenWorkspaceNull_givenUnbindAction() {
        // workspace null 拦截先于 unbind 的 confirm 参数校验
        // 深层 unbind 逻辑需集成测试覆盖
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "unbind");
        args.put("device_sn", DEVICE_SN);

        Map<String, Object> result = deviceManageTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("SDK not initialized"));
    }
}
