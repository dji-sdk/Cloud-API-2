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

package com.dji.flighthub.mcp.audit;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.context.RequestContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.session.SessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * 审计服务 — 记录所有 MCP Tool 执行的审计日志。
 *
 * <p>输出方式：
 * <ul>
 *   <li>结构化日志（SLF4J，可通过 JSON appender 采集到 ELK/Loki）</li>
 *   <li>可选：异步 HTTP POST 到外部审计端点（非阻塞，失败不影响工具执行）</li>
 * </ul>
 *
 * <p>配置：
 * <ul>
 *   <li>{@code flighthub.audit.enabled} — 是否启用审计（默认 true）</li>
 *   <li>{@code flighthub.audit.endpoint} — 远程审计端点 URL（留空则仅本地日志）</li>
 * </ul>
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    /** 审计专用 logger（独立 appender 配置，便于采集） */
    private static final Logger AUDIT_LOG = LoggerFactory.getLogger("AUDIT");

    private final boolean enabled;
    private final String endpoint;
    private final SessionManager sessionManager;

    public AuditService(FlightHubProperties properties, SessionManager sessionManager) {
        this.enabled = properties.getAudit().isEnabled();
        this.endpoint = properties.getAudit().getEndpoint();
        this.sessionManager = sessionManager;

        if (enabled) {
            log.info("AuditService 启动: endpoint={}", endpoint != null && !endpoint.isBlank() ? endpoint : "(本地日志)");
        } else {
            log.info("AuditService 已禁用");
        }
    }

    /**
     * 记录工具执行审计事件。
     *
     * @param toolName      工具名
     * @param success       是否成功
     * @param errorMessage  错误信息（成功时为 null）
     * @param durationMs    执行耗时
     * @param idempotentHit 是否命中幂等缓存
     */
    public void logExecution(String toolName, boolean success, String errorMessage,
                             long durationMs, boolean idempotentHit) {
        if (!enabled) {
            return;
        }

        RequestContext ctx = RequestContext.current();
        String sessionId = resolveSessionId(ctx);

        AuditEvent event = new AuditEvent(
                generateEventId(),
                Instant.now(),
                sessionId,
                ctx != null ? ctx.getClientId() : "unknown",
                ctx != null ? ctx.getRequestId() : null,
                toolName,
                durationMs,
                success,
                errorMessage,
                idempotentHit
        );

        // 结构化日志输出
        AUDIT_LOG.info("event_id={} tool={} success={} duration_ms={} session={} client={} request_id={} idempotent={}{}",
                event.eventId(),
                event.toolName(),
                event.success(),
                event.durationMs(),
                event.sessionId(),
                event.clientId(),
                event.requestId(),
                event.idempotentHit(),
                errorMessage != null ? " error=\"" + errorMessage + "\"" : "");

        // 可选：远程上报（异步，不阻塞主流程）
        if (endpoint != null && !endpoint.isBlank()) {
            sendToRemoteAsync(event);
        }
    }

    /**
     * 是否启用审计。
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 从 RequestContext 或 SessionManager 解析 session ID。
     */
    private String resolveSessionId(RequestContext ctx) {
        if (ctx != null && ctx.getSessionId() != null) {
            return ctx.getSessionId();
        }
        SessionContext session = sessionManager.getActiveSession();
        if (session != null) {
            // 顺便补充 RequestContext
            if (ctx != null) {
                ctx.enrichWithSession(session);
            }
            return session.getSessionId();
        }
        return null;
    }

    /**
     * 生成唯一审计事件 ID。
     */
    private String generateEventId() {
        return "aud_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    /**
     * 异步发送到远程审计端点（失败仅记录警告，不影响主流程）。
     */
    private void sendToRemoteAsync(AuditEvent event) {
        // TODO: 使用 WebClient 或 RestTemplate 异步 POST
        // 当前版本仅记录日志，后续按需接入
        log.debug("审计事件待远程上报: event_id={}, endpoint={}", event.eventId(), endpoint);
    }
}
