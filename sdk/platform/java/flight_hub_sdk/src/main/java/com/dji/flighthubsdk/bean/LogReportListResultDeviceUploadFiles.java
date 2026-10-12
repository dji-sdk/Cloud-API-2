// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Collections;
import java.util.List;
import java.util.Objects;
/** 日志报告设备上传文件。 */
public final class LogReportListResultDeviceUploadFiles {
    /** 上传文件列表 */
    private List<LogReportListResultDeviceUploadFilesItem> files;
    public LogReportListResultDeviceUploadFiles() {
    }

    public LogReportListResultDeviceUploadFiles(List<LogReportListResultDeviceUploadFilesItem> files) {
        this.files = files != null ? files : Collections.emptyList();
    }

    /** 上传文件列表 */
    public List<LogReportListResultDeviceUploadFilesItem> getFiles() {
        return files;
    }

    /** @param files 上传文件列表 */
    public void setFiles(List<LogReportListResultDeviceUploadFilesItem> files) {
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
        LogReportListResultDeviceUploadFiles that = (LogReportListResultDeviceUploadFiles) o;
        return Objects.equals(files, that.files);
    }

    @Override
    public int hashCode() {
        return Objects.hash(files);
    }

    @Override
    public String toString() {
        return "LogReportListResultDeviceUploadFiles{files=" + files + '}';
    }
}
