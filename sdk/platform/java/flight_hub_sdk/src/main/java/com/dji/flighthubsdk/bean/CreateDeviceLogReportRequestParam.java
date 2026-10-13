// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;
/** 创建设备日志上报请求参数 */
public class CreateDeviceLogReportRequestParam {
    /** 报告描述信息 */
    private String reportInformation = "";
    /** 错误发生起始时间，Unix 时间戳（秒） */
    private long errorStartTime;
    /** 设备拓扑信息 */
    private DeviceLogReportDeviceTopoParam deviceTopo;
    /** 上报联系信息 */
    private DeviceLogReportContactInformationParam reportContactInformation;
    /** 附件文件列表 */
    private List<String> appendixFiles = new ArrayList<>();
    /** 日志列表 */
    private List<DeviceLogReportLogListItemParam> logList = new ArrayList<>();
    /** 领域列表 */
    private List<DeviceLogReportDomainItemParam> domains = new ArrayList<>();
    /** 是否崩溃 */
    private boolean isCrashed;

    public CreateDeviceLogReportRequestParam() {
    }


    /** 报告描述信息 */
    public String getReportInformation() { return reportInformation; }
    /** @param reportInformation 报告描述信息 */
    public void setReportInformation(String reportInformation) { this.reportInformation = reportInformation != null ? reportInformation : ""; }
    /** 错误发生起始时间，Unix 时间戳（秒） */
    public long getErrorStartTime() { return errorStartTime; }
    /** @param errorStartTime 错误发生起始时间，Unix 时间戳（秒） */
    public void setErrorStartTime(long errorStartTime) { this.errorStartTime = errorStartTime; }
    /** 设备拓扑信息 */
    public DeviceLogReportDeviceTopoParam getDeviceTopo() { return deviceTopo; }
    /** @param deviceTopo 设备拓扑信息 */
    public void setDeviceTopo(DeviceLogReportDeviceTopoParam deviceTopo) { this.deviceTopo = deviceTopo; }
    /** 上报联系信息 */
    public DeviceLogReportContactInformationParam getReportContactInformation() { return reportContactInformation; }
    /** @param reportContactInformation 上报联系信息 */
    public void setReportContactInformation(DeviceLogReportContactInformationParam reportContactInformation) { this.reportContactInformation = reportContactInformation; }
    /** 附件文件列表 */
    public List<String> getAppendixFiles() { return appendixFiles; }
    /** @param appendixFiles 附件文件列表 */
    public void setAppendixFiles(List<String> appendixFiles) { this.appendixFiles = appendixFiles != null ? appendixFiles : new ArrayList<>(); }
    /** 日志列表 */
    public List<DeviceLogReportLogListItemParam> getLogList() { return logList; }
    /** @param logList 日志列表 */
    public void setLogList(List<DeviceLogReportLogListItemParam> logList) { this.logList = logList != null ? logList : new ArrayList<>(); }
    /** 领域列表 */
    public List<DeviceLogReportDomainItemParam> getDomains() { return domains; }
    /** @param domains 领域列表 */
    public void setDomains(List<DeviceLogReportDomainItemParam> domains) { this.domains = domains != null ? domains : new ArrayList<>(); }
    /** 是否为崩溃日志 */
    public boolean isCrashed() { return isCrashed; }
    /** @param crashed 是否为崩溃日志 */
    public void setCrashed(boolean crashed) { isCrashed = crashed; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CreateDeviceLogReportRequestParam that = (CreateDeviceLogReportRequestParam) o;
        return Objects.equals(reportInformation, that.reportInformation)
                && errorStartTime == that.errorStartTime
                && Objects.equals(deviceTopo, that.deviceTopo)
                && Objects.equals(reportContactInformation, that.reportContactInformation)
                && Objects.equals(appendixFiles, that.appendixFiles)
                && Objects.equals(logList, that.logList)
                && Objects.equals(domains, that.domains)
                && isCrashed == that.isCrashed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                reportInformation,
                errorStartTime,
                deviceTopo,
                reportContactInformation,
                appendixFiles,
                logList,
                domains,
                isCrashed);
    }

    @Override
    public String toString() {
        return "CreateDeviceLogReportRequestParam{"
                + "reportInformation='" + reportInformation + '\''
                + ", errorStartTime=" + errorStartTime
                + ", deviceTopo=" + deviceTopo
                + ", reportContactInformation=" + reportContactInformation
                + ", appendixFiles=" + appendixFiles
                + ", logList=" + logList
                + ", domains=" + domains
                + ", isCrashed=" + isCrashed
                + '}';
    }
}
