// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 创建要素返回结果 */
public final class ElementCreateResult {

    private String id = "";

    public ElementCreateResult() {
    }

    /**
     * 构造 ElementCreateResult。
     *
     * @param id 新建要素 UUID
     */
    public ElementCreateResult(String id) {
        this.id = id != null ? id : "";
    }

    /** 新建要素 UUID */
    public String getId() { return id; }

    /** 新建要素 UUID */
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
        ElementCreateResult that = (ElementCreateResult) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ElementCreateResult{"
                + "id='" + id + '\''
                + '}';
    }
}
