// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/**
 * POI 状态事件。
 */
public final class PoiStatusEvent {

    /** 机场设备 SN */
    private String deviceSn;
    /** 飞行器 SN */
    private String droneSn;
    /** POI 中心点坐标 */
    private Waypoint centerPoint;
    /** 环绕半径，单位：米 */
    private double circleRadius;
    /** 当前环绕速度，单位：m/s */
    private double circleSpeed;
    /** 最大环绕速度，单位：m/s */
    private double maxCircleSpeed;
    /** 状态变更原因 */
    private int reason;
    /** 当前执行状态 */
    private String status;
    /** 时间戳 */
    private long timestamp;
    /** 已执行时长 */
    private long executeDuration;
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex;
    public PoiStatusEvent() {
    }

/**
     * 构造 PoiStatusEvent。
     *
     * @param deviceSn 机场设备 SN
     * @param droneSn 飞行器 SN
     * @param centerPoint POI 中心点坐标
     * @param circleRadius 环绕半径，单位：米
     * @param circleSpeed 当前环绕速度，单位：m/s
     * @param maxCircleSpeed 最大环绕速度，单位：m/s
     * @param reason 状态变更原因
     * @param status 当前执行状态
     * @param timestamp 时间戳
     * @param executeDuration 执行时长
     * @param payloadIndex 负载索引
     */
    public PoiStatusEvent(
            String deviceSn,
            String droneSn,
            Waypoint centerPoint,
            double circleRadius,
            double circleSpeed,
            double maxCircleSpeed,
            int reason,
            String status,
            long timestamp,
            long executeDuration,
            String payloadIndex) {
        this.deviceSn = deviceSn != null ? deviceSn : "";
        this.droneSn = droneSn != null ? droneSn : "";
        this.centerPoint = centerPoint != null ? centerPoint : new Waypoint(0, 0, 0);
        this.circleRadius = circleRadius;
        this.circleSpeed = circleSpeed;
        this.maxCircleSpeed = maxCircleSpeed;
        this.reason = reason;
        this.status = status != null ? status : "";
        this.timestamp = timestamp;
        this.executeDuration = executeDuration;
        this.payloadIndex = payloadIndex != null ? payloadIndex : "";
    }

    /** 机场设备 SN */
    public String getDeviceSn() {
        return deviceSn;
    }

    /** 机场设备 SN */
    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    /** 飞行器 SN */
    public String getDroneSn() {
        return droneSn;
    }

    /** 飞行器 SN */
    public void setDroneSn(String droneSn) {
        this.droneSn = droneSn;
    }

    /** POI 中心点坐标 */
    public Waypoint getCenterPoint() {
        return centerPoint;
    }

    /** POI 中心点坐标 */
    public void setCenterPoint(Waypoint centerPoint) {
        this.centerPoint = centerPoint;
    }

    /** 环绕半径，单位：米 */
    public double getCircleRadius() {
        return circleRadius;
    }

    /** 环绕半径，单位：米 */
    public void setCircleRadius(double circleRadius) {
        this.circleRadius = circleRadius;
    }

    /** 当前环绕速度，单位：m/s */
    public double getCircleSpeed() {
        return circleSpeed;
    }

    /** 当前环绕速度，单位：m/s */
    public void setCircleSpeed(double circleSpeed) {
        this.circleSpeed = circleSpeed;
    }

    /** 最大环绕速度，单位：m/s */
    public double getMaxCircleSpeed() {
        return maxCircleSpeed;
    }

    /** 最大环绕速度，单位：m/s */
    public void setMaxCircleSpeed(double maxCircleSpeed) {
        this.maxCircleSpeed = maxCircleSpeed;
    }

    /** 状态变更原因 */
    public int getReason() {
        return reason;
    }

    /** 状态变更原因 */
    public void setReason(int reason) {
        this.reason = reason;
    }

    /** 当前执行状态 */
    public String getStatus() {
        return status;
    }

    /** 当前执行状态 */
    public void setStatus(String status) {
        this.status = status;
    }

    /** 时间戳 */
    public long getTimestamp() {
        return timestamp;
    }

    /** 时间戳 */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /** 已执行时长 */
    public long getExecuteDuration() {
        return executeDuration;
    }

    /** @param executeDuration 已执行时长 */
    public void setExecuteDuration(long executeDuration) {
        this.executeDuration = executeDuration;
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() {
        return payloadIndex;
    }

    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) {
        this.payloadIndex = payloadIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PoiStatusEvent that = (PoiStatusEvent) o;
        return Double.compare(that.circleRadius, circleRadius) == 0
                && Double.compare(that.circleSpeed, circleSpeed) == 0
                && Double.compare(that.maxCircleSpeed, maxCircleSpeed) == 0
                && reason == that.reason
                && timestamp == that.timestamp
                && executeDuration == that.executeDuration
                && Objects.equals(deviceSn, that.deviceSn)
                && Objects.equals(droneSn, that.droneSn)
                && Objects.equals(centerPoint, that.centerPoint)
                && Objects.equals(status, that.status)
                && Objects.equals(payloadIndex, that.payloadIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                deviceSn, droneSn, centerPoint, circleRadius, circleSpeed, maxCircleSpeed,
                reason, status, timestamp, executeDuration, payloadIndex);
    }

    @Override
    public String toString() {
        return "PoiStatusEvent{"
                + "deviceSn='" + deviceSn + '\''
                + ", droneSn='" + droneSn + '\''
                + ", centerPoint=" + centerPoint
                + ", circleRadius=" + circleRadius
                + ", circleSpeed=" + circleSpeed
                + ", maxCircleSpeed=" + maxCircleSpeed
                + ", reason=" + reason
                + ", status='" + status + '\''
                + ", timestamp=" + timestamp
                + ", executeDuration=" + executeDuration
                + ", payloadIndex='" + payloadIndex + '\''
                + '}';
    }
}
