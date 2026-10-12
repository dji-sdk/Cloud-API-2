// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 设备解锁证书项，包含公共字段及各类型解锁详情（区域/圆形/国家/高度/多边形/功率/RID） */
public final class DeviceUnlockLicenseItem {

    private DeviceUnlockLicenseCommonFields commonFields;
    private DeviceUnlockAreaUnlock areaUnlock;
    private DeviceUnlockCircleUnlock circleUnlock;
    private DeviceUnlockCountryUnlock countryUnlock;
    private DeviceUnlockHeightUnlock heightUnlock;
    private DeviceUnlockPolygonUnlock polygonUnlock;
    private DeviceUnlockPowerUnlock powerUnlock;
    private DeviceUnlockRidUnlock ridUnlock;
    public DeviceUnlockLicenseItem() {
    }

/**
     * 构造 DeviceUnlockLicenseItem。
     *
     * @param commonFields 公共字段信息
     * @param areaUnlock 区域解锁详情
     * @param circleUnlock 圆形解锁详情
     * @param countryUnlock 国家解锁详情
     * @param heightUnlock 高度解锁详情
     * @param polygonUnlock 多边形解锁详情
     * @param powerUnlock 功率解锁详情
     * @param ridUnlock RID 解锁详情
     */
    public DeviceUnlockLicenseItem(
            DeviceUnlockLicenseCommonFields commonFields,
            DeviceUnlockAreaUnlock areaUnlock,
            DeviceUnlockCircleUnlock circleUnlock,
            DeviceUnlockCountryUnlock countryUnlock,
            DeviceUnlockHeightUnlock heightUnlock,
            DeviceUnlockPolygonUnlock polygonUnlock,
            DeviceUnlockPowerUnlock powerUnlock,
            DeviceUnlockRidUnlock ridUnlock) {
        this.commonFields = commonFields;
        this.areaUnlock = areaUnlock;
        this.circleUnlock = circleUnlock;
        this.countryUnlock = countryUnlock;
        this.heightUnlock = heightUnlock;
        this.polygonUnlock = polygonUnlock;
        this.powerUnlock = powerUnlock;
        this.ridUnlock = ridUnlock;
    }

    /** 公共字段信息 */
    public DeviceUnlockLicenseCommonFields getCommonFields() {
        return commonFields;
    }

    /** 公共字段信息 */
    public void setCommonFields(DeviceUnlockLicenseCommonFields commonFields) {
        this.commonFields = commonFields;
    }

    /** 区域解锁详情 */
    public DeviceUnlockAreaUnlock getAreaUnlock() {
        return areaUnlock;
    }

    /** 区域解锁详情 */
    public void setAreaUnlock(DeviceUnlockAreaUnlock areaUnlock) {
        this.areaUnlock = areaUnlock;
    }

    /** 圆形解锁详情 */
    public DeviceUnlockCircleUnlock getCircleUnlock() {
        return circleUnlock;
    }

    /** 圆形解锁详情 */
    public void setCircleUnlock(DeviceUnlockCircleUnlock circleUnlock) {
        this.circleUnlock = circleUnlock;
    }

    /** 国家解锁详情 */
    public DeviceUnlockCountryUnlock getCountryUnlock() {
        return countryUnlock;
    }

    /** 国家解锁详情 */
    public void setCountryUnlock(DeviceUnlockCountryUnlock countryUnlock) {
        this.countryUnlock = countryUnlock;
    }

    /** 高度解锁详情 */
    public DeviceUnlockHeightUnlock getHeightUnlock() {
        return heightUnlock;
    }

    /** 高度解锁详情 */
    public void setHeightUnlock(DeviceUnlockHeightUnlock heightUnlock) {
        this.heightUnlock = heightUnlock;
    }

    /** 多边形解锁详情 */
    public DeviceUnlockPolygonUnlock getPolygonUnlock() {
        return polygonUnlock;
    }

    /** 多边形解锁详情 */
    public void setPolygonUnlock(DeviceUnlockPolygonUnlock polygonUnlock) {
        this.polygonUnlock = polygonUnlock;
    }

    /** 功率解锁详情 */
    public DeviceUnlockPowerUnlock getPowerUnlock() {
        return powerUnlock;
    }

    /** 功率解锁详情 */
    public void setPowerUnlock(DeviceUnlockPowerUnlock powerUnlock) {
        this.powerUnlock = powerUnlock;
    }

    /** RID 解锁详情 */
    public DeviceUnlockRidUnlock getRidUnlock() {
        return ridUnlock;
    }

    /** RID 解锁详情 */
    public void setRidUnlock(DeviceUnlockRidUnlock ridUnlock) {
        this.ridUnlock = ridUnlock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceUnlockLicenseItem that = (DeviceUnlockLicenseItem) o;
        return Objects.equals(commonFields, that.commonFields)
                && Objects.equals(areaUnlock, that.areaUnlock)
                && Objects.equals(circleUnlock, that.circleUnlock)
                && Objects.equals(countryUnlock, that.countryUnlock)
                && Objects.equals(heightUnlock, that.heightUnlock)
                && Objects.equals(polygonUnlock, that.polygonUnlock)
                && Objects.equals(powerUnlock, that.powerUnlock)
                && Objects.equals(ridUnlock, that.ridUnlock);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                commonFields,
                areaUnlock,
                circleUnlock,
                countryUnlock,
                heightUnlock,
                polygonUnlock,
                powerUnlock,
                ridUnlock);
    }

    @Override
    public String toString() {
        return "DeviceUnlockLicenseItem{"
                + "commonFields=" + commonFields
                + ", areaUnlock=" + areaUnlock
                + ", circleUnlock=" + circleUnlock
                + ", countryUnlock=" + countryUnlock
                + ", heightUnlock=" + heightUnlock
                + ", polygonUnlock=" + polygonUnlock
                + ", powerUnlock=" + powerUnlock
                + ", ridUnlock=" + ridUnlock
                + '}';
    }
}
