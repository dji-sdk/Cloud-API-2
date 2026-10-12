// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.List;
import java.util.Objects;

/** 相机可见能力集（DRC {@code drc_camera_state_push.camera_visible_capability}）。 */
public final class CameraVisibleCapability {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex;
    private int cameraMode;
    private List<CameraVisibleCapabilityItem> events;

    public CameraVisibleCapability() {}
    public CameraVisibleCapability(String payloadIndex, int cameraMode, List<CameraVisibleCapabilityItem> events) {
        this.payloadIndex = payloadIndex != null ? payloadIndex : "";
        this.cameraMode = cameraMode;
        this.events = events;
    }
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String v) { this.payloadIndex = v; }
    public int getCameraMode() { return cameraMode; }
    public void setCameraMode(int v) { this.cameraMode = v; }
    public List<CameraVisibleCapabilityItem> getEvents() { return events; }
    public void setEvents(List<CameraVisibleCapabilityItem> v) { this.events = v; }
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CameraVisibleCapability)) return false;
        CameraVisibleCapability that = (CameraVisibleCapability) o;
        return cameraMode == that.cameraMode && Objects.equals(payloadIndex, that.payloadIndex)
                && Objects.equals(events, that.events);
    }
    @Override public int hashCode() { return Objects.hash(payloadIndex, cameraMode, events); }
    @Override public String toString() {
        return "CameraVisibleCapability{payloadIndex='" + payloadIndex + "', cameraMode="
                + cameraMode + ", events=" + events + '}';
    }
}
