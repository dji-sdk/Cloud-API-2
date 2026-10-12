// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 机场首次上电时间。 */
public final class DockFirstPowerOnTime {

    /** string, 首次上电时间(ms); */
    private String firstPowerOn;
    public DockFirstPowerOnTime() {
    }

/**
     * 构造 DockFirstPowerOnTime。
     *
     * @param firstPowerOn 首次开机时间
     */
    public DockFirstPowerOnTime(String firstPowerOn) {
        this.firstPowerOn = firstPowerOn != null ? firstPowerOn : "";
    }

    /** string, 首次上电时间(ms); */
    public String getFirstPowerOn() {
        return firstPowerOn;
    }

    /** @param firstPowerOn string, 首次上电时间(ms); */
    public void setFirstPowerOn(String firstPowerOn) {
        this.firstPowerOn = firstPowerOn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockFirstPowerOnTime that = (DockFirstPowerOnTime) o;
        return Objects.equals(firstPowerOn, that.firstPowerOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstPowerOn);
    }

    @Override
    public String toString() {
        return "DockFirstPowerOnTime{firstPowerOn='" + firstPowerOn + '\'' + '}';
    }
}
