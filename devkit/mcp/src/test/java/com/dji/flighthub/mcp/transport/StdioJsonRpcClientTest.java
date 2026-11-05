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

package com.dji.flighthub.mcp.transport;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.config.McpPromptRegistry;
import com.dji.flighthub.mcp.config.McpToolRegistry;
import com.dji.flighthub.mcp.contract.ContractAssertions;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link StdioJsonRpcClient} 自检：证明它能把请求送进 StdioTransport 的完整链路并读回响应。
 *
 * @author DJI DevKit Team
 */
class StdioJsonRpcClientTest {

    /** 慢工具的执行耗时，必须明显长于读超时 */
    private static final long SLOW_TOOL_MILLIS = 500L;

    /** 第一次 tools/call 的读超时，必须短于慢工具耗时 */
    private static final Duration SHORT_READ_TIMEOUT = Duration.ofMillis(200);

    /** 超时后等待慢响应落地的时长，确保陈旧帧确实进入队列 */
    private static final long STALE_FRAME_WAIT_MILLIS = 600L;

    /** 后续请求的读超时，需容纳慢工具清理与 ping 往返 */
    private static final Duration NORMAL_READ_TIMEOUT = Duration.ofSeconds(2);

    @Test
    void send_shouldReturnPong_whenMethodIsPing() {
        McpToolRegistry registry = new McpToolRegistry();
        registry.register(Map.of("name", "contract_noop", "description", "契约测试占位工具"),
                args -> Map.of("success", true));

        try (StdioJsonRpcClient client = newClient(registry)) {
            Map<String, Object> response = client.send(Map.of(
                    "jsonrpc", "2.0", "id", 1, "method", "ping"), NORMAL_READ_TIMEOUT);

            ContractAssertions.assertEnvelope(response, 1);
            assertTrue(response.get("result") instanceof Map,
                    "ping 的 result 必须是对象，实际响应: " + response);

            // 重复关闭必须幂等：try-with-resources 退出时会再调用一次 close()
            client.close();
        }
    }

    @Test
    void send_shouldDiscardStaleFrame_whenPreviousCallTimesOut() throws InterruptedException {
        McpToolRegistry registry = new McpToolRegistry();
        registry.register(Map.of("name", "slow_tool", "description", "慢工具，用于制造陈旧帧"),
                args -> {
                    sleep(SLOW_TOOL_MILLIS);
                    return Map.of("success", true);
                });

        try (StdioJsonRpcClient client = newClient(registry)) {
            Map<String, Object> callRequest = Map.of(
                    "jsonrpc", "2.0", "id", 100, "method", "tools/call",
                    "params", Map.of("name", "slow_tool", "arguments", Map.of()));

            // 第一次调用在读超时内拿不到响应，必须抛出超时错误
            assertThrows(AssertionError.class, () -> client.send(callRequest, SHORT_READ_TIMEOUT),
                    "慢工具应在短读超时内触发超时");

            // 等待慢工具的响应写入捕获流，此时它已成为陈旧帧
            Thread.sleep(STALE_FRAME_WAIT_MILLIS);

            Map<String, Object> pong = client.send(Map.of(
                    "jsonrpc", "2.0", "id", 200, "method", "ping"), NORMAL_READ_TIMEOUT);

            // 若陈旧帧未被丢弃，这里会读到 id=100 的 tools/call 响应
            ContractAssertions.assertEnvelope(pong, 200);
            assertEquals(200, pong.get("id"), "必须返回 ping 的响应而不是陈旧的 tools/call 响应");
        }
    }

    private static StdioJsonRpcClient newClient(McpToolRegistry registry) {
        return new StdioJsonRpcClient(registry, new McpPromptRegistry(),
                new ObjectMapper(), new FlightHubProperties());
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("慢工具被中断", e);
        }
    }
}
