// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 推荐设备信息。 */
public final class RecommendedDevice {
    /** 飞行器 SN */
    private String droneSn;
    /** 网关设备 SN */
    private String gatewaySn;
    /** 距离，单位：米 */
    private double distance;
    /** 是否匹配 */
    private boolean matched;
    public RecommendedDevice() {
    }

/**
     * 构造 RecommendedDevice。
     *
     * @param droneSn 飞行器序列号
     * @param gatewaySn 网关设备序列号
     * @param distance 距离
     * @param matched 是否匹配
     */
    public RecommendedDevice(String droneSn, String gatewaySn, double distance, boolean matched) {
        this.droneSn = droneSn != null ? droneSn : "";
        this.gatewaySn = gatewaySn != null ? gatewaySn : "";
        this.distance = distance;
        this.matched = matched;
    }

    /** 飞行器 SN */
    public String getDroneSn() { return droneSn; }
    /** 网关设备 SN */
    public String getGatewaySn() { return gatewaySn; }
    /** 距离，单位：米 */
    public double getDistance() { return distance; }
    /** 是否匹配 */
    public boolean isMatched() { return matched; }

    /** @param droneSn 飞行器 SN */
    public void setDroneSn(String droneSn) {
        this.droneSn = droneSn;
    }

    /** @param gatewaySn 网关设备 SN */
    public void setGatewaySn(String gatewaySn) {
        this.gatewaySn = gatewaySn;
    }

    /** @param distance 距离，单位：米 */
    public void setDistance(double distance) {
        this.distance = distance;
    }

    /** 是否匹配 */
    public void setMatched(boolean matched) {
        this.matched = matched;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RecommendedDevice that = (RecommendedDevice) o;
        return Objects.equals(droneSn, that.droneSn)
                && Objects.equals(gatewaySn, that.gatewaySn)
                && Double.compare(that.distance, distance) == 0
                && matched == that.matched;
    }

    @Override
    public int hashCode() {
        return Objects.hash(droneSn, gatewaySn, distance, matched);
    }

    @Override
    public String toString() {
        return "RecommendedDevice{"
                + "droneSn='" + droneSn + '\''
                + ", gatewaySn='" + gatewaySn + '\''
                + ", distance=" + distance
                + ", matched=" + matched
                + '}';
    }
}
