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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 进程内 stdio JSON-RPC 客户端：把请求交给 {@link StdioTransport#processLine(String)}，
 * 并从注入的捕获流按行读取响应。
 *
 * <p>与反射调 {@code handleRequest} 不同，本类走完整的「解析 → 分发 → 写回」链路，
 * 因此可以钉住错误码、通知语义与信封形状。必须与 {@link StdioTransport} 同包，
 * 才能访问包可见构造器与 {@code processLine}。</p>
 *
 * @author DJI DevKit Team
 */
public final class StdioJsonRpcClient implements AutoCloseable {

    /** 默认单次读取超时；tools/call 在 stdio 下走工作线程，必须靠超时兜底 */
    private static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(5);

    /** 换行符，即 stdio 的帧分隔符 */
    private static final char LINE_SEPARATOR = '\n';

    /** 回车符，兼容 CRLF 帧 */
    private static final char CARRIAGE_RETURN = '\r';

    /** JSON-RPC 请求 id 字段名 */
    private static final String FIELD_ID = "id";

    /** 单次请求最多可丢弃的陈旧帧数；超出说明通道已经失序，应立即失败以便定位 */
    private static final int MAX_DISCARDED_FRAMES = 8;

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() { };

    private final StdioTransport transport;

    private final LineCaptureStream captureStream;

    private final ObjectMapper objectMapper;

    /** close 是否已执行，保证重复关闭不抛异常 */
    private boolean closed;

    /**
     * 构造客户端。
     *
     * @param toolRegistry   Tool 注册表
     * @param promptRegistry Prompt 注册表
     * @param objectMapper   Jackson 实例
     * @param properties     FlightHub 配置
     */
    public StdioJsonRpcClient(McpToolRegistry toolRegistry, McpPromptRegistry promptRegistry,
                              ObjectMapper objectMapper, FlightHubProperties properties) {
        this.objectMapper = objectMapper;
        this.captureStream = new LineCaptureStream();
        PrintStream out = new PrintStream(captureStream, true, StandardCharsets.UTF_8);
        this.transport = new StdioTransport(toolRegistry, promptRegistry, objectMapper, properties, out);
    }

    /**
     * 发送原始文本（可以是非法 JSON），用于解析错误用例。
     *
     * @param raw 原始一行文本
     */
    public void sendRaw(String raw) {
        transport.processLine(raw);
    }

    /**
     * 发送请求并使用默认超时读取一条响应。
     *
     * @param request JSON-RPC 请求对象
     * @return 解析后的响应
     */
    public Map<String, Object> send(Map<String, Object> request) {
        return send(request, DEFAULT_READ_TIMEOUT);
    }

    /**
     * 发送请求并读取与该请求 id 匹配的响应。
     *
     * <p>为什么需要丢弃陈旧帧：tools/call 在 transport 的工作线程上异步写回，而调用方通常使用较短的
     * 读取超时。若某个 tool 的执行时间超过该超时，本次 {@code send} 会超时返回，真正的响应随后才
     * 落到捕获流里。若下一次 {@code send} 直接取队首帧，就会读到上一条请求的响应，导致 id 不匹配、
     * 信封断言失败，并在批量扫描中把后续所有用例误报为失败。因此在写入前先清空队列，写入后按 id
     * 逐帧比对，丢弃不匹配的帧。</p>
     *
     * @param request JSON-RPC 请求对象
     * @param timeout 读取超时（从写入开始计算的总预算）
     * @return 解析后的响应
     */
    public Map<String, Object> send(Map<String, Object> request, Duration timeout) {
        // 写入前队列里的帧必然属于更早的请求，全部视为陈旧帧
        captureStream.drain();
        sendRaw(write(request));
        long deadlineNanos = System.nanoTime() + timeout.toNanos();

        Object expectedId = request.get(FIELD_ID);
        if (expectedId == null) {
            // 无 id 的请求（通知/畸形请求）无从匹配，保持原有语义：接受第一帧
            return readRemaining(deadlineNanos, timeout);
        }

        List<Object> discardedIds = new ArrayList<>();
        while (true) {
            Map<String, Object> response = readRemaining(deadlineNanos, timeout);
            Object actualId = response.get(FIELD_ID);
            if (expectedId.equals(actualId)) {
                return response;
            }
            discardedIds.add(actualId);
            if (discardedIds.size() > MAX_DISCARDED_FRAMES) {
                throw new AssertionError("丢弃的陈旧响应帧过多（>" + MAX_DISCARDED_FRAMES
                        + " 帧），期望 id=" + expectedId + "，实际收到 id 列表: " + discardedIds);
            }
        }
    }

    /**
     * 在剩余时间内读取一条响应，超时消息统一口径。
     *
     * @param deadlineNanos 截止时间（{@link System#nanoTime()} 口径）
     * @param timeout       调用方声明的总超时，用于失败信息
     * @return 解析后的响应
     */
    private Map<String, Object> readRemaining(long deadlineNanos, Duration timeout) {
        long remainingNanos = deadlineNanos - System.nanoTime();
        if (remainingNanos <= 0) {
            throw new AssertionError("等待响应超时（" + timeout.toMillis() + "ms）");
        }
        return read(Duration.ofNanos(remainingNanos));
    }

    /**
     * 读取一条响应。
     *
     * @param timeout 读取超时
     * @return 解析后的响应
     */
    public Map<String, Object> read(Duration timeout) {
        String line = captureStream.poll(timeout);
        if (line == null) {
            throw new AssertionError("等待响应超时（" + timeout.toMillis() + "ms）");
        }
        try {
            return objectMapper.readValue(line, MAP_TYPE);
        } catch (Exception e) {
            throw new AssertionError("响应不是合法 JSON: " + line, e);
        }
    }

    /**
     * 断言在静默窗口内没有任何出站流量。
     *
     * <p>注意：本方法只证明「给定窗口内」没有响应，不是永久性不变量——超时窗口之后的迟到响应
     * 不会被这里捕获，需要时序验证时应改用更长的窗口或显式状态断言。</p>
     *
     * @param quietPeriod 静默窗口
     * @param scenario    场景描述，用于失败信息
     */
    public void assertNoResponse(Duration quietPeriod, String scenario) {
        String line = captureStream.poll(quietPeriod);
        if (line != null) {
            throw new AssertionError(scenario + "：无 id 的消息不应产生响应，实际收到: " + line);
        }
    }

    /**
     * 把消息序列化成一行 JSON 文本。
     *
     * @param message 消息对象
     * @return 单行 JSON
     */
    public String write(Map<String, Object> message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            throw new IllegalArgumentException("无法序列化请求", e);
        }
    }

    /**
     * 关闭客户端：停掉 transport 的线程池并清空尚未读取的出站帧。
     *
     * <p>每个客户端都会构造自己的 {@link StdioTransport}，其 tool 线程池与超时调度器只在
     * {@code run()} 的 finally 中关闭，而测试从不调用 {@code run()}。若不显式关闭，线程池会
     * 随测试用例数量累积泄漏。本方法幂等：重复调用不会抛异常。</p>
     */
    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;
        transport.shutdown();
        captureStream.drain();
    }

    /**
     * 按换行切帧的捕获流。
     */
    private static final class LineCaptureStream extends OutputStream {

        private final BlockingQueue<String> lines = new LinkedBlockingQueue<>();

        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        @Override
        public synchronized void write(int b) {
            if (b == LINE_SEPARATOR) {
                String line = buffer.toString(StandardCharsets.UTF_8);
                buffer.reset();
                if (!line.isEmpty() && line.charAt(line.length() - 1) == CARRIAGE_RETURN) {
                    line = line.substring(0, line.length() - 1);
                }
                lines.add(line);
                return;
            }
            buffer.write(b);
        }

        @Override
        public synchronized void write(byte[] bytes, int offset, int length) {
            for (int index = offset; index < offset + length; index++) {
                write(bytes[index]);
            }
        }

        /**
         * 阻塞读取一行。不能加 {@code synchronized}：tools/call 在 StdioTransport 工作线程上写回，
         * 持锁阻塞会让写线程永远拿不到锁。
         *
         * @param timeout 读取超时
         * @return 一行文本；超时返回 null
         */
        String poll(Duration timeout) {
            try {
                return lines.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("读取响应被中断", e);
            }
        }

        synchronized void drain() {
            lines.clear();
            buffer.reset();
        }
    }
}
