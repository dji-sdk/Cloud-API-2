// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

/** GET /drc/paas/v2/ai-alert/records 请求参数 */
public class GetAiAlertRecordsParam {
    /** 架次 ID 列表（来自 getAiAlertFlights，必填） */
    private List<String> flightId = new ArrayList<>();
    /** 无人机 SN 过滤 */
    private List<String> droneSn = new ArrayList<>();
    /** 目标类型过滤：0=人 / 1=车 / 2=船 */
    private List<Integer> targetType = new ArrayList<>();
    /** 算法来源过滤 */
    private List<Integer> algorithmSources = new ArrayList<>();
    /** 开始时间（毫秒时间戳，0 表示不传） */
    private long beginAt;
    /** 结束时间（毫秒时间戳，0 表示不传） */
    private long endAt;
    /** 页码，默认 1 */
    private int page = 1;
    /** 每页数量，默认 50 */
    private int pageSize = 50;

    public GetAiAlertRecordsParam() {}

    public List<String> getFlightId() { return flightId; }
    public void setFlightId(List<String> v) { this.flightId = v != null ? v : new ArrayList<>(); }

    public List<String> getDroneSn() { return droneSn; }
    public void setDroneSn(List<String> v) { this.droneSn = v != null ? v : new ArrayList<>(); }

    public List<Integer> getTargetType() { return targetType; }
    public void setTargetType(List<Integer> v) { this.targetType = v != null ? v : new ArrayList<>(); }

    public List<Integer> getAlgorithmSources() { return algorithmSources; }
    public void setAlgorithmSources(List<Integer> v) { this.algorithmSources = v != null ? v : new ArrayList<>(); }

    public long getBeginAt() { return beginAt; }
    public void setBeginAt(long v) { this.beginAt = v; }

    public long getEndAt() { return endAt; }
    public void setEndAt(long v) { this.endAt = v; }

    public int getPage() { return page; }
    public void setPage(int v) { this.page = v; }

    public int getPageSize() { return pageSize; }
    public void setPageSize(int v) { this.pageSize = v; }
}
