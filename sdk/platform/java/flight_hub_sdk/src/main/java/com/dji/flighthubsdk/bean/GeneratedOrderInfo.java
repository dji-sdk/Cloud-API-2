// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 创建报修订单 - 生成的订单信息 */
public final class GeneratedOrderInfo {
    private String orderNumber;
    private int status;
    private String ruiyunOrderNumber;
    public GeneratedOrderInfo() {
    }

/**
     * 构造 GeneratedOrderInfo。
     *
     * @param orderNumber 订单编号
     * @param status 订单状态
     * @param ruiyunOrderNumber 瑞云订单编号（瑞云三方工单号）
     */
    public GeneratedOrderInfo(String orderNumber, int status, String ruiyunOrderNumber) {
        this.orderNumber = orderNumber;
        this.status = status;
        this.ruiyunOrderNumber = ruiyunOrderNumber;
    }

    /** 订单编号 */
    public String getOrderNumber() { return orderNumber; }
    /** 订单状态 */
    public int getStatus() { return status; }
    /** 瑞云订单编号（瑞云三方工单号） */
    public String getRuiyunOrderNumber() { return ruiyunOrderNumber; }

    /** 订单编号 */
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    /** 订单状态 */
    public void setStatus(int status) {
        this.status = status;
    }

    /** 瑞云订单编号（瑞云三方工单号） */
    public void setRuiyunOrderNumber(String ruiyunOrderNumber) {
        this.ruiyunOrderNumber = ruiyunOrderNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GeneratedOrderInfo that = (GeneratedOrderInfo) o;
        return Objects.equals(orderNumber, that.orderNumber)
                && status == that.status
                && Objects.equals(ruiyunOrderNumber, that.ruiyunOrderNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderNumber, status, ruiyunOrderNumber);
    }

    @Override
    public String toString() {
        return "GeneratedOrderInfo{orderNumber='" + orderNumber + '\'' + ", status=" + status + ", ruiyunOrderNumber='" + ruiyunOrderNumber + '\'' + '}';
    }
}
