// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 可设置能力事件项。 */
public final class CameraSettableEvent {
    private int eventId;
    private int settable;

    public CameraSettableEvent() {}
    public CameraSettableEvent(int eventId, int settable) {
        this.eventId = eventId;
        this.settable = settable;
    }
    public int getEventId() { return eventId; }
    public void setEventId(int v) { this.eventId = v; }
    public int getSettable() { return settable; }
    public void setSettable(int v) { this.settable = v; }
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CameraSettableEvent)) return false;
        CameraSettableEvent that = (CameraSettableEvent) o;
        return eventId == that.eventId && settable == that.settable;
    }
    @Override public int hashCode() { return Objects.hash(eventId, settable); }
    @Override public String toString() { return "CameraSettableEvent{eventId=" + eventId + ", settable=" + settable + '}'; }
}
