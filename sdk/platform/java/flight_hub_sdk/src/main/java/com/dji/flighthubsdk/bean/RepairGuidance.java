// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 不支持报修时返回的维修指导，support_repair=true 时为默认值 */
public final class RepairGuidance {
    /** 维修指导跳转链接，型号不支持时指向官网维修页; */
    private String redirectUrl;
    /** 描述码：devices_repair_guidance_prefix(型号不支持), devices_repair_detail_repair_order_unfinished(有未完成订单); */
    private String description;
    public RepairGuidance() {
    }

/**
     * 构造 RepairGuidance。
     *
     * @param redirectUrl 维修指导跳转链接，型号不支持时指向官网维修页
     * @param description 描述码：devices_repair_guidance_prefix(型号不支持), devices_repair_detail_repair_order_unfinished(有未完成订单)
     */
    public RepairGuidance(String redirectUrl, String description) {
        this.redirectUrl = redirectUrl;
        this.description = description;
    }

    /** 维修指导跳转链接，型号不支持时指向官网维修页; */
    public String getRedirectUrl() { return redirectUrl; }
    /** 描述码：devices_repair_guidance_prefix(型号不支持), devices_repair_detail_repair_order_unfinished(有未完成订单); */
    public String getDescription() { return description; }

    /** @param redirectUrl 维修指导跳转链接，型号不支持时指向官网维修页; */
    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    /** @param description 描述码：devices_repair_guidance_prefix(型号不支持), devices_repair_detail_repair_order_unfinished(有未完成订单); */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RepairGuidance that = (RepairGuidance) o;
        return Objects.equals(redirectUrl, that.redirectUrl)
                && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(redirectUrl, description);
    }

    @Override
    public String toString() {
        return "RepairGuidance{redirectUrl='" + redirectUrl + '\'' + ", description='" + description + '\'' + '}';
    }
}
