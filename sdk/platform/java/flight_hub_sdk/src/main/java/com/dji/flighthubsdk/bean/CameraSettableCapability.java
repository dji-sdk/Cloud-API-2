// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.List;
import java.util.Objects;

/** 相机可设置能力集（DRC {@code drc_camera_state_push.camera_settable_capability}）。 */
public final class CameraSettableCapability {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex;
    private List<CameraSettableEvent> events;

    public CameraSettableCapability() {}
    public CameraSettableCapability(String payloadIndex, List<CameraSettableEvent> events) {
        this.payloadIndex = payloadIndex != null ? payloadIndex : "";
        this.events = events;
    }
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String v) { this.payloadIndex = v; }
    public List<CameraSettableEvent> getEvents() { return events; }
    public void setEvents(List<CameraSettableEvent> v) { this.events = v; }
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CameraSettableCapability)) return false;
        CameraSettableCapability that = (CameraSettableCapability) o;
        return Objects.equals(payloadIndex, that.payloadIndex) && Objects.equals(events, that.events);
    }
    @Override public int hashCode() { return Objects.hash(payloadIndex, events); }
    @Override public String toString() {
        return "CameraSettableCapability{payloadIndex='" + payloadIndex + "', events=" + events + '}';
    }
}
