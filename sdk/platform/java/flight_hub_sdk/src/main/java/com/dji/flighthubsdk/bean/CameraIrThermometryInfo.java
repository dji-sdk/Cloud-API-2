// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/**
 * 红外测温上行推送（DRC {@code drc_camera_osd_info_push.ir_thermometry} 全量透传）。
 * <p>与 {@code setIrMeteringMode} / {@code setIrMeteringPoint} / {@code setIrMeteringArea} 成对。</p>
 */
public final class CameraIrThermometryInfo {

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex;
    /** 测温模式（点/区域/关） */
    private int irMeteringMode;
    /** 点测温结果 */
    private CameraIrMeteringPoint irMeteringPoint;
    /** 区域测温结果（含区域内最高/最低温点） */
    private CameraIrMeteringArea irMeteringArea;

    public CameraIrThermometryInfo() {}

    public CameraIrThermometryInfo(String payloadIndex,
                                   int irMeteringMode,
                                   CameraIrMeteringPoint irMeteringPoint,
                                   CameraIrMeteringArea irMeteringArea) {
        this.payloadIndex = payloadIndex != null ? payloadIndex : "";
        this.irMeteringMode = irMeteringMode;
        this.irMeteringPoint = irMeteringPoint;
        this.irMeteringArea = irMeteringArea;
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String v) { this.payloadIndex = v; }

    /** 测温模式（点/区域/关） */
    public int getIrMeteringMode() { return irMeteringMode; }
    /** @param irMeteringMode 测温模式（点/区域/关） */
    public void setIrMeteringMode(int v) { this.irMeteringMode = v; }

    /** 点测温结果 */
    public CameraIrMeteringPoint getIrMeteringPoint() { return irMeteringPoint; }
    /** @param irMeteringPoint 点测温结果 */
    public void setIrMeteringPoint(CameraIrMeteringPoint v) { this.irMeteringPoint = v; }

    /** 区域测温结果（含区域内最高/最低温点） */
    public CameraIrMeteringArea getIrMeteringArea() { return irMeteringArea; }
    /** @param irMeteringArea 区域测温结果（含区域内最高/最低温点） */
    public void setIrMeteringArea(CameraIrMeteringArea v) { this.irMeteringArea = v; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CameraIrThermometryInfo that = (CameraIrThermometryInfo) o;
        return irMeteringMode == that.irMeteringMode
                && Objects.equals(payloadIndex, that.payloadIndex)
                && Objects.equals(irMeteringPoint, that.irMeteringPoint)
                && Objects.equals(irMeteringArea, that.irMeteringArea);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, irMeteringMode, irMeteringPoint, irMeteringArea);
    }

    @Override
    public String toString() {
        return "CameraIrThermometryInfo{"
                + "payloadIndex='" + payloadIndex + '\''
                + ", irMeteringMode=" + irMeteringMode
                + ", irMeteringPoint=" + irMeteringPoint
                + ", irMeteringArea=" + irMeteringArea
                + '}';
    }
}
