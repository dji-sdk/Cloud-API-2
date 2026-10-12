// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 删除要素返回结果 */
public final class ElementDeleteResult {

    private String id = "";

    public ElementDeleteResult() {
    }

    /**
     * 构造 ElementDeleteResult。
     *
     * @param id 被删除的要素 UUID
     */
    public ElementDeleteResult(String id) {
        this.id = id != null ? id : "";
    }

    /** 被删除的要素 UUID */
    public String getId() { return id; }

    /** 被删除的要素 UUID */
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ElementDeleteResult that = (ElementDeleteResult) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ElementDeleteResult{"
                + "id='" + id + '\''
                + '}';
    }
}
