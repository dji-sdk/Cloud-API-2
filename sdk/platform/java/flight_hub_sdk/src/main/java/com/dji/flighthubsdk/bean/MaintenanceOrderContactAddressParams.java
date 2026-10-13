// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 创建报修订单 - 联系地址参数 */
public class MaintenanceOrderContactAddressParams {
    private String country = "";
    private String province = "";
    private String city = "";
    private String district = "";
    private String detail = "";

    public MaintenanceOrderContactAddressParams() {
    }


    /** 国家 */
    public String getCountry() {
        return country;
    }

    /** @param country 国家 */
    public void setCountry(String country) {
        this.country = country != null ? country : "";
    }

    /** 省份 */
    public String getProvince() {
        return province;
    }

    /** @param province 省份 */
    public void setProvince(String province) {
        this.province = province != null ? province : "";
    }

    /** 城市 */
    public String getCity() {
        return city;
    }

    /** @param city 城市 */
    public void setCity(String city) {
        this.city = city != null ? city : "";
    }

    /** 区县 */
    public String getDistrict() {
        return district;
    }

    /** @param district 区县 */
    public void setDistrict(String district) {
        this.district = district != null ? district : "";
    }

    /** 详细地址 */
    public String getDetail() {
        return detail;
    }

    /** @param detail 详细地址 */
    public void setDetail(String detail) {
        this.detail = detail != null ? detail : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MaintenanceOrderContactAddressParams that = (MaintenanceOrderContactAddressParams) o;
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
        return "MaintenanceOrderContactAddressParams{"
                + "country='" + country + '\''
                + ", province='" + province + '\''
                + ", city='" + city + '\''
                + ", district='" + district + '\''
                + ", detail='" + detail + '\''
                + '}';
    }
}
