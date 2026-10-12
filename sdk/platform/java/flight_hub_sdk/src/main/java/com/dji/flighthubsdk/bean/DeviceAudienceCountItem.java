// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 获取在线观众数量 - 响应项（设备 SN + 观众数） */
public final class DeviceAudienceCountItem {
    private String sn;
    private int count;
    public DeviceAudienceCountItem() {
    }

/**
     * 构造 DeviceAudienceCountItem。
     *
     * @param sn 设备序列号
     * @param count 数量
     */
    public DeviceAudienceCountItem(String sn, int count) {
        this.sn = sn != null ? sn : "";
        this.count = count;
    }

    /** 设备序列号 */
    public String getSn() {
        return sn;
    }

    /** 设备序列号 */
    public void setSn(String sn) {
        this.sn = sn;
    }

    /** 数量 */
    public int getCount() {
        return count;
    }

    /** 数量 */
    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceAudienceCountItem that = (DeviceAudienceCountItem) o;
        return count == that.count && Objects.equals(sn, that.sn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sn, count);
    }

    @Override
    public String toString() {
        return "DeviceAudienceCountItem{sn='" + sn + '\'' + ", count=" + count + '}';
    }
}
