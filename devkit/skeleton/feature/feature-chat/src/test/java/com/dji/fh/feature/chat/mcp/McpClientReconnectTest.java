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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * {@link McpClient} 重连熔断与限流处理单元测试。
 *
 * <p>覆盖场景：
 * <ul>
 *   <li>single-flight：并发断连事件只允许拉起一条重连链</li>
 *   <li>链内 attempt 单调递增，失败不重置退避，达到上限后释放标志</li>
 *   <li>HTTP 429 显式上报为 {@link McpRateLimitedException}，不触发重连</li>
 *   <li>{@code Retry-After} 覆盖退避延迟，且不被 {@code reconnectMaxDelay} 截断</li>
 *   <li>抖动只向下收缩，不突破退避上限</li>
 *   <li>请求超时走统一断连入口，同时停止保活</li>
 * </ul>
 *
 * @author DJI DevKit Team
 */
class McpClientReconnectTest {

    /** 测试用回环地址 */
    private static final String LOOPBACK_HOST = "127.0.0.1";

    /** 测试用 message endpoint 路径 */
    private static final String MESSAGE_PATH = "/message";

    /** Retry-After 响应头名 */
    private static final String RETRY_AFTER_HEADER = "Retry-After";

    /** 并发触发断连事件的线程数 */
    private static final int CONCURRENT_TRIGGER_COUNT = 16;

    /** HTTP 限流状态码 */
    private static final int HTTP_TOO_MANY_REQUESTS = 429;

    /** 断言留出的时间容差（毫秒） */
    private static final long TIMING_TOLERANCE_MS = 150L;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** 当前测试持有的 client，统一在 {@link #tearDown()} 中关闭 */
    private McpClient client;

    /** 当前测试启动的本地 HTTP 服务，统一在 {@link #tearDown()} 中停止 */
    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (client != null) {
            client.shutdown();
            client = null;
        }
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    // -----------------------------------------------------------------------
    // single-flight 重连链
    // -----------------------------------------------------------------------

    @Test
    void scheduleReconnect_shouldRunOnlyOneChain_whenTriggeredConcurrently() throws Exception {
        BlockingReconnectClient blockingClient = new BlockingReconnectClient(baseProps());
        client = blockingClient;

        CountDownLatch startGate = new CountDownLatch(1);
        ConcurrentLinkedQueue<Throwable> failures = new ConcurrentLinkedQueue<>();
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < CONCURRENT_TRIGGER_COUNT; i++) {
            Thread thread = new Thread(() -> {
                try {
                    startGate.await();
                    invokePrivate(blockingClient, "scheduleReconnect",
                            new Class<?>[]{int.class}, 1);
                } catch (Throwable t) {
                    failures.add(t);
                }
            }, "trigger-disconnect-" + i);
            thread.start();
            threads.add(thread);
        }

        startGate.countDown();
        for (Thread thread : threads) {
            thread.join(TimeUnit.SECONDS.toMillis(2));
        }

        assertThat(failures).isEmpty();
        // 首次重连尝试必须真的被执行，否则"只有一条链"可能只是没人跑起来
        assertThat(blockingClient.firstAttemptEntered.await(2, TimeUnit.SECONDS)).isTrue();
        // 留出时间窗口，确认其余 15 个触发方没有各自拉起一条链
        Thread.sleep(200L);
        assertThat(blockingClient.attemptCount.get()).isEqualTo(1);
        assertThat(reconnectInFlightOf(blockingClient)).isTrue();

        blockingClient.releaseAttempt.countDown();
    }

    @Test
    void scheduleReconnect_shouldStartNoNewChain_whenChainAlreadyInFlight() throws Exception {
        BlockingReconnectClient blockingClient = new BlockingReconnectClient(baseProps());
        client = blockingClient;
        reconnectInFlightFlagOf(blockingClient).set(true);

        invokePrivate(blockingClient, "scheduleReconnect", new Class<?>[]{int.class}, 1);

        // 已有链在跑，本次调度被忽略；标志保持由原链持有
        assertThat(blockingClient.firstAttemptEntered.await(300, TimeUnit.MILLISECONDS)).isFalse();
        assertThat(blockingClient.attemptCount.get()).isZero();
        assertThat(reconnectInFlightOf(blockingClient)).isTrue();
    }

    @Test
    void scheduleReconnect_shouldStopAtMaxAttempts_whenReconnectKeepsFailing() throws Exception {
        McpClientProperties props = baseProps();
        props.setReconnectMaxAttempts(3);
        props.setReconnectInitialDelay(Duration.ofMillis(10));
        props.setReconnectMaxDelay(Duration.ofMillis(10));

        FailingReconnectClient failingClient = new FailingReconnectClient(props);
        client = failingClient;

        invokePrivate(failingClient, "scheduleReconnect", new Class<?>[]{int.class}, 1);

        assertThat(failingClient.allAttemptsDone.await(3, TimeUnit.SECONDS)).isTrue();
        // 等待最后一次失败后的释放动作
        Thread.sleep(TIMING_TOLERANCE_MS);

        assertThat(failingClient.attemptCount.get()).isEqualTo(3);
        assertThat(reconnectInFlightOf(failingClient)).isFalse();
    }

    @Test
    void scheduleReconnect_shouldReleaseFlag_whenReconnectSucceeds() throws Exception {
        McpClientProperties props = baseProps();
        props.setReconnectInitialDelay(Duration.ofMillis(10));
        props.setReconnectMaxDelay(Duration.ofMillis(10));

        SucceedingReconnectClient succeedingClient = new SucceedingReconnectClient(props);
        client = succeedingClient;

        invokePrivate(succeedingClient, "scheduleReconnect", new Class<?>[]{int.class}, 1);

        assertThat(succeedingClient.attemptLatch.await(2, TimeUnit.SECONDS)).isTrue();
        Thread.sleep(TIMING_TOLERANCE_MS);

        assertThat(succeedingClient.attemptCount.get()).isEqualTo(1);
        assertThat(reconnectInFlightOf(succeedingClient)).isFalse();
        assertThat(succeedingClient.callbackCount.get()).isEqualTo(1);
    }

    // -----------------------------------------------------------------------
    // 退避进度延续 / 重置
    // -----------------------------------------------------------------------

    @Test
    void notifyDisconnected_shouldContinueBackoff_whenConnectionWasShortLived() throws Exception {
        client = new McpClient(slowReconnectProps());
        setField(client, "initialized", true);
        setField(client, "connected", true);
        setField(client, "lastConnectSucceededAt", System.currentTimeMillis() - 1_000L);
        lastReconnectAttemptOf(client).set(3);

        invokePrivate(client, "notifyDisconnected", new Class<?>[]{String.class}, "模拟连上即断");

        // 存活仅 1s（< 30s 稳定阈值），退避进度应继续累加而非回到 1
        assertThat(lastReconnectAttemptOf(client).get()).isEqualTo(4);
    }

    @Test
    void notifyDisconnected_shouldResetBackoff_whenConnectionWasStable() throws Exception {
        client = new McpClient(slowReconnectProps());
        setField(client, "initialized", true);
        setField(client, "connected", true);
        setField(client, "lastConnectSucceededAt", System.currentTimeMillis() - 120_000L);
        lastReconnectAttemptOf(client).set(5);

        invokePrivate(client, "notifyDisconnected", new Class<?>[]{String.class}, "模拟稳定连接后断连");

        // 连接存活超过 30s，视为一次独立故障，退避重置回第 1 次
        assertThat(lastReconnectAttemptOf(client).get()).isEqualTo(1);
    }

    // -----------------------------------------------------------------------
    // HTTP 429 处理
    // -----------------------------------------------------------------------

    @Test
    void postMessage_shouldThrowRateLimited_whenHttp429() throws Exception {
        client = new McpClient(baseProps());
        startServer(HTTP_TOO_MANY_REQUESTS, "5");
        bindMessageEndpoint(client);

        Throwable failure = captureFailure(client, "postMessage",
                new Class<?>[]{JsonNode.class}, MAPPER.createObjectNode());

        assertThat(failure).isInstanceOf(McpRateLimitedException.class);
        long retryAfterMillis = ((McpRateLimitedException) failure).getRetryAfterMillis();
        assertThat(retryAfterMillis).isBetween(4_000L, 5_000L + TIMING_TOLERANCE_MS);
    }

    @Test
    void postMessage_shouldUseDefaultFloor_whenRetryAfterMissing() throws Exception {
        client = new McpClient(baseProps());
        startServer(HTTP_TOO_MANY_REQUESTS, null);
        bindMessageEndpoint(client);

        Throwable failure = captureFailure(client, "postMessage",
                new Class<?>[]{JsonNode.class}, MAPPER.createObjectNode());

        assertThat(failure).isInstanceOf(McpRateLimitedException.class);
        // 无 Retry-After 时使用 60s 兜底
        long retryAfterMillis = ((McpRateLimitedException) failure).getRetryAfterMillis();
        assertThat(retryAfterMillis).isBetween(59_000L, 60_000L + TIMING_TOLERANCE_MS);
    }

    @Test
    void postMessage_shouldUseDefaultFloor_whenRetryAfterInvalid() throws Exception {
        client = new McpClient(baseProps());
        startServer(HTTP_TOO_MANY_REQUESTS, "not-a-number");
        bindMessageEndpoint(client);

        Throwable failure = captureFailure(client, "postMessage",
                new Class<?>[]{JsonNode.class}, MAPPER.createObjectNode());

        assertThat(failure).isInstanceOf(McpRateLimitedException.class);
        long retryAfterMillis = ((McpRateLimitedException) failure).getRetryAfterMillis();
        assertThat(retryAfterMillis).isBetween(59_000L, 60_000L + TIMING_TOLERANCE_MS);
    }

    @Test
    void sendRequest_shouldNotTriggerReconnect_whenRateLimited() throws Exception {
        client = new McpClient(slowReconnectProps());
        setField(client, "initialized", true);
        setField(client, "connected", true);
        startServer(HTTP_TOO_MANY_REQUESTS, "5");
        bindMessageEndpoint(client);

        Throwable failure = captureFailure(client, "sendRequest",
                new Class<?>[]{String.class, JsonNode.class}, "tools/list", MAPPER.createObjectNode());

        assertThat(failure).isInstanceOf(McpRateLimitedException.class);
        // 限流不是断连：连接状态与重连链都不应被改动
        assertThat(client.isConnected()).isTrue();
        assertThat(reconnectInFlightOf(client)).isFalse();
        assertThat(lastReconnectAttemptOf(client).get()).isZero();
    }

    // -----------------------------------------------------------------------
    // 退避延迟计算
    // -----------------------------------------------------------------------

    @Test
    void computeReconnectDelayMs_shouldExceedReconnectMaxDelay_whenRetryAfterRequiresIt() throws Exception {
        client = new McpClient(baseProps());
        // reconnectMaxDelay 为 500ms，服务端要求 5s 后重试
        setField(client, "retryNotBeforeEpochMs", System.currentTimeMillis() + 5_000L);

        long delay = (long) invokePrivate(client, "computeReconnectDelayMs",
                new Class<?>[]{int.class}, 1);

        // 必须服从服务端，不能被 reconnectMaxDelay 截断成 500ms
        assertThat(delay).isBetween(4_000L, 5_000L + TIMING_TOLERANCE_MS);
    }

    @Test
    void computeReconnectDelayMs_shouldClampToRetryAfterMax_whenRetryAfterTooLarge() throws Exception {
        McpClientProperties props = baseProps();
        props.setRetryAfterMax(Duration.ofMillis(1_000));
        client = new McpClient(props);
        setField(client, "retryNotBeforeEpochMs", System.currentTimeMillis() + 60_000L);

        long delay = (long) invokePrivate(client, "computeReconnectDelayMs",
                new Class<?>[]{int.class}, 1);

        assertThat(delay).isBetween(900L, 1_000L + TIMING_TOLERANCE_MS);
    }

    @Test
    void computeReconnectDelayMs_shouldStayWithinJitterBounds_whenJitterEnabled() throws Exception {
        McpClientProperties props = baseProps();
        props.setReconnectInitialDelay(Duration.ofMillis(1_000));
        props.setReconnectMaxDelay(Duration.ofMillis(1_000));
        props.setReconnectJitterRatio(0.5);
        client = new McpClient(props);

        // 抖动只向下收缩：结果必须落在 [base*(1-jitter), base] 内
        for (int i = 0; i < 50; i++) {
            long delay = (long) invokePrivate(client, "computeReconnectDelayMs",
                    new Class<?>[]{int.class}, 1);
            assertThat(delay).isBetween(500L, 1_000L);
        }
    }

    // -----------------------------------------------------------------------
    // callTool 快速失败
    // -----------------------------------------------------------------------

    @Test
    void callTool_shouldFailFast_whenWithinRetryAfterWindow() throws Exception {
        client = new McpClient(slowReconnectProps());
        setField(client, "initialized", true);
        setField(client, "connected", false);
        setField(client, "retryNotBeforeEpochMs", System.currentTimeMillis() + 10_000L);

        assertThatThrownBy(() -> client.callTool("fh_device_list", Map.of()))
                .isInstanceOf(McpRateLimitedException.class)
                .hasMessageContaining("限流");
    }

    @Test
    void callTool_shouldFailFast_whenReconnectChainInFlight() throws Exception {
        client = new McpClient(slowReconnectProps());
        setField(client, "initialized", true);
        setField(client, "connected", false);
        reconnectInFlightFlagOf(client).set(true);

        assertThatThrownBy(() -> client.callTool("fh_device_list", Map.of()))
                .isInstanceOf(McpClientException.class)
                .hasMessageContaining("正在重连中");
    }

    // -----------------------------------------------------------------------
    // 统一断连入口
    // -----------------------------------------------------------------------

    @Test
    void sendRequest_shouldStopKeepalive_whenResponseTimesOut() throws Exception {
        client = new McpClient(keepaliveProps());
        setField(client, "initialized", true);
        setField(client, "connected", true);
        setField(client, "lastHeartbeatReceivedAt", System.currentTimeMillis());
        startServer(200, null);
        bindMessageEndpoint(client);

        invokePrivate(client, "startKeepalive", new Class<?>[0]);
        assertThat(getField(client, "keepaliveTask")).isNotNull();

        Throwable failure = captureFailure(client, "sendRequest",
                new Class<?>[]{String.class, JsonNode.class}, "tools/list", MAPPER.createObjectNode());

        assertThat(failure).isInstanceOf(McpClientException.class)
                .hasMessageContaining("超时");
        // 超时走 notifyDisconnected，必须同时停止保活，避免保活另起一条重连链
        assertThat(getField(client, "keepaliveTask")).isNull();
        assertThat(client.isConnected()).isFalse();
    }

    @Test
    void shutdown_shouldReleaseReconnectInFlightFlag() throws Exception {
        client = new McpClient(baseProps());
        reconnectInFlightFlagOf(client).set(true);

        client.shutdown();

        assertThat(reconnectInFlightOf(client)).isFalse();
    }

    @Test
    void constructor_shouldUseSeparateSchedulerForKeepalive() throws Exception {
        client = new McpClient(baseProps());

        // 重连任务会同步阻塞在 connectAndInitialize；与保活共用单线程会饿死保活检查
        assertThat(getField(client, "keepaliveScheduler"))
                .isNotSameAs(getField(client, "reconnectScheduler"));
        assertThat(getField(client, "keepaliveScheduler")).isNotNull();
    }

    // -----------------------------------------------------------------------
    // 测试数据
    // -----------------------------------------------------------------------

    private static McpClientProperties baseProps() {
        McpClientProperties props = new McpClientProperties();
        props.setUrl("http://localhost:19999");
        props.setConnectTimeout(Duration.ofSeconds(2));
        props.setTimeout(Duration.ofMillis(200));
        props.setReconnectEnabled(true);
        props.setReconnectInitialDelay(Duration.ofMillis(100));
        props.setReconnectMaxDelay(Duration.ofMillis(500));
        props.setReconnectMaxAttempts(3);
        // 关闭抖动，保证退避延迟断言确定
        props.setReconnectJitterRatio(0);
        props.setKeepaliveEnabled(false);
        return props;
    }

    /**
     * 重连延迟拉长的配置：仅用于验证状态流转，避免测试期间真的发起重连。
     *
     * @return 配置对象
     */
    private static McpClientProperties slowReconnectProps() {
        McpClientProperties props = baseProps();
        // 延迟拉到 5s：测试会在调度真正执行前结束，只验证状态流转而不真的发起连接
        props.setReconnectInitialDelay(Duration.ofSeconds(5));
        props.setReconnectMaxDelay(Duration.ofSeconds(5));
        // 上限放宽，避免 attempt 累加断言被 maxAttempts 提前截断
        props.setReconnectMaxAttempts(10);
        return props;
    }

    /**
     * 启用保活的配置：ping 间隔拉到 60s，避免测试期间保活任务自行触发。
     *
     * @return 配置对象
     */
    private static McpClientProperties keepaliveProps() {
        McpClientProperties props = slowReconnectProps();
        props.setKeepaliveEnabled(true);
        props.setKeepaliveInterval(Duration.ofSeconds(60));
        props.setHeartbeatTimeout(Duration.ofSeconds(60));
        return props;
    }

    // -----------------------------------------------------------------------
    // 本地 HTTP 服务（真实 429 / 200 响应）
    // -----------------------------------------------------------------------

    /**
     * 启动一个仅响应 {@link #MESSAGE_PATH} 的本地 HTTP 服务。
     *
     * @param statusCode 响应状态码
     * @param retryAfter Retry-After 头值，null 表示不返回该头
     * @throws IOException 端口绑定失败时抛出
     */
    private void startServer(int statusCode, String retryAfter) throws IOException {
        HttpServer localServer = HttpServer.create(
                new InetSocketAddress(LOOPBACK_HOST, 0), 0);
        localServer.createContext(MESSAGE_PATH, exchange -> {
            exchange.getRequestBody().readAllBytes();
            if (retryAfter != null) {
                exchange.getResponseHeaders().add(RETRY_AFTER_HEADER, retryAfter);
            }
            byte[] body = "{}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(statusCode, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        localServer.start();
        this.server = localServer;
    }

    /**
     * 把 client 的私有 messageEndpoint 指向本地测试服务。
     *
     * @param target 目标 client
     * @throws Exception 反射失败时抛出
     */
    @SuppressWarnings("unchecked")
    private void bindMessageEndpoint(McpClient target) throws Exception {
        String url = "http://" + LOOPBACK_HOST + ":" + server.getAddress().getPort() + MESSAGE_PATH;
        AtomicReference<String> endpoint =
                (AtomicReference<String>) getField(target, "messageEndpoint");
        endpoint.set(url);
    }

    // -----------------------------------------------------------------------
    // 反射辅助方法
    // -----------------------------------------------------------------------

    private static Object invokePrivate(Object target, String methodName, Class<?>[] paramTypes,
                                        Object... args) throws ReflectiveOperationException {
        Method method = McpClient.class.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    /**
     * 反射调用并捕获目标方法抛出的原始异常。
     *
     * @return 目标方法抛出的异常；未抛出时返回 null
     */
    private static Throwable captureFailure(Object target, String methodName,
                                            Class<?>[] paramTypes, Object... args) {
        try {
            invokePrivate(target, methodName, paramTypes, args);
            return null;
        } catch (InvocationTargetException e) {
            return e.getCause();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("反射调用失败: " + methodName, e);
        }
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = McpClient.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static Object getField(Object target, String fieldName) throws Exception {
        Field field = McpClient.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    private static boolean reconnectInFlightOf(Object target) throws Exception {
        return reconnectInFlightFlagOf(target).get();
    }

    private static AtomicBoolean reconnectInFlightFlagOf(Object target) throws Exception {
        return (AtomicBoolean) getField(target, "reconnectInFlight");
    }

    private static AtomicInteger lastReconnectAttemptOf(Object target) throws Exception {
        return (AtomicInteger) getField(target, "lastReconnectAttempt");
    }

    // -----------------------------------------------------------------------
    // 测试替身
    // -----------------------------------------------------------------------

    /**
     * 首次重连后阻塞的 client，用于验证并发断连只拉起一条链。
     */
    private static class BlockingReconnectClient extends McpClient {

        final AtomicInteger attemptCount = new AtomicInteger(0);
        final CountDownLatch firstAttemptEntered = new CountDownLatch(1);
        final CountDownLatch releaseAttempt = new CountDownLatch(1);

        BlockingReconnectClient(McpClientProperties properties) {
            super(properties);
        }

        @Override
        public void connectAndInitialize() {
            attemptCount.incrementAndGet();
            firstAttemptEntered.countDown();
            try {
                releaseAttempt.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 每次重连都失败的 client，用于验证 attempt 累加与上限释放。
     */
    private static class FailingReconnectClient extends McpClient {

        final AtomicInteger attemptCount = new AtomicInteger(0);
        final CountDownLatch allAttemptsDone;

        FailingReconnectClient(McpClientProperties properties) {
            super(properties);
            this.allAttemptsDone = new CountDownLatch(properties.getReconnectMaxAttempts());
        }

        @Override
        public void connectAndInitialize() {
            attemptCount.incrementAndGet();
            allAttemptsDone.countDown();
            throw new McpClientException("模拟重连失败");
        }
    }

    /**
     * 重连成功的 client，用于验证标志释放与回调触发。
     */
    private static class SucceedingReconnectClient extends McpClient {

        final AtomicInteger attemptCount = new AtomicInteger(0);
        final AtomicInteger callbackCount = new AtomicInteger(0);
        final CountDownLatch attemptLatch = new CountDownLatch(1);

        SucceedingReconnectClient(McpClientProperties properties) {
            super(properties);
            setOnReconnectedCallback(callbackCount::incrementAndGet);
        }

        @Override
        public void connectAndInitialize() {
            attemptCount.incrementAndGet();
            attemptLatch.countDown();
        }
    }
}
