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

package com.dji.flighthub.mcp.contract;

import com.dji.flighthub.mcp.annotation.McpTool;
import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.config.McpPromptRegistry;
import com.dji.flighthub.mcp.config.McpToolRegistry;
import com.dji.flighthub.mcp.prompts.FlightHubPrompts;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.sdk.StickControlExecutor;
import com.dji.flighthub.mcp.security.OperatorTokenAuthenticator;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.simulator.SimulatorClient;
import com.dji.flighthub.mcp.simulator.SimulatorProperties;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.tools.ai.AiSubscriptionBuffer;
import com.dji.flighthub.mcp.tools.ai.FhAiIdentifyTool;
import com.dji.flighthub.mcp.tools.ai.FhAiModelTool;
import com.dji.flighthub.mcp.tools.ai.FhAiSubscribeTool;
import com.dji.flighthub.mcp.tools.ai.FhLiveSeiTool;
import com.dji.flighthub.mcp.tools.airspace.FhDeviceRecommendTool;
import com.dji.flighthub.mcp.tools.airspace.FhFlysafeTool;
import com.dji.flighthub.mcp.tools.airspace.FhTcasTool;
import com.dji.flighthub.mcp.tools.airspace.FhUnlockLicenseTool;
import com.dji.flighthub.mcp.tools.camera.FhCameraTool;
import com.dji.flighthub.mcp.tools.connect.FhConnectTool;
import com.dji.flighthub.mcp.tools.connect.FhOpsTool;
import com.dji.flighthub.mcp.tools.connect.FhSessionInfoTool;
import com.dji.flighthub.mcp.tools.debug.FhDeviceDebugTool;
import com.dji.flighthub.mcp.tools.device.FhDeploySubscribeTool;
import com.dji.flighthub.mcp.tools.device.FhDeviceListTool;
import com.dji.flighthub.mcp.tools.device.FhDeviceManageTool;
import com.dji.flighthub.mcp.tools.device.FhDockControlTool;
import com.dji.flighthub.mcp.tools.device.FhDockSnapshotTool;
import com.dji.flighthub.mcp.tools.device.FhDroneSnapshotTool;
import com.dji.flighthub.mcp.tools.diagnosis.FhDiagnosisTool;
import com.dji.flighthub.mcp.tools.drc.FhControlAcquireTool;
import com.dji.flighthub.mcp.tools.drc.FhControlReleaseTool;
import com.dji.flighthub.mcp.tools.drc.FhDrcConnectTool;
import com.dji.flighthub.mcp.tools.drc.FhDrcDisconnectTool;
import com.dji.flighthub.mcp.tools.drc.FhEmergencyStopTool;
import com.dji.flighthub.mcp.tools.drc.FhFlytoTool;
import com.dji.flighthub.mcp.tools.drc.FhLiveDeviceTool;
import com.dji.flighthub.mcp.tools.drc.FhLiveStartTool;
import com.dji.flighthub.mcp.tools.drc.FhOsdSnapshotTool;
import com.dji.flighthub.mcp.tools.drc.FhPayloadAcquireTool;
import com.dji.flighthub.mcp.tools.drc.FhPayloadReleaseTool;
import com.dji.flighthub.mcp.tools.drc.FhPoiTool;
import com.dji.flighthub.mcp.tools.drc.FhReturnHomeTool;
import com.dji.flighthub.mcp.tools.drc.FhStickMoveTool;
import com.dji.flighthub.mcp.tools.drc.FhTakeoffTool;
import com.dji.flighthub.mcp.tools.firmware.FhFirmwareTool;
import com.dji.flighthub.mcp.tools.hms.FhHmsTool;
import com.dji.flighthub.mcp.tools.livesync.FhLiveSyncTool;
import com.dji.flighthub.mcp.tools.log.FhLogTool;
import com.dji.flighthub.mcp.tools.maintenance.FhMaintenanceTool;
import com.dji.flighthub.mcp.tools.media.FhMediaDeleteTool;
import com.dji.flighthub.mcp.tools.media.FhMediaDownloadTool;
import com.dji.flighthub.mcp.tools.media.FhMediaListTool;
import com.dji.flighthub.mcp.tools.media.FhMediaRecordTool;
import com.dji.flighthub.mcp.tools.media.FhMediaUploadTool;
import com.dji.flighthub.mcp.tools.osd.FhOsdListenTool;
import com.dji.flighthub.mcp.tools.psdk.FhPsdkResourceTool;
import com.dji.flighthub.mcp.tools.psdk.FhPsdkSubscribeTool;
import com.dji.flighthub.mcp.tools.psdk.FhPsdkWidgetTool;
import com.dji.flighthub.mcp.tools.psdk.FhSpeakerTool;
import com.dji.flighthub.mcp.tools.record.FhFlightRecordTool;
import com.dji.flighthub.mcp.tools.safety.FhFlightAreaListTool;
import com.dji.flighthub.mcp.tools.safety.FhFlightAreaManageTool;
import com.dji.flighthub.mcp.tools.safety.FhFlightAreaSyncTool;
import com.dji.flighthub.mcp.tools.safety.FhOfflineMapTool;
import com.dji.flighthub.mcp.tools.searchlight.FhSearchlightTool;
import com.dji.flighthub.mcp.tools.settings.FhSettingTool;
import com.dji.flighthub.mcp.tools.simulator.SimDeviceTool;
import com.dji.flighthub.mcp.tools.simulator.SimFlightStateTool;
import com.dji.flighthub.mcp.tools.simulator.SimHmsTool;
import com.dji.flighthub.mcp.tools.simulator.SimRecordingTool;
import com.dji.flighthub.mcp.tools.subscription.ListenChannelSupport;
import com.dji.flighthub.mcp.tools.subscription.SubscriptionBuffer;
import com.dji.flighthub.mcp.tools.task.FhFlightTaskTool;
import com.dji.flighthub.mcp.tools.terra.FhTerraTool;
import com.dji.flighthub.mcp.tools.wayline.FhInFlightWaylineTool;
import com.dji.flighthub.mcp.tools.wayline.FhTestflightTool;
import com.dji.flighthub.mcp.tools.wayline.FhWaylineControlTool;
import com.dji.flighthub.mcp.tools.wayline.FhWaylineExecuteTool;
import com.dji.flighthub.mcp.tools.wayline.FhWaylineRecommendTool;
import com.dji.flighthub.mcp.tools.wayline.FhWaylineStatusTool;
import com.dji.flighthub.mcp.tools.wayline.FhWaylineUploadTool;
import com.dji.flighthub.mcp.tools.workspace.FhWorkspaceTool;
import com.dji.flighthub.mcp.transport.ClientRegistry;
import com.dji.flighthub.mcp.transport.StdioJsonRpcClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 契约测试夹具：构造一套与 Spring 启动时等价的进程内环境。
 *
 * <p>为什么不用 {@code @SpringBootTest}：stdio profile 的 {@code StdioTransport}
 * 实现 {@code CommandLineRunner} 并读 {@code System.in}，会把测试 JVM 卡住。</p>
 *
 * <p>注册循环逐字复刻 {@code McpToolAutoRegistrar}，但**失败即抛出**而不是像生产那样
 * 跳过并打日志——如果某个 Tool 注册不了，契约层必须立刻红。</p>
 *
 * <p><b>维护约定：</b>新增/删除 Tool 时必须同步修改本类的 {@link #buildTools} 清单与
 * {@link #EXPECTED_TOOL_COUNT}，否则 B8 与 C4 都会失败。</p>
 *
 * @author DJI DevKit Team
 */
public final class ContractFixtures {

    /**
     * 全部已注册 Tool 的数量快照（B8 与 C4 共用）。
     *
     * <p>刻意写死而不是从注册表动态统计：它是一道「新增/删除 Tool 时是否同步维护契约层」的护栏。
     * 合入任何增删 Tool 的变更前，必须同时确认 {@link #buildTools} 清单、本常量、
     * B 层分类与 C 层参数覆盖都已更新，否则契约测试应当红。</p>
     */
    public static final int EXPECTED_TOOL_COUNT = 71;

    /**
     * 契约测试共享的摇杆控制执行器。
     *
     * <p>每个测试 JVM 只创建一次，避免每个用例重复构造 daemon 调度线程；
     * 该执行器没有 shutdown()，其线程为 daemon，不会阻止 JVM 退出。</p>
     */
    private static final StickControlExecutor STICK_CONTROL_EXECUTOR = new StickControlExecutor();

    private static final String CONTRACT_WORKSPACE_ID = "contract-workspace";

    private static final String CONTRACT_WORKSPACE_NAME = "Contract";

    private static final String CONTRACT_SERVER_ADDRESS = "http://contract-test";

    private ContractFixtures() {
    }

    /**
     * 构造夹具。
     *
     * @return 已注册全部 Tool、并完成 mock SDK session 初始化的夹具
     */
    public static Fixture create() {
        ObjectMapper objectMapper = new ObjectMapper();
        FlightHubProperties properties = new FlightHubProperties();
        // 生产默认值来自 application.yml 的 FH_MAX_SECURITY_LEVEL:2；Java 字段默认是 3，
        // 直接 new 会绕开 Spring 绑定，必须显式对齐，否则安全等级相关的契约断言建立在错误前提上
        properties.getSecurity().setMaxLevel(2);
        SdkService sdkService = mockSdkService();
        // 初始化 mock SDK session：多个工具在 isMockMode() 之前先校验 getWorkspace()，
        // 未初始化时会在 workspace 守卫处提前返回，走不到 mock 分支
        sdkService.initialize(CONTRACT_SERVER_ADDRESS, 0, null, CONTRACT_WORKSPACE_ID, null);
        SimulatorProperties simulatorProperties = new SimulatorProperties();
        SimulatorClient simulatorClient = new SimulatorClient(simulatorProperties, objectMapper);

        ClientRegistry clientRegistry = new ClientRegistry();
        SessionManager sessionManager = new SessionManager(properties);
        sessionManager.setSdkService(sdkService);
        sessionManager.setClientRegistry(clientRegistry);
        sessionManager.createSession(CONTRACT_WORKSPACE_ID, CONTRACT_WORKSPACE_NAME);

        McpPromptRegistry promptRegistry = new McpPromptRegistry();
        promptRegistry.registerAll(new FlightHubPrompts().getDefinitions());

        McpToolRegistry toolRegistry = new McpToolRegistry();
        registerAll(toolRegistry, buildTools(sessionManager, sdkService, properties, clientRegistry,
                simulatorClient, simulatorProperties));

        return new Fixture(toolRegistry, promptRegistry, sessionManager, properties, objectMapper);
    }

    /**
     * 构造强制 Mock 的 SdkService。
     *
     * <p>{@code SdkService.mockMode} 是构造时从 {@code System.getenv} 读的 final 字段，
     * 进程内改不了环境变量；{@code isMockMode()} 不是 final，因此用匿名子类覆写。</p>
     *
     * @return Mock 模式的 SdkService
     */
    private static SdkService mockSdkService() {
        return new SdkService() {
            @Override
            public boolean isMockMode() {
                return true;
            }
        };
    }

    /**
     * 显式列出全部具体 Tool（不含抽象基类与 5 个非 Tool 辅助类）。
     *
     * @param sessionManager      session 管理
     * @param sdkService          SDK 服务
     * @param properties          FlightHub 配置
     * @param clientRegistry      SSE 客户端注册表
     * @param simulatorClient     模拟器客户端
     * @param simulatorProperties 模拟器配置
     * @return Tool 实例列表
     */
    private static List<AbstractMcpTool> buildTools(SessionManager sessionManager, SdkService sdkService,
                                                    FlightHubProperties properties, ClientRegistry clientRegistry,
                                                    SimulatorClient simulatorClient,
                                                    SimulatorProperties simulatorProperties) {
        OperatorTokenAuthenticator operatorTokenAuthenticator = new OperatorTokenAuthenticator(properties);
        AiSubscriptionBuffer aiSubscriptionBuffer = new AiSubscriptionBuffer();
        // 生产侧 ListenChannelSupport 是 Spring 单例，被全部订阅类 Tool 共享；
        // 夹具同样只构造一份，保持「同一注册表 key 跨 Tool 可见」的运行时前提
        ListenChannelSupport listenSupport = new ListenChannelSupport(new SubscriptionBuffer());

        return List.of(
                // ---- terra / searchlight ----
                new FhTerraTool(sessionManager, sdkService, listenSupport),
                new FhSearchlightTool(sessionManager, sdkService),
                // ---- drc ----
                new FhPoiTool(sessionManager, sdkService, listenSupport),
                new FhControlAcquireTool(sessionManager, sdkService),
                new FhControlReleaseTool(sessionManager, sdkService),
                new FhLiveDeviceTool(sessionManager, sdkService),
                new FhLiveStartTool(sessionManager, sdkService, listenSupport),
                new FhReturnHomeTool(sessionManager, sdkService),
                new FhPayloadReleaseTool(sessionManager, sdkService),
                new FhPayloadAcquireTool(sessionManager, sdkService),
                new FhDrcConnectTool(sessionManager, sdkService),
                new FhDrcDisconnectTool(sessionManager, sdkService),
                new FhEmergencyStopTool(sessionManager, sdkService, operatorTokenAuthenticator),
                new FhOsdSnapshotTool(sessionManager, sdkService),
                new FhFlytoTool(sessionManager, sdkService),
                new FhStickMoveTool(sessionManager, sdkService, STICK_CONTROL_EXECUTOR),
                new FhTakeoffTool(sessionManager, sdkService, properties),
                // ---- wayline ----
                new FhWaylineRecommendTool(sessionManager, sdkService),
                new FhInFlightWaylineTool(sessionManager, sdkService),
                new FhWaylineExecuteTool(sessionManager, sdkService),
                new FhWaylineControlTool(sessionManager, sdkService),
                new FhWaylineUploadTool(sessionManager, sdkService),
                new FhWaylineStatusTool(sessionManager, sdkService),
                // ---- 单点能力 ----
                new FhFirmwareTool(sessionManager, sdkService),
                new FhFlightRecordTool(sessionManager, sdkService),
                new FhHmsTool(sessionManager, sdkService),
                new FhPsdkWidgetTool(sessionManager, sdkService),
                new FhSpeakerTool(sessionManager, sdkService, listenSupport),
                new FhPsdkResourceTool(sessionManager, sdkService),
                new FhPsdkSubscribeTool(sessionManager, sdkService, listenSupport),
                new FhSettingTool(sessionManager, sdkService),
                new FhLiveSyncTool(sessionManager, sdkService),
                new FhCameraTool(sessionManager, sdkService),
                new FhOsdListenTool(sessionManager, sdkService, aiSubscriptionBuffer),
                new FhWorkspaceTool(sessionManager, sdkService, listenSupport),
                new FhFlightTaskTool(sessionManager, sdkService),
                new FhLogTool(sessionManager, sdkService),
                new FhDeviceDebugTool(sessionManager, sdkService),
                new FhMaintenanceTool(sessionManager, sdkService),
                new FhDiagnosisTool(sessionManager, sdkService),
                // ---- safety ----
                new FhFlightAreaListTool(sessionManager, sdkService),
                new FhFlightAreaSyncTool(sessionManager, sdkService),
                new FhFlightAreaManageTool(sessionManager, sdkService),
                new FhOfflineMapTool(sessionManager, sdkService),
                // ---- simulator ----
                new SimHmsTool(sessionManager, simulatorClient, properties, simulatorProperties),
                new SimFlightStateTool(sessionManager, simulatorClient, properties, simulatorProperties),
                new SimRecordingTool(sessionManager, simulatorClient, properties, simulatorProperties),
                new SimDeviceTool(sessionManager, simulatorClient, properties, simulatorProperties),
                // ---- ai ----
                new FhAiIdentifyTool(sessionManager, sdkService),
                new FhAiSubscribeTool(sessionManager, sdkService, aiSubscriptionBuffer),
                new FhAiModelTool(sessionManager, sdkService),
                new FhLiveSeiTool(sessionManager, sdkService, aiSubscriptionBuffer),
                // ---- device ----
                new FhDeviceManageTool(sessionManager, sdkService, listenSupport),
                new FhDeviceListTool(sessionManager, sdkService),
                new FhDockControlTool(sessionManager, sdkService, listenSupport),
                new FhDockSnapshotTool(sessionManager, sdkService),
                new FhDroneSnapshotTool(sessionManager, sdkService),
                new FhDeploySubscribeTool(sessionManager, sdkService, listenSupport),
                // ---- airspace ----
                new FhTcasTool(sessionManager, sdkService),
                new FhFlysafeTool(sessionManager, sdkService),
                new FhUnlockLicenseTool(sessionManager, sdkService),
                new FhDeviceRecommendTool(sessionManager, sdkService),
                // ---- connect ----
                new FhOpsTool(sessionManager, sdkService),
                new FhSessionInfoTool(sessionManager),
                new FhConnectTool(properties, sessionManager, sdkService, clientRegistry),
                // ---- media ----
                new FhMediaDeleteTool(sessionManager, sdkService),
                new FhMediaListTool(sessionManager, sdkService),
                new FhMediaRecordTool(sessionManager, sdkService, listenSupport),
                new FhMediaDownloadTool(sessionManager, sdkService),
                new FhMediaUploadTool(sessionManager, sdkService),
                // ---- wayline 测试飞行 ----
                new FhTestflightTool(sessionManager, sdkService));
    }

    /**
     * 复刻 {@code McpToolAutoRegistrar} 的校验 + 注册流程，但失败即抛出。
     *
     * <p>额外校验 Tool 名唯一：{@code McpToolRegistry} 本身不做重名检查，
     * 重名会让后续 tools/list 数量断言失败且难以定位。</p>
     *
     * @param registry Tool 注册表
     * @param tools    Tool 实例列表
     */
    private static void registerAll(McpToolRegistry registry, List<AbstractMcpTool> tools) {
        Set<String> registeredNames = new HashSet<>();
        for (AbstractMcpTool tool : tools) {
            validateToolConfiguration(tool);
            Map<String, Object> definition = tool.getToolDefinition();
            Object nameValue = definition.get("name");
            if (!(nameValue instanceof String name)) {
                throw new IllegalStateException(tool.getClass().getSimpleName()
                        + " 的 tool definition 缺少合法的 String 类型 name 字段");
            }
            if (!registeredNames.add(name)) {
                throw new IllegalStateException(tool.getClass().getSimpleName()
                        + " 注册了重复的 Tool 名称: " + name);
            }
            registry.register(definition, tool::execute, tool.requiresHumanConfirmation());
        }
    }

    /**
     * 校验 Tool 必须「带 @McpTool 注解」或「覆写了 execute()」二者之一。
     *
     * @param tool Tool 实例
     */
    private static void validateToolConfiguration(AbstractMcpTool tool) {
        Class<?> toolClass = tool.getClass();
        if (toolClass.isAnnotationPresent(McpTool.class)) {
            return;
        }
        try {
            Method executeMethod = toolClass.getMethod("execute", Map.class);
            if (executeMethod.getDeclaringClass() == AbstractMcpTool.class) {
                throw new IllegalStateException(toolClass.getSimpleName()
                        + " 既没有 @McpTool 注解也未覆写 execute()，无法注册为 MCP Tool");
            }
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("反射 execute() 失败: " + toolClass.getSimpleName(), e);
        }
    }

    /**
     * 契约测试夹具的不可变持有者。
     *
     * @param registry       Tool 注册表
     * @param promptRegistry Prompt 注册表
     * @param sessionManager session 管理
     * @param properties     FlightHub 配置
     * @param objectMapper   Jackson 实例
     */
    public record Fixture(McpToolRegistry registry, McpPromptRegistry promptRegistry,
                          SessionManager sessionManager, FlightHubProperties properties,
                          ObjectMapper objectMapper) {

        /**
         * 基于本夹具创建一个 stdio 客户端。
         *
         * @return stdio 客户端
         */
        public StdioJsonRpcClient newClient() {
            return new StdioJsonRpcClient(
                    registry, promptRegistry, objectMapper, properties);
        }
    }
}
