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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * {@link McpClient} 单元测试。
 *
 * <p>覆盖场景：
 * <ul>
 *   <li>初始状态验证（未连接、未初始化）</li>
 *   <li>未初始化时调用抛出异常</li>
 *   <li>已初始化但断连时调用抛出断连异常</li>
 *   <li>shutdown 正确关闭并清理状态</li>
 *   <li>SSE 断线自动触发重连</li>
 *   <li>重连成功恢复 connected 状态</li>
 *   <li>重连禁用时不触发重连</li>
 * </ul>
 *
 * @author DJI DevKit Team
 */
class McpClientTest {

    private static McpClientProperties defaultProps() {
        McpClientProperties props = new McpClientProperties();
        props.setUrl("http://localhost:19999");
        props.setConnectTimeout(Duration.ofSeconds(2));
        props.setTimeout(Duration.ofSeconds(5));
        props.setReconnectEnabled(true);
        props.setReconnectInitialDelay(Duration.ofMillis(100));
        props.setReconnectMaxDelay(Duration.ofMillis(500));
        props.setReconnectMaxAttempts(3);
        return props;
    }

    @Test
    void initialState_shouldBeDisconnectedAndUninitialized() {
        McpClient client = new McpClient(defaultProps());

        assertThat(client.isInitialized()).isFalse();
        assertThat(client.isConnected()).isFalse();
    }

    @Test
    void constructor_shouldAcceptAuthToken_whenConfigured() {
        McpClientProperties props = defaultProps();
        props.setAuthToken("test-token-123");

        McpClient client = new McpClient(props);

        // 仅验证构造不抛异常且初始状态正确，token 透传由集成测试覆盖
        assertThat(client.isInitialized()).isFalse();
        assertThat(client.isConnected()).isFalse();
    }

    @Test
    void constructor_shouldStripTrailingSlash_fromBaseUrl() throws Exception {
        McpClientProperties props = defaultProps();
        props.setUrl("http://localhost:19999/");

        McpClient client = new McpClient(props);

        String baseUrl = (String) reflectiveGet(client, "baseUrl");
        // 去尾部斜杠，避免 connect() 拼出 http://localhost:19999//pf-devkit/ai-plugin/mcp/sse
        assertThat(baseUrl).isEqualTo("http://localhost:19999");
    }

    @Test
    void constructor_shouldStripMcpBasePath_whenUrlAlreadyPrefixed() throws Exception {
        // ISV 误把 base-path 写进 url：http://localhost:19999/pf-devkit/ai-plugin/mcp
        McpClientProperties props = defaultProps();
        props.setUrl("http://localhost:19999/pf-devkit/ai-plugin/mcp");

        McpClient client = new McpClient(props);

        String baseUrl = (String) reflectiveGet(client, "baseUrl");
        // 应剥离 MCP_BASE_PATH，避免 connect() 拼成 .../pf-devkit/ai-plugin/mcp/pf-devkit/ai-plugin/mcp/sse 双重前缀
        assertThat(baseUrl).isEqualTo("http://localhost:19999");
    }

    @Test
    void constructor_shouldStripMcpBasePathAndTrailingSlash_whenUrlAlreadyPrefixedWithSlash()
            throws Exception {
        McpClientProperties props = defaultProps();
        props.setUrl("http://localhost:19999/pf-devkit/ai-plugin/mcp/");

        McpClient client = new McpClient(props);

        String baseUrl = (String) reflectiveGet(client, "baseUrl");
        assertThat(baseUrl).isEqualTo("http://localhost:19999");
    }

    @Test
    void constructor_shouldNotStripUnrelatedPath_fromBaseUrl() throws Exception {
        // url 含其他路径段（非 MCP_BASE_PATH）时不应误剥
        McpClientProperties props = defaultProps();
        props.setUrl("http://localhost:19999/mcp");

        McpClient client = new McpClient(props);

        String baseUrl = (String) reflectiveGet(client, "baseUrl");
        assertThat(baseUrl).isEqualTo("http://localhost:19999/mcp");
    }

    @Test
    void constructor_shouldStripLegacyBasePath_whenUrlHasOldPrefix() throws Exception {
        // 从上一版本升级：旧 base-path 为 /ai-plugin/mcp，常量已改为 /pf-devkit/ai-plugin/mcp。
        // 旧代码会剥离为 http://localhost:19999，升级后须保持等价行为，避免静默 404。
        McpClientProperties props = defaultProps();
        props.setUrl("http://localhost:19999/ai-plugin/mcp");

        McpClient client = new McpClient(props);

        String baseUrl = (String) reflectiveGet(client, "baseUrl");
        assertThat(baseUrl).isEqualTo("http://localhost:19999");
    }

    @Test
    void constructor_shouldStripLegacyBasePathAndTrailingSlash_whenUrlHasOldPrefixWithSlash()
            throws Exception {
        McpClientProperties props = defaultProps();
        props.setUrl("http://localhost:19999/ai-plugin/mcp/");

        McpClient client = new McpClient(props);

        String baseUrl = (String) reflectiveGet(client, "baseUrl");
        assertThat(baseUrl).isEqualTo("http://localhost:19999");
    }

    /**
     * 反射读取私有字段，供 baseUrl 规范化断言使用。
     */
    private static Object reflectiveGet(Object target, String fieldName) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    @Test
    void constructor_shouldAllowBlankAuthToken_whenUnauthenticated() {
        McpClientProperties props = defaultProps();
        props.setAuthToken("");

        McpClient client = new McpClient(props);

        assertThat(client.isConnected()).isFalse();
    }

    @Test
    void callTool_shouldThrow_whenNotInitialized() {
        McpClient client = new McpClient(defaultProps());

        assertThatThrownBy(() -> client.callTool("fh_device_list", Map.of()))
                .isInstanceOf(McpClientException.class)
                .hasMessageContaining("未初始化");
    }

    @Test
    void listTools_shouldThrow_whenNotInitialized() {
        McpClient client = new McpClient(defaultProps());

        assertThatThrownBy(() -> client.listTools())
                .isInstanceOf(McpClientException.class)
                .hasMessageContaining("未初始化");
    }

    @Test
    void shutdown_shouldMarkDisconnected() {
        McpClient client = new McpClient(defaultProps());
        client.shutdown();

        assertThat(client.isConnected()).isFalse();
    }

    @Test
    void shutdown_shouldBeIdempotent() {
        McpClient client = new McpClient(defaultProps());
        client.shutdown();
        client.shutdown();

        assertThat(client.isConnected()).isFalse();
    }

    @Test
    void connectAndInitialize_shouldThrow_whenServerUnreachable() {
        McpClient client = new McpClient(defaultProps());

        assertThatThrownBy(client::connectAndInitialize)
                .isInstanceOf(McpClientException.class)
                .hasMessageContaining("连接初始化失败");
    }

    @Test
    void callTool_shouldThrow_whenDisconnected() {
        // 使用可控的测试子类模拟"曾经连接成功但已断开"的状态
        TestableClient client = new TestableClient(defaultProps());
        client.simulateConnectedThenDisconnected();

        assertThatThrownBy(() -> client.callTool("fh_device_list", Map.of()))
                .isInstanceOf(McpClientException.class)
                .hasMessageContaining("断开");
    }

    @Test
    void reconnect_shouldBeTriggered_whenSseStreamEnds() throws InterruptedException {
        McpClientProperties props = defaultProps();
        props.setReconnectInitialDelay(Duration.ofMillis(50));

        ReconnectTrackingClient client = new ReconnectTrackingClient(props);
        client.simulateConnectedThenSseDisconnect();

        // 等待重连尝试被调度并执行
        boolean reconnected = client.reconnectAttemptLatch.await(2, TimeUnit.SECONDS);
        assertThat(reconnected).isTrue();
        assertThat(client.reconnectAttemptCount.get()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void reconnect_shouldNotTrigger_whenDisabled() throws InterruptedException {
        McpClientProperties props = defaultProps();
        props.setReconnectEnabled(false);

        ReconnectTrackingClient client = new ReconnectTrackingClient(props);
        client.simulateConnectedThenSseDisconnect();

        // 等一小段时间确认没有重连尝试
        boolean reconnected = client.reconnectAttemptLatch.await(300, TimeUnit.MILLISECONDS);
        assertThat(reconnected).isFalse();
        assertThat(client.reconnectAttemptCount.get()).isZero();
    }

    @Test
    void reconnect_shouldNotTrigger_afterShutdown() throws InterruptedException {
        McpClientProperties props = defaultProps();
        props.setReconnectInitialDelay(Duration.ofMillis(200));

        ReconnectTrackingClient client = new ReconnectTrackingClient(props);
        client.simulateConnectedThenSseDisconnect();
        // 立即 shutdown，重连任务应该不执行
        client.shutdown();

        boolean reconnected = client.reconnectAttemptLatch.await(500, TimeUnit.MILLISECONDS);
        assertThat(reconnected).isFalse();
    }

    @Test
    void reconnect_shouldInvokeCallback_whenSucceeded() throws InterruptedException {
        McpClientProperties props = defaultProps();
        props.setReconnectInitialDelay(Duration.ofMillis(50));

        CallbackTrackingClient client = new CallbackTrackingClient(props);
        CountDownLatch callbackLatch = new CountDownLatch(1);
        client.setOnReconnectedCallback(callbackLatch::countDown);
        client.simulateConnectedThenSseDisconnect();

        boolean callbackInvoked = callbackLatch.await(2, TimeUnit.SECONDS);
        assertThat(callbackInvoked).isTrue();
    }

    // -----------------------------------------------------------------------
    // 测试替身
    // -----------------------------------------------------------------------

    /**
     * 可控状态的 McpClient 子类，用于测试断连后行为。
     */
    private static class TestableClient extends McpClient {

        TestableClient(McpClientProperties properties) {
            super(properties);
        }

        /**
         * 模拟：曾经连接成功（initialized=true），但当前已断开（connected=false）。
         */
        void simulateConnectedThenDisconnected() {
            try {
                var initializedField = McpClient.class.getDeclaredField("initialized");
                initializedField.setAccessible(true);
                initializedField.setBoolean(this, true);

                var connectedField = McpClient.class.getDeclaredField("connected");
                connectedField.setAccessible(true);
                connectedField.setBoolean(this, false);
            } catch (Exception e) {
                throw new RuntimeException("反射设置状态失败", e);
            }
        }
    }

    /**
     * 追踪重连尝试的 McpClient 子类。
     */
    private static class ReconnectTrackingClient extends McpClient {

        final AtomicInteger reconnectAttemptCount = new AtomicInteger(0);
        final CountDownLatch reconnectAttemptLatch = new CountDownLatch(1);

        ReconnectTrackingClient(McpClientProperties properties) {
            super(properties);
        }

        /**
         * 模拟：连接成功后 SSE 流断开，触发 finally 块逻辑。
         */
        void simulateConnectedThenSseDisconnect() {
            try {
                var initializedField = McpClient.class.getDeclaredField("initialized");
                initializedField.setAccessible(true);
                initializedField.setBoolean(this, true);

                var connectedField = McpClient.class.getDeclaredField("connected");
                connectedField.setAccessible(true);
                connectedField.setBoolean(this, true);

                // 模拟 SSE 断开：设 connected=false 并触发重连调度
                connectedField.setBoolean(this, false);

                // 直接调用 private scheduleReconnect 方法
                var method = McpClient.class.getDeclaredMethod("scheduleReconnect", int.class);
                method.setAccessible(true);
                method.invoke(this, 1);
            } catch (Exception e) {
                throw new RuntimeException("反射模拟断连失败", e);
            }
        }

        @Override
        public void connectAndInitialize() {
            // 重连时会调用此方法；记录尝试次数
            reconnectAttemptCount.incrementAndGet();
            reconnectAttemptLatch.countDown();
            // 不做真实连接，模拟重连失败（抛异常会触发下一次 scheduleReconnect）
            throw new McpClientException("模拟重连：服务端仍不可用");
        }
    }

    /**
     * 追踪重连回调的 McpClient 子类（重连成功变体）。
     */
    private static class CallbackTrackingClient extends McpClient {

        CallbackTrackingClient(McpClientProperties properties) {
            super(properties);
        }

        void simulateConnectedThenSseDisconnect() {
            try {
                var initializedField = McpClient.class.getDeclaredField("initialized");
                initializedField.setAccessible(true);
                initializedField.setBoolean(this, true);

                var connectedField = McpClient.class.getDeclaredField("connected");
                connectedField.setAccessible(true);
                connectedField.setBoolean(this, false);

                var method = McpClient.class.getDeclaredMethod("scheduleReconnect", int.class);
                method.setAccessible(true);
                method.invoke(this, 1);
            } catch (Exception e) {
                throw new RuntimeException("反射模拟断连失败", e);
            }
        }

        @Override
        public void connectAndInitialize() {
            // 模拟重连成功
            try {
                var initializedField = McpClient.class.getDeclaredField("initialized");
                initializedField.setAccessible(true);
                initializedField.setBoolean(this, true);

                var connectedField = McpClient.class.getDeclaredField("connected");
                connectedField.setAccessible(true);
                connectedField.setBoolean(this, true);
            } catch (Exception e) {
                throw new RuntimeException("反射设置状态失败", e);
            }
        }
    }
}
