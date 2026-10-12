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

package com.dji.flighthub.mcp.tools.connect;

import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * fh_ops — 运维工具，支持健康检查、日志级别调整和优雅关停。
 * 操作类型：status | set_log_level | shutdown
 */
@Component
public class FhOpsTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhOpsTool.class);

    /** MCP 协议版本（与 SseTransport 保持一致） */
    private static final String MCP_PROTOCOL_VERSION = "2025-06-18";

    private static final long MILLIS_PER_SECOND = 1000L;

    private final SdkService sdkService;

    public FhOpsTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", "fh_ops");
        def.put("description", "Operational commands: health check (status), adjust log level (set_log_level), "
                + "or gracefully shutdown the MCP server (shutdown).");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("action", Map.of(
                "type", "string",
                "enum", List.of("status", "set_log_level", "shutdown"),
                "description", "Operation to perform"
        ));
        props.put("level", Map.of(
                "type", "string",
                "enum", List.of("DEBUG", "INFO", "WARN", "ERROR"),
                "description", "Log level (required when action=set_log_level)"
        ));
        schema.put("properties", props);
        schema.put("required", List.of("action"));
        def.put("inputSchema", schema);
        return def;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> args) {
        String action = (String) args.getOrDefault("action", "status");

        return switch (action) {
            case "status" -> executeStatus();
            case "set_log_level" -> executeSetLogLevel(args);
            case "shutdown" -> executeShutdown();
            default -> errorResult("Unknown action: " + action,
                    "Valid actions: status, set_log_level, shutdown");
        };
    }

    private Map<String, Object> executeStatus() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("server_version", "1.0.0");
        data.put("sdk_version", sdkService.isMockMode() ? "MOCK"
                : sdkService.isInitialized() ? sdkService.getSdkVersion() : "(not connected)");
        data.put("sdk_initialized", sdkService.isInitialized());
        data.put("mcp_protocol_version", MCP_PROTOCOL_VERSION);
        data.put("transport", System.getenv().getOrDefault("FH_TRANSPORT", "stdio"));
        data.put("mock_mode", sdkService.isMockMode());
        data.put("session_active", sessionManager.hasActiveSession());
        data.put("uptime_seconds", getUptimeSeconds());
        data.put("java_version", System.getProperty("java.version"));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("data", data);
        result.put("message", "FlightHub MCP Server is healthy.");
        return result;
    }

    private Map<String, Object> executeSetLogLevel(Map<String, Object> args) {
        String level = (String) args.get("level");
        if (level == null || level.isBlank()) {
            return errorResult("Missing 'level' parameter",
                    "Provide level: DEBUG, INFO, WARN, or ERROR");
        }

        // 动态调整根 logger 级别
        ch.qos.logback.classic.Logger rootLogger =
                (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(ch.qos.logback.classic.Level.valueOf(level.toUpperCase()));

        log.info("Log level changed to: {}", level.toUpperCase());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("data", Map.of("log_level", level.toUpperCase()));
        result.put("message", "Log level set to " + level.toUpperCase());
        return result;
    }

    private Map<String, Object> executeShutdown() {
        log.info("Shutdown requested via fh_ops");

        // 关闭所有 session（释放控制权、断开 DRC、取消订阅）
        sessionManager.closeAllSessions();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("message", "MCP Server shutting down. All sessions closed and resources released.");
        result.put("next_steps", List.of("Call fh_connect to reconnect when needed."));

        // 在响应发送后调度 JVM 关停
        new Thread(() -> {
            try {
                Thread.sleep(500); // 等待响应发送完毕
                System.exit(0);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }).start();

        return result;
    }

    private long getUptimeSeconds() {
        return java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime() / MILLIS_PER_SECOND;
    }
}
