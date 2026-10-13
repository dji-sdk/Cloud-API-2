// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 直播画面世界区域。 */
public final class CameraLiveviewWorldRegion {

    private double bottom;
    private double left;
    private double right;
    private double top;
    public CameraLiveviewWorldRegion() {
    }

/**
     * 构造 CameraLiveviewWorldRegion。
     *
     * @param bottom 下边界
     * @param left 左边界
     * @param right 右边界
     * @param top 上边界
     */
    public CameraLiveviewWorldRegion(double bottom, double left, double right, double top) {
        this.bottom = bottom;
        this.left = left;
        this.right = right;
        this.top = top;
    }

    /** 下边界 */
    public double getBottom() {
        return bottom;
    }

    /** 下边界 */
    public void setBottom(double bottom) {
        this.bottom = bottom;
    }

    /** 左边界 */
    public double getLeft() {
        return left;
    }

    /** 左边界 */
    public void setLeft(double left) {
        this.left = left;
    }

    /** 右边界 */
    public double getRight() {
        return right;
    }

    /** 右边界 */
    public void setRight(double right) {
        this.right = right;
    }

    /** 上边界 */
    public double getTop() {
        return top;
    }

    /** 上边界 */
    public void setTop(double top) {
        this.top = top;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CameraLiveviewWorldRegion that = (CameraLiveviewWorldRegion) o;
        return Double.compare(that.bottom, bottom) == 0
                && Double.compare(that.left, left) == 0
                && Double.compare(that.right, right) == 0
                && Double.compare(that.top, top) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bottom, left, right, top);
    }

    @Override
    public String toString() {
        return "CameraLiveviewWorldRegion{"
                + "bottom=" + bottom
                + ", left=" + left
                + ", right=" + right
                + ", top=" + top
                + '}';
    }
}
