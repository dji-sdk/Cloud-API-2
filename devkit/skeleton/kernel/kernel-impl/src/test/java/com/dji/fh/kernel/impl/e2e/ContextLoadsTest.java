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

package com.dji.fh.kernel.impl.e2e;

import com.dji.fh.kernel.impl.auth.AuthorizationAspect;
import com.dji.fh.kernel.impl.event.BridgeEventDispatcher;
import com.dji.fh.kernel.spi.auth.AuthenticationProvider;
import com.dji.fh.kernel.spi.auth.PermissionEvaluator;
import com.dji.fh.kernel.spi.event.SdkEventBus;
import com.dji.fh.kernel.spi.repository.KeyValueStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.task.TaskExecutor;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 完整 Spring context 加载冒烟测试 —— 验证 5 个 AutoConfiguration 全部生效，
 * 所有 Kernel 默认 bean 都装配成功。
 *
 * @author DJI DevKit Team
 */
@SpringBootTest(classes = ContextLoadsTest.MinimalApp.class)
class ContextLoadsTest {

    @Autowired TaskExecutor taskExecutor;
    @Autowired DataSource dataSource;
    @Autowired KeyValueStore kv;
    @Autowired SdkEventBus eventBus;
    @Autowired BridgeEventDispatcher dispatcher;
    @Autowired AuthenticationProvider authProvider;
    @Autowired PermissionEvaluator permissionEvaluator;
    @Autowired AuthorizationAspect aspect;

    @Test
    void allKernelBeans_shouldBePresent_whenContextLoads() {
        assertThat(taskExecutor).isNotNull();
        assertThat(dataSource).isNotNull();
        assertThat(kv).isNotNull();
        assertThat(eventBus).isNotNull();
        assertThat(dispatcher).isNotNull();
        assertThat(authProvider).isNotNull();
        assertThat(permissionEvaluator).isNotNull();
        assertThat(aspect).isNotNull();
    }

    @org.springframework.boot.autoconfigure.SpringBootApplication
    static class MinimalApp {
        @org.springframework.context.annotation.Bean
        com.fasterxml.jackson.databind.ObjectMapper objectMapper() {
            return new com.fasterxml.jackson.databind.ObjectMapper();
        }
    }
}
