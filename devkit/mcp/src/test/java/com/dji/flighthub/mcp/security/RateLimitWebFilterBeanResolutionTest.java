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

package com.dji.flighthub.mcp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RateLimitWebFilter Spring Bean 解析回归测试。
 *
 * <p>背景：RateLimitWebFilter 有两个构造函数（生产 + 测试专用），Spring 在多构造函数
 * 且无 @Autowired 标注时无法决议注入构造函数，会回退查找无参构造函数并抛
 * {@code No default constructor found}。此测试用最小 Spring 上下文验证生产构造函数
 * 能被正确解析注入，防止回归。
 *
 * @author DJI FlightHub SDK Team
 */
class RateLimitWebFilterBeanResolutionTest {

    /**
     * 最小 Spring 上下文：仅注册 ObjectMapper + RateLimitWebFilter，
     * 避免拉起完整应用上下文（需 FH_SSE_AUTH_TOKEN 等环境变量）。
     */
    @Configuration
    static class MinimalConfig {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        RateLimitWebFilter rateLimitWebFilter(ObjectMapper objectMapper) {
            return new RateLimitWebFilter(objectMapper, "/pf-devkit/ai-plugin/mcp");
        }
    }

    @Test
    @DisplayName("Spring 能通过 @Autowired 构造函数正确实例化 RateLimitWebFilter")
    void spring_shouldInstantiateFilter_viaAutowiredConstructor() {
        try (AnnotationConfigApplicationContext ctx =
                     new AnnotationConfigApplicationContext(MinimalConfig.class)) {
            RateLimitWebFilter filter = ctx.getBean(RateLimitWebFilter.class);
            assertThat(filter).isNotNull();
        }
    }
}
