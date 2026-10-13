// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.List;
import java.util.Objects;

/** 相机能力集（DRC {@code drc_camera_state_push.camera_capability}）。 */
public final class CameraCapability {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex;
    /** 变焦倍数范围 */
    private List<Integer> zoomFactorRange;
    /** 红外倍数范围 */
    private List<Integer> irFactorRange;
    private CameraPhotoStorageFormat basePhotoStorageFormat;

    public CameraCapability() {}
    public CameraCapability(String payloadIndex, List<Integer> zoomFactorRange,
                            List<Integer> irFactorRange, CameraPhotoStorageFormat basePhotoStorageFormat) {
        this.payloadIndex = payloadIndex != null ? payloadIndex : "";
        this.zoomFactorRange = zoomFactorRange;
        this.irFactorRange = irFactorRange;
        this.basePhotoStorageFormat = basePhotoStorageFormat;
    }
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String v) { this.payloadIndex = v; }
    /** 变焦倍数范围 */
    public List<Integer> getZoomFactorRange() { return zoomFactorRange; }
    /** @param zoomFactorRange 变焦倍数范围 */
    public void setZoomFactorRange(List<Integer> v) { this.zoomFactorRange = v; }
    /** 红外倍数范围 */
    public List<Integer> getIrFactorRange() { return irFactorRange; }
    /** @param irFactorRange 红外倍数范围 */
    public void setIrFactorRange(List<Integer> v) { this.irFactorRange = v; }
    public CameraPhotoStorageFormat getBasePhotoStorageFormat() { return basePhotoStorageFormat; }
    public void setBasePhotoStorageFormat(CameraPhotoStorageFormat v) { this.basePhotoStorageFormat = v; }
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CameraCapability)) return false;
        CameraCapability that = (CameraCapability) o;
        return Objects.equals(payloadIndex, that.payloadIndex)
                && Objects.equals(zoomFactorRange, that.zoomFactorRange)
                && Objects.equals(irFactorRange, that.irFactorRange)
                && Objects.equals(basePhotoStorageFormat, that.basePhotoStorageFormat);
    }
    @Override public int hashCode() { return Objects.hash(payloadIndex, zoomFactorRange, irFactorRange, basePhotoStorageFormat); }
    @Override public String toString() {
        return "CameraCapability{payloadIndex='" + payloadIndex + "', zoomFactorRange=" + zoomFactorRange
                + ", irFactorRange=" + irFactorRange + ", basePhotoStorageFormat=" + basePhotoStorageFormat + '}';
    }
}
