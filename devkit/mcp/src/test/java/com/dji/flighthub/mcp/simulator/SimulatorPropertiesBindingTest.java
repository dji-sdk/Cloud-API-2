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

package com.dji.flighthub.mcp.simulator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 验证 {@code sim.*} YAML 配置项能正确绑定到 {@link SimulatorProperties}。
 *
 * <p>覆盖 kebab-case YAML key（如 {@code sim.base-url}）到 camelCase 字段
 * （{@code baseUrl}）的 Spring Boot 松散绑定，防止未来 YAML key 或环境变量名
 * 拼写错误时因 {@code ignoreUnknownFields=true} 静默回退默认值而无测试失败。
 *
 * <p>上下文只加载内部的空 {@link EmptyTestConfig}，再由
 * {@link EnableConfigurationProperties} 单独注册
 * {@link SimulatorProperties} 这一个 bean：若改为直接把
 * {@code SimulatorProperties.class} 传给 {@code @SpringBootTest(classes = ...)}，
 * 该类上的 {@code @Component} 注解会让它被当作普通 bean 再注册一次，
 * 与 {@code @EnableConfigurationProperties} 注册的绑定 bean 产生
 * {@code NoUniqueBeanDefinitionException}。
 *
 * @author DJI FlightHub SDK Team
 */
@SpringBootTest(classes = SimulatorPropertiesBindingTest.EmptyTestConfig.class)
@EnableConfigurationProperties(SimulatorProperties.class)
@TestPropertySource(properties = {
        "sim.base-url=http://sim-verify:1234",
        "sim.username=verify-user",
        "sim.password=verify-pass"
})
class SimulatorPropertiesBindingTest {

    @Autowired
    private SimulatorProperties properties;

    @Test
    void binding_shouldMapKebabCaseYamlKeysToCamelCaseFields() {
        assertEquals("http://sim-verify:1234", properties.getBaseUrl());
        assertEquals("verify-user", properties.getUsername());
        assertEquals("verify-pass", properties.getPassword());
    }

    /**
     * 空的测试专用配置类，仅用于给 {@code @SpringBootTest} 提供最小上下文根，
     * 避免加载完整应用上下文或触发 {@link SimulatorProperties} 的重复 bean 注册。
     */
    @Configuration
    static class EmptyTestConfig {
    }
}
