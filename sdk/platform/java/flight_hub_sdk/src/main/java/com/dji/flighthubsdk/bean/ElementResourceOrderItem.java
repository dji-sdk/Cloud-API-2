// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 要素排序项（移动要素时使用） */
public final class ElementResourceOrderItem {

    private String id = "";
    private int order;

    public ElementResourceOrderItem() {
    }

    /**
     * 构造 ElementResourceOrderItem。
     *
     * @param id    要素 UUID
     * @param order 排序序号
     */
    public ElementResourceOrderItem(String id, int order) {
        this.id = id != null ? id : "";
        this.order = order;
    }

    /** 要素 UUID */
    public String getId() { return id; }

    /** 要素 UUID */
    public void setId(String id) {
        this.id = id;
    }

    /** 排序序号 */
    public int getOrder() { return order; }

    /** 排序序号 */
    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElementResourceOrderItem that = (ElementResourceOrderItem) o;
        return order == that.order && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, order);
    }

    @Override
    public String toString() {
        return "ElementResourceOrderItem{"
                + "id='" + id + '\''
                + ", order=" + order
                + '}';
    }
}
