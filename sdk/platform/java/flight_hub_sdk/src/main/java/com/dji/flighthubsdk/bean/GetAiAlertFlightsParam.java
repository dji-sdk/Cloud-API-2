// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

/** GET /drc/paas/v2/ai-alert/flights 请求参数 */
public class GetAiAlertFlightsParam {
    /** 无人机序列号（必填） */
    private String droneSn = "";
    /** 开始时间（毫秒时间戳，0 表示不传） */
    private long beginAt;
    /** 结束时间（毫秒时间戳，0 表示不传） */
    private long endAt;
    /** 算法来源过滤 */
    private List<Integer> algorithmSources = new ArrayList<>();
    /** 页码，默认 1 */
    private int page = 1;
    /** 每页数量，默认 50 */
    private int pageSize = 50;

    public GetAiAlertFlightsParam() {}

    public String getDroneSn() { return droneSn; }
    public void setDroneSn(String v) { this.droneSn = v != null ? v : ""; }

    public long getBeginAt() { return beginAt; }
    public void setBeginAt(long v) { this.beginAt = v; }

    public long getEndAt() { return endAt; }
    public void setEndAt(long v) { this.endAt = v; }

    public List<Integer> getAlgorithmSources() { return algorithmSources; }
    public void setAlgorithmSources(List<Integer> v) { this.algorithmSources = v != null ? v : new ArrayList<>(); }

    public int getPage() { return page; }
    public void setPage(int v) { this.page = v; }

    public int getPageSize() { return pageSize; }
    public void setPageSize(int v) { this.pageSize = v; }
}
