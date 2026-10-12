// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Collections;
import java.util.List;
import java.util.Objects;
/** 日志报告列表结果条目。 */
public final class LogReportListResultItem {
    /** 报告 UUID */
    private String reportUuid;
    /** 报告时间 */
    private long reportTime;
    /** 报告用户 ID */
    private String reportUserId;
    /** 上报用户所属组织呼号 */
    private String reportUserOrganizationCallsign;
    /** 设备拓扑信息 */
    private LogReportListResultDeviceTopo deviceTopo;
    /** 报告描述信息 */
    private String reportInformation;
    /** 追踪编号 */
    private String trackNumber;
    /** 错误发生起始时间 */
    private long errorStartTime;
    /** 附件文件信息 */
    private LogReportListResultAppendixFiles appendixFiles;
    /** 设备日志文件列表 */
    private LogReportListResultDeviceLogs deviceLogs;
    /** 设备上传文件列表 */
    private LogReportListResultDeviceUploadFiles deviceUploadFiles;
    /** 报告状态 */
    private String status;
    /** 上报联系信息 */
    private LogReportListResultContactInformation reportContactInformation;
    /** 业务标识(bid) */
    private String bid;
    /** 设备报告反馈 */
    private String deviceReportFeedback;
    /** 阅读状态 */
    private int readStatus;
    /** 报告 URL */
    private String url;
    public LogReportListResultItem() {
    }

/**
     * 构造 LogReportListResultItem。
     *
     * @param reportUuid 上报 UUID
     * @param reportTime 上报时间
     * @param reportUserId 上报用户 ID
     * @param reportUserOrganizationCallsign 上报用户所属组织呼号
     * @param deviceTopo 设备拓扑信息
     * @param reportInformation 上报信息描述
     * @param trackNumber 追踪编号
     * @param errorStartTime 错误发生起始时间
     * @param appendixFiles 附件文件信息
     * @param status 状态
     * @param reportContactInformation 上报联系信息
     * @param bid 业务标识(bid)
     * @param deviceReportFeedback 设备上报反馈
     * @param readStatus 已读状态
     * @param url 文件下载地址
     */
    public LogReportListResultItem(
            String reportUuid,
            long reportTime,
            String reportUserId,
            String reportUserOrganizationCallsign,
            LogReportListResultDeviceTopo deviceTopo,
            String reportInformation,
            String trackNumber,
            long errorStartTime,
            LogReportListResultAppendixFiles appendixFiles,
            LogReportListResultDeviceLogs deviceLogs,
            LogReportListResultDeviceUploadFiles deviceUploadFiles,
            String status,
            LogReportListResultContactInformation reportContactInformation,
            String bid,
            String deviceReportFeedback,
            int readStatus,
            String url) {
        this.reportUuid = reportUuid != null ? reportUuid : "";
        this.reportTime = reportTime;
        this.reportUserId = reportUserId != null ? reportUserId : "";
        this.reportUserOrganizationCallsign =
                reportUserOrganizationCallsign != null ? reportUserOrganizationCallsign : "";
        this.deviceTopo = deviceTopo;
        this.reportInformation = reportInformation != null ? reportInformation : "";
        this.trackNumber = trackNumber != null ? trackNumber : "";
        this.errorStartTime = errorStartTime;
        this.appendixFiles = appendixFiles;
        this.deviceLogs = deviceLogs;
        this.deviceUploadFiles = deviceUploadFiles;
        this.status = status != null ? status : "";
        this.reportContactInformation = reportContactInformation;
        this.bid = bid != null ? bid : "";
        this.deviceReportFeedback = deviceReportFeedback != null ? deviceReportFeedback : "";
        this.readStatus = readStatus;
        this.url = url != null ? url : "";
    }

    /** 报告 UUID */
    public String getReportUuid() {
        return reportUuid;
    }

    /** @param reportUuid 报告 UUID */
    public void setReportUuid(String reportUuid) {
        this.reportUuid = reportUuid;
    }

    /** 报告时间 */
    public long getReportTime() {
        return reportTime;
    }

    /** @param reportTime 报告时间 */
    public void setReportTime(long reportTime) {
        this.reportTime = reportTime;
    }

    /** 报告用户 ID */
    public String getReportUserId() {
        return reportUserId;
    }

    /** @param reportUserId 报告用户 ID */
    public void setReportUserId(String reportUserId) {
        this.reportUserId = reportUserId;
    }

    /** 上报用户所属组织呼号 */
    public String getReportUserOrganizationCallsign() {
        return reportUserOrganizationCallsign;
    }

    /** 上报用户所属组织呼号 */
    public void setReportUserOrganizationCallsign(String reportUserOrganizationCallsign) {
        this.reportUserOrganizationCallsign = reportUserOrganizationCallsign;
    }

    /** 设备拓扑信息 */
    public LogReportListResultDeviceTopo getDeviceTopo() {
        return deviceTopo;
    }

    /** 设备拓扑信息 */
    public void setDeviceTopo(LogReportListResultDeviceTopo deviceTopo) {
        this.deviceTopo = deviceTopo;
    }

    /** 报告描述信息 */
    public String getReportInformation() {
        return reportInformation;
    }

    /** @param reportInformation 报告描述信息 */
    public void setReportInformation(String reportInformation) {
        this.reportInformation = reportInformation;
    }

    /** 追踪编号 */
    public String getTrackNumber() {
        return trackNumber;
    }

    /** 追踪编号 */
    public void setTrackNumber(String trackNumber) {
        this.trackNumber = trackNumber;
    }

    /** 错误发生起始时间 */
    public long getErrorStartTime() {
        return errorStartTime;
    }

    /** 错误发生起始时间 */
    public void setErrorStartTime(long errorStartTime) {
        this.errorStartTime = errorStartTime;
    }

    /** 附件文件信息 */
    public LogReportListResultAppendixFiles getAppendixFiles() {
        return appendixFiles;
    }

    /** 附件文件信息 */
    public void setAppendixFiles(LogReportListResultAppendixFiles appendixFiles) {
        this.appendixFiles = appendixFiles;
    }

    /** 设备日志文件列表 */
    public LogReportListResultDeviceLogs getDeviceLogs() {
        return deviceLogs;
    }

    /** @param deviceLogs 设备日志文件列表 */
    public void setDeviceLogs(LogReportListResultDeviceLogs deviceLogs) {
        this.deviceLogs = deviceLogs;
    }

    /** 设备上传文件列表 */
    public LogReportListResultDeviceUploadFiles getDeviceUploadFiles() {
        return deviceUploadFiles;
    }

    /** @param deviceUploadFiles 设备上传文件列表 */
    public void setDeviceUploadFiles(LogReportListResultDeviceUploadFiles deviceUploadFiles) {
        this.deviceUploadFiles = deviceUploadFiles;
    }

    /** 报告状态 */
    public String getStatus() {
        return status;
    }

    /** @param status 报告状态 */
    public void setStatus(String status) {
        this.status = status;
    }

    /** 上报联系信息 */
    public LogReportListResultContactInformation getReportContactInformation() {
        return reportContactInformation;
    }

    /** 上报联系信息 */
    public void setReportContactInformation(LogReportListResultContactInformation reportContactInformation) {
        this.reportContactInformation = reportContactInformation;
    }

    /** 业务标识(bid) */
    public String getBid() {
        return bid;
    }

    /** 业务标识(bid) */
    public void setBid(String bid) {
        this.bid = bid;
    }

    /** 设备报告反馈 */
    public String getDeviceReportFeedback() {
        return deviceReportFeedback;
    }

    /** @param deviceReportFeedback 设备报告反馈 */
    public void setDeviceReportFeedback(String deviceReportFeedback) {
        this.deviceReportFeedback = deviceReportFeedback;
    }

    /** 阅读状态 */
    public int getReadStatus() {
        return readStatus;
    }

    /** @param readStatus 阅读状态 */
    public void setReadStatus(int readStatus) {
        this.readStatus = readStatus;
    }

    /** 报告 URL */
    public String getUrl() {
        return url;
    }

    /** @param url 报告 URL */
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LogReportListResultItem that = (LogReportListResultItem) o;
        return Objects.equals(reportUuid, that.reportUuid)
                && reportTime == that.reportTime
                && Objects.equals(reportUserId, that.reportUserId)
                && Objects.equals(reportUserOrganizationCallsign, that.reportUserOrganizationCallsign)
                && Objects.equals(deviceTopo, that.deviceTopo)
                && Objects.equals(reportInformation, that.reportInformation)
                && Objects.equals(trackNumber, that.trackNumber)
                && errorStartTime == that.errorStartTime
                && Objects.equals(appendixFiles, that.appendixFiles)
                && Objects.equals(deviceLogs, that.deviceLogs)
                && Objects.equals(deviceUploadFiles, that.deviceUploadFiles)
                && Objects.equals(status, that.status)
                && Objects.equals(reportContactInformation, that.reportContactInformation)
                && Objects.equals(bid, that.bid)
                && Objects.equals(deviceReportFeedback, that.deviceReportFeedback)
                && readStatus == that.readStatus
                && Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                reportUuid,
                reportTime,
                reportUserId,
                reportUserOrganizationCallsign,
                deviceTopo,
                reportInformation,
                trackNumber,
                errorStartTime,
                appendixFiles,
                deviceLogs,
                deviceUploadFiles,
                status,
                reportContactInformation,
                bid,
                deviceReportFeedback,
                readStatus,
                url);
    }

    @Override
    public String toString() {
        return "LogReportListResultItem{"
                + "reportUuid='" + reportUuid + '\''
                + ", reportTime=" + reportTime
                + ", reportUserId='" + reportUserId + '\''
                + ", reportUserOrganizationCallsign='" + reportUserOrganizationCallsign + '\''
                + ", deviceTopo=" + deviceTopo
                + ", reportInformation='" + reportInformation + '\''
                + ", trackNumber='" + trackNumber + '\''
                + ", errorStartTime=" + errorStartTime
                + ", appendixFiles=" + appendixFiles
                + ", deviceLogs=" + deviceLogs
                + ", deviceUploadFiles=" + deviceUploadFiles
                + ", status='" + status + '\''
                + ", reportContactInformation=" + reportContactInformation
                + ", bid='" + bid + '\''
                + ", deviceReportFeedback='" + deviceReportFeedback + '\''
                + ", readStatus=" + readStatus
                + ", url='" + url + '\''
                + '}';
    }
}
