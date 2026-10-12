// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.fhsdk.demo.service;

import com.dji.fhsdk.demo.model.DeviceContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionRegistry {

    private final ConcurrentHashMap<String, Boolean> sessionReady = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, DeviceContext> deviceContexts = new ConcurrentHashMap<>();

    public void markReady(String sessionId) {
        sessionReady.put(sessionId, true);
    }

    public boolean isReady(String sessionId) {
        return Boolean.TRUE.equals(sessionReady.get(sessionId));
    }

    public DeviceContext getDeviceContext(String sessionId) {
        return deviceContexts.get(sessionId);
    }

    public void setDeviceContext(String sessionId, DeviceContext ctx) {
        if (ctx == null) {
            deviceContexts.remove(sessionId);
        } else {
            ctx.setHttpSessionId(sessionId);
            deviceContexts.put(sessionId, ctx);
        }
    }

    public void clearDeviceContext(String sessionId) {
        deviceContexts.remove(sessionId);
    }
}
