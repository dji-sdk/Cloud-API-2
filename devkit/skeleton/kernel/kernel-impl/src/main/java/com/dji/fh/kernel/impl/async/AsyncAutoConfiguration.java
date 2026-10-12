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

package com.dji.fh.kernel.impl.async;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 异步执行子系统的默认装配。
 *
 * <p>提供一个 Spring 标准命名的 {@code applicationTaskExecutor} bean，
 * 供 {@code @Async} 注解与 {@link org.springframework.context.event.EventListener} 异步派发共享。
 *
 * <p>ISV 可通过自家 {@code @Bean(name = "applicationTaskExecutor") TaskExecutor} 完全覆盖；
 * 或通过 {@code fh.skeleton.async.*} 微调默认池参数。
 *
 * <p>装配优先级最高（HIGHEST_PRECEDENCE），保证后续 AutoConfiguration 拿到的是 Kernel 调优过的池。
 *
 * @author DJI DevKit Team
 */
@AutoConfiguration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@EnableAsync
@EnableConfigurationProperties(FhAsyncProperties.class)
public class AsyncAutoConfiguration {

    /**
     * 默认 thread name 前缀，方便日志区分。
     */
    private static final String THREAD_NAME_PREFIX = "fh-event-";

    /**
     * 装配默认 TaskExecutor。已存在同名 bean 则让位。
     *
     * @param props 异步配置属性
     * @return 配置好的 ThreadPoolTaskExecutor
     */
    @Bean(name = "applicationTaskExecutor")
    @ConditionalOnMissingBean(name = "applicationTaskExecutor")
    public TaskExecutor applicationTaskExecutor(FhAsyncProperties props) {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(props.getCoreSize());
        pool.setMaxPoolSize(props.getMaxSize());
        pool.setQueueCapacity(props.getQueueCapacity());
        pool.setKeepAliveSeconds(props.getKeepAliveSeconds());
        pool.setThreadNamePrefix(THREAD_NAME_PREFIX);
        pool.setRejectedExecutionHandler(props.getRejection().toPolicy());
        pool.initialize();
        return pool;
    }
}
