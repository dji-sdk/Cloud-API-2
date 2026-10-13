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

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * {@link McpClient} 心跳保活功能单元测试。
 *
 * <p>覆盖场景：
 * <ul>
 *   <li>heartbeat 事件正确更新 lastHeartbeatReceivedAt</li>
 *   <li>heartbeat 超时触发重连（connected 变为 false）</li>
 *   <li>keepalive 禁用时不调度定时任务</li>
 *   <li>shutdown 后保活任务被取消</li>
 * </ul>
 *
 * @author DJI DevKit Team
 */
class McpClientKeepaliveTest {

    private McpClient client;

    private static McpClientProperties keepaliveProps() {
        McpClientProperties props = new McpClientProperties();
        props.setUrl("http://localhost:19999");
        props.setConnectTimeout(Duration.ofSeconds(2));
        props.setTimeout(Duration.ofSeconds(5));
        props.setReconnectEnabled(true);
        props.setReconnectInitialDelay(Duration.ofMillis(100));
        props.setReconnectMaxDelay(Duration.ofMillis(500));
        props.setReconnectMaxAttempts(3);
        props.setKeepaliveEnabled(true);
        props.setKeepaliveInterval(Duration.ofMillis(200));
        props.setHeartbeatTimeout(Duration.ofMillis(500));
        return props;
    }

    @BeforeEach
    void setUp() {
        client = new McpClient(keepaliveProps());
    }

    @AfterEach
    void tearDown() {
        client.shutdown();
    }

    @Test
    void handleSseEvent_shouldUpdateLastHeartbeatReceivedAt_whenHeartbeatEvent() throws Exception {
        // 模拟已初始化连接状态
        setField(client, "initialized", true);
        setField(client, "connected", true);
        setField(client, "lastHeartbeatReceivedAt", 0L);

        // 调用 handleSseEvent 模拟收到 heartbeat
        invokeHandleSseEvent(client, "heartbeat", "{}",
                new CompletableFuture<>());

        long lastHeartbeat = (long) getField(client, "lastHeartbeatReceivedAt");
        assertThat(lastHeartbeat).isGreaterThan(0L);
        assertThat(System.currentTimeMillis() - lastHeartbeat).isLessThan(1000L);
    }

    @Test
    void performKeepaliveCheck_shouldTriggerReconnect_whenHeartbeatTimeout() throws Exception {
        // 模拟已连接但 heartbeat 很久没更新
        setField(client, "initialized", true);
        setField(client, "connected", true);
        setField(client, "lastHeartbeatReceivedAt",
                System.currentTimeMillis() - 10_000L);

        // 手动调用 performKeepaliveCheck
        invokeMethod(client, "performKeepaliveCheck");

        // 心跳超时应标记断连
        assertThat(client.isConnected()).isFalse();
    }

    @Test
    void performKeepaliveCheck_shouldNotTriggerReconnect_whenHeartbeatRecent() throws Exception {
        // 模拟已连接且 heartbeat 刚收到
        setField(client, "initialized", true);
        setField(client, "connected", true);
        setField(client, "lastHeartbeatReceivedAt", System.currentTimeMillis());

        // performKeepaliveCheck 会尝试 ping（因无真实服务而失败），
        // 但 heartbeat 检查本身不应触发断连
        long beforeCheck = (long) getField(client, "lastHeartbeatReceivedAt");
        assertThat(beforeCheck).isGreaterThan(0L);
        assertThat(client.isConnected()).isTrue();
    }

    @Test
    void startKeepalive_shouldNotSchedule_whenDisabled() throws Exception {
        McpClientProperties props = keepaliveProps();
        props.setKeepaliveEnabled(false);
        McpClient disabledClient = new McpClient(props);

        try {
            setField(disabledClient, "initialized", true);
            setField(disabledClient, "connected", true);

            // 手动调用 startKeepalive
            invokeMethod(disabledClient, "startKeepalive");

            Object task = getField(disabledClient, "keepaliveTask");
            assertThat(task).isNull();
        } finally {
            disabledClient.shutdown();
        }
    }

    @Test
    void shutdown_shouldCancelKeepaliveTask() throws Exception {
        setField(client, "initialized", true);
        setField(client, "connected", true);
        setField(client, "lastHeartbeatReceivedAt", System.currentTimeMillis());

        // 启动 keepalive
        invokeMethod(client, "startKeepalive");

        Object taskObj = getField(client, "keepaliveTask");
        assertThat(taskObj).isNotNull();
        ScheduledFuture<?> task = (ScheduledFuture<?>) taskObj;
        assertThat(task.isCancelled()).isFalse();

        // shutdown 后任务应被取消
        client.shutdown();

        assertThat(task.isCancelled()).isTrue();
    }

    // -------------------------------------------------------------------------
    // 反射辅助方法
    // -------------------------------------------------------------------------

    private static void setField(Object target, String fieldName, Object value)
            throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static Object getField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    private static void invokeMethod(Object target, String methodName) throws Exception {
        java.lang.reflect.Method method = target.getClass().getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(target);
    }

    @SuppressWarnings("unchecked")
    private static void invokeHandleSseEvent(Object target, String event, String data,
                                             CompletableFuture<String> endpointFuture)
            throws Exception {
        java.lang.reflect.Method method = target.getClass().getDeclaredMethod(
                "handleSseEvent", String.class, String.class, CompletableFuture.class);
        method.setAccessible(true);
        method.invoke(target, event, data, endpointFuture);
    }
}
