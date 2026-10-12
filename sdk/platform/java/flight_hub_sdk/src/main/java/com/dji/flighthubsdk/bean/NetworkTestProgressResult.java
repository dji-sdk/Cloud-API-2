// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 网络测试进度结果。 */
public final class NetworkTestProgressResult {
    private String message;
    private String bid;
    private double createTime;
    private double updateTime;
    private double downloadSpeed;
    private double uploadSpeed;
    public NetworkTestProgressResult() {
    }

/**
     * 构造 NetworkTestProgressResult。
     *
     * @param message 消息
     * @param bid 业务ID
     * @param createTime 创建时间
     * @param updateTime 更新时间
     * @param downloadSpeed 下载速度
     * @param uploadSpeed 上传速度
     */
    public NetworkTestProgressResult(
            String message,
            String bid,
            double createTime,
            double updateTime,
            double downloadSpeed,
            double uploadSpeed) {
        this.message = message != null ? message : "";
        this.bid = bid != null ? bid : "";
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.downloadSpeed = downloadSpeed;
        this.uploadSpeed = uploadSpeed;
    }

    /** 消息 */
    public String getMessage() {
        return message;
    }

    /** 消息 */
    public void setMessage(String message) {
        this.message = message;
    }

    /** 业务ID */
    public String getBid() {
        return bid;
    }

    /** 业务ID */
    public void setBid(String bid) {
        this.bid = bid;
    }

    /** 创建时间 */
    public double getCreateTime() {
        return createTime;
    }

    /** 创建时间 */
    public void setCreateTime(double createTime) {
        this.createTime = createTime;
    }

    /** 更新时间 */
    public double getUpdateTime() {
        return updateTime;
    }

    /** 更新时间 */
    public void setUpdateTime(double updateTime) {
        this.updateTime = updateTime;
    }

    /** 下载速度 */
    public double getDownloadSpeed() {
        return downloadSpeed;
    }

    /** 下载速度 */
    public void setDownloadSpeed(double downloadSpeed) {
        this.downloadSpeed = downloadSpeed;
    }

    /** 上传速度 */
    public double getUploadSpeed() {
        return uploadSpeed;
    }

    /** 上传速度 */
    public void setUploadSpeed(double uploadSpeed) {
        this.uploadSpeed = uploadSpeed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NetworkTestProgressResult that = (NetworkTestProgressResult) o;
        return Objects.equals(message, that.message)
                && Objects.equals(bid, that.bid)
                && Double.compare(that.createTime, createTime) == 0
                && Double.compare(that.updateTime, updateTime) == 0
                && Double.compare(that.downloadSpeed, downloadSpeed) == 0
                && Double.compare(that.uploadSpeed, uploadSpeed) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                message,
                bid,
                createTime,
                updateTime,
                downloadSpeed,
                uploadSpeed);
    }

    @Override
    public String toString() {
        return "NetworkTestProgressResult{"
                + "message='" + message + '\''
                + ", bid='" + bid + '\''
                + ", createTime=" + createTime
                + ", updateTime=" + updateTime
                + ", downloadSpeed=" + downloadSpeed
                + ", uploadSpeed=" + uploadSpeed
                + '}';
    }
}
