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

package com.dji.fh.feature.chat.tool;

import static org.assertj.core.api.Assertions.assertThat;

import com.dji.fh.feature.chat.controller.ChatSseEvent;
import com.dji.fh.kernel.spi.auth.SecurityContext;
import com.dji.fh.kernel.spi.auth.SecurityContextHolder;
import io.micrometer.context.ContextRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 验证 requestId ThreadLocal 通过 Reactor Context Propagation
 * 跨 boundedElastic 线程边界正确传播。
 *
 * <p>此测试模拟真实运行时的线程模型：
 * streamExecutor 线程设置 requestId → Reactor boundedElastic 线程执行 tool 回调。
 * 若传播机制未激活，emit/offer 会因 CURRENT_REQUEST_ID.get() == null 而静默丢弃。
 *
 * @author DJI DevKit Team
 */
class ContextPropagationTest {

    private static final String REQUEST_ID = "propagation-test-001";

    @BeforeAll
    static void enablePropagation() {
        ContextRegistry.getInstance().registerThreadLocalAccessor(new RequestIdThreadLocalAccessor());
        ContextRegistry.getInstance().registerThreadLocalAccessor(new SecurityContextThreadLocalAccessor());
        Hooks.enableAutomaticContextPropagation();
    }

    @AfterAll
    static void cleanup() {
        ToolCallContext.clear(REQUEST_ID);
        SecurityContextHolder.clear();
    }

    @BeforeEach
    void setUp() {
        ToolCallContext.clear(REQUEST_ID);
        NavigationHolder.clear(REQUEST_ID);
        SecurityContextHolder.clear();
    }

    @Test
    void emit_shouldWork_acrossBoundedElasticBoundary() throws InterruptedException {
        List<ChatSseEvent> received = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        ToolCallContext.set(REQUEST_ID, received::add);

        Mono.fromRunnable(() -> {
                    assertThat(Thread.currentThread().getName()).contains("boundedElastic");
                    assertThat(ToolCallContext.currentRequestId()).isEqualTo(REQUEST_ID);
                    ToolCallContext.emit(ChatSseEvent.status("跨线程事件"));
                    latch.countDown();
                })
                .subscribeOn(Schedulers.boundedElastic())
                .block();

        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(received).hasSize(1);
        assertThat(received.get(0).getType()).isEqualTo(ChatSseEvent.TYPE_STATUS);
    }

    @Test
    void navigationOffer_shouldWork_acrossBoundedElasticBoundary() throws InterruptedException {
        List<ChatSseEvent> received = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        ToolCallContext.set(REQUEST_ID, received::add);

        Mono.fromRunnable(() -> {
                    assertThat(ToolCallContext.currentRequestId()).isEqualTo(REQUEST_ID);
                    NavigationHolder.offer("/cockpit", Map.of("drone_sn", "DRONE-X"));
                    latch.countDown();
                })
                .subscribeOn(Schedulers.boundedElastic())
                .block();

        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        NavigationHolder.Payload nav = NavigationHolder.take(REQUEST_ID);
        assertThat(nav).isNotNull();
        assertThat(nav.getPath()).isEqualTo("/cockpit");
        assertThat(nav.getQuery()).containsEntry("drone_sn", "DRONE-X");
    }

    @Test
    void concurrentRequests_shouldIsolate_acrossBoundedElastic() throws InterruptedException {
        String reqA = "cross-thread-A";
        String reqB = "cross-thread-B";
        List<ChatSseEvent> receivedA = new ArrayList<>();
        List<ChatSseEvent> receivedB = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(2);

        Thread threadA = new Thread(() -> {
            ToolCallContext.set(reqA, receivedA::add);
            Mono.fromRunnable(() -> {
                        ToolCallContext.emit(ChatSseEvent.status("A"));
                        NavigationHolder.offer("/cockpit", Map.of("drone_sn", "A"));
                    })
                    .subscribeOn(Schedulers.boundedElastic())
                    .block();
            ToolCallContext.clear(reqA);
            latch.countDown();
        });

        Thread threadB = new Thread(() -> {
            ToolCallContext.set(reqB, receivedB::add);
            Mono.fromRunnable(() -> {
                        ToolCallContext.emit(ChatSseEvent.status("B"));
                        NavigationHolder.offer("/cockpit", Map.of("drone_sn", "B"));
                    })
                    .subscribeOn(Schedulers.boundedElastic())
                    .block();
            ToolCallContext.clear(reqB);
            latch.countDown();
        });

        threadA.start();
        threadB.start();
        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();

        assertThat(receivedA).hasSize(1);
        assertThat(receivedB).hasSize(1);

        NavigationHolder.Payload navA = NavigationHolder.take(reqA);
        NavigationHolder.Payload navB = NavigationHolder.take(reqB);
        assertThat(navA).isNotNull();
        assertThat(navA.getQuery()).containsEntry("drone_sn", "A");
        assertThat(navB).isNotNull();
        assertThat(navB.getQuery()).containsEntry("drone_sn", "B");
    }

    /**
     * 验证 SecurityContext 通过 Reactor Context Propagation 跨 boundedElastic 传播。
     *
     * <p>模拟 ChatController 流式请求：servlet 线程设置 SecurityContext，
     * 然后 Reactor boundedElastic 线程上的 tool 回调应能读取到同一身份。
     * 这确保 SecurityGatedCallback L1 门控在 tool 回调中可正确鉴权。
     */
    @Test
    void securityContext_shouldPropagate_acrossBoundedElasticBoundary() {
        SecurityContext ctx = new SecurityContext() {
            @Override
            public String principalId() {
                return "ctx-prop-user";
            }

            @Override
            public Set<String> roles() {
                return Set.of("pilot", "admin");
            }

            @Override
            public Map<String, Object> attributes() {
                return Map.of();
            }
        };

        SecurityContextHolder.set(ctx);
        ToolCallContext.set(REQUEST_ID, e -> { });

        try {
            SecurityContext[] captured = new SecurityContext[1];

            Mono.fromRunnable(() -> {
                        assertThat(Thread.currentThread().getName()).contains("boundedElastic");
                        captured[0] = SecurityContextHolder.current();
                    })
                    .subscribeOn(Schedulers.boundedElastic())
                    .block();

            assertThat(captured[0]).isNotNull();
            assertThat(captured[0].isAuthenticated()).isTrue();
            assertThat(captured[0].principalId()).isEqualTo("ctx-prop-user");
            assertThat(captured[0].hasRole("PILOT")).isTrue(); // equalsIgnoreCase
            assertThat(captured[0].hasRole("admin")).isTrue();
        } finally {
            SecurityContextHolder.clear();
            ToolCallContext.clear(REQUEST_ID);
        }
    }
}
