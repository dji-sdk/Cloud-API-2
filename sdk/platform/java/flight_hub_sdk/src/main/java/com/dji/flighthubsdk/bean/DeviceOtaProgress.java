// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 设备固件升级进度 */
public final class DeviceOtaProgress {

    /** 业务 ID */
    private String bid;
    /** 时间戳 */
    private long timestamp;
    /** 状态 */
    private String status;
    /** 结果码 */
    private int result;
    /** 进度信息 */
    private DeviceOtaProgressInfo progress;
    public DeviceOtaProgress() {
    }

/**
     * 构造 DeviceOtaProgress。
     *
     * @param bid 业务 ID
     * @param timestamp 时间戳
     * @param status 状态
     * @param result 结果码
     * @param progress 进度信息
     */
    public DeviceOtaProgress(
            String bid,
            long timestamp,
            String status,
            int result,
            DeviceOtaProgressInfo progress) {
        this.bid = bid != null ? bid : "";
        this.timestamp = timestamp;
        this.status = status != null ? status : "";
        this.result = result;
        this.progress = progress;
    }

    /** 业务 ID */
    public String getBid() {
        return bid;
    }

    /** 业务 ID */
    public void setBid(String bid) {
        this.bid = bid;
    }

    /** 时间戳 */
    public long getTimestamp() {
        return timestamp;
    }

    /** 时间戳 */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /** 状态 */
    public String getStatus() {
        return status;
    }

    /** 状态 */
    public void setStatus(String status) {
        this.status = status;
    }

    /** 结果码 */
    public int getResult() {
        return result;
    }

    /** 结果码 */
    public void setResult(int result) {
        this.result = result;
    }

    /** 进度信息 */
    public DeviceOtaProgressInfo getProgress() {
        return progress;
    }

    /** 进度信息 */
    public void setProgress(DeviceOtaProgressInfo progress) {
        this.progress = progress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceOtaProgress that = (DeviceOtaProgress) o;
        return timestamp == that.timestamp
                && result == that.result
                && Objects.equals(bid, that.bid)
                && Objects.equals(status, that.status)
                && Objects.equals(progress, that.progress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bid, timestamp, status, result, progress);
    }

    @Override
    public String toString() {
        return "DeviceOtaProgress{bid='" + bid + '\''
                + ", timestamp=" + timestamp
                + ", status='" + status + '\''
                + ", result=" + result
                + ", progress=" + progress + '}';
    }
}
