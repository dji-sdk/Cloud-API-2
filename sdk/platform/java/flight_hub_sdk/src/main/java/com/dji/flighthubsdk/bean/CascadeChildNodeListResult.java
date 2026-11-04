// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** 分页子节点列表结果。 */
public final class CascadeChildNodeListResult {
    private List<CascadeNodeInfo> list;
    private CascadePage page;

    public CascadeChildNodeListResult() {
        this(Collections.emptyList(), new CascadePage());
    }

    /**
     * 构造 CascadeChildNodeListResult。
     *
     * @param list 节点列表
     * @param page 分页元数据
     */
    public CascadeChildNodeListResult(List<CascadeNodeInfo> list, CascadePage page) {
        this.list = list != null ? list : Collections.emptyList();
        this.page = page != null ? page : new CascadePage();
    }

    /** 节点列表 */
    public List<CascadeNodeInfo> getList() { return list; }
    /** 节点列表 */
    public void setList(List<CascadeNodeInfo> list) {
        this.list = list != null ? list : Collections.emptyList();
    }

    /** 分页元数据 */
    public CascadePage getPage() { return page; }
    /** 分页元数据 */
    public void setPage(CascadePage page) {
        this.page = page != null ? page : new CascadePage();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CascadeChildNodeListResult that = (CascadeChildNodeListResult) o;
        return Objects.equals(list, that.list) && Objects.equals(page, that.page);
    }

    @Override
    public int hashCode() { return Objects.hash(list, page); }

    @Override
    public String toString() {
        return "CascadeChildNodeListResult{list.size=" + list.size() + ", page=" + page + "}";
    }
}
