// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 全选反选场景的筛选条件（加载/取消加载模型时使用）
 * 对应 proto SelectFilter.selected_parent_id (uint32 → int)
 */
public final class TerraSelectFilterParams {

    /** 选中范围的父目录 ID (uint32)；仅当 setSelectedParentId() 被调用后才会传递给服务端 */
    private int selectedParentId;
    /** 是否传 selectedParentId */
    private boolean selectedParentIdSet;
    /** 排除的文件 ID 列表 */
    private List<Integer> exceptIds = new ArrayList<>();
    /** 关键字模糊搜索 */
    private String key = "";

    public TerraSelectFilterParams() {
    }

    public int getSelectedParentId() { return selectedParentId; }
    public boolean isSelectedParentIdSet() { return selectedParentIdSet; }
    /** 调用此方法后 selectedParentId 才会被序列化到请求中 */
    public void setSelectedParentId(int selectedParentId) {
        this.selectedParentId = selectedParentId;
        this.selectedParentIdSet = true;
    }

    public List<Integer> getExceptIds() { return exceptIds; }
    public void setExceptIds(List<Integer> exceptIds) { this.exceptIds = exceptIds != null ? exceptIds : new ArrayList<>(); }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key != null ? key : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TerraSelectFilterParams that = (TerraSelectFilterParams) o;
        return selectedParentId == that.selectedParentId && selectedParentIdSet == that.selectedParentIdSet
                && Objects.equals(exceptIds, that.exceptIds)
                && Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(selectedParentId, selectedParentIdSet, exceptIds, key);
    }

    @Override
    public String toString() {
        return "TerraSelectFilterParams{"
                + "selectedParentId=" + (selectedParentIdSet ? selectedParentId : "unset")
                + ", exceptIds=" + exceptIds
                + ", key='" + key + '\''
                + '}';
    }
}
