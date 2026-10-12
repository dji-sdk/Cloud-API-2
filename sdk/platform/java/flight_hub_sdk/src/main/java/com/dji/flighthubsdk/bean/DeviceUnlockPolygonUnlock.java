// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 设备解锁 - 多边形区域解锁（顶点列表） */
public final class DeviceUnlockPolygonUnlock {

    private List<DeviceUnlockPolygonPoint> points;
    public DeviceUnlockPolygonUnlock() {
    }

/**
     * 构造 DeviceUnlockPolygonUnlock。
     *
     * @param points 坐标点列表
     */
    public DeviceUnlockPolygonUnlock(List<DeviceUnlockPolygonPoint> points) {
        this.points = points != null ? points : Collections.emptyList();
    }

    /** 坐标点列表 */
    public List<DeviceUnlockPolygonPoint> getPoints() {
        return points;
    }

    /** 坐标点列表 */
    public void setPoints(List<DeviceUnlockPolygonPoint> points) {
        this.points = points;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceUnlockPolygonUnlock that = (DeviceUnlockPolygonUnlock) o;
        return Objects.equals(points, that.points);
    }

    @Override
    public int hashCode() {
        return Objects.hash(points);
    }

    @Override
    public String toString() {
        return "DeviceUnlockPolygonUnlock{points=" + points + '}';
    }
}
