// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 报修订单 - 日志报告信息 */
public final class MaintenanceOrderLogReport {
    private String reportUuid;
    private String trackNumber;
    private String status;
    public MaintenanceOrderLogReport() {
    }

/**
     * 构造 MaintenanceOrderLogReport。
     *
     * @param reportUuid 报告UUID
     * @param trackNumber 物流单号
     * @param status 状态
     */
    public MaintenanceOrderLogReport(String reportUuid, String trackNumber, String status) {
        this.reportUuid = reportUuid;
        this.trackNumber = trackNumber;
        this.status = status;
    }

    /** 报告UUID */
    public String getReportUuid() { return reportUuid; }
    /** 物流单号 */
    public String getTrackNumber() { return trackNumber; }
    /** 状态 */
    public String getStatus() { return status; }

    /** 报告UUID */
    public void setReportUuid(String reportUuid) {
        this.reportUuid = reportUuid;
    }

    /** 物流单号 */
    public void setTrackNumber(String trackNumber) {
        this.trackNumber = trackNumber;
    }

    /** 状态 */
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MaintenanceOrderLogReport that = (MaintenanceOrderLogReport) o;
        return Objects.equals(reportUuid, that.reportUuid)
                && Objects.equals(trackNumber, that.trackNumber)
                && Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportUuid, trackNumber, status);
    }

    @Override
    public String toString() {
        return "MaintenanceOrderLogReport{reportUuid='" + reportUuid + '\'' + ", trackNumber='" + trackNumber + '\'' + ", status='" + status + '\'' + '}';
    }
}
