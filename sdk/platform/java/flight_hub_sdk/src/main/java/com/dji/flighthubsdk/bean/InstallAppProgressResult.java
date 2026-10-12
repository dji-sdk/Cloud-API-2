// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 安装应用进度结果 */
public final class InstallAppProgressResult {
    private String bid;
    private String status;
    private int percent;
    private long createTime;
    private long updateTime;
    private int deviceCode;
    private InstallAppProgressExtResult ext;
    public InstallAppProgressResult() {
    }

/**
     * 构造 InstallAppProgressResult。
     *
     * @param bid 业务 ID
     * @param status 状态
     * @param percent 进度百分比
     * @param createTime 创建时间
     * @param updateTime 更新时间
     * @param deviceCode 设备错误码
     * @param ext 进度扩展信息
     */
    public InstallAppProgressResult(String bid, String status, int percent, long createTime, long updateTime, int deviceCode, InstallAppProgressExtResult ext) {
        this.bid = bid != null ? bid : "";
        this.status = status != null ? status : "";
        this.percent = percent;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.deviceCode = deviceCode;
        this.ext = ext;
    }

    /** 业务 ID */
    public String getBid() { return bid; }
    /** 状态 */
    public String getStatus() { return status; }
    /** 进度百分比 */
    public int getPercent() { return percent; }
    /** 创建时间 */
    public long getCreateTime() { return createTime; }
    /** 更新时间 */
    public long getUpdateTime() { return updateTime; }
    /** 设备错误码 */
    public int getDeviceCode() { return deviceCode; }
    /** 进度扩展信息 */
    public InstallAppProgressExtResult getExt() { return ext; }

    /** 业务 ID */
    public void setBid(String bid) {
        this.bid = bid;
    }

    /** 状态 */
    public void setStatus(String status) {
        this.status = status;
    }

    /** 进度百分比 */
    public void setPercent(int percent) {
        this.percent = percent;
    }

    /** 创建时间 */
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    /** 更新时间 */
    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    /** 设备错误码 */
    public void setDeviceCode(int deviceCode) {
        this.deviceCode = deviceCode;
    }

    /** 进度扩展信息 */
    public void setExt(InstallAppProgressExtResult ext) {
        this.ext = ext;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InstallAppProgressResult that = (InstallAppProgressResult) o;
        return Objects.equals(bid, that.bid)
                && Objects.equals(status, that.status)
                && percent == that.percent
                && createTime == that.createTime
                && updateTime == that.updateTime
                && deviceCode == that.deviceCode
                && Objects.equals(ext, that.ext);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                bid,
                status,
                percent,
                createTime,
                updateTime,
                deviceCode,
                ext);
    }

    @Override
    public String toString() {
        return "InstallAppProgressResult{"
                + "bid='" + bid + '\''
                + ", status='" + status + '\''
                + ", percent=" + percent
                + ", createTime=" + createTime
                + ", updateTime=" + updateTime
                + ", deviceCode=" + deviceCode
                + ", ext=" + ext
                + '}';
    }
}
