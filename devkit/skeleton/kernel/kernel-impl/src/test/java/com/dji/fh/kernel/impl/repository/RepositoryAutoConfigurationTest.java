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

package com.dji.fh.kernel.impl.repository;

import com.dji.fh.kernel.spi.repository.KeyValueStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RepositoryAutoConfiguration 装配测试。
 *
 * <p>注：plan 原文用 {@code AutoConfigurations.of(JacksonAutoConfiguration.class, ...)}
 * 提供 ObjectMapper，但 Spring Boot 3.2 中该 AutoConfiguration 的 ObjectMapper bean
 * 受 {@code @ConditionalOnClass} / {@code Jackson2ObjectMapperBuilder} 链路约束，
 * 在 {@link ApplicationContextRunner} 环境下不会被注册。改用 {@code withBean(ObjectMapper.class)}
 * 直接装配 bare-default 实例 —— 本测试不做序列化，bare-default 已足够。
 *
 * @author DJI DevKit Team
 */
class RepositoryAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withBean(ObjectMapper.class)
            .withConfiguration(AutoConfigurations.of(RepositoryAutoConfiguration.class));

    @Test
    void dataSource_shouldExist_whenAutoConfigured() {
        runner.run(ctx -> assertThat(ctx).hasSingleBean(DataSource.class));
    }

    @Test
    void keyValueStore_shouldBeInMemory_whenAutoConfigured() {
        runner.run(ctx -> {
            assertThat(ctx).hasSingleBean(KeyValueStore.class);
            assertThat(ctx.getBean(KeyValueStore.class))
                    .isInstanceOf(InMemoryKeyValueStore.class);
        });
    }

    @Test
    void keyValueStore_shouldLetUserOverride_whenUserBeanPresent() {
        runner.withBean(KeyValueStore.class, () -> new InMemoryKeyValueStore(new ObjectMapper()) {
            // 匿名子类的 getClass() 不等于父类 InMemoryKeyValueStore（虽然 instanceof 仍为 true）
        }).run(ctx -> {
            KeyValueStore kv = ctx.getBean(KeyValueStore.class);
            assertThat(kv.getClass()).isNotEqualTo(InMemoryKeyValueStore.class);
        });
    }
}
