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
import com.dji.flighthub.mcp.tools.subscription.ListenChannelSupport;
import com.dji.flighthub.mcp.tools.subscription.SubscriptionBuffer;
import com.dji.flighthubsdk.core.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * FhDeviceManageTool 单元测试。
 * 覆盖 session 校验、参数校验、workspace 检查、rename/unbind confirm 校验，
 * 以及 workspace 设备变更订阅三元组（listen_workspace_changed/events/listen_stop）。
 *
 * @author DJI FlightHub SDK Team
 */
class FhDeviceManageToolTest {

    private static final String DEVICE_SN = "DEVICE-MANAGE-001";

    private SessionManager sessionManager;
    private ListenChannelSupport listenSupport;
    private FhDeviceManageTool deviceManageTool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);
        listenSupport = new ListenChannelSupport(new SubscriptionBuffer());

        // Mock 模式 SdkService，getWorkspace() 返回 null（未初始化状态）
        SdkService sdkService = new SdkService() {
            @Override
            public boolean isMockMode() {
                return true;
            }
        };
        deviceManageTool = new FhDeviceManageTool(sessionManager, sdkService, listenSupport);
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

    // ── 订阅三元组：listen_workspace_changed / events / listen_stop ──

    @Test
    void getToolDefinition_shouldExposeSubscriptionActions() {
        Map<String, Object> def = deviceManageTool.getToolDefinition();

        @SuppressWarnings("unchecked")
        Map<String, Object> schema = (Map<String, Object>) def.get("inputSchema");
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
        @SuppressWarnings("unchecked")
        Map<String, Object> actionProp = (Map<String, Object>) properties.get("action");
        @SuppressWarnings("unchecked")
        List<String> enumValues = (List<String>) actionProp.get("enum");

        assertTrue(enumValues.contains("listen_workspace_changed"));
        assertTrue(enumValues.contains("events"));
        assertTrue(enumValues.contains("listen_stop"));
    }

    @Test
    void execute_shouldReturnError_whenListenWorkspaceChangedNoSession() {
        Map<String, Object> result = deviceManageTool.execute(Map.of(
                "action", "listen_workspace_changed"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldSucceed_whenListenWorkspaceChangedInMockMode() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = deviceManageTool.execute(Map.of(
                "action", "listen_workspace_changed"
        ));

        assertEquals(true, result.get("success"));
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("listen_workspace_changed", data.get("action"));
        assertEquals("workspace_changed", data.get("channel"));
        assertTrue(((Number) data.get("listen_id")).intValue() > 0);
    }

    @Test
    void execute_shouldReturnEmptyStreams_whenEventsWithoutActiveSubscription() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "events");
        args.put("channels", List.of("workspace_changed"));

        Map<String, Object> result = deviceManageTool.execute(args);

        assertEquals(true, result.get("success"));
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        @SuppressWarnings("unchecked")
        Map<String, Object> streams = (Map<String, Object>) data.get("streams");
        @SuppressWarnings("unchecked")
        Map<String, Object> stream = (Map<String, Object>) streams.get("workspace_changed");
        assertEquals(false, stream.get("active"));
        assertEquals(0, stream.get("count"));
    }

    @Test
    void execute_shouldReturnEvent_whenEventsAfterListenWorkspaceChanged() {
        sessionManager.createSession("uuid", "ws");

        deviceManageTool.execute(Map.of("action", "listen_workspace_changed"));

        Map<String, Object> result = deviceManageTool.execute(Map.of("action", "events"));

        assertEquals(true, result.get("success"));
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        @SuppressWarnings("unchecked")
        Map<String, Object> streams = (Map<String, Object>) data.get("streams");
        @SuppressWarnings("unchecked")
        Map<String, Object> stream = (Map<String, Object>) streams.get("workspace_changed");

        assertEquals(true, stream.get("active"));
        assertEquals(1, stream.get("count"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> events = (List<Map<String, Object>>) stream.get("events");
        assertEquals(1, events.size());
        assertTrue(events.get(0).containsKey("device_changes"));
    }

    @Test
    void execute_shouldSupportCursor_whenEventsCalledWithSinceSeq() {
        sessionManager.createSession("uuid", "ws");

        deviceManageTool.execute(Map.of("action", "listen_workspace_changed"));

        Map<String, Object> firstArgs = new HashMap<>();
        firstArgs.put("action", "events");
        firstArgs.put("limit", 1);
        Map<String, Object> firstResult = deviceManageTool.execute(firstArgs);

        @SuppressWarnings("unchecked")
        Map<String, Object> firstData = (Map<String, Object>) firstResult.get("data");
        @SuppressWarnings("unchecked")
        Map<String, Object> firstStreams = (Map<String, Object>) firstData.get("streams");
        @SuppressWarnings("unchecked")
        Map<String, Object> firstStream = (Map<String, Object>) firstStreams.get("workspace_changed");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> firstEvents = (List<Map<String, Object>>) firstStream.get("events");
        assertEquals(1, firstEvents.size());
        long lastSeq = (long) firstEvents.get(0).get("_seq");

        Map<String, Object> secondArgs = new HashMap<>();
        secondArgs.put("action", "events");
        secondArgs.put("since_seq", lastSeq);
        Map<String, Object> secondResult = deviceManageTool.execute(secondArgs);

        @SuppressWarnings("unchecked")
        Map<String, Object> secondData = (Map<String, Object>) secondResult.get("data");
        @SuppressWarnings("unchecked")
        Map<String, Object> secondStreams = (Map<String, Object>) secondData.get("streams");
        @SuppressWarnings("unchecked")
        Map<String, Object> secondStream = (Map<String, Object>) secondStreams.get("workspace_changed");
        assertEquals(0, secondStream.get("count"));
    }

    @Test
    void execute_shouldStopExplicitChannel_whenListenStopWithChannelsParam() {
        sessionManager.createSession("uuid", "ws");

        deviceManageTool.execute(Map.of("action", "listen_workspace_changed"));

        Map<String, Object> args = new HashMap<>();
        args.put("action", "listen_stop");
        args.put("channels", List.of("workspace_changed"));

        Map<String, Object> result = deviceManageTool.execute(args);

        assertEquals(true, result.get("success"));
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        @SuppressWarnings("unchecked")
        List<String> stopped = (List<String>) data.get("stopped");
        assertEquals(List.of("workspace_changed"), stopped);
    }

    @Test
    void execute_shouldStopAllChannels_whenListenStopWithoutChannelsParam() {
        sessionManager.createSession("uuid", "ws");

        deviceManageTool.execute(Map.of("action", "listen_workspace_changed"));

        Map<String, Object> result = deviceManageTool.execute(Map.of("action", "listen_stop"));

        assertEquals(true, result.get("success"));
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        @SuppressWarnings("unchecked")
        List<String> stopped = (List<String>) data.get("stopped");
        assertEquals(List.of("workspace_changed"), stopped);
    }

    @Test
    void execute_shouldNotLeakRegistration_whenListenRepeatedBeforeStop() {
        sessionManager.createSession("uuid", "ws");

        deviceManageTool.execute(Map.of("action", "listen_workspace_changed"));
        deviceManageTool.execute(Map.of("action", "listen_workspace_changed"));
        deviceManageTool.execute(Map.of("action", "listen_workspace_changed"));

        Map<String, Object> result = deviceManageTool.execute(Map.of("action", "listen_stop"));

        assertEquals(true, result.get("success"));
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        @SuppressWarnings("unchecked")
        List<String> stopped = (List<String>) data.get("stopped");
        @SuppressWarnings("unchecked")
        List<String> notActive = (List<String>) data.get("not_active");

        assertEquals(List.of("workspace_changed"), stopped);
        assertTrue(notActive.isEmpty());
    }

    // ── L1 硬拦截：rename/unbind 工具级 L0 下的单独安全等级校验 ──

    @Test
    void execute_shouldDenyRename_whenSessionLevelBelowReversible() {
        // 工具级是 L0，rename 的 L1 硬拦截必须在 action 内单独生效（含 dry_run）
        FhDeviceManageTool tool = createToolWithMaxLevel(0);

        Map<String, Object> result = tool.execute(Map.of(
                "action", "rename",
                "device_sn", DEVICE_SN,
                "new_name", "renamed",
                "confirm", true,
                "dry_run", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("insufficient security level"));
    }

    @Test
    void execute_shouldDenyUnbind_whenSessionLevelBelowReversible() {
        FhDeviceManageTool tool = createToolWithMaxLevel(0);

        Map<String, Object> result = tool.execute(Map.of(
                "action", "unbind",
                "device_sn", DEVICE_SN,
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("insufficient security level"));
    }

    /**
     * 构造指定 maxSecurityLevel 的 session 与工具实例。
     *
     * <p>workspace 用 mock 提供非 null 值，否则工具会在进入 rename/unbind 之前
     * 以 "SDK not initialized" 提前返回，无法覆盖安全等级拦截分支。
     *
     * @param maxLevel session 允许的最高安全等级
     * @return 使用该 session 的工具实例
     */
    private static FhDeviceManageTool createToolWithMaxLevel(int maxLevel) {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(maxLevel);
        SessionManager sessions = new SessionManager(properties);
        sessions.createSession("uuid", "ws");

        Workspace workspace = mock(Workspace.class);
        SdkService sdkService = new SdkService() {
            @Override
            public boolean isMockMode() {
                return true;
            }

            @Override
            public Workspace getWorkspace() {
                return workspace;
            }
        };
        return new FhDeviceManageTool(sessions, sdkService, new ListenChannelSupport(new SubscriptionBuffer()));
    }
}
