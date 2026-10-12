// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 日志报告列表结果附件文件。 */
public final class LogReportListResultAppendixFiles {
    /** files */
    private List<LogReportListResultAppendixFileItem> files;
    public LogReportListResultAppendixFiles() {
    }

/**
     * 构造 LogReportListResultAppendixFiles。
     *
     * @param files files
     */
    public LogReportListResultAppendixFiles(List<LogReportListResultAppendixFileItem> files) {
        this.files = files != null ? files : Collections.emptyList();
    }

    /** files */
    public List<LogReportListResultAppendixFileItem> getFiles() {
        return files;
    }

    /** files */
    public void setFiles(List<LogReportListResultAppendixFileItem> files) {
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
        LogReportListResultAppendixFiles that = (LogReportListResultAppendixFiles) o;
        return Objects.equals(files, that.files);
    }

    @Override
    public int hashCode() {
        return Objects.hash(files);
    }

    @Override
    public String toString() {
        return "LogReportListResultAppendixFiles{files=" + files + '}';
    }
}
