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

import com.dji.flighthub.mcp.transport.StdioJsonRpcClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A 层：stdio 传输的 JSON-RPC 协议契约（A1–A14 + A6b）。
 *
 * <p>共 19 个用例执行（16 个 {@code @Test}，加上 A6b 参数化 ×3）。</p>
 *
 * <p>用例走完整链路（processLine → dispatch → 写回），因此能钉住错误码与通知语义。</p>
 *
 * @author DJI DevKit Team
 */
class ProtocolContractTest {

    private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

    /** 通知的静默观察窗口 */
    private static final Duration QUIET_PERIOD = Duration.ofMillis(500);

    /** 仅用于验证「handler 抛异常 → -32603」映射的临时 Tool 名（不入 tools/list 契约快照） */
    private static final String CONTRACT_THROWING_TOOL = "contract_test_throwing_tool";

    private ContractFixtures.Fixture fixture;

    private StdioJsonRpcClient client;

    @BeforeEach
    void setUp() {
        fixture = ContractFixtures.create();
        client = fixture.newClient();
    }

    @AfterEach
    void tearDown() {
        client.close();
    }

    @Test
    void initialize_shouldReturnProtocolVersionAndServerInfo() {
        Map<String, Object> response = client.send(request(1, "initialize", Map.of(
                "protocolVersion", "2025-06-18",
                "capabilities", Map.of(),
                "clientInfo", Map.of("name", "contract-test", "version", "1.0"))));

        ContractAssertions.assertEnvelope(response, 1);
        Map<String, Object> result = ContractAssertions.resultOf(response);
        assertTrue(result.get("protocolVersion") instanceof String version && !version.isBlank(),
                "protocolVersion 必须是非空字符串");
        assertTrue(result.get("capabilities") instanceof Map<?, ?> capabilities
                        && capabilities.containsKey("tools"),
                "capabilities.tools 必须存在");
        assertTrue(result.get("serverInfo") instanceof Map<?, ?> info
                        && info.get("name") instanceof String name && !name.isBlank()
                        && info.get("version") instanceof String serverVersion && !serverVersion.isBlank(),
                "serverInfo.name / serverInfo.version 必须非空");
    }

    @Test
    void ping_shouldReturnEmptyResultObject() {
        Map<String, Object> response = client.send(request(2, "ping", null));

        ContractAssertions.assertEnvelope(response, 2);
        assertTrue(ContractAssertions.resultOf(response).isEmpty(), "ping 的 result 必须是空对象");
    }

    @Test
    void toolsList_shouldReturnWellFormedToolDefinitions() {
        Map<String, Object> response = client.send(request(3, "tools/list", null));

        ContractAssertions.assertEnvelope(response, 3);
        Object tools = ContractAssertions.resultOf(response).get("tools");
        assertTrue(tools instanceof List<?> list, "result.tools 必须是数组");
        List<?> toolList = (List<?>) tools;
        assertEquals(fixture.registry().getToolCount(), toolList.size(), "tools/list 数量必须等于注册数");
        for (Object item : toolList) {
            assertTrue(item instanceof Map, "tools 的每一项必须是对象");
            Map<?, ?> tool = (Map<?, ?>) item;
            assertTrue(tool.get("name") instanceof String, "tool.name 必须是字符串");
            assertTrue(tool.get("description") instanceof String, "tool.description 必须是字符串");
            assertTrue(tool.get("inputSchema") instanceof Map, "tool.inputSchema 必须是对象");
        }
    }

    @Test
    void promptsList_shouldReturnPromptArray() {
        Map<String, Object> response = client.send(request(4, "prompts/list", null));

        ContractAssertions.assertEnvelope(response, 4);
        assertTrue(ContractAssertions.resultOf(response).get("prompts") instanceof List,
                "result.prompts 必须是数组");
    }

    @Test
    void promptsGet_shouldReturnInvalidParams_whenNameMissing() {
        Map<String, Object> response = client.send(request(5, "prompts/get", Map.of()));

        ContractAssertions.assertEnvelope(response, 5);
        ContractAssertions.assertErrorCode(response, -32602);
    }

    @ParameterizedTest
    @ValueSource(strings = {"not json", "[]", "{\"unclosed\""})
    void parseError_shouldReturnParseError_whenPayloadIsInvalid(String payload) {
        client.sendRaw(payload);
        Map<String, Object> response = client.read(READ_TIMEOUT);

        ContractAssertions.assertEnvelope(response, null);
        ContractAssertions.assertErrorCode(response, -32700);
    }

    @Test
    void missingMethod_shouldReturnMethodNotFound_whenBodyIsEmptyObject() {
        Map<String, Object> response = client.send(Map.of("jsonrpc", "2.0"));

        ContractAssertions.assertEnvelope(response, null);
        ContractAssertions.assertErrorCode(response, -32601);
    }

    @Test
    void missingMethod_shouldReturnMethodNotFound_whenJsonrpcVersionIsLegacy() {
        Map<String, Object> response = client.send(Map.of("jsonrpc", "1.0", "id", 61));

        ContractAssertions.assertEnvelope(response, 61);
        ContractAssertions.assertErrorCode(response, -32601);
    }

    @Test
    void unknownMethod_shouldReturnMethodNotFound() {
        Map<String, Object> response = client.send(request(7, "tools/unknown", null));

        ContractAssertions.assertEnvelope(response, 7);
        ContractAssertions.assertErrorCode(response, -32601);
    }

    @Test
    void nonStringMethod_shouldReturnMethodNotFound() {
        int id = 8;
        for (Object badMethod : List.<Object>of(42, Map.of("nested", true), List.of("x"))) {
            Map<String, Object> response = client.send(Map.of(
                    "jsonrpc", "2.0", "id", id, "method", badMethod));

            ContractAssertions.assertEnvelope(response, id);
            ContractAssertions.assertErrorCode(response, -32601);
            id++;
        }
    }

    @Test
    void toolsCall_shouldReturnInvalidParams_whenNameIsInvalid() {
        int id = 9;
        for (Map<String, Object> params : List.<Map<String, Object>>of(
                Map.of(), Map.of("name", 42), Map.of("name", "   "))) {
            Map<String, Object> response = client.send(request(id, "tools/call", params));

            ContractAssertions.assertEnvelope(response, id);
            ContractAssertions.assertErrorCode(response, -32602);
            id++;
        }
    }

    @Test
    void toolsCall_shouldReturnInvalidParams_whenArgumentsIsNotObject() {
        int id = 10;
        for (Map<String, Object> params : List.<Map<String, Object>>of(
                Map.of("name", "fh_ops", "arguments", "not-an-object"),
                Map.of("name", "fh_ops", "arguments", List.of("x")),
                Map.of("name", "fh_ops", "arguments", 5))) {
            Map<String, Object> response = client.send(request(id, "tools/call", params));

            ContractAssertions.assertEnvelope(response, id);
            ContractAssertions.assertErrorCode(response, -32602);
            id++;
        }
    }

    @Test
    void toolsCall_shouldReturnToolErrorEnvelope_whenToolDoesNotExist() {
        Map<String, Object> response = client.send(request(11, "tools/call",
                Map.of("name", "fh_does_not_exist", "arguments", Map.of())));

        ContractAssertions.assertEnvelope(response, 11);
        ContractAssertions.assertToolEnvelope(response);
        assertTrue(ContractAssertions.isToolError(response), "未知工具必须返回 isError:true");
        Object errorMessage = ContractAssertions.toolPayload(response).get("error_message");
        assertTrue(errorMessage instanceof String text
                        && text.startsWith("Capability not available: '"),
                "error_message 必须以 Capability not available 开头，实际: " + errorMessage);
    }

    /**
     * 偏差 #2 的契约钉子：工具处理器内部抛异常必须映射为 -32603，而不是被当成解析错误 -32700。
     *
     * <p>刻意在用例内注册一个只属于本用例的抛异常 handler，而不是借某个生产 Tool 的参数强转来
     * 制造异常：断言对象是传输层的「异常 → 错误码」映射，而生产 Tool 的参数处理会随安全加固
     * 演进（例如 {@code FhOpsTool} 已改用 {@code getStringArg}，非字符串 action 不再抛
     * {@code ClassCastException}），把契约钉子绑在参数强转上会随实现变更误报。</p>
     *
     * <p>每个用例的夹具都是新建的，该 handler 不会污染 tools/list 的数量契约。</p>
     */
    @Test
    void toolsCall_shouldReturnInternalError_whenToolHandlerThrows() {
        fixture.registry().register(
                Map.of("name", CONTRACT_THROWING_TOOL,
                        "description", "Contract-test only handler that always throws",
                        "inputSchema", Map.of("type", "object", "properties", Map.of())),
                args -> {
                    throw new IllegalStateException("contract-test handler failure");
                });

        Map<String, Object> response = client.send(request(12, "tools/call",
                Map.of("name", CONTRACT_THROWING_TOOL, "arguments", Map.of())));

        ContractAssertions.assertEnvelope(response, 12);
        ContractAssertions.assertErrorCode(response, -32603);
    }

    @Test
    void notificationsInitialized_shouldProduceNoResponse() {
        client.sendRaw("{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}");

        client.assertNoResponse(QUIET_PERIOD, "notifications/initialized");
    }

    @Test
    void toolsListNotification_shouldProduceNoResponse() {
        client.sendRaw("{\"jsonrpc\":\"2.0\",\"method\":\"tools/list\"}");

        client.assertNoResponse(QUIET_PERIOD, "tools/list 通知");
    }

    /**
     * 通知不仅要「不回响应」，还必须「不执行」——用计数探针钉住后者。
     *
     * <p>探针工具只在本用例的夹具上注册，不影响其他用例的 tools/list 计数。</p>
     */
    @Test
    void toolsCallNotification_shouldProduceNoResponseNorExecute() {
        AtomicInteger probeInvocations = new AtomicInteger();
        Map<String, Object> probeDefinition = new LinkedHashMap<>();
        probeDefinition.put("name", "fh_probe");
        probeDefinition.put("description", "Counting probe for notification semantics");
        probeDefinition.put("inputSchema", Map.of("type", "object", "properties", Map.of()));
        fixture.registry().register(probeDefinition, args -> {
            probeInvocations.incrementAndGet();
            return Map.of("success", true);
        });

        client.sendRaw("{\"jsonrpc\":\"2.0\",\"method\":\"tools/call\","
                + "\"params\":{\"name\":\"fh_probe\",\"arguments\":{}}}");

        client.assertNoResponse(QUIET_PERIOD, "tools/call 通知");
        assertEquals(0, probeInvocations.get(), "通知（无 id）不得真正执行工具处理器");
    }

    private static Map<String, Object> request(Object id, String method, Map<String, Object> params) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("jsonrpc", "2.0");
        message.put("id", id);
        message.put("method", method);
        if (params != null) {
            message.put("params", params);
        }
        return message;
    }
}
