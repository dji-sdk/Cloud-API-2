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

package com.dji.fh.kernel.impl;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * KernelDefaultsEnvironmentPostProcessor 单测。
 *
 * @author DJI DevKit Team
 */
class KernelDefaultsEnvironmentPostProcessorTest {

    private final KernelDefaultsEnvironmentPostProcessor processor =
            new KernelDefaultsEnvironmentPostProcessor();

    @Test
    void postProcessEnvironment_shouldAddDdlAutoDefault_whenInvoked() {
        ConfigurableEnvironment env = new MockEnvironment();

        processor.postProcessEnvironment(env, new SpringApplication());

        assertThat(env.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("none");
    }

    @Test
    void postProcessEnvironment_shouldLetUserOverride_whenIsvSetsExplicit() {
        MockEnvironment env = new MockEnvironment();
        // 模拟 ISV 在 application.yml 中显式覆盖（更高优先级 source）
        env.setProperty("spring.jpa.hibernate.ddl-auto", "validate");

        processor.postProcessEnvironment(env, new SpringApplication());

        // ISV 显式值优先于 addLast 注入的缺省
        assertThat(env.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
    }

    @Test
    void postProcessEnvironment_shouldRegisterPropertySource_withExpectedName() {
        ConfigurableEnvironment env = new MockEnvironment();

        processor.postProcessEnvironment(env, new SpringApplication());

        PropertySource<?> source = env.getPropertySources()
                .get(KernelDefaultsEnvironmentPostProcessor.PROPERTY_SOURCE_NAME);
        assertThat(source).isNotNull();
        assertThat(source.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("none");
    }
}
