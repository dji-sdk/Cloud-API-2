// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 批量加载/取消加载模型到地图请求参数
 * 对应接口: PUT /model/api/v1/files
 */
public final class TerraShowOnMapParams {

    /** 指定文件 ID 列表（与 selectFilter 二选一） */
    private List<Integer> ids = new ArrayList<>();
    /** true=加载，false=取消加载（可选，null 表示不传） */
    private Boolean showOnMap;
    /** 是否已分发（可选，null 表示不传） */
    private Boolean isDistributed;
    /** 全选过滤条件（可选，null 表示不传） */
    private TerraSelectFilterParams selectFilter;

    public TerraShowOnMapParams() {
    }

    /**
     * @param ids       要操作的文件 ID 列表
     * @param showOnMap true=加载，false=取消加载
     */
    public TerraShowOnMapParams(List<Integer> ids, boolean showOnMap) {
        this.ids = ids != null ? ids : new ArrayList<>();
        this.showOnMap = showOnMap;
    }

    public List<Integer> getIds() { return ids; }
    public void setIds(List<Integer> ids) { this.ids = ids != null ? ids : new ArrayList<>(); }

    public Boolean getShowOnMap() { return showOnMap; }
    public void setShowOnMap(Boolean showOnMap) { this.showOnMap = showOnMap; }

    public Boolean getIsDistributed() { return isDistributed; }
    public void setIsDistributed(Boolean isDistributed) { this.isDistributed = isDistributed; }

    public TerraSelectFilterParams getSelectFilter() { return selectFilter; }
    public void setSelectFilter(TerraSelectFilterParams selectFilter) { this.selectFilter = selectFilter; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TerraShowOnMapParams that = (TerraShowOnMapParams) o;
        return Objects.equals(ids, that.ids)
                && Objects.equals(showOnMap, that.showOnMap)
                && Objects.equals(isDistributed, that.isDistributed)
                && Objects.equals(selectFilter, that.selectFilter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ids, showOnMap, isDistributed, selectFilter);
    }

    @Override
    public String toString() {
        return "TerraShowOnMapParams{"
                + "ids=" + ids
                + ", showOnMap=" + showOnMap
                + ", isDistributed=" + isDistributed
                + ", selectFilter=" + selectFilter
                + '}';
    }
}
