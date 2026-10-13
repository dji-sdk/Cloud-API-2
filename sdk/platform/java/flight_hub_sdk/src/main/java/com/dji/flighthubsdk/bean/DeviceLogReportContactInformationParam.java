// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 设备日志上报 - 联系信息参数 */
public class DeviceLogReportContactInformationParam {
    /** 邮箱 */
    private String email = "";
    /** 电话 */
    private String phone = "";

    public DeviceLogReportContactInformationParam() {
    }


    /** 邮箱 */
    public String getEmail() { return email; }
    /** @param email 邮箱 */
    public void setEmail(String email) { this.email = email != null ? email : ""; }
    /** 电话 */
    public String getPhone() { return phone; }
    /** @param phone 电话 */
    public void setPhone(String phone) { this.phone = phone != null ? phone : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceLogReportContactInformationParam that = (DeviceLogReportContactInformationParam) o;
        return Objects.equals(email, that.email)
                && Objects.equals(phone, that.phone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, phone);
    }

    @Override
    public String toString() {
        return "DeviceLogReportContactInformationParam{email='" + email + '\'' + ", phone='" + phone + '\'' + '}';
    }
}
