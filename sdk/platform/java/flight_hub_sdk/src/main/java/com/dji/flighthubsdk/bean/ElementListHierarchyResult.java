// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** 查询元素层级列表返回结果 */
public final class ElementListHierarchyResult {

    private List<ElementGroupOutput> list;

    public ElementListHierarchyResult() {
        this.list = Collections.emptyList();
    }

    /**
     * 构造 ElementListHierarchyResult。
     *
     * @param list 图层树形列表
     */
    public ElementListHierarchyResult(List<ElementGroupOutput> list) {
        this.list = list != null ? list : Collections.emptyList();
    }

    /** 图层树形列表 */
    public List<ElementGroupOutput> getList() { return list; }

    /** 图层树形列表 */
    public void setList(List<ElementGroupOutput> list) {
        this.list = list != null ? list : Collections.emptyList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ElementListHierarchyResult that = (ElementListHierarchyResult) o;
        return Objects.equals(list, that.list);
    }

    @Override
    public int hashCode() {
        return Objects.hash(list);
    }

    @Override
    public String toString() {
        return "ElementListHierarchyResult{"
                + "list.size=" + (list != null ? list.size() : 0)
                + '}';
    }
}
