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

/**
 * 请求路径前缀剥离工具 — 在 WebFilter 中将 base-path 前缀从请求路径中剥离，
 * 使白名单匹配（如 {@code /health}）无论 WebFlux 是否在 filter 前剥离 base-path 都能正确工作。
 *
 * <p>WebFlux 的 {@code spring.webflux.base-path} 行为在不同版本/配置下，
 * filter 内 {@code request.getPath().value()} 可能含或不含前缀。本工具提供幂等剥离：
 * 含前缀则剥，不含则原样返回。
 *
 * @author DJI FlightHub SDK Team
 */
public final class McpPathNormalizer {

    private McpPathNormalizer() {
    }

    /**
     * 从路径中剥离 base-path 前缀。
     *
     * @param basePath base-path（如 {@code /pf-devkit/ai-plugin/mcp}），为空或 null 时不剥离
     * @param path     原始请求路径
     * @return 剥离前缀后的路径；未匹配前缀时原样返回
     */
    public static String stripBasePath(String basePath, String path) {
        if (basePath == null || basePath.isEmpty() || path == null) {
            return path;
        }
        // 必须精确以 basePath 开头，且下一段是路径分隔符或路径恰好等于 basePath，避免部分匹配
        if (path.startsWith(basePath)) {
            if (path.length() == basePath.length()) {
                return "/";
            }
            if (path.charAt(basePath.length()) == '/') {
                return path.substring(basePath.length());
            }
        }
        return path;
    }
}
