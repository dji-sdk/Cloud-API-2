// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;
/** 设备推荐请求 */
public class DeviceRecommendationReq {
    /** 设备 SN 列表 */
    private List<String> sn = new ArrayList<>();
    /** 推荐策略 ID */
    private int strategyId = 1;
    private double flyToLatitude;
    private double flyToLongitude;
    private double flyToHeight;
    private boolean hasFlyToPosition;

    public DeviceRecommendationReq() {
    }


    /** 设备 SN 列表 */
    public List<String> getSn() { return sn; }
    /** @param sn 设备 SN 列表 */
    public void setSn(List<String> sn) { this.sn = sn != null ? sn : new ArrayList<>(); }
    /** 推荐策略 ID */
    public int getStrategyId() { return strategyId; }
    /** @param strategyId 推荐策略 ID */
    public void setStrategyId(int strategyId) { this.strategyId = strategyId; }
    /** 飞向目标纬度。匹配 strategyId=1 时传入 */
    public double getFlyToLatitude() { return flyToLatitude; }
    /** @param flyToLatitude 飞向目标纬度。匹配 strategyId=1 时传入 */
    public void setFlyToLatitude(double flyToLatitude) { this.flyToLatitude = flyToLatitude; }
    /** 飞向目标经度。匹配 strategyId=1 时传入 */
    public double getFlyToLongitude() { return flyToLongitude; }
    /** @param flyToLongitude 飞向目标经度。匹配 strategyId=1 时传入 */
    public void setFlyToLongitude(double flyToLongitude) { this.flyToLongitude = flyToLongitude; }
    /** 飞向目标高度。匹配 strategyId=1 时传入 */
    public double getFlyToHeight() { return flyToHeight; }
    /** @param flyToHeight 飞向目标高度。匹配 strategyId=1 时传入 */
    public void setFlyToHeight(double flyToHeight) { this.flyToHeight = flyToHeight; }
    /** 是否包含飞向目标位置 */
    public boolean isHasFlyToPosition() { return hasFlyToPosition; }
    /** @param hasFlyToPosition 是否包含飞向目标位置 */
    public void setHasFlyToPosition(boolean hasFlyToPosition) { this.hasFlyToPosition = hasFlyToPosition; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceRecommendationReq that = (DeviceRecommendationReq) o;
        return Objects.equals(sn, that.sn)
                && strategyId == that.strategyId
                && Double.compare(that.flyToLatitude, flyToLatitude) == 0
                && Double.compare(that.flyToLongitude, flyToLongitude) == 0
                && Double.compare(that.flyToHeight, flyToHeight) == 0
                && hasFlyToPosition == that.hasFlyToPosition;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                sn,
                strategyId,
                flyToLatitude,
                flyToLongitude,
                flyToHeight,
                hasFlyToPosition);
    }

    @Override
    public String toString() {
        return "DeviceRecommendationReq{"
                + "sn=" + sn
                + ", strategyId=" + strategyId
                + ", flyToLatitude=" + flyToLatitude
                + ", flyToLongitude=" + flyToLongitude
                + ", flyToHeight=" + flyToHeight
                + ", hasFlyToPosition=" + hasFlyToPosition
                + '}';
    }
}
