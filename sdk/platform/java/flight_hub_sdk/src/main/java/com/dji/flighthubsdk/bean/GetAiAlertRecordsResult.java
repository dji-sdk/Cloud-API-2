// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** GET /drc/paas/v2/ai-alert/records 响应，data 按 flightId 分组 */
public class GetAiAlertRecordsResult {
    /** key = flight_id，value = 该架次告警明细列表 */
    private Map<String, List<AiAlertRecordItem>> data = new HashMap<>();
    /** 总条数 */
    private int total;
    /** 当前页码，默认 1 */
    private int page = 1;
    /** 每页数量，默认 50 */
    private int pageSize = 50;
    /** 总页数 */
    private int pageCount;

    public GetAiAlertRecordsResult() {}

    public Map<String, List<AiAlertRecordItem>> getData() { return data; }
    public void setData(Map<String, List<AiAlertRecordItem>> v) { this.data = v != null ? v : new HashMap<>(); }

    public int getTotal() { return total; }
    public void setTotal(int v) { this.total = v; }

    public int getPage() { return page; }
    public void setPage(int v) { this.page = v; }

    public int getPageSize() { return pageSize; }
    public void setPageSize(int v) { this.pageSize = v; }

    public int getPageCount() { return pageCount; }
    public void setPageCount(int v) { this.pageCount = v; }
}
