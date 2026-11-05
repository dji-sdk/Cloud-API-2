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

package com.dji.flighthub.mcp.tools.wayline;

import com.dji.flighthub.mcp.annotation.McpToolAutoRegistrar;
import com.dji.flighthub.mcp.audit.AuditService;
import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.config.McpToolRegistry;
import com.dji.flighthub.mcp.idempotency.IdempotencyCache;
import com.dji.flighthub.mcp.ratelimit.ToolRateLimiter;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthubsdk.FlightHubCallback.CreateInFlightWaylineTaskCallback;
import com.dji.flighthubsdk.FlightHubCallback.GetActiveInFlightWaylineTaskCallback;
import com.dji.flighthubsdk.FlightHubCallback.InFlightWaylineControlCallback;
import com.dji.flighthubsdk.bean.ActiveInFlightWaylineTaskInfo;
import com.dji.flighthubsdk.bean.CreateInFlightWaylineTaskParams;
import com.dji.flighthubsdk.bean.CreateInFlightWaylineTaskResult;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.InFlightWaylineTaskDetail;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.InFlightWaylineModule;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import com.dji.flighthubsdk.value.InFlightWaylineControlAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * FhInFlightWaylineTool 单元测试。
 * 覆盖 schema 声明、session/参数校验、安全等级硬拦截、dry_run 短路、
 * mock 模式返回结构，以及 create/control/status 三个真实 SDK 调用路径。
 *
 * @author DJI FlightHub SDK Team
 */
class FhInFlightWaylineToolTest {

    private static final String DRONE_SN = "DRONE-TEST-001";

    private static final String WAYLINE_UUID = "wayline-uuid-001";

    private SessionManager sessionManager;
    private FhInFlightWaylineTool tool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);

        // 只覆写 isMockMode()：生产实现中 mock 模式下 getDrone() 恒返回 null，
        // 这里保持同一契约，防止 mock 分支因提前调用 getDrone() 而变成死代码却测不出来
        SdkService sdkService = new SdkService() {
            @Override
            public boolean isMockMode() {
                return true;
            }
        };
        tool = new FhInFlightWaylineTool(sessionManager, sdkService);
    }

    // ==================== schema / 声明 ====================

    @Test
    void getToolDefinition_shouldDeclareNameAndRequiredParams() {
        Map<String, Object> def = tool.getToolDefinition();

        assertEquals("fh_inflight_wayline", def.get("name"));
        assertNotNull(def.get("description"));

        @SuppressWarnings("unchecked")
        Map<String, Object> schema = (Map<String, Object>) def.get("inputSchema");
        @SuppressWarnings("unchecked")
        Map<String, Object> props = (Map<String, Object>) schema.get("properties");
        assertTrue(props.containsKey("drone_sn"));
        assertTrue(props.containsKey("action"));
        assertTrue(props.containsKey("wayline_uuid"));
        assertTrue(props.containsKey("rth_altitude"));
        assertTrue(props.containsKey("control_action"));

        @SuppressWarnings("unchecked")
        List<String> required = (List<String>) schema.get("required");
        assertTrue(required.contains("drone_sn"));
        assertTrue(required.contains("action"));
        assertFalse(required.contains("wayline_uuid"));
        assertFalse(required.contains("control_action"));
    }

    @Test
    void getSecurityLevel_shouldReturnReadOnly_soStatusIsNotBlockedByDispatchGate() {
        // 类级声明 L0：派发层只放行到 L0，create/control 在业务方法内自行升到 L2
        assertEquals(SecurityLevel.READ_ONLY, tool.getSecurityLevel());
    }

    @Test
    void requiresHumanConfirmation_shouldBeFalse_becauseStatusIsReadOnly() {
        assertFalse(tool.requiresHumanConfirmation());
    }

    @Test
    void class_shouldBeSpringComponent_soThatAutoRegistrarPicksItUp() {
        // McpToolAutoRegistrar 只注册 Spring 注入的 List<AbstractMcpTool>；
        // 缺少 @Component 时工具不会出现在 tools/list 中
        assertNotNull(FhInFlightWaylineTool.class.getAnnotation(Component.class),
                "FhInFlightWaylineTool 必须标注 @Component，否则不会被注册到 MCP Registry");
    }

    @Test
    void autoRegistrar_shouldRegisterInFlightWaylineTool() {
        McpToolRegistry registry = new McpToolRegistry();
        McpToolAutoRegistrar registrar = new McpToolAutoRegistrar(
                List.of(tool), registry, mock(AuditService.class), mock(IdempotencyCache.class),
                null, mock(ToolRateLimiter.class), sessionManager);

        registrar.afterSingletonsInstantiated();

        assertEquals(1, registry.getToolCount());
        assertTrue(registry.getToolDefinitions().stream()
                .anyMatch(def -> "fh_inflight_wayline".equals(def.get("name"))));
    }

    // ==================== session / 参数校验 ====================

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "status"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenActionInvalid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "explode"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid or missing action"));
    }

    @Test
    void execute_shouldReturnError_whenDroneSnMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "status"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("drone_sn"));
    }

    @Test
    void execute_shouldReturnError_whenCreateMissingWaylineUuid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = createArgs();
        args.remove("wayline_uuid");

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("wayline_uuid"));
    }

    @Test
    void execute_shouldReturnError_whenRthAltitudeMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = createArgs();
        args.remove("rth_altitude");

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("rth_altitude"));
    }

    @Test
    void execute_shouldReturnError_whenRthAltitudeBelowTwo() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = createArgs();
        args.put("rth_altitude", 1);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("rth_altitude"));
    }

    @Test
    void execute_shouldReturnError_whenOutOfControlActionOutOfRange() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = createArgs();
        args.put("out_of_control_action", 3);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("out_of_control_action"));
    }

    @Test
    void execute_shouldReturnError_whenExitWaylineWhenRcLostOutOfRange() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = createArgs();
        args.put("exit_wayline_when_rc_lost", 2);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("exit_wayline_when_rc_lost"));
    }

    @Test
    void execute_shouldReturnError_whenRthAltitudeIsNotANumber() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = createArgs();
        args.put("rth_altitude", "abc");

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("rth_altitude"));
    }

    @Test
    void execute_shouldReturnError_whenOutOfControlActionIsNotANumber() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = createArgs();
        args.put("out_of_control_action", "land-please");

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("out_of_control_action"));
    }

    @Test
    void execute_shouldReturnError_whenExitWaylineWhenRcLostIsNotANumber() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = createArgs();
        args.put("exit_wayline_when_rc_lost", "continue?");

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("exit_wayline_when_rc_lost"));
    }

    @Test
    void execute_shouldAcceptNumericString_whenRthAltitudeProvidedAsString() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = createArgs();
        args.put("rth_altitude", "120");

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"), "数字字符串应被严格解析并接受");
    }

    @Test
    void execute_shouldReturnError_whenControlMissingControlAction() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "control"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("control_action"));
    }

    @Test
    void execute_shouldReturnError_whenDroneNotFound() {
        sessionManager.createSession("uuid", "ws");
        SdkService missingDroneSdk = new SdkService() {
            @Override
            public boolean isMockMode() {
                return false;
            }

            @Override
            public Drone getDrone(String sn) {
                return null;
            }
        };
        FhInFlightWaylineTool missingDroneTool = new FhInFlightWaylineTool(sessionManager, missingDroneSdk);

        Map<String, Object> result = missingDroneTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "status"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Drone instance not found"));
    }

    // ==================== 安全等级 ====================

    @Test
    void execute_shouldDenyCreate_whenSessionMaxLevelBelowFlight() {
        sessionManager = new SessionManager(defaultProperties(1));
        tool = new FhInFlightWaylineTool(sessionManager, mockModeSdk());
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(createArgs());

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("insufficient security level"));
    }

    @Test
    void execute_shouldDenyControl_whenSessionMaxLevelBelowFlight() {
        sessionManager = new SessionManager(defaultProperties(1));
        tool = new FhInFlightWaylineTool(sessionManager, mockModeSdk());
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "control",
                "control_action", "pause",
                "confirm", true));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("insufficient security level"));
    }

    @Test
    void execute_shouldAllowStatus_whenSessionMaxLevelIsZero() {
        sessionManager = new SessionManager(defaultProperties(0));
        tool = new FhInFlightWaylineTool(sessionManager, mockModeSdk());
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "status"));

        assertEquals(true, result.get("success"));
    }

    // ==================== confirm / dry_run ====================

    @Test
    void execute_shouldRequireConfirm_whenCreateWithoutConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = createArgs();
        args.remove("confirm");

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    void execute_shouldNotCallSdk_whenCreateDryRun() {
        sessionManager.createSession("uuid", "ws");
        InFlightWaylineModule module = mock(InFlightWaylineModule.class);
        FhInFlightWaylineTool realTool = realToolWith(module);

        Map<String, Object> args = createArgs();
        args.put("confirm", false);
        args.put("dry_run", true);
        Map<String, Object> result = realTool.execute(args);

        assertEquals(true, result.get("success"));
        assertEquals(true, result.get("_dry_run"));
        verifyNoInteractions(module);
    }

    @Test
    void execute_shouldNotCallSdk_whenControlDryRun() {
        sessionManager.createSession("uuid", "ws");
        InFlightWaylineModule module = mock(InFlightWaylineModule.class);
        FhInFlightWaylineTool realTool = realToolWith(module);

        Map<String, Object> result = realTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "control",
                "control_action", "pause",
                "confirm", false,
                "dry_run", true));

        assertEquals(true, result.get("success"));
        assertEquals(true, result.get("_dry_run"));
        verifyNoInteractions(module);
    }

    @Test
    void execute_shouldNotResolveDrone_whenCreateDryRun() {
        sessionManager.createSession("uuid", "ws");
        SdkService sdk = mock(SdkService.class);
        when(sdk.isMockMode()).thenReturn(false);
        FhInFlightWaylineTool dryRunTool = new FhInFlightWaylineTool(sessionManager, sdk);

        Map<String, Object> args = createArgs();
        args.put("dry_run", true);
        Map<String, Object> result = dryRunTool.execute(args);

        assertEquals(true, result.get("success"));
        // dry_run 必须在解析 Drone 之前短路，否则离线设备上看不到预览
        verify(sdk, never()).getDrone(anyString());
    }

    // ==================== mock 模式 ====================

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnMockCreateResult_whenCreateSucceeds() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(createArgs());

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(WAYLINE_UUID, data.get("wayline_uuid"));
        assertNotNull(data.get("in_flight_wayline_task_id"));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnMockControlResult_whenControlPause() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "control",
                "control_action", "pause",
                "confirm", true));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("pause", data.get("control_action"));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    void execute_shouldReturnMockStatusWithoutActiveTask_whenStatusCalled() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "status"));

        assertEquals(true, result.get("success"));
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(false, data.get("active"));
    }

    // ==================== 真实 SDK 路径 ====================

    @Test
    void execute_shouldCallCreateInFlightWaylineTask_whenCreateWithRealSdk() {
        sessionManager.createSession("uuid", "ws");
        InFlightWaylineModule module = mock(InFlightWaylineModule.class);
        FhInFlightWaylineTool realTool = realToolWith(module);
        doAnswer(inv -> {
            CreateInFlightWaylineTaskCallback cb = inv.getArgument(1);
            cb.onResult(new CreateInFlightWaylineTaskResult("ft-real-1", "ifw-real-1"), successError());
            return null;
        }).when(module).createInFlightWaylineTask(any(CreateInFlightWaylineTaskParams.class),
                any(CreateInFlightWaylineTaskCallback.class));

        Map<String, Object> args = createArgs();
        args.put("task_name", "power-line-inspection");
        args.put("fast_reconstruction", true);
        args.put("out_of_control_action", 2);
        Map<String, Object> result = realTool.execute(args);

        assertEquals(true, result.get("success"));
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("ft-real-1", data.get("flight_task_id"));
        assertEquals("ifw-real-1", data.get("in_flight_wayline_task_id"));

        ArgumentCaptor<CreateInFlightWaylineTaskParams> captor =
                ArgumentCaptor.forClass(CreateInFlightWaylineTaskParams.class);
        verify(module).createInFlightWaylineTask(captor.capture(), any(CreateInFlightWaylineTaskCallback.class));
        CreateInFlightWaylineTaskParams params = captor.getValue();
        assertEquals(WAYLINE_UUID, params.getWaylineUuid());
        assertEquals(60, params.getRthAltitude());
        assertEquals(2, params.getOutOfControlAction());
        assertEquals("power-line-inspection", params.getTaskName());
        assertEquals(Boolean.TRUE, params.getFastReconstruction());
        assertEquals(-1, params.getWaylinePrecisionType());
    }

    @Test
    void execute_shouldOmitOptionalCreateParams_whenNotProvided() {
        sessionManager.createSession("uuid", "ws");
        InFlightWaylineModule module = mock(InFlightWaylineModule.class);
        FhInFlightWaylineTool realTool = realToolWith(module);
        doAnswer(inv -> {
            CreateInFlightWaylineTaskCallback cb = inv.getArgument(1);
            cb.onResult(new CreateInFlightWaylineTaskResult("ft-1", "ifw-1"), successError());
            return null;
        }).when(module).createInFlightWaylineTask(any(CreateInFlightWaylineTaskParams.class),
                any(CreateInFlightWaylineTaskCallback.class));

        realTool.execute(createArgs());

        ArgumentCaptor<CreateInFlightWaylineTaskParams> captor =
                ArgumentCaptor.forClass(CreateInFlightWaylineTaskParams.class);
        verify(module).createInFlightWaylineTask(captor.capture(), any(CreateInFlightWaylineTaskCallback.class));
        CreateInFlightWaylineTaskParams params = captor.getValue();
        assertNull(params.getFastReconstruction(), "未传 fast_reconstruction 时不应下发该字段");
        // taskName 字段的「未下发」语义由 SDK 自身的空串默认值承载（非 -1/null 哨兵）：
        // javap 反编译确认 CreateInFlightWaylineTaskParams 构造器与 setTaskName(null) 均落到 ""，
        // 不存在能让 getTaskName() 返回 null 的公开路径
        assertEquals("", params.getTaskName(), "未传 task_name 时应保持 SDK 默认空串");
        assertEquals(-1, params.getRthMode());
        assertEquals(-1, params.getOutOfControlAction());
        assertEquals(-1, params.getExitWaylineWhenRcLost());
    }

    @Test
    void execute_shouldMapPauseAction_whenControlPauseWithRealSdk() {
        sessionManager.createSession("uuid", "ws");
        InFlightWaylineModule module = mock(InFlightWaylineModule.class);
        FhInFlightWaylineTool realTool = realToolWith(module);
        doAnswer(inv -> {
            InFlightWaylineControlCallback cb = inv.getArgument(1);
            cb.onResult(successError());
            return null;
        }).when(module).controlInFlightWaylineTask(any(InFlightWaylineControlAction.class),
                any(InFlightWaylineControlCallback.class));

        Map<String, Object> result = realTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "control",
                "control_action", "pause",
                "confirm", true));

        assertEquals(true, result.get("success"));
        ArgumentCaptor<InFlightWaylineControlAction> captor =
                ArgumentCaptor.forClass(InFlightWaylineControlAction.class);
        verify(module).controlInFlightWaylineTask(captor.capture(), any(InFlightWaylineControlCallback.class));
        assertEquals(InFlightWaylineControlAction.PAUSE, captor.getValue());
    }

    @Test
    void execute_shouldMapCancelAction_whenControlCancelWithRealSdk() {
        sessionManager.createSession("uuid", "ws");
        InFlightWaylineModule module = mock(InFlightWaylineModule.class);
        FhInFlightWaylineTool realTool = realToolWith(module);
        doAnswer(inv -> {
            InFlightWaylineControlCallback cb = inv.getArgument(1);
            cb.onResult(successError());
            return null;
        }).when(module).controlInFlightWaylineTask(any(InFlightWaylineControlAction.class),
                any(InFlightWaylineControlCallback.class));

        realTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "control",
                "control_action", "cancel",
                "confirm", true));

        ArgumentCaptor<InFlightWaylineControlAction> captor =
                ArgumentCaptor.forClass(InFlightWaylineControlAction.class);
        verify(module).controlInFlightWaylineTask(captor.capture(), any(InFlightWaylineControlCallback.class));
        assertEquals(InFlightWaylineControlAction.CANCEL, captor.getValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldMapActiveTaskInfo_whenStatusWithRealSdk() {
        sessionManager.createSession("uuid", "ws");
        InFlightWaylineModule module = mock(InFlightWaylineModule.class);
        FhInFlightWaylineTool realTool = realToolWith(module);
        ActiveInFlightWaylineTaskInfo info = new ActiveInFlightWaylineTaskInfo();
        info.setFlightId("flight-001");
        info.setFlightType(1);
        info.setSn(DRONE_SN);
        info.setFlightTaskData("{}");
        InFlightWaylineTaskDetail detail = new InFlightWaylineTaskDetail();
        detail.setUuid("ifw-001");
        detail.setStatus(2);
        detail.setWaylineId(WAYLINE_UUID);
        detail.setProgressPercent(42);
        detail.setWayPointIndex(3);
        detail.setErrCode(0);
        detail.setWaylineDuration(120.5);
        detail.setWaylineLength(800.25);
        detail.setTaskName("inspection");
        info.setInFlightTask(detail);
        doAnswer(inv -> {
            GetActiveInFlightWaylineTaskCallback cb = inv.getArgument(0);
            cb.onResult(info, successError());
            return null;
        }).when(module).getActiveInFlightWaylineTask(any(GetActiveInFlightWaylineTaskCallback.class));

        Map<String, Object> result = realTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "status"));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(true, data.get("active"));
        assertEquals("flight-001", data.get("flight_id"));
        assertEquals(DRONE_SN, data.get("sn"));

        Map<String, Object> task = (Map<String, Object>) data.get("in_flight_task");
        assertEquals("ifw-001", task.get("uuid"));
        assertEquals(2, task.get("status"));
        assertEquals(42, task.get("progress_percent"));
        assertEquals(3, task.get("way_point_index"));
        assertEquals(120.5, task.get("wayline_duration"));
        assertEquals("inspection", task.get("task_name"));
        assertEquals("{}", data.get("flight_task_data"), "扩展数据应原样透传，不能丢弃");
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReportInactive_whenStatusReturnsNullInfo() {
        sessionManager.createSession("uuid", "ws");
        InFlightWaylineModule module = mock(InFlightWaylineModule.class);
        FhInFlightWaylineTool realTool = realToolWith(module);
        doAnswer(inv -> {
            GetActiveInFlightWaylineTaskCallback cb = inv.getArgument(0);
            cb.onResult(null, successError());
            return null;
        }).when(module).getActiveInFlightWaylineTask(any(GetActiveInFlightWaylineTaskCallback.class));

        Map<String, Object> result = realTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "status"));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(false, data.get("active"), "SDK 返回 null 时应视为无活跃任务，而不是抛 NPE");
    }

    /**
     * 服务端返回空 data 时，SDK 的 info 各字段均为默认值（in_flight_task 永远非 null，
     * 是 SDK 构造器/setter 的兜底行为），此时应判定为无活跃任务。
     */
    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReportInactive_whenStatusReturnsEmptyInfo() {
        sessionManager.createSession("uuid", "ws");
        InFlightWaylineModule module = mock(InFlightWaylineModule.class);
        FhInFlightWaylineTool realTool = realToolWith(module);
        ActiveInFlightWaylineTaskInfo info = new ActiveInFlightWaylineTaskInfo();
        doAnswer(inv -> {
            GetActiveInFlightWaylineTaskCallback cb = inv.getArgument(0);
            cb.onResult(info, successError());
            return null;
        }).when(module).getActiveInFlightWaylineTask(any(GetActiveInFlightWaylineTaskCallback.class));

        Map<String, Object> result = realTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "status"));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(false, data.get("active"));
        assertFalse(data.containsKey("in_flight_task"));
    }

    @Test
    void execute_shouldReturnError_whenSdkCallbackFails() {
        sessionManager.createSession("uuid", "ws");
        InFlightWaylineModule module = mock(InFlightWaylineModule.class);
        FhInFlightWaylineTool realTool = realToolWith(module);
        doAnswer(inv -> {
            InFlightWaylineControlCallback cb = inv.getArgument(1);
            cb.onResult(new FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            return null;
        }).when(module).controlInFlightWaylineTask(any(InFlightWaylineControlAction.class),
                any(InFlightWaylineControlCallback.class));

        Map<String, Object> result = realTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "control",
                "control_action", "resume",
                "confirm", true));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("SDK error"));
    }

    // ==================== 辅助方法 ====================

    private static FlightHubProperties defaultProperties(int maxLevel) {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(maxLevel);
        return properties;
    }

    private static SdkService mockModeSdk() {
        return new SdkService() {
            @Override
            public boolean isMockMode() {
                return true;
            }
        };
    }

    private FhInFlightWaylineTool realToolWith(InFlightWaylineModule module) {
        Drone drone = mock(Drone.class);
        when(drone.getInFlightWaylineModule()).thenReturn(module);
        SdkService realSdk = new SdkService() {
            @Override
            public boolean isMockMode() {
                return false;
            }

            @Override
            public Drone getDrone(String sn) {
                return drone;
            }
        };
        return new FhInFlightWaylineTool(sessionManager, realSdk);
    }

    private static Map<String, Object> createArgs() {
        Map<String, Object> args = new HashMap<>();
        args.put("drone_sn", DRONE_SN);
        args.put("action", "create");
        args.put("wayline_uuid", WAYLINE_UUID);
        args.put("rth_altitude", 60);
        args.put("confirm", true);
        return args;
    }

    private static FlightHubError successError() {
        return new FlightHubError(FlightHubSdkErrorCode.SUCCESS);
    }
}
