// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 更新要素返回结果 */
public final class ElementUpdateResult {

    private String id = "";

    public ElementUpdateResult() {
    }

    /**
     * 构造 ElementUpdateResult。
     *
     * @param id 被更新的要素 UUID
     */
    public ElementUpdateResult(String id) {
        this.id = id != null ? id : "";
    }

    /** 被更新的要素 UUID */
    public String getId() { return id; }

    /** 被更新的要素 UUID */
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
        ElementUpdateResult that = (ElementUpdateResult) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ElementUpdateResult{"
                + "id='" + id + '\''
                + '}';
    }
}
