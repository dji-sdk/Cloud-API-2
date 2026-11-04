// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** TCAS 起降区域状态 */
public final class TCAZoneStatus {
    private int status;
    private String sn = "";
    private int radius;

    public TCAZoneStatus() {
    }

    public TCAZoneStatus(int status, String sn, int radius) {
        this.status = status;
        this.sn = sn != null ? sn : "";
        this.radius = radius;
    }

    /** 区域状态 */
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    /** 设备序列号 */
    public String getSn() { return sn; }
    public void setSn(String sn) { this.sn = sn != null ? sn : ""; }

    /** 半径 */
    public int getRadius() { return radius; }
    public void setRadius(int radius) { this.radius = radius; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TCAZoneStatus that = (TCAZoneStatus) o;
        return status == that.status && radius == that.radius && Objects.equals(sn, that.sn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, sn, radius);
    }

    @Override
    public String toString() {
        return "TCAZoneStatus{status=" + status + ", sn='" + sn + "', radius=" + radius + '}';
    }
}
