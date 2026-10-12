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

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AsyncAutoConfiguration 装配测试。
 *
 * @author DJI DevKit Team
 */
class AsyncAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AsyncAutoConfiguration.class));

    @Test
    void applicationTaskExecutor_shouldExist_whenAutoConfigured() {
        runner.run(ctx -> {
            assertThat(ctx).hasBean("applicationTaskExecutor");
            assertThat(ctx.getBean("applicationTaskExecutor"))
                    .isInstanceOf(ThreadPoolTaskExecutor.class);
        });
    }

    @Test
    void applicationTaskExecutor_shouldUseDefaults_whenNoOverride() {
        runner.run(ctx -> {
            ThreadPoolTaskExecutor pool = ctx.getBean(
                    "applicationTaskExecutor", ThreadPoolTaskExecutor.class);
            assertThat(pool.getCorePoolSize()).isEqualTo(4);
            assertThat(pool.getMaxPoolSize()).isEqualTo(16);
            assertThat(pool.getQueueCapacity()).isEqualTo(200);
            assertThat(pool.getKeepAliveSeconds()).isEqualTo(60);
            assertThat(pool.getThreadNamePrefix()).isEqualTo("fh-event-");
        });
    }

    @Test
    void applicationTaskExecutor_shouldApplyProperties_whenOverridden() {
        runner.withPropertyValues(
                "fh.skeleton.async.core-size=8",
                "fh.skeleton.async.max-size=32",
                "fh.skeleton.async.queue-capacity=500",
                "fh.skeleton.async.keep-alive-seconds=120",
                "fh.skeleton.async.rejection=abort"
        ).run(ctx -> {
            ThreadPoolTaskExecutor pool = ctx.getBean(
                    "applicationTaskExecutor", ThreadPoolTaskExecutor.class);
            assertThat(pool.getCorePoolSize()).isEqualTo(8);
            assertThat(pool.getMaxPoolSize()).isEqualTo(32);
            assertThat(pool.getQueueCapacity()).isEqualTo(500);
            assertThat(pool.getKeepAliveSeconds()).isEqualTo(120);
            assertThat(pool.getThreadPoolExecutor().getRejectedExecutionHandler())
                    .isInstanceOf(ThreadPoolExecutor.AbortPolicy.class);
        });
    }

    @Test
    void applicationTaskExecutor_shouldLetUserOverride_whenUserBeanPresent() {
        runner.withBean("applicationTaskExecutor", TaskExecutor.class,
                () -> command -> command.run()  // 同步执行的玩具 executor
        ).run(ctx -> {
            TaskExecutor executor = ctx.getBean(
                    "applicationTaskExecutor", TaskExecutor.class);
            assertThat(executor).isNotInstanceOf(ThreadPoolTaskExecutor.class);
        });
    }
}
