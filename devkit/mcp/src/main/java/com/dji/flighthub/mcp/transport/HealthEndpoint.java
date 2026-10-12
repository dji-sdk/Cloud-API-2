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

package com.dji.flighthub.mcp.transport;

import com.dji.flighthub.mcp.config.McpToolRegistry;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 健康检查与就绪探针端点。
 * 用于 K8s liveness/readiness 探针、负载均衡器健康检查。
 *
 * <p>端点：
 * <ul>
 *   <li>GET /health — 存活探针（liveness），只要进程活着就返回 200</li>
 *   <li>GET /ready — 就绪探针（readiness），检查核心组件是否就绪</li>
 *   <li>GET /status — 详细状态（运维用），返回版本、连接数、工具数等</li>
 * </ul>
 *
 * <p>注意：这些端点不经过认证过滤器（AuthenticationWebFilter 不拦截），
 * 确保 K8s 探针不需要 token 就能访问。
 *
 * @author DJI FlightHub SDK Team
 */
@RestController
public class HealthEndpoint {

    private static final String SERVER_VERSION = "1.0.0";
    private static final Instant START_TIME = Instant.now();

    private final McpToolRegistry toolRegistry;
    private final SessionManager sessionManager;
    private final SdkService sdkService;

    public HealthEndpoint(McpToolRegistry toolRegistry,
                          SessionManager sessionManager,
                          SdkService sdkService) {
        this.toolRegistry = toolRegistry;
        this.sessionManager = sessionManager;
        this.sdkService = sdkService;
    }

    /**
     * 存活探针 — K8s liveness probe。
     * 只要 JVM 还在运行就返回 200。
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(body);
    }

    /**
     * 就绪探针 — K8s readiness probe。
     * 检查核心组件是否就绪，不就绪则返回 503。
     */
    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> ready() {
        Map<String, Object> body = new LinkedHashMap<>();
        boolean toolsReady = toolRegistry != null && !toolRegistry.getToolDefinitions().isEmpty();
        boolean sessionReady = sessionManager != null;

        boolean allReady = toolsReady && sessionReady;

        body.put("status", allReady ? "READY" : "NOT_READY");
        body.put("checks", Map.of(
                "tools_registered", toolsReady,
                "session_manager", sessionReady,
                "sdk_mode", sdkService.isMockMode() ? "mock" : "real"
        ));
        body.put("timestamp", Instant.now().toString());

        if (allReady) {
            return ResponseEntity.ok(body);
        } else {
            return ResponseEntity.status(503).body(body);
        }
    }

    /**
     * 详细状态 — 运维诊断用。
     * 返回版本、uptime、工具数、活跃 session 等信息。
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("server", Map.of(
                "name", "flighthub-mcp-server",
                "version", SERVER_VERSION,
                "uptime_seconds", getUptimeSeconds(),
                "start_time", START_TIME.toString()
        ));
        body.put("tools", Map.of(
                "total", toolRegistry.getToolDefinitions().size()
        ));
        body.put("session", Map.of(
                "active", sessionManager.hasActiveSession(),
                "sdk_mode", sdkService.isMockMode() ? "mock" : "real"
        ));
        body.put("jvm", Map.of(
                "max_memory_mb", Runtime.getRuntime().maxMemory() / (1024 * 1024),
                "used_memory_mb", (Runtime.getRuntime().totalMemory()
                        - Runtime.getRuntime().freeMemory()) / (1024 * 1024),
                "processors", Runtime.getRuntime().availableProcessors()
        ));
        body.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(body);
    }

    private long getUptimeSeconds() {
        return ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
    }
}
