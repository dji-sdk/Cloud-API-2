// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** POI 环绕飞行进入参数 */
public class POIEnterParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    /** POI 中心点坐标 */
    private Waypoint poiCenterPoint;
    /** 环绕速度，单位：m/s */
    private double speed;
    /** 环绕半径，单位：米 */
    private double circleRadius;

    public POIEnterParams() {
    }


    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }
    /** POI 中心点坐标 */
    public Waypoint getPoiCenterPoint() { return poiCenterPoint; }
    /** @param poiCenterPoint POI 中心点坐标 */
    public void setPoiCenterPoint(Waypoint poiCenterPoint) { this.poiCenterPoint = poiCenterPoint; }
    /** 环绕速度，单位：m/s */
    public double getSpeed() { return speed; }
    /** @param speed 环绕速度，单位：m/s */
    public void setSpeed(double speed) { this.speed = speed; }
    /** 环绕半径，单位：米 */
    public double getCircleRadius() { return circleRadius; }
    /** @param circleRadius 环绕半径，单位：米 */
    public void setCircleRadius(double circleRadius) { this.circleRadius = circleRadius; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        POIEnterParams that = (POIEnterParams) o;
        return Objects.equals(payloadIndex, that.payloadIndex)
                && Objects.equals(poiCenterPoint, that.poiCenterPoint)
                && Double.compare(that.speed, speed) == 0
                && Double.compare(that.circleRadius, circleRadius) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, poiCenterPoint, speed, circleRadius);
    }

    @Override
    public String toString() {
        return "POIEnterParams{"
                + "payloadIndex='" + payloadIndex + '\''
                + ", poiCenterPoint=" + poiCenterPoint
                + ", speed=" + speed
                + ", circleRadius=" + circleRadius
                + '}';
    }
}
