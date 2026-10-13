// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 报修订单详情 - 状态变更历史 */
public final class MaintenanceOrderStatusHistoryItem {
    private int fromStatus;
    private int toStatus;
    private String changeTime;
    private String operatorId;
    private String operatorName;
    private String remark;
    public MaintenanceOrderStatusHistoryItem() {
    }

/**
     * 构造 MaintenanceOrderStatusHistoryItem。
     *
     * @param fromStatus 变更前状态
     * @param toStatus 变更后状态
     * @param changeTime 变更时间
     * @param operatorId 操作者ID
     * @param operatorName 操作者名称
     * @param remark 备注
     */
    public MaintenanceOrderStatusHistoryItem(int fromStatus, int toStatus, String changeTime, String operatorId, String operatorName, String remark) {
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changeTime = changeTime;
        this.operatorId = operatorId;
        this.operatorName = operatorName;
        this.remark = remark;
    }

    /** 变更前状态 */
    public int getFromStatus() { return fromStatus; }
    /** 变更后状态 */
    public int getToStatus() { return toStatus; }
    /** 变更时间 */
    public String getChangeTime() { return changeTime; }
    /** 操作者ID */
    public String getOperatorId() { return operatorId; }
    /** 操作者名称 */
    public String getOperatorName() { return operatorName; }
    /** 备注 */
    public String getRemark() { return remark; }

    /** 变更前状态 */
    public void setFromStatus(int fromStatus) {
        this.fromStatus = fromStatus;
    }

    /** 变更后状态 */
    public void setToStatus(int toStatus) {
        this.toStatus = toStatus;
    }

    /** 变更时间 */
    public void setChangeTime(String changeTime) {
        this.changeTime = changeTime;
    }

    /** 操作者ID */
    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    /** 操作者名称 */
    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    /** 备注 */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MaintenanceOrderStatusHistoryItem that = (MaintenanceOrderStatusHistoryItem) o;
        return fromStatus == that.fromStatus
                && toStatus == that.toStatus
                && Objects.equals(changeTime, that.changeTime)
                && Objects.equals(operatorId, that.operatorId)
                && Objects.equals(operatorName, that.operatorName)
                && Objects.equals(remark, that.remark);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                fromStatus,
                toStatus,
                changeTime,
                operatorId,
                operatorName,
                remark);
    }

    @Override
    public String toString() {
        return "MaintenanceOrderStatusHistoryItem{"
                + "fromStatus=" + fromStatus
                + ", toStatus=" + toStatus
                + ", changeTime='" + changeTime + '\''
                + ", operatorId='" + operatorId + '\''
                + ", operatorName='" + operatorName + '\''
                + ", remark='" + remark + '\''
                + '}';
    }
}
