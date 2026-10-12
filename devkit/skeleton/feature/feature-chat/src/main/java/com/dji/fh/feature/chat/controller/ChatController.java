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

package com.dji.fh.feature.chat.controller;

import com.dji.fh.feature.chat.ChatProperties;
import com.dji.fh.feature.chat.guard.ChatGuardChain;
import com.dji.fh.feature.chat.guard.GuardResult;
import com.dji.fh.feature.chat.guard.ModerationResult;
import com.dji.fh.feature.chat.tool.NavigationHolder;
import com.dji.fh.feature.chat.tool.ToolCallContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jakarta.annotation.PreDestroy;
import reactor.core.publisher.Flux;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.dji.fh.kernel.spi.auth.RequireAuthenticated;
import com.dji.fh.kernel.spi.auth.SecurityContext;
import com.dji.fh.kernel.spi.auth.SecurityContextHolder;

/**
 * Chat Bot SSE 流式对话接口。
 *
 * <p>端点：{@code POST /api/v1/chat/stream} — 以 SSE 方式流式推送 AI 回复。
 *
 * <p>SSE 事件流程：
 * <ol>
 *   <li>接收请求，构建 Message 历史</li>
 *   <li>调用 ChatClient 流式 API</li>
 *   <li>将每个 token 片段以 {@code token} 事件推送给前端</li>
 *   <li>非 L0 安全等级的 Tool 由 {@link com.dji.fh.feature.chat.tool.SecurityGatedCallback}
 *       返回结构化错误字符串，LLM 将其转化为自然语言回复</li>
 *   <li>流结束后推送 {@code done} 事件并关闭 SSE 连接</li>
 * </ol>
 *
 * <p>线程安全设计：
 * <ul>
 *   <li>每个请求生成唯一 requestId，通过 Reactor Context Propagation 自动传播</li>
 *   <li>{@link ToolCallContext} 和 {@link NavigationHolder} 按 requestId 隔离</li>
 *   <li>SseEmitter.send() 通过 synchronized 保护，防止 Reactor 线程与 streamExecutor 线程并发写入</li>
 * </ul>
 *
 * @author DJI DevKit Team
 */
@RestController
@RequestMapping("/api/v1/chat")
@RequireAuthenticated
public class ChatController {

    private static final Logger LOG = LoggerFactory.getLogger(ChatController.class);

    /** SSE 连接超时时间：5 分钟。 */
    private static final long SSE_TIMEOUT_MS = 5 * 60 * 1000L;

    /** 历史消息角色：用户。 */
    private static final String ROLE_USER = "user";

    /** 历史消息角色：助手。 */
    private static final String ROLE_ASSISTANT = "assistant";

    /** 错误码：流处理异常。 */
    private static final String ERROR_STREAM_FAILED = "stream_failed";

    /** 错误码：参数校验失败。 */
    private static final String ERROR_INVALID_REQUEST = "invalid_request";

    /**
     * 设备操作关键词 — 出现任意一个即判断为 L1 意图，路由到主模型（sonnet + 全工具）。
     * 保守选取，仅包含明确表示"对设备执行操作"的动词短语，避免误判纯查询请求。
     */
    private static final Set<String> ACTION_KEYWORDS = Set.of(
            // 中文操作指令
            "起飞", "飞往", "飞到", "返航",
            "执行航线", "开始航线", "暂停航线", "取消航线",
            "开启直播", "关闭直播", "开始录制", "停止录制",
            "拍照", "拍摄视频",
            "开舱", "关舱",
            "删除媒体", "上传航线",
            "环绕飞行",
            // 英文操作指令
            "takeoff", "take off", "fly to", "return home",
            "execute wayline", "start wayline", "pause wayline", "cancel wayline",
            "start livestream", "stop livestream", "start recording", "stop recording",
            "take photo", "open dock", "close dock",
            "delete media", "upload wayline", "orbit"
    );

    /**
     * 否定/疑问前缀 — 包含时即使含操作关键词也路由到轻量模型（识别为查询或反问意图）。
     */
    private static final Set<String> NEGATION_MARKERS = Set.of(
            "不要", "不能", "不可以", "不应该", "别", "为什么", "为何", "怎么",
            "why", "how to", "should i"
    );

    /** SSE 流式请求异步执行线程池（Java 17 无虚拟线程，使用缓存线程池替代）。 */
    private final ExecutorService streamExecutor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "chat-stream");
        t.setDaemon(true);
        return t;
    });

    /** Spring AI Chat 主客户端（sonnet，全工具集）。 */
    private final ChatClient chatClient;

    /** 轻量 Chat 客户端（haiku，只读工具集），用于 L0 查询类意图。 */
    private final ChatClient lightChatClient;

    /** Chat 配置属性。 */
    private final ChatProperties properties;

    /** Jackson ObjectMapper，用于序列化 SSE 事件数据。 */
    private final ObjectMapper objectMapper;

    /** 安全防护链，可能为 null（guard 功能未启用时）。 */
    @Nullable
    private final ChatGuardChain guardChain;

    /**
     * 构造函数。
     *
     * @param chatClient      Chat 主客户端，不能为 null
     * @param lightChatClient 轻量 Chat 客户端，不能为 null
     * @param properties      Chat 配置属性，不能为 null
     * @param objectMapper    Jackson ObjectMapper，不能为 null
     * @param guardChain      安全防护链，可为 null（功能未启用时）
     */
    public ChatController(ChatClient chatClient,
                          ChatClient lightChatClient,
                          ChatProperties properties,
                          ObjectMapper objectMapper,
                          @Nullable ChatGuardChain guardChain) {
        this.chatClient = chatClient;
        this.lightChatClient = lightChatClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.guardChain = guardChain;
    }

    /**
     * 容器销毁时优雅关闭线程池，确保资源释放。
     */
    @PreDestroy
    public void destroy() {
        streamExecutor.shutdown();
    }

    /**
     * 主对话接口 — 接收用户消息，以 SSE 流式返回 AI 回复。
     *
     * <p>使用独立线程池处理每个请求，避免阻塞 servlet 线程池。
     * 当 guard 启用且配额超限时，直接返回 HTTP 429 响应而非 SSE。
     *
     * @param request 包含用户消息和会话上下文的请求 DTO
     * @return SSE 事件流（正常）或 HTTP 429 响应（配额超限）
     */
    @PostMapping(value = "/stream")
    public Object stream(@RequestBody ChatRequest request) {
        SecurityContext securityCtx = SecurityContextHolder.current();
        String principalId = securityCtx.principalId();

        if (guardChain != null) {
            GuardResult quotaResult = guardChain.quotaPreCheck(principalId);
            if (!quotaResult.allowed()) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Map.of(
                                "error_code", quotaResult.errorCode(),
                                "error_message", quotaResult.errorMessage()));
            }
        }

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        streamExecutor.execute(() -> handleStreamRequest(securityCtx, request, emitter));
        return emitter;
    }

    /**
     * 核心流处理逻辑，运行于独立线程池中。
     *
     * @param securityCtx 安全上下文（在 servlet 线程捕获后传入，不可从 ThreadLocal 读取）
     * @param request     用户请求 DTO
     * @param emitter     SSE 发射器
     */
    private void handleStreamRequest(SecurityContext securityCtx, ChatRequest request, SseEmitter emitter) {
        String principalId = securityCtx.principalId();
        Object emitterLock = new Object();
        SecurityContextHolder.set(securityCtx);
        try {
            validateRequest(request);

            if (guardChain != null) {
                GuardResult preResult = guardChain.preCheck(principalId, request);
                if (!preResult.allowed()) {
                    sendErrorAndComplete(emitter, emitterLock, preResult.errorCode(), preResult.errorMessage());
                    return;
                }
            }

            List<Message> messages = buildMessageHistory(request);
            executeStreamWithEmitter(principalId, request.getMessage(), messages, emitter, emitterLock);
        } catch (IllegalArgumentException e) {
            LOG.warn("ChatController：请求参数校验失败 conversationId={}, reason={}",
                    request.getConversationId(), e.getMessage());
            sendErrorAndComplete(emitter, emitterLock, ERROR_INVALID_REQUEST, e.getMessage());
        } catch (Exception e) {
            LOG.error("ChatController：流处理异常 conversationId={}", request.getConversationId(), e);
            sendErrorAndComplete(emitter, emitterLock, ERROR_STREAM_FAILED, "Chat 服务暂时不可用，请稍后重试");
        } finally {
            SecurityContextHolder.clear();
        }
    }

    /**
     * 校验请求基本参数。
     *
     * @param request 用户请求 DTO
     * @throws IllegalArgumentException 当 message 为空时
     */
    private void validateRequest(ChatRequest request) {
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            throw new IllegalArgumentException("message 不能为空");
        }
    }

    /**
     * 根据请求中的历史消息列表构建 Spring AI Message 列表。
     *
     * <p>按 {@code maxHistoryTurns} 配置截断历史，防止超出模型上下文限制。
     * 历史消息中的 role=user 映射为 UserMessage，role=assistant 映射为 AssistantMessage。
     *
     * @param request 用户请求 DTO
     * @return Spring AI Message 列表（不含本轮用户消息）
     */
    private List<Message> buildMessageHistory(ChatRequest request) {
        List<Message> messages = new ArrayList<>();
        List<ChatRequest.HistoryMessage> history = request.getHistory();
        if (history == null || history.isEmpty()) {
            return messages;
        }

        int maxTurns = properties.getMaxHistoryTurns();
        int maxMessages = maxTurns * 2;
        int startIndex = Math.max(0, history.size() - maxMessages);

        for (int i = startIndex; i < history.size(); i++) {
            ChatRequest.HistoryMessage historyMessage = history.get(i);
            if (ROLE_USER.equalsIgnoreCase(historyMessage.getRole())) {
                messages.add(new UserMessage(historyMessage.getContent()));
            } else if (ROLE_ASSISTANT.equalsIgnoreCase(historyMessage.getRole())) {
                messages.add(new AssistantMessage(historyMessage.getContent()));
            }
        }
        return messages;
    }

    /**
     * 调用 ChatClient 流式 API，逐 token 推送到 SSE。
     *
     * <p>每个请求生成唯一 requestId，通过 {@link ToolCallContext#set(String, java.util.function.Consumer)}
     * 注册回调，Reactor Context Propagation 自动将 requestId 传播到 tool 回调线程。
     *
     * <p>SseEmitter.send() 通过 synchronized 锁保护，防止 Reactor doOnNext 线程
     * 与 tool callback 线程并发写入同一 emitter。</p>
     *
     * @param principalId 用户标识
     * @param userMessage 本轮用户消息文本
     * @param history     历史消息列表
     * @param emitter     SSE 发射器
     * @param emitterLock 发射器同步锁
     */
    private void executeStreamWithEmitter(String principalId,
                                          String userMessage,
                                          List<Message> history,
                                          SseEmitter emitter,
                                          Object emitterLock) {
        String requestId = UUID.randomUUID().toString();

        boolean streamAcquired = false;
        if (guardChain != null) {
            GuardResult streamResult = guardChain.onStreamStart(principalId);
            if (!streamResult.allowed()) {
                sendErrorAndComplete(emitter, emitterLock,
                        streamResult.errorCode(), streamResult.errorMessage());
                return;
            }
            streamAcquired = true;
        }

        StringBuilder fullResponse = new StringBuilder();
        try {
            ToolCallContext.set(requestId, event -> sendEvent(emitter, emitterLock, event));
            ChatClient client = selectClient(userMessage);

            Flux<String> tokenStream = client.prompt()
                    .messages(history)
                    .user(userMessage)
                    .stream()
                    .content();

            tokenStream
                    .doOnNext(token -> {
                        sendEvent(emitter, emitterLock, ChatSseEvent.token(token));
                        fullResponse.append(token);
                    })
                    .blockLast();

            if (guardChain != null) {
                ModerationResult postResult = guardChain.postCheck(fullResponse.toString());
                if (postResult != null) {
                    sendEvent(emitter, emitterLock, ChatSseEvent.warning(postResult.reason()));
                }
            }

            try {
                emitNavigationIfPresent(requestId, emitter, emitterLock);
            } catch (Exception e) {
                LOG.warn("ChatController：发送导航事件失败，忽略", e);
            }
            sendEvent(emitter, emitterLock, ChatSseEvent.done(0, 0));
            completeEmitter(emitter, emitterLock);
        } catch (Exception e) {
            NavigationHolder.clear(requestId);
            LOG.error("ChatController：LLM 流式调用异常", e);
            sendErrorAndComplete(emitter, emitterLock, ERROR_STREAM_FAILED, "LLM 响应异常");
        } finally {
            ToolCallContext.clear(requestId);
            if (guardChain != null && streamAcquired) {
                long estimatedTokens = estimateTokenCount(fullResponse.length());
                guardChain.onStreamEnd(principalId, estimatedTokens);
            }
        }
    }

    /**
     * 粗略估算 token 数（基于字符数）。
     *
     * <p>中文约 1 字 = 1.5 token，英文约 4 字符 = 1 token，取保守中间值：约 2 字符/token。
     *
     * @param charCount 字符数
     * @return 估算的 token 数
     */
    private static long estimateTokenCount(int charCount) {
        return Math.max(1, charCount / 2);
    }

    /**
     * 根据用户消息选择合适的 ChatClient。
     *
     * <p>路由策略：
     * <ol>
     *   <li>先检测是否包含操作关键词（设备操作意图）</li>
     *   <li>若不含操作关键词，使用轻量模型</li>
     *   <li>若含操作关键词，再检查否定/疑问标记是否直接修饰该关键词（如"为什么不能起飞"）</li>
     *   <li>否定标记+操作关键词共存但否定非直接修饰时（如"帮我起飞，不要太高"），仍用主模型</li>
     * </ol>
     *
     * @param message 用户消息文本
     * @return 选中的 ChatClient
     */
    ChatClient selectClient(String message) {
        String matchedKeyword = null;
        int keywordIndex = -1;
        for (String keyword : ACTION_KEYWORDS) {
            int idx = message.indexOf(keyword);
            if (idx >= 0) {
                matchedKeyword = keyword;
                keywordIndex = idx;
                break;
            }
        }

        if (matchedKeyword == null) {
            LOG.debug("ChatController：未检测到操作关键词，使用轻量模型");
            return lightChatClient;
        }

        // 检查否定标记是否直接修饰操作关键词（出现在关键词前方 8 字符以内）
        for (String negation : NEGATION_MARKERS) {
            int negIdx = message.indexOf(negation);
            if (negIdx >= 0 && negIdx < keywordIndex && (keywordIndex - negIdx - negation.length()) <= 6) {
                LOG.debug("ChatController：否定标记 '{}' 直接修饰操作关键词 '{}'，使用轻量模型",
                        negation, matchedKeyword);
                return lightChatClient;
            }
        }

        LOG.debug("ChatController：检测到操作关键词 '{}', 使用主模型", matchedKeyword);
        return chatClient;
    }

    /**
     * 检查 NavigationHolder 是否有待发送的导航信号，有则推送 navigation SSE 事件。
     *
     * @param requestId    请求唯一标识
     * @param emitter      SSE 发射器
     * @param emitterLock  发射器同步锁
     */
    private void emitNavigationIfPresent(String requestId, SseEmitter emitter, Object emitterLock) {
        NavigationHolder.Payload nav = NavigationHolder.take(requestId);
        if (nav != null) {
            sendEvent(emitter, emitterLock, ChatSseEvent.navigation(nav.getPath(), nav.getQuery()));
            LOG.debug("ChatController：推送导航事件 path={} query={}", nav.getPath(), nav.getQuery());
        }
    }

    /**
     * 发送错误事件并关闭 SSE 连接。
     *
     * @param emitter     SSE 发射器
     * @param emitterLock 发射器同步锁
     * @param code        错误码
     * @param message     错误消息
     */
    private void sendErrorAndComplete(SseEmitter emitter, Object emitterLock,
                                      String code, String message) {
        sendEvent(emitter, emitterLock, ChatSseEvent.error(code, message));
        completeEmitter(emitter, emitterLock);
    }

    /**
     * 线程安全地关闭 SSE 连接。
     *
     * @param emitter     SSE 发射器
     * @param emitterLock 发射器同步锁
     */
    private void completeEmitter(SseEmitter emitter, Object emitterLock) {
        synchronized (emitterLock) {
            try {
                emitter.complete();
            } catch (IllegalStateException e) {
                LOG.debug("ChatController：emitter 已关闭，忽略 complete()");
            }
        }
    }

    /**
     * 将 {@link ChatSseEvent} 序列化为 JSON 并线程安全地发送至 SSE emitter。
     *
     * <p>使用 synchronized 保护 emitter.send()，防止 Reactor 线程与 streamExecutor 线程并发写入。
     * 捕获所有可能的异常（包括超时/断开时的 IllegalStateException），避免异常逃逸。
     *
     * @param emitter     SSE 发射器
     * @param emitterLock 发射器同步锁
     * @param event       要发送的事件
     */
    private void sendEvent(SseEmitter emitter, Object emitterLock, ChatSseEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event.getData());
            synchronized (emitterLock) {
                emitter.send(SseEmitter.event()
                        .name(event.getType())
                        .data(json, MediaType.APPLICATION_JSON));
            }
        } catch (JsonProcessingException e) {
            LOG.error("ChatController：SSE 事件序列化失败 type={}", event.getType(), e);
        } catch (IOException e) {
            LOG.debug("ChatController：SSE 发送失败（客户端可能已断开） type={}", event.getType());
        } catch (IllegalStateException e) {
            LOG.debug("ChatController：SSE emitter 已超时或关闭 type={}", event.getType());
        }
    }
}
