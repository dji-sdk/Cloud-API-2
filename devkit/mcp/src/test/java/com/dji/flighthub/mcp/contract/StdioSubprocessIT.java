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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * D 层：真实 fat JAR 的 stdio 帧边界与 stdout 纯净性烟测（D1–D4）。
 *
 * <p>D2 的「每一行 stdout 都是合法 JSON-RPC 消息」由 {@link SubprocessStdioClient}
 * 的 stdout 泵逐行校验并记录首个畸形行，而不是只检查测试主动读到的那几帧。</p>
 *
 * <p>运行前必须先由 Maven 的 package 阶段产出
 * {@code target/flighthub-mcp-server-1.0.0.jar}（pom 的 {@code <version>1.0.0</version>，
 * 没有 -SNAPSHOT）；本类只在 {@code -Pcontract} 的 failsafe 中执行。</p>
 *
 * <p>服务端启动后不会主动输出任何内容，因此 {@link #setUp()} 先发 {@code initialize}
 * 再读取，否则 {@code read} 会一直等到超时。</p>
 *
 * @author DJI DevKit Team
 */
class StdioSubprocessIT {

    private static final String JAR_RELATIVE_PATH = "target/flighthub-mcp-server-1.0.0.jar";

    private static final Duration READ_TIMEOUT = Duration.ofSeconds(60);

    private static final Duration QUIET_PERIOD = Duration.ofSeconds(5);

    private static final String JSON_RPC_VERSION = "2.0";

    private static final String METHOD_INITIALIZE = "initialize";

    private static final String METHOD_TOOLS_LIST = "tools/list";

    private static final String METHOD_TOOLS_CALL = "tools/call";

    private static final int INITIALIZE_REQUEST_ID = 1;

    private static final int TOOLS_LIST_REQUEST_ID = 2;

    private static final int TOOLS_CALL_REQUEST_ID = 3;

    private static final String UNKNOWN_TOOL_NAME = "fh_does_not_exist";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private SubprocessStdioClient client;

    private Map<String, Object> initializeResponse;

    @BeforeEach
    void setUp() {
        Path jarPath = resolveJarPath();
        assertTrue(Files.exists(jarPath), "找不到 fat JAR: " + jarPath + "，请先执行 mvn package");
        client = SubprocessStdioClient.start(jarPath, objectMapper);
        client.send(request(INITIALIZE_REQUEST_ID, METHOD_INITIALIZE, Map.of(
                "protocolVersion", "2025-06-18",
                "capabilities", Map.of(),
                "clientInfo", Map.of("name", "contract-test", "version", "1.0"))));
        initializeResponse = client.read(READ_TIMEOUT);
    }

    @AfterEach
    void tearDown() {
        // setUp 在 JAR 缺失时会先失败，此时 client 尚未赋值，避免用 NPE 掩盖真正的失败原因
        if (client != null) {
            client.close();
        }
    }

    @Test
    void d1_initialize_shouldRespondOverStdio() {
        assertEquals(JSON_RPC_VERSION, initializeResponse.get("jsonrpc"));
        assertNotNull(initializeResponse.get("result"),
                "initialize 必须返回 result；stderr=" + client.stderr());
    }

    @Test
    void d2_everyStdoutLine_shouldBeJsonRpcMessage() {
        // 纯净性由 stdout 泵逐行校验：先制造若干帧，再关闭 stdin 让泵排空，
        // 这样 closeStdin 之后仍滞留在队列里的行也会被检查到（read() 只解析它主动取出的行）
        client.send(notification("notifications/initialized"));
        client.send(request(TOOLS_LIST_REQUEST_ID, METHOD_TOOLS_LIST, null));
        client.read(READ_TIMEOUT);

        client.closeStdin();
        // 用静默窗口给泵一个时间上界：EOF 路径下界是确定的（泵已排空管道），
        // 超时路径只是 5 秒启发式；此处 stdout 仍有输出说明帧边界或退出行为异常
        assertTrue(client.hasNoOutputWithin(QUIET_PERIOD),
                "关闭 stdin 后 stdout 在静默窗口内仍有输出；stderr=" + client.stderr());

        String malformed = client.firstMalformedStdoutLine();
        assertNull(malformed,
                "stdout 出现非 JSON 行（日志污染？）: " + malformed + "；stderr=" + client.stderr());
    }

    @Test
    void d3_fullSession_shouldWorkEndToEnd() {
        client.send(notification("notifications/initialized"));

        client.send(request(TOOLS_LIST_REQUEST_ID, METHOD_TOOLS_LIST, null));
        Map<String, Object> toolsList = client.read(READ_TIMEOUT);
        assertTrue(toolsList.get("result") instanceof Map, "tools/list 必须返回 result");
        assertTrue(((Map<?, ?>) toolsList.get("result")).get("tools") instanceof List<?>,
                "tools/list 的 result.tools 必须是数组");

        client.send(request(TOOLS_CALL_REQUEST_ID, METHOD_TOOLS_CALL,
                Map.of("name", UNKNOWN_TOOL_NAME, "arguments", Map.of())));
        Map<String, Object> callResponse = client.read(READ_TIMEOUT);
        ContractAssertions.assertEnvelope(callResponse, TOOLS_CALL_REQUEST_ID);
        ContractAssertions.assertToolEnvelope(callResponse);
        assertTrue(ContractAssertions.isToolError(callResponse), "未知工具必须返回 isError:true");
    }

    @Test
    void d4_stdout_shouldStop_afterStdinClosed() {
        client.closeStdin();

        assertTrue(client.hasNoOutputWithin(QUIET_PERIOD),
                "关闭 stdin 后 stdout 不应再有输出；stderr=" + client.stderr());
    }

    /**
     * 解析 fat JAR 路径：failsafe 会注入 {@code basedir}，缺失时回退到 {@code user.dir}。
     *
     * @return JAR 路径
     */
    private static Path resolveJarPath() {
        String basedir = System.getProperty("basedir", System.getProperty("user.dir"));
        return Path.of(basedir).resolve(JAR_RELATIVE_PATH);
    }

    private static Map<String, Object> request(Object id, String method, Map<String, Object> params) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("jsonrpc", JSON_RPC_VERSION);
        message.put("id", id);
        message.put("method", method);
        if (params != null) {
            message.put("params", params);
        }
        return message;
    }

    private static Map<String, Object> notification(String method) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("jsonrpc", JSON_RPC_VERSION);
        message.put("method", method);
        return message;
    }
}
