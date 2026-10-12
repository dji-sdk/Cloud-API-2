// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 创建报修订单返回结果 */
public final class CreateMaintenanceOrderResults {
    /** 是否全部创建成功; */
    private boolean success;
    private List<GeneratedOrderInfo> generatedOrders;
    public CreateMaintenanceOrderResults() {
    }

/**
     * 构造 CreateMaintenanceOrderResults。
     *
     * @param success 是否全部创建成功
     * @param generatedOrders 生成的订单信息列表
     */
    public CreateMaintenanceOrderResults(boolean success, List<GeneratedOrderInfo> generatedOrders) {
        this.success = success;
        this.generatedOrders = generatedOrders != null ? generatedOrders : Collections.emptyList();
    }

    /** 是否全部创建成功; */
    public boolean isSuccess() { return success; }
    /** 生成的订单信息列表 */
    public List<GeneratedOrderInfo> getGeneratedOrders() { return generatedOrders; }

    /** @param success 是否全部创建成功; */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /** 生成的订单信息列表 */
    public void setGeneratedOrders(List<GeneratedOrderInfo> generatedOrders) {
        this.generatedOrders = generatedOrders;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CreateMaintenanceOrderResults that = (CreateMaintenanceOrderResults) o;
        return success == that.success
                && Objects.equals(generatedOrders, that.generatedOrders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, generatedOrders);
    }

    @Override
    public String toString() {
        return "CreateMaintenanceOrderResults{success=" + success + ", generatedOrders=" + generatedOrders + '}';
    }
}
