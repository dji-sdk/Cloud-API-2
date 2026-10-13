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
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import javax.sql.DataSource;

/**
 * Repository 子系统的默认装配。
 *
 * <p>提供：
 * <ul>
 *   <li>{@link DataSource} — H2 嵌入式文件模式，路径 {@code ./data/fh-skeleton}</li>
 *   <li>{@link KeyValueStore} — {@link InMemoryKeyValueStore} 默认实例</li>
 * </ul>
 *
 * <p>不提供 {@code Repository<T,ID>} bean —— 该接口带泛型参数，ISV 应继承
 * {@link JpaRepositoryAdapter} 在自家 module 中声明实例（详见基类 Javadoc）。
 *
 * <p>所有默认 bean 都 {@code @ConditionalOnMissingBean}，ISV 可单独覆盖任一。
 *
 * <p><b>Schema 管理</b>：H2 文件模式 URL（{@code jdbc:h2:file:...}）默认不被 Spring Boot
 * 识别为 embedded DB，而 {@link com.dji.fh.kernel.impl.KernelDefaultsEnvironmentPostProcessor}
 * 注入的缺省为 {@code spring.jpa.hibernate.ddl-auto=none}（安全基线：生产不得默认自动改表）。
 * ISV 在使用 JPA 时，本地试用可显式设 {@code update}；生产应设
 * {@code spring.jpa.hibernate.ddl-auto=validate} + Flyway / Liquibase 管理 schema。
 *
 * @author DJI DevKit Team
 */
@AutoConfiguration(after = DataSourceAutoConfiguration.class)
public class RepositoryAutoConfiguration {

    /**
     * 默认 H2 文件路径。容器化部署需 volume mount（Plan A4 处理）。
     */
    private static final String DEFAULT_DB_PATH = "./data/fh-skeleton";

    /**
     * 装配默认 DataSource。已存在则让位。
     *
     * @return H2 嵌入式 DataSource
     */
    @Bean
    @ConditionalOnMissingBean
    public DataSource fhDataSource() {
        return H2DataSourceFactory.embeddedFileBased(DEFAULT_DB_PATH);
    }

    /**
     * 装配默认 KeyValueStore。已存在则让位。
     *
     * @param objectMapper Jackson ObjectMapper（Spring Boot 自带）
     * @return InMemoryKeyValueStore 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public KeyValueStore keyValueStore(ObjectMapper objectMapper) {
        return new InMemoryKeyValueStore(objectMapper);
    }
}
