// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

/**
 * Session 只读信息（对应 C++ SessionInfo POD）。
 */
public class SessionInfo {
    private final int sessionId;
    private final String nodeId;
    private final String displayName;
    private final String notifyUrl;

    public SessionInfo(int sessionId, String nodeId, String displayName, String notifyUrl) {
        this.sessionId = sessionId;
        this.nodeId = nodeId != null ? nodeId : "";
        this.displayName = displayName != null ? displayName : "";
        this.notifyUrl = notifyUrl != null ? notifyUrl : "";
    }

    static SessionInfo fromGenerated(com.dji.flighthubsdk.generated.SessionInfo g) {
        if (g == null) {
            return null;
        }
        return new SessionInfo(
                g.getSession_id(),
                g.getNode_id(),
                g.getDisplay_name(),
                g.getNotify_url());
    }

    public int getSessionId() {
        return sessionId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    @Override
    public String toString() {
        return "{id=" + sessionId
                + " node=\"" + nodeId + "\""
                + " name=\"" + displayName + "\""
                + " url=\"" + notifyUrl + "\"}";
    }
}
