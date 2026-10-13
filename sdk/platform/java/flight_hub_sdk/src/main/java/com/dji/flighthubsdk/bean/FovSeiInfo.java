// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/**
 * 直播 FOV SEI 信息（相机 cam_fov 协议物理量，非视场角度数）。
 * <p>fovHorizontal / fovVertical 为有效成像宽高（μm），视场角（度）需按针孔模型自行换算，例如：
 * {@code 2 * atan(fovHorizontal / (2.0 * curActualFocalLength)) * 180 / PI}。
 */
public final class FovSeiInfo {

    /** 有效成像宽度，单位 μm（微米），对应 image_width */
    private long fovHorizontal;
    /** 有效成像高度，单位 μm（微米），对应 image_height */
    private long fovVertical;
    /** 当前镜头实际焦距，整型值 ÷1000 为 mm，对应 lens_focal_length */
    private long curActualFocalLength;
    public FovSeiInfo() {
    }

/**
     * 构造 FovSeiInfo。
     *
     * @param fovHorizontal 有效成像宽度，单位 μm（微米）
     * @param fovVertical 有效成像高度，单位 μm（微米）
     * @param curActualFocalLength 当前镜头实际焦距，整型值 ÷1000 为 mm
     */
    public FovSeiInfo(long fovHorizontal, long fovVertical, long curActualFocalLength) {
        this.fovHorizontal = fovHorizontal;
        this.fovVertical = fovVertical;
        this.curActualFocalLength = curActualFocalLength;
    }

    /** 有效成像宽度，单位 μm（微米），对应 image_width */
    public long getFovHorizontal() {
        return fovHorizontal;
    }

    /** @param fovHorizontal 有效成像宽度，单位 μm（微米），对应 image_width */
    public void setFovHorizontal(long fovHorizontal) {
        this.fovHorizontal = fovHorizontal;
    }

    /** 有效成像高度，单位 μm（微米），对应 image_height */
    public long getFovVertical() {
        return fovVertical;
    }

    /** @param fovVertical 有效成像高度，单位 μm（微米），对应 image_height */
    public void setFovVertical(long fovVertical) {
        this.fovVertical = fovVertical;
    }

    /** 当前镜头实际焦距，整型值 ÷1000 为 mm，对应 lens_focal_length */
    public long getCurActualFocalLength() {
        return curActualFocalLength;
    }

    /** @param curActualFocalLength 当前镜头实际焦距，整型值 ÷1000 为 mm，对应 lens_focal_length */
    public void setCurActualFocalLength(long curActualFocalLength) {
        this.curActualFocalLength = curActualFocalLength;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FovSeiInfo that = (FovSeiInfo) o;
        return fovHorizontal == that.fovHorizontal
                && fovVertical == that.fovVertical
                && curActualFocalLength == that.curActualFocalLength;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fovHorizontal, fovVertical, curActualFocalLength);
    }

    @Override
    public String toString() {
        return "FovSeiInfo{fovHorizontal=" + fovHorizontal + ", fovVertical=" + fovVertical + ", curActualFocalLength=" + curActualFocalLength + '}';
    }
}
