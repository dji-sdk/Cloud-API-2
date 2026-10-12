// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 定损报告 - 设备上报进度子设备项 */
public final class DiagnosisDeviceProgressItem {
    private String sn;
    private String status;
    private int percent;
    public DiagnosisDeviceProgressItem() {
    }

/**
     * 构造 DiagnosisDeviceProgressItem。
     *
     * @param sn 设备序列号
     * @param status 状态
     * @param percent 百分比
     */
    public DiagnosisDeviceProgressItem(String sn, String status, int percent) {
        this.sn = sn;
        this.status = status;
        this.percent = percent;
    }

    /** 设备序列号 */
    public String getSn() { return sn; }
    /** 状态 */
    public String getStatus() { return status; }
    /** 百分比 */
    public int getPercent() { return percent; }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DiagnosisDeviceProgressItem that = (DiagnosisDeviceProgressItem) o;
        return Objects.equals(sn, that.sn)
                && Objects.equals(status, that.status)
                && percent == that.percent;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sn, status, percent);
    }

    @Override
    public String toString() {
        return "DiagnosisDeviceProgressItem{sn='" + sn + '\'' + ", status='" + status + '\'' + ", percent=" + percent + '}';
    }
}
