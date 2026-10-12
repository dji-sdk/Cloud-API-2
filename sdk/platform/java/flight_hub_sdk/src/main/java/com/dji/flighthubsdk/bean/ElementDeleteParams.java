// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 删除要素请求参数（DELETE /pf-map/api/v1/elements/delete/{id}） */
public final class ElementDeleteParams {

    private String id = "";
    private boolean fromBatchDelete;

    public ElementDeleteParams() {
    }

    /**
     * 构造 ElementDeleteParams。
     *
     * @param id 要素 UUID，必填
     */
    public ElementDeleteParams(String id) {
        this.id = id != null ? id : "";
    }

    /** 要素 UUID，必填 */
    public String getId() { return id; }

    /** 要素 UUID，必填 */
    public void setId(String id) {
        this.id = id;
    }

    /** 是否来自批量删除；true 时跳过单条分发通知 */
    public boolean isFromBatchDelete() { return fromBatchDelete; }

    /** 是否来自批量删除；true 时跳过单条分发通知 */
    public void setFromBatchDelete(boolean fromBatchDelete) {
        this.fromBatchDelete = fromBatchDelete;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ElementDeleteParams that = (ElementDeleteParams) o;
        return fromBatchDelete == that.fromBatchDelete && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fromBatchDelete);
    }

    @Override
    public String toString() {
        return "ElementDeleteParams{"
                + "id='" + id + '\''
                + ", fromBatchDelete=" + fromBatchDelete
                + '}';
    }
}
