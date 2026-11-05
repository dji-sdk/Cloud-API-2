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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 子进程 stdio 客户端：用 {@link ProcessBuilder} 启动真实 fat JAR，逐行读写并带超时。
 *
 * <p>stdout 与 stderr 分开泵：stdout 泵在入队前逐行用 Jackson 校验，
 * 首个不可解析的行记录到 {@link #firstMalformedStdoutLine()}（证明日志没污染 stdout）；
 * stderr 泵只累加文本，不做 JSON 校验。</p>
 *
 * <p>校验在<b>泵线程</b>上完成而不是在 {@link #read(Duration)} 里做：{@code read} 只解析它主动取出的行，
 * {@link #closeStdin()} 之后仍留在队列里的畸形行永远不会被 {@code read} 看到。</p>
 *
 * <p>关闭 stdin 后子进程退出时入队的 {@link #EOF} 哨兵在读取循环之外的 {@code finally} 中写入，
 * 因此不经过校验，不会被误判为畸形行。</p>
 *
 * {@link #close()} 无条件 {@code destroyForcibly()}，不依赖进程自然退出。</p>
 *
 * @author DJI DevKit Team
 */
public final class SubprocessStdioClient implements AutoCloseable {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() { };

    /** 读取线程结束哨兵：子进程 stdout 关闭时入队，用来把「进程退出」和「超时」区分开 */
    private static final String EOF = " EOF ";

    private final Process process;

    private final BufferedWriter stdin;

    private final BlockingQueue<String> stdoutLines = new LinkedBlockingQueue<>();

    private final StringBuilder stderr = new StringBuilder();

    private final ObjectMapper objectMapper;

    /** 首个无法解析为 JSON 的 stdout 行；null 表示目前尚未发现（泵线程写入） */
    private final AtomicReference<String> firstNonJsonLine = new AtomicReference<>(null);

    /**
     * 启动子进程。
     *
     * @param jarPath      fat JAR 路径
     * @param objectMapper Jackson 实例
     */
    private SubprocessStdioClient(Path jarPath, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        ProcessBuilder builder = new ProcessBuilder(
                Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-jar", jarPath.toString(),
                "--spring.profiles.active=stdio",
                "--spring.main.web-application-type=none",
                "--spring.main.banner-mode=off");
        builder.environment().put("FH_MOCK", "true");
        builder.environment().put("FH_SERVER_ADDRESS", "http://placeholder");
        try {
            this.process = builder.start();
        } catch (IOException e) {
            throw new IllegalStateException("无法启动 MCP Server 子进程: " + jarPath, e);
        }
        this.stdin = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
        startPump(process.getInputStream(), stdoutLines);
        startPump(process.getErrorStream(), null);
    }

    /**
     * 启动客户端。
     *
     * @param jarPath      fat JAR 路径
     * @param objectMapper Jackson 实例
     * @return 客户端
     */
    public static SubprocessStdioClient start(Path jarPath, ObjectMapper objectMapper) {
        return new SubprocessStdioClient(jarPath, objectMapper);
    }

    /**
     * 发送一行 JSON。
     *
     * @param message 消息
     */
    public void send(Map<String, Object> message) {
        try {
            stdin.write(objectMapper.writeValueAsString(message));
            stdin.write('\n');
            stdin.flush();
        } catch (IOException e) {
            throw new IllegalStateException("写入子进程 stdin 失败", e);
        }
    }

    /**
     * 读取一行原始 stdout（同时做 JSON 合法性断言）。
     *
     * @param timeout 超时
     * @return 已确认可解析为 JSON 对象的响应
     */
    public Map<String, Object> read(Duration timeout) {
        String line = readRawLine(timeout);
        try {
            return objectMapper.readValue(line, MAP_TYPE);
        } catch (Exception e) {
            throw new AssertionError("stdout 出现非 JSON 行（日志污染？）: " + line, e);
        }
    }

    /**
     * 读取一行原始 stdout。
     *
     * @param timeout 超时
     * @return 原始行
     */
    public String readRawLine(Duration timeout) {
        try {
            String line = stdoutLines.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (line == null) {
                throw new AssertionError("等待子进程响应超时（" + timeout.toMillis() + "ms）；stderr=" + stderr());
            }
            if (EOF.equals(line)) {
                throw new AssertionError("子进程已退出；stderr=" + stderr());
            }
            return line;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("读取子进程 stdout 被中断", e);
        }
    }

    /**
     * 断言在窗口内 stdout 没有新输出。
     *
     * <p>子进程在 stdin 关闭后主动退出属于「不再有输出」的一种，故 EOF 哨兵同样视为静默；
     * 队列是 FIFO 的，若之前还有真实输出，它会先于 EOF 被取出。</p>
     *
     * @param quietPeriod 静默窗口
     * @return true 表示没有输出
     */
    public boolean hasNoOutputWithin(Duration quietPeriod) {
        try {
            String line = stdoutLines.poll(quietPeriod.toMillis(), TimeUnit.MILLISECONDS);
            return line == null || EOF.equals(line);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("等待 stdout 静默被中断", e);
        }
    }

    /**
     * 关闭 stdin，模拟子进程收到 EOF。
     *
     * <p>关闭是「尽力而为」：管道 flush-close 失败不应让测试失败，故吞掉 {@link IOException}。</p>
     */
    public void closeStdin() {
        try {
            stdin.close();
        } catch (IOException ignored) {
            // 忽略：closeStdin 只用于模拟 EOF，关闭失败不影响后续读取 stdout
        }
    }

    /**
     * 取泵线程记录的首个无法解析为 JSON 的 stdout 行。
     *
     * <p>只记录第一行，避免日志刷屏；必须在子进程 stdout 泵读完之后调用，
     * 否则可能漏掉仍在管道缓冲区里的行。</p>
     *
     * @return 首个畸形行；stdout 全部合法时返回 null
     */
    public String firstMalformedStdoutLine() {
        return firstNonJsonLine.get();
    }

    /**
     * 取 stderr 内容，用于失败诊断。
     *
     * @return stderr
     */
    public String stderr() {
        synchronized (stderr) {
            return stderr.toString();
        }
    }

    /**
     * 关闭客户端：先关闭 stdin，再 {@code destroyForcibly()} 强杀子进程，最多等待 10 秒退出。
     *
     * <p>清理阶段的失败一律吞掉，避免在 {@code @AfterEach} 中掩盖真正的测试失败。</p>
     */
    @Override
    public void close() {
        try {
            stdin.close();
        } catch (IOException ignored) {
            // 忽略：进程即将被强杀
        }
        process.destroyForcibly();
        try {
            process.waitFor(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 在校验泵分支记录首个无法解析为 JSON 对象的 stdout 行。
     *
     * <p>只记第一行（{@code compareAndSet(null, line)}），并发下也只会有一条被保留；
     * 解析失败不影响该行继续入队，{@link #read(Duration)} 仍会按原逻辑抛出断言。</p>
     *
     * @param line 原始 stdout 行
     */
    private void recordMalformedLine(String line) {
        try {
            objectMapper.readValue(line, MAP_TYPE);
        } catch (Exception e) {
            firstNonJsonLine.compareAndSet(null, line);
        }
    }

    private void startPump(InputStream stream, BlockingQueue<String> target) {
        Thread pump = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (target != null) {
                        recordMalformedLine(line);
                        // 无论是否合法都照常入队：read() 的行为保持不变，校验只是旁路记录
                        target.add(line);
                    } else {
                        synchronized (stderr) {
                            stderr.append(line).append('\n');
                        }
                    }
                }
            } catch (IOException ignored) {
                // 进程退出时流被关闭，属于预期
            } finally {
                if (target != null) {
                    target.add(EOF);
                }
            }
        }, target != null ? "contract-stdout-pump" : "contract-stderr-pump");
        pump.setDaemon(true);
        pump.start();
    }
}
