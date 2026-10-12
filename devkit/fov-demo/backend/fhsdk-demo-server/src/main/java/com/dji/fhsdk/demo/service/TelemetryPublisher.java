// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.fhsdk.demo.service;

import com.dji.fhsdk.demo.model.TelemetryMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 将 FHSDK 回调数据封装为 {@link TelemetryMessage}，经 WebSocket 推送到对应 HttpSession。
 * <p>
 * SEI：AR 与 FOV 分包到达后合并为一条 type=sei 消息（任一侧有数据即推送）。
 */
@Component
public class TelemetryPublisher {

    private static final Logger log = LoggerFactory.getLogger(TelemetryPublisher.class);

    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, WebSocketSession> wsByHttpSession = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Map<String, Object>> pendingSeiAr = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Map<String, Object>> pendingSeiFov = new ConcurrentHashMap<>();

    public TelemetryPublisher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void registerWs(String httpSessionId, WebSocketSession ws) {
        wsByHttpSession.put(httpSessionId, ws);
    }

    public void unregisterWs(String httpSessionId) {
        wsByHttpSession.remove(httpSessionId);
        pendingSeiAr.remove(httpSessionId);
        pendingSeiFov.remove(httpSessionId);
    }

    public void publishMqttOsd(String httpSessionId, String gatewaySn, String field, Map<String, Object> data) {
        TelemetryMessage msg = base(httpSessionId, gatewaySn, "mqtt_osd", field, data);
        send(httpSessionId, msg);
    }

    public void publishDrcCamera(String httpSessionId, String gatewaySn, Map<String, Object> data) {
        TelemetryMessage msg = base(httpSessionId, gatewaySn, "drc_camera", null, data);
        send(httpSessionId, msg);
    }

    public void publishPayloadRange(String httpSessionId,
                                    String gatewaySn,
                                    java.util.List<com.dji.fhsdk.demo.model.PayloadVo> payloads,
                                    String selectedPayloadIndex,
                                    boolean configured,
                                    String defaultHint) {
        java.util.HashMap<String, Object> data = new java.util.HashMap<>();
        data.put("payloads", payloads);
        data.put("selectedPayloadIndex", selectedPayloadIndex);
        data.put("configured", configured);
        if (defaultHint != null) {
            data.put("defaultPayloadIndex", defaultHint);
        }
        TelemetryMessage msg = base(httpSessionId, gatewaySn, "payload_range", null, data);
        send(httpSessionId, msg);
    }

    public void publishSeiAr(String httpSessionId, String gatewaySn, Map<String, Object> arData) {
        pendingSeiAr.put(httpSessionId, arData);
        tryFlushSei(httpSessionId, gatewaySn);
    }

    public void publishSeiFov(String httpSessionId, String gatewaySn, Map<String, Object> fovData) {
        pendingSeiFov.put(httpSessionId, fovData);
        tryFlushSei(httpSessionId, gatewaySn);
    }

    private void tryFlushSei(String httpSessionId, String gatewaySn) {
        Map<String, Object> ar = pendingSeiAr.get(httpSessionId);
        Map<String, Object> fov = pendingSeiFov.get(httpSessionId);
        if (ar == null && fov == null) {
            return;
        }
        java.util.HashMap<String, Object> merged = new java.util.HashMap<>();
        if (ar != null) merged.putAll(ar);
        if (fov != null) merged.putAll(fov);
        TelemetryMessage msg = base(httpSessionId, gatewaySn, "sei", null, merged);
        send(httpSessionId, msg);
    }

    private TelemetryMessage base(String httpSessionId, String gatewaySn, String type, String field, Map<String, Object> data) {
        TelemetryMessage msg = new TelemetryMessage();
        msg.setUuid(UUID.randomUUID().toString());
        msg.setType(type);
        msg.setTs(System.currentTimeMillis());
        msg.setGatewaySn(gatewaySn);
        msg.setField(field);
        msg.setData(data);
        return msg;
    }

    private void send(String httpSessionId, TelemetryMessage msg) {
        WebSocketSession ws = wsByHttpSession.get(httpSessionId);
        if (ws == null || !ws.isOpen()) {
            return;
        }
        try {
            synchronized (ws) {
                ws.sendMessage(new TextMessage(objectMapper.writeValueAsString(msg)));
            }
        } catch (Exception e) {
            log.warn("WS send failed session={}: {}", httpSessionId, e.getMessage());
        }
    }
}
