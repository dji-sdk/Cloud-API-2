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

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.context.RequestContext;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.transport.ClientRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Sinks;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhConnectTool 单元测试。
 * 覆盖重复连接检查、参数校验、Mock 模式初始化成功与 SDK 初始化失败，
 * 以及 session 自动绑定到 ClientRegistry 的行为。
 *
 * <p>access_token 和 workspace_uuid 不再由调用方传入，始终从环境变量/配置读取。
 * 如需测试不同 token/workspace 场景，修改 {@code properties.getAuth()} 即可。
 *
 * @author DJI FlightHub SDK Team
 */
class FhConnectToolTest {

    private static final String SERVER_ADDRESS = "https://test.flighthub.com";
    private static final String WORKSPACE_UUID = "ws-12345678-abcd";
    private static final String TEST_CLIENT_ID = "test-client-001";

    private FlightHubProperties properties;
    private SessionManager sessionManager;
    private SdkService sdkService;
    private ClientRegistry clientRegistry;
    private FhConnectTool connectTool;

    @BeforeEach
    void setUp() {
        properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        properties.getServer().setAddress(null);
        properties.getServer().setPort(443);
        // access_token 和 workspace_uuid 由环境变量/配置注入，测试中预设到 properties
        properties.getAuth().setAccessToken("test-token");
        properties.getAuth().setWorkspaceUuid(WORKSPACE_UUID);

        sessionManager = new SessionManager(properties);
        clientRegistry = new ClientRegistry();

        sdkService = new SdkService() {
            @Override
            public boolean isMockMode() {
                return true;
            }
        };

        connectTool = new FhConnectTool(properties, sessionManager, sdkService, clientRegistry);
    }

    @AfterEach
    void tearDown() {
        RequestContext.clear();
    }

    @Test
    void getToolDefinition_shouldReturnCorrectName() {
        Map<String, Object> def = connectTool.getToolDefinition();

        assertEquals("fh_connect", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));
    }

    @Test
    void execute_shouldReturnError_whenAlreadyConnected() {
        // 先创建一个 session
        sessionManager.createSession(WORKSPACE_UUID, "ExistingWorkspace");

        Map<String, Object> args = new HashMap<>();
        args.put("server_address", SERVER_ADDRESS);

        Map<String, Object> result = connectTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Already connected"));
    }

    @Test
    void execute_shouldUseDefaultAddress_whenServerAddressMissing() {
        // 不提供 server_address，且 properties 中也为 null → 使用默认地址连接
        Map<String, Object> result = connectTool.execute(Map.of());

        // Mock 模式下默认地址也能连接成功
        assertEquals(true, result.get("success"));
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(FhConnectTool.DEFAULT_SERVER_ADDRESS, data.get("server_address"));
    }

    @Test
    void execute_shouldUseDefaultAddress_whenServerAddressBlank() {
        Map<String, Object> args = new HashMap<>();
        args.put("server_address", "   ");

        Map<String, Object> result = connectTool.execute(args);

        // Mock 模式下默认地址也能连接成功
        assertEquals(true, result.get("success"));
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(FhConnectTool.DEFAULT_SERVER_ADDRESS, data.get("server_address"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_whenMockMode() {
        // access_token 和 workspace_uuid 来自 properties（setUp 已配置），无需在 args 传入
        Map<String, Object> args = new HashMap<>();
        args.put("server_address", SERVER_ADDRESS);
        args.put("port", 443);

        Map<String, Object> result = connectTool.execute(args);

        assertEquals(true, result.get("success"));
        assertNotNull(result.get("message"));
        assertTrue(result.get("message").toString().contains("Successfully connected"));

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertNotNull(data.get("session_id"));
        assertEquals(SERVER_ADDRESS, data.get("server_address"));
        assertEquals("ready", data.get("status"));
        assertEquals("L3", data.get("max_security_level"));
        // workspace_uuid 来自 properties，不是 args
        assertEquals(WORKSPACE_UUID, data.get("workspace_uuid"));

        // 验证 session 确实被创建
        assertTrue(sessionManager.hasActiveSession());
    }

    @Test
    void execute_shouldReturnError_whenSdkInitFails() {
        // 使用一个总是失败的 SdkService
        SdkService failingSdk = new SdkService() {
            @Override
            public boolean isMockMode() {
                return false;
            }

            @Override
            public synchronized long initialize(String serverAddress, int port, String accessToken,
                                                String workspaceUuid, String notificationServerAddress) {
                return -1;
            }

            @Override
            public synchronized long initialize(String serverAddress, int port, String accessToken,
                                                String workspaceUuid, String notificationServerAddress,
                                                String httpAuthKey, String httpAuthValue) {
                return -1;
            }
        };

        FhConnectTool failTool = new FhConnectTool(properties, sessionManager, failingSdk, clientRegistry);

        Map<String, Object> args = new HashMap<>();
        args.put("server_address", SERVER_ADDRESS);

        Map<String, Object> result = failTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Connection failed"));
        // session 不应被创建
        assertFalse(sessionManager.hasActiveSession());
    }

    @Test
    void execute_shouldReturnError_whenSdkThrowsException() {
        // SDK 抛出异常的场景
        SdkService throwingSdk = new SdkService() {
            @Override
            public boolean isMockMode() {
                return false;
            }

            @Override
            public synchronized long initialize(String serverAddress, int port, String accessToken,
                                                String workspaceUuid, String notificationServerAddress) {
                throw new RuntimeException("JNI native load failure");
            }

            @Override
            public synchronized long initialize(String serverAddress, int port, String accessToken,
                                                String workspaceUuid, String notificationServerAddress,
                                                String httpAuthKey, String httpAuthValue) {
                throw new RuntimeException("JNI native load failure");
            }
        };

        FhConnectTool failTool = new FhConnectTool(properties, sessionManager, throwingSdk, clientRegistry);

        Map<String, Object> args = new HashMap<>();
        args.put("server_address", SERVER_ADDRESS);

        Map<String, Object> result = failTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Connection failed"));
    }

    // ==================== Session 绑定到 ClientRegistry ====================

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldBindSessionToClient_whenConnectSucceeds() {
        // 注册客户端并设置 RequestContext（模拟 SSE 传输层设置）
        Sinks.Many<ServerSentEvent<String>> sink = Sinks.many().unicast().onBackpressureBuffer();
        clientRegistry.register(TEST_CLIENT_ID, sink);
        RequestContext.set(1, "fh_connect", TEST_CLIENT_ID);

        Map<String, Object> args = new HashMap<>();
        args.put("server_address", SERVER_ADDRESS);

        Map<String, Object> result = connectTool.execute(args);

        assertEquals(true, result.get("success"));

        // 验证 session 已绑定到 ClientRegistry
        ClientRegistry.ClientEntry entry = clientRegistry.getClient(TEST_CLIENT_ID);
        assertNotNull(entry);
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(data.get("session_id"), entry.getSessionId(),
                "Session should be bound to client in ClientRegistry after fh_connect success");
    }

    @Test
    void execute_shouldNotBindSession_whenNoRequestContext() {
        // 无 RequestContext（如测试环境直接调用）— 不应抛异常
        Map<String, Object> args = new HashMap<>();
        args.put("server_address", SERVER_ADDRESS);

        Map<String, Object> result = connectTool.execute(args);

        // 连接本身应成功
        assertEquals(true, result.get("success"));
        // 但 ClientRegistry 中无绑定（因为没有 RequestContext）
        assertEquals(0, clientRegistry.getActiveClientCount());
    }

    @Test
    void execute_shouldNotBindSession_whenConnectFails() {
        // 使用总是失败的 SdkService 来模拟连接失败
        SdkService failingSdk = new SdkService() {
            @Override
            public boolean isMockMode() {
                return false;
            }

            @Override
            public synchronized long initialize(String serverAddress, int port, String accessToken,
                                                String workspaceUuid, String notificationServerAddress) {
                throw new RuntimeException("Simulated connection failure");
            }

            @Override
            public synchronized long initialize(String serverAddress, int port, String accessToken,
                                                String workspaceUuid, String notificationServerAddress,
                                                String httpAuthKey, String httpAuthValue) {
                throw new RuntimeException("Simulated connection failure");
            }
        };
        FhConnectTool failingTool = new FhConnectTool(properties, sessionManager, failingSdk, clientRegistry);

        // 注册客户端并设置 RequestContext
        Sinks.Many<ServerSentEvent<String>> sink = Sinks.many().unicast().onBackpressureBuffer();
        clientRegistry.register(TEST_CLIENT_ID, sink);
        RequestContext.set(1, "fh_connect", TEST_CLIENT_ID);

        // 使用显式地址（非默认），避免触发 elicitation 分支
        Map<String, Object> args = new HashMap<>();
        args.put("server_address", SERVER_ADDRESS);
        Map<String, Object> result = failingTool.execute(args);

        assertEquals(false, result.get("success"));

        // session 不应绑定
        ClientRegistry.ClientEntry entry = clientRegistry.getClient(TEST_CLIENT_ID);
        assertNotNull(entry);
        assertNull(entry.getSessionId(),
                "Session should NOT be bound when fh_connect fails");
    }
}
