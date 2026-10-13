// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 设置等温线颜色渲染类型参数 */
public class SetIsothermColorRenderingTypeParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    /** 着色类型（0=保留 1=聚焦显色 2=边界警示 3=全域自适应） */
    private int type = 0;

    public SetIsothermColorRenderingTypeParams() {
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }
    /** 着色类型（0=保留 1=聚焦显色 2=边界警示 3=全域自适应） */
    public int getType() { return type; }
    /** @param type 着色类型（0=保留 1=聚焦显色 2=边界警示 3=全域自适应） */
    public void setType(int type) { this.type = type; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SetIsothermColorRenderingTypeParams that = (SetIsothermColorRenderingTypeParams) o;
        return Objects.equals(payloadIndex, that.payloadIndex)
                && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, type);
    }

    @Override
    public String toString() {
        return "SetIsothermColorRenderingTypeParams{payloadIndex='" + payloadIndex + '\'' + ", type=" + type + '}';
    }
}
