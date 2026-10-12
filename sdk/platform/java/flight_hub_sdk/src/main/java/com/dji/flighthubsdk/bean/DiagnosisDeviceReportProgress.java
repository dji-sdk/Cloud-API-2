// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 定损报告 - 设备上报进度 */
public final class DiagnosisDeviceReportProgress {
    private int result;
    private String sn;
    private String status;
    private int percent;
    private List<DiagnosisDeviceProgressItem> allDevice;
    public DiagnosisDeviceReportProgress() {
    }

/**
     * 构造 DiagnosisDeviceReportProgress。
     *
     * @param result 结果
     * @param sn 设备序列号
     * @param status 状态
     * @param percent 百分比
     * @param allDevice 所有设备
     */
    public DiagnosisDeviceReportProgress(int result, String sn, String status, int percent, List<DiagnosisDeviceProgressItem> allDevice) {
        this.result = result;
        this.sn = sn;
        this.status = status;
        this.percent = percent;
        this.allDevice = allDevice != null ? allDevice : Collections.emptyList();
    }

    /** 结果 */
    public int getResult() { return result; }
    /** 设备序列号 */
    public String getSn() { return sn; }
    /** 状态 */
    public String getStatus() { return status; }
    /** 百分比 */
    public int getPercent() { return percent; }
    /** 所有设备 */
    public List<DiagnosisDeviceProgressItem> getAllDevice() { return allDevice; }

    /** 结果 */
    public void setResult(int result) {
        this.result = result;
    }

    /** 设备序列号 */
    public void setSn(String sn) {
        this.sn = sn;
    }

    /** 状态 */
    public void setStatus(String status) {
        this.status = status;
    }

    /** 百分比 */
    public void setPercent(int percent) {
        this.percent = percent;
    }

    /** 所有设备 */
    public void setAllDevice(List<DiagnosisDeviceProgressItem> allDevice) {
        this.allDevice = allDevice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DiagnosisDeviceReportProgress that = (DiagnosisDeviceReportProgress) o;
        return result == that.result
                && Objects.equals(sn, that.sn)
                && Objects.equals(status, that.status)
                && percent == that.percent
                && Objects.equals(allDevice, that.allDevice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                result,
                sn,
                status,
                percent,
                allDevice);
    }

    @Override
    public String toString() {
        return "DiagnosisDeviceReportProgress{"
                + "result=" + result
                + ", sn='" + sn + '\''
                + ", status='" + status + '\''
                + ", percent=" + percent
                + ", allDevice=" + allDevice
                + '}';
    }
}
