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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Kernel 默认实现层的环境后处理器 —— 启动期向 Spring Environment 注入 v0.1 缺省配置。
 *
 * <p>注入位置使用 {@code addLast}（最低优先级），让 ISV 在 {@code application.yml} 或
 * 命令行参数中显式设置的值优先生效。
 *
 * <p>当前注入：
 * <ul>
 *   <li>{@code spring.jpa.hibernate.ddl-auto = none}（D2-2）—— H2 文件模式 URL
 *       不被 Spring Boot 识别为 embedded DB，默认 ddl-auto 是 {@code none}；
 *       ISV 使用 JPA 时需在自家 application.yml 显式配置 {@code update}（本地试用）
 *       或 {@code validate} + Flyway / Liquibase（生产），避免 Schema 变更不可控
 *       （ES320 v3.6 安全基线：生产不得默认自动改表）。</li>
 * </ul>
 *
 * <p>通过 {@code META-INF/spring.factories} 注册激活。
 *
 * @author DJI DevKit Team
 */
public class KernelDefaultsEnvironmentPostProcessor implements EnvironmentPostProcessor {

    /**
     * 缺省属性 source 名称，便于日志 / 调试时识别。
     */
    public static final String PROPERTY_SOURCE_NAME = "fh-skeleton-defaults";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("spring.jpa.hibernate.ddl-auto", "none");
        environment.getPropertySources()
                .addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, defaults));
    }
}
