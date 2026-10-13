// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Collections;
import java.util.List;
import java.util.Objects;
/** 日志报告设备日志。 */
public final class LogReportListResultDeviceLogs {
    /** 设备日志文件列表 */
    private List<LogReportListResultDeviceLog> files;
    public LogReportListResultDeviceLogs() {
    }

    public LogReportListResultDeviceLogs(List<LogReportListResultDeviceLog> files) {
        this.files = files != null ? files : Collections.emptyList();
    }

    /** 设备日志文件列表 */
    public List<LogReportListResultDeviceLog> getFiles() {
        return files;
    }

    /** @param files 设备日志文件列表 */
    public void setFiles(List<LogReportListResultDeviceLog> files) {
        this.files = files;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LogReportListResultDeviceLogs that = (LogReportListResultDeviceLogs) o;
        return Objects.equals(files, that.files);
    }

    @Override
    public int hashCode() {
        return Objects.hash(files);
    }

    @Override
    public String toString() {
        return "LogReportListResultDeviceLogs{files=" + files + '}';
    }
}
