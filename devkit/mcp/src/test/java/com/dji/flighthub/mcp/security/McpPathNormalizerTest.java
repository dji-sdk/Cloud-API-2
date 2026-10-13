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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link McpPathNormalizer} 单元测试。
 *
 * @author DJI FlightHub SDK Team
 */
class McpPathNormalizerTest {

    /** 约定的 MCP base-path 前缀。 */
    private static final String BASE_PATH = "/pf-devkit/ai-plugin/mcp";

    @Test
    void normalize_shouldStripPrefix_whenPathStartsWithBasePath() {
        assertEquals("/health", McpPathNormalizer.stripBasePath(BASE_PATH, "/pf-devkit/ai-plugin/mcp/health"));
        assertEquals("/sse", McpPathNormalizer.stripBasePath(BASE_PATH, "/pf-devkit/ai-plugin/mcp/sse"));
        assertEquals("/message/abc", McpPathNormalizer.stripBasePath(BASE_PATH, "/pf-devkit/ai-plugin/mcp/message/abc"));
    }

    @Test
    void normalize_shouldReturnAsIs_whenPathDoesNotStartWithBasePath() {
        // WebFlux 若已在 filter 前剥离 base-path，filter 看到的就是相对路径
        assertEquals("/health", McpPathNormalizer.stripBasePath(BASE_PATH, "/health"));
        assertEquals("/sse", McpPathNormalizer.stripBasePath(BASE_PATH, "/sse"));
    }

    @Test
    void normalize_shouldReturnAsIs_whenBasePathEmpty() {
        // stdio / 未配置 base-path 场景
        assertEquals("/health", McpPathNormalizer.stripBasePath("", "/health"));
        assertEquals("/health", McpPathNormalizer.stripBasePath(null, "/health"));
    }

    @Test
    void normalize_shouldNotStripPartialMatch() {
        // /pf-devkit/ai-plugin/mcp-foo 不是 /pf-devkit/ai-plugin/mcp 的前缀匹配（避免误剥）
        assertEquals("/pf-devkit/ai-plugin/mcp-foo/health",
                McpPathNormalizer.stripBasePath(BASE_PATH, "/pf-devkit/ai-plugin/mcp-foo/health"));
    }
}
