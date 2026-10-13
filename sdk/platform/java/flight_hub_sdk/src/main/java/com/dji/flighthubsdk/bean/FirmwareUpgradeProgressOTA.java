// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 固件升级 OTA 状态 */
public final class FirmwareUpgradeProgressOTA {

    private String status;
    private FirmwareUpgradeProgressStep progress;
    public FirmwareUpgradeProgressOTA() {
    }

/**
     * 构造 FirmwareUpgradeProgressOTA。
     *
     * @param status 状态
     * @param progress 进度
     */
    public FirmwareUpgradeProgressOTA(String status, FirmwareUpgradeProgressStep progress) {
        this.status = status != null ? status : "";
        this.progress = progress;
    }

    /** 状态 */
    public String getStatus() {
        return status;
    }

    /** 状态 */
    public void setStatus(String status) {
        this.status = status;
    }

    /** 进度 */
    public FirmwareUpgradeProgressStep getProgress() {
        return progress;
    }

    /** 进度 */
    public void setProgress(FirmwareUpgradeProgressStep progress) {
        this.progress = progress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FirmwareUpgradeProgressOTA that = (FirmwareUpgradeProgressOTA) o;
        return Objects.equals(status, that.status)
                && Objects.equals(progress, that.progress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, progress);
    }

    @Override
    public String toString() {
        return "FirmwareUpgradeProgressOTA{status='" + status + '\'' + ", progress=" + progress + '}';
    }
}
