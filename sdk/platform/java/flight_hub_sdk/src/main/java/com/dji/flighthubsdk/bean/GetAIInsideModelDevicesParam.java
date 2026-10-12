// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

/** 获取算法绑定设备详情请求参数 */
public class GetAIInsideModelDevicesParam {
    /** 算法签名 ID（必填） */
    private String signedId = "";
    /** 页码，从 1 开始（必填） */
    private int page = 1;
    /** 每页条数（必填） */
    private int pageSize = 20;
    /** 指定查询的飞机 SN 列表（可选，为空则查询全部绑定设备） */
    private List<String> droneSn = new ArrayList<>();

    public GetAIInsideModelDevicesParam() {}

    /** 算法签名 ID（必填） */
    public String getSignedId() { return signedId; }

    /** @param v 算法签名 ID（必填） */
    public void setSignedId(String v) { this.signedId = v != null ? v : ""; }

    /** 页码，从 1 开始（必填） */
    public int getPage() { return page; }

    /** @param v 页码，从 1 开始（必填） */
    public void setPage(int v) { this.page = v; }

    /** 每页条数（必填） */
    public int getPageSize() { return pageSize; }

    /** @param v 每页条数（必填） */
    public void setPageSize(int v) { this.pageSize = v; }

    /** 指定查询的飞机 SN 列表（可选，为空则查询全部绑定设备） */
    public List<String> getDroneSn() { return droneSn; }

    /** @param v 指定查询的飞机 SN 列表（可选，为空则查询全部绑定设备） */
    public void setDroneSn(List<String> v) { this.droneSn = v != null ? v : new ArrayList<>(); }
}
