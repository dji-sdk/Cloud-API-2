// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 设置要素显示状态请求参数（PUT /pf-map/api/v1/elements/{id}） */
public final class ElementSetVisibilityParams {

    private String id = "";
    private int display;

    public ElementSetVisibilityParams() {
    }

    /**
     * 构造 ElementSetVisibilityParams。
     *
     * @param id      要素 UUID，必填
     * @param display 显示状态：1=显示 0=隐藏，必填
     */
    public ElementSetVisibilityParams(String id, int display) {
        this.id = id != null ? id : "";
        this.display = display;
    }

    /** 要素 UUID，必填 */
    public String getId() { return id; }

    /** 要素 UUID，必填 */
    public void setId(String id) {
        this.id = id;
    }

    /** 显示状态：1=显示 0=隐藏，必填 */
    public int getDisplay() { return display; }

    /** 显示状态：1=显示 0=隐藏，必填 */
    public void setDisplay(int display) {
        this.display = display;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ElementSetVisibilityParams that = (ElementSetVisibilityParams) o;
        return display == that.display && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, display);
    }

    @Override
    public String toString() {
        return "ElementSetVisibilityParams{"
                + "id='" + id + '\''
                + ", display=" + display
                + '}';
    }
}
