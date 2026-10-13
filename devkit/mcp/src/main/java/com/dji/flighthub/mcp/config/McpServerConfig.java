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

package com.dji.flighthub.mcp.config;

import com.dji.flighthub.mcp.prompts.FlightHubPrompts;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MCP Server 配置 — 声明基础 bean，tool 注册由 McpToolAutoRegistrar 自动完成。
 * 传输层（stdio/SSE）通过 registry 提供 MCP 协议请求服务。
 */
@Configuration
public class McpServerConfig {

    private static final Logger log = LoggerFactory.getLogger(McpServerConfig.class);

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public McpToolRegistry mcpToolRegistry() {
        return new McpToolRegistry();
    }

    @Bean
    public McpPromptRegistry mcpPromptRegistry(FlightHubPrompts flightHubPrompts) {
        McpPromptRegistry registry = new McpPromptRegistry();
        registry.registerAll(flightHubPrompts.getDefinitions());
        log.info("Registered {} MCP prompts: {}", registry.getPromptCount(),
                registry.getPromptList().stream().map(p -> p.get("name")).toList());
        return registry;
    }
}
