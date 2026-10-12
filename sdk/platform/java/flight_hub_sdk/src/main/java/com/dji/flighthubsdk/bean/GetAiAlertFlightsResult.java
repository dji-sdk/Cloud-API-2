// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

/** GET /drc/paas/v2/ai-alert/flights 响应 */
public class GetAiAlertFlightsResult {
    /** 架次统计列表 */
    private List<AiAlertFlightItem> data = new ArrayList<>();
    /** 总条数 */
    private int total;
    /** 当前页码，默认 1 */
    private int page = 1;
    /** 每页数量，默认 50 */
    private int pageSize = 50;
    /** 总页数 */
    private int pageCount;

    public GetAiAlertFlightsResult() {}

    public List<AiAlertFlightItem> getData() { return data; }
    public void setData(List<AiAlertFlightItem> v) { this.data = v != null ? v : new ArrayList<>(); }

    public int getTotal() { return total; }
    public void setTotal(int v) { this.total = v; }

    public int getPage() { return page; }
    public void setPage(int v) { this.page = v; }

    public int getPageSize() { return pageSize; }
    public void setPageSize(int v) { this.pageSize = v; }

    public int getPageCount() { return pageCount; }
    public void setPageCount(int v) { this.pageCount = v; }
}
