// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 可见能力事件项。 */
public final class CameraVisibleCapabilityItem {
    private int eventId;
    private int visible;

    public CameraVisibleCapabilityItem() {}
    public CameraVisibleCapabilityItem(int eventId, int visible) {
        this.eventId = eventId;
        this.visible = visible;
    }
    public int getEventId() { return eventId; }
    public void setEventId(int v) { this.eventId = v; }
    public int getVisible() { return visible; }
    public void setVisible(int v) { this.visible = v; }
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CameraVisibleCapabilityItem)) return false;
        CameraVisibleCapabilityItem that = (CameraVisibleCapabilityItem) o;
        return eventId == that.eventId && visible == that.visible;
    }
    @Override public int hashCode() { return Objects.hash(eventId, visible); }
    @Override public String toString() { return "CameraVisibleCapabilityItem{eventId=" + eventId + ", visible=" + visible + '}'; }
}
