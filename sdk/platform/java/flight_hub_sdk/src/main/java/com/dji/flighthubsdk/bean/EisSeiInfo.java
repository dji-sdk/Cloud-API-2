// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** EIS SEI 信息。 */
public final class EisSeiInfo {

    /** 版本号 */
    private int version;
    /** 裁剪区域左上角 */
    private CamCropPos leftTop;
    /** 裁剪区域右上角 */
    private CamCropPos rightTop;
    /** 裁剪区域右下角 */
    private CamCropPos rightBottom;
    /** 裁剪区域左下角 */
    private CamCropPos leftBottom;
    public EisSeiInfo() {
    }

/**
     * 构造 EisSeiInfo。
     *
     * @param version 版本号
     * @param leftTop 左上角裁剪坐标
     * @param rightTop 右上角裁剪坐标
     * @param rightBottom 右下角裁剪坐标
     * @param leftBottom 左下角裁剪坐标
     */
    public EisSeiInfo(
            int version,
            CamCropPos leftTop,
            CamCropPos rightTop,
            CamCropPos rightBottom,
            CamCropPos leftBottom) {
        this.version = version;
        this.leftTop = leftTop != null ? leftTop : new CamCropPos(0, 0);
        this.rightTop = rightTop != null ? rightTop : new CamCropPos(0, 0);
        this.rightBottom = rightBottom != null ? rightBottom : new CamCropPos(0, 0);
        this.leftBottom = leftBottom != null ? leftBottom : new CamCropPos(0, 0);
    }

    /** 版本号 */
    public int getVersion() {
        return version;
    }

    /** 版本号 */
    public void setVersion(int version) {
        this.version = version;
    }

    /** 裁剪区域左上角 */
    public CamCropPos getLeftTop() {
        return leftTop;
    }

    /** @param leftTop 裁剪区域左上角 */
    public void setLeftTop(CamCropPos leftTop) {
        this.leftTop = leftTop;
    }

    /** 裁剪区域右上角 */
    public CamCropPos getRightTop() {
        return rightTop;
    }

    /** @param rightTop 裁剪区域右上角 */
    public void setRightTop(CamCropPos rightTop) {
        this.rightTop = rightTop;
    }

    /** 裁剪区域右下角 */
    public CamCropPos getRightBottom() {
        return rightBottom;
    }

    /** @param rightBottom 裁剪区域右下角 */
    public void setRightBottom(CamCropPos rightBottom) {
        this.rightBottom = rightBottom;
    }

    /** 裁剪区域左下角 */
    public CamCropPos getLeftBottom() {
        return leftBottom;
    }

    /** @param leftBottom 裁剪区域左下角 */
    public void setLeftBottom(CamCropPos leftBottom) {
        this.leftBottom = leftBottom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EisSeiInfo eisSeiInfo = (EisSeiInfo) o;
        return version == eisSeiInfo.version
                && leftTop.equals(eisSeiInfo.leftTop)
                && rightTop.equals(eisSeiInfo.rightTop)
                && rightBottom.equals(eisSeiInfo.rightBottom)
                && leftBottom.equals(eisSeiInfo.leftBottom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, leftTop, rightTop, rightBottom, leftBottom);
    }

    @Override
    public String toString() {
        return "EisSeiInfo{"
                + "version=" + version
                + ", leftTop=" + leftTop
                + ", rightTop=" + rightTop
                + ", rightBottom=" + rightBottom
                + ", leftBottom=" + leftBottom
                + '}';
    }
}
