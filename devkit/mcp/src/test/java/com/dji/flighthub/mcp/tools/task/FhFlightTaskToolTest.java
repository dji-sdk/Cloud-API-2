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

package com.dji.flighthub.mcp.tools.task;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.FlightPhase;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * FhFlightTaskTool 单元测试。
 * 覆盖：getToolDefinition、无 session 报错、各 action 参数校验、Mock 路径、confirm 门控。
 *
 * @author DJI FlightHub SDK Team
 */
class FhFlightTaskToolTest {

    private SessionManager sessionManager;
    private FhFlightTaskTool tool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);

        SdkService sdkService = new SdkService() {  public boolean isMockMode() { return true; } };
        tool = new FhFlightTaskTool(sessionManager, sdkService);
    }

    @Test
    void getToolDefinition_shouldReturnCorrectNameAndSchema() {
        Map<String, Object> def = tool.getToolDefinition();

        assertEquals("fh_flight_task", def.get("name"));
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
        Map<String, Object> result = tool.execute(Map.of("action", "list"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenActionInvalid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "invalid"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid or missing action"));
    }

    // ==================== list ====================

    @Test
    @SuppressWarnings("unchecked")
    void executeList_shouldReturnMockTasks() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "list"));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data.get("tasks"));
        assertEquals(true, data.get("_mock"));
    }

    // ==================== detail ====================

    @Test
    void executeDetail_shouldReturnError_whenTaskUuidMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "detail"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("task_uuid"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeDetail_shouldReturnMockDetail() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "detail",
                "task_uuid", "task_001"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("task_001", data.get("task_uuid"));
    }

    // ==================== create ====================

    @Test
    void executeCreate_shouldReturnError_whenNameMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "create");
        args.put("sn", "DOCK-001");
        args.put("wayline_uuid", "wl_001");
        args.put("rth_altitude", 120.0);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("name"));
    }

    @Test
    void executeCreate_shouldReturnError_whenRthAltitudeInvalid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "create");
        args.put("name", "Test Task");
        args.put("sn", "DOCK-001");
        args.put("task_type", 1);
        args.put("wayline_uuid", "wl_001");
        args.put("rth_altitude", 5.0);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("rth_altitude"));
    }

    @Test
    void executeCreate_shouldReturnError_whenTaskTypeInvalid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "create");
        args.put("name", "Test Task");
        args.put("sn", "DOCK-001");
        args.put("task_type", 99);
        args.put("wayline_uuid", "wl_001");
        args.put("rth_altitude", 120.0);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("task_type"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeCreate_shouldDefaultToAsiaShanghai_whenTimeZoneMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "create");
        args.put("name", "Test Task");
        args.put("sn", "DOCK-001");
        args.put("task_type", 1);
        args.put("wayline_uuid", "wl_001");
        args.put("rth_altitude", 120.0);
        // 故意不提供 time_zone，应使用默认值 Asia/Shanghai
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("Asia/Shanghai", data.get("time_zone"));
    }

    @Test
    void executeCreate_shouldReturnError_whenOutOfControlActionInvalid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "create");
        args.put("name", "Test Task");
        args.put("sn", "DOCK-001");
        args.put("task_type", 1);
        args.put("wayline_uuid", "wl_001");
        args.put("rth_altitude", 120.0);
        args.put("time_zone", "Asia/Shanghai");
        args.put("out_of_control_action", 99);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("out_of_control_action"));
    }

    @Test
    void executeCreate_shouldReturnError_whenScheduledTaskMissingTimes() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "create");
        args.put("name", "Scheduled Task");
        args.put("sn", "DOCK-001");
        args.put("task_type", 2);
        args.put("wayline_uuid", "wl_001");
        args.put("rth_altitude", 120.0);
        args.put("time_zone", "Asia/Shanghai");
        // 故意不提供 begin_at / end_at

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("begin_at"));
    }

    @Test
    void executeCreate_shouldReturnError_whenEndAtBeforeBeginAt() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "create");
        args.put("name", "Scheduled Task");
        args.put("sn", "DOCK-001");
        args.put("task_type", 2);
        args.put("wayline_uuid", "wl_001");
        args.put("rth_altitude", 120.0);
        args.put("time_zone", "Asia/Shanghai");
        args.put("begin_at", 1700100000L);
        args.put("end_at",   1700000000L);  // 结束时间早于开始时间

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("end_at must be greater than begin_at"));
    }

    @Test
    void executeCreate_shouldRequireConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "create");
        args.put("name", "Test Task");
        args.put("sn", "DOCK-001");
        args.put("task_type", 1);
        args.put("wayline_uuid", "wl_001");
        args.put("rth_altitude", 120.0);
        args.put("time_zone", "Asia/Shanghai");

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeCreate_shouldSucceed_whenConfirmed() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "create");
        args.put("name", "Test Task");
        args.put("sn", "DOCK-001");
        args.put("task_type", 1);
        args.put("wayline_uuid", "wl_001");
        args.put("rth_altitude", 120.0);
        args.put("time_zone", "Asia/Shanghai");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("CREATED", data.get("status"));
        assertEquals("Asia/Shanghai", data.get("time_zone"));
        assertEquals(1, data.get("out_of_control_action"));

        // 验证 next_steps
        assertNotNull(result.get("next_steps"));
        List<Map<String, Object>> nextSteps = (List<Map<String, Object>>) result.get("next_steps");
        assertEquals(2, nextSteps.size());
        // 第一步：查看任务详情
        assertEquals("fh_flight_task", nextSteps.get(0).get("tool"));
        Map<String, Object> detailHint = (Map<String, Object>) nextSteps.get(0).get("params_hint");
        assertEquals("detail", detailHint.get("action"));
        assertNotNull(detailHint.get("task_uuid"));
        // 第二步：通过 L2 工具执行
        assertEquals("fh_wayline_execute", nextSteps.get(1).get("tool"));
        Map<String, Object> executeHint = (Map<String, Object>) nextSteps.get(1).get("params_hint");
        assertNotNull(executeHint.get("task_uuid"));
        assertEquals(true, executeHint.get("confirm"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeCreate_shouldSucceed_withScheduledTask() {
        sessionManager.createSession("uuid", "ws");

        long beginAt = 1800000000L;
        long endAt   = 1800003600L;

        Map<String, Object> args = new HashMap<>();
        args.put("action", "create");
        args.put("name", "Scheduled Patrol");
        args.put("sn", "DOCK-001");
        args.put("task_type", 2);
        args.put("wayline_uuid", "wl_001");
        args.put("rth_altitude", 100.0);
        args.put("time_zone", "UTC");
        args.put("out_of_control_action", 1);
        args.put("begin_at", beginAt);
        args.put("end_at", endAt);
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("CREATED", data.get("status"));
        assertEquals("UTC", data.get("time_zone"));
        assertEquals(beginAt, ((Number) data.get("begin_at")).longValue());
        assertEquals(endAt, ((Number) data.get("end_at")).longValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeCreate_missingWaylineUuid_shouldReturnHint() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "create");
        args.put("name", "Test Task");
        args.put("sn", "DOCK-001");
        args.put("task_type", 1);
        args.put("rth_altitude", 120.0);
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("wayline_uuid"));
        assertNotNull(result.get("hint"));
        assertTrue(result.get("hint").toString().contains("fh_workspace"));

        // 验证 next_steps 包含恢复选项
        assertNotNull(result.get("next_steps"));
        List<Map<String, Object>> nextSteps = (List<Map<String, Object>>) result.get("next_steps");
        assertEquals(3, nextSteps.size());
        assertEquals("fh_workspace", nextSteps.get(0).get("tool"));
        assertEquals("fh_wayline_upload", nextSteps.get(1).get("tool"));
    }

    // ==================== delete ====================

    @Test
    void executeDelete_shouldRequireConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "delete");
        args.put("task_uuid", "task_001");

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeDelete_shouldSucceed_whenConfirmed() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "delete");
        args.put("task_uuid", "task_001");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("deleted", data.get("status"));
    }

    // ==================== edit ====================

    @Test
    void executeEdit_shouldReturnError_whenTaskUuidMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "edit", "confirm", true));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("task_uuid"));
    }

    @Test
    void executeEdit_shouldRequireConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "edit");
        args.put("task_uuid", "task_001");

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeEdit_shouldSucceed_whenConfirmed() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "edit");
        args.put("task_uuid", "task_001");
        args.put("name", "Updated Task Name");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("task_001", data.get("task_uuid"));
        assertEquals("updated", data.get("status"));
        assertEquals(true, data.get("_mock"));
    }

    // ==================== execute ====================

    @Test
    void executeExecute_shouldRequireConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "execute");
        args.put("task_uuid", "task_001");

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    void executeExecute_shouldReturnError_whenTaskUuidMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "execute");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("task_uuid"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeExecute_shouldSucceed_whenConfirmed() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "execute");
        args.put("task_uuid", "task_001");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("EXECUTING", data.get("status"));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    void executeExecute_shouldSetFlightPhase_whenSuccessful() {
        SessionContext session = sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "execute");
        args.put("task_uuid", "task_flight_001");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        // session 应记录活跃航线任务和飞行阶段，确保安全监控能感知
        assertEquals("task_flight_001", session.getActiveWaylineTaskUuid());
        assertEquals(FlightPhase.WAYLINE_EXECUTING, session.getFlightPhase());
    }

    // ==================== update_status ====================

    @Test
    void executeUpdateStatus_shouldReturnError_whenTaskUuidMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "update_status");
        args.put("status", 3);
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("task_uuid"));
    }

    @Test
    void executeUpdateStatus_shouldReturnError_whenStatusMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "update_status");
        args.put("task_uuid", "task_001");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("status"));
    }

    @Test
    void executeUpdateStatus_shouldRequireConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "update_status");
        args.put("task_uuid", "task_001");
        args.put("status", 3);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeUpdateStatus_shouldSucceed_whenConfirmed() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "update_status");
        args.put("task_uuid", "task_001");
        args.put("status", 3);
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("task_001", data.get("task_uuid"));
        assertEquals(3, data.get("new_status"));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    void requiresHumanConfirmation_shouldReturnFalse() {
        assertFalse(tool.requiresHumanConfirmation());
    }

    @Test
    void getToolDefinition_shouldContainRoutingHints() {
        Map<String, Object> def = tool.getToolDefinition();
        String description = (String) def.get("description");

        assertThat(description).contains("WHEN TO USE");
        assertThat(description).contains("DO NOT USE WHEN");
        assertThat(description).contains("fh_wayline_upload");
        assertThat(description).contains("fh_workspace");
        assertThat(description).contains("dock");
        assertThat(description.length()).isLessThanOrEqualTo(600);
    }
}
