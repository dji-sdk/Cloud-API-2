// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 云控制权限状态。 */
public final class ControlRightsCloudControlStatus {

    private String sn;
    private String reason;
    private List<ControlRightsCloudControlStatusItem> controls;
    public ControlRightsCloudControlStatus() {
    }

/**
     * 构造 ControlRightsCloudControlStatus。
     *
     * @param sn 设备序列号(SN)
     * @param reason 原因
     * @param controls 控制权列表
     */
    public ControlRightsCloudControlStatus(String sn, String reason, List<ControlRightsCloudControlStatusItem> controls) {
        this.sn = sn != null ? sn : "";
        this.reason = reason != null ? reason : "";
        this.controls = controls != null ? controls : Collections.emptyList();
    }

    /** 设备序列号(SN) */
    public String getSn() {
        return sn;
    }

    /** 设备序列号(SN) */
    public void setSn(String sn) {
        this.sn = sn;
    }

    /** 原因 */
    public String getReason() {
        return reason;
    }

    /** 原因 */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /** 控制权列表 */
    public List<ControlRightsCloudControlStatusItem> getControls() {
        return controls;
    }

    /** 控制权列表 */
    public void setControls(List<ControlRightsCloudControlStatusItem> controls) {
        this.controls = controls;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ControlRightsCloudControlStatus that = (ControlRightsCloudControlStatus) o;
        return Objects.equals(sn, that.sn)
                && Objects.equals(reason, that.reason)
                && Objects.equals(controls, that.controls);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sn, reason, controls);
    }

    @Override
    public String toString() {
        return "ControlRightsCloudControlStatus{sn='" + sn + '\'' + ", reason='" + reason + '\'' + ", controls=" + controls + '}';
    }
}
