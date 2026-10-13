// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/**
 * 相机镜头 FOV 信息（来自 DRC 上行 OSD 中的 wide/zoom/ir fov_info）。
 * <p>用于计算画面的世界坐标范围、覆盖区域等。</p>
 */
public final class CameraLensFovInfo {

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex;
    /** 图像宽度（像素） */
    private int width;
    /** 图像高度（像素） */
    private int height;
    /** 图像比例 */
    private int imageRatio;
    /** 等效焦距（35mm 等效，单位 mm） */
    private int focalLength;

    public CameraLensFovInfo() {
    }

    public CameraLensFovInfo(String payloadIndex,
                             int width,
                             int height,
                             int imageRatio,
                             int focalLength) {
        this.payloadIndex = payloadIndex != null ? payloadIndex : "";
        this.width = width;
        this.height = height;
        this.imageRatio = imageRatio;
        this.focalLength = focalLength;
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex; }

    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    public int getImageRatio() { return imageRatio; }
    public void setImageRatio(int imageRatio) { this.imageRatio = imageRatio; }

    public int getFocalLength() { return focalLength; }
    public void setFocalLength(int focalLength) { this.focalLength = focalLength; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CameraLensFovInfo that = (CameraLensFovInfo) o;
        return width == that.width
                && height == that.height
                && imageRatio == that.imageRatio
                && focalLength == that.focalLength
                && Objects.equals(payloadIndex, that.payloadIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, width, height, imageRatio, focalLength);
    }

    @Override
    public String toString() {
        return "CameraLensFovInfo{"
                + "payloadIndex='" + payloadIndex + '\''
                + ", width=" + width
                + ", height=" + height
                + ", imageRatio=" + imageRatio
                + ", focalLength=" + focalLength
                + '}';
    }
}
