// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 双击 AIM 参数 */
public class CameraAimParam {
    private String cameraType = "";
    private boolean locked;
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    private double x;
    private double y;

    public String getCameraType() { return cameraType; }
    public void setCameraType(String cameraType) { this.cameraType = cameraType != null ? cameraType : ""; }
    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CameraAimParam that = (CameraAimParam) o;
        return locked == that.locked && Double.compare(that.x, x) == 0 && Double.compare(that.y, y) == 0
                && Objects.equals(cameraType, that.cameraType) && Objects.equals(payloadIndex, that.payloadIndex);
    }

    @Override
    public int hashCode() { return Objects.hash(cameraType, locked, payloadIndex, x, y); }
}
