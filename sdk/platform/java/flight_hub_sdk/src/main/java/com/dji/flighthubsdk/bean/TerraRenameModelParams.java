// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/**
 * 模型重命名请求参数
 * 对应接口: PUT /model/api/v1/files/{id}
 */
public final class TerraRenameModelParams {

    /** 新名称（不允许含 /），必填 */
    private String name;
    /** 等高模式（可选，null 表示不修改） */
    private Boolean inUniformHeight;
    /** 模型显示模式（可选，null 表示不修改）：1-普通 2-环绕 3-线框 */
    private Integer modelDisplayMode;
    /** 采集时间（毫秒时间戳）；仅当 setCollectedAt() 被调用后（collectedAtSet==true）才会传递给服务端 */
    private long collectedAt;
    /** 是否传 collectedAt */
    private boolean collectedAtSet;

    public TerraRenameModelParams() {
    }

    /**
     * @param name 新文件名，必填，不允许含 /
     */
    public TerraRenameModelParams(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Boolean getInUniformHeight() { return inUniformHeight; }
    public void setInUniformHeight(Boolean inUniformHeight) { this.inUniformHeight = inUniformHeight; }

    public Integer getModelDisplayMode() { return modelDisplayMode; }
    public void setModelDisplayMode(Integer modelDisplayMode) { this.modelDisplayMode = modelDisplayMode; }

    public long getCollectedAt() { return collectedAt; }
    public boolean isCollectedAtSet() { return collectedAtSet; }
    /** 调用此方法后 collectedAt 才会被序列化到请求中 */
    public void setCollectedAt(long collectedAt) { this.collectedAt = collectedAt; this.collectedAtSet = true; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TerraRenameModelParams that = (TerraRenameModelParams) o;
        return collectedAt == that.collectedAt && collectedAtSet == that.collectedAtSet
                && Objects.equals(name, that.name)
                && Objects.equals(inUniformHeight, that.inUniformHeight)
                && Objects.equals(modelDisplayMode, that.modelDisplayMode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, inUniformHeight, modelDisplayMode, collectedAt, collectedAtSet);
    }

    @Override
    public String toString() {
        return "TerraRenameModelParams{"
                + "name='" + name + '\''
                + ", inUniformHeight=" + inUniformHeight
                + ", modelDisplayMode=" + modelDisplayMode
                + ", collectedAt=" + (collectedAtSet ? collectedAt : "unset")
                + '}';
    }
}
