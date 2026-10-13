// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 固件状态中的服务信息 */
public final class FirmwareService {

    private String bid;
    private String status;
    private long createTime;
    private long updateTime;
    private int deviceCode;
    private FirmwareServiceProgress progress;
    private FirmwareServiceData data;
    public FirmwareService() {
    }

/**
     * 构造 FirmwareService。
     *
     * @param bid 业务标识
     * @param status 状态
     * @param createTime 创建时间
     * @param updateTime 更新时间
     * @param deviceCode 设备编码
     * @param progress 进度
     * @param data 数据
     */
    public FirmwareService(
            String bid,
            String status,
            long createTime,
            long updateTime,
            int deviceCode,
            FirmwareServiceProgress progress,
            FirmwareServiceData data) {
        this.bid = bid != null ? bid : "";
        this.status = status != null ? status : "";
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.deviceCode = deviceCode;
        this.progress = progress;
        this.data = data;
    }

    /** 业务标识 */
    public String getBid() {
        return bid;
    }

    /** 业务标识 */
    public void setBid(String bid) {
        this.bid = bid;
    }

    /** 状态 */
    public String getStatus() {
        return status;
    }

    /** 状态 */
    public void setStatus(String status) {
        this.status = status;
    }

    /** 创建时间 */
    public long getCreateTime() {
        return createTime;
    }

    /** 创建时间 */
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    /** 更新时间 */
    public long getUpdateTime() {
        return updateTime;
    }

    /** 更新时间 */
    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    /** 设备编码 */
    public int getDeviceCode() {
        return deviceCode;
    }

    /** 设备编码 */
    public void setDeviceCode(int deviceCode) {
        this.deviceCode = deviceCode;
    }

    /** 进度 */
    public FirmwareServiceProgress getProgress() {
        return progress;
    }

    /** 进度 */
    public void setProgress(FirmwareServiceProgress progress) {
        this.progress = progress;
    }

    /** 数据 */
    public FirmwareServiceData getData() {
        return data;
    }

    /** 数据 */
    public void setData(FirmwareServiceData data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FirmwareService that = (FirmwareService) o;
        return Objects.equals(bid, that.bid)
                && Objects.equals(status, that.status)
                && createTime == that.createTime
                && updateTime == that.updateTime
                && deviceCode == that.deviceCode
                && Objects.equals(progress, that.progress)
                && Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                bid,
                status,
                createTime,
                updateTime,
                deviceCode,
                progress,
                data);
    }

    @Override
    public String toString() {
        return "FirmwareService{"
                + "bid='" + bid + '\''
                + ", status='" + status + '\''
                + ", createTime=" + createTime
                + ", updateTime=" + updateTime
                + ", deviceCode=" + deviceCode
                + ", progress=" + progress
                + ", data=" + data
                + '}';
    }
}
