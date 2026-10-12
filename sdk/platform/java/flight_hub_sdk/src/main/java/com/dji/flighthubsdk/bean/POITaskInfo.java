// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import com.dji.flighthubsdk.value.POITaskStatus;
import java.util.Objects;
/**
 * POI 任务信息：标识、关联飞行任务、中心点、速度与状态等。
 *
 * <p>{@link #getTaskStatus()} 使用 {@link POITaskStatus} 表示任务生命周期；需要与协议相同的整型编码时可使用
 * {@link POITaskStatus#getValue()}。未识别的状态值会落在 {@link POITaskStatus#UNKNOWN}。</p>
 *
 * <p>{@link #getErrCode()} 为协议中的整型编码，当前 SDK 未提供单独枚举封装。</p>
 */
public final class POITaskInfo {

    /** POI 任务 UUID */
    private String uuid;
    /** 设备 SN（机场/遥控器） */
    private String deviceSn;
    /** 飞行器 SN */
    private String sn;
    private POITaskStatus taskStatus;
    /** 错误码（任务失败时的错误代码） */
    private int errCode;
    /** POI 中心点坐标（经纬度和高度） */
    private Waypoint poiCenterPoint;
    /** 环绕速度，单位：m/s */
    private double speed;
    /** 环绕半径，单位：米 */
    private double circleRadius;
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex;
    public POITaskInfo() {
    }

/**
     * 构造 POITaskInfo。
     *
     * @param uuid POI 任务 UUID
     * @param deviceSn 设备 SN
     * @param sn 飞行器 SN
     * @param taskStatus POI 任务状态；需要整型编码时使用 {@link POITaskStatus#getValue()}
     * @param errCode 错误码
     * @param poiCenterPoint POI 中心点坐标
     * @param speed 环绕速度，单位：m/s
     * @param circleRadius 环绕半径，单位：米
     * @param payloadIndex 负载索引
     */
    public POITaskInfo(
            String uuid,
            String deviceSn,
            String sn,
            POITaskStatus taskStatus,
            int errCode,
            Waypoint poiCenterPoint,
            double speed,
            double circleRadius,
            String payloadIndex) {
        this.uuid = uuid != null ? uuid : "";
        this.deviceSn = deviceSn != null ? deviceSn : "";
        this.sn = sn != null ? sn : "";
        this.taskStatus = taskStatus != null ? taskStatus : POITaskStatus.UNKNOWN;
        this.errCode = errCode;
        this.poiCenterPoint = poiCenterPoint != null ? poiCenterPoint : new Waypoint(0, 0, 0);
        this.speed = speed;
        this.circleRadius = circleRadius;
        this.payloadIndex = payloadIndex != null ? payloadIndex : "";
    }

    /** POI 任务 UUID */
    public String getUuid() {
        return uuid;
    }

    /** POI 任务 UUID */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /** 设备 SN（机场/遥控器） */
    public String getDeviceSn() {
        return deviceSn;
    }

    /** 设备 SN（机场/遥控器） */
    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    /** 飞行器 SN */
    public String getSn() {
        return sn;
    }

    /** 飞行器 SN */
    public void setSn(String sn) {
        this.sn = sn;
    }

    /** POI 任务状态；需要整型编码时使用 {@link POITaskStatus#getValue()} */
    public POITaskStatus getTaskStatus() {
        return taskStatus;
    }

    /** POI 任务状态；需要整型编码时使用 {@link POITaskStatus#getValue()} */
    public void setTaskStatus(POITaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    /** 错误码（任务失败时的错误代码） */
    public int getErrCode() {
        return errCode;
    }

    /** @param errCode 错误码（任务失败时的错误代码） */
    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }

    /** POI 中心点坐标（经纬度和高度） */
    public Waypoint getPoiCenterPoint() {
        return poiCenterPoint;
    }

    /** @param poiCenterPoint POI 中心点坐标（经纬度和高度） */
    public void setPoiCenterPoint(Waypoint poiCenterPoint) {
        this.poiCenterPoint = poiCenterPoint;
    }

    /** 环绕速度，单位：m/s */
    public double getSpeed() {
        return speed;
    }

    /** 环绕速度，单位：m/s */
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    /** 环绕半径，单位：米 */
    public double getCircleRadius() {
        return circleRadius;
    }

    /** 环绕半径，单位：米 */
    public void setCircleRadius(double circleRadius) {
        this.circleRadius = circleRadius;
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
        POITaskInfo that = (POITaskInfo) o;
        return taskStatus == that.taskStatus
                && errCode == that.errCode
                && Double.compare(that.speed, speed) == 0
                && Double.compare(that.circleRadius, circleRadius) == 0
                && Objects.equals(uuid, that.uuid)
                && Objects.equals(deviceSn, that.deviceSn)
                && Objects.equals(sn, that.sn)
                && Objects.equals(poiCenterPoint, that.poiCenterPoint)
                && Objects.equals(payloadIndex, that.payloadIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                uuid, deviceSn, sn, taskStatus, errCode, poiCenterPoint,
                speed, circleRadius, payloadIndex);
    }

    @Override
    public String toString() {
        return "POITaskInfo{"
                + "uuid='" + uuid + '\''
                + ", deviceSn='" + deviceSn + '\''
                + ", sn='" + sn + '\''
                + ", taskStatus=" + taskStatus
                + ", errCode=" + errCode
                + ", poiCenterPoint=" + poiCenterPoint
                + ", speed=" + speed
                + ", circleRadius=" + circleRadius
                + ", payloadIndex='" + payloadIndex + '\''
                + '}';
    }
}
