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

package com.dji.flighthub.mcp.tools.log;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FhLogTool Spring Bean 解析回归测试。
 *
 * <p>背景：FhLogTool 有两个构造函数（生产 + 测试专用），Spring 在多构造函数且无
 * {@code @Autowired} 标注时无法决议注入构造函数，会回退查找无参构造函数并抛
 * {@code No default constructor found}。该异常发生在上下文刷新阶段，会连带
 * {@code McpToolAutoRegistrar} 的 {@code List<AbstractMcpTool>} 注入失败，
 * 导致进程启动即退出（stdio 模式表现为子进程异常退出）。
 *
 * <p>此测试用最小 Spring 上下文验证生产构造函数能被正确解析注入，防止回归。
 *
 * @author DJI FlightHub SDK Team
 */
class FhLogToolBeanResolutionTest {

    /**
     * 最小 Spring 上下文：仅注册依赖 bean，不扫描整个包，
     * 避免拉起完整应用上下文（需 FH_SERVER_ADDRESS 等环境变量）。
     */
    @Configuration
    static class MinimalConfig {

        @Bean
        FlightHubProperties flightHubProperties() {
            return new FlightHubProperties();
        }

        @Bean
        SessionManager sessionManager(FlightHubProperties properties) {
            return new SessionManager(properties);
        }

        @Bean
        SdkService sdkService(FlightHubProperties properties) {
            return new SdkService(properties);
        }
    }

    @Test
    @DisplayName("Spring 能通过 @Autowired 构造函数正确实例化 FhLogTool")
    void spring_shouldInstantiateTool_viaAutowiredConstructor() {
        try (AnnotationConfigApplicationContext ctx =
                     new AnnotationConfigApplicationContext()) {
            ctx.register(MinimalConfig.class);
            // 以组件候选身份注册，才会走构造器自动装配（@Bean 方式会绕过本回归点）
            ctx.register(FhLogTool.class);
            ctx.refresh();

            FhLogTool tool = ctx.getBean(FhLogTool.class);
            assertThat(tool).isNotNull();
            assertThat(tool.getToolDefinition())
                    .isNotNull()
                    .containsEntry("name", "fh_log");
        }
    }
}
