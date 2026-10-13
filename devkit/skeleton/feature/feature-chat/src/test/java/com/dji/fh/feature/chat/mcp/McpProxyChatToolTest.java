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

package com.dji.fh.feature.chat.mcp;

import static org.assertj.core.api.Assertions.assertThat;

import com.dji.fh.feature.chat.tool.NavigationHolder;
import com.dji.fh.feature.chat.tool.ToolCallContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * {@link McpProxyChatTool} 单元测试。
 *
 * <p>使用 {@link FakeMcpClient} 代替真实 MCP Server 连接。
 *
 * @author DJI DevKit Team
 */
class McpProxyChatToolTest {

    private static final String TOOL_NAME = "fh_drone_snapshot";
    private static final String DESCRIPTION = "Get real-time drone status snapshot";
    private static final String INPUT_SCHEMA = """
            {"type":"object","properties":{"drone_sn":{"type":"string","description":"Target drone SN"}},"required":["drone_sn"]}""";
    private static final String REQUEST_ID = "mcp-test-001";

    @BeforeEach
    void setUp() {
        ToolCallContext.set(REQUEST_ID, event -> { });
    }

    @AfterEach
    void tearDown() {
        NavigationHolder.clear(REQUEST_ID);
        ToolCallContext.clear(REQUEST_ID);
    }

    @Test
    void getToolDefinition_shouldReturnCorrectNameAndSchema() {
        McpClient fakeClient = new FakeMcpClient("{\"battery\":75}");
        McpProxyChatTool tool = new McpProxyChatTool(TOOL_NAME, DESCRIPTION, INPUT_SCHEMA, fakeClient);

        assertThat(tool.getToolDefinition().name()).isEqualTo(TOOL_NAME);
        assertThat(tool.getToolDefinition().description()).isEqualTo(DESCRIPTION);
        assertThat(tool.getToolDefinition().inputSchema()).isEqualTo(INPUT_SCHEMA);
    }

    @Test
    void call_shouldReturnMcpResult_whenSuccessful() {
        String expectedResult = "{\"battery_percent\":75,\"altitude\":120}";
        McpClient fakeClient = new FakeMcpClient(expectedResult);
        McpProxyChatTool tool = new McpProxyChatTool(TOOL_NAME, DESCRIPTION, INPUT_SCHEMA, fakeClient);

        String result = tool.call("{\"drone_sn\":\"DRONE-001\"}");
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void call_shouldReturnError_whenMcpClientThrows() {
        McpClient failClient = new FakeMcpClient(new McpClientException("连接超时"));
        McpProxyChatTool tool = new McpProxyChatTool(TOOL_NAME, DESCRIPTION, INPUT_SCHEMA, failClient);

        String result = tool.call("{\"drone_sn\":\"DRONE-001\"}");
        assertThat(result).contains("mcp_error");
        assertThat(result).contains("连接超时");
    }

    @Test
    void call_shouldReturnError_whenInvalidJson() {
        McpClient fakeClient = new FakeMcpClient("{}");
        McpProxyChatTool tool = new McpProxyChatTool(TOOL_NAME, DESCRIPTION, INPUT_SCHEMA, fakeClient);

        String result = tool.call("not-json");
        assertThat(result).contains("internal_error");
    }

    // ─── NavigationHolder 相关测试 ──────────────────────────────────────

    @Test
    void call_shouldOfferNavigation_whenTakeoffSucceeds() {
        String successResult = "{\"data\":{\"status\":\"takeoff_initiated\"},\"message\":\"OK\"}";
        McpClient fakeClient = new FakeMcpClient(successResult);
        McpProxyChatTool tool = new McpProxyChatTool("fh_takeoff", "Takeoff tool", INPUT_SCHEMA, fakeClient);

        tool.call("{\"drone_sn\":\"DRONE-001\",\"dock_sn\":\"DOCK-001\",\"target_height\":50,\"confirm\":true}");

        NavigationHolder.Payload nav = NavigationHolder.take(REQUEST_ID);
        assertThat(nav).isNotNull();
        assertThat(nav.getPath()).isEqualTo("/cockpit");
        assertThat(nav.getQuery()).containsEntry("drone_sn", "DRONE-001");
    }

    @Test
    void call_shouldNotOfferNavigation_whenTakeoffResultHasError() {
        String errorResult = "{\"error\":\"security_blocked\",\"message\":\"需要安全确认\"}";
        McpClient fakeClient = new FakeMcpClient(errorResult);
        McpProxyChatTool tool = new McpProxyChatTool("fh_takeoff", "Takeoff tool", INPUT_SCHEMA, fakeClient);

        tool.call("{\"drone_sn\":\"DRONE-001\",\"dock_sn\":\"DOCK-001\",\"target_height\":50,\"confirm\":true}");

        assertThat(NavigationHolder.take(REQUEST_ID)).isNull();
    }

    @Test
    void call_shouldNotOfferNavigation_whenOtherToolSucceeds() {
        String successResult = "{\"data\":{\"battery\":80},\"message\":\"OK\"}";
        McpClient fakeClient = new FakeMcpClient(successResult);
        McpProxyChatTool tool = new McpProxyChatTool("fh_drone_snapshot", DESCRIPTION, INPUT_SCHEMA, fakeClient);

        tool.call("{\"drone_sn\":\"DRONE-001\"}");

        assertThat(NavigationHolder.take(REQUEST_ID)).isNull();
    }

    @Test
    void call_shouldNotOfferNavigation_whenDroneSnMissing() {
        String successResult = "{\"data\":{\"status\":\"ok\"},\"message\":\"OK\"}";
        McpClient fakeClient = new FakeMcpClient(successResult);
        McpProxyChatTool tool = new McpProxyChatTool("fh_takeoff", "Takeoff tool", INPUT_SCHEMA, fakeClient);

        tool.call("{\"dock_sn\":\"DOCK-001\",\"target_height\":50,\"confirm\":true}");

        assertThat(NavigationHolder.take(REQUEST_ID)).isNull();
    }

    @Test
    void call_shouldNotOfferNavigation_whenTakeoffResultHasNoDataField() {
        // 无 data 字段（即使无 error 字段）也不应触发导航，避免误判
        String ambiguousResult = "{\"result\":\"failed\",\"message\":\"drone busy\"}";
        McpClient fakeClient = new FakeMcpClient(ambiguousResult);
        McpProxyChatTool tool = new McpProxyChatTool("fh_takeoff", "Takeoff tool", INPUT_SCHEMA, fakeClient);

        tool.call("{\"drone_sn\":\"DRONE-001\",\"dock_sn\":\"DOCK-001\",\"target_height\":50,\"confirm\":true}");

        assertThat(NavigationHolder.take(REQUEST_ID)).isNull();
    }

    /**
     * 测试用的 FakeMcpClient — 继承 McpClient 但覆写 callTool。
     * 因 Java 24 不支持 mock 具体类，采用子类覆写方式。
     */
    private static class FakeMcpClient extends McpClient {

        private final String fakeResponse;
        private final McpClientException fakeException;

        FakeMcpClient(String fakeResponse) {
            super(createTestProperties());
            this.fakeResponse = fakeResponse;
            this.fakeException = null;
        }

        FakeMcpClient(McpClientException exception) {
            super(createTestProperties());
            this.fakeResponse = null;
            this.fakeException = exception;
        }

        @Override
        public String callTool(String toolName, java.util.Map<String, Object> arguments) {
            if (fakeException != null) {
                throw fakeException;
            }
            return fakeResponse;
        }

        private static McpClientProperties createTestProperties() {
            McpClientProperties props = new McpClientProperties();
            props.setUrl("http://localhost:9999");
            return props;
        }
    }
}
