// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** 联通性检测结果（v1.1 §8 GET /cascade/api/connectivity）。 */
public final class CascadeConnectivityResult {
    private List<CascadeConnectivityItem> items;

    public CascadeConnectivityResult() {
        this(Collections.emptyList());
    }

    /**
     * 构造 CascadeConnectivityResult。
     *
     * @param items 每个下级节点的探活结果
     */
    public CascadeConnectivityResult(List<CascadeConnectivityItem> items) {
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
    }

    /** 探活结果列表 */
    public List<CascadeConnectivityItem> getItems() { return items; }
    /** 探活结果列表 */
    public void setItems(List<CascadeConnectivityItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CascadeConnectivityResult that = (CascadeConnectivityResult) o;
        return Objects.equals(items, that.items);
    }

    @Override
    public int hashCode() { return Objects.hash(items); }

    @Override
    public String toString() { return "CascadeConnectivityResult{items=" + items + "}"; }
}
