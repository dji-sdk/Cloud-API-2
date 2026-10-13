// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 创建设备日志报告结果。 */
public final class CreateDeviceLogReportResult {
    /** 追踪编号 */
    private String trackNumber;
    /** 报告 UUID */
    private String reportUuid;
    /** 错误日志列表 */
    private String errorLogList;
    private CreateDeviceJobDataResult createDeviceJobData;
    public CreateDeviceLogReportResult() {
    }

/**
     * 构造 CreateDeviceLogReportResult。
     *
     * @param trackNumber 追踪编号
     * @param reportUuid 报告UUID
     * @param errorLogList 错误日志列表
     * @param createDeviceJobData 创建设备作业数据
     */
    public CreateDeviceLogReportResult(
            String trackNumber,
            String reportUuid,
            String errorLogList,
            CreateDeviceJobDataResult createDeviceJobData) {
        this.trackNumber = trackNumber != null ? trackNumber : "";
        this.reportUuid = reportUuid != null ? reportUuid : "";
        this.errorLogList = errorLogList != null ? errorLogList : "";
        this.createDeviceJobData = createDeviceJobData;
    }

    /** 追踪编号 */
    public String getTrackNumber() {
        return trackNumber;
    }

    /** 追踪编号 */
    public void setTrackNumber(String trackNumber) {
        this.trackNumber = trackNumber;
    }

    /** 报告 UUID */
    public String getReportUuid() {
        return reportUuid;
    }

    /** @param reportUuid 报告 UUID */
    public void setReportUuid(String reportUuid) {
        this.reportUuid = reportUuid;
    }

    /** 错误日志列表 */
    public String getErrorLogList() {
        return errorLogList;
    }

    /** 错误日志列表 */
    public void setErrorLogList(String errorLogList) {
        this.errorLogList = errorLogList;
    }

    /** 创建设备作业数据 */
    public CreateDeviceJobDataResult getCreateDeviceJobData() {
        return createDeviceJobData;
    }

    /** 创建设备作业数据 */
    public void setCreateDeviceJobData(CreateDeviceJobDataResult createDeviceJobData) {
        this.createDeviceJobData = createDeviceJobData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CreateDeviceLogReportResult that = (CreateDeviceLogReportResult) o;
        return Objects.equals(trackNumber, that.trackNumber)
                && Objects.equals(reportUuid, that.reportUuid)
                && Objects.equals(errorLogList, that.errorLogList)
                && Objects.equals(createDeviceJobData, that.createDeviceJobData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trackNumber, reportUuid, errorLogList, createDeviceJobData);
    }

    @Override
    public String toString() {
        return "CreateDeviceLogReportResult{"
                + "trackNumber='" + trackNumber + '\''
                + ", reportUuid='" + reportUuid + '\''
                + ", errorLogList='" + errorLogList + '\''
                + ", createDeviceJobData=" + createDeviceJobData
                + '}';
    }
}
