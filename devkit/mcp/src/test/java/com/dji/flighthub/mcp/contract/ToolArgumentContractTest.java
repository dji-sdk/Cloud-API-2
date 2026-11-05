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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * C 层：参数错误契约（C1–C4）。
 *
 * <p>本层只钉住「不崩传输层」这一结构性质：任何缺参/类型错误都只能产生
 * 「成功的 JSON-RPC 信封」或 {@code -32603}，不得变成 {@code -32700/-32601/-32602}。
 * C1/C3 对成功信封只校验 {@code isError} 是布尔值，**不**要求它为 true——
 * 多数 Tool 对空参数或类型错误的参数会宽容放行，强制 isError:true 会把契约测试
 * 变成业务语义测试。{@code isError:true} 的精确语义只对 C2 白名单断言，
 * 且不断言错误文案（各 Tool 文案不统一）。</p>
 *
 * <p>C4（覆盖率可见性）由 C1 打印的「参与/跳过」名单、C3 打印的 {@code -32603} 降级名单
 * 以及下面的 {@code c4} 分类断言共同实现，不做静默截断。</p>
 *
 * @author DJI DevKit Team
 */
class ToolArgumentContractTest {

    /** 单条工具调用的读取上限，防止某个 Tool 卡住整层 */
    private static final Duration TOOL_READ_TIMEOUT = Duration.ofSeconds(5);

    /** 传输层可以合法返回的唯一错误码（内部错误） */
    private static final int ALLOWED_ERROR_CODE = -32603;

    /** 被禁止的错误码：说明请求在到达 Tool 之前就被误判 */
    private static final List<Integer> FORBIDDEN_ERROR_CODES = List.of(-32700, -32601, -32602);

    /** 首个请求 id；A/B/C/D 层一律用 int 字面量，避免 Jackson 读回类型不一致 */
    private static final int FIRST_REQUEST_ID = 1000;

    /** JSON-RPC 协议版本字面量 */
    private static final String JSON_RPC_VERSION = "2.0";

    private static final String METHOD_TOOLS_CALL = "tools/call";

    private static final String FIELD_JSONRPC = "jsonrpc";

    private static final String FIELD_ID = "id";

    private static final String FIELD_METHOD = "method";

    private static final String FIELD_PARAMS = "params";

    private static final String FIELD_ARGUMENTS = "arguments";

    private static final String FIELD_ERROR = "error";

    private static final String FIELD_NAME = "name";

    private static final String FIELD_INPUT_SCHEMA = "inputSchema";

    private static final String FIELD_REQUIRED = "required";

    private static final String FIELD_PROPERTIES = "properties";

    private static final String FIELD_TYPE = "type";

    private static final String FIELD_ERROR_MESSAGE = "error_message";

    private static final String JSON_TYPE_ARRAY = "array";

    /**
     * C2 白名单：tool -> 被移除的必填参数。
     * 选中的参数校验必须排在该 Tool 的 control authority / DRC / SDK 调用之前。
     */
    private static final Map<String, String> WHITELIST = Map.of(
            "fh_takeoff", "drone_sn",
            "fh_searchlight", "drone_sn",
            "fh_device_manage", "action");

    private static final String DUMMY_STRING = "contract-value";

    private ContractFixtures.Fixture fixture;

    private StdioJsonRpcClient client;

    private ObjectMapper objectMapper;

    private List<Map<String, Object>> definitions;

    private int nextId = FIRST_REQUEST_ID;

    /**
     * 最近一次 {@link #send} 使用的请求 id。
     * 这是 {@code send -> lastId -> assertEnvelope} 的隐式侧信道：{@code send} 在写入后立即赋值，
     * 调用方必须在同一次循环迭代内使用，不得缓存跨迭代的 id。
     */
    private int lastId;

    @BeforeEach
    void setUp() {
        fixture = ContractFixtures.create();
        client = fixture.newClient();
        objectMapper = fixture.objectMapper();
        definitions = fixture.registry().getToolDefinitions();
    }

    @AfterEach
    void tearDown() {
        client.close();
    }

    /**
     * C1：空参数下，传输层不得把调用误判成 JSON-RPC 客户端错误。
     */
    @Test
    void c1_emptyArguments_shouldNeverBeRejectedByTransport() {
        List<String> participating = new ArrayList<>();
        List<String> skipped = new ArrayList<>();
        List<String> failures = new ArrayList<>();

        for (Map<String, Object> definition : definitions) {
            String name = (String) definition.get(FIELD_NAME);
            List<String> required = requiredOf(definition);
            if (required.isEmpty()) {
                skipped.add(name);
                continue;
            }
            participating.add(name);
            CallOutcome outcome = invokeAndCheck(name, Map.of());
            if (outcome.failure() != null) {
                failures.add(name + " -> " + outcome.failure());
            }
        }

        System.out.println("[C4] 参与 C 层的 Tool 数=" + participating.size() + ": " + participating);
        System.out.println("[C4] 跳过（required 为空）的 Tool=" + skipped);

        assertTrue(failures.isEmpty(), "以下 Tool 在空参数下崩了传输层:\n" + String.join("\n", failures));
    }

    /**
     * C2：白名单 Tool 缺失必填参数时应返回 isError:true，且 error_message 非空。
     *
     * <p>不断言错误文案是否包含参数名：各 Tool 文案不统一，强制包含会把契约测试变成业务文案测试。</p>
     */
    @Test
    void c2_missingWhitelistedParameter_shouldReturnToolError() {
        List<String> failures = new ArrayList<>();
        for (Map.Entry<String, String> entry : WHITELIST.entrySet()) {
            String toolName = entry.getKey();
            String missingParam = entry.getValue();
            Map<String, Object> definition = findDefinition(toolName);
            List<String> required = requiredOf(definition);
            // 白名单参数必须真的在 required 里，否则删除它构造不出「缺参」场景
            if (!required.contains(missingParam)) {
                failures.add(toolName + " -> 白名单参数 " + missingParam + " 不在 required " + required + " 中");
                continue;
            }

            Map<String, Object> arguments = new LinkedHashMap<>();
            for (String requiredName : required) {
                if (!requiredName.equals(missingParam)) {
                    arguments.put(requiredName, DUMMY_STRING);
                }
            }

            Map<String, Object> response;
            try {
                response = send(toolName, arguments);
            } catch (AssertionError e) {
                failures.add(toolName + " -> 传输层调用失败: " + e.getMessage());
                continue;
            }
            try {
                ContractAssertions.assertEnvelope(response, lastId);
                ContractAssertions.assertToolEnvelope(response);
                if (!ContractAssertions.isToolError(response)) {
                    failures.add(toolName + " -> 缺 " + missingParam + " 时应返回 isError:true");
                    continue;
                }
                Object errorMessage = ContractAssertions.toolPayload(response).get(FIELD_ERROR_MESSAGE);
                // 只要求非空字符串：错误文案的具体措辞属于各 Tool 的业务实现，契约层不锁定
                if (!(errorMessage instanceof String text) || text.isBlank()) {
                    failures.add(toolName + " -> error_message 必须非空");
                }
            } catch (AssertionError e) {
                failures.add(toolName + " -> " + e.getMessage());
            }
        }
        assertTrue(failures.isEmpty(), String.join("\n", failures));
    }

    /**
     * C3：首个必填参数类型错误时，传输层同样不得返回客户端错误码。
     */
    @Test
    void c3_typeMismatchedArgument_shouldNeverBeRejectedByTransport() {
        List<String> failures = new ArrayList<>();
        List<String> internalErrors = new ArrayList<>();

        for (Map<String, Object> definition : definitions) {
            String name = (String) definition.get(FIELD_NAME);
            List<String> required = requiredOf(definition);
            if (required.isEmpty()) {
                continue;
            }
            String firstRequired = required.get(0);
            Object badValue = badValueFor(definition, firstRequired);
            CallOutcome outcome = invokeAndCheck(name, Map.of(firstRequired, badValue));
            if (outcome.failure() == null) {
                if (outcome.errorCode() != null && outcome.errorCode() == ALLOWED_ERROR_CODE) {
                    internalErrors.add(name);
                }
                continue;
            }
            failures.add(name + " -> " + outcome.failure());
        }

        System.out.println("[C4] 返回 -32603 的 Tool=" + internalErrors);
        assertTrue(failures.isEmpty(), "以下 Tool 的类型错误崩了传输层:\n" + String.join("\n", failures));
    }

    /**
     * C4：全部已注册 Tool 必须被显式分类，且参与集不能为空（防止空跑通过）。
     */
    @Test
    void c4_everyRegisteredTool_shouldBeClassified() {
        List<String> participating = new ArrayList<>();
        List<String> skipped = new ArrayList<>();
        for (Map<String, Object> definition : definitions) {
            String name = (String) definition.get(FIELD_NAME);
            if (requiredOf(definition).isEmpty()) {
                skipped.add(name);
            } else {
                participating.add(name);
            }
        }
        // 参与 + 跳过必须恰好等于快照数：遍历若被提前 break、或某个 definition 半途缺失，
        // 两边同时缩水也不会被发现，因此这里对齐的是「快照」而不是 definitions.size()
        assertEquals(ContractFixtures.EXPECTED_TOOL_COUNT, participating.size() + skipped.size(),
                "工具分类数与快照不一致：参与=" + participating.size() + " 跳过=" + skipped.size()
                        + " 快照=" + ContractFixtures.EXPECTED_TOOL_COUNT);
        System.out.println("[C4] 分类完成：参与=" + participating.size() + " 跳过=" + skipped.size()
                + " 合计=" + definitions.size());
        assertTrue(participating.size() > 0, "没有任何 Tool 声明 required，C1/C3 实际未执行任何用例");
    }

    /**
     * 校验单次 tools/call 的传输层契约：信封形状、错误码归属、成功信封的 isError 类型。
     *
     * @param toolName  Tool 名
     * @param arguments 参数
     * @return 校验结果；失败时带上可定位到 Tool 的原因
     */
    private CallOutcome invokeAndCheck(String toolName, Map<String, Object> arguments) {
        Map<String, Object> response;
        try {
            response = send(toolName, arguments);
        } catch (AssertionError e) {
            return new CallOutcome("传输层调用失败: " + e.getMessage(), null);
        }
        try {
            ContractAssertions.assertEnvelope(response, lastId);
            if (response.containsKey(FIELD_ERROR)) {
                int code = ContractAssertions.errorCode(response);
                if (FORBIDDEN_ERROR_CODES.contains(code)) {
                    return new CallOutcome("被误判为客户端错误: " + code, code);
                }
                return code == ALLOWED_ERROR_CODE
                        ? new CallOutcome(null, code)
                        : new CallOutcome("不允许的错误码: " + code, code);
            }
            // 成功信封必须带布尔 isError；非布尔会由 isToolError 抛 AssertionError
            ContractAssertions.isToolError(response);
            return new CallOutcome(null, null);
        } catch (AssertionError e) {
            return new CallOutcome("信封不变量失败: " + e.getMessage(), null);
        }
    }

    /**
     * 发送一次 tools/call 请求并读取匹配 id 的响应。
     *
     * <p>读取失败（真超时、响应非法 JSON、id 错位）以 {@link AssertionError} 抛出，
     * 保留传输层原始原因，由调用方归因到具体 Tool。</p>
     *
     * @param toolName  Tool 名
     * @param arguments 参数
     * @return 响应
     */
    private Map<String, Object> send(String toolName, Map<String, Object> arguments) {
        int id = nextId++;
        lastId = id;
        Map<String, Object> request = new LinkedHashMap<>();
        request.put(FIELD_JSONRPC, JSON_RPC_VERSION);
        request.put(FIELD_ID, id);
        request.put(FIELD_METHOD, METHOD_TOOLS_CALL);
        request.put(FIELD_PARAMS, Map.of(FIELD_NAME, toolName, FIELD_ARGUMENTS, arguments));
        return client.send(request, TOOL_READ_TIMEOUT);
    }

    /**
     * 按 Tool 名查找已注册定义。
     *
     * @param toolName Tool 名
     * @return 定义
     */
    private Map<String, Object> findDefinition(String toolName) {
        for (Map<String, Object> definition : definitions) {
            if (toolName.equals(definition.get(FIELD_NAME))) {
                return definition;
            }
        }
        throw new IllegalStateException("白名单里的 Tool 未注册: " + toolName);
    }

    /**
     * 取 inputSchema 里的 required 列表。
     *
     * @param definition Tool 定义
     * @return required 参数名列表
     */
    private List<String> requiredOf(Map<String, Object> definition) {
        JsonNode required = schemaOf(definition).path(FIELD_REQUIRED);
        List<String> names = new ArrayList<>();
        if (required.isArray()) {
            for (JsonNode item : required) {
                names.add(item.asText());
            }
        }
        return names;
    }

    /**
     * 构造与声明类型不相容的取值：array 传对象，其余传数组。
     *
     * @param definition Tool 定义
     * @param property   属性名
     * @return 类型错误的取值
     */
    private Object badValueFor(Map<String, Object> definition, String property) {
        JsonNode type = schemaOf(definition).path(FIELD_PROPERTIES).path(property).path(FIELD_TYPE);
        return JSON_TYPE_ARRAY.equals(type.asText()) ? Map.of() : List.of();
    }

    /**
     * 取 Tool 定义的 inputSchema 节点。
     *
     * @param definition Tool 定义
     * @return inputSchema 节点
     */
    private JsonNode schemaOf(Map<String, Object> definition) {
        Object schema = definition.get(FIELD_INPUT_SCHEMA);
        assertNotNull(schema, definition.get(FIELD_NAME) + ": 缺少 inputSchema");
        return objectMapper.valueToTree(schema);
    }

    /**
     * 单次 tools/call 的传输层校验结果。
     *
     * @param failure   失败原因；null 表示通过
     * @param errorCode 响应里的 JSON-RPC 错误码；成功信封或传输失败时为 null
     */
    private record CallOutcome(String failure, Integer errorCode) {
    }
}
