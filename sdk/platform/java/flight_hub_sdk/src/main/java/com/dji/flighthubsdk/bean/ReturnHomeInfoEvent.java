// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 多机场返航路径信息事件。 */
public final class ReturnHomeInfoEvent {
    private String flightId;
    private String sn;
    private ReturnHomeInfo info;

    public ReturnHomeInfoEvent() {
    }

    /**
     * 构造 ReturnHomeInfoEvent。
     *
     * @param flightId 航班 ID
     * @param sn 设备序列号
     * @param info 返航信息
     */
    public ReturnHomeInfoEvent(String flightId, String sn, ReturnHomeInfo info) {
        this.flightId = flightId != null ? flightId : "";
        this.sn = sn != null ? sn : "";
        this.info = info;
    }

    /** 航班 ID */
    public String getFlightId() { return flightId; }
    /** 设备序列号 */
    public String getSn() { return sn; }
    /** 返航信息 */
    public ReturnHomeInfo getInfo() { return info; }

    /** 航班 ID */
    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

    /** 设备序列号 */
    public void setSn(String sn) {
        this.sn = sn;
    }

    /** 返航信息 */
    public void setInfo(ReturnHomeInfo info) {
        this.info = info;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReturnHomeInfoEvent that = (ReturnHomeInfoEvent) o;
        return Objects.equals(flightId, that.flightId)
                && Objects.equals(sn, that.sn)
                && Objects.equals(info, that.info);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flightId, sn, info);
    }

    @Override
    public String toString() {
        return "ReturnHomeInfoEvent{"
                + "flightId='" + flightId + '\''
                + ", sn='" + sn + '\''
                + ", info=" + info
                + '}';
    }
}
