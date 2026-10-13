// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** 获取可操作设备数量统计请求参数 */
public class GetAIInsideModelDevicesSummaryParam {
    /** 算法签名 ID（必填） */
    private String signedId = "";
    /** 操作类型：load（装载）或 update（更新）（必填） */
    private String operationType = "";

    public GetAIInsideModelDevicesSummaryParam() {}

    /** 算法签名 ID（必填） */
    public String getSignedId() { return signedId; }

    /** @param v 算法签名 ID（必填） */
    public void setSignedId(String v) { this.signedId = v != null ? v : ""; }

    /** 操作类型：load（装载）或 update（更新）（必填） */
    public String getOperationType() { return operationType; }

    /** @param v 操作类型：load（装载）或 update（更新）（必填） */
    public void setOperationType(String v) { this.operationType = v != null ? v : ""; }
}
