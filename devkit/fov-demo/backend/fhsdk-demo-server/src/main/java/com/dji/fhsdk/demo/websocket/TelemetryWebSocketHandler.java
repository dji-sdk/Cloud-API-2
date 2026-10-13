// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.fhsdk.demo.websocket;

import com.dji.fhsdk.demo.service.TelemetryPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * 遥测 WebSocket（仅服务端推送）。
 * 握手时从 HttpSession 取 sessionId，与 REST 共用 Cookie，否则 OSD 无法下发。
 */
@Component
public class TelemetryWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(TelemetryWebSocketHandler.class);

    private final TelemetryPublisher publisher;

    public TelemetryWebSocketHandler(TelemetryPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String httpSessionId = httpSessionId(session);
        if (httpSessionId != null) {
            publisher.registerWs(httpSessionId, session);
            log.info("WS connected httpSession={}", httpSessionId);
        } else {
            log.warn("WS connected but no HttpSession in handshake — OSD will not push (check Cookie / proxy)");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String httpSessionId = httpSessionId(session);
        if (httpSessionId != null) {
            publisher.unregisterWs(httpSessionId);
            log.info("WS closed httpSession={} status={}", httpSessionId, status);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 仅下行，忽略客户端消息
    }

    private String httpSessionId(WebSocketSession session) {
        Object attr = session.getAttributes().get("HTTP.SESSION.ID");
        if (attr instanceof String s) return s;
        Object httpSession = session.getAttributes().get("HTTP_SESSION");
        if (httpSession instanceof jakarta.servlet.http.HttpSession hs) return hs.getId();
        return null;
    }
}
