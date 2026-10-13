// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 导出飞行记录结果。 */
public final class ExportFlightRecordsResults {
    private String exportId;
    public ExportFlightRecordsResults() {
    }

/**
     * 构造 ExportFlightRecordsResults。
     *
     * @param exportId 导出任务 ID
     */
    public ExportFlightRecordsResults(String exportId) {
        this.exportId = exportId != null ? exportId : "";
    }

    /** 导出任务 ID */
    public String getExportId() {
        return exportId;
    }

    /** 导出任务 ID */
    public void setExportId(String exportId) {
        this.exportId = exportId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExportFlightRecordsResults that = (ExportFlightRecordsResults) o;
        return Objects.equals(exportId, that.exportId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exportId);
    }

    @Override
    public String toString() {
        return "ExportFlightRecordsResults{exportId='" + exportId + '\'' + '}';
    }
}
