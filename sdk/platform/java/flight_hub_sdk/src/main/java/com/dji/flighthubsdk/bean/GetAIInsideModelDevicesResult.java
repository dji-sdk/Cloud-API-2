// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

/** 获取算法绑定设备详情结果（分页） */
public class GetAIInsideModelDevicesResult {
    /** 当前页设备列表 */
    private List<AIInsideDeviceItem> list = new ArrayList<>();
    /** 总设备数 */
    private int total;
    /** 当前页码 */
    private int page;
    /** 每页条数 */
    private int pageSize;

    public GetAIInsideModelDevicesResult() {}

    /** 当前页设备列表 */
    public List<AIInsideDeviceItem> getList() { return list; }

    /** @param v 当前页设备列表 */
    public void setList(List<AIInsideDeviceItem> v) { this.list = v != null ? v : new ArrayList<>(); }

    /** 总设备数 */
    public int getTotal() { return total; }

    /** @param v 总设备数 */
    public void setTotal(int v) { this.total = v; }

    /** 当前页码 */
    public int getPage() { return page; }

    /** @param v 当前页码 */
    public void setPage(int v) { this.page = v; }

    /** 每页条数 */
    public int getPageSize() { return pageSize; }

    /** @param v 每页条数 */
    public void setPageSize(int v) { this.pageSize = v; }
}
