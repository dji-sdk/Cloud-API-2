// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

/** 批量下发算法操作指令请求参数 */
public class OperateAIInsideModelParam {
    /** 算法签名 ID（必填） */
    private String signedId = "";
    /** 操作类型：load（装载）/ update（更新）/ unload（卸载）（必填） */
    private String operationType = "";
    /** 指定操作的设备 SN 列表（可选，为空则对全部可操作设备下发） */
    private List<String> droneSNList = new ArrayList<>();

    public OperateAIInsideModelParam() {}

    /** 算法签名 ID（必填） */
    public String getSignedId() { return signedId; }

    /** @param v 算法签名 ID（必填） */
    public void setSignedId(String v) { this.signedId = v != null ? v : ""; }

    /** 操作类型：load（装载）/ update（更新）/ unload（卸载）（必填） */
    public String getOperationType() { return operationType; }

    /** @param v 操作类型：load（装载）/ update（更新）/ unload（卸载）（必填） */
    public void setOperationType(String v) { this.operationType = v != null ? v : ""; }

    /** 指定操作的设备 SN 列表（可选，为空则对全部可操作设备下发） */
    public List<String> getDroneSNList() { return droneSNList; }

    /** @param v 指定操作的设备 SN 列表（可选，为空则对全部可操作设备下发） */
    public void setDroneSNList(List<String> v) { this.droneSNList = v != null ? v : new ArrayList<>(); }
}
