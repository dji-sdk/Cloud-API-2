// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 报修订单详情 - 联系地址 */
public final class MaintenanceOrderContactAddress {
    private String country;
    private String province;
    private String city;
    private String district;
    private String detail;
    public MaintenanceOrderContactAddress() {
    }

/**
     * 构造 MaintenanceOrderContactAddress。
     *
     * @param country 国家
     * @param province 省份
     * @param city 城市
     * @param district 区县
     * @param detail 详细地址
     */
    public MaintenanceOrderContactAddress(String country, String province, String city, String district, String detail) {
        this.country = country;
        this.province = province;
        this.city = city;
        this.district = district;
        this.detail = detail;
    }

    /** 国家 */
    public String getCountry() { return country; }
    /** 省份 */
    public String getProvince() { return province; }
    /** 城市 */
    public String getCity() { return city; }
    /** 区县 */
    public String getDistrict() { return district; }
    /** 详细地址 */
    public String getDetail() { return detail; }

    /** 国家 */
    public void setCountry(String country) {
        this.country = country;
    }

    /** 省份 */
    public void setProvince(String province) {
        this.province = province;
    }

    /** 城市 */
    public void setCity(String city) {
        this.city = city;
    }

    /** 区县 */
    public void setDistrict(String district) {
        this.district = district;
    }

    /** 详细地址 */
    public void setDetail(String detail) {
        this.detail = detail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MaintenanceOrderContactAddress that = (MaintenanceOrderContactAddress) o;
        return Objects.equals(country, that.country)
                && Objects.equals(province, that.province)
                && Objects.equals(city, that.city)
                && Objects.equals(district, that.district)
                && Objects.equals(detail, that.detail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                country,
                province,
                city,
                district,
                detail);
    }

    @Override
    public String toString() {
        return "MaintenanceOrderContactAddress{"
                + "country='" + country + '\''
                + ", province='" + province + '\''
                + ", city='" + city + '\''
                + ", district='" + district + '\''
                + ", detail='" + detail + '\''
                + '}';
    }
}
