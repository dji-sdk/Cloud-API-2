// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** POI任务数据。 */
public final class PoiTaskData {
    /** 任务 UUID */
    private String uuid;
    /** 机场序列号 */
    private String sn;
    /** 设备（无人机）序列号 */
    private String deviceSn;
    /** POI 中心点坐标 */
    private PoiCenterPoint poiCenterPoint;
    /** 飞行速度（米/秒） */
    private double speed;
    /** 环绕半径（米） */
    private double circleRadius;
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex;
    /** 任务状态 */
    private int status;
    /** 错误码 */
    private int errCode;
    public PoiTaskData() {
    }

/**
     * 构造 PoiTaskData。
     *
     * @param uuid 任务 UUID
     * @param sn 起飞机场序列号
     * @param deviceSn 设备 SN（精确匹配）
     * @param poiCenterPoint POI 中心点坐标
     * @param speed 环绕速度，单位：m/s
     * @param circleRadius 环绕半径，单位：米
     * @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
     * @param status 状态
     * @param errCode 错误码
     */
    public PoiTaskData(String uuid, String sn, String deviceSn,
            PoiCenterPoint poiCenterPoint, double speed, double circleRadius,
            String payloadIndex, int status, int errCode) {
        this.uuid = uuid != null ? uuid : "";
        this.sn = sn != null ? sn : "";
        this.deviceSn = deviceSn != null ? deviceSn : "";
        this.poiCenterPoint = poiCenterPoint;
        this.speed = speed;
        this.circleRadius = circleRadius;
        this.payloadIndex = payloadIndex != null ? payloadIndex : "";
        this.status = status;
        this.errCode = errCode;
    }

    /** 任务 UUID */
    public String getUuid() { return uuid; }
    /** 机场序列号 */
    public String getSn() { return sn; }
    /** 设备（无人机）序列号 */
    public String getDeviceSn() { return deviceSn; }
    /** POI 中心点坐标 */
    public PoiCenterPoint getPoiCenterPoint() { return poiCenterPoint; }
    /** 飞行速度（米/秒） */
    public double getSpeed() { return speed; }
    /** 环绕半径（米） */
    public double getCircleRadius() { return circleRadius; }
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** 任务状态 */
    public int getStatus() { return status; }
    /** 错误码 */
    public int getErrCode() { return errCode; }

    /** 任务 UUID */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /** @param sn 机场序列号 */
    public void setSn(String sn) {
        this.sn = sn;
    }

    /** @param deviceSn 设备（无人机）序列号 */
    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    /** POI 中心点坐标 */
    public void setPoiCenterPoint(PoiCenterPoint poiCenterPoint) {
        this.poiCenterPoint = poiCenterPoint;
    }

    /** @param speed 飞行速度（米/秒） */
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    /** @param circleRadius 环绕半径（米） */
    public void setCircleRadius(double circleRadius) {
        this.circleRadius = circleRadius;
    }

    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) {
        this.payloadIndex = payloadIndex;
    }

    /** @param status 任务状态 */
    public void setStatus(int status) {
        this.status = status;
    }

    /** 错误码 */
    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PoiTaskData that = (PoiTaskData) o;
        return Objects.equals(uuid, that.uuid)
                && Objects.equals(sn, that.sn)
                && Objects.equals(deviceSn, that.deviceSn)
                && Objects.equals(poiCenterPoint, that.poiCenterPoint)
                && Double.compare(that.speed, speed) == 0
                && Double.compare(that.circleRadius, circleRadius) == 0
                && Objects.equals(payloadIndex, that.payloadIndex)
                && status == that.status
                && errCode == that.errCode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                uuid,
                sn,
                deviceSn,
                poiCenterPoint,
                speed,
                circleRadius,
                payloadIndex,
                status,
                errCode);
    }

    @Override
    public String toString() {
        return "PoiTaskData{"
                + "uuid='" + uuid + '\''
                + ", sn='" + sn + '\''
                + ", deviceSn='" + deviceSn + '\''
                + ", poiCenterPoint=" + poiCenterPoint
                + ", speed=" + speed
                + ", circleRadius=" + circleRadius
                + ", payloadIndex='" + payloadIndex + '\''
                + ", status=" + status
                + ", errCode=" + errCode
                + '}';
    }
}
