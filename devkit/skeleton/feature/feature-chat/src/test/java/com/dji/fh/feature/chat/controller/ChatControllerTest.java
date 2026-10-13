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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dji.fh.feature.chat.ChatProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.AfterEach;
import com.dji.fh.kernel.spi.auth.NoOpSecurityContext;
import com.dji.fh.kernel.spi.auth.SecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

/**
 * {@link ChatController} 单元测试。
 *
 * @author DJI CloudAPI2 Team
 */
class ChatControllerTest {

    private ChatClient chatClient;
    private ChatClient lightChatClient;
    private ChatProperties properties;
    private ObjectMapper objectMapper;
    private ChatController controller;

    @BeforeEach
    void setUp() {
        chatClient = mock(ChatClient.class);
        lightChatClient = mock(ChatClient.class);
        properties = new ChatProperties();
        objectMapper = new ObjectMapper();
        controller = new ChatController(chatClient, lightChatClient, properties, objectMapper, null);
    }

    @AfterEach
    void tearDown() {
        controller.destroy();
    }

    // -------------------------------------------------------------------------
    // 模型路由：selectClient
    // -------------------------------------------------------------------------

    @Test
    void selectClient_shouldReturnLightClient_whenNoActionKeywords() {
        ChatClient selected = controller.selectClient("查询在线设备列表");
        assertThat(selected).isSameAs(lightChatClient);
    }

    @Test
    void selectClient_shouldReturnPrimaryClient_whenTakeoffKeyword() {
        ChatClient selected = controller.selectClient("帮我起飞");
        assertThat(selected).isSameAs(chatClient);
    }

    @Test
    void selectClient_shouldReturnPrimaryClient_whenFlyToKeyword() {
        ChatClient selected = controller.selectClient("飞往东经118度位置");
        assertThat(selected).isSameAs(chatClient);
    }

    @Test
    void selectClient_shouldReturnPrimaryClient_whenReturnHomeKeyword() {
        ChatClient selected = controller.selectClient("执行返航");
        assertThat(selected).isSameAs(chatClient);
    }

    @Test
    void selectClient_shouldReturnLightClient_whenQueryAboutFlight() {
        ChatClient selected = controller.selectClient("查看最近的飞行记录");
        assertThat(selected).isSameAs(lightChatClient);
    }

    @Test
    void selectClient_shouldReturnLightClient_whenNegationPrefixBeforeActionKeyword() {
        ChatClient selected = controller.selectClient("不要起飞");
        assertThat(selected).isSameAs(lightChatClient);
    }

    @Test
    void selectClient_shouldReturnLightClient_whenWhyQuestionContainsActionKeyword() {
        ChatClient selected = controller.selectClient("为什么要返航");
        assertThat(selected).isSameAs(lightChatClient);
    }

    @Test
    void selectClient_shouldReturnPrimaryClient_whenEnglishTakeoffKeyword() {
        ChatClient selected = controller.selectClient("please takeoff now");
        assertThat(selected).isSameAs(chatClient);
    }

    // -------------------------------------------------------------------------
    // SSE 流式请求
    // -------------------------------------------------------------------------

    @Test
    void stream_shouldReturnSseEmitter_whenStreamReturnsContent() throws Exception {
        stubLightClientStream("设备列表：drone-001, drone-002");

        ChatRequest request = new ChatRequest();
        request.setMessage("查询在线设备");
        request.setConversationId("conv-1");

        SseEmitter emitter = (SseEmitter) controller.stream(request);
        assertThat(emitter).isNotNull();
        Thread.sleep(300);
    }

    @Test
    void stream_shouldHandleEmptyStream() throws Exception {
        stubLightClientStream();

        ChatRequest request = new ChatRequest();
        request.setMessage("测试空响应");
        request.setConversationId("conv-2");

        SseEmitter emitter = (SseEmitter) controller.stream(request);
        assertThat(emitter).isNotNull();
        Thread.sleep(200);
    }

    @Test
    void stream_shouldHandleExceptionFromLlm() throws Exception {
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.StreamResponseSpec streamSpec = mock(ChatClient.StreamResponseSpec.class);
        when(lightChatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.messages(any(List.class))).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.stream()).thenReturn(streamSpec);
        when(streamSpec.content()).thenReturn(Flux.error(new RuntimeException("LLM 连接超时")));

        ChatRequest request = new ChatRequest();
        request.setMessage("测试异常");
        request.setConversationId("conv-4");

        SseEmitter emitter = (SseEmitter) controller.stream(request);
        assertThat(emitter).isNotNull();
        Thread.sleep(200);
    }

    @Test
    void stream_shouldHandleHistoryMessages() throws Exception {
        stubLightClientStream("收到历史上下文");

        ChatRequest.HistoryMessage userMsg = new ChatRequest.HistoryMessage();
        userMsg.setRole("user");
        userMsg.setContent("之前的问题");

        ChatRequest.HistoryMessage assistantMsg = new ChatRequest.HistoryMessage();
        assistantMsg.setRole("assistant");
        assistantMsg.setContent("之前的回答");

        ChatRequest request = new ChatRequest();
        request.setMessage("新问题");
        request.setConversationId("conv-5");
        request.setHistory(List.of(userMsg, assistantMsg));

        SseEmitter emitter = (SseEmitter) controller.stream(request);
        assertThat(emitter).isNotNull();
        Thread.sleep(300);
    }

    @Test
    void stream_shouldHandleMultiTokenStream() throws Exception {
        stubLightClientStream("token1", "token2", "token3");

        ChatRequest request = new ChatRequest();
        request.setMessage("多token测试");
        request.setConversationId("conv-6");

        SseEmitter emitter = (SseEmitter) controller.stream(request);
        assertThat(emitter).isNotNull();
        Thread.sleep(300);
    }

    // -------------------------------------------------------------------------
    // 参数校验（BUG-2 回归测试）
    // -------------------------------------------------------------------------

    @Test
    void stream_shouldReturnInvalidRequestError_whenMessageIsNull() throws Exception {
        ChatRequest request = new ChatRequest();
        request.setMessage(null);
        request.setConversationId("conv-validation-1");

        List<String> events = captureStreamEvents(request);

        assertThat(events).anyMatch(json ->
                json.contains("\"code\":\"invalid_request\"") && json.contains("message 不能为空"));
    }

    @Test
    void stream_shouldReturnInvalidRequestError_whenMessageIsBlank() throws Exception {
        ChatRequest request = new ChatRequest();
        request.setMessage("   ");
        request.setConversationId("conv-validation-2");

        List<String> events = captureStreamEvents(request);

        assertThat(events).anyMatch(json ->
                json.contains("\"code\":\"invalid_request\"") && json.contains("message 不能为空"));
    }

    @Test
    void stream_shouldReturnInvalidRequestError_whenMessageIsEmpty() throws Exception {
        ChatRequest request = new ChatRequest();
        request.setMessage("");
        request.setConversationId("conv-validation-3");

        List<String> events = captureStreamEvents(request);

        assertThat(events).anyMatch(json ->
                json.contains("\"code\":\"invalid_request\"") && json.contains("message 不能为空"));
    }

    @Test
    void destroy_shouldShutdownExecutors() {
        controller.destroy();
    }

    // -------------------------------------------------------------------------
    // 工具方法
    // -------------------------------------------------------------------------

    /**
     * 为 lightChatClient 设置流式返回内容的 stub。
     *
     * @param tokens 流式返回的 token 片段，空参表示空流
     */
    private void stubLightClientStream(String... tokens) {
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.StreamResponseSpec streamSpec = mock(ChatClient.StreamResponseSpec.class);
        when(lightChatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.messages(any(List.class))).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.stream()).thenReturn(streamSpec);
        when(streamSpec.content()).thenReturn(
                tokens.length > 0 ? Flux.just(tokens) : Flux.empty()
        );
    }

    /**
     * 通过反射调用 handleStreamRequest，用可观测 ObjectMapper 子类捕获序列化输出。
     * 返回 ObjectMapper.writeValueAsString 的实际 JSON 结果列表。
     */
    private List<String> captureStreamEvents(ChatRequest request) throws Exception {
        List<String> captured = new CopyOnWriteArrayList<>();

        // ObjectMapper 子类拦截 writeValueAsString
        ObjectMapper capturingMapper = new ObjectMapper() {
            @Override
            public String writeValueAsString(Object value) throws JsonProcessingException {
                String json = super.writeValueAsString(value);
                captured.add(json);
                return json;
            }
        };

        ChatController capturingController = new ChatController(
                chatClient, lightChatClient, properties, capturingMapper, null);

        try {
            Method handleMethod = ChatController.class.getDeclaredMethod(
                    "handleStreamRequest", SecurityContext.class, ChatRequest.class, SseEmitter.class);
            handleMethod.setAccessible(true);

            SseEmitter emitter = new SseEmitter(5000L);
            handleMethod.invoke(capturingController, NoOpSecurityContext.INSTANCE, request, emitter);
        } finally {
            capturingController.destroy();
        }
        return captured;
    }
}
