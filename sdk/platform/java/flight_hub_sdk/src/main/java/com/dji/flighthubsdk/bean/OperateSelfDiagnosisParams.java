// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 定损操作（取消/删除）请求参数 */
public class OperateSelfDiagnosisParams {
    /** 定损报告 UUID; */
    private String reportUuid = "";
    /** 操作指令: "cancel" / "delete"; */
    private String reportCmdMethod = "";

    public OperateSelfDiagnosisParams() {
    }


    /** 定损报告 UUID; */
    public String getReportUuid() {
        return reportUuid;
    }

    /** @param reportUuid 定损报告 UUID; */
    public void setReportUuid(String reportUuid) {
        this.reportUuid = reportUuid != null ? reportUuid : "";
    }

    /** 操作指令: "cancel" / "delete"; */
    public String getReportCmdMethod() {
        return reportCmdMethod;
    }

    /** @param reportCmdMethod 操作指令: "cancel" / "delete"; */
    public void setReportCmdMethod(String reportCmdMethod) {
        this.reportCmdMethod = reportCmdMethod != null ? reportCmdMethod : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OperateSelfDiagnosisParams that = (OperateSelfDiagnosisParams) o;
        return Objects.equals(reportUuid, that.reportUuid)
                && Objects.equals(reportCmdMethod, that.reportCmdMethod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportUuid, reportCmdMethod);
    }

    @Override
    public String toString() {
        return "OperateSelfDiagnosisParams{reportUuid='" + reportUuid + '\'' + ", reportCmdMethod='" + reportCmdMethod + '\'' + '}';
    }
}
