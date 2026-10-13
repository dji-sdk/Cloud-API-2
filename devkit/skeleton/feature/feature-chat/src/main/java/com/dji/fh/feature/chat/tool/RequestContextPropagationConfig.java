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

import io.micrometer.context.ContextRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Hooks;

/**
 * 激活 Reactor Context Propagation 并注册 requestId / SecurityContext ThreadLocalAccessor。
 *
 * <p>Spring Boot 3.2.5 默认 {@code spring.reactor.context-propagation=LIMITED}，
 * 该模式仅从已填充的 Reactor Context 中恢复 ThreadLocal，不会自动安装
 * {@code Schedulers.onScheduleHook} 来跨线程快照/恢复。
 *
 * <p>本配置显式调用 {@link Hooks#enableAutomaticContextPropagation()} 安装该钩子，
 * 使得 streamExecutor 线程上设置的 {@code CURRENT_REQUEST_ID} ThreadLocal
 * 在 Reactor 调度到 boundedElastic 线程执行 tool 回调时自动恢复。
 *
 * @author DJI DevKit Team
 */
@Configuration
public class RequestContextPropagationConfig {

    private static final Logger LOG = LoggerFactory.getLogger(RequestContextPropagationConfig.class);

    /**
     * 注册 ThreadLocalAccessor 并激活自动上下文传播。
     *
     * <p>{@code enableAutomaticContextPropagation()} 是幂等的，多次调用安全。
     * 它安装 {@code Schedulers.onScheduleHook("CONTEXT_IN_THREAD_LOCALS", ...)}，
     * 在每次 Reactor 线程切换时自动快照/恢复已注册的 ThreadLocal 变量。
     */
    @PostConstruct
    public void registerAccessor() {
        ContextRegistry.getInstance().registerThreadLocalAccessor(new RequestIdThreadLocalAccessor());
        ContextRegistry.getInstance().registerThreadLocalAccessor(new SecurityContextThreadLocalAccessor());
        Hooks.enableAutomaticContextPropagation();
        LOG.info("已激活 Reactor 自动上下文传播，requestId 与 SecurityContext 将跨 boundedElastic 边界传播");
    }
}
