// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 设置要素显示状态返回结果 */
public final class ElementSetVisibilityResult {

    private String id = "";

    public ElementSetVisibilityResult() {
    }

    /**
     * 构造 ElementSetVisibilityResult。
     *
     * @param id 被操作的要素 UUID
     */
    public ElementSetVisibilityResult(String id) {
        this.id = id != null ? id : "";
    }

    /** 被操作的要素 UUID */
    public String getId() { return id; }

    /** 被操作的要素 UUID */
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
        ElementSetVisibilityResult that = (ElementSetVisibilityResult) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ElementSetVisibilityResult{"
                + "id='" + id + '\''
                + '}';
    }
}
