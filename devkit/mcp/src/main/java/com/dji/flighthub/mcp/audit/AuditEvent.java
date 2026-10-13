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

import java.time.Instant;

/**
 * 审计事件 — 记录每一次 MCP Tool 执行的上下文。
 *
 * @param eventId       唯一事件 ID（aud_ 前缀）
 * @param timestamp     事件时间
 * @param sessionId     Session ID（可为 null，如未创建 session 时的调用）
 * @param clientId      客户端标识（SSE clientId 或 "stdio"）
 * @param requestId     JSON-RPC request id
 * @param toolName      工具名（如 "fh_takeoff"）
 * @param durationMs    执行耗时（毫秒）
 * @param success       是否成功
 * @param errorMessage  失败时的错误信息
 * @param idempotentHit 是否命中幂等缓存
 * @author DJI FlightHub SDK Team
 */
public record AuditEvent(
        String eventId,
        Instant timestamp,
        String sessionId,
        String clientId,
        Object requestId,
        String toolName,
        long durationMs,
        boolean success,
        String errorMessage,
        boolean idempotentHit
) {
}
