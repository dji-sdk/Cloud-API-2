// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 日志报告列表结果联系方式信息。 */
public final class LogReportListResultContactInformation {
    /** 邮箱 */
    private String email;
    /** 电话 */
    private String phone;
    public LogReportListResultContactInformation() {
    }

/**
     * 构造 LogReportListResultContactInformation。
     *
     * @param email 邮箱
     * @param phone 电话
     */
    public LogReportListResultContactInformation(String email, String phone) {
        this.email = email != null ? email : "";
        this.phone = phone != null ? phone : "";
    }

    /** 邮箱 */
    public String getEmail() {
        return email;
    }

    /** 邮箱 */
    public void setEmail(String email) {
        this.email = email;
    }

    /** 电话 */
    public String getPhone() {
        return phone;
    }

    /** 电话 */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LogReportListResultContactInformation that = (LogReportListResultContactInformation) o;
        return Objects.equals(email, that.email)
                && Objects.equals(phone, that.phone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, phone);
    }

    @Override
    public String toString() {
        return "LogReportListResultContactInformation{email='" + email + '\'' + ", phone='" + phone + '\'' + '}';
    }
}
