// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

/** 分页查询转码器列表结果 */
public class LiveSyncListResult {
    /** 当前页码 */
    private int page = 1;
    /** 每页数量 */
    private int size = 20;
    /** 总条数 */
    private long total = 0;
    /** 转码器列表 */
    private List<LiveSyncItem> list = new ArrayList<>();

    public LiveSyncListResult() {}

    public int getPage() { return page; }
    public void setPage(int v) { this.page = v; }

    public int getSize() { return size; }
    public void setSize(int v) { this.size = v; }

    public long getTotal() { return total; }
    public void setTotal(long v) { this.total = v; }

    public List<LiveSyncItem> getList() { return list; }
    public void setList(List<LiveSyncItem> v) { this.list = v != null ? v : new ArrayList<>(); }
}
