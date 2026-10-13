// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import com.dji.flighthubsdk.value.SeiDataType;

import java.util.Objects;
/**
 * SEI 数据项（联合体风格），通过 {@link #getDataType()} 区分实际数据类型。
 *
 * <p>推荐使用静态工厂方法创建：
 * {@link #ofCustomData(SeiData)}、{@link #ofDetectionBox(SeiDetectionBox)}、
 * {@link #ofDetectionPolygon(SeiDetectionPolygon)}、{@link #ofTextOverlay(SeiTextOverlay)}。</p>
 */
public class SeiItem {
    private SeiDataType dataType = SeiDataType.UNKNOWN;
    /** 自定义数据（dataType=CUSTOM_DATA 时有效） */
    private SeiData customData;
    /** 目标检测框（dataType=DETECTION_BOX 时有效） */
    private SeiDetectionBox detectionBox;
    /** 多边形检测区域（dataType=DETECTION_POLYGON 时有效） */
    private SeiDetectionPolygon detectionPolygon;
    /** 文本叠加（dataType=TEXT_OVERLAY 时有效） */
    private SeiTextOverlay textOverlay;
    public SeiItem() {
    }


    /**
     * 构造 SeiItem。
     */
/** 创建自定义数据类型的 SeiItem */
    public static SeiItem ofCustomData(SeiData data) {
        SeiItem item = new SeiItem();
        item.dataType = SeiDataType.CUSTOM_DATA;
        item.customData = data;
        return item;
    }

    /** 创建目标检测框类型的 SeiItem */
    public static SeiItem ofDetectionBox(SeiDetectionBox box) {
        SeiItem item = new SeiItem();
        item.dataType = SeiDataType.DETECTION_BOX;
        item.detectionBox = box;
        return item;
    }

    /** 创建多边形检测区域类型的 SeiItem */
    public static SeiItem ofDetectionPolygon(SeiDetectionPolygon polygon) {
        SeiItem item = new SeiItem();
        item.dataType = SeiDataType.DETECTION_POLYGON;
        item.detectionPolygon = polygon;
        return item;
    }

    /** 创建文本叠加类型的 SeiItem */
    public static SeiItem ofTextOverlay(SeiTextOverlay text) {
        SeiItem item = new SeiItem();
        item.dataType = SeiDataType.TEXT_OVERLAY;
        item.textOverlay = text;
        return item;
    }

    /** 数据类型 */
    public SeiDataType getDataType() { return dataType; }
    /** @param dataType 数据类型 */
    public void setDataType(SeiDataType dataType) { this.dataType = dataType != null ? dataType : SeiDataType.UNKNOWN; }
    /** 自定义数据（dataType=CUSTOM_DATA 时有效） */
    public SeiData getCustomData() { return customData; }
    /** @param customData 自定义数据（dataType=CUSTOM_DATA 时有效） */
    public void setCustomData(SeiData customData) { this.customData = customData; }
    /** 目标检测框（dataType=DETECTION_BOX 时有效） */
    public SeiDetectionBox getDetectionBox() { return detectionBox; }
    /** @param detectionBox 目标检测框（dataType=DETECTION_BOX 时有效） */
    public void setDetectionBox(SeiDetectionBox detectionBox) { this.detectionBox = detectionBox; }
    /** 多边形检测区域（dataType=DETECTION_POLYGON 时有效） */
    public SeiDetectionPolygon getDetectionPolygon() { return detectionPolygon; }
    /** @param detectionPolygon 多边形检测区域（dataType=DETECTION_POLYGON 时有效） */
    public void setDetectionPolygon(SeiDetectionPolygon detectionPolygon) { this.detectionPolygon = detectionPolygon; }
    /** 文本叠加（dataType=TEXT_OVERLAY 时有效） */
    public SeiTextOverlay getTextOverlay() { return textOverlay; }
    /** @param textOverlay 文本叠加（dataType=TEXT_OVERLAY 时有效） */
    public void setTextOverlay(SeiTextOverlay textOverlay) { this.textOverlay = textOverlay; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SeiItem that = (SeiItem) o;
        return Objects.equals(dataType, that.dataType)
                && Objects.equals(customData, that.customData)
                && Objects.equals(detectionBox, that.detectionBox)
                && Objects.equals(detectionPolygon, that.detectionPolygon)
                && Objects.equals(textOverlay, that.textOverlay);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                dataType,
                customData,
                detectionBox,
                detectionPolygon,
                textOverlay);
    }

    @Override
    public String toString() {
        return "SeiItem{"
                + "dataType=" + dataType
                + ", customData=" + customData
                + ", detectionBox=" + detectionBox
                + ", detectionPolygon=" + detectionPolygon
                + ", textOverlay=" + textOverlay
                + '}';
    }
}
