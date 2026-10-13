// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 设备解锁 - 国家解锁（国家编号、高度） */
public final class DeviceUnlockCountryUnlock {

    private int countryNumber;
    private double height;
    public DeviceUnlockCountryUnlock() {
    }

/**
     * 构造 DeviceUnlockCountryUnlock。
     *
     * @param countryNumber 国家编号
     * @param height 高度
     */
    public DeviceUnlockCountryUnlock(int countryNumber, double height) {
        this.countryNumber = countryNumber;
        this.height = height;
    }

    /** 国家编号 */
    public int getCountryNumber() {
        return countryNumber;
    }

    /** 国家编号 */
    public void setCountryNumber(int countryNumber) {
        this.countryNumber = countryNumber;
    }

    /** 高度 */
    public double getHeight() {
        return height;
    }

    /** 高度 */
    public void setHeight(double height) {
        this.height = height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceUnlockCountryUnlock that = (DeviceUnlockCountryUnlock) o;
        return countryNumber == that.countryNumber
                && Double.compare(that.height, height) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(countryNumber, height);
    }

    @Override
    public String toString() {
        return "DeviceUnlockCountryUnlock{countryNumber=" + countryNumber + ", height=" + height + '}';
    }
}
