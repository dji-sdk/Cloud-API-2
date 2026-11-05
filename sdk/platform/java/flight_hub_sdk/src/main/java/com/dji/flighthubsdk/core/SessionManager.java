// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.generated.ISessionManager;
import com.dji.flighthubsdk.generated.SessionConfig;
import com.dji.flighthubsdk.generated.SessionInfoVector;
import com.dji.flighthubsdk.util.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SessionManager 包装（对外不暴露 {@code generated.ISessionManager}）。
 * <p>用户只 register / getAllSessionInfo；设备走 {@code FlightHubSDK.getXxx(sessionId)}。
 */
public class SessionManager {
    private static final String TAG = "SessionManager";

    /** 默认 Session（initialize 后自动创建，node_id=""） */
    public static final int kDefaultSessionId = 0;

    private final ISessionManager raw;

    SessionManager(ISessionManager raw) {
        this.raw = raw;
    }

    /**
     * 注册子 Session；同 nodeId 幂等。失败返回 -1。
     */
    public int register(SessionConfig config) {
        if (raw == null || config == null) {
            return -1;
        }
        try {
            return raw.registerSession(config);
        } catch (Exception e) {
            Logger.error(TAG, "register error: " + e.getMessage(), e);
            return -1;
        }
    }

    public List<SessionInfo> getAllSessionInfo() {
        if (raw == null) {
            return Collections.emptyList();
        }
        try {
            SessionInfoVector vec = raw.getAllSessionInfo();
            if (vec == null) {
                return Collections.emptyList();
            }
            List<SessionInfo> out = new ArrayList<>((int) vec.size());
            for (int i = 0; i < vec.size(); i++) {
                com.dji.flighthubsdk.generated.SessionInfo g = vec.get(i);
                if (g == null) {
                    continue;
                }
                out.add(new SessionInfo(
                        g.getSession_id(),
                        g.getNode_id(),
                        g.getDisplay_name(),
                        g.getNotify_url()));
            }
            return out;
        } catch (Exception e) {
            Logger.error(TAG, "getAllSessionInfo error: " + e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
