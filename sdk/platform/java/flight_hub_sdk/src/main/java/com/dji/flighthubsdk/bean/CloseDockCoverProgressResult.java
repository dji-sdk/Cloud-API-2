// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 关闭机场舱盖进度结果。 */
public final class CloseDockCoverProgressResult {
    private String message;
    private String bid;
    private double createTime;
    private double updateTime;
    public CloseDockCoverProgressResult() {
    }

/**
     * 构造 CloseDockCoverProgressResult。
     *
     * @param message 消息
     * @param bid 业务标识(bid)
     * @param createTime 创建时间
     * @param updateTime 更新时间
     */
    public CloseDockCoverProgressResult(String message, String bid, double createTime, double updateTime) {
        this.message = message != null ? message : "";
        this.bid = bid != null ? bid : "";
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    /** 消息 */
    public String getMessage() {
        return message;
    }

    /** 消息 */
    public void setMessage(String message) {
        this.message = message;
    }

    /** 业务标识(bid) */
    public String getBid() {
        return bid;
    }

    /** 业务标识(bid) */
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CloseDockCoverProgressResult that = (CloseDockCoverProgressResult) o;
        return Objects.equals(message, that.message)
                && Objects.equals(bid, that.bid)
                && Double.compare(that.createTime, createTime) == 0
                && Double.compare(that.updateTime, updateTime) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, bid, createTime, updateTime);
    }

    @Override
    public String toString() {
        return "CloseDockCoverProgressResult{"
                + "message='" + message + '\''
                + ", bid='" + bid + '\''
                + ", createTime=" + createTime
                + ", updateTime=" + updateTime
                + '}';
    }
}
